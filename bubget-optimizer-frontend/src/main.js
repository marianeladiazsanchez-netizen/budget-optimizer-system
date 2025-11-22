// ============================================
// 🎯 BUDGET OPTIMIZER - FRONTEND MAIN.JS
// ============================================

// ==========================================
// 📡 CONFIGURACIÓN DE API
// ==========================================
const API_URL = 'http://localhost:8080/api';

// ==========================================
// 🔐 GESTIÓN DE SESIÓN
// ==========================================
class SessionManager {
    static saveUser(userData) {
        localStorage.setItem('budgetUser', JSON.stringify(userData));
        localStorage.setItem('userId', userData.id);
    }

    static getUser() {
        const userStr = localStorage.getItem('budgetUser');
        return userStr ? JSON.parse(userStr) : null;
    }

    static getUserId() {
        return localStorage.getItem('userId');
    }

    static isLoggedIn() {
        return this.getUser() !== null;
    }

    static logout() {
        localStorage.removeItem('budgetUser');
        localStorage.removeItem('userId');
    }
}

// ==========================================
// 🌐 CLIENTE HTTP
// ==========================================
class ApiClient {
    static async request(endpoint, options = {}) {
        try {
            const response = await axios({
                url: `${API_URL}${endpoint}`,
                method: options.method || 'GET',
                data: options.data,
                params: options.params,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            });
            return { success: true, data: response.data };
        } catch (error) {
            console.error('API Error:', error);
            return {
                success: false,
                error: error.response?.data?.mensaje || error.message
            };
        }
    }

    static get(endpoint, params) {
        return this.request(endpoint, { params });
    }

    static post(endpoint, data) {
        return this.request(endpoint, { method: 'POST', data });
    }

    static put(endpoint, data) {
        return this.request(endpoint, { method: 'PUT', data });
    }

    static delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}

// ==========================================
// 👤 SERVICIO DE USUARIOS
// ==========================================
class UsuarioService {
    static async registrar(userData) {
        return ApiClient.post('/usuarios/registro', userData);
    }

    static async buscarPorEmail(email) {
        return ApiClient.get('/usuarios/buscar', { email });
    }

    static async buscarPorId(id) {
        return ApiClient.get(`/usuarios/${id}`);
    }

    static async actualizar(id, userData) {
        return ApiClient.put(`/usuarios/${id}`, userData);
    }
}

// ==========================================
// 💰 SERVICIO DE PRESUPUESTOS
// ==========================================
class PresupuestoService {
    static async crear(usuarioId, presupuestoData) {
        return ApiClient.post(`/presupuestos?usuarioId=${usuarioId}`, presupuestoData);
    }

    static async listarPorUsuario(usuarioId) {
        return ApiClient.get(`/presupuestos/usuario/${usuarioId}`);
    }

    static async obtenerActual(usuarioId) {
        return ApiClient.get(`/presupuestos/usuario/${usuarioId}/actual`);
    }

    static async activar(id) {
        return ApiClient.put(`/presupuestos/${id}/activar`);
    }

    static async obtenerEstadisticas(usuarioId) {
        return ApiClient.get(`/presupuestos/usuario/${usuarioId}/estadisticas`);
    }
}

// ==========================================
// 📁 SERVICIO DE CATEGORÍAS
// ==========================================
class CategoriaService {
    static async listarTodas() {
        return ApiClient.get('/categorias');
    }

    static async listarParaGastos() {
        return ApiClient.get('/categorias/gastos');
    }
}

// ==========================================
// 💸 SERVICIO DE GASTOS
// ==========================================
class ExpenseService {
    static async crear(gastoData) {
        return ApiClient.post('/expenses', gastoData);
    }

    static async listarPorPresupuesto(presupuestoId) {
        return ApiClient.get(`/expenses/presupuesto/${presupuestoId}`);
    }

    static async listarPorUsuario(usuarioId) {
        return ApiClient.get(`/expenses/usuario/${usuarioId}`);
    }
}

// ==========================================
// 🎨 UI MANAGER
// ==========================================
class UIManager {
    static showLoading(element) {
        element.innerHTML = '<div class="loading">⏳ Cargando...</div>';
    }

    static showError(message) {
        alert(`❌ Error: ${message}`);
    }

    static showSuccess(message) {
        alert(`✅ ${message}`);
    }

    static showLandingPage() {
        document.getElementById('landing-page').classList.add('active');
        document.getElementById('app-layout').classList.add('hidden');
    }

    static showAppLayout() {
        document.getElementById('landing-page').classList.remove('active');
        document.getElementById('app-layout').classList.remove('hidden');
    }

    static showPage(pageId) {
        // Ocultar todas las páginas
        document.querySelectorAll('.page').forEach(page => {
            page.classList.remove('active');
        });

        // Mostrar la página seleccionada
        document.getElementById(pageId).classList.add('active');

        // Actualizar nav items
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });

        const activeNav = document.querySelector(`[data-page="${pageId}"]`);
        if (activeNav) activeNav.classList.add('active');
    }
}

