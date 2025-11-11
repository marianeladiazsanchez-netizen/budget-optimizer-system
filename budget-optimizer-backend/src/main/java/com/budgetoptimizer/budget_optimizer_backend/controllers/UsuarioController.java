package com.budgetoptimizer.budget_optimizer_backend.controllers;

import com.budgetoptimizer.budget_optimizer_backend.dto.usuario.*;
import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;
import com.budgetoptimizer.budget_optimizer_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios
 * Endpoint base: /api/usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    
    // ==========================================
    // REGISTRO Y CREACIÓN
    // ==========================================
    
    /**
     * Registra un nuevo usuario
     * POST /api/usuarios/registro
     * 
     * @param dto Datos del usuario a registrar
     * @return Usuario creado con código 201 CREATED
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(
            @Valid @RequestBody RegistroUsuarioDTO dto) {
        
        log.info("POST /api/usuarios/registro - Email: {}", dto.getEmail());
        UsuarioResponseDTO usuario = usuarioService.registrarUsuario(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }
    
    // ==========================================
    // CONSULTAS Y BÚSQUEDAS
    // ==========================================
    
    /**
     * Busca un usuario por su ID
     * GET /api/usuarios/{id}
     * 
     * @param id ID del usuario
     * @return Usuario encontrado con código 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        log.info("GET /api/usuarios/{}", id);
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        
        return ResponseEntity.ok(usuario);
    }
    
    /**
     * Busca un usuario por su email
     * GET /api/usuarios/email/{email}
     * 
     * @param email Email del usuario
     * @return Usuario encontrado con código 200 OK
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@PathVariable String email) {
        log.info("GET /api/usuarios/email/{}", email);
        UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
        
        return ResponseEntity.ok(usuario);
    }
    
    /**
     * Lista todos los usuarios activos
     * GET /api/usuarios
     * 
     * @return Lista de usuarios con código 200 OK
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        log.info("GET /api/usuarios - Listando todos");
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
        
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * Lista usuarios por tipo de cuenta
     * GET /api/usuarios/tipo/{accountType}
     * 
     * @param accountType Tipo de cuenta (USER, PREMIUM, BUSINESS, ADMIN)
     * @return Lista de usuarios con código 200 OK
     */
    @GetMapping("/tipo/{accountType}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorTipo(
            @PathVariable AccountType accountType) {
        
        log.info("GET /api/usuarios/tipo/{}", accountType);
        List<UsuarioResponseDTO> usuarios = usuarioService.listarPorTipoCuenta(accountType);
        
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * Busca usuarios cercanos a una ubicación
     * GET /api/usuarios/cercanos?lat={lat}&lng={lng}&radio={km}
     * 
     * @param lat Latitud
     * @param lng Longitud
     * @param radio Radio de búsqueda en kilómetros
     * @return Lista de usuarios cercanos con código 200 OK
     */
    @GetMapping("/cercanos")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarCercanos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10.0") double radio) {
        
        log.info("GET /api/usuarios/cercanos - lat={}, lng={}, radio={}km", lat, lng, radio);
        List<UsuarioResponseDTO> usuarios = usuarioService.buscarCercanos(lat, lng, radio);
        
        return ResponseEntity.ok(usuarios);
    }
    
    // ==========================================
    // ACTUALIZACIÓN
    // ==========================================
    
    /**
     * Actualiza los datos de un usuario
     * PUT /api/usuarios/{id}
     * 
     * @param id ID del usuario
     * @param dto Datos a actualizar
     * @return Usuario actualizado con código 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioDTO dto) {
        
        log.info("PUT /api/usuarios/{}", id);
        UsuarioResponseDTO usuario = usuarioService.actualizarUsuario(id, dto);
        
        return ResponseEntity.ok(usuario);
    }
    
    /**
     * Cambia la contraseña de un usuario
     * PUT /api/usuarios/{id}/password
     * 
     * @param id ID del usuario
     * @param dto Datos del cambio de contraseña
     * @return Código 204 NO CONTENT si exitoso
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordDTO dto) {
        
        log.info("PUT /api/usuarios/{}/password", id);
        usuarioService.cambiarPassword(id, dto);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Hace upgrade de la cuenta de un usuario
     * PUT /api/usuarios/{id}/upgrade
     * 
     * @param id ID del usuario
     * @param nuevoTipo Nuevo tipo de cuenta
     * @return Usuario con cuenta actualizada con código 200 OK
     */
    @PutMapping("/{id}/upgrade")
    public ResponseEntity<UsuarioResponseDTO> upgradeCuenta(
            @PathVariable Long id,
            @RequestParam AccountType nuevoTipo) {
        
        log.info("PUT /api/usuarios/{}/upgrade - Nuevo tipo: {}", id, nuevoTipo);
        UsuarioResponseDTO usuario = usuarioService.upgradeCuenta(id, nuevoTipo);
        
        return ResponseEntity.ok(usuario);
    }
    
    // ==========================================
    // DESACTIVACIÓN Y ELIMINACIÓN
    // ==========================================
    
    /**
     * Desactiva un usuario (soft delete)
     * DELETE /api/usuarios/{id}
     * 
     * @param id ID del usuario
     * @return Código 204 NO CONTENT si exitoso
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        log.info("DELETE /api/usuarios/{}", id);
        usuarioService.desactivarUsuario(id);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Reactiva un usuario desactivado
     * PUT /api/usuarios/{id}/reactivar
     * 
     * @param id ID del usuario
     * @return Código 204 NO CONTENT si exitoso
     */
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivarUsuario(@PathVariable Long id) {
        log.info("PUT /api/usuarios/{}/reactivar", id);
        usuarioService.reactivarUsuario(id);
        
        return ResponseEntity.noContent().build();
    }
}