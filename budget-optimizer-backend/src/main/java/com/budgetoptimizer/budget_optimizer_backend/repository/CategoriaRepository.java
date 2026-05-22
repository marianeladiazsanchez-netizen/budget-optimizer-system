package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType;
import com.budgetoptimizer.budget_optimizer_backend.enums.TipoEmpresa;
import com.budgetoptimizer.budget_optimizer_backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // ==========================================
    // BÚSQUEDAS BÁSICAS
    // ==========================================

    Optional<Categoria> findByNombre(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Categoria> findByNombreContainingIgnoreCase(String texto);

    // ==========================================
    // ESTADO
    // ==========================================

    List<Categoria> findByActivaTrue();

    List<Categoria> findByActivaFalse();

    // ==========================================
    // TIPO
    // ==========================================

    List<Categoria> findByTipo(CategoryType tipo);

    List<Categoria> findByActivaTrueAndTipo(CategoryType tipo);

    List<Categoria> findByActivaTrueAndTipoIn(List<CategoryType> tipos);

    // ==========================================
    // GASTOS (FIX DEFINITIVO)
    // ==========================================

    @Query("""
        SELECT c
        FROM Categoria c
        WHERE c.activa = true
        AND (c.tipo = com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType.EXPENSE
             OR c.tipo = com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType.BOTH)
    """)
    List<Categoria> findCategoriasParaGastos();

    // ==========================================
    // EMPRESAS (FIX DEFINITIVO)
    // ==========================================

    @Query("""
        SELECT c
        FROM Categoria c
        WHERE c.activa = true
        AND (c.tipo = com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType.BUSINESS
             OR c.tipo = com.budgetoptimizer.budget_optimizer_backend.enums.CategoryType.BOTH)
    """)
    List<Categoria> findCategoriasParaEmpresas();
}