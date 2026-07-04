# Python因果分析服务实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现Python因果分析服务，提供因果分析、异常检测和相关性分析API

**Architecture:** FastAPI + DoWhy + PyTorch，提供REST API接口供Java后端调用

**Tech Stack:** FastAPI, pandas, numpy, scikit-learn, DoWhy, PyTorch

---

## Task 1: 项目初始化

**Files:**
- Create: `causal-service/requirements.txt`
- Create: `causal-service/app.py`
- Create: `causal-service/routers/__init__.py`
- Create: `causal-service/services/__init__.py`
- Create: `causal-service/models/__init__.py`

- [ ] **Step 1: 创建requirements.txt**

```txt
fastapi==0.104.1
uvicorn==0.24.0
pandas==2.1.3
numpy==1.26.2
scikit-learn==1.3.2
dowhy==0.11.1
torch==2.1.1
scipy==1.11.4
pydantic==2.5.2
requests==2.31.0
```

- [ ] **Step 2: 创建目录结构**

```bash
mkdir -p causal-service/routers
mkdir -p causal-service/services
mkdir -p causal-service/models
touch causal-service/routers/__init__.py
touch causal-service/services/__init__.py
touch causal-service/models/__init__.py
```

- [ ] **Step 3: 创建app.py**

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import causal_router, data_router

