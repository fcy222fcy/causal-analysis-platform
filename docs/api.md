# 智慧养殖环境异常溯源系统 - API 文档

> Base URL: `http://localhost:8080/api`  
> 前端开发基地址：`/api`（Vite 代理至 8080）  
> 认证方式：登录成功后，后续请求需在 Header 中携带 `Authorization: Bearer {token}`  
> 默认管理员账号：`admin` / `admin123`

---

## 1. 用户管理 `/api/user`

### 1.1 用户登录

```
POST /api/user/login
Content-Type: application/json
```

**请求体：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**示例：**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "admin",
  "role": "admin"
}
```

---

### 1.2 用户注册

```
POST /api/user/register
Content-Type: application/json
```

**请求体：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| role | string | 否 | 角色（admin/user），默认 user |

---

### 1.3 获取当前用户信息

```
GET /api/user/info
Headers: Authorization: Bearer {token}
```

**响应：**
```json
{
  "userId": 1,
  "username": "admin",
  "role": "admin"
}
```

---

## 2. 仪表盘 `/api/dashboard`

### 2.1 汇总统计

```
GET /api/dashboard/stats
```

**响应（示例）：**
```json
{
  "sensorCount": 10,
  "anomalyCount": 40,
  "alarmCount": 14,
  "causalCount": 0,
  "reportCount": 1,
  "pendingAlarmCount": 12
}
```

---

### 2.2 环境趋势数据（折线图用）

```
GET /api/dashboard/trend?limit=50
```

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| limit | integer | 否 | 50 | 返回条数 |

**响应：**
```json
{
  "timestamps": ["16:12:44", "16:12:49", "..."],
  "temperature": [25.5, 26.1, ...],
  "humidity":    [65.2, 64.8, ...],
  "ammoniaLevel": [12.3, 13.1, ...]
}
```

---

### 2.3 异常事件分布（饼图用）

```
GET /api/dashboard/anomaly-distribution
```

**响应：**
```json
{
  "data": [
    { "name": "环境异常", "value": 32 },
    { "name": "行为异常", "value": 5 },
    { "name": "复合异常", "value": 3 },
    { "name": "其他",     "value": 0 }
  ]
}
```

---

## 3. 传感器数据 `/api/sensor`

### 3.1 保存单条传感器数据

```
POST /api/sensor/data
Content-Type: application/json
```

**请求体字段对应 `EnvSensorData`：**

| 字段 | 类型 | 必填 |
|------|------|------|
| sensorId | string | 是 |
| barnId | long | 是 |
| temperature | decimal | 是 |
| humidity | decimal | 是 |
| ammoniaLevel | decimal | 是 |
| timestamp | datetime | 是 |

---

### 3.2 批量导入传感器数据

```
POST /api/sensor/batch
Content-Type: application/json
```

请求体：数组 `EnvSensorData[]`

---

### 3.3 分页获取传感器数据

```
GET /api/sensor/data?page=1&size=20&barnId=1
```

| 参数 | 类型 | 必填 | 默认 |
|------|------|------|------|
| barnId | long | 否 | - |
| page | integer | 否 | 1 |
| size | integer | 否 | 50 |

**响应：**
```json
{
  "list": [{ "id": 1, "sensorId": "S001", "barnId": 1, "temperature": 25.5, "humidity": 65.0, "ammoniaLevel": 12.0, "timestamp": "2026-07-03T16:15:59" }],
  "total": 40,
  "page": 1,
  "size": 20
}
```

---

### 3.4 获取全部传感器数据（不分页，最多 200 条）

```
GET /api/sensor/data/all?barnId=1
```

---

### 3.5 按时间范围获取

```
GET /api/sensor/data/range?barnId=1&startTime=2026-07-03T00:00:00&endTime=2026-07-03T23:59:59
```

---

### 3.6 传感器列表

```
GET /api/sensor/list?barnId=1
```

---

### 3.7 传感器统计

```
GET /api/sensor/stats
```

---

### 3.8 环境趋势数据（带棚舍筛选）

```
GET /api/sensor/trend?barnId=1&limit=50
```

响应格式同 2.2。

---

## 4. 动物行为数据 `/api/behavior`

### 4.1 分页获取行为数据

```
GET /api/behavior/list?page=1&size=20&barnId=1&behaviorType=normal
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| barnId | long | 否 | 棚舍ID |
| behaviorType | string | 否 | normal/stress/abnormal/feeding/resting |
| page | int | 否 | 1 |
| size | int | 否 | 50 |

