package com.smartfarming.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BehaviorDTO {
    private Long barnId;
    private String behaviorType;
    private BigDecimal confidenceScore;
}
