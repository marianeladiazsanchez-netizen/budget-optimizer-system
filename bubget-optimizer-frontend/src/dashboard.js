// ============================================
// 📊 DASHBOARD CONTROLLER
// ============================================

class DashboardController {
    constructor() {
        this.expensesChart = null;
    }

    // ==========================================
    // 📈 CARGAR DATOS COMPLETOS
    // ==========================================
    async cargarDashboard() {
        const userId = SessionManager.getUserId();
        if (!userId) {
            UIManager.showError('No hay sesión activa');
            return;
        }

        console.log('📊 Cargando dashboard para usuario:', userId);

        try {
            // Cargar datos en paralelo
            const [statsResult, presupuestoActual, gastosResult] = await Promise.all([
                PresupuestoService.obtenerEstadisticas(userId),
                PresupuestoService.obtenerActual(userId),
                ExpenseService.listarPorUsuario(userId)
            ]);

            // Actualizar tarjetas
            if (statsResult.success) {
                this.actualizarTarjetas(statsResult.data);
            }

            // Actualizar gráficos
            if (gastosResult.success) {
                this.crearGraficoGastos(gastosResult.data);
            }

            console.log('✅ Dashboard cargado exitosamente');
        } catch (error) {
            console.error('❌ Error al cargar dashboard:', error);
            UIManager.showError('Error al cargar el dashboard');
        }
    }

    // ==========================================
    // 💰 ACTUALIZAR TARJETAS
    // ==========================================
    actualizarTarjetas(stats) {
        // Ingresos (presupuesto total)
        document.getElementById('total-income').textContent = 
            `$${(stats.totalPresupuestado || 0).toFixed(2)}`;

        // Gastos
        document.getElementById('total-expenses').textContent = 
            `$${(stats.totalGastado || 0).toFixed(2)}`;

        // Ahorros (restante)
        const ahorros = (stats.totalRestante || 0);
        const ahorrrosEl = document.getElementById('total-savings');
        ahorrrosEl.textContent = `$${ahorros.toFixed(2)}`;
        
        // Color según si es positivo o negativo
        ahorrrosEl.style.color = ahorros >= 0 ? '#4CAF50' : '#F44336';

        console.log('✅ Tarjetas actualizadas:', {
            presupuestado: stats.totalPresupuestado,
            gastado: stats.totalGastado,
            restante: stats.totalRestante
        });
    }

    // ==========================================
    // 📊 CREAR GRÁFICO DE GASTOS POR CATEGORÍA
    // ==========================================
    crearGraficoGastos(gastos) {
        if (!gastos || gastos.length === 0) {
            console.log('⚠️ No hay gastos para mostrar en el gráfico');
            document.getElementById('expenses-chart').parentElement.innerHTML = 
                '<p style="text-align:center;color:#999;">No hay gastos registrados aún</p>';
            return;
        }

        // Agrupar gastos por categoría
        const gastosPorCategoria = {};
        
        gastos.forEach(gasto => {
            const categoria = gasto.categoriaNombre || 'Sin categoría';
            if (!gastosPorCategoria[categoria]) {
                gastosPorCategoria[categoria] = 0;
            }
            gastosPorCategoria[categoria] += parseFloat(gasto.monto);
        });

        // Preparar datos para Chart.js
        const categorias = Object.keys(gastosPorCategoria);
        const montos = Object.values(gastosPorCategoria);

        // Colores dinámicos
        const colores = this.generarColores(categorias.length);

        // Destruir gráfico anterior si existe
        if (this.expensesChart) {
            this.expensesChart.destroy();
        }

        // Crear nuevo gráfico
        const ctx = document.getElementById('expenses-chart');
        if (!ctx) {
            console.error('❌ Canvas no encontrado');
            return;
        }

        this.expensesChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: categorias,
                datasets: [{
                    label: 'Gastos por Categoría',
                    data: montos,
                    backgroundColor: colores,
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 15,
                            font: {
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.parsed || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${label}: $${value.toFixed(2)} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });

        console.log('✅ Gráfico de gastos creado:', {
            categorias: categorias.length,
            total: montos.reduce((a, b) => a + b, 0).toFixed(2)
        });
    }

    // ==========================================
    // 🎨 GENERAR COLORES DINÁMICOS
    // ==========================================
    generarColores(cantidad) {
        const coloresBase = [
            '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', 
            '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2',
            '#F8B739', '#52B788', '#E76F51', '#2A9D8F'
        ];

        const colores = [];
        for (let i = 0; i < cantidad; i++) {
            colores.push(coloresBase[i % coloresBase.length]);
        }
        return colores;
    }

    // ==========================================
    // 🔄 REFRESCAR DASHBOARD
    // ==========================================
    async refrescar() {
        console.log('🔄 Refrescando dashboard...');
        await this.cargarDashboard();
    }
}

// ==========================================
// 🌐 EXPORTAR
// ==========================================
window.DashboardController = DashboardController;

// Instancia global
window.dashboardController = new DashboardController();

console.log('✅ DashboardController cargado');