// ==========================================
// 🔐 AUTENTICACIÓN
// ==========================================
class AuthController {
    static async handleLogin(event) {
        event.preventDefault();

        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        if (!email || !password) {
            UIManager.showError('Por favor completa todos los campos');
            return;
        }

        // Buscar usuario por email
        const result = await UsuarioService.buscarPorEmail(email);

        if (!result.success) {
            UIManager.showError('Usuario no encontrado');
            return;
        }

        // ⚠️ NOTA: El backend NO valida password aún (TODO: implementar BCrypt)
        // Por ahora, solo verificamos que exista el usuario
        const usuario = result.data;

        // Guardar sesión
        SessionManager.saveUser(usuario);

        // Mostrar app
        UIManager.showAppLayout();
        UIManager.showPage('comparador-page');

        // Actualizar nombre de usuario
        document.getElementById('user-name-display').textContent = usuario.nombre;

        // Cerrar modal
        document.getElementById('auth-overlay').style.display = 'none';

        UIManager.showSuccess(`¡Bienvenido ${usuario.nombre}!`);
    }

    static async handleRegister(event) {
        event.preventDefault();

        const userData = {
            nombre: document.getElementById('register-name').value.trim(),
            email: document.getElementById('register-email').value.trim(),
            password: document.getElementById('register-password').value,
            ciudad: 'Cartagena', // Placeholder
            pais: 'Colombia',     // Placeholder
            presupuestoMensualBase: 5000.00
        };

        if (!userData.nombre || !userData.email || !userData.password) {
            UIManager.showError('Por favor completa todos los campos');
            return;
        }

        const result = await UsuarioService.registrar(userData);

        if (!result.success) {
            UIManager.showError(result.error || 'Error al registrar usuario');
            return;
        }

        UIManager.showSuccess('¡Usuario registrado exitosamente!');

        // Auto-login después de registro
        SessionManager.saveUser(result.data);
        UIManager.showAppLayout();
        UIManager.showPage('comparador-page');
        document.getElementById('user-name-display').textContent = result.data.nombre;
        document.getElementById('auth-overlay').style.display = 'none';
    }

    static handleLogout() {
        if (confirm('¿Seguro que quieres cerrar sesión?')) {
            SessionManager.logout();
            UIManager.showLandingPage();
            UIManager.showSuccess('Sesión cerrada');
        }
    }
}

// Nota: DashboardController ahora está en dashboard.js

// ==========================================
// 🎬 INICIALIZACIÓN
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Budget Optimizer iniciado');

    // Verificar sesión
    if (SessionManager.isLoggedIn()) {
        const user = SessionManager.getUser();
        UIManager.showAppLayout();
        UIManager.showPage('comparador-page');
        document.getElementById('user-name-display').textContent = user.nombre;
    } else {
        UIManager.showLandingPage();
    }

    // ==========================================
    // 🔗 EVENT LISTENERS - AUTENTICACIÓN
    // ==========================================

    // Abrir modal de login
    document.getElementById('btn-open-auth')?.addEventListener('click', () => {
        document.getElementById('auth-overlay').style.display = 'flex';
        document.getElementById('login-form').classList.remove('hidden');
        document.getElementById('register-form').classList.add('hidden');
    });

    document.getElementById('btn-go-comparador')?.addEventListener('click', () => {
        if (SessionManager.isLoggedIn()) {
            UIManager.showAppLayout();
            UIManager.showPage('comparador-page');
        } else {
            document.getElementById('auth-overlay').style.display = 'flex';
        }
    });

    // Cerrar modales
    document.getElementById('close-login')?.addEventListener('click', () => {
        document.getElementById('auth-overlay').style.display = 'none';
    });

    document.getElementById('close-register')?.addEventListener('click', () => {
        document.getElementById('auth-overlay').style.display = 'none';
    });

    // Switch entre login/registro
    document.getElementById('switch-to-register')?.addEventListener('click', () => {
        document.getElementById('login-form').classList.add('hidden');
        document.getElementById('register-form').classList.remove('hidden');
    });

    document.getElementById('switch-to-login')?.addEventListener('click', () => {
        document.getElementById('register-form').classList.add('hidden');
        document.getElementById('login-form').classList.remove('hidden');
    });

    // Formularios
    document.getElementById('form-login')?.addEventListener('submit', AuthController.handleLogin);
    document.getElementById('form-register')?.addEventListener('submit', AuthController.handleRegister);

    // Logout
    document.getElementById('btn-logout')?.addEventListener('click', AuthController.handleLogout);

    // ==========================================
    // 🔗 EVENT LISTENERS - NAVEGACIÓN
    // ==========================================
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const page = item.getAttribute('data-page');
            UIManager.showPage(page);

            // Cargar datos según la página
            if (page === 'dashboard-page') {
                window.dashboardController?.cargarDashboard();
            } else if (page === 'budgets-page') {
                window.presupuestosController?.inicializar();
            }
        });
    });

    console.log('✅ Event listeners configurados');
});

// ==========================================
// 🌐 EXPORTAR PARA USO GLOBAL
// ==========================================
window.SessionManager = SessionManager;
window.ApiClient = ApiClient;
window.UsuarioService = UsuarioService;
window.PresupuestoService = PresupuestoService;
window.CategoriaService = CategoriaService;
window.ExpenseService = ExpenseService;
window.UIManager = UIManager;