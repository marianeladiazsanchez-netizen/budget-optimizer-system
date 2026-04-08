package com.budgetoptimizer.budget_optimizer_backend.exception;

import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;

/**
 * Excepción cuando un usuario alcanza su límite de transacciones mensuales
 */
public class LimiteTransaccionesException extends RuntimeException {
    
    public LimiteTransaccionesException(String mensaje) {
        super(mensaje);
    }
    
    public LimiteTransaccionesException(AccountType accountType, Integer realizadas) {
        super(String.format(
            "Has alcanzado el límite de %d transacciones mensuales para cuentas %s. " +
            "Transacciones realizadas: %d. Considera actualizar tu cuenta.",
            accountType.getLimiteTransaccionesMes(),
            accountType.getDisplayName(),
            realizadas
        ));
    }
}