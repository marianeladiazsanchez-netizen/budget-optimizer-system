package com.budgetoptimizer.budget_optimizer_backend.dto.ml;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizacionPresupuestoResponse {
    private Map<String, BigDecimal> distribucionOptimizada;
    private BigDecimal ahorroPotencial;
    private List<String> recomendaciones;
    private Map<String, String> justificaciones;
}
