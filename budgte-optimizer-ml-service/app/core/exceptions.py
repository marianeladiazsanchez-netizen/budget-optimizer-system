class BudgetServiceError(Exception):
    """Base exception for budget ML service errors."""
    pass

class InvalidBudgetRequestError(BudgetServiceError):
    pass
