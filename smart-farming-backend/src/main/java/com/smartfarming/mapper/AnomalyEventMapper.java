package com.smartfarming.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfarming.entity.AnomalyEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnomalyEventMapper extends BaseMapper<AnomalyEvent> {
}
