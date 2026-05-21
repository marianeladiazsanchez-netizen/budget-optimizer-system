package com.budgetoptimizer.budget_optimizer_backend.dto.ml;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoHistorico {
    private String categoria;
    private BigDecimal monto;
    private String mes; // formato: "2025-01"
    private String fecha;
}