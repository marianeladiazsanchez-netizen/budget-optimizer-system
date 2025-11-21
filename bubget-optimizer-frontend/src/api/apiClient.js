/**
 * Cliente HTTP base usando Axios
 * 
 * Este módulo centraliza todas las peticiones HTTP al backend.
 * Incluye:
 * - Configuración de Axios
 * - Interceptores para autenticación
 * - Manejo centralizado de errores
 * - Logging de peticiones
 */

import { API_CONFIG } from './config.js';

// ============================================
// VERIFICACIÓN: Axios debe estar cargado globalmente
// ============================================
if (typeof axios === 'undefined') {
  throw new Error('❌ Axios no está cargado. Asegúrate de incluir el script en index.html');
}

// ============================================
// CREAR INSTANCIA DE AXIOS
// ============================================
const apiClient = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: API_CONFIG.HEADERS
});

// ============================================
// INTERCEPTOR DE REQUEST (Antes de enviar)
// ============================================
apiClient.interceptors.request.use(
  (config) => {
    // 1. Agregar token de autenticación si existe
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 2. Log de petición (solo en desarrollo)
    if (import.meta.env.DEV) {
      console.log(`🌐 ${config.method.toUpperCase()} ${config.url}`, {
        params: config.params,
        data: config.data
      });
    }
    
    return config;
  },
  (error) => {
    console.error('❌ Error en interceptor de request:', error);
    return Promise.reject(error);
  }
);

// ============================================
// INTERCEPTOR DE RESPONSE (Después de recibir)
// ============================================
apiClient.interceptors.response.use(
  (response) => {
    // Log de respuesta exitosa (solo en desarrollo)
    if (import.meta.env.DEV) {
      console.log(`✅ ${response.config.method.toUpperCase()} ${response.config.url}`, {
        status: response.status,
        data: response.data
      });
    }
    
    return response;
  },
  (error) => {
    // Manejar errores de respuesta
    handleResponseError(error);
    return Promise.reject(error);
  }
);

// ============================================
// FUNCIONES AUXILIARES
// ============================================

/**
 * Obtiene el token de autenticación del localStorage
 * @returns {string|null} Token de autenticación
 */
function getAuthToken() {
  return localStorage.getItem('authToken');
}

/**
 * Guarda el token de autenticación
 * @param {string} token - Token JWT
 */
export function setAuthToken(token) {
  if (token) {
    localStorage.setItem('authToken', token);
    console.log('🔐 Token guardado');
  }
}

/**
 * Elimina el token de autenticación
 */
export function clearAuthToken() {
  localStorage.removeItem('authToken');
  console.log('🔓 Token eliminado');
}

/**
 * Verifica si hay un token válido
 * @returns {boolean}
 */
export function isAuthenticated() {
  return !!getAuthToken();
}

/**
 * Maneja errores de respuesta HTTP
 * @param {Error} error - Error de Axios
 */
function handleResponseError(error) {
  if (!error.response) {
    // Error de red o timeout
    console.error('❌ Error de conexión:', error.message);
    return;
  }
  
  const { status, data } = error.response;
  
  console.error(`❌ Error ${status}:`, {
    url: error.config.url,
    method: error.config.method,
    message: data.mensaje || data.message || 'Error desconocido',
    details: data
  });
  
  // Manejar errores específicos
  switch (status) {
    case 401:
      // No autorizado - Limpiar token y redirigir
      console.warn('⚠️ Sesión expirada');
      clearAuthToken();
      // TODO: Redirigir a login si es necesario
      break;
      
    case 403:
      console.warn('⚠️ Acceso prohibido');
      break;
      
    case 404:
      console.warn('⚠️ Recurso no encontrado');
      break;
      
    case 409:
      console.warn('⚠️ Conflicto (probablemente email duplicado)');
      break;
      
    case 500:
      console.error('❌ Error interno del servidor');
      break;
      
    default:
      console.error(`❌ Error ${status}`);
  }
}

// ============================================
// MÉTODOS HTTP HELPER
// ============================================

/**
 * Realiza una petición GET
 * @param {string} url - URL del endpoint
 * @param {object} params - Query parameters
 * @returns {Promise} Promesa con la respuesta
 */
export async function get(url, params = {}) {
  const response = await apiClient.get(url, { params });
  return response.data;
}

/**
 * Realiza una petición POST
 * @param {string} url - URL del endpoint
 * @param {object} data - Datos del body
 * @returns {Promise} Promesa con la respuesta
 */
export async function post(url, data = {}) {
  const response = await apiClient.post(url, data);
  return response.data;
}

/**
 * Realiza una petición PUT
 * @param {string} url - URL del endpoint
 * @param {object} data - Datos del body
 * @returns {Promise} Promesa con la respuesta
 */
export async function put(url, data = {}) {
  const response = await apiClient.put(url, data);
  return response.data;
}

/**
 * Realiza una petición DELETE
 * @param {string} url - URL del endpoint
 * @returns {Promise} Promesa con la respuesta
 */
export async function del(url) {
  const response = await apiClient.delete(url);
  return response.data;
}

// ============================================
// EXPORTAR CLIENTE
// ============================================
export default apiClient;

// Log de inicialización
if (import.meta.env.DEV) {
  console.log('✅ API Client inicializado:', {
    baseURL: API_CONFIG.BASE_URL,
    authenticated: isAuthenticated()
  });
}