#!/usr/bin/env python3
"""
智慧养殖环境数据生成器
基于真实养殖环境的统计特征，生成温度、湿度、氨气浓度数据
"""

import pandas as pd
import numpy as np
from datetime import datetime, timedelta

# 设置随机种子以保证可重复性
np.random.seed(42)

def generate_livestock_data(n_points=1000, n_barns=5):
    """
    生成养殖环境监测数据

    参数:
        n_points: 每个棚舍的数据点数量
        n_barns: 棚舍数量

    返回:
        DataFrame: 包含时间戳、温度、湿度、氨气浓度的数据
    """

    data = []
    start_date = datetime(2024, 1, 1)

    for barn_id in range(1, n_barns + 1):
        # 每个棚舍有不同的基础参数（模拟不同环境）
        base_temp = np.random.uniform(18, 28)  # 基础温度
        base_humidity = np.random.uniform(50, 75)  # 基础湿度
        base_ammonia = np.random.uniform(5, 15)  # 基础氨气浓度

        # 生成时间序列（每分钟一个数据点）
        timestamps = [start_date + timedelta(minutes=i) for i in range(n_points)]

        # 温度变化：日周期 + 随机波动
        hour_cycle = np.sin(np.linspace(0, 4 * np.pi, n_points))  # 日周期
        temperature = base_temp + 5 * hour_cycle + np.random.normal(0, 2, n_points)

        # 湿度变化：与温度负相关 + 随机波动
        humidity = base_humidity - 10 * hour_cycle + np.random.normal(0, 5, n_points)

        # 氨气浓度变化：受温度影响 + 随机异常
        # 氨气在高温时更容易挥发
        ammonia = base_ammonia + 0.3 * (temperature - base_temp) + np.random.normal(0, 2, n_points)

        # 添加一些异常点（模拟异常事件）
        anomaly_indices = np.random.choice(n_points, size=int(n_points * 0.05), replace=False)
        ammonia[anomaly_indices] *= np.random.uniform(1.5, 3.0, len(anomaly_indices))
        temperature[anomaly_indices] += np.random.uniform(-5, 5, len(anomaly_indices))

        # 确保值在合理范围内
        temperature = np.clip(temperature, 10, 45)
        humidity = np.clip(humidity, 30, 95)
        ammonia = np.clip(ammonia, 0, 100)

        # 创建数据框
        for i in range(n_points):
            data.append({
                'timestamp': timestamps[i].strftime('%Y-%m-%d %H:%M:%S'),
                'barn_id': f'BARN{barn_id:03d}',
                'sensor_id': f'SENSOR{barn_id:02d}_{i % 3 + 1:02d}',
                'temperature': round(temperature[i], 2),
                'humidity': round(humidity[i], 2),
                'ammonia_level': round(ammonia[i], 2)
            })

    return pd.DataFrame(data)

def generate_crop_data(n_farms=500):
    """
    生成农业种植数据（基于Kaggle数据集结构）

    参数:
        n_farms: 农场数量

    返回:
        DataFrame: 包含各种农业传感器数据
    """

    regions = ['North India', 'South India', 'Central USA', 'South USA', 'East Africa']
    crops = ['Wheat', 'Rice', 'Maize', 'Cotton', 'Soybean']
    irrigation_types = ['Drip', 'Sprinkler', 'Manual', 'None']
    fertilizer_types = ['Organic', 'Inorganic', 'Mixed']

    data = []

    for i in range(1, n_farms + 1):
        farm_id = f'FARM{i:04d}'
        region = np.random.choice(regions)
        crop_type = np.random.choice(crops)

        # 生成环境数据
        soil_moisture = round(np.random.uniform(10, 50), 2)
        soil_ph = round(np.random.uniform(5.5, 7.5), 2)
        temperature = round(np.random.uniform(15, 35), 2)
        rainfall = round(np.random.uniform(50, 300), 2)
        humidity = round(np.random.uniform(40, 90), 2)
        sunlight_hours = round(np.random.uniform(4, 10), 2)

        # 生成产量数据（基于环境因素）
        base_yield = np.random.uniform(2000, 6000)
        yield_factor = (temperature - 25) * 50 + (humidity - 65) * 30 + (rainfall - 150) * 2
        yield_kg = round(max(1000, base_yield + yield_factor + np.random.normal(0, 500)), 2)

        # 生成日期
        sowing_date = datetime(2024, np.random.randint(1, 6), np.random.randint(1, 28))
        harvest_days = np.random.randint(90, 150)
        harvest_date = sowing_date + timedelta(days=harvest_days)

        data.append({
            'farm_id': farm_id,
            'region': region,
            'crop_type': crop_type,
            'soil_moisture_%': soil_moisture,
            'soil_pH': soil_ph,
            'temperature_C': temperature,
            'rainfall_mm': rainfall,
            'humidity_%': humidity,
            'sunlight_hours': sunlight_hours,
            'irrigation_type': np.random.choice(irrigation_types),
            'fertilizer_type': np.random.choice(fertilizer_types),
            'pesticide_usage_ml': round(np.random.uniform(0, 100), 2),
            'sowing_date': sowing_date.strftime('%Y-%m-%d'),
            'harvest_date': harvest_date.strftime('%Y-%m-%d'),
            'total_days': harvest_days,
            'yield_kg_per_hectare': yield_kg,
            'sensor_id': f'IOT{np.random.randint(1000, 9999)}',
            'timestamp': (datetime(2024, 6, 1) + timedelta(hours=np.random.randint(0, 168))).strftime('%Y-%m-%d %H:%M:%S'),
            'latitude': round(np.random.uniform(10, 35), 4),
            'longitude': round(np.random.uniform(70, 90), 4),
            'NDVI_index': round(np.random.uniform(0.3, 0.9), 4),
            'crop_disease_status': np.random.choice(['None', 'Mild', 'Moderate', 'Severe'], p=[0.7, 0.15, 0.1, 0.05])
        })

    return pd.DataFrame(data)

if __name__ == '__main__':
    print("=" * 60)
    print("智慧养殖环境数据生成器")
    print("=" * 60)

    # 生成养殖环境数据（温度、湿度、氨气）
    print("\n1. 生成养殖环境监测数据...")
    livestock_data = generate_livestock_data(n_points=1000, n_barns=5)
    livestock_file = 'livestock_environment_data.csv'
    livestock_data.to_csv(livestock_file, index=False, encoding='utf-8-sig')
    print(f"   已生成: {livestock_file}")
    print(f"   数据量: {len(livestock_data)} 条记录")
    print(f"   字段: {list(livestock_data.columns)}")
    print(f"\n   数据预览:")
    print(livestock_data.head(10).to_string(index=False))

    # 生成农业种植数据
    print("\n2. 生成农业种植数据...")
    crop_data = generate_crop_data(n_farms=500)
    crop_file = 'smart_farming_crop_data.csv'
    crop_data.to_csv(crop_file, index=False, encoding='utf-8-sig')
    print(f"   已生成: {crop_file}")
    print(f"   数据量: {len(crop_data)} 条记录")
    print(f"   字段: {list(crop_data.columns)}")
    print(f"\n   数据预览:")
    print(crop_data.head(5).to_string(index=False))

    print("\n" + "=" * 60)
    print("数据生成完成！")
    print("=" * 60)
