# 智慧养殖环境异常溯源与因果分析系统 - 设计文档

## 项目概述

本系统用于智慧养殖场景中环境异常事件的溯源分析与因果关系建模，实现从"检测异常"到"解释异常"的升级。

## 系统架构

### 四层架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    前端层（Vue3）                            │
│   ECharts图表 + Graph Visualization因果图 + 溯源路径        │
├─────────────────────────────────────────────────────────────┤
│                    Spring Boot 后端                          │
├─────────────────────────────────────────────────────────────┤
│  Controller层  │  Service层  │  DAO层  │  异步任务管理       │
├─────────────────────────────────────────────────────────────┤
│              MySQL（分库分表）      │     Redis缓存          │
├─────────────────────────────────────────────────────────────┤
│           Python Flask/FastAPI（因果分析服务）               │
│              DoWhy + PyTorch + scikit-learn                 │
└─────────────────────────────────────────────────────────────┘
```

### 核心业务流程

1. 采集养殖环境数据（温度/湿度/氨气浓度）
2. 采集动物行为数据（声纹+视频行为）
3. 对数据进行时间对齐（秒级同步）
4. 检测环境异常（基于滑动窗口统计模型）
5. 检测动物行为异常（声纹分类+行为识别）
6. 构建异常事件时间链路
7. 计算变量间相关性矩阵
8. 构建因果关系图（Causal Graph）
9. 推断异常可能源头（最优因果路径）
10. 生成异常溯源报告（原因链解释）
11. 写入数据库并存储分析结果
12. 前端展示因果关系图与溯源路径

## 数据库设计

### 数据库分库分表策略

- 按barn_id分库（每个棚舍一个库）
- 按时间维度分区（月分区）
- 保留5年历史数据

### 表结构设计（9张表）

#### 1. sys_user（用户表）
```sql
CREATE TABLE sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    barn_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
);
```

#### 2. env_sensor_data（环境传感器数据表）
```sql
CREATE TABLE env_sensor_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sensor_id VARCHAR(50) NOT NULL,
    barn_id BIGINT NOT NULL,
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    ammonia_level DECIMAL(5,2),
    timestamp DATETIME NOT NULL,
    INDEX idx_sensor_time (sensor_id, timestamp),
    INDEX idx_barn_time (barn_id, timestamp)
);
```

#### 3. animal_behavior（动物行为数据表）
```sql
CREATE TABLE animal_behavior (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    barn_id BIGINT NOT NULL,
    behavior_type VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(5,4),
    timestamp DATETIME NOT NULL,
    INDEX idx_barn_time (barn_id, timestamp),
    INDEX idx_behavior_type (behavior_type)
);
```

#### 4. anomaly_event（异常事件表）
```sql
CREATE TABLE anomaly_event (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    barn_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    severity_level VARCHAR(10) NOT NULL,
    description TEXT,
    timestamp DATETIME NOT NULL,
    INDEX idx_barn_time (barn_id, timestamp),
    INDEX idx_severity (severity_level)
);
```

#### 5. causal_relation（因果关系表）
```sql
CREATE TABLE causal_relation (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    barn_id BIGINT NOT NULL,
    cause_variable VARCHAR(50) NOT NULL,
    effect_variable VARCHAR(50) NOT NULL,
    correlation_score DECIMAL(5,4),
    causal_strength DECIMAL(5,4),
    timestamp DATETIME NOT NULL,
    INDEX idx_barn_time (barn_id, timestamp),
    INDEX idx_cause_effect (cause_variable, effect_variable)
);
```

#### 6. traceability_report（溯源报告表）
```sql
CREATE TABLE traceability_report (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    barn_id BIGINT NOT NULL,
    cause_chain TEXT,
    report_content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_id (event_id),
    INDEX idx_barn_time (barn_id, create_time)
);
```

#### 7. operation_log（操作日志表）
```sql
CREATE TABLE operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    operation VARCHAR(100) NOT NULL,
    method VARCHAR(100),
    params TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, create_time)
);
```

#### 8. alarm_record（告警记录表）
```sql
CREATE TABLE alarm_record (
    alarm_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    barn_id BIGINT NOT NULL,
    alarm_type VARCHAR(20) NOT NULL,
    alarm_level VARCHAR(10) NOT NULL,
    alarm_content TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    handle_time DATETIME,
    INDEX idx_barn_time (barn_id, create_time),
    INDEX idx_status (status)
);
```

#### 9. system_config（系统配置表）
```sql
CREATE TABLE system_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
);
```

## 后端技术架构

### 技术栈

- Spring Boot 3.x
- MyBatis-Plus
- Spring Security + JWT
- Redis（缓存）
- WebSocket（实时告警推送）

### 分层架构

```
com.smartfarming
├── controller/          # 控制器层
│   ├── UserController.java
│   ├── SensorDataController.java
│   ├── BehaviorDataController.java
│   ├── AnomalyController.java
│   ├── CausalAnalysisController.java
│   ├── TraceabilityController.java
│   ├── AlarmController.java
│   └── ReportController.java
├── service/             # 服务层
│   ├── UserService.java
│   ├── SensorDataService.java
│   ├── BehaviorDataService.java
│   ├── AnomalyDetectionService.java
│   ├── CausalAnalysisService.java
│   ├── TraceabilityService.java
│   ├── AlarmService.java
│   └── ReportService.java
├── mapper/              # 数据访问层
│   ├── UserMapper.java
│   ├── SensorDataMapper.java
│   ├── BehaviorDataMapper.java
│   ├── AnomalyEventMapper.java
│   ├── CausalRelationMapper.java
│   ├── TraceabilityReportMapper.java
│   ├── OperationLogMapper.java
│   ├── AlarmRecordMapper.java
│   └── SystemConfigMapper.java
├── entity/              # 实体类
├── dto/                 # 数据传输对象
├── vo/                  # 视图对象
├── config/              # 配置类
├── security/            # 安全配置
├── task/                # 异步任务
│   └── AsyncTaskManager.java
├── websocket/           # WebSocket
│   └── AlarmWebSocket.java
└── utils/               # 工具类
```

### 异步任务处理

```java
// 异步任务管理器
@Service
public class AsyncTaskManager {
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    public CompletableFuture<CausalAnalysisResult> submitCausalAnalysis(CausalAnalysisRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            // 调用Python服务进行因果分析
            return callPythonCausalService(request);
        }, taskExecutor);
    }
}
```

## Python因果分析服务

### 技术栈

- Flask/FastAPI
- DoWhy（因果推断）
- PyTorch（深度学习）
- pandas（数据处理）
- scikit-learn（特征工程）

### API接口

```
POST /api/causal/analyze
POST /api/causal/build-graph
GET /api/causal/trace-path
```

### 核心算法

1. 时间序列异常检测（滑动窗口统计）
2. 相关性分析（皮尔逊/斯皮尔曼）
3. 因果图构建（DoWhy）
4. 因果路径推断（最优路径算法）

## 前端架构

### 技术栈

- Vue3 + Vite
- ECharts（统计图表）
- D3.js（因果关系图可视化）
- Axios（API调用）
- Element Plus（UI组件）

### 页面结构

```
src/
├── views/
│   ├── Login.vue
│   ├── Dashboard.vue
│   ├── SensorData.vue
│   ├── BehaviorData.vue
│   ├── AnomalyDetection.vue
│   ├── CausalAnalysis.vue
│   ├── Traceability.vue
│   ├── AlarmManagement.vue
│   └── Report.vue
├── components/
│   ├── CausalGraph.vue
│   ├── TraceabilityPath.vue
│   ├── AnomalyChart.vue
│   └── AlarmNotification.vue
├── api/
│   ├── user.js
│   ├── sensor.js
│   ├── behavior.js
│   ├── anomaly.js
│   ├── causal.js
│   ├── traceability.js
│   ├── alarm.js
│   └── report.js
├── router/
├── store/
└── utils/
```

## 6大工程模块实现

### 1. 用户与权限模块

- 登录/注册（JWT认证）
- 角色权限（管理员/普通用户）
- 权限控制（Spring Security）

### 2. 数据接入模块

- REST API数据接收
- 模拟数据生成器
- 批量数据导入

### 3. 数据存储模块

- MySQL分库分表
- Redis缓存
- 历史数据存储

### 4. 业务处理模块

- 因果分析算法（Python）
- 异常检测（Java）
- 相关性分析

### 5. 可视化模块

- ECharts统计图表
- 因果关系图（D3.js）
- 溯源路径展示

### 6. 预警/决策模块

#### 6.1 阈值配置管理

| 配置项 | 说明 |
|--------|------|
| 指标名称 | temperature / humidity / ammonia_level |
| 阈值上限 | 各指标正常范围上限 |
| 阈值下限 | 各指标正常范围下限 |
| 适用棚舍 | 支持按barn_id配置不同阈值 |
| 生效时间 | 支持定时生效 |

#### 6.2 告警规则

- **单指标超限**：单个指标超出阈值范围即触发告警
- **组合条件触发**：多个指标同时异常时触发高级别告警（如温度高+氨气高）

#### 6.3 告警分级与通知策略

| 告警级别 | 触发条件 | 通知方式 |
|---------|---------|---------|
| 低 | 单指标轻微超限 | 日志记录 |
| 中 | 单指标严重超限或组合异常 | 页面提醒 |
| 高 | 多指标同时异常或紧急情况 | WebSocket实时推送 |

#### 6.4 告警处理闭环

```
pending → acknowledged → resolved
   │          │            │
   │          │            └── 记录处理人和处理时间
   │          └── 记录确认人
   └── 初始状态
