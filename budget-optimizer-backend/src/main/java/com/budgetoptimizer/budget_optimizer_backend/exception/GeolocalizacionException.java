package com.budgetoptimizer.budget_optimizer_backend.exception;

/**
 * Excepción cuando falla el servicio de geolocalización
 */
public class GeolocalizacionException extends RuntimeException {
    
    public GeolocalizacionException(String ciudad, String pais) {
        super(String.format(
            "No se pudieron obtener las coordenadas para: %s, %s. " +
            "Verifica que la ciudad y el país sean correctos.",
            ciudad, pais
        ));
    }
    
    public GeolocalizacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}