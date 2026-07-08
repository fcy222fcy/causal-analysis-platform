package com.smartfarming.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartfarming.entity.*;
import com.smartfarming.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "仪表盘", description = "仪表盘汇总统计接口")
public class DashboardController {

    @Autowired
    private EnvSensorDataMapper sensorDataMapper;

    @Autowired
    private AnomalyEventMapper anomalyEventMapper;

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    @Autowired
    private CausalRelationMapper causalRelationMapper;

    @Autowired
    private TraceabilityReportMapper reportMapper;

    @Autowired
    private AnimalBehaviorMapper behaviorMapper;

    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘汇总统计")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        QueryWrapper<EnvSensorData> sWrapper = new QueryWrapper<>();
        sWrapper.select("DISTINCT sensor_id");
        Long sensorCount = (long) sensorDataMapper.selectObjs(sWrapper).size();
        stats.put("sensorCount", sensorCount);

        stats.put("anomalyCount", anomalyEventMapper.selectCount(null));
        stats.put("alarmCount", alarmRecordMapper.selectCount(null));
        stats.put("causalCount", causalRelationMapper.selectCount(null));
        stats.put("reportCount", reportMapper.selectCount(null));
        stats.put("behaviorCount", behaviorMapper.selectCount(null));
        stats.put("barnCount", 5L);

        LambdaQueryWrapper<AlarmRecord> pendingW = new LambdaQueryWrapper<>();
        pendingW.eq(AlarmRecord::getStatus, "pending");
        stats.put("pendingAlarmCount", alarmRecordMapper.selectCount(pendingW));

        return stats;
    }

    @GetMapping("/trend")
    @Operation(summary = "获取环境趋势数据（简化版）")
    public Map<String, Object> getTrend(@RequestParam(defaultValue = "100") Integer limit) {
        // 查询最新的数据（倒序取limit条，再正序展示）
        LambdaQueryWrapper<EnvSensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EnvSensorData::getTimestamp);
        wrapper.last("LIMIT " + limit);
        List<EnvSensorData> dataList = sensorDataMapper.selectList(wrapper);
        // 反转为正序（从旧到新）
        java.util.Collections.reverse(dataList);

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

    @GetMapping("/anomaly-distribution")
    @Operation(summary = "获取异常分布饼图数据")
    public Map<String, Object> getAnomalyDistribution() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();

        LambdaQueryWrapper<AnomalyEvent> envW = new LambdaQueryWrapper<>();
        envW.eq(AnomalyEvent::getEventType, "environmental");
        Long envCount = anomalyEventMapper.selectCount(envW);
        data.add(Map.of("name", "环境异常", "value", envCount));

        LambdaQueryWrapper<AnomalyEvent> behW = new LambdaQueryWrapper<>();
        behW.eq(AnomalyEvent::getEventType, "behavioral");
        Long behCount = anomalyEventMapper.selectCount(behW);
        data.add(Map.of("name", "行为异常", "value", behCount));

        LambdaQueryWrapper<AnomalyEvent> compW = new LambdaQueryWrapper<>();
        compW.eq(AnomalyEvent::getEventType, "composite");
        Long compCount = anomalyEventMapper.selectCount(compW);
        data.add(Map.of("name", "复合异常", "value", compCount));

        Long total = anomalyEventMapper.selectCount(null);
        Long other = Math.max(0L, total - envCount - behCount - compCount);
        data.add(Map.of("name", "其他", "value", other));

        result.put("data", data);
        return result;
    }
}