```

- **pending**：告警刚产生，待处理
- **acknowledged**：已确认，待解决
- **resolved**：已解决，记录处理人和处理时间

## 可扩展能力设计

### 1. 架构扩展性

当前系统为"异常溯源系统"，可扩展为"因果决策系统"：

- **决策引擎**：预留决策模块接口，可接入规则引擎或强化学习模型
- **数字孪生**：预留实时数据流接口，支持数字孪生系统接入

### 2. 模块化设计

- **因果分析模块**：Python算法可替换为强化学习模型
- **数据接入层**：支持新增传感器类型和数据源
- **可视化层**：支持新增图表类型和展示方式

### 3. API扩展性

- RESTful API设计，支持版本管理（v1/v2）
- 预留Webhook接口，支持事件驱动架构

## 团队分工

| 角色 | 人数 | 主要职责 |
|------|------|----------|
| 后端开发 | 1人 | Spring Boot接口、数据库设计、业务逻辑、Swagger文档 |
| 数据/算法 | 1人 | 数据清洗、特征提取、Python因果分析模块 |
| 前端开发 | 1人 | Vue3页面、ECharts可视化、因果图展示 |
| 系统集成/测试 | 1人 | 系统联调、接口测试、文档整理 |

## 开发计划

### 阶段一（第1-2周）：基础搭建
- 项目初始化
- 数据库设计与实现
- 用户模块

### 阶段二（第3-4周）：数据层
- 数据接入模块
- 模拟数据生成器
- 数据存储

### 阶段三（第5-6周）：核心算法
- 异常检测模块
- Python因果分析模块

### 阶段四（第7-8周）：可视化与集成
- 前端页面开发
- 因果图可视化
- 系统联调与测试
