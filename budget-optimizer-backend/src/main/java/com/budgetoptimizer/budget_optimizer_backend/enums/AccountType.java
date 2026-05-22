package com.budgetoptimizer.budget_optimizer_backend.enums;

import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Enum unificado para tipos de cuenta/roles de usuario
 * Combina permisos, beneficios y comportamientos en una sola estructura
 * 
 * Reemplaza a: UserRole y TipoCuenta
 */
@Getter
public enum AccountType {
    
    USER(
        "Usuario Básico",
        "Usuario estándar con acceso básico",
        BigDecimal.valueOf(0), // Sin descuento
        100,                    // 100 transacciones/mes
        1,                      // Nivel de acceso básico
        false,                  // No tiene beneficios premium
        false,                  // No puede administrar empresas
        "Cuenta gratuita con funciones esenciales"
    ),
    
    PREMIUM(
        "Usuario Premium",
        "Usuario premium con funciones avanzadas y descuentos",
        BigDecimal.valueOf(10),                   // 10% descuento
        1000,                   // 1000 transacciones/mes
        2,                      // Nivel de acceso medio
        true,                   // Tiene beneficios premium
        false,                  // No puede administrar empresas
        "Cuenta premium con máximos beneficios, descuentos y análisis ML avanzado"
    ),
    
    BUSINESS(
        "Empresa/Vendedor",
        "Usuario tipo empresa que puede administrar su negocio",
        BigDecimal.valueOf(5),                    // 5% descuento
        500,                    // 500 transacciones/mes
        2,                      // Nivel de acceso medio
        true,                   // Tiene algunos beneficios
        true,                   // Puede administrar empresas
        "Cuenta para vendedores y empresas con comisiones reducidas"
    ),
    
    ADMIN(
        "Administrador",
        "Administrador del sistema con acceso completo",
        BigDecimal.valueOf(15),                   // 15% descuento (máximo)
        999999,                 // Sin límite práctico
        3,                      // Nivel de acceso máximo
        true,                   // Todos los beneficios
        true,                   // Puede administrar empresas
        "Administrador del sistema con permisos totales"
    );
    
    // ============================================
    // ATRIBUTOS
    // ============================================
    
    private final String displayName;
    private final String descripcion;
    private final BigDecimal descuentoPorcentaje;
    private final Integer limiteTransaccionesMes;
    private final Integer nivelAcceso;
    private final Boolean tieneBeneficios;
    private final Boolean puedeAdministrarEmpresas;
    private final String descripcionDetallada;
    
    // Constructor
    AccountType(String displayName, String descripcion, BigDecimal descuentoPorcentaje,
                Integer limiteTransaccionesMes, Integer nivelAcceso, 
                Boolean tieneBeneficios, Boolean puedeAdministrarEmpresas,
                String descripcionDetallada) {
        this.displayName = displayName;
        this.descripcion = descripcion;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.limiteTransaccionesMes = limiteTransaccionesMes;
        this.nivelAcceso = nivelAcceso;
        this.tieneBeneficios = tieneBeneficios;
        this.puedeAdministrarEmpresas = puedeAdministrarEmpresas;
        this.descripcionDetallada = descripcionDetallada;
    }
    
    // ============================================
    // MÉTODOS DE PERMISOS (de UserRole)
    // ============================================
    
    /**
     * Verifica si tiene permisos de administrador
     */
    public Boolean esAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Verifica si tiene acceso premium
     */
    public Boolean tienePremium() {
        return this == PREMIUM || this == ADMIN;
    }
    
    /**
     * Verifica si tiene nivel de acceso superior a otro tipo
     */
    public Boolean esSuperiorA(AccountType otroTipo) {
        return this.nivelAcceso > otroTipo.nivelAcceso;
    }
    
    /**
     * Verifica si es una cuenta de negocio
     */
    public Boolean esCuentaNegocio() {
        return this == BUSINESS || this == ADMIN;
    }
    
    // ============================================
    // MÉTODOS DE DESCUENTOS (de TipoCuenta)
    // ============================================
    
    /**
     * Calcula el monto con descuento aplicado según el tipo de cuenta
     */
    public BigDecimal aplicarDescuento(BigDecimal montoOriginal) {
        if (montoOriginal == null ||
        montoOriginal.compareTo(BigDecimal.ZERO) <= 0) {
        return montoOriginal;
    }

    BigDecimal porcentajeDescuento =
            descuentoPorcentaje.divide(
                    BigDecimal.valueOf(100)
            );

          return montoOriginal.multiply(BigDecimal.ONE.subtract(porcentaje));  
    }
    
    /**
     * Calcula el ahorro obtenido con el descuento
     */
    public BigDecimal calcularAhorro(BigDecimal montoOriginal) {
         if (montoOriginal == null ||
        montoOriginal.compareTo(BigDecimal.ZERO) <= 0) {
        return BigDecimal.ZERO;
    }

    return montoOriginal.multiply(descuentoPorcentaje.divide(BigDecimal.valueOf(100))); 
    }
    
    /**
     * Calcula el monto final después de aplicar descuento
     * (Alias más semántico de aplicarDescuento)
     */
    public BigDecimal calcularMontoFinal(BigDecimal montoOriginal) {
        return aplicarDescuento(montoOriginal);
    }
    
    // ============================================
    // MÉTODOS DE LÍMITES Y TRANSACCIONES
    // ============================================
    
