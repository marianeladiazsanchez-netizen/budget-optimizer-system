// ============================================
// 💰 PRESUPUESTOS CONTROLLER
// ============================================

class PresupuestosController {
    constructor() {
        this.presupuestos = [];
        this.categorias = [];
    }

    // ==========================================
    // 📋 INICIALIZAR PÁGINA
    // ==========================================
    async inicializar() {
        console.log('💰 Inicializando módulo de presupuestos');

        // Cargar categorías para el formulario
        await this.cargarCategorias();

        // Cargar presupuestos del usuario
        await this.cargarPresupuestos();

        // Configurar event listeners
        this.configurarEventos();
    }

    // ==========================================
    // 📁 CARGAR CATEGORÍAS
    // ==========================================
    async cargarCategorias() {
        const result = await CategoriaService.listarParaGastos();
        
        if (result.success) {
            this.categorias = result.data;
            console.log(`✅ ${this.categorias.length} categorías cargadas`);
        } else {
            console.error('❌ Error al cargar categorías');
        }
    }

    // ==========================================
    // 📊 CARGAR PRESUPUESTOS
    // ==========================================
    async cargarPresupuestos() {
        const userId = SessionManager.getUserId();
        if (!userId) return;

        const result = await PresupuestoService.listarPorUsuario(userId);

        if (result.success) {
            this.presupuestos = result.data;
            this.renderizarLista();
            console.log(`✅ ${this.presupuestos.length} presupuestos cargados`);
        } else {
            console.error('❌ Error al cargar presupuestos');
        }
    }

    // ==========================================
    // 🎨 RENDERIZAR LISTA
    // ==========================================
    renderizarLista() {
        const container = document.getElementById('budgets-list');
        if (!container) return;

        if (this.presupuestos.length === 0) {
            container.innerHTML = `
                <div style="text-align:center; padding:3rem; color:#999;">
                    <i class="fas fa-wallet" style="font-size:3rem;margin-bottom:1rem;"></i>
                    <p>No tienes presupuestos creados</p>
                    <button class="btn btn-primary" onclick="presupuestosController.mostrarFormulario()">
                        <i class="fas fa-plus"></i> Crear mi primer presupuesto
                    </button>
                </div>
            `;
            return;
        }

        container.innerHTML = this.presupuestos.map(p => this.crearCardPresupuesto(p)).join('');
    }

    // ==========================================
    // 🎴 CREAR CARD DE PRESUPUESTO
    // ==========================================
    crearCardPresupuesto(presupuesto) {
        const porcentaje = presupuesto.porcentajeGastado || 0;
        const colorBarra = this.obtenerColorPorcentaje(porcentaje);
        const estadoBadge = this.obtenerBadgeEstado(presupuesto.status);

        return `
            <div class="budget-card" data-id="${presupuesto.id}">
                <div class="budget-header">
                    <h3>${presupuesto.nombre}</h3>
                    ${estadoBadge}
                </div>
                
                <div class="budget-periodo">
                    <i class="fas fa-calendar"></i>
                    ${presupuesto.periodoDisplay} (${presupuesto.duracionDias} días)
                </div>

                <div class="budget-fechas">
                    <small>
                        ${this.formatearFecha(presupuesto.fechaInicio)} - 
                        ${this.formatearFecha(presupuesto.fechaFin)}
                    </small>
                </div>

                <div class="budget-montos">
                    <div class="monto-item">
                        <span>Total:</span>
                        <strong>$${presupuesto.montoTotal.toFixed(2)}</strong>
                    </div>
                    <div class="monto-item">
                        <span>Gastado:</span>
                        <strong style="color:${presupuesto.estaExcedido ? '#F44336' : '#FF9800'}">
                            $${presupuesto.montoGastado.toFixed(2)}
                        </strong>
                    </div>
                    <div class="monto-item">
                        <span>Restante:</span>
                        <strong style="color:${presupuesto.montoRestante >= 0 ? '#4CAF50' : '#F44336'}">
                            $${presupuesto.montoRestante.toFixed(2)}
                        </strong>
                    </div>
                </div>

                <div class="budget-progress">
                    <div class="progress-bar">
                        <div class="progress-fill" 
                             style="width:${Math.min(porcentaje, 100)}%; background:${colorBarra}">
                        </div>
                    </div>
                    <span class="progress-text">${porcentaje.toFixed(1)}% utilizado</span>
                </div>

                <div class="budget-actions">
                    ${this.generarBotonesAccion(presupuesto)}
                </div>
            </div>
        `;
    }

