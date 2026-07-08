package com.smartfarming.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("anomaly_event")
public class AnomalyEvent {

    @TableId(type = IdType.AUTO)
    private Long eventId;

    private Long barnId;

    private String eventType;

    private String severityLevel;

    private String description;

    private LocalDateTime timestamp;
}
