# 🐄 智慧养殖环境异常溯源与因果分析系统

> Smart Farming Environment Anomaly Traceability and Causal Analysis System

[![Java](https://img.shields.io/badge/Java-17-green.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.3-blue.svg)](https://vuejs.org/)
[![Python](https://img.shields.io/badge/Python-3.8+-yellow.svg)](https://www.python.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#)

## 📋 项目简介

本系统用于**智慧养殖场景**中环境异常事件的溯源分析与因果关系建模，实现从 **"检测异常"** 到 **"解释异常"** 的升级。

系统不仅识别异常，还进一步分析异常产生的原因链路，例如温湿度变化、氨气浓度上升与动物应激行为之间的关联关系，从而实现养殖环境的智能监控与决策支持。

### 🎯 核心功能

- ✅ **多源数据采集** - 环境传感器 + 声纹数据 + 视频行为数据
- ✅ **智能异常检测** - 阈值模型 + 时序异常检测算法
- ✅ **因果关系建模** - 基于 DoWhy 的因果图构建与分析
- ✅ **异常溯源分析** - 异常源定位 + 最优因果路径推断
- ✅ **实时告警推送** - WebSocket 实时通知 + 多级告警分类
- ✅ **可视化展示** - ECharts 图表 + 因果关系图 + 溯源路径
- ✅ **报告生成** - 自动生成异常溯源分析报告

### 💡 创新亮点

1. **因果推理替代相关性分析** - 不只告诉"发生了什么"，更解释"为什么发生"
2. **多源数据融合** - 环境传感器 + 声纹 + 视频行为数据联合分析
3. **可解释因果链** - 每个异常事件必须具备可解释的因果路径
4. **Java + Python 混合架构** - 充分利用两种语言优势

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    前端展示层（Vue 3）                        │
│   ECharts 图表 + Graph Visualization 因果图 + 溯源路径       │
├─────────────────────────────────────────────────────────────┤
│                    业务处理层（Spring Boot）                  │
│   Controller → Service → DAO → 异步任务管理                  │
├─────────────────────────────────────────────────────────────┤
│                    数据存储层                                 │
│         MySQL（分库分表）    │    Redis 缓存                  │
├─────────────────────────────────────────────────────────────┤
│                    算法分析层（Python）                       │
│         FastAPI + DoWhy + PyTorch + scikit-learn            │
└─────────────────────────────────────────────────────────────┘
```

### 🔄 核心业务流程

```
数据采集 → 时间对齐 → 异常检测 → 因果建模 → 溯源分析 → 报告生成
   ↓          ↓          ↓          ↓          ↓          ↓
传感器数据   秒级同步   滑动窗口   因果图     最优路径   可视化展示
声纹数据     ↓         统计模型   构建       推断       ↓
视频数据   统一时间     ↓         ↓          ↓       ECharts
   ↓       坐标      环境异常   相关性     异常源     因果图
行为数据               检测     矩阵计算   定位       溯源图
```

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.5 | 后端框架 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存数据库 |
| JWT | 0.12.5 | 身份认证 |
| Knife4j | 4.3.0 | API 文档 |
| WebSocket | - | 实时告警推送 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.3 | 前端框架 |
| Vite | 5.0 | 构建工具 |
| Element Plus | 2.4 | UI 组件库 |
| ECharts | 5.4 | 图表可视化 |
| D3.js | 7.8 | 因果图可视化 |
| Axios | 1.6 | HTTP 客户端 |

### 算法技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Python | 3.8+ | 编程语言 |
| FastAPI | 0.104 | Web 框架 |
| DoWhy | 0.11 | 因果推理库 |
| PyTorch | 2.1 | 深度学习框架 |
| scikit-learn | 1.3 | 机器学习库 |
| pandas | 2.1 | 数据处理 |
| numpy | 1.26 | 数值计算 |

## 📁 项目结构

```
smart-farming-causal-analysis/
├── frontend/                        # 前端项目
│   ├── src/
│   │   ├── api/                    # API 接口封装
│   │   ├── components/             # 公共组件
│   │   ├── views/                  # 页面视图
│   │   ├── router/                 # 路由配置
│   │   └── stores/                 # Pinia 状态管理
│   ├── public/
│   └── package.json
│
├── causal-service/                  # Python 因果分析服务
│   ├── app.py                      # FastAPI 入口
│   ├── routers/                    # 路由模块
│   │   ├── causal_router.py       # 因果分析路由
│   │   └── data_router.py         # 数据处理路由
│   ├── services/                   # 业务服务
│   │   ├── causal_service.py      # 因果分析服务
│   │   └── data_service.py        # 数据处理服务
│   ├── models/                     # 数据模型
│   └── requirements.txt
│
├── smart-farming-backend/           # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com.smartfarming/
│   │       ├── controller/        # 控制器层
│   │       ├── service/           # 服务层
│   │       ├── mapper/            # 数据访问层
│   │       ├── entity/            # 实体类
│   │       ├── dto/               # 数据传输对象
│   │       └── config/            # 配置类
│   ├── src/main/resources/
│   │   ├── mapper/                # MyBatis 映射文件
│   │   └── db/                    # 数据库脚本
│   └── pom.xml
│
├── docs/                            # 项目文档
│   ├── 项目要求.md
│   └── 项目设计文档.md
│
├── start-services.sh               # Linux/Mac 启动脚本
├── start-services.bat              # Windows 启动脚本
├── stop-services.sh                # Linux/Mac 停止脚本
├── stop-services.bat               # Windows 停止脚本
├── .gitignore
└── README.md
```

## 🚀 快速开始

### 环境要求

- **Node.js** >= 16.x
- **Python** >= 3.8
- **Java** >= 17
- **Maven** >= 3.8 (可选，项目包含 Maven Wrapper)
- **MySQL** >= 8.0 (生产环境)
- **Redis** >= 6.0 (可选，用于缓存)

### 一键启动

#### Windows 用户

```bash
# 双击运行或命令行执行
start-services.bat
```

#### Linux/Mac 用户

```bash
# 添加执行权限
chmod +x start-services.sh stop-services.sh

# 启动所有服务
./start-services.sh

# 停止所有服务
./stop-services.sh
```

### 手动启动

#### 1. 启动因果分析服务

```bash
cd causal-service

# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt

# 启动服务
python app.py
```

#### 2. 启动后端服务

```bash
cd smart-farming-backend

# 使用 Maven Wrapper (推荐)
./mvnw spring-boot:run

# 或使用系统 Maven
mvn spring-boot:run
```

#### 3. 启动前端服务

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 📦 数据初始化

**首次运行时自动加载示例数据**，无需手动操作！

项目内置了示例数据脚本 `smart-farming-backend/src/main/resources/db/data.sql`，包含：

- 📊 2个猪舍的传感器数据（21条）
- 🐷 动物行为数据（22条）
- ⚠️ 异常事件记录（6条）
- 🔗 因果关系分析结果（7条）
- 🚨 告警记录（4条）
- 📋 追溯报告（2份）
- 📝 操作日志（5条）

**场景说明**：模拟1号猪舍高温应激事件的完整因果链

| 时间 | 温度 | 氨气 | 事件 |
|------|------|------|------|
| 06:00 | 25.5°C | 10.3ppm | 正常 |
| 14:00 | 31.2°C | 16.8ppm | 预警 |
| 18:00 | 35.2°C | 22.3ppm | 危险 |
| 20:00 | 33.5°C | 20.1ppm | 处理后下降 |

**默认账号**：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| zhangsan | admin123 | 普通用户 |
| lisi | admin123 | 普通用户 |

详细说明请查看 [数据库初始化说明](smart-farming-backend/src/main/resources/db/README.md)

## 🌐 服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:5173 | Vue.js 开发服务器 |
| 后端 API | http://localhost:8080 | Spring Boot 服务 |
| API 文档 | http://localhost:8080/doc.html | Knife4j 接口文档 |
| 因果分析 API | http://localhost:5000 | FastAPI 服务 |
| H2 控制台 | http://localhost:8080/h2-console | 数据库控制台 |

## 📊 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_user | 用户表 | user_id, username, password, role |
| env_sensor_data | 环境传感器数据 | sensor_id, temperature, humidity, ammonia_level |
| animal_behavior | 动物行为数据 | behavior_type, confidence_score |
| anomaly_event | 异常事件表 | event_type, severity_level |
| causal_relation | 因果关系表 | cause_variable, effect_variable, causal_strength |
| traceability_report | 溯源报告表 | cause_chain, report_content |
| alarm_record | 告警记录表 | alarm_type, alarm_level, status |
| operation_log | 操作日志表 | user_id, operation, params |
| system_config | 系统配置表 | config_key, config_value |

### 数据库特点

- ✅ 按 `barn_id` 分库（每个棚舍一个库）
- ✅ 按时间维度分区（月分区）
- ✅ 支持 5 年历史数据存储
- ✅ 完善的索引设计

## 🔧 配置说明

### 后端配置

编辑 `smart-farming-backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_farming
    username: root
    password: your_password

# JWT 配置
jwt:
  secret: your-secret-key
  expiration: 86400000

# Python 服务地址
python:
  causal:
    service:
      url: http://localhost:5000
```

### 前端配置

编辑 `frontend/vite.config.js`:

```javascript
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 🎨 功能模块

### 1. 用户与权限模块

- 登录/注册（JWT 认证）
- 角色权限控制（管理员/普通用户）
- 操作日志记录

### 2. 数据采集模块

- 环境传感器数据接入（温度、湿度、氨气浓度）
- 动物声纹数据采集与分类
- 视频行为数据识别
- 模拟数据生成器（支持批量导入）

### 3. 异常检测模块

- 基于滑动窗口的时序异常检测
- 多维度阈值判断
- 行为异常识别（置信度评分）

### 4. 因果分析模块

- 基于 DoWhy 的因果图构建
- 相关性矩阵计算（皮尔逊/斯皮尔曼）
- 因果强度量化

### 5. 溯源分析模块

- 异常源自动定位
- 最优因果路径推断
- 原因链自动生成

### 6. 可视化模块

- 数据大屏展示（Dashboard）
- ECharts 统计图表
- D3.js 因果关系图（交互式）
- 溯源路径可视化

### 7. 告警管理模块

- 实时告警推送（WebSocket）
- 告警记录查询与处理
- 告警级别分类（低/中/高）

### 8. 报告生成模块

- 自动生成异常溯源分析报告
- 包含原因链解释
- 支持导出与历史查看

## 🧪 测试

### 运行单元测试

```bash
# 前端测试
cd frontend
npm run test

# 后端测试
cd smart-farming-backend
./mvnw test

# 因果分析服务测试
cd causal-service
python -m pytest tests/
```

## 📚 相关文档

- [项目要求文档](docs/项目要求.md)
- [系统设计文档](docs/项目设计文档.md)
- [启动说明文档](README-启动说明.md)

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 团队

| 角色 | 职责 |
|------|------|
| 后端开发 | Spring Boot 接口开发、数据库设计、业务逻辑实现 |
| 算法开发 | 数据清洗、特征提取、因果分析算法实现 |
| 前端开发 | Vue3 页面开发、ECharts 可视化、UI 交互设计 |
| 测试文档 | 系统联调、接口测试、文档整理 |

## 📞 联系方式

- 项目地址: https://github.com/your-username/smart-farming-causal-analysis
- 问题反馈: [Issues](https://github.com/your-username/smart-farming-causal-analysis/issues)

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [DoWhy](https://github.com/py-why/dowhy)
- [ECharts](https://echarts.apache.org/)
- [Element Plus](https://element-plus.org/)

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！
