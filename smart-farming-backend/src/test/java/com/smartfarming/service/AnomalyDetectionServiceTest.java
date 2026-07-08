package com.smartfarming.service;

import com.smartfarming.entity.AnomalyEvent;
import com.smartfarming.entity.EnvSensorData;
import com.smartfarming.mapper.AnomalyEventMapper;
import com.smartfarming.mapper.EnvSensorDataMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private EnvSensorDataMapper sensorDataMapper;

    @Mock
    private AnomalyEventMapper anomalyEventMapper;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    @Test
    void testDetectAnomaliesWithHighTemperature() {
        // 准备包含高温异常的数据
        EnvSensorData normalData = createTestData(new BigDecimal("25.0"));
        EnvSensorData abnormalData = createTestData(new BigDecimal("40.0")); // 超过35度阈值

        when(sensorDataMapper.selectList(any())).thenReturn(Arrays.asList(normalData, abnormalData));
        when(anomalyEventMapper.insert(any())).thenReturn(1);

        List<AnomalyEvent> anomalies = anomalyDetectionService.detectAnomalies(1L);

        assertNotNull(anomalies);
        assertTrue(anomalies.size() > 0);
        assertEquals("high", anomalies.get(0).getSeverityLevel());
    }

    @Test
    void testDetectAnomaliesWithNormalData() {
        EnvSensorData normalData1 = createTestData(new BigDecimal("25.0"));
        EnvSensorData normalData2 = createTestData(new BigDecimal("26.0"));

        when(sensorDataMapper.selectList(any())).thenReturn(Arrays.asList(normalData1, normalData2));

        List<AnomalyEvent> anomalies = anomalyDetectionService.detectAnomalies(1L);

        assertNotNull(anomalies);
        assertEquals(0, anomalies.size());
    }

    @Test
    void testGetAnomaliesByBarnId() {
        List<AnomalyEvent> mockAnomalies = Arrays.asList(createAnomalyEvent(), createAnomalyEvent());
        when(anomalyEventMapper.selectList(any())).thenReturn(mockAnomalies);

        List<AnomalyEvent> result = anomalyDetectionService.getAnomaliesByBarnId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    private EnvSensorData createTestData(BigDecimal temperature) {
        EnvSensorData data = new EnvSensorData();
        data.setSensorId("S001");
        data.setBarnId(1L);
        data.setTemperature(temperature);
        data.setHumidity(new BigDecimal("65.0"));
        data.setAmmoniaLevel(new BigDecimal("15.0"));
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    private AnomalyEvent createAnomalyEvent() {
        AnomalyEvent event = new AnomalyEvent();
        event.setEventId(1L);
        event.setBarnId(1L);
        event.setEventType("environmental");
        event.setSeverityLevel("high");
        event.setDescription("温度异常");
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
