import pytest
from fastapi.testclient import TestClient
from app import app

client = TestClient(app)

def test_health_check():
    """测试健康检查接口"""
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_analyze_causal():
    """测试因果分析接口"""
    response = client.post("/api/causal/analyze", json={"barn_id": 1})
    assert response.status_code == 200
    data = response.json()
    assert "correlation_matrix" in data
    assert "causal_graph" in data
    assert "causal_paths" in data

def test_detect_anomalies():
    """测试异常检测接口"""
    response = client.post("/api/causal/detect-anomalies?barn_id=1")
    assert response.status_code == 200
    data = response.json()
    assert "anomalies" in data
    assert "total_count" in data

def test_get_correlation():
    """测试相关性分析接口"""
    response = client.get("/api/causal/correlation?barn_id=1")
    assert response.status_code == 200
    data = response.json()
    assert "pearson" in data
    assert "spearman" in data

def test_trace_path():
    """测试因果路径追踪接口"""
    response = client.get("/api/causal/trace-path?cause=temperature&effect=humidity")
    assert response.status_code == 200
    data = response.json()
    assert "cause" in data
    assert "effect" in data
    assert "strength" in data

def test_ingest_data():
    """测试数据接收接口"""
    response = client.post("/api/data/ingest", json=[
        {
            "sensor_id": "S001",
            "barn_id": 1,
            "temperature": 25.5,
            "humidity": 65.0,
            "ammonia_level": 15.0,
            "timestamp": "2024-01-15T10:00:00"
        }
    ])
    assert response.status_code == 200
    data = response.json()
    assert "count" in data
    assert data["count"] == 1
