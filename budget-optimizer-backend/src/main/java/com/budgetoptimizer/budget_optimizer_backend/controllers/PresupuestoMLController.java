package com.budgetoptimizer.budget_optimizer_backend.controllers;

import com.budgetoptimizer.budget_optimizer_backend.dto.ml.*;
import com.budgetoptimizer.budget_optimizer_backend.service.PresupuestoMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PresupuestoMLController {

    private final PresupuestoMLService presupuestoMLService;

    /**
     * Analiza un presupuesto con IA
     * GET /api/ml/analizar/{presupuestoId}
     */
    @GetMapping("/analizar/{presupuestoId}")
    public ResponseEntity<AnalisisPresupuestoResponse> analizarPresupuesto(
            @PathVariable Long presupuestoId) {
        
        log.info("📊 Solicitud de análisis para presupuesto {}", presupuestoId);
        
        try {
            AnalisisPresupuestoResponse response = 
                presupuestoMLService.analizarPresupuestoConIA(presupuestoId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al analizar presupuesto: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Predice gastos futuros
     * GET /api/ml/predecir?usuarioId=1&meses=3
     */
    @GetMapping("/predecir")
    public ResponseEntity<PrediccionGastosResponse> predecirGastos(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "3") Integer meses) {
        
        log.info("🔮 Solicitud de predicción para usuario {} - {} meses", usuarioId, meses);
        
        try {
            PrediccionGastosResponse response = 
                presupuestoMLService.predecirGastosFuturos(usuarioId, meses);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al predecir gastos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Optimiza distribución del presupuesto
     * GET /api/ml/optimizar/{presupuestoId}
     */
    @GetMapping("/optimizar/{presupuestoId}")
    public ResponseEntity<OptimizacionPresupuestoResponse> optimizarPresupuesto(
            @PathVariable Long presupuestoId) {
        
        log.info("⚙️ Solicitud de optimización para presupuesto {}", presupuestoId);
        
        try {
            OptimizacionPresupuestoResponse response = 
                presupuestoMLService.optimizarDistribucion(presupuestoId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al optimizar presupuesto: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Detecta anomalías en gastos
     * GET /api/ml/anomalias?usuarioId=1&dias=30
     */
    @GetMapping("/anomalias")
    public ResponseEntity<DeteccionAnomaliaResponse> detectarAnomalias(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "30") Integer dias) {
        
        log.info("🔍 Solicitud de detección de anomalías para usuario {}", usuarioId);
        
        try {
            DeteccionAnomaliaResponse response = 
                presupuestoMLService.detectarAnomalias(usuarioId, dias);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al detectar anomalías: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verifica salud del servicio ML
     * GET /api/ml/health
     */
    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> checkMLServiceHealth() {
        boolean isHealthy = presupuestoMLService.isMLServiceHealthy();
        
        HealthCheckResponse response = HealthCheckResponse.builder()
                .status(isHealthy ? "UP" : "DOWN")
                .mlService(isHealthy ? "CONNECTED" : "DISCONNECTED")
                .message(isHealthy ? "Servicio ML operativo" : "Servicio ML no disponible")
                .build();
        
        return isHealthy 
            ? ResponseEntity.ok(response)
            : ResponseEntity.status(503).body(response);
    }
}

// DTO para health check
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class HealthCheckResponse {
    private String status;
    private String mlService;
    private String message;
}