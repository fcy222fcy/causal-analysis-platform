package com.smartfarming.config;

import com.smartfarming.entity.EnvSensorData;
import com.smartfarming.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据初始化器 - 启动时自动导入CSV数据到数据库
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SensorDataService sensorDataService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("开始初始化传感器数据...");
        System.out.println("========================================");

        try {
            // 检查数据库是否已有数据
            List<EnvSensorData> existingData = sensorDataService.getSensorDataByBarnId(1L);
            if (!existingData.isEmpty()) {
                System.out.println("数据库已有数据，跳过导入");
                return;
            }

            // 从CSV文件导入数据
            List<EnvSensorData> dataList = loadCsvData();
            if (!dataList.isEmpty()) {
                sensorDataService.batchSaveSensorData(dataList);
                System.out.println("成功导入 " + dataList.size() + " 条传感器数据");
            } else {
                System.out.println("CSV文件为空或不存在，跳过导入");
            }
        } catch (Exception e) {
            System.out.println("数据导入失败: " + e.getMessage());
        }

        System.out.println("========================================");
    }

    /**
     * 从CSV文件加载数据
     */
    private List<EnvSensorData> loadCsvData() throws Exception {
        List<EnvSensorData> dataList = new ArrayList<>();

        // 尝试从classpath加载
        ClassPathResource resource = new ClassPathResource("data/livestock_environment_data.csv");
        if (!resource.exists()) {
            System.out.println("CSV文件不存在: data/livestock_environment_data.csv");
            return dataList;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            int count = 0;
            int maxRecords = 1000; // 限制导入数量，避免启动太慢

            while ((line = reader.readLine()) != null && count < maxRecords) {
                // 跳过BOM和表头
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // 跳过空行
                if (line.trim().isEmpty()) continue;

                try {
                    // 解析CSV行
                    String[] parts = line.split(",");
                    if (parts.length < 6) continue;

                    EnvSensorData data = new EnvSensorData();
                    data.setTimestamp(LocalDateTime.parse(parts[0].trim(), formatter));
                    data.setBarnId(parseBarnId(parts[1].trim()));
                    data.setSensorId(parts[2].trim());
                    data.setTemperature(new BigDecimal(parts[3].trim()));
                    data.setHumidity(new BigDecimal(parts[4].trim()));
                    data.setAmmoniaLevel(new BigDecimal(parts[5].trim()));

                    dataList.add(data);
                    count++;
                } catch (Exception e) {
                    // 跳过解析失败的行
                    continue;
                }
            }
        }

        return dataList;
    }

    /**
     * 解析棚舍ID（BARN001 -> 1）
     */
    private Long parseBarnId(String barnId) {
        try {
            return Long.parseLong(barnId.replace("BARN", "").replaceFirst("^0+", ""));
        } catch (Exception e) {
            return 1L;
        }
    }
}
