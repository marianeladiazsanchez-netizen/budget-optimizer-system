// ============================================
// 💸 GASTOS CONTROLLER
// ============================================

class GastosController {
    constructor() {
        this.gastos = [];
        this.categorias = [];
        this.presupuestoActual = null;
    }

    // ==========================================
    // 🎬 INICIALIZAR
    // ==========================================
    async inicializar() {
        console.log('💸 Inicializando módulo de gastos');

        try {
            // Cargar datos necesarios
            await this.cargarCategorias();
            await this.cargarPresupuestoActual();
            await this.cargarGastos();

            // Configurar eventos
            this.configurarEventos();
        } catch (error) {
            console.error('❌ Error al inicializar gastos:', error);
        }
    }

    // ==========================================
    // 📁 CARGAR CATEGORÍAS
    // ==========================================
    async cargarCategorias() {
        const result = await CategoriaService.listarParaGastos();
        
        if (result.success) {
            this.categorias = result.data;
            console.log(`✅ ${this.categorias.length} categorías cargadas`);
        }
    }

    // ==========================================
    // 💰 CARGAR PRESUPUESTO ACTUAL
    // ==========================================
    async cargarPresupuestoActual() {
        const userId = SessionManager.getUserId();
        if (!userId) return;

        const result = await PresupuestoService.obtenerActual(userId);

        if (result.success) {
            this.presupuestoActual = result.data;
            console.log('✅ Presupuesto actual:', this.presupuestoActual.nombre);
        } else {
            console.log('⚠️ No hay presupuesto activo');
            this.presupuestoActual = null;
        }
    }

    // ==========================================
    // 📊 CARGAR GASTOS
    // ==========================================
    async cargarGastos() {
        const userId = SessionManager.getUserId();
        if (!userId) return;

        const result = await ExpenseService.listarPorUsuario(userId);

        if (result.success) {
            this.gastos = result.data;
            this.renderizarLista();
            this.actualizarResumen();
            console.log(`✅ ${this.gastos.length} gastos cargados`);
        }
    }

    // ==========================================
    // 🎨 RENDERIZAR LISTA DE GASTOS
    // ==========================================
    renderizarLista() {
        const container = document.getElementById('transactions-list');
        if (!container) return;

        if (this.gastos.length === 0) {
            container.innerHTML = `
                <div style="text-align:center; padding:3rem; color:#999;">
                    <i class="fas fa-receipt" style="font-size:3rem;margin-bottom:1rem;"></i>
                    <p>No hay gastos registrados</p>
                    <button class="btn btn-primary" onclick="gastosController.mostrarFormulario()">
                        <i class="fas fa-plus"></i> Registrar mi primer gasto
                    </button>
                </div>
            `;
            return;
        }

        // Agrupar por fecha
        const gastosPorFecha = this.agruparPorFecha(this.gastos);

        let html = '';
        for (const [fecha, gastosDia] of Object.entries(gastosPorFecha)) {
            html += `
                <div class="gastos-grupo">
                    <div class="gastos-fecha-header">
                        <h3>${this.formatearFechaGrupo(fecha)}</h3>
                        <span class="total-dia">$${this.calcularTotalDia(gastosDia).toFixed(2)}</span>
                    </div>
                    <div class="gastos-items">
                        ${gastosDia.map(g => this.crearItemGasto(g)).join('')}
                    </div>
                </div>
            `;
        }

        container.innerHTML = html;
    }

