package com.smartfarming.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.AnimalBehavior;
import com.smartfarming.service.BehaviorDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/behavior")
@Tag(name = "行为数据", description = "动物行为数据接口")
public class BehaviorController {

    @Autowired
    private BehaviorDataService behaviorDataService;

    @GetMapping("/list")
    @Operation(summary = "获取行为数据（分页）")
    public Map<String, Object> getBehaviors(
            @Parameter(description = "棚舍ID") @RequestParam(required = false) Long barnId,
            @Parameter(description = "行为类型") @RequestParam(required = false) String behaviorType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "50") Integer size) {
        Page<AnimalBehavior> result = behaviorDataService.getBehaviorList(page, size, barnId, behaviorType);
        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有行为数据（不分页）")
    public List<AnimalBehavior> getAllBehaviors(
            @Parameter(description = "棚舍ID") @RequestParam(required = false) Long barnId) {
        return behaviorDataService.getAllBehaviors(barnId);
    }

    @PostMapping("/add")
    @Operation(summary = "新增行为记录")
    public AnimalBehavior addBehavior(@RequestBody AnimalBehavior behavior) {
        return behaviorDataService.addBehavior(behavior);
    }
}
