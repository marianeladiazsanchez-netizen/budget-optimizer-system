/**
 * Servicio de Usuarios
 * 
 * Maneja todas las operaciones relacionadas con usuarios:
 * - Registro y autenticación
 * - Consultas y búsquedas
 * - Actualización de perfil
 * - Gestión de cuenta
 */

import { get, post, put, del } from '../apiClient.js';
import { API_CONFIG, replaceUrlParams, buildQueryString } from '../config.js';

const { USUARIOS } = API_CONFIG.ENDPOINTS;

// ============================================
// REGISTRO Y AUTENTICACIÓN
// ============================================

/**
 * Registra un nuevo usuario en el sistema
 * 
 * @param {object} userData - Datos del usuario
 * @param {string} userData.nombre - Nombre completo
 * @param {string} userData.email - Email único
 * @param {string} userData.password - Contraseña (min 8 caracteres)
 * @param {string} userData.ciudad - Ciudad
 * @param {string} userData.pais - País
 * @param {number} userData.presupuestoMensualBase - Presupuesto base
 * @param {number} [userData.latitud] - Latitud (opcional)
 * @param {number} [userData.longitud] - Longitud (opcional)
 * @param {string} [userData.telefono] - Teléfono (opcional)
 * 
 * @returns {Promise<object>} Usuario creado
 * 
 * @example
 * const usuario = await usuarioService.registrar({
 *   nombre: "Juan Pérez",
 *   email: "juan@example.com",
 *   password: "Password123",
 *   ciudad: "Cartagena",
 *   pais: "Colombia",
 *   presupuestoMensualBase: 5000
 * });
 */
export async function registrar(userData) {
  try {
    // Validaciones básicas
    validarDatosRegistro(userData);
    
    // Llamada al backend
    const usuario = await post(USUARIOS.REGISTRO, userData);
    
    console.log('✅ Usuario registrado:', usuario.email);
    return usuario;
    
  } catch (error) {
    console.error('❌ Error al registrar usuario:', error);
    throw error;
  }
}

/**
 * ⚠️ NOTA IMPORTANTE: Login real con JWT
 * 
 * Actualmente el backend NO tiene endpoint /login con JWT.
 * Por ahora, el "login" es simplemente buscar el usuario por email.
 * 
 * TODO: Cuando implementes JWT en el backend, crear:
 * POST /api/usuarios/login { email, password }
 * Response: { token, usuario }
 */
export async function login(email, password) {
  try {
    // Por ahora: buscar usuario por email
    const usuario = await buscarPorEmail(email);
    
    // ⚠️ TEMPORAL: No podemos verificar password sin backend de auth
    console.warn('⚠️ Login sin verificación de password (falta JWT en backend)');
    
    // Guardar en localStorage (simulado)
    localStorage.setItem('currentUser', JSON.stringify(usuario));
    
    console.log('✅ "Login" exitoso:', usuario.email);
    return usuario;
    
  } catch (error) {
    console.error('❌ Error en login:', error);
    throw new Error('Usuario no encontrado o credenciales incorrectas');
  }
}

/**
 * Cierra la sesión del usuario
 */
export function logout() {
  localStorage.removeItem('currentUser');
  localStorage.removeItem('authToken');
  console.log('🔓 Sesión cerrada');
}

/**
 * Obtiene el usuario actual de la sesión
 * @returns {object|null} Usuario actual o null
 */
export function getCurrentUser() {
  const userJson = localStorage.getItem('currentUser');
  return userJson ? JSON.parse(userJson) : null;
}

// ============================================
// CONSULTAS Y BÚSQUEDAS
// ============================================

/**
 * Busca un usuario por su ID
 * 
 * @param {number} id - ID del usuario
 * @returns {Promise<object>} Usuario encontrado
 * 
 * @example
 * const usuario = await usuarioService.buscarPorId(5);
 */
