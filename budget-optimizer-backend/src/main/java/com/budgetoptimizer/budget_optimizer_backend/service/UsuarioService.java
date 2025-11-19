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
            log.error("Error obteniendo coordenadas para {}, {}", 
                dto.getCiudad(), dto.getPais(), e);
            throw new GeolocalizacionException(dto.getCiudad(), dto.getPais());
        }
        
        // 3. Crear usuario con Builder
        Usuario usuario = Usuario.builder()
            .nombre(dto.getNombre())
            .email(dto.getEmail())
            .password(dto.getPassword()) // TODO: Encriptar con BCrypt
            .telefono(dto.getTelefono())
            .ubicacion(ubicacion)
            .ciudad(dto.getCiudad())
            .pais(dto.getPais())
            .presupuestoMensualBase(dto.getPresupuestoMensualBase())
            .accountType(AccountType.USER)
            .activo(true)
            .transaccionesMesActual(0)
            .build();
        
        usuario = usuarioRepo.save(usuario);
        log.info("Usuario creado con ID: {}", usuario.getId());
        
        // 4. Crear cuenta asociada
        Cuenta cuenta = new Cuenta();
        cuenta.setUsuario(usuario);
        cuenta.setTipoCuenta(AccountType.USER);
        cuenta.setSaldo(0.0);
        
        cuentaRepo.save(cuenta);
        log.info("Cuenta creada para usuario ID: {}", usuario.getId());
        
        return convertirAResponse(usuario);
    }
    
    // ==========================================
    // CONSULTAS Y BÚSQUEDAS
    // ==========================================
    
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        log.info("Buscando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        return convertirAResponse(usuario);
    }
    
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        log.info("Buscando usuario con email: {}", email);
        
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new UsuarioNoEncontradoException(email));
        
        return convertirAResponse(usuario);
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        log.info("Listando todos los usuarios activos");
        
        return usuarioRepo.findByActivoTrue()
            .stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarPorTipoCuenta(AccountType accountType) {
        log.info("Listando usuarios con tipo de cuenta: {}", accountType);
        
        return usuarioRepo.findByAccountTypeAndActivoTrue(accountType)
            .stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> buscarCercanos(double latitud, double longitud, double radioKm) {
        log.info("Buscando usuarios en radio de {}km desde lat={}, lng={}", 
            radioKm, latitud, longitud);
        
        return usuarioRepo.findUsuariosCercanos(latitud, longitud, radioKm)
            .stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }
    
    // ==========================================
    // ACTUALIZACIÓN
    // ==========================================
    
    public UsuarioResponseDTO actualizarUsuario(Long id, ActualizarUsuarioDTO dto) {
        log.info("Actualizando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        // Actualizar solo los campos que vengan
        if (dto.getNombre() != null) {
            usuario.setNombre(dto.getNombre());
        }
        
        if (dto.getTelefono() != null) {
            usuario.setTelefono(dto.getTelefono());
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
                usuario.setCiudad(dto.getCiudad());
                usuario.setPais(dto.getPais());
                
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
    
    public void cambiarPassword(Long id, CambiarPasswordDTO dto) {
        log.info("Cambiando contraseña para usuario ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        // TODO: Usar BCrypt para comparar
        if (!usuario.getPassword().equals(dto.getPasswordActual())) {
            log.error("Contraseña actual incorrecta para usuario ID: {}", id);
            throw new PasswordIncorrectaException();
        }
        
        if (!dto.getPasswordNueva().equals(dto.getPasswordConfirmacion())) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
        }
        
        // TODO: Encriptar con BCrypt
        usuario.setPassword(dto.getPasswordNueva());
        usuarioRepo.save(usuario);
        
        log.info("Contraseña actualizada para usuario ID: {}", id);
    }
    
    public UsuarioResponseDTO upgradeCuenta(Long id, AccountType nuevoTipo) {
        log.info("Upgrade de cuenta para usuario ID: {} a tipo: {}", id, nuevoTipo);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        if (!nuevoTipo.esSuperiorA(usuario.getAccountType())) {
            throw new IllegalArgumentException(
                "El nuevo tipo de cuenta debe ser superior al actual"
            );
        }
        
        usuario.upgradeCuenta(nuevoTipo);
        
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
    
    public void desactivarUsuario(Long id) {
        log.info("Desactivando usuario ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        usuario.setActivo(false);
        usuarioRepo.save(usuario);
        
        log.info("Usuario desactivado: ID={}", id);
    }
    
    public void reactivarUsuario(Long id) {
        log.info("Reactivando usuario ID: {}", id);
        
        Usuario usuario = usuarioRepo.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        
        usuario.setActivo(true);
        usuarioRepo.save(usuario);
        
        log.info("Usuario reactivado: ID={}", id);
    }
    
    // ==========================================
    // MÉTODO AUXILIAR
    // ==========================================
    
    /**
     * Convierte una entidad Usuario a UsuarioResponseDTO
     */
    private UsuarioResponseDTO convertirAResponse(Usuario usuario) {
        AccountType accountType = usuario.getAccountType();
        
        return UsuarioResponseDTO.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .email(usuario.getEmail())
            .telefono(usuario.getTelefono())
            // Ubicación
            .latitud(usuario.getUbicacion() != null ? 
                usuario.getUbicacion().getLatitud() : null)
            .longitud(usuario.getUbicacion() != null ? 
                usuario.getUbicacion().getLongitud() : null)
            .ciudad(usuario.getCiudad())
            .pais(usuario.getPais())
            // Cuenta
            .accountType(accountType)
            .accountTypeDisplay(accountType.getDisplayName())
            .presupuestoMensualBase(usuario.getPresupuestoMensualBase())
            .transaccionesMesActual(usuario.getTransaccionesMesActual())
            .limiteTransaccionesMes(accountType.getLimiteTransaccionesMes())
            // Beneficios
            .descuentoPorcentaje(accountType.getDescuentoPorcentaje())
            .tieneBeneficios(accountType.getTieneBeneficios())
            .puedeAdministrarEmpresas(accountType.getPuedeAdministrarEmpresas())
            // Estado
            .activo(usuario.getActivo())
            .fechaCreacion(usuario.getFechaCreacion())
            .ultimoAcceso(usuario.getUltimoAcceso())
            // Estadísticas
            .totalPresupuestos(usuario.getPresupuestos() != null ? 
                usuario.getPresupuestos().size() : 0)
            .totalGastos(usuario.getExpenses() != null ? 
                usuario.getExpenses().size() : 0)
            .totalReviews(usuario.getReviews() != null ? 
                usuario.getReviews().size() : 0)
            .build();
    }
}