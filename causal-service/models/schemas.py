from pydantic import BaseModel
from typing import List, Dict, Optional
from datetime import datetime

class SensorData(BaseModel):
    sensor_id: str
    barn_id: int
    temperature: float
    humidity: float
    ammonia_level: float
    timestamp: datetime

class CausalAnalysisRequest(BaseModel):
    barn_id: int
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None

class CausalAnalysisResponse(BaseModel):
    correlation_matrix: Dict
    causal_graph: Dict
    causal_paths: List[Dict]

class AnomalyResponse(BaseModel):
    anomalies: Dict
    total_count: int

class CorrelationResponse(BaseModel):
    pearson: Dict
    spearman: Dict