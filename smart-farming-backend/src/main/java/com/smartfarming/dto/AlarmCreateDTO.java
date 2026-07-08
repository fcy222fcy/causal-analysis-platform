package com.smartfarming.dto;

import lombok.Data;

@Data
public class AlarmCreateDTO {
    private Long eventId;
    private Long barnId;
    private String alarmType;
    private String alarmLevel;
    private String alarmContent;
}