export async function buscarPorId(id) {
  try {
    const url = `${USUARIOS.BUSCAR_ID}/${id}`;
    const usuario = await get(url);
    
    console.log('✅ Usuario encontrado:', usuario.email);
    return usuario;
    
  } catch (error) {
    console.error(`❌ Error al buscar usuario ID ${id}:`, error);
    throw error;
  }
}

/**
 * Busca un usuario por su email
 * 
 * ✅ FIXED: Usa query parameter en lugar de path variable
 * 
 * @param {string} email - Email del usuario
 * @returns {Promise<object>} Usuario encontrado
 * 
 * @example
 * const usuario = await usuarioService.buscarPorEmail('juan@example.com');
 */
export async function buscarPorEmail(email) {
  try {
    // ✅ CORRECTO: /api/usuarios/buscar?email=...
    const url = USUARIOS.BUSCAR_EMAIL + buildQueryString({ email });
    const usuario = await get(url);
    
    console.log('✅ Usuario encontrado por email:', email);
    return usuario;
    
  } catch (error) {
    console.error(`❌ Error al buscar usuario por email ${email}:`, error);
    throw error;
  }
}

/**
 * Lista todos los usuarios activos
 * 
 * @returns {Promise<Array>} Lista de usuarios
 */
export async function listarTodos() {
  try {
    const usuarios = await get(USUARIOS.LISTAR);
    
    console.log(`✅ ${usuarios.length} usuarios listados`);
    return usuarios;
    
  } catch (error) {
    console.error('❌ Error al listar usuarios:', error);
    throw error;
  }
}

/**
 * Busca usuarios cercanos a una ubicación
 * 
 * @param {number} lat - Latitud
 * @param {number} lng - Longitud
 * @param {number} radio - Radio en kilómetros (default: 10)
 * @returns {Promise<Array>} Usuarios cercanos
 * 
 * @example
 * const cercanos = await usuarioService.buscarCercanos(10.4236, -75.5223, 5);
 */
export async function buscarCercanos(lat, lng, radio = 10) {
  try {
    const url = `${USUARIOS.LISTAR}/cercanos` + buildQueryString({ lat, lng, radio });
    const usuarios = await get(url);
    
    console.log(`✅ ${usuarios.length} usuarios cercanos encontrados`);
    return usuarios;
    
  } catch (error) {
    console.error('❌ Error al buscar usuarios cercanos:', error);
    throw error;
  }
}

// ============================================
// ACTUALIZACIÓN
// ============================================

/**
 * Actualiza el perfil de un usuario
 * 
 * @param {number} id - ID del usuario
 * @param {object} data - Datos a actualizar
 * @param {string} [data.nombre] - Nuevo nombre
 * @param {string} [data.telefono] - Nuevo teléfono
 * @param {string} [data.ciudad] - Nueva ciudad
 * @param {string} [data.pais] - Nuevo país
 * @param {number} [data.latitud] - Nueva latitud
 * @param {number} [data.longitud] - Nueva longitud
 * @param {number} [data.presupuestoMensualBase] - Nuevo presupuesto
 * 
 * @returns {Promise<object>} Usuario actualizado
 */
export async function actualizar(id, data) {
  try {
    const url = `${USUARIOS.ACTUALIZAR}/${id}`;
    const usuario = await put(url, data);
    
    console.log('✅ Usuario actualizado:', usuario.email);
    
    // Actualizar en localStorage si es el usuario actual
    const currentUser = getCurrentUser();
    if (currentUser && currentUser.id === id) {
      localStorage.setItem('currentUser', JSON.stringify(usuario));
    }
    
    return usuario;
    
  } catch (error) {
    console.error(`❌ Error al actualizar usuario ${id}:`, error);
    throw error;
  }
}

/**
 * Cambia la contraseña de un usuario
 * 
 * @param {number} id - ID del usuario
 * @param {object} passwords - Contraseñas
 * @param {string} passwords.passwordActual - Contraseña actual
 * @param {string} passwords.passwordNueva - Nueva contraseña
 * @param {string} passwords.passwordConfirmacion - Confirmación
 * 
 * @returns {Promise<void>}
 */
