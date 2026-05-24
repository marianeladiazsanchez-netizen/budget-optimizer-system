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
    // USUARIO
    // ==========================================
    List<Expense> findByUsuario_Id(Long usuarioId);

    List<Expense> findByUsuarioIdOrderByFechaGastoDesc(Long usuarioId);

    List<Expense> findByUsuarioIdAndFechaGastoBetweenOrderByFechaGastoDesc(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Expense> findByUsuario_IdAndCategoria_Id(Long usuarioId, Long categoriaId);

    List<Expense> findByUsuario_IdAndEmpresa_Id(Long usuarioId, Long empresaId);

    List<Expense> findByUsuario_IdAndMetodo_Pago(Long usuarioId, PaymentMethod metodoPago);

    List<Expense> findByUsuario_IdAndDescripcionContainingIgnoreCase(Long usuarioId, String keyword);

    // ==========================================
    // PRESUPUESTO
    // ==========================================
    List<Expense> findByPresupuestoId(Long presupuestoId);

    List<Expense> findByPresupuestoIdOrderByFechaGastoDesc(Long presupuestoId);

    List<Expense> findByPresupuestoIdAndCategoriaIdOrderByFechaGastoDesc(
            Long presupuestoId,
            Long categoriaId
    );

    List<Expense> findByPresupuestoIdAndFechaGastoBetween(
            Long presupuestoId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // ==========================================
    // CATEGORÍA
    // ==========================================
    List<Expense> findByCategoriaId(Long categoriaId);

    // ==========================================
    // EMPRESA (FIX: Long, no String)
    // ==========================================
    List<Expense> findByEmpresaId(Long empresaId);

    // ==========================================
    // MÉTODO DE PAGO
    // ==========================================
    List<Expense> findByMetodoPago(PaymentMethod metodoPago);

    // ==========================================
    // FECHAS
    // ==========================================
    List<Expense> findByFechaGastoBetween(LocalDateTime inicio, LocalDateTime fin);

    // ==========================================
    // MONTO
    // ==========================================
    List<Expense> findByMontoGreaterThan(BigDecimal monto);

    List<Expense> findByMontoBetween(BigDecimal min, BigDecimal max);

    List<Expense> findTop10ByUsuarioIdOrderByMontoDesc(Long usuarioId);

    // ==========================================
    // DESCRIPCIÓN
    // ==========================================
    List<Expense> findByDescripcionContainingIgnoreCase(String keyword);

    // ==========================================
    // AGREGACIONES
    // ==========================================
    @Query("""
        SELECT COALESCE(SUM(e.monto), 0)
        FROM Expense e
        WHERE e.presupuesto.id = :presupuestoId
    """)
    BigDecimal sumMontoByPresupuestoId(@Param("presupuestoId") Long presupuestoId);

    @Query("""
        SELECT COALESCE(SUM(e.monto), 0)
        FROM Expense e
        WHERE e.categoria.id = :categoriaId
    """)
    BigDecimal sumMontoByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query("""
        SELECT e.categoria.nombre, SUM(e.monto)
        FROM Expense e
        WHERE e.usuario.id = :usuarioId
        GROUP BY e.categoria.nombre
        ORDER BY SUM(e.monto) DESC
    """)
    List<Object[]> findGastosPorCategoria(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT e.categoria.nombre, AVG(e.monto)
        FROM Expense e
        WHERE e.usuario.id = :usuarioId
        GROUP BY e.categoria.nombre
    """)
    List<Object[]> findPromedioGastoPorCategoria(@Param("usuarioId") Long usuarioId);
}