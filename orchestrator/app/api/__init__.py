from fastapi import APIRouter

api_router = APIRouter()

# Import and include all endpoint routers here
from .endpoints import parse, decompose, size, estimate, plan  # noqa

api_router.include_router(parse.router, prefix="/parse", tags=["parsing"])
api_router.include_router(decompose.router, prefix="/decompose", tags=["decomposition"])
api_router.include_router(size.router, prefix="/size", tags=["sizing"])
api_router.include_router(estimate.router, prefix="/estimate", tags=["estimation"])
api_router.include_router(plan.router, prefix="/plan", tags=["planning"])