    // ==========================================
    // 🎯 GENERAR BOTONES DE ACCIÓN
    // ==========================================
    generarBotonesAccion(presupuesto) {
        let botones = '';

        // Botón Activar (si está en DRAFT o PAUSED)
        if (presupuesto.status === 'DRAFT' || presupuesto.status === 'PAUSED') {
            botones += `
                <button class="btn btn-success btn-sm" 
                        onclick="presupuestosController.activar(${presupuesto.id})">
                    <i class="fas fa-play"></i> Activar
                </button>
            `;
        }

        // Botón Pausar (si está ACTIVE o EXCEEDED)
        if (presupuesto.status === 'ACTIVE' || presupuesto.status === 'EXCEEDED') {
            botones += `
                <button class="btn btn-warning btn-sm" 
                        onclick="presupuestosController.pausar(${presupuesto.id})">
                    <i class="fas fa-pause"></i> Pausar
                </button>
            `;
        }

        // Botón Completar (si no está ya completado)
        if (!['COMPLETED', 'CANCELLED', 'ARCHIVED'].includes(presupuesto.status)) {
            botones += `
                <button class="btn btn-primary btn-sm" 
                        onclick="presupuestosController.completar(${presupuesto.id})">
                    <i class="fas fa-check"></i> Completar
                </button>
            `;
        }

        // Botón Ver Gastos (si está activo)
        if (presupuesto.puedeRegistrarGastos) {
            botones += `
                <button class="btn btn-info btn-sm" 
                        onclick="presupuestosController.verGastos(${presupuesto.id})">
                    <i class="fas fa-receipt"></i> Ver Gastos (${presupuesto.totalGastos})
                </button>
            `;
        }

        return botones;
    }

    // ==========================================
    // 🎨 HELPERS DE UI
    // ==========================================
    obtenerColorPorcentaje(porcentaje) {
        if (porcentaje >= 90) return '#F44336'; // Rojo
        if (porcentaje >= 70) return '#FF9800'; // Naranja
        return '#4CAF50'; // Verde
    }

    obtenerBadgeEstado(status) {
        const colores = {
            DRAFT: '#9E9E9E',
            ACTIVE: '#4CAF50',
            PAUSED: '#FF9800',
            COMPLETED: '#2196F3',
            EXCEEDED: '#F44336',
            CANCELLED: '#757575',
            ARCHIVED: '#607D8B'
        };

        const nombres = {
            DRAFT: 'Borrador',
            ACTIVE: 'Activo',
            PAUSED: 'Pausado',
            COMPLETED: 'Completado',
            EXCEEDED: 'Excedido',
            CANCELLED: 'Cancelado',
            ARCHIVED: 'Archivado'
        };

        return `<span class="badge" style="background:${colores[status]}">${nombres[status]}</span>`;
    }

    formatearFecha(fecha) {
        const d = new Date(fecha);
        return d.toLocaleDateString('es-ES', { 
            day: '2-digit', 
            month: 'short', 
            year: 'numeric' 
        });
    }

    // ==========================================
    // ✏️ MOSTRAR FORMULARIO DE CREACIÓN
    // ==========================================
    mostrarFormulario() {
        const modal = this.crearModalFormulario();
        document.body.insertAdjacentHTML('beforeend', modal);
    }

