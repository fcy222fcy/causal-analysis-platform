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