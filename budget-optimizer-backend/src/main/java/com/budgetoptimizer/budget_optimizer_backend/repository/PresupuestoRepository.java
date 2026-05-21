package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetPeriod;
import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetStatus;
import com.budgetoptimizer.budget_optimizer_backend.model.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    // ==========================================
    // CONSULTAS POR USUARIO
    // ==========================================

    List<Presupuesto> findByUsuarioId(Long usuarioId);

    List<Presupuesto> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Presupuesto> findByUsuarioIdAndStatus(
            Long usuarioId,
            BudgetStatus status
    );

    long countByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    // ==========================================
    // CONSULTAS POR ESTADO
    // ==========================================

    List<Presupuesto> findByStatus(BudgetStatus status);

    List<Presupuesto> findByStatusAndFechaFinAfter(
            BudgetStatus status,
            LocalDateTime fecha
    );

    // ==========================================
    // CONSULTAS POR PERÍODO
    // ==========================================

    List<Presupuesto> findByPeriodo(BudgetPeriod periodo);

    List<Presupuesto> findByPeriodoAndStatus(
            BudgetPeriod periodo,
            BudgetStatus status
    );

    // ==========================================
    // CONSULTAS POR FECHAS
    // ==========================================

    List<Presupuesto> findByFechaCreacionBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Presupuesto> findByFechaFinBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // ==========================================
    // CONSULTAS POR MONTO
    // ==========================================

    List<Presupuesto> findByMontoTotalGreaterThan(BigDecimal monto);

    List<Presupuesto> findByMontoTotalBetween(
            BigDecimal min,
            BigDecimal max
    );

    // ==========================================
    // ORDENAMIENTO
    // ==========================================

    List<Presupuesto> findTop10ByStatusOrderByMontoTotalDesc(
            BudgetStatus status
    );

    // ==========================================
    // CONSULTAS PERSONALIZADAS
    // ==========================================

    /**
     * Presupuestos vigentes en una fecha
     */
    @Query("""
        SELECT p
        FROM Presupuesto p
        WHERE :fecha BETWEEN p.fechaInicio AND p.fechaFin
    """)
    List<Presupuesto> findPresupuestosVigentes(
            @Param("fecha") LocalDateTime fecha
    );

    /**
     * Obtiene el presupuesto activo actual de un usuario
     */
    @Query("""
        SELECT p
        FROM Presupuesto p
        WHERE p.usuario.id = :usuarioId
        AND p.status = :status
        AND :ahora BETWEEN p.fechaInicio AND p.fechaFin
        ORDER BY p.fechaCreacion DESC
    """)
    Optional<Presupuesto> findPresupuestoActivoActual(
            @Param("usuarioId") Long usuarioId,
            @Param("status") BudgetStatus status,
            @Param("ahora") LocalDateTime ahora
    );

    /**
     * Presupuestos próximos a vencer
     */
    @Query("""
        SELECT p
        FROM Presupuesto p
        WHERE p.status = :status
        AND p.fechaFin BETWEEN :ahora AND :fechaLimite
    """)
    List<Presupuesto> findPresupuestosProximosAVencer(
            @Param("status") BudgetStatus status,
            @Param("ahora") LocalDateTime ahora,
            @Param("fechaLimite") LocalDateTime fechaLimite
    );
}