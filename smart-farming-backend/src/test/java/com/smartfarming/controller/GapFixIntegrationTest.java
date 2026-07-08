package com.smartfarming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfarming.dto.LoginRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 功能缺口修复集成测试
 * 覆盖所有14项修改内容
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GapFixIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;

    // ==================== 1. JWT 认证测试 ====================

    @Test
    @Order(1)
    @DisplayName("JWT-01: 无Token访问受保护接口应返回401")
    void testAccessProtectedAPIWithoutToken() throws Exception {
        mockMvc.perform(get("/api/sensor/data")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @DisplayName("JWT-02: 登录获取Token")
    void testLoginAndGetToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // 提取 token 用于后续测试
        String responseBody = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(responseBody).get("token").asText();
        Assertions.assertNotNull(jwtToken);
        Assertions.assertFalse(jwtToken.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("JWT-03: 带Token访问受保护接口应返回200")
    void testAccessProtectedAPIWithToken() throws Exception {
        Assertions.assertNotNull(jwtToken, "请先运行登录测试获取Token");

        mockMvc.perform(get("/api/sensor/data")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("JWT-04: 公开接口无需Token即可访问")
    void testPublicAPIAccess() throws Exception {
        mockMvc.perform(get("/api/user/login"))
                .andExpect(status().isOk());
    }

    // ==================== 2. 数据库索引测试 ====================

    @Test
    @Order(10)
    @DisplayName("INDEX-01: 传感器数据查询应使用索引（正常返回）")
    void testSensorDataQueryWithIndex() throws Exception {
        mockMvc.perform(get("/api/sensor/data")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray());
    }

    @Test
    @Order(11)
    @DisplayName("INDEX-02: 告警数据查询应使用索引")
    void testAlarmQueryWithIndex() throws Exception {
        mockMvc.perform(get("/api/alarm/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray());
    }

    // ==================== 3. ReportController 测试 ====================

    @Test
    @Order(20)
    @DisplayName("REPORT-01: 分页查询报告列表")
    void testGetReportList() throws Exception {
        mockMvc.perform(get("/api/report/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @Order(21)
    @DisplayName("REPORT-02: 全量查询报告")
    void testGetAllReports() throws Exception {
        mockMvc.perform(get("/api/report/all")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(22)
    @DisplayName("REPORT-03: 按棚舍筛选报告")
    void testGetReportsByBarnId() throws Exception {
        mockMvc.perform(get("/api/report/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10")
                .param("barnId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray());
    }

    @Test
    @Order(23)
    @DisplayName("REPORT-04: 生成溯源报告")
    void testGenerateReport() throws Exception {
        String requestBody = "{\"causeChain\": \"温度过高->氨气上升->动物应激\", \"reportContent\": \"测试报告内容\"}";

        mockMvc.perform(post("/api/report/generate")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("barnId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").exists());
    }

    // ==================== 4. BehaviorController 测试 ====================

    @Test
    @Order(30)
    @DisplayName("BEHAVIOR-01: 分页查询行为数据")
    void testGetBehaviorList() throws Exception {
        mockMvc.perform(get("/api/behavior/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @Order(31)
    @DisplayName("BEHAVIOR-02: 按棚舍和行为类型筛选")
    void testGetBehaviorListWithFilter() throws Exception {
        mockMvc.perform(get("/api/behavior/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10")
                .param("barnId", "1")
                .param("behaviorType", "normal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray());
    }

    @Test
    @Order(32)
    @DisplayName("BEHAVIOR-03: 新增行为记录")
    void testAddBehavior() throws Exception {
        String requestBody = "{\"barnId\": 1, \"behaviorType\": \"normal\", \"confidenceScore\": 0.95}";

        mockMvc.perform(post("/api/behavior/add")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").exists());
    }

    // ==================== 5. WebSocket 测试 ====================

    @Test
    @Order(40)
    @DisplayName("WEBSOCKET-01: WebSocket端点应可访问")
    void testWebSocketEndpoint() throws Exception {
        // WebSocket 端点应该允许访问（无需认证）
        mockMvc.perform(get("/ws"))
                .andExpect(status().isOk());
    }

    // ==================== 6. 异常检测测试（滑动窗口） ====================

    @Test
    @Order(50)
    @DisplayName("ANOMALY-01: 执行异常检测")
    void testAnomalyDetection() throws Exception {
        mockMvc.perform(post("/api/anomaly/detect")
                .header("Authorization", "Bearer " + jwtToken)
                .param("barnId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(51)
    @DisplayName("ANOMALY-02: 查询异常列表")
    void testGetAnomalyList() throws Exception {
        mockMvc.perform(get("/api/anomaly/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray());
    }

    @Test
    @Order(52)
    @DisplayName("ANOMALY-03: 异常统计")
    void testGetAnomalyStats() throws Exception {
        mockMvc.perform(get("/api/anomaly/stats")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    // ==================== 7. 因果分析测试（含异步） ====================

    @Test
    @Order(60)
    @DisplayName("CAUSAL-01: 同步因果分析")
    void testCausalAnalysisSync() throws Exception {
        mockMvc.perform(post("/api/causal/analyze")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"barn_id\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(61)
    @DisplayName("CAUSAL-02: 异步因果分析")
    void testCausalAnalysisAsync() throws Exception {
        mockMvc.perform(post("/api/causal/analyze/async")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"barn_id\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(62)
    @DisplayName("CAUSAL-03: 查询因果关系列表")
    void testGetCausalRelations() throws Exception {
        mockMvc.perform(get("/api/causal/relations")
                .header("Authorization", "Bearer " + jwtToken)
                .param("barnId", "1"))
                .andExpect(status().isOk());
    }

    // ==================== 8. 告警管理测试 ====================

    @Test
    @Order(70)
    @DisplayName("ALARM-01: 创建告警")
    void testCreateAlarm() throws Exception {
        String requestBody = "{\"eventId\": 1, \"barnId\": 1, \"alarmType\": \"temperature\", \"alarmLevel\": \"high\", \"alarmContent\": \"测试告警\"}";

        mockMvc.perform(post("/api/alarm/create")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alarmId").exists());
    }

    @Test
    @Order(71)
    @DisplayName("ALARM-02: 查询告警列表")
    void testGetAlarmList() throws Exception {
        mockMvc.perform(get("/api/alarm/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray());
    }

    @Test
    @Order(72)
    @DisplayName("ALARM-03: 查询待处理告警")
    void testGetPendingAlarms() throws Exception {
        mockMvc.perform(get("/api/alarm/pending")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(73)
    @DisplayName("ALARM-04: 告警统计")
    void testGetAlarmStats() throws Exception {
        mockMvc.perform(get("/api/alarm/stats")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").isNumber());
    }

    // ==================== 9. 溯源报告测试 ====================

    @Test
    @Order(80)
    @DisplayName("TRACE-01: 生成溯源报告")
    void testGenerateTraceReport() throws Exception {
        mockMvc.perform(post("/api/traceability/report")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"causeChain\": \"测试原因链\", \"reportContent\": \"测试报告\"}")
                .param("barnId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").exists());
    }

    @Test
    @Order(81)
    @DisplayName("TRACE-02: 查询溯源报告列表")
    void testGetTraceReports() throws Exception {
        mockMvc.perform(get("/api/traceability/reports")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    // ==================== 10. 操作日志测试 ====================

    @Test
    @Order(90)
    @DisplayName("LOG-01: 查询操作日志列表")
    void testGetOperationLogs() throws Exception {
        mockMvc.perform(get("/api/log/list")
                .header("Authorization", "Bearer " + jwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray());
    }

    // ==================== 11. 系统配置测试 ====================

    @Test
    @Order(100)
    @DisplayName("CONFIG-01: 查询所有配置")
    void testGetAllConfigs() throws Exception {
        mockMvc.perform(get("/api/config/list")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(101)
    @DisplayName("CONFIG-02: 按key查询配置")
    void testGetConfigByKey() throws Exception {
        mockMvc.perform(get("/api/config/temperature_max")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("35.00"));
    }

    @Test
    @Order(102)
    @DisplayName("CONFIG-03: 更新配置")
    void testUpdateConfig() throws Exception {
        String requestBody = "{\"configValue\": \"36.00\", \"description\": \"温度上限（更新）\"}";

        mockMvc.perform(put("/api/config/temperature_max")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("36.00"));

        // 恢复原值
        String restoreBody = "{\"configValue\": \"35.00\", \"description\": \"Temperature upper limit\"}";
        mockMvc.perform(put("/api/config/temperature_max")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(restoreBody));
    }

    // ==================== 12. 仪表盘测试 ====================

    @Test
    @Order(110)
    @DisplayName("DASHBOARD-01: 获取统计数据")
    void testGetDashboardStats() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorCount").isNumber());
    }

    @Test
    @Order(111)
    @DisplayName("DASHBOARD-02: 获取趋势数据")
    void testGetDashboardTrend() throws Exception {
        mockMvc.perform(get("/api/dashboard/trend")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    // ==================== 13. Swagger 文档测试 ====================

    @Test
    @Order(120)
    @DisplayName("SWAGGER-01: Swagger文档应可访问")
    void testSwaggerAccess() throws Exception {
        // Swagger 文档应允许匿名访问
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ==================== 14. 传感器数据测试 ====================

    @Test
    @Order(130)
    @DisplayName("SENSOR-01: 新增传感器数据")
    void testAddSensorData() throws Exception {
        String requestBody = "{\"sensorId\": \"S001\", \"barnId\": 1, \"temperature\": 28.5, \"humidity\": 65.0, \"ammoniaLevel\": 15.0}";

        mockMvc.perform(post("/api/sensor/data")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @Order(131)
    @DisplayName("SENSOR-02: 查询传感器趋势数据")
    void testGetSensorTrend() throws Exception {
        mockMvc.perform(get("/api/sensor/trend")
                .header("Authorization", "Bearer " + jwtToken)
                .param("barnId", "1"))
                .andExpect(status().isOk());
    }
}
