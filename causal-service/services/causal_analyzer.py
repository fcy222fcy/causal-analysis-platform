import pandas as pd
import numpy as np
from typing import Dict, List, Optional
import warnings
warnings.filterwarnings('ignore')

# 尝试导入 DoWhy，失败时降级为相关性分析
try:
    import dowhy
    from dowhy import CausalModel
    DOWHY_AVAILABLE = True
except ImportError:
    DOWHY_AVAILABLE = False
    print("Warning: DoWhy not available, falling back to correlation-based analysis")


class CausalAnalyzer:
    def __init__(self):
        self.causal_graph = None

    def analyze(self, data: pd.DataFrame) -> Dict:
        """执行因果分析"""
        correlation_matrix = self._calculate_correlation(data)

        # 优先使用 DoWhy 进行因果推断
        if DOWHY_AVAILABLE and len(data) >= 30:
            try:
                causal_graph = self._build_causal_graph_with_dowhy(data)
            except Exception as e:
                print(f"DoWhy analysis failed: {e}, falling back to correlation-based")
                causal_graph = self._build_causal_graph(data)
        else:
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

    def _build_causal_graph_with_dowhy(self, data: pd.DataFrame) -> Dict:
        """使用 DoWhy 构建因果图"""
        numeric_cols = ['temperature', 'humidity', 'ammonia_level']
        available_cols = [col for col in numeric_cols if col in data.columns]

        if len(available_cols) < 2:
            return self._build_causal_graph(data)

        # 构建因果模型
        # 假设因果关系：temperature -> humidity, ammonia_level -> temperature
        # 这是领域知识定义的因果结构
        causal_edges = []
        if 'temperature' in available_cols and 'humidity' in available_cols:
            causal_edges.append(("temperature", "humidity"))
        if 'ammonia_level' in available_cols and 'temperature' in available_cols:
            causal_edges.append(("ammonia_level", "temperature"))
        if 'ammonia_level' in available_cols and 'humidity' in available_cols:
            causal_edges.append(("ammonia_level", "humidity"))

        # 构建因果图DOT格式
        dot_graph = "digraph {"
        for cause, effect in causal_edges:
            dot_graph += f" {cause} -> {effect};"
        for col in available_cols:
            dot_graph += f" {col};"
        dot_graph += "}"

        try:
            # 创建 DoWhy CausalModel
            model = CausalModel(
                data=data[available_cols],
                treatment=causal_edges[0][0] if causal_edges else available_cols[0],
                outcome=causal_edges[0][1] if causal_edges else available_cols[-1],
                graph=dot_graph
            )

            # 识别因果效应
            identified_estimand = model.identify_effect()

            # 估计因果效应
            estimate = model.estimate_effect(
                identified_estimand,
                method_name="backdoor.linear_regression"
            )

            # 构建节点
            nodes = []
            for col in available_cols:
                color = "#ff6b6b" if col == "temperature" else "#4ecdc4" if col == "humidity" else "#ffd93d"
                nodes.append({
                    "name": col,
                    "itemStyle": {"color": color}
                })

            # 构建边（基于 DoWhy 估计的因果效应）
            edges = []
            for cause, effect in causal_edges:
                # 获取因果强度
                try:
                    strength = abs(float(estimate.value))
                    strength = min(strength, 1.0)  # 归一化到 [0, 1]
                except:
                    strength = 0.5

                edges.append({
                    "source": cause,
                    "target": effect,
                    "strength": round(strength, 4)
                })

            graph = {
                "nodes": nodes,
                "edges": edges,
                "method": "DoWhy Causal Inference"
            }

        except Exception as e:
            print(f"DoWhy estimation failed: {e}")
            graph = self._build_causal_graph(data)

        self.causal_graph = graph
        return graph

    def _build_causal_graph(self, data: pd.DataFrame) -> Dict:
        """基于相关性构建因果图（降级方案）"""
        numeric_cols = ['temperature', 'humidity', 'ammonia_level']
        available_cols = [col for col in numeric_cols if col in data.columns]

        # 构建节点
        nodes = []
        for col in available_cols:
            color = "#ff6b6b" if col == "temperature" else "#4ecdc4" if col == "humidity" else "#ffd93d"
            nodes.append({
                "name": col,
                "itemStyle": {"color": color}
            })

        # 构建边（基于相关性）
        edges = []
        if 'temperature' in data.columns and 'humidity' in data.columns:
            corr = data['temperature'].corr(data['humidity'])
            edges.append({
                "source": "temperature",
                "target": "humidity",
                "strength": round(abs(corr), 4)
            })

        if 'ammonia_level' in data.columns and 'temperature' in data.columns:
            corr = data['ammonia_level'].corr(data['temperature'])
            edges.append({
                "source": "ammonia_level",
                "target": "temperature",
                "strength": round(abs(corr), 4)
            })

        if 'ammonia_level' in data.columns and 'humidity' in data.columns:
            corr = data['ammonia_level'].corr(data['humidity'])
            edges.append({
                "source": "ammonia_level",
                "target": "humidity",
                "strength": round(abs(corr), 4)
            })

        graph = {
            "nodes": nodes,
            "edges": edges,
            "method": "Correlation-based (Fallback)"
        }

        self.causal_graph = graph
        return graph

    def _infer_causal_paths(self, data: pd.DataFrame, graph: Dict) -> List[Dict]:
        """推断因果路径"""
        paths = []

        for edge in graph.get("edges", []):
            cause = edge["source"]
            effect = edge["target"]

            if cause in data.columns and effect in data.columns:
                correlation = data[cause].corr(data[effect])

                paths.append({
                    "cause": cause,
                    "effect": effect,
                    "strength": round(abs(correlation), 4),
                    "direction": "positive" if correlation > 0 else "negative"
                })

        return paths
