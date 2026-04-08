package com.budgetoptimizer.budget_optimizer_backend.dto.presupuesto;

import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetPeriod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para crear un nuevo presupuesto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearPresupuestoDTO {
    
    @NotBlank(message = "El nombre del presupuesto es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 12, fraction = 2, message = "El monto debe tener máximo 12 enteros y 2 decimales")
    private BigDecimal montoTotal;
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;
    
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;
    
    @NotNull(message = "El período es obligatorio")
    private BudgetPeriod periodo;
    
    // Opcional: límites por categoría
    // Map<categoriaId, montoLimite>
    private Map<Long, BigDecimal> limitesCategorias;
    
    /**
     * Valida que fechaFin sea posterior a fechaInicio
     */
    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isFechasValidas() {
        if (fechaInicio == null || fechaFin == null) {
            return true; // La validación @NotNull se encarga
        }
        return fechaFin.isAfter(fechaInicio);
    }
}