package com.budgetoptimizer.budget_optimizer_backend.dto.presupuesto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para estadísticas agregadas de presupuestos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasPresupuestoDTO {
    
    // Totales
    private Integer totalPresupuestos;
    private Integer presupuestosActivos;
    private Integer presupuestosCompletados;
    private Integer presupuestosExcedidos;
    
    // Montos
    private BigDecimal totalPresupuestado;
    private BigDecimal totalGastado;
    private BigDecimal totalRestante;
    private Double porcentajeGastoGeneral;
    
    // Promedios
    private BigDecimal gastoPromedioDiario;
    private BigDecimal gastoPromedioMensual;
    
    // Top categorías
    private List<GastoCategoria> topCategorias;
    
    // Tendencias
    private String tendencia;  // "MEJORANDO", "ESTABLE", "EMPEORANDO"
    private String mensaje;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GastoCategoria {
        private String categoria;
        private BigDecimal monto;
        private BigDecimal porcentaje;
    }
}