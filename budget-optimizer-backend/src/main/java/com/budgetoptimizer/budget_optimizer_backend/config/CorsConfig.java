package com.budgetoptimizer.budget_optimizer_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuración global de CORS para permitir peticiones desde el frontend
 * 
 * CRÍTICO: Sin esto, el navegador bloqueará todas las peticiones desde 
 * localhost:5173 hacia localhost:8080 por política de Same-Origin
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // ✅ Permitir credenciales (cookies, headers de autenticación)
        config.setAllowCredentials(true);
        
        // ✅ Orígenes permitidos (frontend)
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",           // Frontend desarrollo local
            "http://localhost:*",              // Cualquier puerto localhost
            "http://127.0.0.1:*"               // IPv4 loopback
        ));
        
        // ✅ Headers permitidos
        config.setAllowedHeaders(Arrays.asList(
            "Origin",
            "Content-Type",
            "Accept",
            "Authorization",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // ✅ Headers expuestos (que el frontend puede leer)
        config.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        // ✅ Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));
        
        // ✅ Tiempo de cache de preflight (segundos)
        config.setMaxAge(3600L);
        
        // Aplicar configuración a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}