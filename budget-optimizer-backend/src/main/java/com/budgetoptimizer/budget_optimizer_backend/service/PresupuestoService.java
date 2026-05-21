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

        log.info("Creando presupuesto para usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException(usuarioId));

        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio"
            );
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

        if (dto.getLimitesCategorias() != null &&
                !dto.getLimitesCategorias().isEmpty()) {

            crearLimitesCategorias(
                    presupuesto,
                    dto.getLimitesCategorias()
            );
        }

        log.info("Presupuesto creado con ID: {}", presupuesto.getId());

        return convertirAResponse(presupuesto);
    }

    private void crearLimitesCategorias(
            Presupuesto presupuesto,
            Map<Long, BigDecimal> limites
    ) {

        BigDecimal sumaLimites = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : limites.entrySet()) {

            Long categoriaId = entry.getKey();
            BigDecimal monto = entry.getValue();

            Categoria categoria = categoriaRepo.findById(categoriaId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Categoría no encontrada: " + categoriaId
                            ));

            CategoryLimit limite = new CategoryLimit();

            limite.setPresupuesto(presupuesto);
            limite.setCategoria(categoria);

            // ⚠️ AJUSTAR SEGÚN TU ENTITY
            limite.setLimiteAsignado(monto.doubleValue());
            limite.setGastoActual(0.0);

            categoryLimitRepo.save(limite);

            sumaLimites = sumaLimites.add(monto);
        }

        if (sumaLimites.compareTo(presupuesto.getMontoTotal()) > 0) {

            throw new IllegalArgumentException(
                    "La suma de límites excede el presupuesto total"
            );
        }

        log.info(
                "Se crearon {} límites para presupuesto ID: {}",
                limites.size(),
                presupuesto.getId()
        );
    }

    // ==========================================
    // CONSULTAS
    // ==========================================

    @Transactional(readOnly = true)
    public PresupuestoResponseDTO buscarPorId(Long id) {

        log.info("Buscando presupuesto ID: {}", id);

        Presupuesto presupuesto = presupuestoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Presupuesto no encontrado: " + id
                        ));

        return convertirAResponse(presupuesto);
    }

    @Transactional(readOnly = true)
    public List<PresupuestoResponseDTO> listarPorUsuario(Long usuarioId) {

        log.info("Listando presupuestos del usuario ID: {}", usuarioId);

        return presupuestoRepo
                .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PresupuestoResponseDTO> listarPorUsuarioYEstado(
            Long usuarioId,
            BudgetStatus status
    ) {

        log.info(
                "Listando presupuestos del usuario {} con estado {}",
                usuarioId,
                status
        );

        return presupuestoRepo
                .findByUsuarioIdAndStatus(usuarioId, status)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PresupuestoResponseDTO buscarPresupuestoActual(Long usuarioId) {

        log.info(
                "Buscando presupuesto activo actual del usuario ID: {}",
                usuarioId
        );

        Presupuesto presupuesto = presupuestoRepo
                .findPresupuestoActivoActual(
                        usuarioId,
                        BudgetStatus.ACTIVE,
                        LocalDateTime.now()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No hay presupuesto activo actualmente"
                        ));

        return convertirAResponse(presupuesto);
    }

    // ==========================================
    // ACTUALIZACIÓN
    // ==========================================

    public PresupuestoResponseDTO actualizarPresupuesto(
            Long id,
            ActualizarPresupuestoDTO dto
    ) {

        log.info("Actualizando presupuesto ID: {}", id);

        Presupuesto presupuesto = presupuestoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Presupuesto no encontrado: " + id
                        ));

        if (!presupuesto.getStatus().getPuedeEditar()) {

            throw new IllegalStateException(
                    "No se puede editar un presupuesto en estado "
                            + presupuesto.getStatus()
            );
        }

        if (dto.getNombre() != null) {
            presupuesto.setNombre(dto.getNombre());
        }

        if (dto.getMontoTotal() != null) {
            presupuesto.setMontoTotal(dto.getMontoTotal());
        }

        if (dto.getFechaInicio() != null) {
            presupuesto.setFechaInicio(dto.getFechaInicio());
        }

        if (dto.getFechaFin() != null) {
            presupuesto.setFechaFin(dto.getFechaFin());
        }

        if (dto.getPeriodo() != null) {
            presupuesto.setPeriodo(dto.getPeriodo());
        }

        presupuesto = presupuestoRepo.save(presupuesto);

        log.info("Presupuesto actualizado: {}", id);

        return convertirAResponse(presupuesto);
    }

    // ==========================================
    // ESTADOS
    // ==========================================

    public PresupuestoResponseDTO activarPresupuesto(Long id) {

        Presupuesto presupuesto = presupuestoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Presupuesto no encontrado"
                        ));

        if (!presupuesto.getStatus().puedeActivarse()) {

            throw new IllegalStateException(
                    "No se puede activar desde el estado "
                            + presupuesto.getStatus()
            );
        }

        presupuesto.setStatus(BudgetStatus.ACTIVE);

        presupuesto = presupuestoRepo.save(presupuesto);

        log.info("Presupuesto activado: {}", id);

        return convertirAResponse(presupuesto);
    }

    public PresupuestoResponseDTO pausarPresupuesto(Long id) {

        Presupuesto presupuesto = presupuestoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Presupuesto no encontrado"
                        ));

        if (!presupuesto.getStatus().puedePausarse()) {

            throw new IllegalStateException(
                    "No se puede pausar desde el estado "
                            + presupuesto.getStatus()
            );
        }

        presupuesto.setStatus(BudgetStatus.PAUSED);

        presupuesto = presupuestoRepo.save(presupuesto);

        log.info("Presupuesto pausado: {}", id);

        return convertirAResponse(presupuesto);
    }

    public PresupuestoResponseDTO completarPresupuesto(Long id) {

        Presupuesto presupuesto = presupuestoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Presupuesto no encontrado"
                        ));

        presupuesto.setStatus(BudgetStatus.COMPLETED);

        presupuesto = presupuestoRepo.save(presupuesto);

        log.info("Presupuesto completado: {}", id);

        return convertirAResponse(presupuesto);
    }

    public void eliminarPresupuesto(Long id) {

        log.info("Eliminando presupuesto ID: {}", id);

        Presupuesto presupuesto = presupuestoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Presupuesto no encontrado"
                        ));

        if (presupuesto.getStatus() != BudgetStatus.DRAFT &&
                presupuesto.getExpenses() != null &&
                !presupuesto.getExpenses().isEmpty()) {

            throw new IllegalStateException(
                    "No se puede eliminar un presupuesto con gastos"
            );
        }

        presupuestoRepo.delete(presupuesto);

        log.info("Presupuesto eliminado: {}", id);
    }

    // ==========================================
    // ESTADÍSTICAS
    // ==========================================

    @Transactional(readOnly = true)
    public EstadisticasPresupuestoDTO obtenerEstadisticas(
            Long usuarioId
    ) {

        List<Presupuesto> presupuestos =
                presupuestoRepo.findByUsuarioId(usuarioId);

        if (presupuestos.isEmpty()) {

            return EstadisticasPresupuestoDTO.builder()
                    .totalPresupuestos(0)
                    .mensaje("No hay presupuestos registrados")
                    .build();
        }

        BigDecimal totalPresupuestado = presupuestos.stream()
                .map(Presupuesto::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastado = presupuestos.stream()
                .map(this::calcularGastoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRestante =
                totalPresupuestado.subtract(totalGastado);

        Double porcentajeGasto =
                totalPresupuestado.compareTo(BigDecimal.ZERO) > 0
                        ? totalGastado
                        .divide(
                                totalPresupuestado,
                                4,
                                RoundingMode.HALF_UP
                        )
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                        : 0.0;

        long activos = presupuestos.stream()
                .filter(p -> p.getStatus() == BudgetStatus.ACTIVE)
                .count();

        long completados = presupuestos.stream()
                .filter(p -> p.getStatus() == BudgetStatus.COMPLETED)
                .count();

        long excedidos = presupuestos.stream()
                .filter(p -> p.getStatus() == BudgetStatus.EXCEEDED)
                .count();

        return EstadisticasPresupuestoDTO.builder()
                .totalPresupuestos(presupuestos.size())
                .presupuestosActivos((int) activos)
                .presupuestosCompletados((int) completados)
                .presupuestosExcedidos((int) excedidos)
                .totalPresupuestado(totalPresupuestado)
                .totalGastado(totalGastado)
                .totalRestante(totalRestante)
                .porcentajeGastoGeneral(porcentajeGasto)
                .mensaje("Estadísticas generadas exitosamente")
                .build();
    }

    // ==========================================
    // AUXILIARES
    // ==========================================

    private PresupuestoResponseDTO convertirAResponse(
            Presupuesto presupuesto
    ) {

        BudgetStatus status = presupuesto.getStatus();

        BigDecimal gastoTotal =
                calcularGastoTotal(presupuesto);

        BigDecimal restante =
                presupuesto.getMontoTotal()
                        .subtract(gastoTotal);

        Double porcentaje =
                presupuesto.getMontoTotal().compareTo(BigDecimal.ZERO) > 0
                        ? gastoTotal
                        .divide(
                                presupuesto.getMontoTotal(),
                                4,
                                RoundingMode.HALF_UP
                        )
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                        : 0.0;

        List<PresupuestoResponseDTO.LimiteCategoriaDTO> limites =
                presupuesto.getLimitesCategorias()
                        .stream()
                        .map(this::convertirLimiteADTO)
                        .collect(Collectors.toList());

        return PresupuestoResponseDTO.builder()
                .id(presupuesto.getId())
                .usuarioId(presupuesto.getUsuario().getId())
                .usuarioNombre(presupuesto.getUsuario().getNombre())
                .nombre(presupuesto.getNombre())
                .montoTotal(presupuesto.getMontoTotal())
                .montoGastado(gastoTotal)
                .montoRestante(restante)
                .porcentajeGastado(porcentaje)
                .status(status)
                .statusDisplay(status.getDisplayName())
                .statusColor(status.getColorHex())
                .periodo(presupuesto.getPeriodo())
                .periodoDisplay(
                        presupuesto.getPeriodo().getDisplayName()
                )
                .duracionDias(
                        presupuesto.getPeriodo().getDuracionDias()
                )
                .fechaInicio(presupuesto.getFechaInicio())
                .fechaFin(presupuesto.getFechaFin())
                .fechaCreacion(presupuesto.getFechaCreacion())
                .totalGastos(
                        presupuesto.getExpenses() != null
                                ? presupuesto.getExpenses().size()
                                : 0
                )
                .totalCategorias(limites.size())
                .estaVigente(estaVigente(presupuesto))
                .estaCercaDelLimite(porcentaje >= 80.0)
                .estaExcedido(
                        gastoTotal.compareTo(
                                presupuesto.getMontoTotal()
                        ) > 0
                )
                .puedeEditar(status.getPuedeEditar())
                .puedeRegistrarGastos(
                        status.getPuedeRegistrarGastos()
                )
                .limitesCategorias(limites)
                .build();
    }

    private PresupuestoResponseDTO.LimiteCategoriaDTO convertirLimiteADTO(
            CategoryLimit limite
    ) {

        BigDecimal limiteAsignado =
                BigDecimal.valueOf(limite.getLimiteAsignado());

        BigDecimal gastoActual =
                BigDecimal.valueOf(limite.getGastoActual());

        BigDecimal restante =
                limiteAsignado.subtract(gastoActual);

        Double porcentaje =
                limite.getLimiteAsignado() > 0
                        ? (limite.getGastoActual()
                        / limite.getLimiteAsignado()) * 100
                        : 0.0;

        return PresupuestoResponseDTO
                .LimiteCategoriaDTO
                .builder()
                .categoriaId(limite.getCategoria().getId())
                .categoriaNombre(
                        limite.getCategoria().getNombre()
                )
                .categoriaIcono(
                        limite.getCategoria().getIcono()
                )
                .limiteAsignado(limiteAsignado)
                .gastoActual(gastoActual)
                .restante(restante)
                .porcentajeUtilizado(porcentaje)
                .estaCercaDelLimite(porcentaje >= 80.0)
                .excedido(
                        gastoActual.compareTo(limiteAsignado) > 0
                )
                .build();
    }

    private BigDecimal calcularGastoTotal(Presupuesto presupuesto) {

        if (presupuesto.getExpenses() == null) {
            return BigDecimal.ZERO;
        }

        return presupuesto.getExpenses()
                .stream()
                .map(Expense::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean estaVigente(Presupuesto presupuesto) {

        LocalDateTime ahora = LocalDateTime.now();

        return ahora.isAfter(presupuesto.getFechaInicio())
                && ahora.isBefore(presupuesto.getFechaFin());
    }
}