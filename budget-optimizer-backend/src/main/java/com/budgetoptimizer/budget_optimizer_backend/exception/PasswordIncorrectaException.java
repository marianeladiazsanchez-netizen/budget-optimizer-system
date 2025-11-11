package com.budgetoptimizer.budget_optimizer_backend.exception;

/**
 * Excepción cuando la contraseña proporcionada es incorrecta
 */
public class PasswordIncorrectaException extends RuntimeException {
    
    public PasswordIncorrectaException() {
        super("La contraseña actual es incorrecta");
    }
    
    public PasswordIncorrectaException(String mensaje) {
        super(mensaje);
    }
}