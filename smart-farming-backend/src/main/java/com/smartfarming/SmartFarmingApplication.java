package com.smartfarming;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.smartfarming.mapper")
@EnableAsync
@EnableScheduling
public class SmartFarmingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartFarmingApplication.class, args);
    }
}
