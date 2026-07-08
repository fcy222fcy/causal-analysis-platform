package com.smartfarming.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.TraceabilityReport;
import com.smartfarming.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "报告管理", description = "溯源报告的查询与生成")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(summary = "分页查询报告列表")
    @GetMapping("/list")
    public Map<String, Object> getReports(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "棚舍ID") @RequestParam(required = false) Long barnId) {
        Page<TraceabilityReport> result = reportService.getReports(page, size, barnId);
        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @Operation(summary = "全量查询报告")
    @GetMapping("/all")
    public List<TraceabilityReport> getAllReports(
            @Parameter(description = "棚舍ID") @RequestParam(required = false) Long barnId) {
        return reportService.getAllReports(barnId);
    }

    @Operation(summary = "获取报告详情")
    @GetMapping("/{reportId}")
    public TraceabilityReport getReportDetail(@PathVariable Long reportId) {
        return reportService.getReportDetail(reportId);
    }

    @Operation(summary = "生成溯源报告")
    @PostMapping("/generate")
    public TraceabilityReport generateReport(
            @Parameter(description = "异常事件ID") @RequestParam(required = false) Long eventId,
            @Parameter(description = "棚舍ID") @RequestParam(required = false) Long barnId,
            @RequestBody Map<String, String> body) {
        return reportService.generateReport(
                eventId,
                barnId,
                body.get("causeChain"),
                body.get("reportContent")
        );
    }
}
