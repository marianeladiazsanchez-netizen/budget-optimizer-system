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
    public PresupuestoResponseDTO crearPresupuesto(
            Long usuarioId,
            CrearPresupuestoDTO dto
    ) {

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

        if (dto.getLimitesCategorias() != null && !dto.getLimitesCategorias().isEmpty()) {
            crearLimitesCategorias(presupuesto, dto.getLimitesCategorias());
        }

        return convertirAResponse(presupuesto);
    }

    // ==========================================
    // LIMITES CATEGORÍAS
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

            // ⚠️ mantener coherencia con double si tu entity aún lo usa
            limit.setLimiteAsignado(monto.doubleValue());
            limit.setGastoActual(0.0);

            categoryLimitRepo.save(limit);

            sumaLimites = sumaLimites.add(monto);
        }

        if (sumaLimites.compareTo(presupuesto.getMontoTotal()) > 0) {
            throw new IllegalArgumentException("Límites exceden presupuesto");
        }
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
        return presupuestoRepo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // ==========================================
    // ESTADÍSTICAS
    // ==========================================
    public EstadisticasPresupuestoDTO obtenerEstadisticas(Long usuarioId) {

        List<Presupuesto> presupuestos = presupuestoRepo.findByUsuarioId(usuarioId);

        if (presupuestos.isEmpty()) {
            return EstadisticasPresupuestoDTO.builder()
                    .totalPresupuestos(0)
                    .mensaje("Sin datos")
                    .build();
        }

        BigDecimal totalPresupuestado = presupuestos.stream()
                .map(Presupuesto::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastado = presupuestos.stream()
                .map(this::calcularGastoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRestante = totalPresupuestado.subtract(totalGastado);

        Double porcentaje = totalPresupuestado.compareTo(BigDecimal.ZERO) > 0
                ? totalGastado
                .divide(totalPresupuestado, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return EstadisticasPresupuestoDTO.builder()
                .totalPresupuestos(presupuestos.size())
                .totalPresupuestado(totalPresupuestado)
                .totalGastado(totalGastado)
                .totalRestante(totalRestante)
                .porcentajeGastoGeneral(porcentaje)
                .build();
    }

    // ==========================================
    // MAPPER
    // ==========================================
    private PresupuestoResponseDTO convertirAResponse(Presupuesto presupuesto) {

        BigDecimal gastoTotal = calcularGastoTotal(presupuesto);
        BigDecimal total = presupuesto.getMontoTotal() != null
                ? presupuesto.getMontoTotal()
                : BigDecimal.ZERO;

        BigDecimal restante = total.subtract(gastoTotal);

        Double porcentaje = total.compareTo(BigDecimal.ZERO) > 0
                ? gastoTotal
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        List<PresupuestoResponseDTO.LimiteCategoriaDTO> limites =
                presupuesto.getLimitesCategorias() == null
                        ? List.of()
                        : presupuesto.getLimitesCategorias()
                        .stream()
                        .map(this::convertirLimiteADTO)
                        .toList();

        return PresupuestoResponseDTO.builder()
                .id(presupuesto.getId())
                .usuarioId(presupuesto.getUsuario().getId())
                .nombre(presupuesto.getNombre())
                .montoTotal(total)
                .montoGastado(gastoTotal)
                .montoRestante(restante)
                .porcentajeGastado(porcentaje)
                .status(presupuesto.getStatus())
                .fechaInicio(presupuesto.getFechaInicio())
                .fechaFin(presupuesto.getFechaFin())
                .totalGastos(presupuesto.getExpenses() != null ? presupuesto.getExpenses().size() : 0)
                .limitesCategorias(limites)
                .build();
    }

    // ==========================================
    // LIMITE DTO
    // ==========================================
    private PresupuestoResponseDTO.LimiteCategoriaDTO convertirLimiteADTO(CategoryLimit limite) {

        BigDecimal asignado = BigDecimal.valueOf(limite.getLimiteAsignado());
        BigDecimal gastado = BigDecimal.valueOf(limite.getGastoActual());

        BigDecimal restante = asignado.subtract(gastado);

        Double porcentaje = limite.getLimiteAsignado() > 0
                ? gastado.divide(asignado, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return PresupuestoResponseDTO.LimiteCategoriaDTO.builder()
                .categoriaId(limite.getCategoria().getId())
                .categoriaNombre(limite.getCategoria().getNombre())
                .limiteAsignado(asignado)
                .gastoActual(gastado)
                .restante(restante)
                .porcentajeUtilizado(porcentaje)
                .build();
    }

    // ==========================================
    // AUX
    // ==========================================
    private BigDecimal calcularGastoTotal(Presupuesto presupuesto) {
        if (presupuesto.getExpenses() == null) return BigDecimal.ZERO;

        return presupuesto.getExpenses()
                .stream()
                .map(Expense::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}