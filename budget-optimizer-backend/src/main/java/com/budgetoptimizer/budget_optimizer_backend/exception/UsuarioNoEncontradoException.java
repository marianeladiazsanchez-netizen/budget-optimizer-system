package com.budgetoptimizer.budget_optimizer_backend.exception;

/**
 * Excepción cuando un usuario no existe en la base de datos
 */
public class UsuarioNoEncontradoException extends RuntimeException {
    
    public UsuarioNoEncontradoException(Long id) {
        super("Usuario con ID " + id + " no encontrado");
    }
    
    public UsuarioNoEncontradoException(String email) {
        super("Usuario con email '" + email + "' no encontrado");
    }
    
    public UsuarioNoEncontradoException(String campo, String valor) {
        super("Usuario con " + campo + " '" + valor + "' no encontrado");
    }
}