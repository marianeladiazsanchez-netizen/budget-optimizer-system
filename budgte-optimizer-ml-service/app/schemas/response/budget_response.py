from pydantic import BaseModel
from typing import List, Optional

class BudgetResponse(BaseModel):
    summary: str
    recommended_budget: Optional[float] = None
    risk_level: Optional[str] = None
    detected_anomalies: Optional[List[str]] = None
    details: Optional[dict] = None
