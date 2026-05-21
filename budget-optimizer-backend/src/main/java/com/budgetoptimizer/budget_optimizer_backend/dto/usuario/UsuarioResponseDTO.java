package com.budgetoptimizer.budget_optimizer_backend.dto.usuario;

import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de usuario
 * NO incluye password por seguridad
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    
    // Ubicación
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String ciudad;
    private String pais;
    
    // Cuenta y presupuesto
    private AccountType accountType;
    private String accountTypeDisplay;  // "Usuario Premium"
    private BigDecimal presupuestoMensualBase;
    private Integer transaccionesMesActual;
    private Integer limiteTransaccionesMes;
    
    // Beneficios de la cuenta
    private BigDecimal descuentoPorcentaje;
    private Boolean tieneBeneficios;
    private Boolean puedeAdministrarEmpresas;
    
    // Estado
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    
    // Estadísticas
    private Integer totalPresupuestos;
    private Integer totalGastos;
    private Integer totalReviews;
}
