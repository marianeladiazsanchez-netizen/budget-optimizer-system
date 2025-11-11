package com.budgetoptimizer.budget_optimizer_backend.exception;

/**
 * Excepción cuando se intenta registrar un email que ya existe
 */
public class EmailYaRegistradoException extends RuntimeException {
    
    public EmailYaRegistradoException(String email) {
        super("El email '" + email + "' ya está registrado en el sistema");
    }
}