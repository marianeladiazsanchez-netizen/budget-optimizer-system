from app.providers.gemini_provider import GeminiProvider
from app.schemas.request.budget_request import BudgetRequest
from app.schemas.response.budget_response import BudgetResponse

class BudgetService:
    def __init__(self):
        self.provider = GeminiProvider()

    def analyze(self, request: BudgetRequest) -> BudgetResponse:
        prompt = self._build_analysis_prompt(request)
        result = self.provider.call_model(prompt)
        return BudgetResponse(
            summary=result.get("summary", "Análisis completado"),
            recommended_budget=result.get("recommended_budget", request.current_budget),
            risk_level=result.get("risk_level", "moderado"),
            details=result.get("details", {}),
        )

    def predict(self, request: BudgetRequest) -> BudgetResponse:
        prompt = self._build_prediction_prompt(request)
        result = self.provider.call_model(prompt)
        return BudgetResponse(
            summary=result.get("summary", "Predicción de presupuesto"),
            recommended_budget=result.get("predicted_budget", request.current_budget),
            risk_level=result.get("risk_level", "moderado"),
            details=result.get("details", {}),
        )

    def optimize(self, request: BudgetRequest) -> BudgetResponse:
        prompt = self._build_optimization_prompt(request)
        result = self.provider.call_model(prompt)
        return BudgetResponse(
            summary=result.get("summary", "Optimización de presupuesto"),
            recommended_budget=result.get("optimized_budget", request.current_budget),
            risk_level=result.get("risk_level", "bajo"),
            details=result.get("details", {}),
        )

    def detect_anomalies(self, request: BudgetRequest) -> BudgetResponse:
        prompt = self._build_anomaly_prompt(request)
        result = self.provider.call_model(prompt)
        return BudgetResponse(
            summary=result.get("summary", "Detección de anomalías"),
            detected_anomalies=result.get("anomalies", []),
            details=result.get("details", {}),
        )

    def _build_analysis_prompt(self, request: BudgetRequest) -> str:
        return f"Analiza este presupuesto para el periodo {request.period} y sugiere mejoras."

    def _build_prediction_prompt(self, request: BudgetRequest) -> str:
        return f"Predice el presupuesto necesario para el periodo {request.period}."

    def _build_optimization_prompt(self, request: BudgetRequest) -> str:
        return f"Optimiza los gastos actuales y sugiere un presupuesto mejorado para {request.period}."

    def _build_anomaly_prompt(self, request: BudgetRequest) -> str:
        return f"Encuentra anomalías en los gastos del periodo {request.period}."
