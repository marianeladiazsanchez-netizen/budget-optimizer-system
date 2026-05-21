package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.enums.PaymentMethod;
import com.budgetoptimizer.budget_optimizer_backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ==========================================
    // CONSULTAS POR USUARIO
    // ==========================================

    List<Expense> findByUsuarioId(Long usuarioId);

    List<Expense> findByUsuarioIdOrderByFechaGastoDesc(Long usuarioId);

    // ==========================================
    // CONSULTAS POR PRESUPUESTO
    // ==========================================

    List<Expense> findByPresupuestoId(Long presupuestoId);

    List<Expense> findByPresupuestoIdAndCategoriaId(
            Long presupuestoId,
            Long categoriaId
    );

    List<Expense> findByPresupuestoIdAndFechaGastoBetween(
            Long presupuestoId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // ==========================================
    // CONSULTAS POR CATEGORÍA
    // ==========================================

    List<Expense> findByCategoriaId(Long categoriaId);

    List<Expense> findByUsuarioIdAndCategoriaId(
            Long usuarioId,
            Long categoriaId
    );

    // ==========================================
    // CONSULTAS POR EMPRESA
    // ==========================================

    List<Expense> findByEmpresaId(String empresaId);

    List<Expense> findByUsuarioIdAndEmpresaId(
            Long usuarioId,
            String empresaId
    );

    // ==========================================
    // CONSULTAS POR MÉTODO DE PAGO
    // ==========================================

    List<Expense> findByMetodoPago(PaymentMethod metodoPago);

    List<Expense> findByUsuarioIdAndMetodoPago(
            Long usuarioId,
            PaymentMethod metodoPago
    );

    // ==========================================
    // CONSULTAS POR FECHAS
    // ==========================================

    List<Expense> findByFechaGastoBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Expense> findByUsuarioIdAndFechaGastoBetween(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // ==========================================
    // CONSULTAS POR MONTO
    // ==========================================

    List<Expense> findByMontoGreaterThan(BigDecimal monto);

    List<Expense> findByMontoBetween(
            BigDecimal min,
            BigDecimal max
    );

    List<Expense> findTop10ByUsuarioIdOrderByMontoDesc(Long usuarioId);

    // ==========================================
    // CONSULTAS POR DESCRIPCIÓN
    // ==========================================

    List<Expense> findByDescripcionContainingIgnoreCase(String keyword);

    List<Expense> findByUsuarioIdAndDescripcionContainingIgnoreCase(
            Long usuarioId,
            String keyword
    );

    // ==========================================
    // AGREGACIONES
    // ==========================================

    /**
     * Suma total de gastos de un presupuesto
     */
    @Query("""
        SELECT COALESCE(SUM(e.monto), 0)
        FROM Expense e
        WHERE e.presupuesto.id = :presupuestoId
    """)
    BigDecimal sumMontoByPresupuestoId(
            @Param("presupuestoId") Long presupuestoId
    );

    /**
     * Suma total de gastos de una categoría
     */
    @Query("""
        SELECT COALESCE(SUM(e.monto), 0)
        FROM Expense e
        WHERE e.categoria.id = :categoriaId
    """)
    BigDecimal sumMontoByCategoriaId(
            @Param("categoriaId") Long categoriaId
    );

    /**
     * Gastos agrupados por categoría
     */
    @Query("""
        SELECT e.categoria.nombre, SUM(e.monto)
        FROM Expense e
        WHERE e.usuario.id = :usuarioId
        GROUP BY e.categoria.nombre
        ORDER BY SUM(e.monto) DESC
    """)
    List<Object[]> findGastosPorCategoria(
            @Param("usuarioId") Long usuarioId
    );

    /**
     * Promedio de gasto por categoría
     */
    @Query("""
        SELECT e.categoria.nombre, AVG(e.monto)
        FROM Expense e
        WHERE e.usuario.id = :usuarioId
        GROUP BY e.categoria.nombre
    """)
    List<Object[]> findPromedioGastoPorCategoria(
            @Param("usuarioId") Long usuarioId
    );