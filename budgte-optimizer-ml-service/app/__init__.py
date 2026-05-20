from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.endpoints import router


def create_app():
    app = FastAPI(
        title="Budget Optimizer ML Service",
        description="Servicio de IA para optimización de presupuestos",
        version="2.0.0"
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(router, prefix="/api/ml", tags=["ml"])

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

    return app


app = create_app()
