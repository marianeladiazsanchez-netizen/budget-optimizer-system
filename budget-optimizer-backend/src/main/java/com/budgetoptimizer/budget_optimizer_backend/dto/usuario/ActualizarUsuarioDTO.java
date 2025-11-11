package com.budgetoptimizer.budget_optimizer_backend.dto.usuario;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para actualización de perfil de usuario
 * Todos los campos son opcionales (solo se actualiza lo que venga)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUsuarioDTO {
    
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;
    
    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String pais;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "El presupuesto debe ser mayor a 0")
    @Digits(integer = 12, fraction = 2, message = "El presupuesto debe tener máximo 12 enteros y 2 decimales")
    private BigDecimal presupuestoMensualBase;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El teléfono debe ser válido (formato internacional)")
    private String telefono;
}