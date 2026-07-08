package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarming.entity.AlarmRecord;
import com.smartfarming.mapper.AlarmRecordMapper;
import com.smartfarming.websocket.AlarmWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlarmService {

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    @Autowired
    private AlarmWebSocket alarmWebSocket;

    public AlarmRecord createAlarm(AlarmRecord alarm) {
        alarm.setStatus("pending");
        alarm.setCreateTime(LocalDateTime.now());
        alarmRecordMapper.insert(alarm);

        // 通过 WebSocket 推送高危告警
        alarmWebSocket.sendAlarm(alarm);

        return alarm;
    }

    public List<AlarmRecord> getAlarmsByBarnId(Long barnId) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRecord::getBarnId, barnId)
               .orderByDesc(AlarmRecord::getCreateTime);
        return alarmRecordMapper.selectList(wrapper);
    }

    public List<AlarmRecord> getPendingAlarms() {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRecord::getStatus, "pending")
               .orderByDesc(AlarmRecord::getCreateTime);
        return alarmRecordMapper.selectList(wrapper);
    }

    public void acknowledgeAlarm(Long alarmId, Long handlerId) {
        AlarmRecord alarm = alarmRecordMapper.selectById(alarmId);
        if (alarm != null) {
            alarm.setStatus("acknowledged");
            alarm.setHandlerId(handlerId);
            alarmRecordMapper.updateById(alarm);
        }
    }

    public void resolveAlarm(Long alarmId, Long handlerId) {
        AlarmRecord alarm = alarmRecordMapper.selectById(alarmId);
        if (alarm != null) {
            alarm.setStatus("resolved");
            alarm.setHandleTime(LocalDateTime.now());
            alarm.setHandlerId(handlerId);
            alarmRecordMapper.updateById(alarm);
        }
    }

    public Map<String, Object> getAlarmStats() {
        Map<String, Object> stats = new HashMap<>();
        LambdaQueryWrapper<AlarmRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(AlarmRecord::getStatus, "pending");
        stats.put("pending", alarmRecordMapper.selectCount(pendingWrapper));

        LambdaQueryWrapper<AlarmRecord> acknowledgedWrapper = new LambdaQueryWrapper<>();
        acknowledgedWrapper.eq(AlarmRecord::getStatus, "acknowledged");
        stats.put("acknowledged", alarmRecordMapper.selectCount(acknowledgedWrapper));

        LambdaQueryWrapper<AlarmRecord> resolvedWrapper = new LambdaQueryWrapper<>();
        resolvedWrapper.eq(AlarmRecord::getStatus, "resolved");
        stats.put("resolved", alarmRecordMapper.selectCount(resolvedWrapper));

        stats.put("total", alarmRecordMapper.selectCount(null));
        return stats;
    }
}
