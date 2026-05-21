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
    // FILTROS POR ESTADO
    // ==========================================

    List<Categoria> findByActivaTrue();

    List<Categoria> findByActivaFalse();

    // ==========================================
    // FILTROS POR TIPO
    // ==========================================

    List<Categoria> findByTipo(CategoryType tipo);

    List<Categoria> findByActivaTrueAndTipo(CategoryType tipo);

    /**
     * Categorías activas por múltiples tipos
     */
    List<Categoria> findByActivaTrueAndTipoIn(List<CategoryType> tipos);

    // ==========================================
    // FILTROS POR TIPO DE EMPRESA
    // ==========================================

    List<Categoria> findByTipoEmpresaAsociada(TipoEmpresa tipoEmpresa);

    List<Categoria> findByTipoEmpresaAsociadaIsNull();

    List<Categoria> findByTipoEmpresaAsociadaIsNullAndActivaTrue();

    // ==========================================
    // CONSULTAS PERSONALIZADAS
    // ==========================================

    /**
     * Categorías válidas para gastos
     * (EXPENSE o BOTH)
     */
    @Query("""
        SELECT c
        FROM Categoria c
        WHERE c.activa = true
        AND c.tipo IN :tipos
    """)
    List<Categoria> findCategoriasParaGastos(
            @Param("tipos") List<CategoryType> tipos
    );

    /**
     * Categorías válidas para empresas
     * (BUSINESS o BOTH)
     */
    @Query("""
        SELECT c
        FROM Categoria c
        WHERE c.activa = true
        AND c.tipo IN :tipos
    """)
    List<Categoria> findCategoriasParaEmpresas(
            @Param("tipos") List<CategoryType> tipos
    );
}