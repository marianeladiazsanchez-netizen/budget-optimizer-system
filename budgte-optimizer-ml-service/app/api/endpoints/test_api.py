import requests
import json

BASE_URL = "http://localhost:8000"

def test_health():
    """Test el health check"""
    print("\n=== TEST: Health Check ===")
    response = requests.get(f"{BASE_URL}/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    return response.status_code == 200

def test_root():
    """Test el endpoint raíz"""
    print("\n=== TEST: Root Endpoint ===")
    response = requests.get(BASE_URL)
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    return response.status_code == 200

def test_analizar_presupuesto():
    """Test análisis de presupuesto"""
    print("\n=== TEST: Analizar Presupuesto ===")
    
    payload = {
        "nombre": "Juan",
        "prompt": "Gano 3 millones de pesos mensuales. Gasto 1.5 en vivienda, 600k en comida, 300k en transporte, 200k en entretenimiento",
        "usuarioId": 1,
        "presupuestoId": 1
    }
    
    print(f"Enviando: {json.dumps(payload, indent=2)}")
    
    response = requests.post(
        f"{BASE_URL}/api/ml/analizar",
        json=payload
    )
    
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200

def test_predecir_gastos():
    """Test predicción de gastos"""
    print("\n=== TEST: Predecir Gastos ===")
    
    payload = {
        "usuarioId": 1,
        "gastosHistoricos": [
            {"categoria": "Vivienda", "monto": 1500000, "mes": "Enero"},
            {"categoria": "Vivienda", "monto": 1500000, "mes": "Febrero"},
            {"categoria": "Comida", "monto": 600000, "mes": "Enero"},
            {"categoria": "Comida", "monto": 650000, "mes": "Febrero"},
            {"categoria": "Transporte", "monto": 300000, "mes": "Enero"},
            {"categoria": "Transporte", "monto": 320000, "mes": "Febrero"},
        ],
        "mesesAdelante": 3
    }
    
    print(f"Enviando: {json.dumps(payload, indent=2)}")
    
    response = requests.post(
        f"{BASE_URL}/api/ml/predict",
        json=payload
    )
    
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200

def test_optimizar_presupuesto():
    """Test optimización de presupuesto"""
    print("\n=== TEST: Optimizar Presupuesto ===")
    
    payload = {
        "montoTotal": 3000000,
        "categorias": ["Vivienda", "Comida", "Transporte", "Entretenimiento", "Ahorro"],
        "prioridades": [5, 4, 3, 1, 2]
    }
    
    print(f"Enviando: {json.dumps(payload, indent=2)}")
    
    response = requests.post(
        f"{BASE_URL}/api/ml/optimize",
        json=payload
    )
    
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200

def test_detectar_anomalias():
    """Test detección de anomalías"""
    print("\n=== TEST: Detectar Anomalías ===")
    
    payload = {
        "usuarioId": 1,
        "gastos": [
            {"categoria": "Comida", "monto": 600000, "mes": "Enero"},
            {"categoria": "Comida", "monto": 650000, "mes": "Febrero"},
            {"categoria": "Comida", "monto": 620000, "mes": "Marzo"},
            {"categoria": "Comida", "monto": 2500000, "mes": "Abril"},  # ANOMALIA
            {"categoria": "Comida", "monto": 610000, "mes": "Mayo"},
        ],
        "ventanaTiempo": 30
    }
    
    print(f"Enviando: {json.dumps(payload, indent=2)}")
    
    response = requests.post(
        f"{BASE_URL}/api/ml/anomalias",
        json=payload
    )
    
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200

def run_all_tests():
    """Ejecuta todos los tests"""
    print("=" * 60)
    print("INICIANDO TESTS DE LA API")
    print("=" * 60)
    
    results = {
        "Health Check": test_health(),
        "Root Endpoint": test_root(),
        "Predecir Gastos": test_predecir_gastos(),
        "Optimizar Presupuesto": test_optimizar_presupuesto(),
        "Detectar Anomalías": test_detectar_anomalias(),
    }
    
    # Este test se ejecuta al final porque consume IA
    print("\n--- Ejecutando test de IA (puede tardar...) ---")
    results["Analizar Presupuesto"] = test_analizar_presupuesto()
    
    print("\n" + "=" * 60)
    print("RESUMEN DE TESTS")
    print("=" * 60)
    
    for test_name, result in results.items():
        status = "PASSED" if result else "FAILED"
        print(f"{test_name}: {status}")
    
    total = len(results)
    passed = sum(1 for r in results.values() if r)
    print(f"\nTotal: {passed}/{total} tests pasaron")

if __name__ == "__main__":
    try:
        run_all_tests()
    except requests.exceptions.ConnectionError:
        print("ERROR: No se pudo conectar a la API.")
        print("Asegúrate de que el servidor está corriendo en http://localhost:8000")
        print("\nPara iniciar el servidor, ejecuta:")
        print("python -m uvicorn main:app --reload")
    except Exception as e:
        print(f"ERROR: {e}")