export async function cambiarPassword(id, passwords) {
  try {
    // Validar que las contraseñas coincidan
    if (passwords.passwordNueva !== passwords.passwordConfirmacion) {
      throw new Error('Las contraseñas no coinciden');
    }
    
    const url = replaceUrlParams(USUARIOS.CAMBIAR_PASSWORD, { id });
    await put(url, passwords);
    
    console.log('✅ Contraseña cambiada exitosamente');
    
  } catch (error) {
    console.error('❌ Error al cambiar contraseña:', error);
    throw error;
  }
}

/**
 * Hace upgrade de la cuenta de un usuario
 * 
 * @param {number} id - ID del usuario
 * @param {string} nuevoTipo - Nuevo tipo (USER, PREMIUM, BUSINESS, ADMIN)
 * @returns {Promise<object>} Usuario actualizado
 */
export async function upgradeCuenta(id, nuevoTipo) {
  try {
    const url = replaceUrlParams(USUARIOS.UPGRADE, { id }) + buildQueryString({ nuevoTipo });
    const usuario = await put(url);
    
    console.log(`✅ Upgrade exitoso a ${nuevoTipo}`);
    return usuario;
    
  } catch (error) {
    console.error('❌ Error en upgrade:', error);
    throw error;
  }
}

// ============================================
// DESACTIVACIÓN
// ============================================

/**
 * Desactiva un usuario (soft delete)
 * 
 * @param {number} id - ID del usuario
 * @returns {Promise<void>}
 */
export async function desactivar(id) {
  try {
    const url = `${USUARIOS.DESACTIVAR}/${id}`;
    await del(url);
    
    console.log('✅ Usuario desactivado');
    
  } catch (error) {
    console.error('❌ Error al desactivar usuario:', error);
    throw error;
  }
}

/**
 * Reactiva un usuario desactivado
 * 
 * @param {number} id - ID del usuario
 * @returns {Promise<void>}
 */
export async function reactivar(id) {
  try {
    const url = replaceUrlParams(USUARIOS.REACTIVAR, { id });
    await put(url);
    
    console.log('✅ Usuario reactivado');
    
  } catch (error) {
    console.error('❌ Error al reactivar usuario:', error);
    throw error;
  }
}

// ============================================
// VALIDACIONES
// ============================================

/**
 * Valida los datos de registro antes de enviar
 * @param {object} data - Datos del usuario
 * @throws {Error} Si hay datos inválidos
 */
function validarDatosRegistro(data) {
  const errores = [];
  
  if (!data.nombre || data.nombre.trim().length < 2) {
    errores.push('El nombre debe tener al menos 2 caracteres');
  }
  
  if (!data.email || !data.email.includes('@')) {
    errores.push('El email es inválido');
  }
  
  if (!data.password || data.password.length < 8) {
    errores.push('La contraseña debe tener al menos 8 caracteres');
  }
  
  if (!data.ciudad || data.ciudad.trim().length === 0) {
    errores.push('La ciudad es obligatoria');
  }
  
  if (!data.pais || data.pais.trim().length === 0) {
    errores.push('El país es obligatorio');
  }
  
  if (!data.presupuestoMensualBase || data.presupuestoMensualBase <= 0) {
    errores.push('El presupuesto mensual debe ser mayor a 0');
  }
  
  if (errores.length > 0) {
    throw new Error('Errores de validación:\n' + errores.join('\n'));
  }
}

// ============================================
// EXPORTAR COMO OBJETO
// ============================================
export default {
  // Auth
  registrar,
  login,
  logout,
  getCurrentUser,
  
  // Consultas
  buscarPorId,
  buscarPorEmail,
  listarTodos,
  buscarCercanos,
  
  // Actualización
  actualizar,
  cambiarPassword,
  upgradeCuenta,
  
  // Desactivación
  desactivar,
  reactivar
};