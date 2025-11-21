/**
 * ============================================
 * MAIN.JS - BUDGET OPTIMIZER
 * ============================================
 * Versión REFACTORIZADA con Backend integrado
 */

// ============================================
// 📦 IMPORTS
// ============================================
import usuarioService from './api/services/usuarioService.js';
import { 
  showFieldError, 
  clearFormErrors, 
  showError, 
  showSuccess,
  isValidEmail,
  validatePassword
} from './utils/errorHandler.js';

// ============================================
// 🗂️ DATOS MOCK
// ============================================
const mockProducts = [
  {
    id: 1, name: 'iPhone 15', image: '📱', category: 'Electrónica',
    prices: [
      { store: 'Amazon', price: 999 },
      { store: 'Best Buy', price: 1019 },
      { store: 'Apple', price: 999 }
    ]
  },
  {
    id: 2, name: 'Zapatillas Nike', image: '👟', category: 'Ropa',
    prices: [
      { store: 'Amazon', price: 120 },
      { store: 'Nike Store', price: 140 },
      { store: 'Walmart', price: 115 }
    ]
  },
  {
    id: 3, name: 'Laptop HP', image: '💻', category: 'Electrónica',
    prices: [
      { store: 'Amazon', price: 599 },
      { store: 'Best Buy', price: 579 },
      { store: 'Newegg', price: 589 }
    ]
  },
  {
    id: 4, name: 'Sofá gris', image: '🛋️', category: 'Muebles',
    prices: [
      { store: 'IKEA', price: 299 },
      { store: 'Wayfair', price: 319 },
      { store: 'Amazon', price: 289 }
    ]
  }
];

// ============================================
// 🌍 VARIABLES GLOBALES
// ============================================
let currentProducts = [...mockProducts];
let selectedProduct = null;
let currentUser = null;

// Gráficas
let expensesChart = null;
let trendChart = null;
let distributionChart = null;

// ============================================
// 🚀 INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', () => {
  console.log('🚀 Iniciando Budget Optimizer...');
  initApp();
  setupEventListeners();
});

function initApp() {
  currentUser = usuarioService.getCurrentUser();
  
  if (currentUser) {
    console.log('✅ Usuario en sesión:', currentUser.email);
    goToDashboard();
  } else {
    console.log('ℹ️ No hay sesión activa');
  }
}

// ============================================
// 🎯 EVENT LISTENERS - SIN ONCLICK INLINE
// ============================================
function setupEventListeners() {
  // Autenticación
  document.getElementById('form-login')?.addEventListener('submit', handleLogin);
  document.getElementById('form-register')?.addEventListener('submit', handleRegister);
  
  // Botones de navegación
  document.getElementById('btn-open-auth')?.addEventListener('click', openAuthOverlay);
  document.getElementById('btn-go-comparador')?.addEventListener('click', goToComparador);
  document.getElementById('btn-logout')?.addEventListener('click', handleLogout);
  
  // Modales
  document.getElementById('close-login')?.addEventListener('click', closeAuthOverlay);
  document.getElementById('close-register')?.addEventListener('click', closeAuthOverlay);
  document.getElementById('switch-to-register')?.addEventListener('click', switchToRegister);
  document.getElementById('switch-to-login')?.addEventListener('click', switchToLogin);
  
  // Navegación lateral
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', () => navigateTo(item.dataset.page));
  });

  // Búsqueda
  document.getElementById('btn-search')?.addEventListener('click', handleSearch);
  document.getElementById('search-input')?.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') handleSearch();
  });
  
  // Botones de acciones
  document.getElementById('btn-add-budget')?.addEventListener('click', openAddBudget);
  document.getElementById('btn-add-transaction')?.addEventListener('click', openAddTransaction);
}

// ============================================
// 🔐 AUTENTICACIÓN
// ============================================
async function handleRegister(e) {
  e.preventDefault();
  clearFormErrors('form-register');
  
  try {
    const nombre = document.getElementById('register-name').value.trim();
    const email = document.getElementById('register-email').value.trim();
    const password = document.getElementById('register-password').value;
    
    // Validaciones frontend
    if (!nombre || nombre.length < 2) {
      showFieldError('register-name', 'El nombre debe tener al menos 2 caracteres');
      return;
    }
    
    if (!isValidEmail(email)) {
      showFieldError('register-email', 'El email no es válido');
      return;
    }
    
    const passwordValidation = validatePassword(password);
    if (!passwordValidation.valid) {
      showFieldError('register-password', passwordValidation.errors[0]);
      return;
    }
    
    // Datos para el backend
    const userData = {
      nombre,
      email,
      password,
      ciudad: 'Cartagena',
      pais: 'Colombia',
      presupuestoMensualBase: 5000.00,
      latitud: 10.4236,
      longitud: -75.5223
    };
    
    console.log('📤 Registrando usuario...', { email });
    
    const usuario = await usuarioService.registrar(userData);
    
    console.log('✅ Usuario registrado:', usuario);
    showSuccess('¡Registro exitoso! Por favor inicia sesión.');
    
    document.getElementById('form-register').reset();
    switchToLogin();
    
  } catch (error) {
    console.error('❌ Error en registro:', error);
    
    if (error.response?.status === 409) {
      showFieldError('register-email', 'Este email ya está registrado');
    } else {
      showError(error, 'al registrar usuario');
    }
  }
}

