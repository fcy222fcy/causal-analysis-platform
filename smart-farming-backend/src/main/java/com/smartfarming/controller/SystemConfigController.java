package com.smartfarming.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarming.entity.SystemConfig;
import com.smartfarming.mapper.SystemConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统配置", description = "系统参数配置管理")
@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    @Autowired
    private SystemConfigMapper configMapper;

    @Operation(summary = "查询所有配置")
    @GetMapping("/list")
    public List<SystemConfig> getAllConfigs() {
        return configMapper.selectList(null);
    }

    @Operation(summary = "按key查询配置")
    @GetMapping("/{key}")
    public SystemConfig getConfigByKey(@PathVariable String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        return configMapper.selectOne(wrapper);
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{key}")
    public SystemConfig updateConfig(
            @PathVariable String key,
            @RequestBody SystemConfig config) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig existing = configMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setConfigValue(config.getConfigValue());
            existing.setDescription(config.getDescription());
            configMapper.updateById(existing);
            return existing;
        }
        return null;
    }
}
