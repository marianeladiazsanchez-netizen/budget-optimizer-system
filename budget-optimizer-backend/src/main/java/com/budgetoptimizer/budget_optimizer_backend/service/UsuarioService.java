package com.budgetoptimizer.budget_optimizer_backend.service;

import com.budgetoptimizer.budget_optimizer_backend.dto.usuario.*;
import com.budgetoptimizer.budget_optimizer_backend.enums.AccountType;
import com.budgetoptimizer.budget_optimizer_backend.exception.*;
import com.budgetoptimizer.budget_optimizer_backend.model.*;
import com.budgetoptimizer.budget_optimizer_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para gestión de usuarios
 * ADAPTADO A LOS MODELOS EXISTENTES (sin modificarlos)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepo;
    private final CuentaRepository cuentaRepo;
    private final ServicioGeolocalizacion geoService;
    
    // ==========================================
    // REGISTRO Y CREACIÓN
    // ==========================================
    
    /**
     * Registra un nuevo usuario en el sistema
     * 
     * @param dto Datos del usuario a registrar
     * @return Usuario creado con su cuenta asociada
     * @throws EmailYaRegistradoException si el email ya existe
     * @throws GeolocalizacionException si no se pueden obtener coordenadas
     */
    public UsuarioResponseDTO registrarUsuario(RegistroUsuarioDTO dto) {
        log.info("Iniciando registro de usuario con email: {}", dto.getEmail());
        
        // 1. Validar email único
        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            log.error("Intento de registro con email duplicado: {}", dto.getEmail());
            throw new EmailYaRegistradoException(dto.getEmail());
        }
        
        // 2. Obtener coordenadas de ubicación
        Coordenada ubicacion;
        try {
            ubicacion = geoService.obtenerCoordenadas(dto.getCiudad(), dto.getPais());
            log.info("Coordenadas obtenidas: lat={}, lng={}", 
                ubicacion.getLatitud(), ubicacion.getLongitud());
        } catch (Exception e) {
            log.error("Error obteniendo coordenadas para {}, {}", dto.getCiudad(), dto.getPais(), e);
            throw new GeolocalizacionException(dto.getCiudad(), dto.getPais());
        }
        
        // 3. Crear usuario (usando campos que SÍ existen en el modelo)
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword()); // TODO: Encriptar con BCrypt cuando tengamos Security
        usuario.setUbicacion(ubicacion);
        usuario.setPresupuestoMensualBase(dto.getPresupuestoMensualBase());
        usuario.setAccountType(AccountType.USER); // Por defecto cuenta básica
        usuario.setActivo(true);
        // fechaCreacion se asigna automáticamente con @CreationTimestamp
        
        usuario = usuarioRepo.save(usuario);
        log.info("Usuario creado con ID: {}", usuario.getId());
        
        // 4. Crear cuenta asociada (usando campos que SÍ existen)
        Cuenta cuenta = new Cuenta();
        cuenta.setUsuario(usuario);
        cuenta.setTipoCuenta(AccountType.USER);
        cuenta.setSaldo(0.0);
        // fechaCreacion se asigna automáticamente con @CreationTimestamp
        
        cuentaRepo.save(cuenta);
        log.info("Cuenta creada para usuario ID: {}", usuario.getId());
        
        return convertirAResponse(usuario);
    }
    
    // ==========================================
    // CONSULTAS Y BÚSQUEDAS
    // ==========================================
    
    /**
     * Busca un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws UsuarioNoEncontradoException si no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        log.info("Buscando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        return convertirAResponse(usuario);
    }
    
    /**
     * Busca un usuario por su email
     * 
     * @param email Email del usuario
     * @return Usuario encontrado
     * @throws UsuarioNoEncontradoException si no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        log.info("Buscando usuario con email: {}", email);
        
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new UsuarioNoEncontradoException(email));
        
        return convertirAResponse(usuario);
    }
    
    /**
     * Lista todos los usuarios activos
     * 
     * @return Lista de usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        log.info("Listando todos los usuarios activos");
        
        return usuarioRepo.findByActivoTrue()
            .stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista usuarios por tipo de cuenta
     * 
     * @param accountType Tipo de cuenta
     * @return Lista de usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarPorTipoCuenta(AccountType accountType) {
        log.info("Listando usuarios con tipo de cuenta: {}", accountType);
        
        return usuarioRepo.findByAccountTypeAndActivoTrue(accountType)
            .stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Busca usuarios cercanos a una ubicación
     * 
     * @param latitud Latitud del punto de referencia
     * @param longitud Longitud del punto de referencia
     * @param radioKm Radio de búsqueda en kilómetros
     * @return Lista de usuarios cercanos
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> buscarCercanos(double latitud, double longitud, double radioKm) {
        log.info("Buscando usuarios en radio de {}km desde lat={}, lng={}", radioKm, latitud, longitud);
        
        return usuarioRepo.findUsuariosCercanos(latitud, longitud, radioKm)
            .stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }
    
    // ==========================================
    // ACTUALIZACIÓN
    // ==========================================
    
    /**
     * Actualiza los datos de un usuario
     * 
     * @param id ID del usuario
     * @param dto Datos a actualizar
     * @return Usuario actualizado
     * @throws UsuarioNoEncontradoException si no existe
     */
    public UsuarioResponseDTO actualizarUsuario(Long id, ActualizarUsuarioDTO dto) {
        log.info("Actualizando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        // Actualizar solo los campos que vengan (todos son opcionales)
        if (dto.getNombre() != null) {
            usuario.setNombre(dto.getNombre());
        }
        
        if (dto.getPresupuestoMensualBase() != null) {
            usuario.setPresupuestoMensualBase(dto.getPresupuestoMensualBase());
        }
        
        // Si cambió ciudad o país, actualizar ubicación
        if (dto.getCiudad() != null && dto.getPais() != null) {
            try {
                Coordenada nuevaUbicacion = geoService.obtenerCoordenadas(
                    dto.getCiudad(), 
                    dto.getPais()
                );
                usuario.setUbicacion(nuevaUbicacion);
                log.info("Ubicación actualizada: lat={}, lng={}", 
                    nuevaUbicacion.getLatitud(), nuevaUbicacion.getLongitud());
            } catch (Exception e) {
                log.error("Error actualizando ubicación", e);
                throw new GeolocalizacionException(dto.getCiudad(), dto.getPais());
            }
        }
        
        usuario = usuarioRepo.save(usuario);
        log.info("Usuario actualizado exitosamente: ID={}", id);
        
        return convertirAResponse(usuario);
    }
    
    /**
     * Cambia la contraseña de un usuario
     * 
     * @param id ID del usuario
     * @param dto Datos del cambio de contraseña
     * @throws UsuarioNoEncontradoException si no existe
     * @throws PasswordIncorrectaException si la contraseña actual es incorrecta
     * @throws IllegalArgumentException si las contraseñas no coinciden
     */
    public void cambiarPassword(Long id, CambiarPasswordDTO dto) {
        log.info("Cambiando contraseña para usuario ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        // Validar contraseña actual (TODO: Usar BCrypt cuando tengamos Security)
        if (!usuario.getPassword().equals(dto.getPasswordActual())) {
            log.error("Contraseña actual incorrecta para usuario ID: {}", id);
            throw new PasswordIncorrectaException();
        }
        
        // Validar que las contraseñas nuevas coincidan
        if (!dto.getPasswordNueva().equals(dto.getPasswordConfirmacion())) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
        }
        
        // Actualizar contraseña (TODO: Encriptar)
        usuario.setPassword(dto.getPasswordNueva());
        usuarioRepo.save(usuario);
        
        log.info("Contraseña actualizada para usuario ID: {}", id);
    }
    
    /**
     * Upgrade de cuenta de usuario
     * 
     * @param id ID del usuario
     * @param nuevoTipo Nuevo tipo de cuenta
     * @return Usuario con cuenta actualizada
     * @throws UsuarioNoEncontradoException si no existe
     * @throws IllegalArgumentException si el nuevo tipo no es superior
     */
    public UsuarioResponseDTO upgradeCuenta(Long id, AccountType nuevoTipo) {
        log.info("Upgrade de cuenta para usuario ID: {} a tipo: {}", id, nuevoTipo);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        // Validar que sea un upgrade
        if (!nuevoTipo.esSuperiorA(usuario.getAccountType())) {
            throw new IllegalArgumentException(
                "El nuevo tipo de cuenta debe ser superior al actual"
            );
        }
        
        usuario.setAccountType(nuevoTipo);
        
        // Actualizar también la cuenta asociada
        Cuenta cuenta = cuentaRepo.findByUsuarioId(id)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        cuenta.setTipoCuenta(nuevoTipo);
        cuentaRepo.save(cuenta);
        
        usuario = usuarioRepo.save(usuario);
        log.info("Upgrade exitoso. Usuario ID: {} ahora es {}", id, nuevoTipo);
        
        return convertirAResponse(usuario);
    }
    
    // ==========================================
    // DESACTIVACIÓN Y ELIMINACIÓN
    // ==========================================
    
    /**
     * Desactiva un usuario (soft delete)
     * 
     * @param id ID del usuario
     */
    public void desactivarUsuario(Long id) {
        log.info("Desactivando usuario ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        usuario.setActivo(false);
        usuarioRepo.save(usuario);
        
        log.info("Usuario desactivado: ID={}", id);
    }
    
    /**
     * Reactiva un usuario desactivado
     * 
     * @param id ID del usuario
     */
    public void reactivarUsuario(Long id) {
        log.info("Reactivando usuario ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        usuario.setActivo(true);
        usuarioRepo.save(usuario);
        
        log.info("Usuario reactivado: ID={}", id);
    }
    
    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================
    
    /**
     * Convierte una entidad Usuario a UsuarioResponseDTO
     * USA SOLO LOS CAMPOS QUE EXISTEN EN EL MODELO
     */
    private UsuarioResponseDTO convertirAResponse(Usuario usuario) {
        AccountType accountType = usuario.getAccountType();
        
        return UsuarioResponseDTO.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .email(usuario.getEmail())
            .telefono(null) // Campo no existe en modelo actual
            // Ubicación
            .latitud(usuario.getUbicacion() != null ? usuario.getUbicacion().getLatitud() : null)
            .longitud(usuario.getUbicacion() != null ? usuario.getUbicacion().getLongitud() : null)
            .ciudad(null) // Campo no existe en modelo actual
            .pais(null) // Campo no existe en modelo actual
            // Cuenta
            .accountType(accountType)
            .accountTypeDisplay(accountType.getDisplayName())
            .presupuestoMensualBase(usuario.getPresupuestoMensualBase())
            .transaccionesMesActual(0) // Campo no existe en modelo actual
            .limiteTransaccionesMes(accountType.getLimiteTransaccionesMes())
            // Beneficios
            .descuentoPorcentaje(accountType.getDescuentoPorcentaje())
            .tieneBeneficios(accountType.getTieneBeneficios())
            .puedeAdministrarEmpresas(accountType.getPuedeAdministrarEmpresas())
            // Estado
            .activo(usuario.getActivo())
            .fechaCreacion(usuario.getFechaCreacion())
            .ultimoAcceso(null) // Campo no existe en modelo actual
            // Estadísticas
            .totalPresupuestos(usuario.getPresupuestos() != null ? usuario.getPresupuestos().size() : 0)
            .totalGastos(usuario.getExpenses() != null ? usuario.getExpenses().size() : 0)
            .totalReviews(usuario.getReviews() != null ? usuario.getReviews().size() : 0)
            .build();
    }
}