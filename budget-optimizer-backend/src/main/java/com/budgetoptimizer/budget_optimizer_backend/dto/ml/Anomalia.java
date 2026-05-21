package com.budgetoptimizer.budget_optimizer_backend.dto.ml;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anomalia {
    private LocalDateTime fecha;
    private String categoria;
    private BigDecimal montoEsperado;
    private BigDecimal montoReal;
    private BigDecimal desviacion;
    private String descripcion;
}