async function handleLogin(e) {
  e.preventDefault();
  clearFormErrors('form-login');
  
  try {
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    
    if (!email || !password) {
      showFieldError('login-email', 'Todos los campos son requeridos');
      return;
    }
    
    if (!isValidEmail(email)) {
      showFieldError('login-email', 'El email no es válido');
      return;
    }
    
    console.log('📤 Intentando login...', { email });
    
    const usuario = await usuarioService.login(email, password);
    
    currentUser = usuario;
    
    console.log('✅ Login exitoso:', usuario.email);
    document.getElementById('form-login').reset();
    closeAuthOverlay();
    goToDashboard();
    
    alert(`¡Bienvenido, ${usuario.nombre}! 🎉`);
    
  } catch (error) {
    console.error('❌ Error en login:', error);
    
    if (error.response?.status === 404) {
      showFieldError('login-email', 'Usuario no encontrado');
    } else {
      showFieldError('login-password', 'Email o contraseña incorrectos');
    }
  }
}

function handleLogout() {
  if (!confirm('¿Estás seguro de que deseas cerrar sesión?')) {
    return;
  }
  
  usuarioService.logout();
  currentUser = null;
  
  document.getElementById('landing-page').classList.add('active');
  document.getElementById('app-layout').classList.add('hidden');
  
  console.log('✅ Sesión cerrada');
}

// ============================================
// 🎨 UI - OVERLAYS
// ============================================
function openAuthOverlay() {
  document.getElementById('auth-overlay')?.classList.add('active');
}

function closeAuthOverlay() {
  document.getElementById('auth-overlay')?.classList.remove('active');
}

function switchToRegister() {
  document.getElementById('login-form')?.classList.add('hidden');
  document.getElementById('register-form')?.classList.remove('hidden');
}

function switchToLogin() {
  document.getElementById('register-form')?.classList.add('hidden');
  document.getElementById('login-form')?.classList.remove('hidden');
}

// ============================================
// 🧭 NAVEGACIÓN
// ============================================
function goToComparador() {
  if (!currentUser) {
    openAuthOverlay();
    return;
  }
  showAppLayout();
  navigateTo('comparador');
}

function goToDashboard() {
  showAppLayout();
  navigateTo('dashboard');
}

function navigateTo(page) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById(page + '-page')?.classList.add('active');
  
  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
  document.querySelector(`[data-page="${page}"]`)?.classList.add('active');
  
  switch (page) {
    case 'comparador':
      renderProducts();
      loadChart();
      break;
    case 'dashboard':
      loadDashboardData();
      break;
    case 'reports':
      loadReports();
      break;
  }
}

function showAppLayout() {
  document.getElementById('landing-page')?.classList.remove('active');
  document.getElementById('app-layout')?.classList.remove('hidden');
  
  if (currentUser) {
    const nameDisplay = document.getElementById('user-name-display');
    if (nameDisplay) {
      nameDisplay.textContent = currentUser.nombre || currentUser.name;
    }
  }
}

// ============================================
// 🛒 COMPARADOR DE PRODUCTOS
// ============================================
function renderProducts() {
  const container = document.getElementById('products-list');
  if (!container) return;
  
  if (currentProducts.length === 0) {
    container.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: #999;">No se encontraron productos</p>';
    return;
  }

  container.innerHTML = currentProducts.map(product => `
    <div class="product-card" data-product-id="${product.id}">
      <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">${product.image}</div>
      <h4>${product.name}</h4>
      <p>$${Math.min(...product.prices.map(p => p.price)).toFixed(2)}</p>
    </div>
  `).join('');
  
  // ✅ Event delegation en lugar de onclick inline
  container.querySelectorAll('.product-card').forEach(card => {
    card.addEventListener('click', () => {
      const productId = parseInt(card.dataset.productId);
      selectProduct(productId);
    });
  });
}

function selectProduct(productId) {
  selectedProduct = currentProducts.find(p => p.id === productId);
  if (!selectedProduct) return;

  document.querySelectorAll('.product-card').forEach(card => card.classList.remove('selected'));
  document.querySelector(`[data-product-id="${productId}"]`)?.classList.add('selected');

  const detailHtml = `
    <div style="text-align: center;">
      <div style="font-size: 4rem; margin-bottom: 1rem;">${selectedProduct.image}</div>
      <h3 style="color: #4f46e5; font-size: 1.5rem; margin-bottom: 0.5rem;">${selectedProduct.name}</h3>
      <p style="color: #999; margin-bottom: 0.5rem;">${selectedProduct.category}</p>
      <p style="color: #999; margin-bottom: 1.5rem;">Compara precios en todas las tiendas</p>
    </div>
  `;
  
  const detailContainer = document.getElementById('product-detail');
  if (detailContainer) {
    detailContainer.innerHTML = detailHtml;
  }

  showComparison(selectedProduct);
  updateStats(selectedProduct);
}

