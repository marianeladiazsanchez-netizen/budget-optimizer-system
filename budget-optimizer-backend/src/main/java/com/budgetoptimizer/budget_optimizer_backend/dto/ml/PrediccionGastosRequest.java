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
public class PrediccionGastosRequest {
    private Long usuarioId;
    private List<GastoHistorico> gastosHistoricos;
    private Integer mesesAdelante;
}
