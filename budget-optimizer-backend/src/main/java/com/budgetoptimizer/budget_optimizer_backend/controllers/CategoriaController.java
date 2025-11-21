package com.budgetoptimizer.budget_optimizer_backend.controllers;

import com.budgetoptimizer.budget_optimizer_backend.dto.categoria.CategoriaResponseDTO;
import com.budgetoptimizer.budget_optimizer_backend.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Obtener todas las categorías activas
     * GET /api/categorias
     */
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> getAllActiveCategories() {
        List<CategoriaResponseDTO> categorias = categoriaService.getAllActiveCategories();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtener categorías para gastos (tipo EXPENSE o BOTH)
     * GET /api/categorias/gastos
     */
    @GetMapping("/gastos")
    public ResponseEntity<List<CategoriaResponseDTO>> getCategoriesForExpenses() {
        List<CategoriaResponseDTO> categorias = categoriaService.getCategoriesForExpenses();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtener categorías para ingresos (tipo INCOME o BOTH)
     * GET /api/categorias/ingresos
     */
    @GetMapping("/ingresos")
    public ResponseEntity<List<CategoriaResponseDTO>> getCategoriesForIncome() {
        List<CategoriaResponseDTO> categorias = categoriaService.getCategoriesForIncome();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtener una categoría por ID
     * GET /api/categorias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoriaResponseDTO categoria = categoriaService.getCategoryById(id);
        return ResponseEntity.ok(categoria);
    }
}