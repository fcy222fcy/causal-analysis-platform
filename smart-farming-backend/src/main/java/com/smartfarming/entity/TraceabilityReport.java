package com.smartfarming.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("traceability_report")
public class TraceabilityReport {

    @TableId(type = IdType.AUTO)
    private Long reportId;

    private Long eventId;

    private Long barnId;

    private String causeChain;

    private String reportContent;

    private LocalDateTime createTime;
}
