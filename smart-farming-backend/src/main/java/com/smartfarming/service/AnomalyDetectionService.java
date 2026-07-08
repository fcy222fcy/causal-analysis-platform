package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarming.entity.AnomalyEvent;
import com.smartfarming.entity.EnvSensorData;
import com.smartfarming.mapper.AnomalyEventMapper;
import com.smartfarming.mapper.EnvSensorDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnomalyDetectionService {

    @Autowired
    private EnvSensorDataMapper sensorDataMapper;

    @Autowired
    private AnomalyEventMapper anomalyEventMapper;

    // 默认阈值
    private static final BigDecimal TEMP_THRESHOLD_HIGH = new BigDecimal("35.00");
    private static final BigDecimal TEMP_THRESHOLD_LOW = new BigDecimal("15.00");
    private static final BigDecimal HUMIDITY_THRESHOLD_HIGH = new BigDecimal("85.00");
    private static final BigDecimal AMMONIA_THRESHOLD_HIGH = new BigDecimal("25.00");

    // 滑动窗口参数
    private static final int DEFAULT_WINDOW_SIZE = 60;
    private static final double DEFAULT_THRESHOLD = 2.0;

    /**
     * 执行异常检测（阈值 + 滑动窗口）
     */
    public List<AnomalyEvent> detectAnomalies(Long barnId) {
        LambdaQueryWrapper<EnvSensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvSensorData::getBarnId, barnId)
               .orderByAsc(EnvSensorData::getTimestamp)
               .last("LIMIT 200");
        List<EnvSensorData> dataList = sensorDataMapper.selectList(wrapper);

        List<AnomalyEvent> anomalies = new ArrayList<>();

        // 1. 阈值检测
        anomalies.addAll(detectByThreshold(dataList, barnId));

        // 2. 滑动窗口检测
        anomalies.addAll(detectBySlidingWindow(dataList, barnId));

        return anomalies;
    }

    /**
     * 基于阈值的异常检测
     */
    private List<AnomalyEvent> detectByThreshold(List<EnvSensorData> dataList, Long barnId) {
        List<AnomalyEvent> anomalies = new ArrayList<>();

        for (EnvSensorData data : dataList) {
            if (data.getTemperature() != null && data.getTemperature().compareTo(TEMP_THRESHOLD_HIGH) > 0) {
                AnomalyEvent event = createAnomalyEvent(barnId, "environmental", "high",
                        "温度过高: " + data.getTemperature() + "°C", data.getTimestamp());
                anomalies.add(event);
            }

            if (data.getAmmoniaLevel() != null && data.getAmmoniaLevel().compareTo(AMMONIA_THRESHOLD_HIGH) > 0) {
                AnomalyEvent event = createAnomalyEvent(barnId, "environmental", "medium",
                        "氨气浓度超标: " + data.getAmmoniaLevel() + "ppm", data.getTimestamp());
                anomalies.add(event);
            }
        }

        return anomalies;
    }

    /**
     * 基于滑动窗口的异常检测
     * 计算窗口内的均值和标准差，超出阈值倍标准差则标记为异常
     */
    private List<AnomalyEvent> detectBySlidingWindow(List<EnvSensorData> dataList, Long barnId) {
        return detectBySlidingWindow(dataList, barnId, DEFAULT_WINDOW_SIZE, DEFAULT_THRESHOLD);
    }

    private List<AnomalyEvent> detectBySlidingWindow(List<EnvSensorData> dataList, Long barnId,
                                                      int windowSize, double threshold) {
        List<AnomalyEvent> anomalies = new ArrayList<>();

        if (dataList.size() < windowSize) {
            return anomalies;
        }

        // 提取温度和氨气数据
        List<Double> temperatures = new ArrayList<>();
        List<Double> ammoniaLevels = new ArrayList<>();
        List<LocalDateTime> timestamps = new ArrayList<>();

        for (EnvSensorData data : dataList) {
            if (data.getTemperature() != null) {
                temperatures.add(data.getTemperature().doubleValue());
            }
            if (data.getAmmoniaLevel() != null) {
                ammoniaLevels.add(data.getAmmoniaLevel().doubleValue());
            }
            timestamps.add(data.getTimestamp());
        }

        // 滑动窗口检测温度异常
        if (temperatures.size() >= windowSize) {
            for (int i = windowSize; i < temperatures.size(); i++) {
                List<Double> window = temperatures.subList(i - windowSize, i);
                double mean = calculateMean(window);
                double std = calculateStd(window, mean);
                double current = temperatures.get(i);

                if (std > 0 && Math.abs(current - mean) > threshold * std) {
                    AnomalyEvent event = createAnomalyEvent(barnId, "environmental", "medium",
                            String.format("温度滑动窗口异常: 当前%.1f°C, 窗口均值%.1f°C, 标准差%.2f",
                                    current, mean, std),
                            timestamps.get(i));
                    anomalies.add(event);
                }
            }
        }

        // 滑动窗口检测氨气异常
        if (ammoniaLevels.size() >= windowSize) {
            for (int i = windowSize; i < ammoniaLevels.size(); i++) {
                List<Double> window = ammoniaLevels.subList(i - windowSize, i);
                double mean = calculateMean(window);
                double std = calculateStd(window, mean);
                double current = ammoniaLevels.get(i);

                if (std > 0 && Math.abs(current - mean) > threshold * std) {
                    AnomalyEvent event = createAnomalyEvent(barnId, "environmental", "low",
                            String.format("氨气滑动窗口异常: 当前%.1fppm, 窗口均值%.1fppm, 标准差%.2f",
                                    current, mean, std),
                            timestamps.get(i));
                    anomalies.add(event);
                }
            }
        }

        return anomalies;
    }

    /**
     * 计算均值
     */
    private double calculateMean(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 计算标准差
     */
    private double calculateStd(List<Double> data, double mean) {
        double variance = data.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * 创建异常事件对象
     */
    private AnomalyEvent createAnomalyEvent(Long barnId, String eventType, String severity,
                                             String description, LocalDateTime timestamp) {
        AnomalyEvent event = new AnomalyEvent();
        event.setBarnId(barnId);
        event.setEventType(eventType);
        event.setSeverityLevel(severity);
        event.setDescription(description);
        event.setTimestamp(timestamp);
        anomalyEventMapper.insert(event);
        return event;
    }

    /**
     * 按棚舍查询异常事件
     */
    public List<AnomalyEvent> getAnomaliesByBarnId(Long barnId) {
        LambdaQueryWrapper<AnomalyEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnomalyEvent::getBarnId, barnId)
               .orderByDesc(AnomalyEvent::getTimestamp);
        return anomalyEventMapper.selectList(wrapper);
    }
}
