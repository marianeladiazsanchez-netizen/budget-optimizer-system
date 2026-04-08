package com.budgetoptimizer.budget_optimizer_backend.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de gastos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDTO {

    private Long id;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fechaGasto;
    private Long presupuestoId;
    private String presupuestoNombre;
    private Long categoriaId;
    private String categoriaNombre;
    private String categoriaIcono;
    private String categoriaColor;
    private Long usuarioId;
    private String usuarioNombre;
    
    // ✅ Cambiado de createdAt a fechaCreacion para coincidir con la entidad
    private LocalDateTime fechaCreacion;
}