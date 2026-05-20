from fastapi import APIRouter
from app.schemas.request.budget_request import BudgetRequest
from app.schemas.response.budget_response import BudgetResponse
from app.services.budget_service import BudgetService

router = APIRouter()
service = BudgetService()

@router.post("/analizar", response_model=BudgetResponse)
def analyze_budget(request: BudgetRequest):
    return service.analyze(request)

@router.post("/predict", response_model=BudgetResponse)
def predict_budget(request: BudgetRequest):
    return service.predict(request)

@router.post("/optimize", response_model=BudgetResponse)
def optimize_budget(request: BudgetRequest):
    return service.optimize(request)

@router.post("/anomalias", response_model=BudgetResponse)
def detect_anomalies(request: BudgetRequest):
    return service.detect_anomalies(request)
