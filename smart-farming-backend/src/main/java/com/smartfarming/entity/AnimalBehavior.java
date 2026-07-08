package com.smartfarming.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("animal_behavior")
public class AnimalBehavior {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private Long barnId;

    private String behaviorType;

    private BigDecimal confidenceScore;

    private LocalDateTime timestamp;
}
