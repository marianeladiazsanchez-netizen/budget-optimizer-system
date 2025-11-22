package com.budgetoptimizer.budget_optimizer_backend.dto.ml;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeteccionAnomaliaResponse {
    private List<Anomalia> anomalias;
    private Integer totalAnomalias;
    private String severidad;
}