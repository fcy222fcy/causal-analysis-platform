package com.smartfarming.dto;

import lombok.Data;

@Data
public class SensorDataDTO {
    private String sensorId;
    private Long barnId;
    private Double temperature;
    private Double humidity;
    private Double ammoniaLevel;
}
