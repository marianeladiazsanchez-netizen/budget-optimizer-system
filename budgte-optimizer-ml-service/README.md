# Budget Optimizer ML Service

Servicio ligero de FastAPI para análisis, predicción, optimización y detección de anomalías en presupuestos.

## Estado actual
- Proyecto simplificado para estudiantes de semestre 4.
- Se eliminaron dependencias y archivos innecesarios.
- El servicio usa solo lo mínimo que requiere el código actual.

## Dependencias
Instala las dependencias de runtime:

```bash
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
```

## Configuración de entorno
Crea o edita el archivo `.env` en la raíz del servicio.

```env
GEMINI_API_KEY=tu_api_key_aqui
GEMINI_MODEL_NAME=gemini-1.5-flash
APP_ENV=development
```

### Sobre Gemini
El servicio utiliza **Google Gemini 1.5 Flash**, un modelo optimizado para:
- Análisis rápido de presupuestos y gastos
- Predicción de tendencias de gasto
- Detección de anomalías en transacciones
- Optimización de presupuestos personalizados

Este modelo es ideal para este caso de uso porque ofrece:
- **Velocidad**: Respuestas rápidas para análisis en tiempo real
- **Eficiencia**: Menor costo por llamada API
- **Precisión**: Modelo entrenado en análisis financiero

**Requisito:** Debes configurar tu `GEMINI_API_KEY` en el archivo `.env` para que el servicio funcione.

## Ejecutar localmente

```bash
python main.py
```

O bien con Uvicorn:

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

## Docker

Construir la imagen:

```bash
docker build -t budget-optimizer-ml-service .
```

Ejecutar el contenedor:

```bash
docker run --rm -p 8000:8000 budget-optimizer-ml-service
```

## Estructura relevante

- `main.py` - punto de entrada de FastAPI, exporta la app desde `app`.
- `app/` - aplicación principal con API, servicios, proveedores, prompts y utilidades.
- `requirements.txt` - dependencias reducidas al runtime.
- `Dockerfile` - contenedor más ligero sin compilación de paquetes innecesarios.
- `.env` - configuración de entorno para la aplicación.

## Cambios clave

- ✅ **Integración con Google Gemini 1.5 Flash**: El servicio ahora llama a la API de Google Gemini para análisis inteligente de presupuestos.
- ✅ Se eliminó la instalación de `build-essential` en Docker para reducir el peso de la imagen.
- ✅ Se removió `start.sh` porque apuntaba a `app.py`, que no existe en este proyecto.
- ✅ Se agregó configuración de `.env` en el servicio con `GEMINI_API_KEY`.
- Dependencias: `fastapi`, `uvicorn`, `pydantic`, `google-generativeai`
