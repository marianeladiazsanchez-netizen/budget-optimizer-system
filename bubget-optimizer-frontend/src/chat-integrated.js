// ============================================
// 💬 CHAT IA - CONEXIÓN DIRECTA A ML
// ============================================

console.log('🚀 Chat iniciando...');

// Esperar a que el DOM esté completamente cargado
function iniciarChat() {
    console.log('🎯 Buscando elementos del chat...');
    
    // Obtener elementos
    const chatMessages = document.getElementById('chat-messages');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const typingIndicator = document.getElementById('typing-indicator');
    const statusBadge = document.getElementById('status-badge');
    const quickActions = document.querySelectorAll('.quick-action-card');

    // Validar que los elementos existan
    if (!chatMessages || !messageInput || !sendButton) {
        console.error('❌ Elementos no encontrados');
        return;
    }

    console.log('✅ Todos los elementos encontrados');

    const ML_URL = 'http://localhost:8000';

    // ==========================================
    // 🎨 FUNCIONES AUXILIARES
    // ==========================================

    function agregarMensajeUsuario(texto) {
        const div = document.createElement('div');
        div.className = 'message user';
        div.innerHTML = `
            <div class="message-content">
                <p>${escapeHtml(texto)}</p>
            </div>
        `;
        chatMessages.appendChild(div);
        scroll();
    }

    function agregarMensajeBot(html) {
        const div = document.createElement('div');
        div.className = 'message bot';
        div.innerHTML = `<div class="message-content">${html}</div>`;
        chatMessages.appendChild(div);
        scroll();
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function scroll() {
        setTimeout(() => {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }, 100);
    }

    function mostrarEscritura() {
        typingIndicator.classList.add('active');
        scroll();
    }

    function ocultarEscritura() {
        typingIndicator.classList.remove('active');
    }

    // ==========================================
    // 🔌 CONECTAR A ML
    // ==========================================

    async function llamarML(endpoint, datos) {
        console.log(`📡 Llamando a ${endpoint}...`, datos);
        
        try {
            const response = await fetch(`${ML_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(datos),
                mode: 'cors'
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const data = await response.json();
            console.log('✅ Respuesta ML:', data);
            return data;
        } catch (error) {
            console.error('❌ Error ML:', error);
            throw error;
        }
    }

    // ==========================================
    // 💬 ENVIAR MENSAJE
    // ==========================================
    async function enviarMensaje() {
        const mensaje = messageInput.value.trim();

        if (!mensaje) {
            console.warn('Mensaje vacío');
            return;
        }

        console.log('📤 Mensaje:', mensaje);

        // Agregar mensaje del usuario
        agregarMensajeUsuario(mensaje);
        messageInput.value = '';
        sendButton.disabled = true;

        // Mostrar indicador de escritura
        mostrarEscritura();

        try {
            const msgLower = mensaje.toLowerCase();
            let respuesta = null;

            if (msgLower.includes('analizar')) {
                respuesta = await llamarML('/analizar', {
                    usuario: 'usuario_1',
                    mensaje: mensaje
                });
                ocultarEscritura();
                agregarMensajeBot(`<strong>📊 Análisis:</strong><br>${respuesta.respuesta || respuesta.analisis || 'Presupuesto analizado'}`);

            } else if (msgLower.includes('predecir')) {
                respuesta = await llamarML('/predecir', {
                    usuario: 'usuario_1',
                    mensaje: mensaje
                });
                ocultarEscritura();
                agregarMensajeBot(`<strong>🔮 Predicción:</strong><br>${respuesta.respuesta || respuesta.prediccion || 'Predicción generada'}`);

            } else if (msgLower.includes('optimizar')) {
                respuesta = await llamarML('/optimizar', {
                    usuario: 'usuario_1',
                    mensaje: mensaje
                });
                ocultarEscritura();
                agregarMensajeBot(`<strong>⚙️ Optimización:</strong><br>${respuesta.respuesta || respuesta.optimizacion || 'Optimización completada'}`);

            } else if (msgLower.includes('anomalía') || msgLower.includes('anomalías')) {
                respuesta = await llamarML('/anomalias', {
                    usuario: 'usuario_1',
                    mensaje: mensaje
                });
                ocultarEscritura();
                agregarMensajeBot(`<strong>🔍 Anomalías:</strong><br>${respuesta.respuesta || respuesta.anomalias || 'Análisis de anomalías completado'}`);

            } else {
                respuesta = await llamarML('/chat', {
                    usuario: 'usuario_1',
                    mensaje: mensaje
                });
                ocultarEscritura();
                agregarMensajeBot(`<strong>💬 Respuesta:</strong><br>${respuesta.respuesta || respuesta.mensaje || 'Respuesta procesada'}`);
            }

        } catch (error) {
            console.error('Error:', error.message);
            ocultarEscritura();
            
            // Fallback si ML no responde
            agregarMensajeBot(
                '<strong>⚠️ Servicio ML no disponible</strong><br>' +
                `<p>Error: ${error.message}</p>` +
                '<p>Asegúrate de que el servidor ML esté corriendo en http://localhost:8000</p>'
            );
        }

        sendButton.disabled = false;
    }

    // ==========================================
    // ⚡ ACCIONES RÁPIDAS
    // ==========================================
    quickActions.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const action = btn.dataset.action;
            console.log('⚡ Acción:', action);

            switch (action) {
                case 'analizar':
                    messageInput.value = 'Analiza mi presupuesto';
                    break;
                case 'predecir':
                    messageInput.value = 'Predice mis gastos';
                    break;
                case 'optimizar':
                    messageInput.value = 'Optimiza mi presupuesto';
                    break;
                case 'anomalias':
                    messageInput.value = 'Detecta anomalías';
                    break;
            }

            enviarMensaje();
        });
    });

    // ==========================================
    // 🔗 EVENT LISTENERS
    // ==========================================

    // Botón enviar
    sendButton.addEventListener('click', (e) => {
        e.preventDefault();
        console.log('📤 Click en enviar');
        enviarMensaje();
    });

    // Enter en input
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            console.log('⌨️ Enter presionado');
            enviarMensaje();
        }
    });

    // Estado online - verificar ML
    async function verificarML() {
        try {
            const response = await fetch(`${ML_URL}/health`, { mode: 'cors' });
            if (response.ok) {
                console.log('✅ Servicio ML Online');
                if (statusBadge) {
                    statusBadge.className = 'status-badge online';
                    statusBadge.textContent = '🟢 Online';
                }
            }
        } catch (error) {
            console.warn('⚠️ Servicio ML Offline');
            if (statusBadge) {
                statusBadge.className = 'status-badge offline';
                statusBadge.textContent = '🔴 Offline';
            }
        }
    }

    verificarML();

    console.log('✅ Chat IA inicializado correctamente');
    console.log('🔗 Conectado a:', ML_URL);
}

// ==========================================
// 🚀 INICIAR CUANDO EL DOM ESTÉ LISTO
// ==========================================

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', iniciarChat);
} else {
    iniciarChat();
}

console.log('✅ Chat Script Cargado');