package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.model.Expense;
import com.budgetoptimizer.budget_optimizer_backend.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    // ==========================================
    // QUERY METHODS AUTOMÁTICOS
    // ==========================================
    
    // Por usuario
    List<Expense> findByUsuarioId(Long usuarioId);
    List<Expense> findByUsuarioIdOrderByFechaGastoDesc(Long usuarioId);
    
    // Por presupuesto
    List<Expense> findByPresupuestoId(Long presupuestoId);
    
    // Por categoría
    List<Expense> findByCategoriaId(Long categoriaId);
    List<Expense> findByUsuarioIdAndCategoriaId(Long usuarioId, Long categoriaId);
    
    // ⭐ NUEVOS: Combinaciones para filtros avanzados
    List<Expense> findByPresupuestoIdAndCategoriaId(Long presupuestoId, Long categoriaId);
    List<Expense> findByPresupuestoIdAndFechaGastoBetween(Long presupuestoId, LocalDateTime inicio, LocalDateTime fin);
    
    // Por empresa
    List<Expense> findByEmpresaId(String empresaId);
    List<Expense> findByUsuarioIdAndEmpresaId(Long usuarioId, String empresaId);
    
    // Por método de pago
    List<Expense> findByMetodoPago(PaymentMethod metodoPago);
    List<Expense> findByUsuarioIdAndMetodoPago(Long usuarioId, PaymentMethod metodoPago);
    
    // Por fechas
    List<Expense> findByFechaGastoBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Expense> findByUsuarioIdAndFechaGastoBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);
    
    // Por monto
    List<Expense> findByMontoGreaterThan(Double monto);
    List<Expense> findByMontoBetween(Double min, Double max);
    List<Expense> findTop10ByUsuarioIdOrderByMontoDesc(Long usuarioId);
    
    // Por descripción
    List<Expense> findByDescripcionContainingIgnoreCase(String keyword);
    List<Expense> findByUsuarioIdAndDescripcionContainingIgnoreCase(Long usuarioId, String keyword);
    
    // ==========================================
    // @Query PARA AGREGACIONES Y CÁLCULOS
    // ==========================================
    
    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Expense e WHERE e.presupuesto.id = :presupuestoId")
    Double sumMontoByPresupuestoId(@Param("presupuestoId") Long presupuestoId);
    
    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Expense e WHERE e.categoria.id = :categoriaId")
    Double sumMontoByCategoriaId(@Param("categoriaId") Long categoriaId);
    
    @Query("SELECT e.categoria.nombre, SUM(e.monto) " +
           "FROM Expense e " +
           "WHERE e.usuario.id = :usuarioId " +
           "GROUP BY e.categoria.nombre " +
           "ORDER BY SUM(e.monto) DESC")
    List<Object[]> findGastosPorCategoria(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT e.categoria.nombre, AVG(e.monto) " +
           "FROM Expense e " +
           "WHERE e.usuario.id = :usuarioId " +
           "GROUP BY e.categoria.nombre")
    List<Object[]> findPromedioGastoPorCategoria(@Param("usuarioId") Long usuarioId);
}