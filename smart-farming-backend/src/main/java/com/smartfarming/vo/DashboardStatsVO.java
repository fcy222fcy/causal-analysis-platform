package com.smartfarming.vo;

import lombok.Data;

@Data
public class DashboardStatsVO {
    private Long sensorCount;
    private Long anomalyCount;
    private Long alarmCount;
    private Long causalCount;
    private Long pendingAlarmCount;
    private Long reportCount;
}
