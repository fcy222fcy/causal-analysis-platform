package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarming.entity.TraceabilityReport;
import com.smartfarming.mapper.TraceabilityReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TraceabilityService {

    @Autowired
    private TraceabilityReportMapper reportMapper;

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

    public List<TraceabilityReport> getReportsByBarnId(Long barnId) {
        LambdaQueryWrapper<TraceabilityReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceabilityReport::getBarnId, barnId)
               .orderByDesc(TraceabilityReport::getCreateTime);
        return reportMapper.selectList(wrapper);
    }

    public TraceabilityReport getReportById(Long reportId) {
        return reportMapper.selectById(reportId);
    }
}
