package com.budgetoptimizer.budget_optimizer_backend.dto.categoria;

import com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de categorías
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String icono;
    private String color;
    private CategoryType tipo;
    private boolean activa;
}
