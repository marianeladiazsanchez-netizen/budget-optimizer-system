 // ==========================================
        // CONFIGURACIÓN
        // ==========================================
        const API_URL = 'http://localhost:8080/api';
        const ML_URL = 'http://localhost:8000';

        // ==========================================
        // ELEMENTOS DEL DOM
        // ==========================================
        const chatMessages = document.getElementById('chat-messages');
        const messageInput = document.getElementById('message-input');
        const sendButton = document.getElementById('send-button');
        const typingIndicator = document.getElementById('typing-indicator');
        const statusBadge = document.getElementById('status-badge');
        const quickActions = document.querySelectorAll('.quick-action');

        // ==========================================
        // ESTADO
        // ==========================================
        let currentUser = null;
        let currentPresupuesto = null;

        // ==========================================
        // INICIALIZACIÓN
        // ==========================================
        async function init() {
            await checkMLServiceHealth();
            await loadUserData();
            setupEventListeners();
        }

        // ==========================================
        // VERIFICAR ESTADO DEL SERVICIO ML
        // ==========================================
        async function checkMLServiceHealth() {
            try {
                const response = await fetch(`${ML_URL}/health`);
                const data = await response.json();
                
                if (data.status === 'ok' && data.gemini === 'connected') {
                    updateStatus(true);
                } else {
                    updateStatus(false);
                    addBotMessage('⚠️ El servicio de IA está conectado pero Gemini no está disponible.');
                }
            } catch (error) {
                console.error('Error conectando al servicio ML:', error);
                updateStatus(false);
                addBotMessage('❌ No se pudo conectar con el servicio de IA. Verifica que el servidor ML esté corriendo en http://localhost:8000');
            }
        }

        function updateStatus(isOnline) {
            if (isOnline) {
                statusBadge.textContent = 'Online';
                statusBadge.className = 'status-badge online';
            } else {
                statusBadge.textContent = 'Offline';
                statusBadge.className = 'status-badge offline';
            }
        }

        // ==========================================
        // CARGAR DATOS DEL USUARIO
        // ==========================================
        async function loadUserData() {
            const userId = localStorage.getItem('userId');
            
            if (!userId) {
                addBotMessage('⚠️ No has iniciado sesión. Algunas funciones estarán limitadas.');
                return;
            }

            try {
                // Obtener usuario
                const userResponse = await fetch(`${API_URL}/usuarios/${userId}`);
                currentUser = await userResponse.json();

                // Obtener presupuesto actual
                try {
                    const presupuestoResponse = await fetch(`${API_URL}/presupuestos/usuario/${userId}/actual`);
                    currentPresupuesto = await presupuestoResponse.json();
                } catch (error) {
                    console.log('No hay presupuesto activo');
                }

            } catch (error) {
                console.error('Error cargando datos:', error);
            }
        }

        // ==========================================
        // EVENT LISTENERS
        // ==========================================
        function setupEventListeners() {
            sendButton.addEventListener('click', handleSendMessage);
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') handleSendMessage();
            });

            quickActions.forEach(btn => {
                btn.addEventListener('click', () => {
                    const action = btn.dataset.action;
                    handleQuickAction(action);
                });
            });
        }

        // ==========================================
        // MANEJAR ENVÍO DE MENSAJE
        // ==========================================
        async function handleSendMessage() {
            const message = messageInput.value.trim();
            if (!message) return;

            // Agregar mensaje del usuario
            addUserMessage(message);
            messageInput.value = '';
            sendButton.disabled = true;

            // Mostrar indicador de escritura
            showTyping();

            try {
                // Determinar tipo de consulta
                if (message.toLowerCase().includes('analizar') || message.toLowerCase().includes('análisis')) {
                    await analizarPresupuesto();
                } else if (message.toLowerCase().includes('predecir') || message.toLowerCase().includes('predicción')) {
                    await predecirGastos();
                } else if (message.toLowerCase().includes('optimizar')) {
                    await optimizarPresupuesto();
                } else if (message.toLowerCase().includes('anomalía') || message.toLowerCase().includes('anomalias')) {
                    await detectarAnomalias();
                } else {
                    // Respuesta genérica usando IA
                    await consultaGeneral(message);
                }
            } catch (error) {
                console.error('Error:', error);
                hideTyping();
                addBotMessage('❌ Ocurrió un error al procesar tu solicitud. Por favor, intenta de nuevo.');
            }

            sendButton.disabled = false;
        }

        // ==========================================
        // ACCIONES RÁPIDAS
        // ==========================================
        async function handleQuickAction(action) {
            switch (action) {
                case 'analizar':
                    addUserMessage('Analiza mi presupuesto actual');
                    await analizarPresupuesto();
                    break;
                case 'predecir':
                    addUserMessage('Predice mis gastos futuros');
                    await predecirGastos();
                    break;
                case 'optimizar':
                    addUserMessage('Optimiza mi distribución de presupuesto');
                    await optimizarPresupuesto();
                    break;
                case 'anomalias':
                    addUserMessage('Detecta anomalías en mis gastos');
                    await detectarAnomalias();
                    break;
            }
        }

        // ==========================================
        // ANALIZAR PRESUPUESTO
        // ==========================================
        async function analizarPresupuesto() {
            if (!currentUser || !currentPresupuesto) {
                hideTyping();
                addBotMessage('⚠️ Necesitas tener un presupuesto activo para realizar el análisis.');
                return;
            }

            showTyping();

            try {
                // Obtener gastos
                const gastosResponse = await fetch(`${API_URL}/expenses/presupuesto/${currentPresupuesto.id}`);
                const gastos = await gastosResponse.json();

                // Construir prompt
                const prompt = `
Usuario: ${currentUser.nombre}
Ciudad: ${currentUser.ciudad}, ${currentUser.pais}
Presupuesto: ${currentPresupuesto.nombre}
Período: ${currentPresupuesto.periodoDisplay}
Monto Total: $${currentPresupuesto.montoTotal}
Total Gastado: $${currentPresupuesto.montoGastado} (${currentPresupuesto.porcentajeGastado?.toFixed(1)}%)
Restante: $${currentPresupuesto.montoRestante}
Total de Gastos: ${gastos.length}
                `;

                // Llamar al servicio ML
                const response = await fetch(`${ML_URL}/api/ml/analizar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        nombre: currentUser.nombre,
                        prompt: prompt,
                        usuarioId: currentUser.id,
                        presupuestoId: currentPresupuesto.id
                    })
                });

                if (!response.ok) throw new Error('Error al analizar presupuesto');

                const analisis = await response.json();
                hideTyping();

                // Mostrar análisis
                const html = `
                    <p><strong>📊 Análisis de tu presupuesto:</strong></p>
                    <div class="analysis-card">
                        <h4>Análisis Detallado:</h4>
                        <p>${analisis.analisis}</p>
                        
                        <h4>Recomendaciones:</h4>
                        <ul>
                            ${analisis.recomendaciones.map(r => `<li>${r}</li>`).join('')}
                        </ul>
                        
                        <h4>💰 Ahorro Potencial:</h4>
                        <p><strong>${analisis.ahorroPotencial}</strong></p>
                        
                        <p style="margin-top: 10px; font-size: 0.85rem; opacity: 0.7;">
                            Confianza: ${(analisis.confianza * 100).toFixed(0)}%
                        </p>
                    </div>
                `;

                addBotMessage(html);

            } catch (error) {
                console.error('Error:', error);
                hideTyping();
                addBotMessage(`❌ Error al analizar: ${error.message}`);
            }
        }

        // ==========================================
        // PREDECIR GASTOS
        // ==========================================
        async function predecirGastos() {
            if (!currentUser) {
                hideTyping();
                addBotMessage('⚠️ Necesitas iniciar sesión para predecir gastos.');
                return;
            }

            showTyping();

            try {
                const gastosResponse = await fetch(`${API_URL}/expenses/usuario/${currentUser.id}`);
                const gastos = await gastosResponse.json();

                if (gastos.length === 0) {
                    hideTyping();
                    addBotMessage('⚠️ No tienes gastos registrados aún. Registra algunos gastos para obtener predicciones.');
                    return;
                }

                // Convertir gastos al formato ML
                const gastosHistoricos = gastos.map(g => ({
                    categoria: g.categoriaNombre,
                    monto: parseFloat(g.monto),
                    mes: new Date(g.fechaGasto).toISOString().substring(0, 7),
                    fecha: g.fechaGasto
                }));

                const response = await fetch(`${ML_URL}/api/ml/predict`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        usuarioId: currentUser.id,
                        gastosHistoricos: gastosHistoricos,
                        mesesAdelante: 3
                    })
                });

                const prediccion = await response.json();
                hideTyping();

                const html = `
                    <p><strong>🔮 Predicción de gastos futuros:</strong></p>
                    <div class="analysis-card">
                        ${prediccion.predicciones.map((p, i) => `
                            <h4>${p.mes}:</h4>
                            <ul>
                                ${Object.entries(p.prediccionesPorCategoria).map(([cat, monto]) => 
                                    `<li>${cat}: $${monto}</li>`
                                ).join('')}
                            </ul>
                            <p><strong>Total estimado: $${p.totalPredicho}</strong></p>
                            ${i < prediccion.predicciones.length - 1 ? '<hr>' : ''}
                        `).join('')}
                        
                        <p style="margin-top: 10px; font-size: 0.85rem; opacity: 0.7;">
                            Modelo: ${prediccion.modelo} | Confianza: ${(prediccion.confianza * 100).toFixed(0)}%
                        </p>
                    </div>
                `;

                addBotMessage(html);

            } catch (error) {
                console.error('Error:', error);
                hideTyping();
                addBotMessage(`❌ Error al predecir: ${error.message}`);
            }
        }

        // ==========================================
        // OPTIMIZAR PRESUPUESTO
        // ==========================================
        async function optimizarPresupuesto() {
            if (!currentPresupuesto) {
                hideTyping();
                addBotMessage('⚠️ Necesitas un presupuesto activo para optimizarlo.');
                return;
            }

            showTyping();

            try {
                const gastosResponse = await fetch(`${API_URL}/expenses/presupuesto/${currentPresupuesto.id}`);
                const gastos = await gastosResponse.json();

                const categorias = [...new Set(gastos.map(g => g.categoriaNombre))];

                const response = await fetch(`${ML_URL}/api/ml/optimize`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        montoTotal: parseFloat(currentPresupuesto.montoTotal),
                        categorias: categorias,
                        prioridades: null,
                        gastosActuales: null
                    })
                });

                const optimizacion = await response.json();
                hideTyping();

                const html = `
                    <p><strong>⚙️ Optimización del presupuesto:</strong></p>
                    <div class="analysis-card">
                        <h4>Distribución Optimizada:</h4>
                        <ul>
                            ${Object.entries(optimizacion.distribucionOptimizada).map(([cat, monto]) => 
                                `<li>${cat}: $${monto}</li>`
                            ).join('')}
                        </ul>
                        
                        <h4>💰 Ahorro Potencial:</h4>
                        <p><strong>$${optimizacion.ahorroPotencial}</strong></p>
                        
                        <h4>Recomendaciones:</h4>
                        <ul>
                            ${optimizacion.recomendaciones.map(r => `<li>${r}</li>`).join('')}
                        </ul>
                    </div>
                `;

                addBotMessage(html);

            } catch (error) {
                console.error('Error:', error);
                hideTyping();
                addBotMessage(`❌ Error al optimizar: ${error.message}`);
            }
        }

        // ==========================================
        // DETECTAR ANOMALÍAS
        // ==========================================
        async function detectarAnomalias() {
            if (!currentUser) {
                hideTyping();
                addBotMessage('⚠️ Necesitas iniciar sesión.');
                return;
            }

            showTyping();

            try {
                const gastosResponse = await fetch(`${API_URL}/expenses/usuario/${currentUser.id}`);
                const gastos = await gastosResponse.json();

                if (gastos.length < 10) {
                    hideTyping();
                    addBotMessage('⚠️ Necesitas al menos 10 gastos registrados para detectar anomalías.');
                    return;
                }

                const gastosHistoricos = gastos.map(g => ({
                    categoria: g.categoriaNombre,
                    monto: parseFloat(g.monto),
                    mes: new Date(g.fechaGasto).toISOString().substring(0, 7),
                    fecha: g.fechaGasto
                }));

                const response = await fetch(`${ML_URL}/api/ml/anomalias`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        usuarioId: currentUser.id,
                        gastos: gastosHistoricos,
                        ventanaTiempo: 30
                    })
                });

                const deteccion = await response.json();
                hideTyping();

                if (deteccion.totalAnomalias === 0) {
                    addBotMessage('✅ ¡Excelente! No se detectaron anomalías en tus gastos. Tus patrones de gasto son consistentes.');
                    return;
                }

                const html = `
                    <p><strong>🔍 Anomalías detectadas:</strong></p>
                    <div class="analysis-card">
                        <p>Se encontraron <strong>${deteccion.totalAnomalias}</strong> anomalías.</p>
                        <p>Severidad: <strong>${deteccion.severidad}</strong></p>
                        
                        <h4>Detalles:</h4>
                        <ul>
                            ${deteccion.anomalias.slice(0, 5).map(a => `
                                <li>
                                    <strong>${a.categoria}</strong>: 
                                    $${a.montoReal} (esperado: $${a.montoEsperado})
                                    <br><small>${a.descripcion}</small>
                                </li>
                            `).join('')}
                        </ul>
                        
                        ${deteccion.totalAnomalias > 5 ? 
                            `<p><small>... y ${deteccion.totalAnomalias - 5} anomalías más</small></p>` : ''}
                    </div>
                `;

                addBotMessage(html);

            } catch (error) {
                console.error('Error:', error);
                hideTyping();
                addBotMessage(`❌ Error al detectar anomalías: ${error.message}`);
            }
        }

        // ==========================================
        // CONSULTA GENERAL
        // ==========================================
        async function consultaGeneral(mensaje) {
            hideTyping();
            addBotMessage('🤔 Para obtener mejores resultados, prueba con una de las opciones disponibles: analizar, predecir, optimizar o detectar anomalías.');
        }

        // ==========================================
        // AGREGAR MENSAJES
        // ==========================================
        function addUserMessage(text) {
            const messageDiv = document.createElement('div');
            messageDiv.className = 'message user';
            messageDiv.innerHTML = `
                <div class="message-content">
                    <p>${text}</p>
                    <div class="message-time">${getCurrentTime()}</div>
                </div>
            `;
            chatMessages.appendChild(messageDiv);
            scrollToBottom();
        }

        function addBotMessage(html) {
            const messageDiv = document.createElement('div');
            messageDiv.className = 'message bot';
            messageDiv.innerHTML = `
                <div class="message-content">
                    ${html}
                    <div class="message-time">${getCurrentTime()}</div>
                </div>
            `;
            chatMessages.appendChild(messageDiv);
            scrollToBottom();
        }

        // ==========================================
        // UTILIDADES
        // ==========================================
        function showTyping() {
            typingIndicator.classList.add('active');
            scrollToBottom();
        }

        function hideTyping() {
            typingIndicator.classList.remove('active');
        }

        function scrollToBottom() {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

        function getCurrentTime() {
            return new Date().toLocaleTimeString('es-ES', { 
                hour: '2-digit', 
                minute: '2-digit' 
            });
        }

        // ==========================================
        // INICIAR
        // ==========================================
        init();