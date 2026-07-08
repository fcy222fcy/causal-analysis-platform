package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.TraceabilityReport;
import com.smartfarming.mapper.TraceabilityReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private TraceabilityReportMapper reportMapper;

    /**
     * 分页查询报告
     */
    public Page<TraceabilityReport> getReports(int page, int size, Long barnId) {
        Page<TraceabilityReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TraceabilityReport> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(TraceabilityReport::getBarnId, barnId);
        }
        wrapper.orderByDesc(TraceabilityReport::getCreateTime);
        return reportMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 全量查询报告
     */
    public List<TraceabilityReport> getAllReports(Long barnId) {
        LambdaQueryWrapper<TraceabilityReport> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(TraceabilityReport::getBarnId, barnId);
        }
        wrapper.orderByDesc(TraceabilityReport::getCreateTime);
        wrapper.last("LIMIT 200");
        return reportMapper.selectList(wrapper);
    }

    /**
     * 获取报告详情
     */
    public TraceabilityReport getReportDetail(Long reportId) {
        return reportMapper.selectById(reportId);
    }

    /**
     * 生成溯源报告
     */
    public TraceabilityReport generateReport(Long eventId, Long barnId, String causeChain, String reportContent) {
        TraceabilityReport report = new TraceabilityReport();
        report.setEventId(eventId);
        report.setBarnId(barnId);
        report.setCauseChain(causeChain);
        report.setReportContent(reportContent);
        report.setCreateTime(LocalDateTime.now());
        reportMapper.insert(report);
        return report;
    }
}
