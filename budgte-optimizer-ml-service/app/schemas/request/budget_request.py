from pydantic import BaseModel
from typing import List, Optional

class ExpenseItem(BaseModel):
    category: str
    amount: float
    description: Optional[str] = None

class BudgetRequest(BaseModel):
    user_id: Optional[str] = None
    period: str
    current_budget: float
    expenses: List[ExpenseItem]
    goals: Optional[List[str]] = None
