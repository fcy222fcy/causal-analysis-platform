package com.smartfarming.websocket;

import com.smartfarming.entity.AlarmRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 告警推送服务
 * 将高危告警实时推送到前端
 */
@Component
public class AlarmWebSocket {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 推送告警到前端
     * 前端订阅 /topic/alarms 即可接收
     */
    public void sendAlarm(AlarmRecord alarm) {
        if (alarm != null && "high".equals(alarm.getAlarmLevel())) {
            messagingTemplate.convertAndSend("/topic/alarms", alarm);
        }
    }

    /**
     * 推送任意消息到指定主题
     */
    public void sendMessage(String destination, Object message) {
        messagingTemplate.convertAndSend(destination, message);
    }
}
