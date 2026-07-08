package com.smartfarming.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("causal_relation")
public class CausalRelation {

    @TableId(type = IdType.AUTO)
    private Long relationId;

    private Long barnId;

    private String causeVariable;

    private String effectVariable;

    private BigDecimal correlationScore;

    private BigDecimal causalStrength;

    private LocalDateTime timestamp;
}
