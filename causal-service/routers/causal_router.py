from fastapi import APIRouter
router = APIRouter()

@router.get("/test")
async def test():
    return {"message": "causal router works"}