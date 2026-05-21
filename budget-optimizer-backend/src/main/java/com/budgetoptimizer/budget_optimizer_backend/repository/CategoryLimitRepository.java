package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.model.CategoryLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryLimitRepository extends JpaRepository<CategoryLimit, Long> {

    // ==========================================
    // CONSULTAS BÁSICAS
    // ==========================================

    List<CategoryLimit> findByPresupuestoId(Long presupuestoId);

    List<CategoryLimit> findByCategoriaId(Long categoriaId);

    Optional<CategoryLimit> findByPresupuestoIdAndCategoriaId(
            Long presupuestoId,
            Long categoriaId
    );

    boolean existsByPresupuestoIdAndCategoriaId(
            Long presupuestoId,
            Long categoriaId
    );

    // ==========================================
    // CONSULTAS PERSONALIZADAS
    // ==========================================

    /**
     * Obtiene límites que están cerca del tope
     * (80% o más utilizado)
     */
    @Query("""
        SELECT cl
        FROM CategoryLimit cl
        WHERE cl.presupuesto.id = :presupuestoId
        AND cl.limiteAsignado > 0
        AND (cl.gastoActual / cl.limiteAsignado) >= 0.8
    """)
    List<CategoryLimit> findLimitesCercaDelTope(
            @Param("presupuestoId") Long presupuestoId
    );

    /**
     * Obtiene límites excedidos
     */
    @Query("""
        SELECT cl
        FROM CategoryLimit cl
        WHERE cl.presupuesto.id = :presupuestoId
        AND cl.gastoActual > cl.limiteAsignado
    """)
    List<CategoryLimit> findLimitesExcedidos(
            @Param("presupuestoId") Long presupuestoId
    );
}