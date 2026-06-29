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