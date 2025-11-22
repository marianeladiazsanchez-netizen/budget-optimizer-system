/**
 * Servicio de Usuarios - VERSIÓN ACTUALIZADA
 * Compatible con el nuevo apiClient async
 */

import apiClient, { setAuthToken, clearAuthToken, isAuthenticated } from '../apiClient.js';
import { API_CONFIG, replaceUrlParams, buildQueryString } from '../config.js';

const { USUARIOS } = API_CONFIG.ENDPOINTS;

// ============================================
// REGISTRO Y AUTENTICACIÓN
// ============================================

export async function registrar(userData) {
  try {
    validarDatosRegistro(userData);
    
    console.log('📤 Registrando usuario...', { email: userData.email });
    
    const usuario = await apiClient.post(USUARIOS.REGISTRO, userData);
    
    console.log('✅ Usuario registrado:', usuario.email);
    return usuario;
    
  } catch (error) {
    console.error('❌ Error al registrar usuario:', error);
    throw error;
  }
}

export async function login(email, password) {
  try {
    const usuario = await buscarPorEmail(email);
    
    console.warn('⚠️ Login sin verificación de password (falta JWT en backend)');
    
    localStorage.setItem('currentUser', JSON.stringify(usuario));
    
    console.log('✅ "Login" exitoso:', usuario.email);
    return usuario;
    
  } catch (error) {
    console.error('❌ Error en login:', error);
    throw new Error('Usuario no encontrado o credenciales incorrectas');
  }
}

export function logout() {
  localStorage.removeItem('currentUser');
  clearAuthToken();
  console.log('🔓 Sesión cerrada');
}

export function getCurrentUser() {
  const userJson = localStorage.getItem('currentUser');
  return userJson ? JSON.parse(userJson) : null;
}

// ============================================
// CONSULTAS Y BÚSQUEDAS
// ============================================

export async function buscarPorId(id) {
  try {
    const url = `${USUARIOS.BUSCAR_ID}/${id}`;
    const usuario = await apiClient.get(url);
    
    console.log('✅ Usuario encontrado:', usuario.email);
    return usuario;
    
  } catch (error) {
    console.error(`❌ Error al buscar usuario ID ${id}:`, error);
    throw error;
  }
}

export async function buscarPorEmail(email) {
  try {
    const url = USUARIOS.BUSCAR_EMAIL + buildQueryString({ email });
    const usuario = await apiClient.get(url);
    
    console.log('✅ Usuario encontrado por email:', email);
    return usuario;
    
  } catch (error) {
    console.error(`❌ Error al buscar usuario por email ${email}:`, error);
    throw error;
  }
}

export async function listarTodos() {
  try {
    const usuarios = await apiClient.get(USUARIOS.LISTAR);
    
    console.log(`✅ ${usuarios.length} usuarios listados`);
    return usuarios;
    
  } catch (error) {
    console.error('❌ Error al listar usuarios:', error);
    throw error;
  }
}

export async function buscarCercanos(lat, lng, radio = 10) {
  try {
    const url = `${USUARIOS.LISTAR}/cercanos` + buildQueryString({ lat, lng, radio });
    const usuarios = await apiClient.get(url);
    
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

export async function actualizar(id, data) {
  try {
    const url = `${USUARIOS.ACTUALIZAR}/${id}`;
    const usuario = await apiClient.put(url, data);
    
    console.log('✅ Usuario actualizado:', usuario.email);
    
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

export async function cambiarPassword(id, passwords) {
  try {
    if (passwords.passwordNueva !== passwords.passwordConfirmacion) {
      throw new Error('Las contraseñas no coinciden');
    }
    
    const url = replaceUrlParams(USUARIOS.CAMBIAR_PASSWORD, { id });
    await apiClient.put(url, passwords);
    
    console.log('✅ Contraseña cambiada exitosamente');
    
  } catch (error) {
    console.error('❌ Error al cambiar contraseña:', error);
    throw error;
  }
}

export async function upgradeCuenta(id, nuevoTipo) {
  try {
    const url = replaceUrlParams(USUARIOS.UPGRADE, { id }) + buildQueryString({ nuevoTipo });
    const usuario = await apiClient.put(url);
    
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

export async function desactivar(id) {
  try {
    const url = `${USUARIOS.DESACTIVAR}/${id}`;
    await apiClient.del(url);
    
    console.log('✅ Usuario desactivado');
    
  } catch (error) {
    console.error('❌ Error al desactivar usuario:', error);
    throw error;
  }
}

export async function reactivar(id) {
  try {
    const url = replaceUrlParams(USUARIOS.REACTIVAR, { id });
    await apiClient.put(url);
    
    console.log('✅ Usuario reactivado');
    
  } catch (error) {
    console.error('❌ Error al reactivar usuario:', error);
    throw error;
  }
}

// ============================================
// VALIDACIONES
// ============================================

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
  registrar,
  login,
  logout,
  getCurrentUser,
  buscarPorId,
  buscarPorEmail,
  listarTodos,
  buscarCercanos,
  actualizar,
  cambiarPassword,
  upgradeCuenta,
  desactivar,
  reactivar
};