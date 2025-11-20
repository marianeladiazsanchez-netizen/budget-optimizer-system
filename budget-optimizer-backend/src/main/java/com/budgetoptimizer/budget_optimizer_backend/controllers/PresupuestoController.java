package com.budgetoptimizer.budget_optimizer_backend.controllers;

import com.budgetoptimizer.budget_optimizer_backend.dto.presupuesto.*;
import com.budgetoptimizer.budget_optimizer_backend.enums.BudgetStatus;
import com.budgetoptimizer.budget_optimizer_backend.service.PresupuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de presupuestos
 * Endpoint base: /api/presupuestos
 */
@RestController
@RequestMapping("/api/presupuestos")
@RequiredArgsConstructor
@Slf4j
public class PresupuestoController {
    
    private final PresupuestoService presupuestoService;
    
    // ==========================================
    // CREACIÓN
    // ==========================================
    
    /**
     * Crea un nuevo presupuesto para un usuario
     * POST /api/presupuestos
     * 
     * @param dto Datos del presupuesto a crear
     * @return Presupuesto creado con código 201 CREATED
     */
    @PostMapping
    public ResponseEntity<PresupuestoResponseDTO> crearPresupuesto(
            @RequestParam Long usuarioId,
            @Valid @RequestBody CrearPresupuestoDTO dto) {
        
        log.info("POST /api/presupuestos - Usuario: {}, Nombre: {}", usuarioId, dto.getNombre());
        PresupuestoResponseDTO presupuesto = presupuestoService.crearPresupuesto(usuarioId, dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuesto);
    }
    
    // ==========================================
    // CONSULTAS Y BÚSQUEDAS
    // ==========================================
    
    /**
     * Busca un presupuesto por su ID
     * GET /api/presupuestos/{id}
     * 
     * @param id ID del presupuesto
     * @return Presupuesto encontrado con código 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoResponseDTO> buscarPorId(@PathVariable Long id) {
        log.info("GET /api/presupuestos/{}", id);
        PresupuestoResponseDTO presupuesto = presupuestoService.buscarPorId(id);
        
        return ResponseEntity.ok(presupuesto);
    }
    
    /**
     * Lista todos los presupuestos de un usuario
     * GET /api/presupuestos/usuario/{usuarioId}
     * 
     * @param usuarioId ID del usuario
     * @return Lista de presupuestos con código 200 OK
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PresupuestoResponseDTO>> listarPorUsuario(
            @PathVariable Long usuarioId) {
        
        log.info("GET /api/presupuestos/usuario/{}", usuarioId);
        List<PresupuestoResponseDTO> presupuestos = presupuestoService.listarPorUsuario(usuarioId);
        
        return ResponseEntity.ok(presupuestos);
    }
    
    /**
     * Lista presupuestos de un usuario filtrados por estado
     * GET /api/presupuestos/usuario/{usuarioId}/estado/{status}
     * 
     * @param usuarioId ID del usuario
     * @param status Estado del presupuesto (DRAFT, ACTIVE, COMPLETED, etc.)
     * @return Lista de presupuestos con código 200 OK
     */
    @GetMapping("/usuario/{usuarioId}/estado/{status}")
    public ResponseEntity<List<PresupuestoResponseDTO>> listarPorUsuarioYEstado(
            @PathVariable Long usuarioId,
            @PathVariable BudgetStatus status) {
        
        log.info("GET /api/presupuestos/usuario/{}/estado/{}", usuarioId, status);
        List<PresupuestoResponseDTO> presupuestos = 
            presupuestoService.listarPorUsuarioYEstado(usuarioId, status);
        
        return ResponseEntity.ok(presupuestos);
    }
    
    /**
     * Obtiene el presupuesto activo actual de un usuario
     * GET /api/presupuestos/usuario/{usuarioId}/actual
     * 
     * @param usuarioId ID del usuario
     * @return Presupuesto activo actual con código 200 OK
     */
    @GetMapping("/usuario/{usuarioId}/actual")
    public ResponseEntity<PresupuestoResponseDTO> buscarPresupuestoActual(
            @PathVariable Long usuarioId) {
        
        log.info("GET /api/presupuestos/usuario/{}/actual", usuarioId);
        PresupuestoResponseDTO presupuesto = presupuestoService.buscarPresupuestoActual(usuarioId);
        
        return ResponseEntity.ok(presupuesto);
    }
    
