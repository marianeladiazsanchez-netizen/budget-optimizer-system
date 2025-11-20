package com.budgetoptimizer.budget_optimizer_backend.dto.presupuesto;

import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetPeriod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para actualizar presupuestos (solo en estado DRAFT)
 * Todos los campos son opcionales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPresupuestoDTO {
    
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 12, fraction = 2, message = "El monto debe tener máximo 12 enteros y 2 decimales")
    private BigDecimal montoTotal;
    
    private LocalDateTime fechaInicio;
    
    private LocalDateTime fechaFin;
    
    private BudgetPeriod periodo;
}