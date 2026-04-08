import os
from dotenv import load_dotenv
from pathlib import Path

# Cargar .env desde budgte-optimizer-ml-service/
env_path = Path(__file__).parent.parent.parent.parent / '.env'
print(f"DEBUG: Buscando .env en: {env_path}")
print(f"DEBUG: Existe: {env_path.exists()}")
print(f"DEBUG: Ruta absoluta: {env_path.absolute()}")

load_dotenv(dotenv_path=env_path, verbose=True)

# Ver qué se cargó
print(f"DEBUG: GOOGLE_AI_API_KEY={os.getenv('GOOGLE_AI_API_KEY')}")

class Config:
    ML_SERVICE_PORT = int(os.getenv('ML_SERVICE_PORT', '8000'))
    ML_SERVICE_HOST = os.getenv('ML_SERVICE_HOST', '0.0.0.0')
    GOOGLE_AI_API_KEY = os.getenv('GOOGLE_AI_API_KEY')  # Nombre correcto del .env
    REDIS_HOST = os.getenv('REDIS_HOST', 'redis')
    REDIS_PORT = int(os.getenv('REDIS_PORT', '6379'))
    REDIS_PASSWORD = os.getenv('REDIS_PASSWORD', 'development_redis_pass')
    REDIS_TIMEOUT = int(os.getenv('REDIS_TIMEOUT', '5'))
    REDIS_CACHE_EXPIRATION = int(os.getenv('REDIS_CACHE_EXPIRATION', '3600'))
    LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
    
    @classmethod
    def validate(cls):
        if not cls.GOOGLE_AI_API_KEY:
            raise ValueError("GOOGLE_AI_API_KEY no configurada en .env")
        return True

Config.validate()