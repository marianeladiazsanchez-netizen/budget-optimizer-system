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
public class PrediccionGastosResponse {
    private List<PrediccionMensual> predicciones;
    private Double confianza;
    private String modelo;
}
