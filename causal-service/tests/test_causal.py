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