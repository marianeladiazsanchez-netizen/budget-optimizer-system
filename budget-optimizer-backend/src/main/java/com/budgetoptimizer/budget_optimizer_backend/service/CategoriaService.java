package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.categoria.CategoriaResponseDTO;
import com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType;
import com.budgetoptimizer.budget_optimizer_backend.exception.ResourceNotFoundException;
import com.budgetoptimizer.budget_optimizer_backend.model.Categoria;
import com.budgetoptimizer.budget_optimizer_backend.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // ==========================================
    // OBTENER TODAS LAS CATEGORÍAS ACTIVAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getAllActiveCategories() {

        return categoriaRepository.findByActivaTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // CATEGORÍAS PARA GASTOS
    // ==========================================

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getCategoriesForExpenses() {

        return categoriaRepository.findCategoriasParaGastos()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // CATEGORÍAS PARA INGRESOS
    // ==========================================

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getCategoriesForIncome() {

        return categoriaRepository.findByActivaTrue()
                .stream()
                .filter(categoria ->
                        categoria.getTipo() == CategoryType.INCOME
                )
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // OBTENER CATEGORÍA POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public CategoriaResponseDTO getCategoryById(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoría no encontrada con ID: " + id
                        )
                );

        return mapToResponseDTO(categoria);
    }

    // ==========================================
    // ENTITY -> DTO
    // ==========================================

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