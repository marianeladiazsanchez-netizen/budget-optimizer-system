package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.presupuesto.*;
import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetStatus;
import com.budgetoptimizer.budget_optimizer_backend.exception.UsuarioNoEncontradoException;
import com.budgetoptimizer.budget_optimizer_backend.model.*;
import com.budgetoptimizer.budget_optimizer_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepo;
    private final UsuarioRepository usuarioRepo;
    private final CategoriaRepository categoriaRepo;
    private final CategoryLimitRepository categoryLimitRepo;

    // ==========================================
    // CREACIÓN
    // ==========================================
    public PresupuestoResponseDTO crearPresupuesto(Long usuarioId, CrearPresupuestoDTO dto) {

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException("Fecha fin inválida");
        }

        Presupuesto presupuesto = Presupuesto.builder()
                .usuario(usuario)
                .nombre(dto.getNombre())
                .montoTotal(dto.getMontoTotal())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .periodo(dto.getPeriodo())
                .status(BudgetStatus.DRAFT)
                .build();

        presupuesto = presupuestoRepo.save(presupuesto);

        if (dto.getLimitesCategorias() != null) {
            crearLimitesCategorias(presupuesto, dto.getLimitesCategorias());
        }

        return convertirAResponse(presupuesto);
    }

    // ==========================================
    // MÉTODOS FALTANTES (DEL CONTROLLER)
    // ==========================================

    public List<PresupuestoResponseDTO> listarPorUsuarioYEstado(Long usuarioId, BudgetStatus status) {
    return presupuestoRepo.findByUsuario_ IdAndStatus(usuarioId, status)
            .stream()
            .map(this::convertirAResponse)
            .toList();
        }

public PresupuestoResponseDTO buscarPresupuestoActual(Long usuarioId) {
    return convertirAResponse(
            presupuestoRepo.findFirstByUsuario_IdAndStatusOrderByFechaCreacionDesc(usuarioId, BudgetStatus.ACTIVE)
            .orElseThrow(() -> new RuntimeException("No hay presupuesto activo"))
        );
}

public PresupuestoResponseDTO actualizarPresupuesto(Long id, ActualizarPresupuestoDTO dto) {
    Presupuesto p = presupuestoRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("No encontrado"));

    p.setNombre(dto.getNombre());
    p.setMontoTotal(dto.getMontoTotal());
    p.setFechaInicio(dto.getFechaInicio());
    p.setFechaFin(dto.getFechaFin());

    return convertirAResponse(presupuestoRepo.save(p));
}

public PresupuestoResponseDTO activarPresupuesto(Long id) {
    Presupuesto p = presupuestoRepo.findById(id).orElseThrow();
    p.setStatus(BudgetStatus.ACTIVE);
    return convertirAResponse(presupuestoRepo.save(p));
}

public PresupuestoResponseDTO pausarPresupuesto(Long id) {
    Presupuesto p = presupuestoRepo.findById(id).orElseThrow();
    p.setStatus(BudgetStatus.PAUSED);
    return convertirAResponse(presupuestoRepo.save(p));
}

public PresupuestoResponseDTO completarPresupuesto(Long id) {
    Presupuesto p = presupuestoRepo.findById(id).orElseThrow();
    p.setStatus(BudgetStatus.COMPLETED);
    return convertirAResponse(presupuestoRepo.save(p));
}

public void eliminarPresupuesto(Long id) {
    presupuestoRepo.deleteById(id);
}

    // ==========================================
    // CONSULTAS
    // ==========================================
    public PresupuestoResponseDTO buscarPorId(Long id) {
        return convertirAResponse(
                presupuestoRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("No encontrado"))
        );
    }

    public List<PresupuestoResponseDTO> listarPorUsuario(Long usuarioId) {
        return presupuestoRepo.findByUsuario_IdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // ==========================================
    // ESTADÍSTICAS
    // ==========================================
    public EstadisticasPresupuestoDTO obtenerEstadisticas(Long usuarioId) {

        List<Presupuesto> presupuestos = presupuestoRepo.findByUsuario_Id(usuarioId);

        BigDecimal total = presupuestos.stream()
                .map(Presupuesto::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gastado = presupuestos.stream()
                .map(this::calcularGastoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal restante = total.subtract(gastado);

        double porcentaje = total.compareTo(BigDecimal.ZERO) > 0
                ? gastado.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return EstadisticasPresupuestoDTO.builder()
                .totalPresupuestos(presupuestos.size())
                .totalPresupuestado(total)
                .totalGastado(gastado)
                .totalRestante(restante)
                .porcentajeGastoGeneral(porcentaje)
                .build();
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private void crearLimitesCategorias(
        Presupuesto presupuesto,
        Map<Long, BigDecimal> limites
) {

    BigDecimal sumaLimites = BigDecimal.ZERO;

    for (var entry : limites.entrySet()) {

        Categoria categoria = categoriaRepo.findById(entry.getKey())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        BigDecimal monto = entry.getValue();

        CategoryLimit limit = new CategoryLimit();
        limit.setPresupuesto(presupuesto);
        limit.setCategoria(categoria);

        limit.setLimiteAsignado(monto);   
        limit.setGastoActual(BigDecimal.ZERO);

        categoryLimitRepo.save(limit);

        sumaLimites = sumaLimites.add(monto);
    }

    if (sumaLimites.compareTo(presupuesto.getMontoTotal()) > 0) {
        throw new IllegalArgumentException("Límites exceden presupuesto");
    }
}

    private PresupuestoResponseDTO convertirAResponse(Presupuesto p) {

        BigDecimal gastado = calcularGastoTotal(p);
        BigDecimal total = p.getMontoTotal() != null ? p.getMontoTotal() : BigDecimal.ZERO;

        BigDecimal restante = total.subtract(gastado);

        double porcentaje = total.compareTo(BigDecimal.ZERO) > 0
        ? gastado.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
        : 0.0;

        return PresupuestoResponseDTO.builder()
                .id(p.getId())
                .usuarioId(p.getUsuario().getId())
                .nombre(p.getNombre())
                .montoTotal(total)
                .montoGastado(gastado)
                .montoRestante(restante)
                .porcentajeGastado(porcentaje)
                .status(p.getStatus())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .totalGastos(p.getExpenses() != null ? p.getExpenses().size() : 0)
                .build();
    }

    private BigDecimal calcularGastoTotal(Presupuesto p) {
        if (p.getExpenses() == null) return BigDecimal.ZERO;

        return p.getExpenses()
                .stream()
                .map(Expense::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}