**响应：**
```json
{
  "list": [{
    "recordId": 1, "barnId": 1, "behaviorType": "normal",
    "confidenceScore": 0.85, "timestamp": "..."
  }],
  "total": 100,
  "page": 1,
  "size": 20
}
```

---

### 4.2 获取全部行为数据（最多 200 条）

```
GET /api/behavior/all?barnId=1
```

---

### 4.3 新增行为记录

```
POST /api/behavior/add
Content-Type: application/json
```

字段：`barnId`, `behaviorType`, `confidenceScore`, `timestamp`

---

## 5. 异常检测 `/api/anomaly`

### 5.1 执行异常检测

```
POST /api/anomaly/detect?barnId=1
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| barnId | long | 否 | 指定棚舍；不填则全部棚舍检测 |

**响应：**
```json
{
  "detected": 15,
  "message": "检测完成"
}
```

---

### 5.2 分页获取异常事件

```
GET /api/anomaly/list?page=1&size=20&barnId=1&eventType=environmental
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| barnId | long | 否 | - |
| eventType | string | 否 | environmental（环境）/behavioral（行为）/composite（复合） |
| page/size | int | 否 | 1/50 |

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| eventId | long | 事件ID |
| barnId | long | 棚舍ID |
| eventType | string | 事件类型 |
| severityLevel | string | low / medium / high |
| description | string | 异常描述 |
| timestamp | datetime | 发生时间 |

---

### 5.3 获取全部异常（最多 200 条）

```
GET /api/anomaly/all?barnId=1
```

---

### 5.4 异常统计

```
GET /api/anomaly/stats
```

**响应：**
```json
{
  "total": 76,
  "environmental": 68,
  "behavioral": 5,
  "composite": 3
}
```

---

## 6. 因果分析 `/api/causal`

### 6.1 执行因果分析（同步）

```
POST /api/causal/analyze
Content-Type: application/json
```

**请求体（可选）：**
```json
{ "barn_id": 1 }
```

不填 `barn_id` 时默认为棚舍 1。

**响应示例：**
```json
{
  "message": "分析完成",
  "causal_paths": [
    { "cause": "ammonia_level", "effect": "temperature", "strength": 0.85, "direction": "positive" },
    { "cause": "temperature",   "effect": "humidity",    "strength": 0.72, "direction": "negative" }
  ],
  "graph": {
    "nodes": [
      { "name": "temperature",   "itemStyle": { "color": "#409EFF" } },
      { "name": "humidity",      "itemStyle": { "color": "#67C23A" } },
      { "name": "ammonia_level", "itemStyle": { "color": "#E6A23C" } },
      { "name": "stress",        "itemStyle": { "color": "#F56C6C" } },
      { "name": "abnormal_behavior", "itemStyle": { "color": "#909399" } }
    ],
    "edges": [
      { "source": "ammonia_level", "target": "temperature",       "strength": 0.85 },
      { "source": "temperature",   "target": "humidity",          "strength": 0.72 },
      { "source": "stress",        "target": "abnormal_behavior", "strength": 0.82 }
    ]
  }
}
```

> 变量中英文映射：
>
> | Key | 中文 |
> |-----|------|
> | temperature | 温度 |
> | humidity | 湿度 |
> | ammonia_level | 氨气浓度 |
> | stress | 应激反应 |
> | abnormal_behavior | 异常行为 |

---

### 6.2 执行因果分析（异步）

```
POST /api/causal/analyze/async
Content-Type: application/json
{ "barn_id": 1 }
```

返回 `CompletableFuture<Map>`，响应体内容同 6.1。