function showComparison(product) {
  const container = document.getElementById('comparison-list');
  if (!container) return;
  
  const sorted = [...product.prices].sort((a, b) => a.price - b.price);
  
  container.innerHTML = sorted.map((item, index) => `
    <div class="store-item">
      <span class="store-name">${index === 0 ? '🏆 ' : ''}${item.store}</span>
      <span class="store-price">$${item.price.toFixed(2)}</span>
    </div>
  `).join('');
}

function updateStats(product) {
  const prices = product.prices.map(p => p.price);
  const avg = Math.round(prices.reduce((a, b) => a + b) / prices.length);
  const best = Math.min(...prices);
  const worst = Math.max(...prices);
  const savings = worst - best;

  document.getElementById('avg-price').textContent = `$${avg.toFixed(2)}`;
  document.getElementById('best-price').textContent = `$${best.toFixed(2)}`;
  document.getElementById('max-savings').textContent = `$${savings.toFixed(2)}`;

  updateDistributionChart(product);
}

function handleSearch() {
  const searchInput = document.getElementById('search-input');
  if (!searchInput) return;
  
  const searchTerm = searchInput.value.trim().toLowerCase();
  
  if (!searchTerm) {
    currentProducts = [...mockProducts];
  } else {
    currentProducts = mockProducts.filter(p => 
      p.name.toLowerCase().includes(searchTerm) ||
      p.category.toLowerCase().includes(searchTerm)
    );
  }
  
  renderProducts();
  selectedProduct = null;
  
  document.getElementById('product-detail').innerHTML = '<p style="color: #999; text-align: center; padding: 2rem;"><i class="fas fa-inbox"></i> Selecciona un producto</p>';
  document.getElementById('comparison-list').innerHTML = '<p style="color: #999; text-align: center; padding: 1.5rem;"><i class="fas fa-inbox"></i> No hay comparación</p>';
}

// ============================================
// 📊 DASHBOARD
// ============================================
function loadDashboardData() {
  const expenses = currentUser?.expenses || [];
  const totalExpenses = expenses.reduce((sum, e) => sum + e.amount, 0);
  const totalIncome = currentUser?.income || 0;
  const savings = totalIncome - totalExpenses;

  document.getElementById('total-income').textContent = `$${totalIncome.toFixed(2)}`;
  document.getElementById('total-expenses').textContent = `$${totalExpenses.toFixed(2)}`;
  document.getElementById('total-savings').textContent = `$${savings.toFixed(2)}`;

  loadExpensesChart();
}

// ============================================
// 📈 GRÁFICAS
// ============================================
function loadExpensesChart() {
  const canvas = document.getElementById('expenses-chart');
  if (!canvas) return;
  
  if (expensesChart) expensesChart.destroy();
  
  const ctx = canvas.getContext('2d');
  expensesChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Alimentación', 'Transporte', 'Vivienda'],
      datasets: [{
        data: [1200, 400, 800],
        backgroundColor: ['#4f46e5', '#6366f1', '#a5b4fc'],
        borderColor: '#fff',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { position: 'bottom' } }
    }
  });
}

function loadChart() {
  const canvas = document.getElementById('trend-chart');
  if (!canvas) return;
  
  if (trendChart) trendChart.destroy();
  
  const ctx = canvas.getContext('2d');
  trendChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sab', 'Dom'],
      datasets: [{
        label: 'Precio promedio',
        data: [850, 820, 900, 870, 880, 900, 920],
        borderColor: '#4f46e5',
        backgroundColor: 'rgba(79, 70, 229, 0.1)',
        borderWidth: 3,
        fill: true,
        tension: 0.4
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } }
    }
  });
}

function updateDistributionChart(product) {
  const canvas = document.getElementById('distribution-chart');
  if (!canvas) return;
  
  if (distributionChart) distributionChart.destroy();
  
  const ctx = canvas.getContext('2d');
  const stores = product.prices.map(p => p.store);
  const prices = product.prices.map(p => p.price);
  
  distributionChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: stores,
      datasets: [{
        data: prices,
        backgroundColor: ['#4f46e5', '#6366f1', '#a5b4fc'],
        borderColor: '#fff',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { position: 'bottom' } }
    }
  });
}

// ============================================
// 💰 PRESUPUESTOS Y TRANSACCIONES
// ============================================
function openAddBudget() {
  alert('📋 Funcionalidad de presupuestos próximamente');
}

function openAddTransaction() {
  alert('💸 Funcionalidad de transacciones próximamente');
}

function loadReports() {
  console.log('📊 Cargando reportes...');
}

// ============================================
// 📝 LOG FINAL
// ============================================
console.log(`
✅ Main.js cargado
📦 Módulos conectados:
   - ✅ Usuarios (Backend)
   - ⚠️ Presupuestos (Pendiente)
   - ⚠️ Gastos (Pendiente)
`);