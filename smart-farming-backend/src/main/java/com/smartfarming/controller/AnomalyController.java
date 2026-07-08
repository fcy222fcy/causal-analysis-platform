package com.smartfarming.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.AnomalyEvent;
import com.smartfarming.mapper.AnomalyEventMapper;
import com.smartfarming.service.AnomalyDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anomaly")
@Tag(name = "异常检测", description = "异常检测接口")
public class AnomalyController {

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @Autowired
    private AnomalyEventMapper anomalyEventMapper;

    @PostMapping("/detect")
    @Operation(summary = "执行异常检测")
    public Map<String, Object> detectAnomalies(@RequestParam(required = false) Long barnId) {
        Map<String, Object> result = new HashMap<>();
        int totalDetected = 0;
        if (barnId != null) {
            List<AnomalyEvent> list = anomalyDetectionService.detectAnomalies(barnId);
            totalDetected = list.size();
        } else {
            for (long b = 1; b <= 5; b++) {
                totalDetected += anomalyDetectionService.detectAnomalies(b).size();
            }
        }
        result.put("detected", totalDetected);
        result.put("message", "检测完成");
        return result;
    }

    @GetMapping("/list")
    @Operation(summary = "获取异常事件列表（分页）")
    public Map<String, Object> getAnomalies(
            @RequestParam(required = false) Long barnId,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        LambdaQueryWrapper<AnomalyEvent> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(AnomalyEvent::getBarnId, barnId);
        }
        if (eventType != null && !eventType.isEmpty()) {
            wrapper.eq(AnomalyEvent::getEventType, eventType);
        }
        wrapper.orderByDesc(AnomalyEvent::getTimestamp);
        Page<AnomalyEvent> pageParam = new Page<>(page, size);
        IPage<AnomalyEvent> result = anomalyEventMapper.selectPage(pageParam, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有异常事件（不分页）")
    public List<AnomalyEvent> getAllAnomalies(@RequestParam(required = false) Long barnId) {
        LambdaQueryWrapper<AnomalyEvent> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(AnomalyEvent::getBarnId, barnId);
        }
        wrapper.orderByDesc(AnomalyEvent::getTimestamp);
        wrapper.last("LIMIT 200");
        return anomalyEventMapper.selectList(wrapper);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取异常统计（Dashboard用）")
    public Map<String, Object> getAnomalyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", anomalyEventMapper.selectCount(null));

        LambdaQueryWrapper<AnomalyEvent> envWrapper = new LambdaQueryWrapper<>();
        envWrapper.eq(AnomalyEvent::getEventType, "environmental");
        stats.put("environmental", anomalyEventMapper.selectCount(envWrapper));

        LambdaQueryWrapper<AnomalyEvent> behWrapper = new LambdaQueryWrapper<>();
        behWrapper.eq(AnomalyEvent::getEventType, "behavioral");
        stats.put("behavioral", anomalyEventMapper.selectCount(behWrapper));

        LambdaQueryWrapper<AnomalyEvent> compWrapper = new LambdaQueryWrapper<>();
        compWrapper.eq(AnomalyEvent::getEventType, "composite");
        stats.put("composite", anomalyEventMapper.selectCount(compWrapper));
        return stats;
    }
}
