package com.smartfarming.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("env_sensor_data")
public class EnvSensorData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sensorId;

    private Long barnId;

    private BigDecimal temperature;

    private BigDecimal humidity;

    private BigDecimal ammoniaLevel;

    private LocalDateTime timestamp;
}
