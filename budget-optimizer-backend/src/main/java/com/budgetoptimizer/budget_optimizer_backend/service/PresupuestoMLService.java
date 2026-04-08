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
     * Analiza un presupuesto con IA y retorna recomendaciones
     * VERSIÓN SÍNCRONA (bloquea hasta obtener respuesta)
     */
    public AnalisisPresupuestoResponse analizarPresupuestoConIA(Long presupuestoId) {
        log.info("🤖 Analizando presupuesto {} con IA", presupuestoId);

        // 1. Obtener presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        // 2. Obtener usuario
        Usuario usuario = presupuesto.getUsuario();

        // 3. Obtener gastos del presupuesto
        List<Expense> gastos = expenseRepository.findByPresupuestoId(presupuestoId);

        // 4. Construir prompt para Gemini
        String prompt = construirPromptAnalisis(presupuesto, usuario, gastos);

        // 5. Crear request
        AnalisisPresupuestoRequest request = AnalisisPresupuestoRequest.builder()
                .nombre(usuario.getNombre())
                .prompt(prompt)
                .usuarioId(usuario.getId())
                .presupuestoId(presupuestoId)
                .build();

        // 6. Llamar al servicio ML (bloqueando con .block())
        try {
            AnalisisPresupuestoResponse response = mlServiceClient
                    .analizarPresupuesto(request)
                    .block(); // ⚠️ Bloquea hasta obtener respuesta
            
            log.info("✅ Análisis completado para presupuesto {}", presupuestoId);
            return response;
        } catch (Exception e) {
            log.error("❌ Error al analizar presupuesto: {}", e.getMessage());
            throw new RuntimeException("Error al analizar presupuesto con IA", e);
        }
    }

    /**
     * Predice gastos futuros
     */
    public PrediccionGastosResponse predecirGastosFuturos(Long usuarioId, Integer mesesAdelante) {
        log.info("🔮 Prediciendo gastos para usuario {} - {} meses adelante", usuarioId, mesesAdelante);

        // 1. Obtener gastos históricos
        List<Expense> gastos = expenseRepository.findByUsuarioId(usuarioId);

        if (gastos.isEmpty()) {
            throw new RuntimeException("No hay datos históricos suficientes para predicción");
        }

        // 2. Convertir a formato ML
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        List<GastoHistorico> gastosHistoricos = gastos.stream()
                .map(gasto -> GastoHistorico.builder()
                        .categoria(gasto.getCategoria().getNombre())
                        .monto(gasto.getMonto().doubleValue())
                        .mes(gasto.getFechaGasto().format(formatter))
                        .fecha(gasto.getFechaGasto().toString())
                        .build())
                .collect(Collectors.toList());

        // 3. Crear request
        PrediccionGastosRequest request = PrediccionGastosRequest.builder()
                .usuarioId(usuarioId)
                .gastosHistoricos(gastosHistoricos)
                .mesesAdelante(mesesAdelante)
                .build();

        // 4. Llamar servicio ML (bloqueando)
        try {
            PrediccionGastosResponse response = mlServiceClient
                    .predecirGastos(request)
                    .block();
            
            log.info("✅ Predicción completada: {} meses", response.getPredicciones().size());
            return response;
        } catch (Exception e) {
            log.error("❌ Error al predecir gastos: {}", e.getMessage());
            throw new RuntimeException("Error al predecir gastos", e);
        }
    }

    /**
     * Optimiza la distribución del presupuesto
     */
    public OptimizacionPresupuestoResponse optimizarDistribucion(Long presupuestoId) {
        log.info("⚙️ Optimizando distribución del presupuesto {}", presupuestoId);

        // 1. Obtener presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        // 2. Obtener categorías y gastos actuales
        List<Expense> gastos = expenseRepository.findByPresupuestoId(presupuestoId);

        List<String> categorias = gastos.stream()
                .map(g -> g.getCategoria().getNombre())
                .distinct()
                .collect(Collectors.toList());

        // 3. Crear request
        OptimizacionPresupuestoRequest request = OptimizacionPresupuestoRequest.builder()
                .montoTotal(presupuesto.getMontoTotal().doubleValue())
                .categorias(categorias)
                .prioridades(null) // Se puede agregar lógica de prioridades
                .gastosActuales(null) // Se puede agregar gastos actuales
                .build();

        // 4. Llamar servicio ML (bloqueando)
        try {
            OptimizacionPresupuestoResponse response = mlServiceClient
                    .optimizarPresupuesto(request)
                    .block();
            
            log.info("✅ Optimización completada. Ahorro: ${}", response.getAhorroPotencial());
            return response;
        } catch (Exception e) {
            log.error("❌ Error al optimizar presupuesto: {}", e.getMessage());
            throw new RuntimeException("Error al optimizar presupuesto", e);
        }
    }

    /**
     * Detecta anomalías en gastos
     */
    public DeteccionAnomaliaResponse detectarAnomalias(Long usuarioId, Integer diasVentana) {
        log.info("🔍 Detectando anomalías para usuario {}", usuarioId);

        // 1. Obtener gastos
        List<Expense> gastos = expenseRepository.findByUsuarioId(usuarioId);

        if (gastos.isEmpty()) {
            throw new RuntimeException("No hay gastos para analizar");
        }

        // 2. Convertir a formato  
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        List<GastoHistorico> gastosHistoricos = gastos.stream()
                .map(gasto -> GastoHistorico.builder()
                        .categoria(gasto.getCategoria().getNombre())
                        .monto(gasto.getMonto().doubleValue())
                        .mes(gasto.getFechaGasto().format(formatter))
                        .fecha(gasto.getFechaGasto().toString())
                        .build())
                .collect(Collectors.toList());

        // 3. Crear request
        DeteccionAnomaliaRequest request = DeteccionAnomaliaRequest.builder()
                .usuarioId(usuarioId)
                .gastos(gastosHistoricos)
                .ventanaTiempo(diasVentana)
                .build();

        // 4. Llamar servicio ML (bloqueando)
        try {
            DeteccionAnomaliaResponse response = mlServiceClient
                    .detectarAnomalias(request)
                    .block();
            
            log.info("✅ Detección completada: {} anomalías encontradas", 
                response.getTotalAnomalias());
            return response;
        } catch (Exception e) {
            log.error("❌ Error al detectar anomalías: {}", e.getMessage());
            throw new RuntimeException("Error al detectar anomalías", e);
        }
    }

    /**
     * Verifica salud del servicio ML
     */
    public boolean isMLServiceHealthy() {
        try {
            Boolean result = mlServiceClient.checkHealth().block();
            return result != null && result;
        } catch (Exception e) {
            log.error("❌ Error en health check: {}", e.getMessage());
            return false;
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private String construirPromptAnalisis(Presupuesto presupuesto, Usuario usuario, List<Expense> gastos) {
        BigDecimal totalGastado = gastos.stream()
                .map(Expense::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal porcentajeUsado = presupuesto.getMontoTotal().compareTo(BigDecimal.ZERO) > 0
                ? totalGastado.divide(presupuesto.getMontoTotal(), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return String.format(
                "Usuario: %s\n" +
                "Ciudad: %s, %s\n" +
                "Presupuesto: %s\n" +
                "Período: %s\n" +
                "Monto Total: $%.2f\n" +
                "Total Gastado: $%.2f (%.1f%%)\n" +
                "Restante: $%.2f\n" +
                "Total de Gastos: %d\n" +
                "Categorías principales: %s",
                usuario.getNombre(),
                usuario.getCiudad(),
                usuario.getPais(),
                presupuesto.getNombre(),
                presupuesto.getPeriodo(),
                presupuesto.getMontoTotal(),
                totalGastado,
                porcentajeUsado,
                presupuesto.getMontoTotal().subtract(totalGastado),
                gastos.size(),
                obtenerCategoriasTop(gastos)
        );
    }

    private String obtenerCategoriasTop(List<Expense> gastos) {
        Map<String, BigDecimal> categoriasPorMonto = gastos.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getCategoria().getNombre(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getMonto, BigDecimal::add)
                ));
        
        return categoriasPorMonto.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(e -> String.format("%s ($%.2f)", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", "));
    }
}