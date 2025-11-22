from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional, Dict
import google.generativeai as genai
from dotenv import load_dotenv
import redis
import json
import logging
import os

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

load_dotenv()

app = FastAPI(
    title="Budget Optimizer ML Service",
    description="Servicio de IA para optimización de presupuestos",
    version="2.0.0"
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configurar Redis
try:
    redis_client = redis.Redis(
        host=os.getenv('REDIS_HOST', 'localhost'),
        port=int(os.getenv('REDIS_PORT', 6379)),
        password=os.getenv('REDIS_PASSWORD'),
        decode_responses=True,
        socket_connect_timeout=5
    )
    redis_client.ping()
    logger.info("✅ Redis conectado exitosamente")
except Exception as e:
    logger.error(f"❌ Error conectando a Redis: {e}")
    redis_client = None

# Configurar Gemini
try:
    genai.configure(api_key=os.getenv('GOOGLE_IA_API_KEY'))
    model = genai.GenerativeModel('gemini-2.0-flash-exp')
    logger.info("✅ Gemini configurado correctamente")
except Exception as e:
    logger.error(f"❌ Error configurando Gemini: {e}")
    model = None

# ==========================================
# MODELOS PYDANTIC
# ==========================================

class GastoHistorico(BaseModel):
    categoria: str
    monto: float
    mes: str
    fecha: Optional[str] = None

class AnalisisPresupuestoRequest(BaseModel):
    nombre: str
    prompt: str
    usuarioId: Optional[int] = None
    presupuestoId: Optional[int] = None

class PrediccionGastosRequest(BaseModel):
    usuarioId: int
    gastosHistoricos: List[GastoHistorico]
    mesesAdelante: int = Field(default=3, ge=1, le=12)

class OptimizacionPresupuestoRequest(BaseModel):
    montoTotal: float
    categorias: List[str]
    prioridades: Optional[List[int]] = None
    gastosActuales: Optional[Dict[str, float]] = None

class DeteccionAnomaliaRequest(BaseModel):
    usuarioId: int
    gastos: List[GastoHistorico]
    ventanaTiempo: int = Field(default=30, ge=7, le=365)

# ==========================================
# ENDPOINTS
# ==========================================

@app.get("/")
def root():
    return {
        "service": "Budget Optimizer ML Service",
        "version": "2.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "analizar": "/api/ml/analizar",
            "predecir": "/api/ml/predict",
            "optimizar": "/api/ml/optimize",
            "anomalias": "/api/ml/anomalias"
        }
    }

@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "gemini": "connected" if model else "disconnected",
        "redis": "connected" if redis_client else "disconnected"
    }

