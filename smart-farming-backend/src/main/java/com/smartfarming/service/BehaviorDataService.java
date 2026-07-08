package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarming.entity.AnimalBehavior;
import com.smartfarming.mapper.AnimalBehaviorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BehaviorDataService {

    @Autowired
    private AnimalBehaviorMapper behaviorMapper;

    /**
     * 分页查询行为数据
     */
    public Page<AnimalBehavior> getBehaviorList(int page, int size, Long barnId, String behaviorType) {
        Page<AnimalBehavior> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AnimalBehavior> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(AnimalBehavior::getBarnId, barnId);
        }
        if (behaviorType != null && !behaviorType.isEmpty()) {
            wrapper.eq(AnimalBehavior::getBehaviorType, behaviorType);
        }
        wrapper.orderByDesc(AnimalBehavior::getTimestamp);
        return behaviorMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 全量查询行为数据
     */
    public List<AnimalBehavior> getAllBehaviors(Long barnId) {
        LambdaQueryWrapper<AnimalBehavior> wrapper = new LambdaQueryWrapper<>();
        if (barnId != null) {
            wrapper.eq(AnimalBehavior::getBarnId, barnId);
        }
        wrapper.orderByDesc(AnimalBehavior::getTimestamp);
        wrapper.last("LIMIT 200");
        return behaviorMapper.selectList(wrapper);
    }

    /**
     * 新增行为记录
     */
    public AnimalBehavior addBehavior(AnimalBehavior behavior) {
        if (behavior.getTimestamp() == null) {
            behavior.setTimestamp(LocalDateTime.now());
        }
        behaviorMapper.insert(behavior);
        return behavior;
    }
}
