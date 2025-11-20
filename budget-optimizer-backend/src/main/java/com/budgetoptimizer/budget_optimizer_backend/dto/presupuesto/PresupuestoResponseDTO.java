package com.budgetoptimizer.budget_optimizer_backend.dto.presupuesto;

import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetPeriod;
import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para información de presupuesto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoResponseDTO {
    
    // Identificación
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    
    // Información básica
    private String nombre;
    private String descripcion;
    
    // Montos
    private BigDecimal montoTotal;
    private BigDecimal montoGastado;
    private BigDecimal montoRestante;
    private Double porcentajeGastado;
    
    // Estado y período
    private BudgetStatus status;
    private String statusDisplay;  // "Activo", "Completado", etc.
    private String statusColor;    // Color hex para UI
    
    private BudgetPeriod periodo;
    private String periodoDisplay; // "Mensual", "Anual", etc.
    private Integer duracionDias;
    
    // Fechas
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    
    // Estadísticas
    private Integer totalGastos;
    private Integer totalCategorias;
    private Boolean estaVigente;
    private Boolean estaCercaDelLimite;  // >= 80%
    private Boolean estaExcedido;
    
    // Permisos
    private Boolean puedeEditar;
    private Boolean puedeRegistrarGastos;
    
    // Límites por categoría (opcional)
    private List<LimiteCategoriaDTO> limitesCategorias;
    
    // DTO interno para límites
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LimiteCategoriaDTO {
        private Long categoriaId;
        private String categoriaNombre;
        private String categoriaIcono;
        private BigDecimal limiteAsignado;
        private BigDecimal gastoActual;
        private BigDecimal restante;
        private Double porcentajeUtilizado;
        private Boolean estaCercaDelLimite;
        private Boolean excedido;
    }
}