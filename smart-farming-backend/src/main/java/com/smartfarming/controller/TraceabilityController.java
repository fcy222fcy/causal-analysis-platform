package com.smartfarming.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.TraceabilityReport;
import com.smartfarming.mapper.TraceabilityReportMapper;
import com.smartfarming.service.TraceabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/traceability")
@Tag(name = "溯源分析", description = "溯源分析接口")
public class TraceabilityController {

    @Autowired
    private TraceabilityService traceabilityService;

    @Autowired
    private TraceabilityReportMapper reportMapper;

    @PostMapping("/report")
    @Operation(summary = "生成溯源报告")
    public TraceabilityReport generateReport(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long barnId,
            @RequestBody(required = false) Map<String, String> body) {
        String causeChain = (body != null) ? body.getOrDefault("causeChain", "温度异常 → 氨气升高 → 环境恶化 → 动物应激") : "";
        String reportContent = (body != null) ? body.getOrDefault("reportContent", "根据溯源分析，环境因子异常是主要原因链。") : "";
        return traceabilityService.generateReport(
                eventId != null ? eventId : 1L,
                barnId != null ? barnId : 1L,
                causeChain.isEmpty() ? "温度异常 → 氨气升高 → 环境恶化 → 动物应激" : causeChain,
                reportContent.isEmpty() ? "针对以上原因链，建议加强通风、监控温度，并定期进行消毒。" : reportContent);
    }

    @GetMapping("/reports")
    @Operation(summary = "获取溯源报告列表（分页）")
    public Map<String, Object> getReports(
            @RequestParam(required = false) Long barnId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        LambdaQueryWrapper<TraceabilityReport> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(TraceabilityReport::getBarnId, barnId);
        }
        wrapper.orderByDesc(TraceabilityReport::getCreateTime);
        Page<TraceabilityReport> pageParam = new Page<>(page, size);
        IPage<TraceabilityReport> result = reportMapper.selectPage(pageParam, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有报告（不分页）")
    public java.util.List<TraceabilityReport> getAllReports(@RequestParam(required = false) Long barnId) {
        LambdaQueryWrapper<TraceabilityReport> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(TraceabilityReport::getBarnId, barnId);
        }
        wrapper.orderByDesc(TraceabilityReport::getCreateTime);
        wrapper.last("LIMIT 200");
        return reportMapper.selectList(wrapper);
    }

    @GetMapping("/report/{reportId}")
    @Operation(summary = "获取溯源报告详情")
    public TraceabilityReport getReport(@PathVariable Long reportId) {
        return traceabilityService.getReportById(reportId);
    }
}
