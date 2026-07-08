package com.smartfarming.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReportGenerateDTO {
    private List<Long> eventIds;
    private Long barnId;
    private String causeChain;
    private String reportContent;
}
