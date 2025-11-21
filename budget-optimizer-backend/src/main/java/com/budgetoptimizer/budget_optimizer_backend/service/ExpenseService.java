package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.expense.ExpenseDTO;
import com.budgetoptimizer.budget_optimizer_backend.dto.expense.ExpenseResponseDTO;
import com.budgetoptimizer.budget_optimizer_backend.exception.ResourceNotFoundException;
import com.budgetoptimizer.budget_optimizer_backend.model.Categoria;
import com.budgetoptimizer.budget_optimizer_backend.model.Expense;
import com.budgetoptimizer.budget_optimizer_backend.model.Presupuesto;
import com.budgetoptimizer.budget_optimizer_backend.model.Usuario;
import com.budgetoptimizer.budget_optimizer_backend.repository.CategoriaRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.ExpenseRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.PresupuestoRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseDTO dto) {
        // Validar que el presupuesto existe
        Presupuesto presupuesto = presupuestoRepository.findById(dto.getPresupuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + dto.getPresupuestoId()));

        // Validar que la categoría existe y está activa
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoriaId()));

        if (!categoria.isActiva()) {
            throw new IllegalArgumentException("La categoría está inactiva");
        }

        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        // Validar que el presupuesto pertenece al usuario
        if (!presupuesto.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("El presupuesto no pertenece al usuario");
        }

        // Crear el gasto
        Expense expense = Expense.builder()
                .monto(dto.getMonto())
                .descripcion(dto.getDescripcion())
                .fechaGasto(dto.getFechaGasto())
                .presupuesto(presupuesto)
                .categoria(categoria)
                .usuario(usuario)
                .build();

        Expense saved = expenseRepository.save(expense);

        // Actualizar el gasto actual del presupuesto
        BigDecimal nuevoGastoActual = presupuesto.getGastoActual().add(dto.getMonto());
        presupuesto.setGastoActual(nuevoGastoActual);
        presupuestoRepository.save(presupuesto);

        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByPresupuesto(Long presupuestoId) {
        if (!presupuestoRepository.existsById(presupuestoId)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId);
        }

        return expenseRepository.findByPresupuestoId(presupuestoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId);
        }

        return expenseRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByUsuarioAndDateRange(Long usuarioId, LocalDateTime inicio, LocalDateTime fin) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId);
        }

        return expenseRepository.findByUsuarioIdAndFechaGastoBetween(usuarioId, inicio, fin)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado con ID: " + id));
        return mapToResponseDTO(expense);
    }

    @Transactional
    public ExpenseResponseDTO updateExpense(Long id, ExpenseDTO dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado con ID: " + id));

        BigDecimal montoAnterior = expense.getMonto();
        Presupuesto presupuestoAnterior = expense.getPresupuesto();

        // Validar nuevo presupuesto si cambió
        Presupuesto nuevoPresupuesto = presupuestoRepository.findById(dto.getPresupuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + dto.getPresupuestoId()));

        // Validar nueva categoría si cambió
        Categoria nuevaCategoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoriaId()));

        if (!nuevaCategoria.isActiva()) {
            throw new IllegalArgumentException("La categoría está inactiva");
        }

        // Actualizar el gasto
        expense.setMonto(dto.getMonto());
        expense.setDescripcion(dto.getDescripcion());
        expense.setFechaGasto(dto.getFechaGasto());
        expense.setPresupuesto(nuevoPresupuesto);
        expense.setCategoria(nuevaCategoria);

        Expense updated = expenseRepository.save(expense);

        // Actualizar gastos actuales de los presupuestos
        if (presupuestoAnterior.getId().equals(nuevoPresupuesto.getId())) {
            // Mismo presupuesto, solo ajustar la diferencia
            BigDecimal diferencia = dto.getMonto().subtract(montoAnterior);
            nuevoPresupuesto.setGastoActual(nuevoPresupuesto.getGastoActual().add(diferencia));
            presupuestoRepository.save(nuevoPresupuesto);
        } else {
            // Cambió de presupuesto
            // Restar del anterior
            presupuestoAnterior.setGastoActual(presupuestoAnterior.getGastoActual().subtract(montoAnterior));
            presupuestoRepository.save(presupuestoAnterior);

            // Sumar al nuevo
            nuevoPresupuesto.setGastoActual(nuevoPresupuesto.getGastoActual().add(dto.getMonto()));
            presupuestoRepository.save(nuevoPresupuesto);
        }

        return mapToResponseDTO(updated);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado con ID: " + id));

        // Actualizar el gasto actual del presupuesto (restar)
        Presupuesto presupuesto = expense.getPresupuesto();
        BigDecimal nuevoGastoActual = presupuesto.getGastoActual().subtract(expense.getMonto());
        presupuesto.setGastoActual(nuevoGastoActual);
        presupuestoRepository.save(presupuesto);

        // Eliminar el gasto
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByPresupuestoAndCategoria(Long presupuestoId, Long categoriaId) {
        if (!presupuestoRepository.existsById(presupuestoId)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId);
        }

        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + categoriaId);
        }

        return expenseRepository.findByPresupuestoIdAndCategoriaId(presupuestoId, categoriaId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ExpenseResponseDTO mapToResponseDTO(Expense expense) {
        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .monto(expense.getMonto())
                .descripcion(expense.getDescripcion())
                .fechaGasto(expense.getFechaGasto())
                .presupuestoId(expense.getPresupuesto().getId())
                .presupuestoNombre(expense.getPresupuesto().getNombre())
                .categoriaId(expense.getCategoria().getId())
                .categoriaNombre(expense.getCategoria().getNombre())
                .categoriaIcono(expense.getCategoria().getIcono())
                .categoriaColor(expense.getCategoria().getColor())
                .usuarioId(expense.getUsuario().getId())
                .usuarioNombre(expense.getUsuario().getNombre())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}