package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.categoria.CategoriaResponseDTO;
import com.budgetoptimizer.budget_optimizer_backend.exception.ResourceNotFoundException;
import com.budgetoptimizer.budget_optimizer_backend.model.Categoria;
import com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType;
import com.budgetoptimizer.budget_optimizer_backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getAllActiveCategories() {
        return categoriaRepository.findByActivaTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getCategoriesForExpenses() {
        // ✅ Usar el método @Query del repositorio
        return categoriaRepository.findCategoriasParaGastos()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getCategoriesForIncome() {
        // ✅ Para ingresos, filtramos manualmente ya que CategoryType no tiene INCOME
        // Asumiendo que las categorías de ingreso tienen tipo EXPENSE o BOTH
        // Si necesitas un tipo específico, deberías agregar INCOME al enum CategoryType
        return categoriaRepository.findByActivaTrue()
                .stream()
                .filter(c -> c.getTipo() == CategoryType.EXPENSE || c.getTipo() == CategoryType.BOTH)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO getCategoryById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return mapToResponseDTO(categoria);
    }

    private CategoriaResponseDTO mapToResponseDTO(Categoria categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .icono(categoria.getIcono())
                .color(categoria.getColor())
                .tipo(categoria.getTipo())
                .activa(categoria.getActiva())
                .build();
    }
}