---

### 6.3 因果关系列表（分页）

```
GET /api/causal/relations?page=1&size=50&barnId=1
```

---

### 6.4 因果关系统计

```
GET /api/causal/stats
```

---

## 7. 溯源报告 `/api/traceability`

### 7.1 生成溯源报告

```
POST /api/traceability/report?eventId=1&barnId=1
Content-Type: application/json
```

请求体（可选）：
```json
{
  "causeChain": "温度异常 → 氨气升高 → 环境恶化 → 动物应激",
  "reportContent": "根据溯源分析，环境因子异常是主要原因链。"
}
```

不填则使用默认文案。返回 `TraceabilityReport` 实体。

---

### 7.2 分页获取报告列表

```
GET /api/traceability/reports?page=1&size=20&barnId=1
```

**响应：**
```json
{
  "list": [{
    "reportId": 1, "eventId": 1, "barnId": 1,
    "causeChain": "温度异常 → 氨气升高 → 环境恶化 → 动物应激",
    "reportContent": "根据溯源分析...",
    "createTime": "2026-07-03T16:18:17"
  }],
  "total": 1,
  "page": 1,
  "size": 20
}
```

---

### 7.3 获取全部报告（最多 200 条）

```
GET /api/traceability/all?barnId=1
```

---

### 7.4 获取单份报告详情

```
GET /api/traceability/report/{reportId}
```

---

## 8. 告警管理 `/api/alarm`

### 8.1 创建告警

```
POST /api/alarm/create
Content-Type: application/json
```

字段：`eventId`, `barnId`, `alarmType`, `alarmLevel`, `alarmContent`, `status`（默认 pending）

---

### 8.2 分页获取告警列表

```
GET /api/alarm/list?page=1&size=20&barnId=1&status=pending
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| barnId | long | 否 | 棚舍 |
| status | string | 否 | pending/acknowledged/resolved |
| page/size | int | 否 | 1/50 |

**响应字段：**

| 字段 | 类型 | 说明 |
|------|------|------|
| alarmId | long | 告警ID |
| barnId | long | 棚舍 |
| alarmType | string | temperature/humidity/ammonia/behavior/composite |
| alarmLevel | string | low/medium/high |
| alarmContent | string | 告警内容 |
| status | string | pending 待处理 → acknowledged 已确认 → resolved 已解决 |
| createTime | datetime | 创建时间 |
| handleTime | datetime | 处理时间 |
| handlerId | long | 处理人ID |

---

### 8.3 获取全部告警（最多 200 条）

```
GET /api/alarm/all?barnId=1
```

---

### 8.4 获取待处理告警

```
GET /api/alarm/pending
```

---

### 8.5 确认告警

```
PUT /api/alarm/{alarmId}/acknowledge?handlerId=1
```

**响应：**
```json
{ "message": "确认成功", "alarmId": 2 }
```

> 调用后 status 由 `pending` 变为 `acknowledged`。

---

### 8.6 解决告警

```
PUT /api/alarm/{alarmId}/resolve?handlerId=1
```

**响应：**
```json
{ "message": "解决成功", "alarmId": 2 }
```

> 调用后 status 变为 `resolved`，可从任何状态直接解决。

---

### 8.7 告警统计

```
GET /api/alarm/stats
```

---

## 附录：通用状态说明

### 告警状态流转
```
pending ──(确认)──▶ acknowledged ──(解决)──▶ resolved
   └──────────────────(直接解决)──────────────▶
```

### 告警级别
| 等级 | Tag 颜色 |
|------|----------|
| low 低 | info（灰） |
| medium 中 | warning（橙） |
| high 高 | danger（红） |

### 异常事件类型
| Key | 中文 | Tag 颜色 |
|-----|------|----------|
| environmental | 环境异常 | danger |
| behavioral | 行为异常 | warning |
| composite | 复合异常 | primary |

### 行为类型
| Key | 中文 |
|-----|------|
| normal | 正常 |
| stress | 应激 |
| abnormal | 异常 |
| feeding | 进食 |
| resting | 休息 |
