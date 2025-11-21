/**
 * Manejador centralizado de errores
 * 
 * Proporciona funciones para:
 * - Mostrar errores al usuario
 * - Parsear mensajes del backend
 * - Limpiar mensajes de error
 */

// ============================================
// MOSTRAR ERRORES EN UI
// ============================================

/**
 * Muestra un error en un campo específico
 * 
 * @param {string} fieldId - ID del campo de input
 * @param {string} mensaje - Mensaje de error
 * @param {number} duration - Duración en ms (default: 3000)
 * 
 * @example
 * showFieldError('register-email', 'El email ya está registrado', 5000);
 */
export function showFieldError(fieldId, mensaje, duration = 3000) {
  const input = document.getElementById(fieldId);
  if (!input) {
    console.warn(`⚠️ Campo ${fieldId} no encontrado`);
    return;
  }
  
  const errorDiv = input.nextElementSibling;
  if (!errorDiv || !errorDiv.classList.contains('error-message')) {
    console.warn(`⚠️ Error message div no encontrado para ${fieldId}`);
    return;
  }
  
  // Mostrar error
  errorDiv.textContent = mensaje;
  errorDiv.style.display = 'block';
  input.style.borderColor = '#ef4444';
  
  // Limpiar después del tiempo especificado
  setTimeout(() => {
    clearFieldError(fieldId);
  }, duration);
}

/**
 * Limpia el error de un campo
 * 
 * @param {string} fieldId - ID del campo de input
 */
export function clearFieldError(fieldId) {
  const input = document.getElementById(fieldId);
  if (!input) return;
  
  const errorDiv = input.nextElementSibling;
  if (errorDiv && errorDiv.classList.contains('error-message')) {
    errorDiv.style.display = 'none';
    errorDiv.textContent = '';
  }
  
  input.style.borderColor = '#e5e7eb';
}

/**
 * Limpia todos los errores de un formulario
 * 
 * @param {string} formId - ID del formulario
 */
export function clearFormErrors(formId) {
  const form = document.getElementById(formId);
  if (!form) return;
  
  // Limpiar todos los error-message
  const errorDivs = form.querySelectorAll('.error-message');
  errorDivs.forEach(div => {
    div.style.display = 'none';
    div.textContent = '';
  });
  
  // Resetear bordes de inputs
  const inputs = form.querySelectorAll('input');
  inputs.forEach(input => {
    input.style.borderColor = '#e5e7eb';
  });
}

// ============================================
// PARSEAR ERRORES DEL BACKEND
// ============================================

/**
 * Extrae mensaje de error del response del backend
 * 
 * El backend puede devolver:
 * - { mensaje: "..." }
 * - { message: "..." }
 * - { error: "...", mensaje: "..." }
 * - { errores: ["...", "..."] }
 * 
 * @param {Error} error - Error de Axios
 * @returns {string} Mensaje de error legible
 */
export function extractErrorMessage(error) {
  // Si no hay respuesta, es un error de red
  if (!error.response) {
    if (error.message === 'Network Error') {
      return 'No se pudo conectar con el servidor. Verifica tu conexión.';
    }
    if (error.code === 'ECONNABORTED') {
      return 'La petición tardó demasiado. Intenta de nuevo.';
    }
    return `Error de conexión: ${error.message}`;
  }
  
  const { status, data } = error.response;
  
  // Mensajes específicos por código de estado
  switch (status) {
    case 400:
      return parseValidationErrors(data) || 'Datos inválidos. Verifica los campos.';
      
    case 401:
      return 'No autorizado. Por favor inicia sesión.';
      
    case 403:
      return 'No tienes permisos para realizar esta acción.';
      
    case 404:
      return data.mensaje || 'Recurso no encontrado.';
      
    case 409:
      return data.mensaje || 'El recurso ya existe (email duplicado).';
      
    case 429:
      return 'Has excedido el límite de peticiones. Intenta más tarde.';
      
    case 500:
      return 'Error interno del servidor. Intenta más tarde.';
      
    default:
      return data.mensaje || data.message || `Error ${status}`;
  }
}

/**
 * Parsea errores de validación del backend
 * 
 * @param {object} data - Respuesta del backend
 * @returns {string|null} Mensaje concatenado o null
 */
function parseValidationErrors(data) {
  // Caso 1: Array de errores
  if (data.errores && Array.isArray(data.errores)) {
    return data.errores.join('\n');
  }
  
  // Caso 2: Mensaje único
  if (data.mensaje) {
    return data.mensaje;
  }
  
  if (data.message) {
    return data.message;
  }
  
  return null;
}

// ============================================
// MOSTRAR NOTIFICACIONES GENERALES
// ============================================

/**
 * Muestra una notificación toast
 * 
 * ⚠️ NOTA: Necesitas implementar un toast UI
 * Por ahora usa alert() como fallback
 * 
 * @param {string} mensaje - Mensaje a mostrar
 * @param {string} tipo - Tipo: 'success' | 'error' | 'warning' | 'info'
 * @param {number} duration - Duración en ms
 */
export function showToast(mensaje, tipo = 'info', duration = 3000) {
  // TODO: Implementar toast UI real
  // Por ahora, usar alert como fallback
  
  const icon = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  }[tipo] || 'ℹ️';
  
  console.log(`${icon} ${tipo.toUpperCase()}: ${mensaje}`);
  
  // Fallback a alert (temporal)
  if (tipo === 'error') {
    alert(`${icon} ${mensaje}`);
  }
}

/**
 * Muestra un error genérico con alert
 * 
 * @param {Error} error - Error capturado
 * @param {string} contexto - Contexto del error (ej: "al registrar usuario")
 */
export function showError(error, contexto = '') {
  const mensaje = extractErrorMessage(error);
  const textoCompleto = contexto 
    ? `Error ${contexto}:\n\n${mensaje}` 
    : mensaje;
  
  console.error('❌', textoCompleto, error);
  alert(textoCompleto);
}

/**
 * Muestra un mensaje de éxito
 * 
 * @param {string} mensaje - Mensaje de éxito
 */
export function showSuccess(mensaje) {
  console.log('✅', mensaje);
  showToast(mensaje, 'success');
}

// ============================================
// VALIDACIONES DE FORMULARIO
// ============================================

/**
 * Valida un email
 * @param {string} email - Email a validar
 * @returns {boolean}
 */
export function isValidEmail(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email);
}

/**
 * Valida una contraseña
 * @param {string} password - Contraseña a validar
 * @returns {object} { valid: boolean, errors: string[] }
 */
export function validatePassword(password) {
  const errors = [];
  
  if (password.length < 8) {
    errors.push('Debe tener al menos 8 caracteres');
  }
  
  if (!/[A-Z]/.test(password)) {
    errors.push('Debe contener al menos una mayúscula');
  }
  
  if (!/[a-z]/.test(password)) {
    errors.push('Debe contener al menos una minúscula');
  }
  
  if (!/\d/.test(password)) {
    errors.push('Debe contener al menos un número');
  }
  
  return {
    valid: errors.length === 0,
    errors
  };
}

// ============================================
// EXPORTAR FUNCIONES
// ============================================
export default {
  // UI
  showFieldError,
  clearFieldError,
  clearFormErrors,
  
  // Parsing
  extractErrorMessage,
  
  // Notificaciones
  showToast,
  showError,
  showSuccess,
  
  // Validaciones
  isValidEmail,
  validatePassword
};