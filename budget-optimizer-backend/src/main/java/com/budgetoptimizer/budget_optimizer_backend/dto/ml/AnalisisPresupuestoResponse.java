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
public class AnalisisPresupuestoResponse {
    private String usuario;
    private String analisis;
    private List<String> recomendaciones;
    private String ahorroPotencial;
    private Double confianza;
}
