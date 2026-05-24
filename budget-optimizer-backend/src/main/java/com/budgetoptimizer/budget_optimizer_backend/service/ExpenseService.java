package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.expense.ExpenseDTO;
import com.budgetoptimizer.budget_optimizer_backend.dto.expense.ExpenseResponseDTO;
import com.budgetoptimizer.budget_optimizer_backend.enums.PaymentMethod;
import com.budgetoptimizer.budget_optimizer_backend.model.Categoria;
import com.budgetoptimizer.budget_optimizer_backend.model.Empresa;
import com.budgetoptimizer.budget_optimizer_backend.model.Expense;
import com.budgetoptimizer.budget_optimizer_backend.model.Presupuesto;
import com.budgetoptimizer.budget_optimizer_backend.model.Usuario;
import com.budgetoptimizer.budget_optimizer_backend.repository.CategoriaRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.EmpresaRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.ExpenseRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.PresupuestoRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final PresupuestoRepository presupuestoRepo;
    private final CategoriaRepository categoriaRepo;
    private final UsuarioRepository usuarioRepo;
    private final EmpresaRepository empresaRepo;

    // ==========================================
    // CREAR GASTO
    // ==========================================
    public ExpenseResponseDTO createExpense(ExpenseDTO dto) {

        Usuario usuario = usuarioRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Presupuesto presupuesto = presupuestoRepo.findById(dto.getPresupuestoId())
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        Categoria categoria = categoriaRepo.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Empresa empresa = null;

        if (dto.getEmpresaId() != null) {
            empresa = empresaRepo.findById(dto.getEmpresaId()).orElse(null);
        }

        Expense expense = Expense.builder()
                .usuario(usuario)
                .presupuesto(presupuesto)
                .categoria(categoria)
                .empresa(empresa)
                .descripcion(dto.getDescripcion())
                .monto(dto.getMonto())
                .fechaGasto(dto.getFechaGasto() != null ? dto.getFechaGasto() : LocalDateTime.now())
                .metodoPago(dto.getMetodoPago() != null ? dto.getMetodoPago() : PaymentMethod.CASH)
                .notas(dto.getNotas())
                .build();

        return convertirAResponse(expenseRepo.save(expense));
    }

    // ==========================================
    // GASTOS POR USUARIO
    // ==========================================
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByUsuario(Long usuarioId) {
        return expenseRepo.findByUsuario_IdOrderByFechaGastoDesc(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GASTOS POR PRESUPUESTO
    // ==========================================
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByPresupuesto(Long presupuestoId) {
        return expenseRepo.findByPresupuesto_IdOrderByFechaGastoDesc(presupuestoId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GASTOS POR USUARIO Y RANGO DE FECHAS
    // ==========================================
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByUsuarioAndDateRange(
            Long usuarioId,
            LocalDateTime start,
            LocalDateTime end) {

        return expenseRepo.findByUsuario_IdAndFechaGastoBetweenOrderByFechaGastoDesc(
                        usuarioId, start, end)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GASTOS POR PRESUPUESTO Y CATEGORÍA
    // ==========================================
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByPresupuestoAndCategoria(
            Long presupuestoId,
            Long categoriaId) {

        return expenseRepo.findByPresupuesto_IdAndCategoria_IdOrderByFechaGastoDesc(
                        presupuestoId, categoriaId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Long id) {
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        return convertirAResponse(expense);
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================
    public ExpenseResponseDTO updateExpense(Long id, ExpenseDTO dto) {

        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        Categoria categoria = categoriaRepo.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Empresa empresa = null;

        if (dto.getEmpresaId() != null) {
            empresa = empresaRepo.findById(dto.getEmpresaId()).orElse(null);
        }

        expense.setDescripcion(dto.getDescripcion());
        expense.setMonto(dto.getMonto());
        expense.setCategoria(categoria);
        expense.setEmpresa(empresa);
        expense.setFechaGasto(dto.getFechaGasto());
        expense.setMetodoPago(dto.getMetodoPago());
        expense.setNotas(dto.getNotas());

        return convertirAResponse(expenseRepo.save(expense));
    }

    // ==========================================
    // ELIMINAR
    // ==========================================
    public void deleteExpense(Long id) {
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        expenseRepo.delete(expense);
    }

    // ==========================================
    // MAPPER
    // ==========================================
    private ExpenseResponseDTO convertirAResponse(Expense expense) {
        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .descripcion(expense.getDescripcion())
                .monto(expense.getMonto())
                .fechaGasto(expense.getFechaGasto())
                .usuarioId(expense.getUsuario().getId())
                .usuarioNombre(expense.getUsuario().getNombre())
                .categoriaId(expense.getCategoria().getId())
                .categoriaNombre(expense.getCategoria().getNombre())
                .presupuestoId(expense.getPresupuesto().getId())
                .presupuestoNombre(expense.getPresupuesto().getNombre())
                .fechaCreacion(expense.getFechaCreacion())
                .build();
    }
}