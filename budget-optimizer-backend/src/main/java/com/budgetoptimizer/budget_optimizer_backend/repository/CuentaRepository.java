package com.budgetoptimizer.budget_optimizer_backend.repository;

import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;
import com.budgetoptimizer.budget_optimizer_backend.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    // ==========================================
    // BÚSQUEDAS BÁSICAS
    // ==========================================

    Optional<Cuenta> findByUsuario_Id(Long usuarioId);

    List<Cuenta> findByTipoCuenta(AccountType tipoCuenta);

    List<Cuenta> findBySaldoGreaterThan(BigDecimal saldo);

    List<Cuenta> findBySaldoLessThan(BigDecimal saldo);

    List<Cuenta> findBySaldoGreaterThanEqual(BigDecimal saldo);

    boolean existsByUsuario_Id(Long usuarioId);
}