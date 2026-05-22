package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.ml.*;
import com.budgetoptimizer.budget_optimizer_backend.model.Expense;
import com.budgetoptimizer.budget_optimizer_backend.model.Presupuesto;
import com.budgetoptimizer.budget_optimizer_backend.model.Usuario;
import com.budgetoptimizer.budget_optimizer_backend.repository.ExpenseRepository;
import com.budgetoptimizer.budget_optimizer_backend.repository.PresupuestoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresupuestoMLService {

    private final MLServiceClient mlServiceClient;
    private final PresupuestoRepository presupuestoRepository;
    private final ExpenseRepository expenseRepository;

    /**
     * Analiza un presupuesto con IA
     */
    public AnalisisPresupuestoResponse analizarPresupuestoConIA(Long presupuestoId) {

        log.info("Analizando presupuesto {} con IA", presupuestoId);

        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() ->
                        new RuntimeException("Presupuesto no encontrado con ID: " + presupuestoId));

        Usuario usuario = presupuesto.getUsuario();

        List<Expense> gastos = expenseRepository.findByPresupuestoId(presupuestoId);

        String prompt = construirPromptAnalisis(
                presupuesto,
                usuario,
                gastos
        );

        AnalisisPresupuestoRequest request = AnalisisPresupuestoRequest.builder()
                .nombre(usuario.getNombre())
                .prompt(prompt)
                .usuarioId(usuario.getId())
                .presupuestoId(presupuestoId)
                .build();

        try {

            AnalisisPresupuestoResponse response = mlServiceClient
                    .analizarPresupuesto(request)
                    .block();

            if (response == null) {
                throw new RuntimeException("El servicio ML devolvió una respuesta vacía");
            }

            log.info("Análisis completado correctamente");

            return response;

        } catch (Exception e) {

            log.error("Error analizando presupuesto: {}", e.getMessage(), e);

            throw new RuntimeException(
                    "Error al analizar presupuesto con IA",
                    e
                );
        }
    }

    /**
     * Predice gastos futuros
     */
    public PrediccionGastosResponse predecirGastosFuturos(
            Long usuarioId,
            Integer mesesAdelante
    ) {

        log.info(
                "Prediciendo gastos para usuario {} a {} meses",
                usuarioId,
                mesesAdelante
        );

        List<Expense> gastos = expenseRepository.findByUsuarioId(usuarioId);

        if (gastos == null || gastos.isEmpty()) {
            throw new RuntimeException(
                    "No existen gastos históricos suficientes"
            );
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM");

        List<GastoHistorico> gastosHistoricos = gastos.stream()
        .map(gasto -> GastoHistorico.builder()
                .categoria(gasto.getCategoria().getNombre())
                .monto(gasto.getMonto())
                .mes(gasto.getFechaGasto().format(formatter))
                .fecha(gasto.getFechaGasto().toString())
                .build())
        .collect(Collectors.toList());

        PrediccionGastosRequest request =
                PrediccionGastosRequest.builder()
                        .usuarioId(usuarioId)
                        .gastosHistoricos(gastosHistoricos)
                        .mesesAdelante(mesesAdelante)
                        .build();

        try {

            PrediccionGastosResponse response =
                    mlServiceClient
                            .predecirGastos(request)
                            .block();

            if (response == null) {
                throw new RuntimeException(
                        "El servicio ML devolvió una respuesta vacía"
                );
            }

            log.info(
                    "Predicción completada: {} meses",
                    response.getPredicciones().size()
            );

            return response;

        } catch (Exception e) {

            log.error(
                    "Error prediciendo gastos: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Error al predecir gastos",
                    e
            );
        }
    }

    /**
     * Optimiza distribución del presupuesto
     */
    public OptimizacionPresupuestoResponse optimizarDistribucion(
            Long presupuestoId
    ) {

        log.info(
                "Optimizando presupuesto {}",
                presupuestoId
        );

        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Presupuesto no encontrado"
                        ));

        List<Expense> gastos =
                expenseRepository.findByPresupuestoId(presupuestoId);

        List<String> categorias = gastos.stream()
                .map(g -> g.getCategoria().getNombre())
                .distinct()
                .collect(Collectors.toList());

        OptimizacionPresupuestoRequest request =
                OptimizacionPresupuestoRequest.builder()
                        .montoTotal(presupuesto.getMontoTotal())
                        .categorias(categorias)
                        .prioridades(Collections.emptyMap())
                        .gastosActuales(Collections.emptyMap())
                        .build();

        try {

            OptimizacionPresupuestoResponse response =
                    mlServiceClient
                            .optimizarPresupuesto(request)
                            .block();

            if (response == null) {
                throw new RuntimeException(
                        "El servicio ML devolvió una respuesta vacía"
                );
            }

            log.info(
                    "Optimización completada. Ahorro potencial: {}",
                    response.getAhorroPotencial()
            );

            return response;

        } catch (Exception e) {

            log.error(
                    "Error optimizando presupuesto: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Error al optimizar presupuesto",
                    e
            );
        }
    }

    /**
     * Detecta anomalías
     */
    public DeteccionAnomaliaResponse detectarAnomalias(
            Long usuarioId,
            Integer diasVentana
    ) {

        log.info(
                "Detectando anomalías para usuario {}",
                usuarioId
        );

        List<Expense> gastos =
                expenseRepository.findByUsuarioId(usuarioId);

        if (gastos == null || gastos.isEmpty()) {
            throw new RuntimeException(
                    "No hay gastos para analizar"
            );
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM");

        List<GastoHistorico> gastosHistoricos = gastos.stream()
                .map(gasto -> GastoHistorico.builder()
                        .categoria(gasto.getCategoria().getNombre())
                        .monto(gasto.getMonto())
                        .mes(gasto.getFechaGasto().toString())
                        .fecha(gasto.getFechaGasto().toString())
                        .build())
                .collect(Collectors.toList());

        DeteccionAnomaliaRequest request =
                DeteccionAnomaliaRequest.builder()
                        .usuarioId(usuarioId)
                        .gastos(gastosHistoricos)
                        .ventanaTiempo(diasVentana)
                        .build();

        try {

            DeteccionAnomaliaResponse response =
                    mlServiceClient
                            .detectarAnomalias(request)
                            .block();

            if (response == null) {
                throw new RuntimeException(
                        "El servicio ML devolvió una respuesta vacía"
                );
            }

            log.info(
                    "Detección completada: {} anomalías",
                    response.getTotalAnomalias()
            );

            return response;

        } catch (Exception e) {

            log.error(
                    "Error detectando anomalías: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Error al detectar anomalías",
                    e
            );
        }
    }

    /**
     * Health check del servicio ML
     */
    public boolean isMLServiceHealthy() {

        try {

            Boolean result =
                    mlServiceClient.checkHealth().block();

            return result != null && result;

        } catch (Exception e) {

            log.error(
                    "Error verificando ML Service: {}",
                    e.getMessage(),
                    e
            );

            return false;
        }
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private String construirPromptAnalisis(
            Presupuesto presupuesto,
            Usuario usuario,
            List<Expense> gastos
    ) {

        BigDecimal totalGastado = gastos.stream()
                .map(Expense::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal porcentajeUsado =
                presupuesto.getMontoTotal()
                        .compareTo(BigDecimal.ZERO) > 0
                        ? totalGastado.divide(
                                presupuesto.getMontoTotal(),
                                2,
                                RoundingMode.HALF_UP
                        ).multiply(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO;

        return String.format(
                """
                Usuario: %s
                Ciudad: %s, %s
                Presupuesto: %s
                Periodo: %s
                Monto total: $%.2f
                Total gastado: $%.2f (%.1f%%)
                Restante: $%.2f
                Total gastos: %d
                Categorías principales: %s
                """,
                usuario.getNombre(),
                usuario.getCiudad(),
                usuario.getPais(),
                presupuesto.getNombre(),
                presupuesto.getPeriodo(),
                presupuesto.getMontoTotal().doubleValue(),
                totalGastado.doubleValue(),
                porcentajeUsado.doubleValue(),
                presupuesto.getMontoTotal().subtract(totalGastado).doubleValue(),
                gastos.size(),
                obtenerCategoriasTop(gastos)
        );
    }

    private String obtenerCategoriasTop(List<Expense> gastos) {

        Map<String, BigDecimal> categoriasPorMonto =
                gastos.stream()
                        .collect(Collectors.groupingBy(
                                g -> g.getCategoria().getNombre(),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Expense::getMonto,
                                        BigDecimal::add
                                )
                        ));

        return categoriasPorMonto.entrySet()
                .stream()
                .sorted((e1, e2) ->
                        e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(e ->
                        String.format(
                                "%s ($%.2f)",
                                e.getKey(),
                                e.getValue().doubleValue()
                        )
                )
                .collect(Collectors.joining(", "));
    }
}