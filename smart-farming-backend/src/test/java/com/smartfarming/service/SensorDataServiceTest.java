package com.smartfarming.service;

import com.smartfarming.entity.EnvSensorData;
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
class SensorDataServiceTest {

    @Mock
    private EnvSensorDataMapper sensorDataMapper;

    @InjectMocks
    private SensorDataService sensorDataService;

    @Test
    void testSaveSensorData() {
        EnvSensorData data = createTestData();
        when(sensorDataMapper.insert(any())).thenReturn(1);

        sensorDataService.saveSensorData(data);

        verify(sensorDataMapper, times(1)).insert(any());
    }

    @Test
    void testBatchSaveSensorData() {
        List<EnvSensorData> dataList = Arrays.asList(
            createTestData(),
            createTestData(),
            createTestData()
        );
        when(sensorDataMapper.insert(any())).thenReturn(1);

        sensorDataService.batchSaveSensorData(dataList);

        verify(sensorDataMapper, times(3)).insert(any());
    }

    @Test
    void testGetSensorDataByBarnId() {
        List<EnvSensorData> mockData = Arrays.asList(createTestData(), createTestData());
        when(sensorDataMapper.selectList(any())).thenReturn(mockData);

        List<EnvSensorData> result = sensorDataService.getSensorDataByBarnId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
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
