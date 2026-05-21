package com.budgetoptimizer.budget_optimizer_backend.dto.usuario;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para registro de nuevos usuarios
 * Contiene validaciones de entrada
 * Las coordenadas son opcionales y deben ser provistas por el frontend
 * (que las obtuvo de tu API Python de geolocalización)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
    )
    private String password;
    
    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;
    
    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String pais;
    
    // COORDENADAS OPCIONALES - Provistas por el frontend desde tu API Python
    private BigDecimal latitud;
    private BigDecimal longitud;
    
    @NotNull(message = "El presupuesto mensual base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El presupuesto debe ser mayor a 0")
    @Digits(integer = 12, fraction = 2, message = "El presupuesto debe tener máximo 12 enteros y 2 decimales")
    private BigDecimal presupuestoMensualBase;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El teléfono debe ser válido (formato internacional)")
    private String telefono;
}