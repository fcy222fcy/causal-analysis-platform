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