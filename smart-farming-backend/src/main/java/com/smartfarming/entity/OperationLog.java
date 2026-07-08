package com.smartfarming.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private Long userId;

    private String operation;

    private String method;

    private String params;

    private String ip;

    private LocalDateTime createTime;
}
