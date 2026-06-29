from fastapi import APIRouter
from typing import List
from models.schemas import SensorData

router = APIRouter()

@router.post("/ingest")
async def ingest_sensor_data(data: List[SensorData]):
    """接收传感器数据"""
    return {
        "message": f"成功接收 {len(data)} 条数据",
        "count": len(data)
    }

@router.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy"}