@app.post("/api/ml/analizar")
async def analizar_presupuesto(request: AnalisisPresupuestoRequest):
    """
    Analiza un presupuesto con IA y genera recomendaciones
    """
    try:
        if not model:
            raise HTTPException(
                status_code=503, 
                detail="Servicio de IA no disponible"
            )
        
        logger.info(f"📊 Analizando presupuesto para: {request.nombre}")
        
        # Verificar caché
        cache_key = f"analisis:{request.nombre}:{hash(request.prompt)}"
        
        if redis_client and redis_client.exists(cache_key):
            logger.info(f"💾 Usando datos de caché para {request.nombre}")
            cached_data = redis_client.get(cache_key)
            return json.loads(cached_data)
        
        # Prompt mejorado para Gemini
        prompt_final = f"""
Eres un analista financiero experto especializado en presupuestos personales.

CONTEXTO DEL USUARIO:
Usuario: {request.nombre}
Información: {request.prompt}

TAREA:
Analiza la situación financiera del usuario y proporciona:
1. Un análisis detallado de su presupuesto actual
2. Recomendaciones específicas y accionables (mínimo 3)
3. Estimación de ahorro potencial mensual/anual

FORMATO DE RESPUESTA:
Debes responder ÚNICAMENTE con un objeto JSON válido (sin markdown, sin backticks) con esta estructura:
{{
  "usuario": "{request.nombre}",
  "analisis": "análisis detallado aquí...",
  "recomendaciones": [
    "recomendación específica 1",
    "recomendación específica 2",
    "recomendación específica 3"
  ],
  "ahorroPotencial": "estimación de ahorro (ejemplo: '$200-300 USD mensuales')",
  "confianza": 0.85
}}

IMPORTANTE: 
- Sé específico y práctico
- Considera el contexto local (Cartagena, Colombia)
- Las recomendaciones deben ser accionables
- Responde SOLO con el JSON, sin texto adicional
"""
        
        # Generar respuesta
        response = model.generate_content(prompt_final)
        response_text = response.text.strip()
        
        # Limpiar respuesta
        if response_text.startswith('```'):
            response_text = response_text.split('```')[1]
            if response_text.startswith('json'):
                response_text = response_text[4:]
            response_text = response_text.strip()
        
        # Parsear JSON
        resultado = json.loads(response_text)
        
        # Guardar en caché
        if redis_client:
            try:
                redis_client.setex(
                    cache_key, 
                    3600,  # 1 hora
                    json.dumps(resultado)
                )
                logger.info(f"💾 Resultado guardado en caché")
            except Exception as e:
                logger.warning(f"⚠️ Error guardando en caché: {e}")
        
        logger.info(f"✅ Análisis completado para {request.nombre}")
        return resultado
        
    except json.JSONDecodeError as e:
        logger.error(f"❌ Error parseando JSON de Gemini: {e}")
        raise HTTPException(
            status_code=500,
            detail="Error procesando respuesta de IA"
        )
    except Exception as e:
        logger.error(f"❌ Error en análisis: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

@app.post("/api/ml/predict")
async def predecir_gastos(request: PrediccionGastosRequest):
    """
    Predice gastos futuros basándose en datos históricos
    """
    try:
        logger.info(f"🔮 Prediciendo gastos para usuario {request.usuarioId}")
        
        # Agrupar gastos por categoría
        gastos_por_categoria = {}
        for gasto in request.gastosHistoricos:
            if gasto.categoria not in gastos_por_categoria:
                gastos_por_categoria[gasto.categoria] = []
            gastos_por_categoria[gasto.categoria].append(gasto.monto)
        
        # Calcular promedios y generar predicciones
        predicciones = []
        for i in range(request.mesesAdelante):
            predicciones_mes = {}
            total_mes = 0.0
            
            for categoria, montos in gastos_por_categoria.items():
                # Predicción simple: promedio + tendencia
                promedio = sum(montos) / len(montos)
                tendencia = promedio * 0.05 * i  # 5% de incremento por mes
                prediccion = round(promedio + tendencia, 2)
                
                predicciones_mes[categoria] = prediccion
                total_mes += prediccion
            
            predicciones.append({
                "mes": f"Mes +{i+1}",
                "prediccionesPorCategoria": predicciones_mes,
                "totalPredicho": round(total_mes, 2)
            })
        
        logger.info(f"✅ Predicción completada: {len(predicciones)} meses")
        
        return {
            "predicciones": predicciones,
            "confianza": 0.75,
            "modelo": "promedio_tendencial"
        }
        
    except Exception as e:
        logger.error(f"❌ Error en predicción: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/ml/optimize")
async def optimizar_presupuesto(request: OptimizacionPresupuestoRequest):
    """
    Optimiza la distribución del presupuesto entre categorías
    """
    try:
        logger.info(f"⚙️ Optimizando presupuesto de ${request.montoTotal}")
        
        num_categorias = len(request.categorias)
        
        if num_categorias == 0:
            raise HTTPException(
                status_code=400, 
                detail="No se proporcionaron categorías"
            )
        
        # Distribución basada en prioridades o uniforme
        if request.prioridades and len(request.prioridades) == num_categorias:
            total_prioridad = sum(request.prioridades)
            distribucion = {}
            
            for cat, prioridad in zip(request.categorias, request.prioridades):
                monto = (prioridad / total_prioridad) * request.montoTotal
                distribucion[cat] = round(monto, 2)
        else:
            # Distribución uniforme
            monto_por_categoria = request.montoTotal / num_categorias
            distribucion = {
                cat: round(monto_por_categoria, 2) 
                for cat in request.categorias
            }
        
        # Calcular ahorro potencial
        ahorro_potencial = request.montoTotal * 0.15
        
        recomendaciones = [
            "Prioriza categorías esenciales como alimentación y vivienda",
            "Reduce gastos en categorías no esenciales en un 10-20%",
            "Establece un fondo de emergencia del 10% del presupuesto"
        ]
        
        logger.info(f"✅ Optimización completada. Ahorro: ${ahorro_potencial:.2f}")
        
        return {
            "distribucionOptimizada": distribucion,
            "ahorroPotencial": round(ahorro_potencial, 2),
            "recomendaciones": recomendaciones,
            "justificaciones": {
                cat: f"Asignado ${monto:.2f} basado en análisis"
                for cat, monto in distribucion.items()
            }
        }
        
    except Exception as e:
        logger.error(f"❌ Error en optimización: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/ml/anomalias")
async def detectar_anomalias(request: DeteccionAnomaliaRequest):
    """
    Detecta anomalías en patrones de gasto
    """
    try:
        logger.info(f"🔍 Detectando anomalías para usuario {request.usuarioId}")
        
        # Agrupar por categoría
        gastos_categoria = {}
        for gasto in request.gastos:
            if gasto.categoria not in gastos_categoria:
                gastos_categoria[gasto.categoria] = []
            gastos_categoria[gasto.categoria].append(gasto)
        
        anomalias = []
        
        # Detectar anomalías por categoría
        for categoria, gastos in gastos_categoria.items():
            montos = [g.monto for g in gastos]
            promedio = sum(montos) / len(montos)
            desviacion_std = (sum((m - promedio) ** 2 for m in montos) / len(montos)) ** 0.5
            
            # Detectar gastos anómalos (> 2 desviaciones estándar)
            for gasto in gastos:
                desviacion = abs(gasto.monto - promedio) / desviacion_std if desviacion_std > 0 else 0
                
                if desviacion > 2.0:
                    anomalias.append({
                        "fecha": gasto.fecha or gasto.mes,
                        "categoria": categoria,
                        "montoEsperado": round(promedio, 2),
                        "montoReal": gasto.monto,
                        "desviacion": round(desviacion, 2),
                        "descripcion": f"Gasto {'alto' if gasto.monto > promedio else 'bajo'} inusual detectado"
                    })
        
        severidad = "ALTA" if len(anomalias) > 5 else "MEDIA" if len(anomalias) > 2 else "BAJA"
        
        logger.info(f"✅ Detección completada: {len(anomalias)} anomalías")
        
        return {
            "anomalias": anomalias,
            "totalAnomalias": len(anomalias),
            "severidad": severidad
        }
        
    except Exception as e:
        logger.error(f"❌ Error en detección de anomalías: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app, 
        host="0.0.0.0", 
        port=8000,
        log_level="info"
    )