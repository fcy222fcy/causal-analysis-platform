package com.smartfarming.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.AlarmRecord;
import com.smartfarming.mapper.AlarmRecordMapper;
import com.smartfarming.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alarm")
@Tag(name = "告警管理", description = "告警管理接口")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    @PostMapping("/create")
    @Operation(summary = "创建告警")
    public AlarmRecord createAlarm(@RequestBody AlarmRecord alarm) {
        return alarmService.createAlarm(alarm);
    }

    @GetMapping("/list")
    @Operation(summary = "获取告警列表（支持分页）")
    public Map<String, Object> getAlarms(
            @RequestParam(required = false) Long barnId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(AlarmRecord::getBarnId, barnId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AlarmRecord::getStatus, status);
        }
        wrapper.orderByDesc(AlarmRecord::getCreateTime);
        Page<AlarmRecord> pageParam = new Page<>(page, size);
        IPage<AlarmRecord> result = alarmRecordMapper.selectPage(pageParam, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有告警（不分页）")
    public List<AlarmRecord> getAllAlarms(@RequestParam(required = false) Long barnId) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(AlarmRecord::getBarnId, barnId);
        }
        wrapper.orderByDesc(AlarmRecord::getCreateTime);
        wrapper.last("LIMIT 200");
        return alarmRecordMapper.selectList(wrapper);
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待处理告警")
    public List<AlarmRecord> getPendingAlarms() {
        return alarmService.getPendingAlarms();
    }

    @PutMapping("/{alarmId}/acknowledge")
    @Operation(summary = "确认告警")
    public Map<String, Object> acknowledgeAlarm(@PathVariable Long alarmId,
                                                 @RequestParam(required = false) Long handlerId) {
        alarmService.acknowledgeAlarm(alarmId, handlerId != null ? handlerId : 1L);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "确认成功");
        result.put("alarmId", alarmId);
        return result;
    }

    @PutMapping("/{alarmId}/resolve")
    @Operation(summary = "解决告警")
    public Map<String, Object> resolveAlarm(@PathVariable Long alarmId,
                                             @RequestParam(required = false) Long handlerId) {
        alarmService.resolveAlarm(alarmId, handlerId != null ? handlerId : 1L);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "解决成功");
        result.put("alarmId", alarmId);
        return result;
    }

    @GetMapping("/stats")
    @Operation(summary = "获取告警统计")
    public Map<String, Object> getAlarmStats() {
        return alarmService.getAlarmStats();
    }
}
