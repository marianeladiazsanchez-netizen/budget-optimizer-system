# ============================================
# 🚀 MAIN.PY - ML SERVICE - VERSIÓN CORRECTA
# ============================================

import sys
import os
from pathlib import Path

# Agregar la carpeta app al path
sys.path.insert(0, str(Path(__file__).parent))

# ✅ IMPORTAR DIRECTAMENTE DE ENDPOINTS
from app.api.endpoints.endpoints import app

if __name__ == "__main__":
    import uvicorn
    
    print("=" * 60)
    print("🚀 Iniciando Budget Optimizer ML Service")
    print("=" * 60)
    print("📡 Servidor: http://0.0.0.0:8000")
    print("📚 Docs: http://localhost:8000/docs")
    print("🔍 Health: http://localhost:8000/health")
    print("=" * 60)
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        log_level="info",
        reload=True
    )