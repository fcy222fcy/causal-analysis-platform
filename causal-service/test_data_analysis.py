#!/usr/bin/env python3
"""
测试数据加载和因果分析
"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import pandas as pd
import numpy as np
from services.causal_analyzer import CausalAnalyzer
from services.anomaly_detector import AnomalyDetector
from services.correlation_analyzer import CorrelationAnalyzer

def main():
    print("=" * 60)
    print("智慧养殖数据因果分析测试")
    print("=" * 60)

    # 1. 加载数据
    print("\n1. 加载CSV数据...")
    data_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'data', 'livestock_environment_data.csv')

    if not os.path.exists(data_file):
        print(f"   错误: 数据文件不存在: {data_file}")
        return

    df = pd.read_csv(data_file)
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    print(f"   加载成功! 总记录数: {len(df)}")
    print(f"   棚舍数量: {df['barn_id'].nunique()}")
    print(f"   传感器数量: {df['sensor_id'].nunique()}")

    # 2. 选择一个棚舍进行分析
    barn_id = 'BARN001'
    barn_data = df[df['barn_id'] == barn_id][['timestamp', 'temperature', 'humidity', 'ammonia_level']]
    print(f"\n2. 分析棚舍 {barn_id} 的数据...")
    print(f"   记录数: {len(barn_data)}")
    print(f"   时间范围: {barn_data['timestamp'].min()} ~ {barn_data['timestamp'].max()}")

    # 3. 因果分析
    print("\n3. 执行因果分析...")
    causal_analyzer = CausalAnalyzer()
    causal_result = causal_analyzer.analyze(barn_data)

    print("   相关性矩阵 (Pearson):")
    if 'pearson' in causal_result['correlation_matrix']:
        for var1, correlations in causal_result['correlation_matrix']['pearson'].items():
            for var2, value in correlations.items():
                if var1 != var2:
                    print(f"     {var1} <-> {var2}: {value:.4f}")

    print("\n   因果图:")
    if 'edges' in causal_result['causal_graph']:
        for edge in causal_result['causal_graph']['edges']:
            print(f"     {edge[0]} -> {edge[1]}")

    print("\n   因果路径:")
    for path in causal_result['causal_paths']:
        print(f"     {path['cause']} -> {path['effect']}: 强度={path['strength']:.4f}, 方向={path['direction']}")

    # 4. 异常检测
    print("\n4. 执行异常检测...")
    anomaly_detector = AnomalyDetector(window_size=60, threshold=2.0)
    anomalies = anomaly_detector.detect(barn_data)

    for col, col_anomalies in anomalies.items():
        print(f"   {col}: 检测到 {len(col_anomalies)} 个异常点")
        if col_anomalies:
            print(f"     示例: {col_anomalies[0]}")

    # 5. 相关性分析
    print("\n5. 执行相关性分析...")
    correlation_analyzer = CorrelationAnalyzer()
    corr_result = correlation_analyzer.analyze(barn_data)

    print("   Pearson相关性:")
    for var1, correlations in corr_result['pearson'].items():
        for var2, value in correlations.items():
            if var1 != var2:
                print(f"     {var1} <-> {var2}: {value:.4f}")

    print("\n   Top 3 相关性:")
    top_corr = correlation_analyzer.get_top_correlations(barn_data, top_n=3)
    for item in top_corr:
        print(f"     {item['var1']} <-> {item['var2']}: {item['correlation']:.4f}")

    print("\n" + "=" * 60)
    print("分析完成!")
    print("=" * 60)

if __name__ == '__main__':
    main()
