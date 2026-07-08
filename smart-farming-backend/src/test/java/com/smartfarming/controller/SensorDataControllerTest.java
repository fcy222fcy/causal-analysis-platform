package com.smartfarming.controller;

import com.smartfarming.entity.EnvSensorData;
import com.smartfarming.mapper.EnvSensorDataMapper;
import com.smartfarming.service.SensorDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorDataControllerTest {

    @Mock
    private SensorDataService sensorDataService;

    @Mock
    private EnvSensorDataMapper sensorDataMapper;

    @InjectMocks
    private SensorDataController sensorDataController;

    @Test
    void testGetSensorData() {
        Map<String, Object> result = sensorDataController.getSensorData(1L, 1, 10);
        assertNotNull(result);
        assertTrue(result.containsKey("list"));
        assertTrue(result.containsKey("total"));
    }

    @Test
    void testSaveSensorData() {
        EnvSensorData data = createTestData();
        doNothing().when(sensorDataService).saveSensorData(any());

        assertDoesNotThrow(() -> {
            sensorDataController.saveSensorData(data);
        });

        verify(sensorDataService, times(1)).saveSensorData(any());
    }

    @Test
    void testGetSensorList() {
        var result = sensorDataController.getSensorList(1L);
        assertNotNull(result);
    }

    @Test
    void testGetSensorStats() {
        Map<String, Object> stats = sensorDataController.getSensorStats();
        assertNotNull(stats);
    }

    private EnvSensorData createTestData() {
        EnvSensorData data = new EnvSensorData();
        data.setSensorId("S001");
        data.setBarnId(1L);
        data.setTemperature(new BigDecimal("25.5"));
        data.setHumidity(new BigDecimal("65.0"));
        data.setAmmoniaLevel(new BigDecimal("15.0"));
        data.setTimestamp(LocalDateTime.now());
        return data;
    }
}
