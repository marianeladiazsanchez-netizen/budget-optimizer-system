package com.budgetoptimizer.budget_optimizer_backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisPresupuestoRequest {
    private String nombre;
    private String prompt;
    private Long usuarioId;
    private Long presupuestoId;
}
