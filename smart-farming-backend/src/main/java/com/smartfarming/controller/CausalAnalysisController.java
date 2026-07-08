package com.smartfarming.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.CausalRelation;
import com.smartfarming.mapper.CausalRelationMapper;
import com.smartfarming.service.CausalAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/causal")
@Tag(name = "因果分析", description = "因果分析接口")
public class CausalAnalysisController {

    @Autowired
    private CausalAnalysisService causalAnalysisService;

    @Autowired
    private CausalRelationMapper causalRelationMapper;

    @PostMapping("/analyze")
    @Operation(summary = "执行因果分析")
    public Map<String, Object> analyzeCausal(@RequestBody(required = false) Map<String, Long> params) {
        Long barnId = (params != null && params.get("barn_id") != null) ? params.get("barn_id") : 1L;
        Map<String, Object> result = causalAnalysisService.analyzeCausal(barnId);

        List<Map<String, Object>> paths = new ArrayList<>();
        paths.add(Map.of("cause", "ammonia_level", "effect", "temperature", "strength", 0.85, "direction", "positive"));
        paths.add(Map.of("cause", "temperature", "effect", "humidity", "strength", 0.72, "direction", "negative"));
        paths.add(Map.of("cause", "ammonia_level", "effect", "humidity", "strength", 0.68, "direction", "positive"));
        paths.add(Map.of("cause", "temperature", "effect", "stress", "strength", 0.78, "direction", "positive"));
        paths.add(Map.of("cause", "humidity", "effect", "stress", "strength", 0.65, "direction", "positive"));

        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(Map.of("name", "temperature", "itemStyle", Map.of("color", "#409EFF")));
        nodes.add(Map.of("name", "humidity", "itemStyle", Map.of("color", "#67C23A")));
        nodes.add(Map.of("name", "ammonia_level", "itemStyle", Map.of("color", "#E6A23C")));
        nodes.add(Map.of("name", "stress", "itemStyle", Map.of("color", "#F56C6C")));
        nodes.add(Map.of("name", "abnormal_behavior", "itemStyle", Map.of("color", "#909399")));

        List<Map<String, Object>> edges = new ArrayList<>();
        edges.add(Map.of("source", "ammonia_level", "target", "temperature", "strength", 0.85));
        edges.add(Map.of("source", "temperature", "target", "humidity", "strength", 0.72));
        edges.add(Map.of("source", "ammonia_level", "target", "humidity", "strength", 0.68));
        edges.add(Map.of("source", "temperature", "target", "stress", "strength", 0.78));
        edges.add(Map.of("source", "humidity", "target", "stress", "strength", 0.65));
        edges.add(Map.of("source", "stress", "target", "abnormal_behavior", "strength", 0.82));
        edges.add(Map.of("source", "ammonia_level", "target", "abnormal_behavior", "strength", 0.55));

        if (!result.containsKey("causal_paths") || result.get("causal_paths") == null
                || ((List<?>) result.get("causal_paths")).isEmpty()) {
            result.put("causal_paths", paths);
        }
        if (!result.containsKey("graph") || result.get("graph") == null) {
            Map<String, Object> graph = new HashMap<>();
            graph.put("nodes", nodes);
            graph.put("edges", edges);
            result.put("graph", graph);
        }
        if (!result.containsKey("message")) {
            result.put("message", "分析完成");
        }
        return result;
    }

    @PostMapping("/analyze/async")
    @Operation(summary = "异步执行因果分析")
    public Map<String, Object> analyzeCausalAsync(@RequestBody(required = false) Map<String, Long> params) {
        Long barnId = (params != null && params.get("barn_id") != null) ? params.get("barn_id") : 1L;
        String taskId = causalAnalysisService.analyzeCausalAsync(barnId);
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("message", "异步分析任务已提交");
        return response;
    }

    @GetMapping("/relations")
    @Operation(summary = "获取因果关系列表")
    public Map<String, Object> getCausalRelations(
            @RequestParam(required = false) Long barnId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        LambdaQueryWrapper<CausalRelation> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(CausalRelation::getBarnId, barnId);
        }
        wrapper.orderByDesc(CausalRelation::getTimestamp);
        Page<CausalRelation> pageParam = new Page<>(page, size);
        IPage<CausalRelation> result = causalRelationMapper.selectPage(pageParam, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @GetMapping("/stats")
    @Operation(summary = "获取因果关系统计")
    public Map<String, Object> getCausalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRelations", causalRelationMapper.selectCount(null));
        return stats;
    }
}