    /**
     * Verifica si el tipo de cuenta puede realizar una transacción
     */
    public Boolean puedeRealizarTransaccion(Integer transaccionesRealizadas) {
        if (transaccionesRealizadas == null) {
            return true;
        }
        return transaccionesRealizadas < limiteTransaccionesMes;
    }
    
    /**
     * Calcula el porcentaje de transacciones utilizadas
     */
    public Double calcularPorcentajeTransaccionesUsadas(Integer transaccionesRealizadas) {
        if (transaccionesRealizadas == null || limiteTransaccionesMes == 0) {
            return 0.0;
        }
        return (transaccionesRealizadas.doubleValue() / limiteTransaccionesMes) * 100;
    }
    
    /**
     * Calcula transacciones restantes en el mes
     */
    public Integer calcularTransaccionesRestantes(Integer transaccionesRealizadas) {
        if (transaccionesRealizadas == null) {
            return limiteTransaccionesMes;
        }
        return Math.max(0, limiteTransaccionesMes - transaccionesRealizadas);
    }
    
    /**
     * Verifica si está cerca del límite de transacciones (>80%)
     */
    public Boolean estaCercaDelLimite(Integer transaccionesRealizadas) {
        return calcularPorcentajeTransaccionesUsadas(transaccionesRealizadas) >= 80.0;
    }
    
    // ============================================
    // MÉTODOS DE UPGRADE/COMPARACIÓN
    // ============================================
    
    /**
     * Obtiene el siguiente nivel de cuenta (upgrade)
     */
    public AccountType obtenerSiguienteNivel() {
        AccountType[] valores = AccountType.values();
        int indiceActual = this.ordinal();
        if (indiceActual < valores.length - 1) {
            return valores[indiceActual + 1];
        }
        return this; // Ya está en el nivel máximo
    }
    
    /**
     * Verifica si puede hacer upgrade
     */
    public Boolean puedeHacerUpgrade() {
        return this != ADMIN; // ADMIN es el máximo nivel
    }
    
    /**
     * Calcula el beneficio de upgrade (diferencia de descuento)
     */
    public BigDecimal calcularBeneficioUpgrade(BigDecimal gastoMensualPromedio) {
         if (!puedeHacerUpgrade()
            || gastoMensualPromedio == null
            || gastoMensualPromedio.compareTo(BigDecimal.ZERO) <= 0) {

        return BigDecimal.ZERO;
    }

    AccountType siguienteNivel = obtenerSiguienteNivel();

    BigDecimal ahorroActual =
            this.calcularAhorro(gastoMensualPromedio);

    BigDecimal ahorroSiguiente =
            siguienteNivel.calcularAhorro(gastoMensualPromedio);

    return ahorroSiguiente.subtract(ahorroActual);
    }
    
    // ============================================
    // MÉTODOS DE UTILIDAD
    // ============================================
    
    /**
     * Obtiene el icono representativo del tipo de cuenta
     */
    public String getIcono() {
        switch (this) {
            case USER: return "👤";
            case PREMIUM: return "⭐";
            case BUSINESS: return "🏢";
            case ADMIN: return "🔧";
            default: return "❓";
        }
    }
    
    /**
     * Obtiene el color representativo para UI
     */
    public String getColorHex() {
        switch (this) {
            case USER: return "#9E9E9E";      // Gris
            case PREMIUM: return "#FFD700";   // Dorado
            case BUSINESS: return "#2196F3";  // Azul
            case ADMIN: return "#F44336";     // Rojo
            default: return "#000000";
        }
    }
    
    /**
     * Obtiene una descripción completa con todos los beneficios
     */
    public String getDescripcionCompleta() {
        return String.format(
            "%s %s - %s\n" +
            "• Descuento: %.1f%%\n" +
            "• Límite transacciones: %d/mes\n" +
            "• Nivel de acceso: %d\n" +
            "• Beneficios premium: %s\n" +
            "• Administrar empresas: %s",
            getIcono(),
            displayName,
            descripcionDetallada,
            descuentoPorcentaje,
            limiteTransaccionesMes,
            nivelAcceso,
            tieneBeneficios ? "Sí" : "No",
            puedeAdministrarEmpresas ? "Sí" : "No"
        );
    }
    
    /**
     * Busca un AccountType por su nombre (case-insensitive)
     */
    public static AccountType fromString(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser null");
        }
        
        for (AccountType tipo : AccountType.values()) {
            if (tipo.name().equalsIgnoreCase(nombre) || 
                tipo.displayName.equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        
        throw new IllegalArgumentException("Tipo de cuenta no válido: " + nombre);
    }
    
    /**
     * Obtiene el tipo de cuenta recomendado según gasto mensual
     */
    public static AccountType recomendarSegunGasto(BigDecimal gastoMensual) {
        if (gastoMensual.compareTo(BigDecimal.valueOf(BigDecimal.ZERO)) <= 0) {
            return USER;
        }
        
        // Si gasta menos de $500, cuenta básica
        if (gastoMensual.compareTo(BigDecimal.valueOf(500)) < 0) {
            return USER;
        }
        
        // Si gasta entre $500-2000, calcular si premium vale la pena
        if (gastoMensual.compareTo(BigDecimal.valueOf(2000)) <= 0) {
            // Premium ahorra 10%, si ahorra más de $10/mes vale la pena
            BigDecimal ahorroPremium = PREMIUM.calcularAhorro(gastoMensual);
            return ahorroPremium.compareTo(BigDecimal.valueOf(10)) > 0? PREMIUM: USER;
        }
        
        // Si gasta más de $2000, definitivamente premium
        return PREMIUM;
    }
}