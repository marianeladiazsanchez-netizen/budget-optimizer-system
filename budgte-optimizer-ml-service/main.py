import sys
import os

# Agregar la ruta actual al path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app import create_app
from app.api.endpoints import endpoints
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = create_app()

# Incluir rutas
app.include_router(endpoints.router, prefix="/api/ml", tags=["ml"])

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
        "service": "Budget Optimizer ML Service"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)