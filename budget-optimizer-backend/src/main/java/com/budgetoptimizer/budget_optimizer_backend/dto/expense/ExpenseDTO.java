package com.budgetoptimizer.budget_optimizer_backend.dto.expense;

import com.budgetoptimizer.budget_optimizer_backend.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDateTime fechaGasto;

    @NotNull(message = "El presupuesto es obligatorio")
    private Long presupuestoId;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    // ✅ AGREGADO: Campo metodoPago (opcional)
    private PaymentMethod metodoPago;

    // ✅ AGREGADO: Campo opcional para empresa
    private String empresaId;

    // ✅ AGREGADO: Notas opcionales
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;
}