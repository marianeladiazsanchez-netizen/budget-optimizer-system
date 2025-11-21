/**
 * Configuración centralizada de la API
 * 
 * IMPORTANTE:
 * - En desarrollo local: usa http://localhost:8080
 * - En Docker: el navegador del cliente NO puede acceder a "backend:8080"
 *   porque esa es la red INTERNA de Docker.
 * - Solución: Siempre usa localhost con el puerto mapeado
 */

// ⚠️ CRÍTICO: Axios debe estar cargado desde el HTML
// Ya lo tienes en index.html:
// <script src="https://cdn.jsdelivr.net/npm/axios@1.6.8/dist/axios.min.js"></script>

export const API_CONFIG = {
  // ✅ Usa variable de entorno o fallback a localhost
  BASE_URL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  
  // Timeout de 15 segundos
  TIMEOUT: 15000,
  
  // Headers por defecto
  HEADERS: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  
  // Endpoints principales
  ENDPOINTS: {
    // Usuarios
    USUARIOS: {
      REGISTRO: '/api/usuarios/registro',
      BUSCAR_ID: '/api/usuarios',
      BUSCAR_EMAIL: '/api/usuarios/buscar',
      LISTAR: '/api/usuarios',
      ACTUALIZAR: '/api/usuarios',
      CAMBIAR_PASSWORD: '/api/usuarios/:id/password',
      UPGRADE: '/api/usuarios/:id/upgrade',
      DESACTIVAR: '/api/usuarios/:id',
      REACTIVAR: '/api/usuarios/:id/reactivar'
    },
    
    // Presupuestos
    PRESUPUESTOS: {
      CREAR: '/api/presupuestos',
      BUSCAR: '/api/presupuestos',
      LISTAR_USUARIO: '/api/presupuestos/usuario',
      ACTUAL: '/api/presupuestos/usuario/:id/actual',
      ACTUALIZAR: '/api/presupuestos/:id',
      ACTIVAR: '/api/presupuestos/:id/activar',
      PAUSAR: '/api/presupuestos/:id/pausar',
      COMPLETAR: '/api/presupuestos/:id/completar',
      ELIMINAR: '/api/presupuestos/:id',
      ESTADISTICAS: '/api/presupuestos/usuario/:id/estadisticas'
    },
    
    // Gastos
    EXPENSES: {
      CREAR: '/api/expenses',
      BUSCAR: '/api/expenses',
      POR_PRESUPUESTO: '/api/expenses/presupuesto',
      POR_USUARIO: '/api/expenses/usuario',
      ACTUALIZAR: '/api/expenses/:id',
      ELIMINAR: '/api/expenses/:id'
    },
    
    // Categorías
    CATEGORIAS: {
      LISTAR: '/api/categorias',
      GASTOS: '/api/categorias/gastos',
      INGRESOS: '/api/categorias/ingresos',
      BUSCAR: '/api/categorias/:id'
    },
    
    // Health check
    HEALTH: '/actuator/health'
  }
};

/**
 * Reemplaza parámetros en URLs
 * 
 * @param {string} url - URL con parámetros (:id, :userId, etc.)
 * @param {object} params - Objeto con valores a reemplazar
 * @returns {string} URL con parámetros reemplazados
 * 
 * @example
 * replaceUrlParams('/api/usuarios/:id/password', { id: 5 })
 * // => '/api/usuarios/5/password'
 */
export function replaceUrlParams(url, params = {}) {
  let result = url;
  for (const [key, value] of Object.entries(params)) {
    result = result.replace(`:${key}`, value);
  }
  return result;
}

/**
 * Construye query string desde objeto
 * 
 * @param {object} params - Parámetros de query
 * @returns {string} Query string (?key=value&...)
 * 
 * @example
 * buildQueryString({ email: 'test@example.com', activo: true })
 * // => '?email=test@example.com&activo=true'
 */
export function buildQueryString(params = {}) {
  const keys = Object.keys(params).filter(key => params[key] !== null && params[key] !== undefined);
  
  if (keys.length === 0) return '';
  
  const query = keys
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');
  
  return `?${query}`;
}

// Log de configuración al cargar (solo en desarrollo)
if (import.meta.env.DEV) {
  console.log('🔧 API Configuration loaded:', {
    baseUrl: API_CONFIG.BASE_URL,
    timeout: `${API_CONFIG.TIMEOUT}ms`,
    endpoints: Object.keys(API_CONFIG.ENDPOINTS)
  });
}