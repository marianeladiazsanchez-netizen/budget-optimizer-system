package com.budgetoptimizer.budget_optimizer_backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.budgetoptimizer.budget_optimizer_backend.model.MLOptimization;
import com.budgetoptimizer.budget_optimizer_backend.enums.OptimizationType;
import java.util.List;

@Repository
public interface MLOptimizationRepository extends JpaRepository<MLOptimization, Long> {
    List<MLOptimization> findByUsuario_Id(Long usuarioId);
    List<MLOptimization> findByUsuario_IdAndTipo(Long usuarioId, OptimizationType tipo);
    List<MLOptimization> findByUsuario_IdAndAplicadaFalse(Long usuarioId);
    List<MLOptimization> findByPresupuesto_Id(Long presupuestoId);
}
