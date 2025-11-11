package com.budgetoptimizer.budget_optimizer_backend.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estructura estándar para respuestas de error de la API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private Integer status;
    private String error;
    private String mensaje;
    private String path;
    
    // Para errores de validación (múltiples errores)
    private List<String> errores;
    
    /**
     * Constructor para un solo error
     */
    public ErrorResponse(Integer status, String error, String mensaje, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
    }
}