    // ==========================================
    // 🎴 CREAR ITEM DE GASTO
    // ==========================================
    crearItemGasto(gasto) {
        return `
            <div class="gasto-item" data-id="${gasto.id}">
                <div class="gasto-icon" style="background:${gasto.categoriaColor || '#999'}">
                    ${gasto.categoriaIcono || '📝'}
                </div>
                <div class="gasto-info">
                    <h4>${gasto.descripcion}</h4>
                    <div class="gasto-meta">
                        <span class="categoria-tag">${gasto.categoriaNombre}</span>
                        <span class="tiempo">${this.formatearHora(gasto.fechaGasto)}</span>
                        ${gasto.presupuestoNombre ? 
                            `<span class="presupuesto-tag">${gasto.presupuestoNombre}</span>` : ''}
                    </div>
                </div>
                <div class="gasto-monto">
                    <span class="monto-principal">$${parseFloat(gasto.monto).toFixed(2)}</span>
                </div>
                <div class="gasto-actions">
                    <button class="btn-icon" onclick="gastosController.eliminar(${gasto.id})" title="Eliminar">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }

    // ==========================================
    // 📈 ACTUALIZAR RESUMEN
    // ==========================================
    actualizarResumen() {
        // Calcular totales
        const total = this.gastos.reduce((sum, g) => sum + parseFloat(g.monto), 0);

        // Agrupar por categoría
        const porCategoria = {};
        this.gastos.forEach(g => {
            const cat = g.categoriaNombre || 'Sin categoría';
            porCategoria[cat] = (porCategoria[cat] || 0) + parseFloat(g.monto);
        });

        // Encontrar top 3 categorías
        const top3 = Object.entries(porCategoria)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 3);

        console.log('📊 Resumen:', { total, categorias: Object.keys(porCategoria).length });
    }

    // ==========================================
    // ✏️ MOSTRAR FORMULARIO
    // ==========================================
    mostrarFormulario() {
        if (!this.presupuestoActual) {
            UIManager.showError('No tienes un presupuesto activo. Crea uno primero en la sección Presupuestos.');
            return;
        }

        const modal = this.crearModalFormulario();
        document.body.insertAdjacentHTML('beforeend', modal);

        // Pre-seleccionar fecha y hora actual
        const ahora = new Date();
        const fechaInput = document.getElementById('gasto-fecha');
        if (fechaInput) {
            fechaInput.value = this.formatearFechaParaInput(ahora);
        }
    }

    crearModalFormulario() {
        return `
            <div id="modal-gasto" class="modal-overlay" style="display:flex;">
                <div class="modal-content" style="max-width:500px;">
                    <div class="modal-header">
                        <h2>Registrar Nuevo Gasto</h2>
                        <button class="btn-close" onclick="gastosController.cerrarModal()">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>

                    <form id="form-gasto" onsubmit="gastosController.guardar(event)">
                        <div class="form-group">
                            <label>Monto *</label>
                            <div class="input-group">
                                <span class="input-prefix">$</span>
                                <input type="number" name="monto" required 
                                       min="0.01" step="0.01"
                                       placeholder="0.00"
                                       class="form-control"
                                       autofocus>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Descripción *</label>
                            <input type="text" name="descripcion" required 
                                   placeholder="Ej: Almuerzo en restaurante"
                                   maxlength="200"
                                   class="form-control">
                        </div>

                        <div class="form-group">
                            <label>Categoría *</label>
                            <select name="categoriaId" required class="form-control">
                                <option value="">Selecciona una categoría</option>
                                ${this.categorias.map(c => 
                                    `<option value="${c.id}">${c.icono} ${c.nombre}</option>`
                                ).join('')}
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Fecha y Hora *</label>
                            <input type="datetime-local" 
                                   id="gasto-fecha"
                                   name="fechaGasto" 
                                   required 
                                   class="form-control">
                        </div>

                        <div class="form-group">
                            <label>Método de Pago</label>
                            <select name="metodoPago" class="form-control">
                                <option value="CASH">💵 Efectivo</option>
                                <option value="DEBIT_CARD">💳 Tarjeta de Débito</option>
                                <option value="CREDIT_CARD">💳 Tarjeta de Crédito</option>
                                <option value="MOBILE_PAYMENT">📱 Pago Móvil</option>
                                <option value="QR_CODE">📲 Código QR</option>
                                <option value="BANK_TRANSFER">🏦 Transferencia</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Notas (opcional)</label>
                            <textarea name="notas" rows="3" 
                                      placeholder="Agrega notas adicionales..."
                                      maxlength="300"
                                      class="form-control"></textarea>
                        </div>

                        <div class="presupuesto-info">
                            <i class="fas fa-info-circle"></i>
                            Se registrará en: <strong>${this.presupuestoActual.nombre}</strong>
                        </div>

                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" 
                                    onclick="gastosController.cerrarModal()">
                                Cancelar
                            </button>
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save"></i> Registrar Gasto
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        `;
    }

    // ==========================================
    // 💾 GUARDAR GASTO
    // ==========================================
    async guardar(event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        const gastoData = {
            monto: parseFloat(formData.get('monto')),
            descripcion: formData.get('descripcion'),
            categoriaId: parseInt(formData.get('categoriaId')),
            fechaGasto: formData.get('fechaGasto'),
            metodoPago: formData.get('metodoPago') || 'CASH',
            notas: formData.get('notas') || null,
            presupuestoId: this.presupuestoActual.id,
            usuarioId: parseInt(SessionManager.getUserId())
        };

        console.log('💾 Guardando gasto:', gastoData);

        const result = await ExpenseService.crear(gastoData);

        if (result.success) {
            UIManager.showSuccess('¡Gasto registrado exitosamente!');
            this.cerrarModal();
            await this.cargarGastos();
            
            // Refrescar dashboard si está visible
            if (window.dashboardController) {
                window.dashboardController.refrescar();
            }
        } else {
            UIManager.showError(result.error || 'Error al registrar el gasto');
        }
    }

    // ==========================================
    // 🗑️ ELIMINAR GASTO
    // ==========================================
    async eliminar(id) {
        if (!confirm('¿Seguro que quieres eliminar este gasto?')) return;

        const result = await ApiClient.delete(`/expenses/${id}`);

        if (result.success) {
            UIManager.showSuccess('Gasto eliminado');
            await this.cargarGastos();
            
            // Refrescar dashboard
            if (window.dashboardController) {
                window.dashboardController.refrescar();
            }
        } else {
            UIManager.showError(result.error || 'Error al eliminar el gasto');
        }
    }

    // ==========================================
    // 🔧 UTILIDADES
    // ==========================================
    agruparPorFecha(gastos) {
        const grupos = {};
        
        gastos.forEach(gasto => {
            const fecha = new Date(gasto.fechaGasto).toISOString().split('T')[0];
            if (!grupos[fecha]) {
                grupos[fecha] = [];
            }
            grupos[fecha].push(gasto);
        });

        // Ordenar por fecha descendente
        const ordenados = {};
        Object.keys(grupos)
            .sort((a, b) => new Date(b) - new Date(a))
            .forEach(fecha => {
                ordenados[fecha] = grupos[fecha];
            });

        return ordenados;
    }

    calcularTotalDia(gastos) {
        return gastos.reduce((sum, g) => sum + parseFloat(g.monto), 0);
    }

    formatearFechaGrupo(fecha) {
        const d = new Date(fecha + 'T00:00:00');
        const hoy = new Date();
        const ayer = new Date(hoy);
        ayer.setDate(ayer.getDate() - 1);

        if (d.toDateString() === hoy.toDateString()) return 'Hoy';
        if (d.toDateString() === ayer.toDateString()) return 'Ayer';

        return d.toLocaleDateString('es-ES', { 
            weekday: 'long', 
            day: 'numeric', 
            month: 'long' 
        });
    }

    formatearHora(fechaISO) {
        const d = new Date(fechaISO);
        return d.toLocaleTimeString('es-ES', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    }

    formatearFechaParaInput(fecha) {
        const year = fecha.getFullYear();
        const month = String(fecha.getMonth() + 1).padStart(2, '0');
        const day = String(fecha.getDate()).padStart(2, '0');
        const hours = String(fecha.getHours()).padStart(2, '0');
        const minutes = String(fecha.getMinutes()).padStart(2, '0');
        
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    }

    cerrarModal() {
        const modal = document.getElementById('modal-gasto');
        if (modal) modal.remove();
    }

    configurarEventos() {
        const btnAdd = document.getElementById('btn-add-transaction');
        if (btnAdd) {
            btnAdd.onclick = () => this.mostrarFormulario();
        }
    }
}

// ==========================================
// 🌐 EXPORTAR
// ==========================================
window.GastosController = GastosController;
window.gastosController = new GastosController();

console.log('✅ GastosController cargado');