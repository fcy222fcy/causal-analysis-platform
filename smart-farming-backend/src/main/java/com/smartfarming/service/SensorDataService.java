package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarming.entity.EnvSensorData;
import com.smartfarming.mapper.EnvSensorDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorDataService {

    @Autowired
    private EnvSensorDataMapper sensorDataMapper;

    public void saveSensorData(EnvSensorData data) {
        sensorDataMapper.insert(data);
    }

    public void batchSaveSensorData(List<EnvSensorData> dataList) {
        for (EnvSensorData data : dataList) {
            sensorDataMapper.insert(data);
        }
    }

    public List<EnvSensorData> getSensorDataByBarnId(Long barnId) {
        LambdaQueryWrapper<EnvSensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvSensorData::getBarnId, barnId)
               .orderByDesc(EnvSensorData::getTimestamp);
        return sensorDataMapper.selectList(wrapper);
    }

    public List<EnvSensorData> getSensorDataByTimeRange(Long barnId, String startTime, String endTime) {
        LambdaQueryWrapper<EnvSensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvSensorData::getBarnId, barnId)
               .ge(EnvSensorData::getTimestamp, startTime)
               .le(EnvSensorData::getTimestamp, endTime)
               .orderByAsc(EnvSensorData::getTimestamp);
        return sensorDataMapper.selectList(wrapper);
    }
}
