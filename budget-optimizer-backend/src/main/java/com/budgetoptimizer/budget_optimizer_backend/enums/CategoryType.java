package com.budgetoptimizer.budget_optimizer_backend.enums;

import lombok.Getter;
/**
 * Enum para diferenciar el uso de las categorías
 * Permite que una categoría sirva para gastos, empresas o ambos
 */
@Getter
public enum CategoryType {
    
    INCOME(
        "Ingreso",
        "Categoría para ingresos del usuario",
        "💰",
        true,
        false
    ),

    EXPENSE(
        "Gasto",
        "Categoría para gastos del usuario",
        "📝",
        true,
        false
    ),

    BUSINESS(
        "Empresa",
        "Categoría para empresas",
        "🏢",
        false,
        true
    ),

    BOTH(
        "Ambos",
        "Categoría versátil",
        "🔄",
        true,
        true
    );
    
    private final String displayName;
    private final String descripcion;
    private final String icono;
    private final Boolean aplicaParaGastos;
    private final Boolean aplicaParaEmpresas;
    
    CategoryType(String displayName, String descripcion, String icono,
                 Boolean aplicaParaGastos, Boolean aplicaParaEmpresas) {
        this.displayName = displayName;
        this.descripcion = descripcion;
        this.icono = icono;
        this.aplicaParaGastos = aplicaParaGastos;
        this.aplicaParaEmpresas = aplicaParaEmpresas;
    }
    
    /**
     * Verifica si la categoría se puede usar para registrar gastos
     */
    public Boolean puedeUsarseParaGastos() {
        return aplicaParaGastos;
    }
    
    /**
     * Verifica si la categoría se puede usar para clasificar empresas
     */
    public Boolean puedeUsarseParaEmpresas() {
        return aplicaParaEmpresas;
    }
    
    /**
     * Verifica si es versátil (sirve para ambos propósitos)
     */
    public Boolean esVersatil() {
        return this == BOTH;
    }
    
    /**
     * Busca un CategoryType por nombre
     */
    public static CategoryType fromString(String nombre) {
        for (CategoryType tipo : CategoryType.values()) {
            if (tipo.name().equalsIgnoreCase(nombre) || 
                tipo.displayName.equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de categoría no válido: " + nombre);
    }
}
