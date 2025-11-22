from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import List, Optional, Dict
import google.generativeai as genai
from app.api.endpoints.configuracion import Config
import redis
import json
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# DEFINIR EL ROUTER AQUI
router = APIRouter()

# Configurar Redis
try:
    redis_client = redis.Redis(
        host=Config.REDIS_HOST,
        port=Config.REDIS_PORT,
        password=Config.REDIS_PASSWORD,
        decode_responses=True,
        socket_connect_timeout=Config.REDIS_TIMEOUT
    )
    redis_client.ping()
    logger.info("Redis conectado exitosamente")
except Exception as e:
    logger.error(f"Error conectando a Redis: {e}")
    redis_client = None

# Configurar Gemini
try:
    genai.configure(api_key=Config.GOOGLE_AI_API_KEY)
    model = genai.GenerativeModel('gemini-2.5-pro')
    logger.info("Gemini configurado correctamente")
except Exception as e:
    logger.error(f"Error configurando Gemini: {e}")
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

@router.post("/analizar")
async def analizar_presupuesto(request: AnalisisPresupuestoRequest):
    try:
        if not model:
            raise HTTPException(
                status_code=503,
                detail="Servicio de IA no disponible"
            )
        
        logger.info(f"Analizando presupuesto para: {request.nombre}")
        
        cache_key = f"analisis:{request.nombre}:{hash(request.prompt)}"
        
        if redis_client and redis_client.exists(cache_key):
            logger.info(f"Usando datos de caché para {request.nombre}")
            cached_data = redis_client.get(cache_key)
            return json.loads(cached_data)
        
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
        
        response = model.generate_content(prompt_final)
        response_text = response.text.strip()
        
        if response_text.startswith('```'):
            response_text = response_text.split('```')[1]
            if response_text.startswith('json'):
                response_text = response_text[4:]
            response_text = response_text.strip()
        
        resultado = json.loads(response_text)
        
        if redis_client:
            try:
                redis_client.setex(
                    cache_key,
                    3600,
                    json.dumps(resultado)
                )
                logger.info("Resultado guardado en caché")
            except Exception as e:
                logger.warning(f"Error guardando en caché: {e}")
        
        logger.info(f"Análisis completado para {request.nombre}")
        return resultado
        
    except json.JSONDecodeError as e:
        logger.error(f"Error parseando JSON de Gemini: {e}")
        raise HTTPException(
            status_code=500,
            detail="Error procesando respuesta de IA"
        )
    except Exception as e:
        logger.error(f"Error en análisis: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

@router.post("/predict")
async def predecir_gastos(request: PrediccionGastosRequest):
    try:
        logger.info(f"Prediciendo gastos para usuario {request.usuarioId}")
        
        gastos_por_categoria = {}
        for gasto in request.gastosHistoricos:
            if gasto.categoria not in gastos_por_categoria:
                gastos_por_categoria[gasto.categoria] = []
            gastos_por_categoria[gasto.categoria].append(gasto.monto)
        
        predicciones = []
        for i in range(request.mesesAdelante):
            predicciones_mes = {}
            total_mes = 0.0
            
            for categoria, montos in gastos_por_categoria.items():
                promedio = sum(montos) / len(montos)
                tendencia = promedio * 0.05 * i
                prediccion = round(promedio + tendencia, 2)
                
                predicciones_mes[categoria] = prediccion
                total_mes += prediccion
            
            predicciones.append({
                "mes": f"Mes +{i+1}",
                "prediccionesPorCategoria": predicciones_mes,
                "totalPredicho": round(total_mes, 2)
            })
        
        logger.info(f"Predicción completada: {len(predicciones)} meses")
        
        return {
            "predicciones": predicciones,
            "confianza": 0.75,
            "modelo": "promedio_tendencial"
        }
        
    except Exception as e:
        logger.error(f"Error en predicción: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/optimize")
async def optimizar_presupuesto(request: OptimizacionPresupuestoRequest):
    try:
        logger.info(f"Optimizando presupuesto de ${request.montoTotal}")
        
        num_categorias = len(request.categorias)
        
        if num_categorias == 0:
            raise HTTPException(
                status_code=400,
                detail="No se proporcionaron categorías"
            )
        
        if request.prioridades and len(request.prioridades) == num_categorias:
            total_prioridad = sum(request.prioridades)
            distribucion = {}
            
            for cat, prioridad in zip(request.categorias, request.prioridades):
                monto = (prioridad / total_prioridad) * request.montoTotal
                distribucion[cat] = round(monto, 2)
        else:
            monto_por_categoria = request.montoTotal / num_categorias
            distribucion = {
                cat: round(monto_por_categoria, 2)
                for cat in request.categorias
            }
        
        ahorro_potencial = request.montoTotal * 0.15
        
        recomendaciones = [
            "Prioriza categorías esenciales como alimentación y vivienda",
            "Reduce gastos en categorías no esenciales en un 10-20%",
            "Establece un fondo de emergencia del 10% del presupuesto"
        ]
        
        logger.info(f"Optimización completada. Ahorro: ${ahorro_potencial:.2f}")
        
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
        logger.error(f"Error en optimización: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/anomalias")
async def detectar_anomalias(request: DeteccionAnomaliaRequest):
    try:
        logger.info(f"Detectando anomalías para usuario {request.usuarioId}")
        
        gastos_categoria = {}
        for gasto in request.gastos:
            if gasto.categoria not in gastos_categoria:
                gastos_categoria[gasto.categoria] = []
            gastos_categoria[gasto.categoria].append(gasto)
        
        anomalias = []
        
        for categoria, gastos in gastos_categoria.items():
            montos = [g.monto for g in gastos]
            promedio = sum(montos) / len(montos)
            desviacion_std = (sum((m - promedio) ** 2 for m in montos) / len(montos)) ** 0.5
            
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
        
        logger.info(f"Detección completada: {len(anomalias)} anomalías")
        
        return {
            "anomalias": anomalias,
            "totalAnomalias": len(anomalias),
            "severidad": severidad
        }
        
    except Exception as e:
        logger.error(f"Error en detección de anomalías: {e}")
        raise HTTPException(status_code=500, detail=str(e))