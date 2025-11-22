package com.budgetoptimizer.budget_optimizer_backend.dto.ml;
import java.math.BigDecimal;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionMensual {
    private String mes;
    private Map<String, BigDecimal> prediccionesPorCategoria;
    private BigDecimal totalPredicho;
}