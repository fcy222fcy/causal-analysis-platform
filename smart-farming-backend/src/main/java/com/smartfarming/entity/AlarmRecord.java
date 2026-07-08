package com.smartfarming.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alarm_record")
public class AlarmRecord {

    @TableId(type = IdType.AUTO)
    private Long alarmId;

    private Long eventId;

    private Long barnId;

    private String alarmType;

    private String alarmLevel;

    private String alarmContent;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime handleTime;

    private Long handlerId;
}
