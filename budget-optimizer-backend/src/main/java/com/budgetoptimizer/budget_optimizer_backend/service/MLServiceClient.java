package com.budgetoptimizer.budget_optimizer_backend.service;


import com.budgetoptimizer.budget_optimizer_backend.dto.ml.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
 
import java.time.Duration;

@Slf4j
@Service
public class MLServiceClient {

    private final WebClient webClient;

    public MLServiceClient(
            @Value("${ml.service.url:http://localhost:8000}") String mlServiceUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder
                .baseUrl(mlServiceUrl)
                .build();
    }

    /**
     * Analiza un presupuesto y obtiene recomendaciones de IA
     */
    public Mono<AnalisisPresupuestoResponse> analizarPresupuesto(AnalisisPresupuestoRequest request) {
        log.info("Solicitando análisis de presupuesto para usuario: {}", request.getNombre());
        
        return webClient.post()
                .uri("/api/ml/analizar")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AnalisisPresupuestoResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> 
                    log.info("Análisis completado exitosamente para: {}", request.getNombre()))
                .doOnError(error -> 
                    log.error("Error al analizar presupuesto: {}", error.getMessage()));
    }

    /**
     * Predice gastos futuros basado en datos históricos
     */
    public Mono<PrediccionGastosResponse> predecirGastos(PrediccionGastosRequest request) {
        log.info("Solicitando predicción de gastos para usuario ID: {}", request.getUsuarioId());
        
        return webClient.post()
                .uri("/api/ml/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PrediccionGastosResponse.class)
                .timeout(Duration.ofSeconds(45))
                .doOnSuccess(response -> 
                    log.info("Predicción completada: {} meses predichos", 
                        response.getPredicciones().size()))
                .doOnError(error -> 
                    log.error("Error al predecir gastos: {}", error.getMessage()));
    }

    /**
     * Optimiza la distribución del presupuesto
     */
    public Mono<OptimizacionPresupuestoResponse> optimizarPresupuesto(
            OptimizacionPresupuestoRequest request) {
        log.info("Solicitando optimización para presupuesto de ${}", request.getMontoTotal());
        
        return webClient.post()
                .uri("/api/ml/optimize")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OptimizacionPresupuestoResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> 
                    log.info("Optimización completada. Ahorro potencial: ${}", 
                        response.getAhorroPotencial()))
                .doOnError(error -> 
                    log.error("Error al optimizar presupuesto: {}", error.getMessage()));
    }

    /**
     * Detecta anomalías en los patrones de gasto
     */
    public Mono<DeteccionAnomaliaResponse> detectarAnomalias(DeteccionAnomaliaRequest request) {
        log.info("Solicitando detección de anomalías para usuario ID: {}", request.getUsuarioId());
        
        return webClient.post()
                .uri("/api/ml/anomalias")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DeteccionAnomaliaResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> 
                    log.info("Detección completada: {} anomalías encontradas", 
                        response.getAnomalias().size()))
                .doOnError(error -> 
                    log.error("Error al detectar anomalías: {}", error.getMessage()));
    }

    /**
     * Verifica el estado del servicio ML
     */
    public Mono<Boolean> checkHealth() {
        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(HealthResponse.class)
                .map(response -> "ok".equalsIgnoreCase(response.getStatus()))
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(false);
    }
}
