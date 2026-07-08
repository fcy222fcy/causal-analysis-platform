package com.smartfarming.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.EnvSensorData;
import com.smartfarming.mapper.EnvSensorDataMapper;
import com.smartfarming.service.SensorDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensor")
@Tag(name = "传感器数据", description = "传感器数据管理接口")
public class SensorDataController {

    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private EnvSensorDataMapper sensorDataMapper;

    @PostMapping("/data")
    @Operation(summary = "保存传感器数据")
    public void saveSensorData(@RequestBody EnvSensorData data) {
        sensorDataService.saveSensorData(data);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量导入传感器数据")
    public void batchImport(@RequestBody List<EnvSensorData> dataList) {
        sensorDataService.batchSaveSensorData(dataList);
    }

    @PostMapping("/import")
    @Operation(summary = "导入CSV文件")
    public Map<String, Object> importCsvFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<EnvSensorData> dataList = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            while ((line = reader.readLine()) != null) {
                // 跳过BOM和表头
                if (isHeader) {
                    isHeader = false;
                    // 检查是否包含BOM
                    if (line.startsWith("﻿")) {
                        line = line.substring(1);
                    }
                    continue;
                }

                // 跳过空行
                if (line.trim().isEmpty()) continue;

                try {
                    // 解析CSV行
                    String[] parts = line.split(",");
                    if (parts.length < 6) {
                        failCount++;
                        continue;
                    }

                    EnvSensorData data = new EnvSensorData();
                    data.setTimestamp(LocalDateTime.parse(parts[0].trim(), formatter));
                    data.setBarnId(parseBarnId(parts[1].trim()));
                    data.setSensorId(parts[2].trim());
                    data.setTemperature(new BigDecimal(parts[3].trim()));
                    data.setHumidity(new BigDecimal(parts[4].trim()));
                    data.setAmmoniaLevel(new BigDecimal(parts[5].trim()));

                    dataList.add(data);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                }
            }

            // 批量保存到数据库
            if (!dataList.isEmpty()) {
                sensorDataService.batchSaveSensorData(dataList);
            }

            result.put("success", true);
            result.put("message", "导入完成");
            result.put("totalRows", successCount + failCount);
            result.put("successCount", successCount);
            result.put("failCount", failCount);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
        }

        return result;
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

    @GetMapping("/data")
    @Operation(summary = "获取传感器数据（支持分页）")
    public Map<String, Object> getSensorData(
            @RequestParam(required = false) Long barnId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        LambdaQueryWrapper<EnvSensorData> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(EnvSensorData::getBarnId, barnId);
        }
        wrapper.orderByDesc(EnvSensorData::getTimestamp);
        Page<EnvSensorData> pageParam = new Page<>(page, size);
        IPage<EnvSensorData> result = sensorDataMapper.selectPage(pageParam, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @GetMapping("/data/all")
    @Operation(summary = "获取所有传感器数据（不分页）")
    public List<EnvSensorData> getAllSensorData(@RequestParam(required = false) Long barnId) {
        LambdaQueryWrapper<EnvSensorData> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(EnvSensorData::getBarnId, barnId);
        }
        wrapper.orderByDesc(EnvSensorData::getTimestamp);
        wrapper.last("LIMIT 200");
        return sensorDataMapper.selectList(wrapper);
    }

    @GetMapping("/data/range")
    @Operation(summary = "按时间范围获取传感器数据")
    public List<EnvSensorData> getSensorDataByTimeRange(
            @RequestParam(required = false) Long barnId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return sensorDataService.getSensorDataByTimeRange(barnId != null ? barnId : 1L, startTime, endTime);
    }

    @GetMapping("/list")
    @Operation(summary = "获取传感器列表")
    public List<Map<String, Object>> getSensorList(@RequestParam(required = false) Long barnId) {
        List<Map<String, Object>> result = new ArrayList<>();
        QueryWrapper<EnvSensorData> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT sensor_id, barn_id");
        if (barnId != null) {
            wrapper.eq("barn_id", barnId);
        }
        List<Object> objs = sensorDataMapper.selectObjs(wrapper);
        for (int i = 0; i < Math.min(objs.size(), 20); i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("sensorId", "SENSOR-" + (i + 1));
            m.put("barnId", barnId != null ? barnId : ((i % 5) + 1));
            m.put("name", "传感器 " + (i + 1));
            m.put("type", i % 3 == 0 ? "temperature" : i % 3 == 1 ? "humidity" : "ammonia");
            result.add(m);
        }
        if (result.isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                Map<String, Object> m = new HashMap<>();
                m.put("sensorId", "SENSOR-" + i);
                m.put("barnId", ((i - 1) % 5) + 1);
                m.put("name", "传感器 " + i);
                m.put("type", i % 3 == 0 ? "temperature" : i % 3 == 1 ? "humidity" : "ammonia");
                result.add(m);
            }
        }
        return result;
    }

    @GetMapping("/stats")
    @Operation(summary = "获取传感器统计数据（Dashboard用）")
    public Map<String, Object> getSensorStats() {
        Map<String, Object> stats = new HashMap<>();
        QueryWrapper<EnvSensorData> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT sensor_id");
        Long sensorCount = (long) sensorDataMapper.selectObjs(wrapper).size();
        stats.put("sensorCount", sensorCount);
        stats.put("totalRecords", sensorDataMapper.selectCount(null));
        return stats;
    }

    @GetMapping("/trend")
    @Operation(summary = "获取环境趋势数据（Dashboard图表用）")
    public Map<String, Object> getTrendData(@RequestParam(required = false) Long barnId,
                                            @RequestParam(defaultValue = "50") Integer limit) {
        LambdaQueryWrapper<EnvSensorData> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(EnvSensorData::getBarnId, barnId);
        }
        wrapper.orderByAsc(EnvSensorData::getTimestamp);
        wrapper.last("LIMIT " + limit);
        List<EnvSensorData> dataList = sensorDataMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        List<String> timestamps = new ArrayList<>();
        List<BigDecimal> temps = new ArrayList<>();
        List<BigDecimal> humiditys = new ArrayList<>();
        List<BigDecimal> ammonias = new ArrayList<>();

        for (EnvSensorData d : dataList) {
            timestamps.add(d.getTimestamp() != null ? d.getTimestamp().toString().substring(11, 19) : "");
            temps.add(d.getTemperature());
            humiditys.add(d.getHumidity());
            ammonias.add(d.getAmmoniaLevel());
        }
        result.put("timestamps", timestamps);
        result.put("temperature", temps);
        result.put("humidity", humiditys);
        result.put("ammoniaLevel", ammonias);
        return result;
    }
}
