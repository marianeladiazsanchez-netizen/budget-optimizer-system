/**
 * Cliente HTTP base usando Axios
 * 
 * IMPORTANTE: Este módulo se importa ANTES de que Axios esté disponible.
 * Solución: Lazy-load el cliente
 */

import { API_CONFIG } from './config.js';

// ============================================
// LAZY-LOADED API CLIENT
// ============================================
let apiClient = null;
let clientPromise = null;

/**
 * Obtiene la instancia del cliente API
 * Espera a que Axios esté disponible si es necesario
 */
function getApiClient() {
  if (apiClient) {
    return apiClient;
  }
  
  if (!clientPromise) {
    clientPromise = initializeClient();
  }
  
  return clientPromise;
}

/**
 * Inicializa el cliente cuando Axios esté listo
 */
async function initializeClient() {
  // Esperar a que Axios esté disponible (máx 5 segundos)
  let attempts = 0;
  while (typeof axios === 'undefined' && attempts < 50) {
    await new Promise(r => setTimeout(r, 100));
    attempts++;
  }
  
  if (typeof axios === 'undefined') {
    throw new Error('❌ Axios no se cargó correctamente. Verifica index.html');
  }
  
  console.log('✅ Axios disponible, creando cliente API...');
  
  apiClient = axios.create({
    baseURL: API_CONFIG.BASE_URL,
    timeout: API_CONFIG.TIMEOUT,
    headers: API_CONFIG.HEADERS
  });
  
  setupInterceptors(apiClient);
  
  if (import.meta.env.DEV) {
    console.log('✅ API Client inicializado:', {
      baseURL: API_CONFIG.BASE_URL,
      timeout: `${API_CONFIG.TIMEOUT}ms`
    });
  }
  
  return apiClient;
}

// ============================================
// INTERCEPTORS
// ============================================
function setupInterceptors(client) {
  // REQUEST INTERCEPTOR
  client.interceptors.request.use(
    (config) => {
      const token = getAuthToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      
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
  
  // RESPONSE INTERCEPTOR
  client.interceptors.response.use(
    (response) => {
      if (import.meta.env.DEV) {
        console.log(`✅ ${response.config.method.toUpperCase()} ${response.config.url}`, {
          status: response.status
        });
      }
      return response;
    },
    (error) => {
      handleResponseError(error);
      return Promise.reject(error);
    }
  );
}

// ============================================
// FUNCIONES AUXILIARES
// ============================================

function getAuthToken() {
  return localStorage.getItem('authToken');
}

export function setAuthToken(token) {
  if (token) {
    localStorage.setItem('authToken', token);
    console.log('🔐 Token guardado');
  }
}

export function clearAuthToken() {
  localStorage.removeItem('authToken');
  console.log('🔓 Token eliminado');
}

export function isAuthenticated() {
  return !!getAuthToken();
}

function handleResponseError(error) {
  if (!error.response) {
    console.error('❌ Error de conexión:', error.message);
    return;
  }
  
  const { status, data } = error.response;
  
  console.error(`❌ Error ${status}:`, {
    url: error.config.url,
    message: data.mensaje || data.message || 'Error desconocido'
  });
  
  switch (status) {
    case 401:
      console.warn('⚠️ Sesión expirada');
      clearAuthToken();
      break;
    case 403:
      console.warn('⚠️ Acceso prohibido');
      break;
    case 404:
      console.warn('⚠️ Recurso no encontrado');
      break;
    case 409:
      console.warn('⚠️ Conflicto (email duplicado)');
      break;
    case 500:
      console.error('❌ Error interno del servidor');
      break;
  }
}

// ============================================
// MÉTODOS HTTP HELPER (Async)
// ============================================

export async function get(url, params = {}) {
  const client = await getApiClient();
  const response = await client.get(url, { params });
  return response.data;
}

export async function post(url, data = {}) {
  const client = await getApiClient();
  const response = await client.post(url, data);
  return response.data;
}

export async function put(url, data = {}) {
  const client = await getApiClient();
  const response = await client.put(url, data);
  return response.data;
}

export async function del(url) {
  const client = await getApiClient();
  const response = await client.delete(url);
  return response.data;
}

// ============================================
// EXPORTAR
// ============================================
export default {
  getApiClient,
  get,
  post,
  put,
  del,
  setAuthToken,
  clearAuthToken,
  isAuthenticated
};