    // ==========================================
    // ACTUALIZACIÓN
    // ==========================================
    
    /**
     * Actualiza un presupuesto (solo en estado DRAFT)
     * PUT /api/presupuestos/{id}
     * 
     * @param id ID del presupuesto
     * @param dto Datos a actualizar
     * @return Presupuesto actualizado con código 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoResponseDTO> actualizarPresupuesto(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPresupuestoDTO dto) {
        
        log.info("PUT /api/presupuestos/{}", id);
        PresupuestoResponseDTO presupuesto = presupuestoService.actualizarPresupuesto(id, dto);
        
        return ResponseEntity.ok(presupuesto);
    }
    
    // ==========================================
    // CAMBIOS DE ESTADO
    // ==========================================
    
    /**
     * Activa un presupuesto (DRAFT/PAUSED → ACTIVE)
     * PUT /api/presupuestos/{id}/activar
     * 
     * @param id ID del presupuesto
     * @return Presupuesto activado con código 200 OK
     */
    @PutMapping("/{id}/activar")
    public ResponseEntity<PresupuestoResponseDTO> activarPresupuesto(@PathVariable Long id) {
        log.info("PUT /api/presupuestos/{}/activar", id);
        PresupuestoResponseDTO presupuesto = presupuestoService.activarPresupuesto(id);
        
        return ResponseEntity.ok(presupuesto);
    }
    
    /**
     * Pausa un presupuesto (ACTIVE/EXCEEDED → PAUSED)
     * PUT /api/presupuestos/{id}/pausar
     * 
     * @param id ID del presupuesto
     * @return Presupuesto pausado con código 200 OK
     */
    @PutMapping("/{id}/pausar")
    public ResponseEntity<PresupuestoResponseDTO> pausarPresupuesto(@PathVariable Long id) {
        log.info("PUT /api/presupuestos/{}/pausar", id);
        PresupuestoResponseDTO presupuesto = presupuestoService.pausarPresupuesto(id);
        
        return ResponseEntity.ok(presupuesto);
    }
    
    /**
     * Completa un presupuesto (cualquier estado → COMPLETED)
     * PUT /api/presupuestos/{id}/completar
     * 
     * @param id ID del presupuesto
     * @return Presupuesto completado con código 200 OK
     */
    @PutMapping("/{id}/completar")
    public ResponseEntity<PresupuestoResponseDTO> completarPresupuesto(@PathVariable Long id) {
        log.info("PUT /api/presupuestos/{}/completar", id);
        PresupuestoResponseDTO presupuesto = presupuestoService.completarPresupuesto(id);
        
        return ResponseEntity.ok(presupuesto);
    }
    
    // ==========================================
    // ELIMINACIÓN
    // ==========================================
    
    /**
     * Elimina un presupuesto (solo si está en DRAFT o sin gastos)
     * DELETE /api/presupuestos/{id}
     * 
     * @param id ID del presupuesto
     * @return Código 204 NO CONTENT si exitoso
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPresupuesto(@PathVariable Long id) {
        log.info("DELETE /api/presupuestos/{}", id);
        presupuestoService.eliminarPresupuesto(id);
        
        return ResponseEntity.noContent().build();
    }
    
    // ==========================================
    // ESTADÍSTICAS
    // ==========================================
    
    /**
     * Obtiene estadísticas de presupuestos de un usuario
     * GET /api/presupuestos/usuario/{usuarioId}/estadisticas
     * 
     * @param usuarioId ID del usuario
     * @return Estadísticas con código 200 OK
     */
    @GetMapping("/usuario/{usuarioId}/estadisticas")
    public ResponseEntity<EstadisticasPresupuestoDTO> obtenerEstadisticas(
            @PathVariable Long usuarioId) {
        
        log.info("GET /api/presupuestos/usuario/{}/estadisticas", usuarioId);
        EstadisticasPresupuestoDTO estadisticas = presupuestoService.obtenerEstadisticas(usuarioId);
        
        return ResponseEntity.ok(estadisticas);
    }
}