    crearModalFormulario() {
        return `
            <div id="modal-presupuesto" class="modal-overlay" style="display:flex;">
                <div class="modal-content" style="max-width:600px;">
                    <div class="modal-header">
                        <h2>Crear Nuevo Presupuesto</h2>
                        <button class="btn-close" onclick="presupuestosController.cerrarModal()">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>

                    <form id="form-presupuesto" onsubmit="presupuestosController.guardar(event)">
                        <div class="form-group">
                            <label>Nombre del Presupuesto *</label>
                            <input type="text" name="nombre" required 
                                   placeholder="Ej: Presupuesto Enero 2025"
                                   class="form-control">
                        </div>

                        <div class="form-group">
                            <label>Descripción</label>
                            <textarea name="descripcion" rows="3" 
                                      placeholder="Opcional"
                                      class="form-control"></textarea>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label>Monto Total *</label>
                                <input type="number" name="montoTotal" required 
                                       min="0.01" step="0.01"
                                       placeholder="0.00"
                                       class="form-control">
                            </div>

                            <div class="form-group">
                                <label>Período *</label>
                                <select name="periodo" required class="form-control">
                                    <option value="DAILY">Diario</option>
                                    <option value="WEEKLY">Semanal</option>
                                    <option value="BIWEEKLY">Quincenal</option>
                                    <option value="MONTHLY" selected>Mensual</option>
                                    <option value="QUARTERLY">Trimestral</option>
                                    <option value="YEARLY">Anual</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label>Fecha Inicio *</label>
                                <input type="datetime-local" name="fechaInicio" required 
                                       class="form-control">
                            </div>

                            <div class="form-group">
                                <label>Fecha Fin *</label>
                                <input type="datetime-local" name="fechaFin" required 
                                       class="form-control">
                            </div>
                        </div>

                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" 
                                    onclick="presupuestosController.cerrarModal()">
                                Cancelar
                            </button>
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save"></i> Crear Presupuesto
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        `;
    }

    // ==========================================
    // 💾 GUARDAR PRESUPUESTO
    // ==========================================
    async guardar(event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        const presupuestoData = {
            nombre: formData.get('nombre'),
            descripcion: formData.get('descripcion'),
            montoTotal: parseFloat(formData.get('montoTotal')),
            periodo: formData.get('periodo'),
            fechaInicio: formData.get('fechaInicio'),
            fechaFin: formData.get('fechaFin')
        };

        // Validar fechas
        if (new Date(presupuestoData.fechaFin) <= new Date(presupuestoData.fechaInicio)) {
            UIManager.showError('La fecha de fin debe ser posterior a la de inicio');
            return;
        }

        const userId = SessionManager.getUserId();
        const result = await PresupuestoService.crear(userId, presupuestoData);

        if (result.success) {
            UIManager.showSuccess('¡Presupuesto creado exitosamente!');
            this.cerrarModal();
            await this.cargarPresupuestos();
        } else {
            UIManager.showError(result.error || 'Error al crear presupuesto');
        }
    }

    // ==========================================
    // 🎬 ACCIONES DE PRESUPUESTO
    // ==========================================
    async activar(id) {
        const result = await PresupuestoService.activar(id);
        
        if (result.success) {
            UIManager.showSuccess('Presupuesto activado');
            await this.cargarPresupuestos();
        } else {
            UIManager.showError(result.error);
        }
    }

    async pausar(id) {
        const result = await ApiClient.put(`/presupuestos/${id}/pausar`);
        
        if (result.success) {
            UIManager.showSuccess('Presupuesto pausado');
            await this.cargarPresupuestos();
        } else {
            UIManager.showError(result.error);
        }
    }

    async completar(id) {
        if (!confirm('¿Seguro que quieres completar este presupuesto?')) return;

        const result = await ApiClient.put(`/presupuestos/${id}/completar`);
        
        if (result.success) {
            UIManager.showSuccess('Presupuesto completado');
            await this.cargarPresupuestos();
        } else {
            UIManager.showError(result.error);
        }
    }

    verGastos(id) {
        // TODO: Implementar vista de gastos
        console.log('Ver gastos del presupuesto:', id);
        UIManager.showError('Función en desarrollo');
    }

    // ==========================================
    // 🔧 UTILIDADES
    // ==========================================
    cerrarModal() {
        const modal = document.getElementById('modal-presupuesto');
        if (modal) modal.remove();
    }

    configurarEventos() {
        // Botón crear presupuesto
        const btnAdd = document.getElementById('btn-add-budget');
        if (btnAdd) {
            btnAdd.onclick = () => this.mostrarFormulario();
        }
    }
}

// ==========================================
// 🌐 EXPORTAR
// ==========================================
window.PresupuestosController = PresupuestosController;
window.presupuestosController = new PresupuestosController();

console.log('✅ PresupuestosController cargado');