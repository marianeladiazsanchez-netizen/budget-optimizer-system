package com.budgetoptimizer.budget_optimizer_backend.exception;

import com.budgetoptimizer.budget_optimizer_backend.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la aplicación
 * Captura excepciones y las convierte en respuestas HTTP consistentes
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Usuario no encontrado (404 Not Found)
     */
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoEncontrado(
            UsuarioNoEncontradoException ex, 
            HttpServletRequest request) {
        
        log.error("Usuario no encontrado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .mensaje(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Email ya registrado (409 Conflict)
     */
    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<ErrorResponse> handleEmailYaRegistrado(
            EmailYaRegistradoException ex, 
            HttpServletRequest request) {
        
        log.error("Email duplicado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .mensaje(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    /**
     * Contraseña incorrecta (401 Unauthorized)
     */
    @ExceptionHandler(PasswordIncorrectaException.class)
    public ResponseEntity<ErrorResponse> handlePasswordIncorrecta(
            PasswordIncorrectaException ex, 
            HttpServletRequest request) {
        
        log.error("Password incorrecta: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .mensaje(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Límite de transacciones excedido (429 Too Many Requests)
     */
    @ExceptionHandler(LimiteTransaccionesException.class)
    public ResponseEntity<ErrorResponse> handleLimiteTransacciones(
            LimiteTransaccionesException ex, 
            HttpServletRequest request) {
        
        log.warn("Límite de transacciones alcanzado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error("Too Many Requests")
            .mensaje(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }
    
    /**
     * Error de geolocalización (503 Service Unavailable)
     */
    @ExceptionHandler(GeolocalizacionException.class)
    public ResponseEntity<ErrorResponse> handleGeolocalizacion(
            GeolocalizacionException ex, 
            HttpServletRequest request) {
        
        log.error("Error de geolocalización: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error("Service Unavailable")
            .mensaje(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * Errores de validación (@Valid) (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        List<String> errores = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        log.error("Errores de validación: {}", errores);
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .mensaje("Errores de validación en los datos enviados")
            .path(request.getRequestURI())
            .errores(errores)
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Argumentos ilegales (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        log.error("Argumento ilegal: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .mensaje(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Errores generales no capturados (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Error interno del servidor", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .mensaje("Ha ocurrido un error inesperado. Por favor, contacta al soporte.")
            .path(request.getRequestURI())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}