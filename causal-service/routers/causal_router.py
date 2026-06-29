from fastapi import APIRouter, HTTPException
from typing import List
from models.schemas import CausalAnalysisRequest, CausalAnalysisResponse, AnomalyResponse
from services.causal_analyzer import CausalAnalyzer
from services.anomaly_detector import AnomalyDetector
from services.correlation_analyzer import CorrelationAnalyzer
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

router = APIRouter()
causal_analyzer = CausalAnalyzer()
anomaly_detector = AnomalyDetector()
correlation_analyzer = CorrelationAnalyzer()

@router.post("/analyze", response_model=CausalAnalysisResponse)
async def analyze_causal(request: CausalAnalysisRequest):
    """执行因果分析"""
    try:
        data = _get_mock_data(request.barn_id, request.start_time, request.end_time)
        result = causal_analyzer.analyze(data)

        return CausalAnalysisResponse(
            correlation_matrix=result["correlation_matrix"],
            causal_graph=result["causal_graph"],
            causal_paths=result["causal_paths"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/detect-anomalies", response_model=AnomalyResponse)
async def detect_anomalies(barn_id: int):
    """检测异常"""
    try:
        data = _get_mock_data(barn_id, None, None)
        anomalies = anomaly_detector.detect(data)
        total_count = sum(len(v) for v in anomalies.values())

        return AnomalyResponse(anomalies=anomalies, total_count=total_count)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/correlation")
async def get_correlation(barn_id: int):
    """获取相关性分析"""
    try:
        data = _get_mock_data(barn_id, None, None)
        result = correlation_analyzer.analyze(data)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/trace-path")
async def trace_causal_path(cause: str, effect: str):
    """追踪因果路径"""
    return {
        "cause": cause,
        "effect": effect,
        "path": [cause, effect],
        "strength": 0.85,
        "description": f"{cause} 导致 {effect}"
    }

def _get_mock_data(barn_id: int, start_time, end_time) -> pd.DataFrame:
    """获取模拟数据"""
    if start_time is None:
        start_time = datetime.now() - timedelta(days=7)
    if end_time is None:
        end_time = datetime.now()

    np.random.seed(42)
    n_points = 1000

    timestamps = pd.date_range(start=start_time, periods=n_points, freq='1min')
    temperature = 25 + 5 * np.sin(np.linspace(0, 4*np.pi, n_points)) + np.random.normal(0, 2, n_points)
    humidity = 60 + 10 * np.sin(np.linspace(0, 4*np.pi, n_points)) + np.random.normal(0, 5, n_points)
    ammonia_level = 15 + 5 * np.sin(np.linspace(0, 4*np.pi, n_points)) + np.random.normal(0, 3, n_points)

    df = pd.DataFrame({
        'timestamp': timestamps,
        'temperature': temperature,
        'humidity': humidity,
        'ammonia_level': ammonia_level
    })

    return df