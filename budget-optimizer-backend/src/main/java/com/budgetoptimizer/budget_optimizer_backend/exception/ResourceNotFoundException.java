package com.budgetoptimizer.budget_optimizer_backend.exception;

/**
 * Excepción cuando un recurso genérico no existe en la base de datos
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
    
    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " con ID " + id + " no encontrado");
    }
    
    public ResourceNotFoundException(String recurso, String campo, String valor) {
        super(recurso + " con " + campo + " '" + valor + "' no encontrado");
    }
}