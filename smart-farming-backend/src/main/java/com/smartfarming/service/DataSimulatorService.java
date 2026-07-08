package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarming.entity.AlarmRecord;
import com.smartfarming.entity.AnomalyEvent;
import com.smartfarming.entity.AnimalBehavior;
import com.smartfarming.entity.EnvSensorData;
import com.smartfarming.entity.SystemConfig;
import com.smartfarming.mapper.AnimalBehaviorMapper;
import com.smartfarming.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class DataSimulatorService {

    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private AnimalBehaviorMapper animalBehaviorMapper;

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    private final Random random = new Random();
    private boolean initialized = false;

    /**
     * 启动时批量生成历史数据
     */
    @PostConstruct
    public void initBulkData() {
        if (initialized) return;
        initialized = true;

        System.out.println(">>> 开始生成历史数据...");

        // 生成200条传感器数据（模拟最近2小时）
        for (int i = 0; i < 200; i++) {
            generateSensorDataInternal(LocalDateTime.now().minusMinutes(120 - i));
        }

        // 生成100条行为数据
        for (int i = 0; i < 100; i++) {
            generateBehaviorDataInternal(LocalDateTime.now().minusMinutes(100 - i));
        }

        // 运行异常检测
        for (long barnId = 1; barnId <= 5; barnId++) {
            anomalyDetectionService.detectAnomalies(barnId);
        }

        System.out.println(">>> 历史数据生成完成！");
    }

    /**
     * 内部方法：生成指定时间的传感器数据
     */
    private void generateSensorDataInternal(LocalDateTime timestamp) {
        EnvSensorData data = new EnvSensorData();
        data.setSensorId("S" + String.format("%03d", random.nextInt(10) + 1));
        data.setBarnId((long) (random.nextInt(5) + 1));

        int abnormalChance = random.nextInt(100);
        BigDecimal temperature;
        BigDecimal humidity;
        BigDecimal ammoniaLevel;

        if (abnormalChance < 15) {
            temperature = BigDecimal.valueOf(36 + random.nextDouble() * 4).setScale(2, RoundingMode.HALF_UP);
        } else if (abnormalChance < 25) {
            temperature = BigDecimal.valueOf(12 + random.nextDouble() * 3).setScale(2, RoundingMode.HALF_UP);
        } else {
            temperature = BigDecimal.valueOf(25 + random.nextGaussian() * 5).setScale(2, RoundingMode.HALF_UP);
        }

        if (random.nextInt(100) < 10) {
            humidity = BigDecimal.valueOf(86 + random.nextDouble() * 10).setScale(2, RoundingMode.HALF_UP);
        } else {
            humidity = BigDecimal.valueOf(60 + random.nextGaussian() * 10).setScale(2, RoundingMode.HALF_UP);
        }

        if (random.nextInt(100) < 12) {
            ammoniaLevel = BigDecimal.valueOf(26 + random.nextDouble() * 10).setScale(2, RoundingMode.HALF_UP);
        } else {
            ammoniaLevel = BigDecimal.valueOf(15 + random.nextGaussian() * 5).setScale(2, RoundingMode.HALF_UP);
        }

        data.setTemperature(temperature);
        data.setHumidity(humidity);
        data.setAmmoniaLevel(ammoniaLevel);
        data.setTimestamp(timestamp);
        sensorDataService.saveSensorData(data);
    }

    /**
     * 内部方法：生成指定时间的行为数据
     */
    private void generateBehaviorDataInternal(LocalDateTime timestamp) {
        AnimalBehavior behavior = new AnimalBehavior();
        behavior.setBarnId((long) (random.nextInt(5) + 1));

        String[] types = {"normal", "normal", "normal", "normal", "stress", "abnormal", "feeding", "resting"};
        String type = types[random.nextInt(types.length)];
        behavior.setBehaviorType(type);

        BigDecimal confidence;
        if ("normal".equals(type)) {
            confidence = BigDecimal.valueOf(0.85 + random.nextDouble() * 0.14).setScale(4, RoundingMode.HALF_UP);
        } else if ("abnormal".equals(type)) {
            confidence = BigDecimal.valueOf(0.6 + random.nextDouble() * 0.3).setScale(4, RoundingMode.HALF_UP);
        } else {
            confidence = BigDecimal.valueOf(0.7 + random.nextDouble() * 0.28).setScale(4, RoundingMode.HALF_UP);
        }
        behavior.setConfidenceScore(confidence);
        behavior.setTimestamp(timestamp);
        animalBehaviorMapper.insert(behavior);
    }

    private Map<String, String> getThresholds() {
        Map<String, String> thresholds = new HashMap<>();
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SystemConfig::getConfigKey,
                "temperature_max", "temperature_min",
                "humidity_max", "humidity_min",
                "ammonia_level_max");
        systemConfigMapper.selectList(wrapper).forEach(c ->
                thresholds.put(c.getConfigKey(), c.getConfigValue()));
        return thresholds;
    }

    @Scheduled(fixedRate = 3000)
    public void generateSensorData() {
        EnvSensorData data = new EnvSensorData();
        data.setSensorId("S" + String.format("%03d", random.nextInt(10) + 1));
        data.setBarnId((long) (random.nextInt(5) + 1));

        Map<String, String> thresholds = getThresholds();
        BigDecimal tempMax = new BigDecimal(thresholds.getOrDefault("temperature_max", "35.00"));
        BigDecimal tempMin = new BigDecimal(thresholds.getOrDefault("temperature_min", "15.00"));
        BigDecimal humidityMax = new BigDecimal(thresholds.getOrDefault("humidity_max", "85.00"));
        BigDecimal ammoniaMax = new BigDecimal(thresholds.getOrDefault("ammonia_level_max", "25.00"));

        int abnormalChance = random.nextInt(100);
        BigDecimal temperature;
        BigDecimal humidity;
        BigDecimal ammoniaLevel;

        if (abnormalChance < 15) {
            temperature = tempMax.add(BigDecimal.valueOf(random.nextDouble() * 3)).setScale(2, RoundingMode.HALF_UP);
        } else if (abnormalChance < 25) {
            temperature = tempMin.subtract(BigDecimal.valueOf(random.nextDouble() * 2)).setScale(2, RoundingMode.HALF_UP);
        } else {
            temperature = BigDecimal.valueOf(25 + random.nextGaussian() * 5).setScale(2, RoundingMode.HALF_UP);
        }

        if (random.nextInt(100) < 10) {
            humidity = humidityMax.add(BigDecimal.valueOf(random.nextDouble() * 5)).setScale(2, RoundingMode.HALF_UP);
        } else {
            humidity = BigDecimal.valueOf(60 + random.nextGaussian() * 10).setScale(2, RoundingMode.HALF_UP);
        }

        if (random.nextInt(100) < 12) {
            ammoniaLevel = ammoniaMax.add(BigDecimal.valueOf(random.nextDouble() * 8)).setScale(2, RoundingMode.HALF_UP);
        } else {
            ammoniaLevel = BigDecimal.valueOf(15 + random.nextGaussian() * 5).setScale(2, RoundingMode.HALF_UP);
        }

        data.setTemperature(temperature);
        data.setHumidity(humidity);
        data.setAmmoniaLevel(ammoniaLevel);
        data.setTimestamp(LocalDateTime.now());
        sensorDataService.saveSensorData(data);
    }

    @Scheduled(fixedRate = 5000)
    public void generateAnimalBehavior() {
        AnimalBehavior behavior = new AnimalBehavior();
        behavior.setBarnId((long) (random.nextInt(5) + 1));

        String[] types = {"normal", "normal", "normal", "normal", "stress", "abnormal", "feeding", "resting"};
        String type = types[random.nextInt(types.length)];
        behavior.setBehaviorType(type);

        BigDecimal confidence;
        if ("normal".equals(type)) {
            confidence = BigDecimal.valueOf(0.85 + random.nextDouble() * 0.14).setScale(4, RoundingMode.HALF_UP);
        } else if ("abnormal".equals(type)) {
            confidence = BigDecimal.valueOf(0.6 + random.nextDouble() * 0.3).setScale(4, RoundingMode.HALF_UP);
        } else {
            confidence = BigDecimal.valueOf(0.7 + random.nextDouble() * 0.28).setScale(4, RoundingMode.HALF_UP);
        }
        behavior.setConfidenceScore(confidence);
        behavior.setTimestamp(LocalDateTime.now());
        animalBehaviorMapper.insert(behavior);
    }

    @Scheduled(fixedRate = 30000)
    public void runAnomalyDetection() {
        for (long barnId = 1; barnId <= 5; barnId++) {
            anomalyDetectionService.detectAnomalies(barnId);
        }
    }

    @Scheduled(fixedRate = 20000)
    public void checkAndGenerateAlarms() {
        Map<String, String> thresholds = getThresholds();
        BigDecimal tempMax = new BigDecimal(thresholds.getOrDefault("temperature_max", "35.00"));
        BigDecimal humidityMax = new BigDecimal(thresholds.getOrDefault("humidity_max", "85.00"));
        BigDecimal ammoniaMax = new BigDecimal(thresholds.getOrDefault("ammonia_level_max", "25.00"));

        for (long barnId = 1; barnId <= 5; barnId++) {
            java.util.List<EnvSensorData> recentData = sensorDataService.getSensorDataByBarnId(barnId);
            if (recentData.isEmpty()) continue;

            EnvSensorData latest = recentData.get(0);

            if (latest.getTemperature() != null && latest.getTemperature().compareTo(tempMax) > 0) {
                AlarmRecord alarm = new AlarmRecord();
                alarm.setEventId(0L);
                alarm.setBarnId(barnId);
                alarm.setAlarmType("temperature");
                alarm.setAlarmLevel(latest.getTemperature().compareTo(tempMax.add(BigDecimal.ONE)) > 0 ? "high" : "medium");
                alarm.setAlarmContent("温度过高：" + latest.getTemperature() + "°C（阈值：" + tempMax + "°C）");
                safeCreateAlarm(alarm);
            }
            if (latest.getHumidity() != null && latest.getHumidity().compareTo(humidityMax) > 0) {
                AlarmRecord alarm = new AlarmRecord();
                alarm.setEventId(0L);
                alarm.setBarnId(barnId);
                alarm.setAlarmType("humidity");
                alarm.setAlarmLevel("medium");
                alarm.setAlarmContent("湿度过高：" + latest.getHumidity() + "%（阈值：" + humidityMax + "%）");
                safeCreateAlarm(alarm);
            }
            if (latest.getAmmoniaLevel() != null && latest.getAmmoniaLevel().compareTo(ammoniaMax) > 0) {
                AlarmRecord alarm = new AlarmRecord();
                alarm.setEventId(0L);
                alarm.setBarnId(barnId);
                alarm.setAlarmType("ammonia");
                alarm.setAlarmLevel(latest.getAmmoniaLevel().compareTo(ammoniaMax.add(BigDecimal.valueOf(5))) > 0 ? "high" : "medium");
                alarm.setAlarmContent("氨气浓度超标：" + latest.getAmmoniaLevel() + "ppm（阈值：" + ammoniaMax + "ppm）");
                safeCreateAlarm(alarm);
            }
        }
    }

    private void safeCreateAlarm(AlarmRecord alarm) {
        try {
            alarmService.createAlarm(alarm);
        } catch (Exception ignored) {
        }
    }
}
