package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.ml.*;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class MLServiceClient {

    private final WebClient webClient;

    public MLServiceClient(
            @Value("${ml.service.url:http://ml-service:8000}") String mlServiceUrl,
            WebClient.Builder webClientBuilder
    ) {

        log.info("ML Service URL: {}", mlServiceUrl);

        this.webClient = webClientBuilder
                .baseUrl(mlServiceUrl)
                .build();
    }

    // ==========================================
    // ANALIZAR PRESUPUESTO
    // ==========================================

    public Mono<AnalisisPresupuestoResponse> analizarPresupuesto(
            AnalisisPresupuestoRequest request
    ) {

        log.info("Analizando presupuesto para usuario: {}", request.getNombre());

        return webClient.post()
                .uri("/api/ml/analizar")
                .bodyValue(request)
                .retrieve()

                .onStatus(
                HttpStatusCode::isError,
                response -> response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    log.error("Error ML Service: {}", errorBody);
                    return Mono.error(new RuntimeException(errorBody));
                })
                )

                .bodyToMono(AnalisisPresupuestoResponse.class)

                .timeout(Duration.ofSeconds(30))

                .doOnSuccess(response ->
                        log.info("Análisis completado correctamente")
                )

                .doOnError(error ->
                        log.error("Error analizando presupuesto", error)
                );
    }

    // ==========================================
    // PREDECIR GASTOS
    // ==========================================

    public Mono<PrediccionGastosResponse> predecirGastos(
            PrediccionGastosRequest request
    ) {

        log.info("Predicción de gastos para usuario ID: {}", request.getUsuarioId());

        return webClient.post()
                .uri("/api/ml/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PrediccionGastosResponse.class)
                .timeout(Duration.ofSeconds(45))

                .doOnSuccess(response ->
                        log.info(
                                "Predicción completada: {} registros",
                                response.getPredicciones().size()
                        )
                )

                .doOnError(error ->
                        log.error("Error en predicción", error)
                );
    }

    // ==========================================
    // OPTIMIZAR PRESUPUESTO
    // ==========================================

    public Mono<OptimizacionPresupuestoResponse> optimizarPresupuesto(
            OptimizacionPresupuestoRequest request
    ) {

        log.info("Optimizando presupuesto: {}", request.getMontoTotal());

        return webClient.post()
                .uri("/api/ml/optimize")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OptimizacionPresupuestoResponse.class)
                .timeout(Duration.ofSeconds(30))

                .doOnSuccess(response ->
                        log.info(
                                "Optimización completada. Ahorro: {}",
                                response.getAhorroPotencial()
                        )
                )

                .doOnError(error ->
                        log.error("Error optimizando presupuesto", error)
                );
    }

    // ==========================================
    // DETECTAR ANOMALÍAS
    // ==========================================

    public Mono<DeteccionAnomaliaResponse> detectarAnomalias(
            DeteccionAnomaliaRequest request
    ) {

        log.info("Detectando anomalías usuario ID: {}", request.getUsuarioId());

        return webClient.post()
                .uri("/api/ml/anomalias")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DeteccionAnomaliaResponse.class)
                .timeout(Duration.ofSeconds(30))

                .doOnSuccess(response ->
                        log.info(
                                "Anomalías encontradas: {}",
                                response.getAnomalias().size()
                        )
                )

                .doOnError(error ->
                        log.error("Error detectando anomalías", error)
                );
    }

    // ==========================================
    // HEALTH CHECK
    // ==========================================

    public Mono<Boolean> checkHealth() {

        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(HealthResponse.class)

                .map(response ->
                        "ok".equalsIgnoreCase(response.getStatus())
                )

                .timeout(Duration.ofSeconds(5))

                .onErrorResume(error -> {

                    log.error("ML Service offline", error);

                    return Mono.just(false);
                });
    }
}