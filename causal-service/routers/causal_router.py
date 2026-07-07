from fastapi import APIRouter, HTTPException
from typing import List
from models.schemas import CausalAnalysisRequest, CausalAnalysisResponse, AnomalyResponse
from services.causal_analyzer import CausalAnalyzer
from services.anomaly_detector import AnomalyDetector
from services.correlation_analyzer import CorrelationAnalyzer
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import os

router = APIRouter()
causal_analyzer = CausalAnalyzer()
anomaly_detector = AnomalyDetector()
correlation_analyzer = CorrelationAnalyzer()

# 数据文件路径
DATA_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'data')
LIVESTOCK_DATA_FILE = os.path.join(DATA_DIR, 'livestock_environment_data.csv')

# 缓存加载的数据
_cached_data = None
_last_load_time = None

def _load_data_from_csv() -> pd.DataFrame:
    """从CSV文件加载数据"""
    global _cached_data, _last_load_time

    # 如果缓存存在且不超过5分钟，则使用缓存
    if _cached_data is not None and _last_load_time is not None:
        if (datetime.now() - _last_load_time).seconds < 300:
            return _cached_data

    try:
        if os.path.exists(LIVESTOCK_DATA_FILE):
            df = pd.read_csv(LIVESTOCK_DATA_FILE)
            df['timestamp'] = pd.to_datetime(df['timestamp'])
            _cached_data = df
            _last_load_time = datetime.now()
            return df
        else:
            # 如果文件不存在，返回空DataFrame
            return pd.DataFrame(columns=['timestamp', 'barn_id', 'sensor_id', 'temperature', 'humidity', 'ammonia_level'])
    except Exception as e:
        print(f"加载数据文件失败: {e}")
        return pd.DataFrame(columns=['timestamp', 'barn_id', 'sensor_id', 'temperature', 'humidity', 'ammonia_level'])

def _get_mock_data(barn_id: int, start_time, end_time) -> pd.DataFrame:
    """获取数据（优先使用CSV数据，如果没有则生成模拟数据）"""
    # 尝试从CSV加载数据
    df = _load_data_from_csv()

    # 如果CSV数据可用，按条件过滤
    if not df.empty:
        # 按barn_id过滤
        barn_str = f'BARN{barn_id:03d}'
        df = df[df['barn_id'] == barn_str]

        # 按时间范围过滤
        if start_time is not None:
            df = df[df['timestamp'] >= start_time]
        if end_time is not None:
            df = df[df['timestamp'] <= end_time]

        # 如果过滤后有数据，返回
        if not df.empty:
            return df[['timestamp', 'temperature', 'humidity', 'ammonia_level']]

    # 如果CSV数据不可用，生成模拟数据
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

@router.get("/data-stats")
async def get_data_stats():
    """获取数据统计信息"""
    try:
        df = _load_data_from_csv()
        if df.empty:
            return {"message": "没有可用数据"}

        return {
            "total_records": len(df),
            "barns": df['barn_id'].unique().tolist(),
            "sensors": df['sensor_id'].nunique(),
            "time_range": {
                "start": str(df['timestamp'].min()),
                "end": str(df['timestamp'].max())
            },
            "statistics": {
                "temperature": {
                    "mean": round(df['temperature'].mean(), 2),
                    "std": round(df['temperature'].std(), 2),
                    "min": round(df['temperature'].min(), 2),
                    "max": round(df['temperature'].max(), 2)
                },
                "humidity": {
                    "mean": round(df['humidity'].mean(), 2),
                    "std": round(df['humidity'].std(), 2),
                    "min": round(df['humidity'].min(), 2),
                    "max": round(df['humidity'].max(), 2)
                },
                "ammonia_level": {
                    "mean": round(df['ammonia_level'].mean(), 2),
                    "std": round(df['ammonia_level'].std(), 2),
                    "min": round(df['ammonia_level'].min(), 2),
                    "max": round(df['ammonia_level'].max(), 2)
                }
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