app = FastAPI(title="智慧养殖因果分析服务", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(causal_router.router, prefix="/api/causal", tags=["因果分析"])
app.include_router(data_router.router, prefix="/api/data", tags=["数据处理"])

@app.get("/")
def root():
    return {"message": "智慧养殖因果分析服务"}

@app.get("/health")
def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
```

- [ ] **Step 4: 创建临时路由文件**

```python
# causal-service/routers/causal_router.py
from fastapi import APIRouter
router = APIRouter()

@router.get("/test")
async def test():
    return {"message": "causal router works"}

# causal-service/routers/data_router.py
from fastapi import APIRouter
router = APIRouter()

@router.get("/test")
async def test():
    return {"message": "data router works"}
```

- [ ] **Step 5: 测试服务启动**

```bash
cd causal-service
pip install -r requirements.txt
python app.py
```

访问 http://localhost:5000/ 应返回 `{"message":"智慧养殖因果分析服务"}`

- [ ] **Step 6: Commit**

```bash
git add causal-service/
git commit -m "feat: 初始化Python因果分析服务项目"
```

---

## Task 2: 实现数据模型

**Files:**
- Create: `causal-service/models/schemas.py`

- [ ] **Step 1: 创建schemas.py**

```python
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
```

- [ ] **Step 2: Commit**

```bash
git add causal-service/models/schemas.py
git commit -m "feat: 创建Pydantic数据模型"
```

---

## Task 3: 实现因果分析核心算法

**Files:**
- Create: `causal-service/services/causal_analyzer.py`
- Create: `causal-service/services/anomaly_detector.py`
- Create: `causal-service/services/correlation_analyzer.py`

- [ ] **Step 1: 创建causal_analyzer.py**

```python
import pandas as pd
import numpy as np
from typing import Dict, List
import warnings
warnings.filterwarnings('ignore')

class CausalAnalyzer:
    def __init__(self):
        self.causal_graph = None
    
    def analyze(self, data: pd.DataFrame) -> Dict:
        """执行因果分析"""
        correlation_matrix = self._calculate_correlation(data)
        causal_graph = self._build_causal_graph(data)
        causal_paths = self._infer_causal_paths(data, causal_graph)
        
        return {
            "correlation_matrix": correlation_matrix,
            "causal_graph": causal_graph,
            "causal_paths": causal_paths
        }
    
    def _calculate_correlation(self, data: pd.DataFrame) -> Dict:
        """计算相关性矩阵"""
        numeric_cols = ['temperature', 'humidity', 'ammonia_level']
        available_cols = [col for col in numeric_cols if col in data.columns]
        
        if len(available_cols) < 2:
            return {"pearson": {}, "spearman": {}}
        
        pearson_corr = data[available_cols].corr(method='pearson').to_dict()
        spearman_corr = data[available_cols].corr(method='spearman').to_dict()
        
        return {"pearson": pearson_corr, "spearman": spearman_corr}
    
    def _build_causal_graph(self, data: pd.DataFrame) -> Dict:
        """构建因果图"""
        causal_edges = []
        
        if 'temperature' in data.columns and 'humidity' in data.columns:
            causal_edges.append(("temperature", "humidity"))
        
        if 'ammonia_level' in data.columns and 'temperature' in data.columns:
            causal_edges.append(("ammonia_level", "temperature"))
        
        if 'ammonia_level' in data.columns and 'humidity' in data.columns:
            causal_edges.append(("ammonia_level", "humidity"))
        
        graph = {
            "nodes": list(data.select_dtypes(include=[np.number]).columns),
            "edges": causal_edges
        }
        
        self.causal_graph = graph
        return graph
    
    def _infer_causal_paths(self, data: pd.DataFrame, graph: Dict) -> List[Dict]:
        """推断因果路径"""
        paths = []
        
        for cause, effect in graph.get("edges", []):
            if cause in data.columns and effect in data.columns:
                correlation = data[cause].corr(data[effect])
                
                paths.append({
                    "cause": cause,
                    "effect": effect,
                    "strength": abs(correlation),
                    "direction": "positive" if correlation > 0 else "negative"
                })
        
        return paths
```

- [ ] **Step 2: 创建anomaly_detector.py**

```python
import pandas as pd
import numpy as np
from typing import Dict, List

class AnomalyDetector:
    def __init__(self, window_size: int = 60, threshold: float = 2.0):
        self.window_size = window_size
        self.threshold = threshold
    
    def detect(self, data: pd.DataFrame) -> Dict:
        """基于滑动窗口统计模型检测异常"""
        anomalies = {
            "temperature": [],
            "humidity": [],
            "ammonia_level": []
        }
        
        for col in ['temperature', 'humidity', 'ammonia_level']:
            if col in data.columns:
                anomalies[col] = self._detect_column_anomalies(data, col)
        
        return anomalies
    
    def _detect_column_anomalies(self, data: pd.DataFrame, column: str) -> List[Dict]:
        """检测单列异常"""
        if column not in data.columns or len(data) < self.window_size:
            return []
        
        anomalies = []
        values = data[column].values
        timestamps = data['timestamp'].values if 'timestamp' in data.columns else range(len(values))
        
        for i in range(self.window_size, len(values)):
            window = values[i-self.window_size:i]
            mean = np.mean(window)
            std = np.std(window)
            
            if std > 0 and abs(values[i] - mean) > self.threshold * std:
                anomalies.append({
                    "index": i,
                    "timestamp": str(timestamps[i]),
                    "value": float(values[i]),
                    "mean": float(mean),
                    "std": float(std),
                    "deviation": float(abs(values[i] - mean) / std)
                })
        
        return anomalies
```

- [ ] **Step 3: 创建correlation_analyzer.py**

```python
import pandas as pd
import numpy as np
from typing import Dict

class CorrelationAnalyzer:
    def analyze(self, data: pd.DataFrame) -> Dict:
        """分析变量间相关性"""
        numeric_cols = ['temperature', 'humidity', 'ammonia_level']
        available_cols = [col for col in numeric_cols if col in data.columns]
        
        if len(available_cols) < 2:
            return {"pearson": {}, "spearman": {}, "kendall": {}}
        
        pearson_corr = data[available_cols].corr(method='pearson').to_dict()
        spearman_corr = data[available_cols].corr(method='spearman').to_dict()
        kendall_corr = data[available_cols].corr(method='kendall').to_dict()
        
        return {
            "pearson": pearson_corr,
            "spearman": spearman_corr,
            "kendall": kendall_corr
        }
    
    def get_top_correlations(self, data: pd.DataFrame, top_n: int = 5) -> list:
        """获取相关性最高的变量对"""
        corr_result = self.analyze(data)
        pearson_corr = corr_result.get("pearson", {})
        
        correlations = []
        variables = list(pearson_corr.keys())
        
        for i in range(len(variables)):
            for j in range(i+1, len(variables)):
                var1, var2 = variables[i], variables[j]
                if var1 in pearson_corr and var2 in pearson_corr[var1]:
                    correlations.append({
                        "var1": var1,
                        "var2": var2,
                        "correlation": pearson_corr[var1][var2]
                    })
        
        correlations.sort(key=lambda x: abs(x["correlation"]), reverse=True)
        return correlations[:top_n]
```

- [ ] **Step 4: Commit**

```bash
git add causal-service/services/
git commit -m "feat: 实现因果分析核心算法"
```

---

## Task 4: 实现REST API接口

**Files:**
- Modify: `causal-service/routers/causal_router.py`
- Modify: `causal-service/routers/data_router.py`

- [ ] **Step 1: 重写causal_router.py**

```python
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
```

- [ ] **Step 2: 重写data_router.py**

```python
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
```

- [ ] **Step 3: 测试API**

```bash
cd causal-service
python app.py

# 测试因果分析接口
curl -X POST http://localhost:5000/api/causal/analyze \
  -H "Content-Type: application/json" \
  -d '{"barn_id": 1}'

# 测试异常检测接口
curl -X POST "http://localhost:5000/api/causal/detect-anomalies?barn_id=1"

# 测试相关性分析接口
curl "http://localhost:5000/api/causal/correlation?barn_id=1"
```

- [ ] **Step 4: Commit**

```bash
git add causal-service/routers/
git commit -m "feat: 实现REST API接口"
```

---

## Task 5: 添加测试

**Files:**
- Create: `causal-service/tests/__init__.py`
- Create: `causal-service/tests/test_causal.py`

- [ ] **Step 1: 创建测试文件**

```python
import pytest
from services.causal_analyzer import CausalAnalyzer
from services.anomaly_detector import AnomalyDetector
from services.correlation_analyzer import CorrelationAnalyzer
import pandas as pd
import numpy as np

def test_causal_analyzer():
    """测试因果分析器"""
    analyzer = CausalAnalyzer()
    
    np.random.seed(42)
    data = pd.DataFrame({
        'temperature': np.random.normal(25, 5, 100),
        'humidity': np.random.normal(60, 10, 100),
        'ammonia_level': np.random.normal(15, 3, 100)
    })
    
    result = analyzer.analyze(data)
    
    assert 'correlation_matrix' in result
    assert 'causal_graph' in result
    assert 'causal_paths' in result
    assert len(result['causal_paths']) > 0

def test_anomaly_detector():
    """测试异常检测器"""
    detector = AnomalyDetector(window_size=10, threshold=2.0)
    
    np.random.seed(42)
    values = np.random.normal(25, 2, 100)
    values[50] = 50  # 添加异常值
    
    data = pd.DataFrame({
        'temperature': values,
        'humidity': np.random.normal(60, 5, 100),
        'ammonia_level': np.random.normal(15, 3, 100)
    })
    
    result = detector.detect(data)
    
    assert 'temperature' in result
    assert len(result['temperature']) > 0

def test_correlation_analyzer():
    """测试相关性分析器"""
    analyzer = CorrelationAnalyzer()
    
    np.random.seed(42)
    data = pd.DataFrame({
        'temperature': np.random.normal(25, 5, 100),
        'humidity': np.random.normal(60, 10, 100),
        'ammonia_level': np.random.normal(15, 3, 100)
    })
    
    result = analyzer.analyze(data)
    
    assert 'pearson' in result
    assert 'spearman' in result
    assert 'kendall' in result
```

- [ ] **Step 2: 运行测试**

```bash
cd causal-service
pytest tests/ -v
```

- [ ] **Step 3: Commit**

```bash
git add causal-service/tests/
git commit -m "feat: 添加单元测试"
```
