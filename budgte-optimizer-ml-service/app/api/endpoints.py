from fastapi import APIRouter
from app.api.budget_controller import router as budget_router

router = APIRouter()
router.include_router(budget_router)
