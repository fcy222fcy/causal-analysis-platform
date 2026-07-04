# 前端Vue3实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现前端Vue3应用，包含登录、仪表盘、因果分析、告警管理等页面

**Architecture:** Vue3 + Vite + Element Plus + ECharts + D3.js

**Tech Stack:** Vue3, Vite, Element Plus, ECharts, D3.js, Axios, Pinia

---

## Task 1: 项目初始化

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`

- [ ] **Step 1: 创建package.json**

```json
{
  "name": "smart-farming-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.3.8",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "axios": "^1.6.2",
    "element-plus": "^2.4.3",
    "echarts": "^5.4.3",
    "d3": "^7.8.5",
    "@element-plus/icons-vue": "^2.3.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.2",
    "vite": "^5.0.4"
  }
}
```

- [ ] **Step 2: 创建vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 3: 创建index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>智慧养殖环境异常溯源系统</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 4: 创建main.js**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
```

- [ ] **Step 5: 创建App.vue**

```vue
<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
```

- [ ] **Step 6: 安装依赖并测试**

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:3000 应显示空白页面

- [ ] **Step 7: Commit**

```bash
git add frontend/
git commit -m "feat: 初始化Vue3前端项目"
```

---

## Task 2: 创建路由和API封装

**Files:**
- Create: `frontend/src/router/index.js`
- Create: `frontend/src/api/index.js`
- Create: `frontend/src/api/user.js`
- Create: `frontend/src/api/causal.js`
- Create: `frontend/src/api/sensor.js`
- Create: `frontend/src/api/alarm.js`

- [ ] **Step 1: 创建router/index.js**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/sensor-data',
    name: 'SensorData',
    component: () => import('../views/SensorData.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/behavior-data',
    name: 'BehaviorData',
    component: () => import('../views/BehaviorData.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/anomaly-detection',
    name: 'AnomalyDetection',
    component: () => import('../views/AnomalyDetection.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/causal-analysis',
    name: 'CausalAnalysis',
    component: () => import('../views/CausalAnalysis.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/traceability',
    name: 'Traceability',
    component: () => import('../views/Traceability.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/alarm-management',
    name: 'AlarmManagement',
    component: () => import('../views/AlarmManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/report',
    name: 'Report',
    component: () => import('../views/Report.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 2: 创建api/index.js**

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
```

- [ ] **Step 3: 创建api/user.js**

```javascript
import api from './index'

export const login = (data) => {
  return api.post('/user/login', data)
}

export const register = (data) => {
  return api.post('/user/register', data)
}

export const getUserInfo = () => {
  return api.get('/user/info')
}
```

- [ ] **Step 4: 创建api/causal.js**

```javascript
import api from './index'

export const getCausalAnalysis = (params) => {
  return api.post('/causal/analyze', params)
}

export const runCausalAnalysis = (barnId) => {
  return api.post('/causal/analyze', { barn_id: barnId })
}

export const detectAnomalies = (barnId) => {
  return api.post('/causal/detect-anomalies', null, { params: { barn_id: barnId } })
}

export const traceCausalPath = (cause, effect) => {
  return api.get('/causal/trace-path', { params: { cause, effect } })
}

export const getCorrelation = (barnId) => {
  return api.get('/causal/correlation', { params: { barn_id: barnId } })
}
```

- [ ] **Step 5: 创建api/sensor.js**

```javascript
import api from './index'

export const getSensorData = (params) => {
  return api.get('/sensor/data', { params })
}

export const getSensorList = () => {
  return api.get('/sensor/list')
}

export const addSensorData = (data) => {
  return api.post('/sensor/data', data)
}

export const batchImportSensorData = (data) => {
  return api.post('/sensor/batch', data)
}
```

- [ ] **Step 6: 创建api/alarm.js**

```javascript
import api from './index'

export const getAlarms = (params) => {
  return api.get('/alarm/list', { params })
}

export const acknowledgeAlarm = (alarmId) => {
  return api.put(`/alarm/${alarmId}/acknowledge`)
}

export const resolveAlarm = (alarmId) => {
  return api.put(`/alarm/${alarmId}/resolve`)
}

export const getAlarmStats = () => {
  return api.get('/alarm/stats')
}
```

- [ ] **Step 7: Commit**

```bash
git add frontend/src/router/
git add frontend/src/api/
git commit -m "feat: 创建路由配置和API封装"
```

---

## Task 3: 创建布局组件

**Files:**
- Create: `frontend/src/components/Layout.vue`
- Create: `frontend/src/components/AlarmNotification.vue`

- [ ] **Step 1: 创建Layout.vue**

```vue
<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #304156">
      <div class="logo">智慧养殖溯源</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/sensor-data">
          <el-icon><Monitor /></el-icon>
          <span>传感器数据</span>
        </el-menu-item>
        <el-menu-item index="/behavior-data">
          <el-icon><VideoCamera /></el-icon>
          <span>行为数据</span>
        </el-menu-item>
        <el-menu-item index="/anomaly-detection">
          <el-icon><Warning /></el-icon>
          <span>异常检测</span>
        </el-menu-item>
        <el-menu-item index="/causal-analysis">
          <el-icon><Connection /></el-icon>
          <span>因果分析</span>
        </el-menu-item>
        <el-menu-item index="/traceability">
          <el-icon><Share /></el-icon>
          <span>溯源分析</span>
        </el-menu-item>
        <el-menu-item index="/alarm-management">
          <el-icon><Bell /></el-icon>
          <span>告警管理</span>
        </el-menu-item>
        <el-menu-item index="/report">
          <el-icon><Document /></el-icon>
          <span>报告中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee">
        <span>{{ route.name }}</span>
        <el-dropdown>
          <span class="el-dropdown-link">
            {{ user?.username || '管理员' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background-color: #f5f7fa">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user'))
  } catch {
    return null
  }
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style>
```

- [ ] **Step 2: 创建AlarmNotification.vue**

```vue
<template>
  <div class="alarm-notification" v-if="visible">
    <el-alert
      :title="alarm.content"
      :type="getAlertType(alarm.level)"
      show-icon
      closable
      @close="visible = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  alarm: {
    type: Object,
    default: () => ({ content: '', level: 'low' })
  }
})

const visible = ref(true)

const getAlertType = (level) => {
  const types = { low: 'info', medium: 'warning', high: 'error' }
  return types[level] || 'info'
}

onMounted(() => {
  // 5秒后自动关闭
  setTimeout(() => {
    visible.value = false
  }, 5000)
})
</script>

<style scoped>
.alarm-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  width: 350px;
}
</style>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/
git commit -m "feat: 创建布局组件和告警通知组件"
```

---

## Task 4: 创建页面组件

**Files:**
- Create: `frontend/src/views/Login.vue`
- Create: `frontend/src/views/Dashboard.vue`
- Create: `frontend/src/views/SensorData.vue`
- Create: `frontend/src/views/CausalAnalysis.vue`
- Create: `frontend/src/views/AlarmManagement.vue`
- Create: `frontend/src/views/Traceability.vue`
- Create: `frontend/src/views/AnomalyDetection.vue`
- Create: `frontend/src/views/BehaviorData.vue`
- Create: `frontend/src/views/Report.vue`

- [ ] **Step 1: 创建Login.vue**

```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align: center; margin-bottom: 30px">智慧养殖溯源系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" size="large" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form.value)
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res))
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  padding: 20px;
}
</style>
```

- [ ] **Step 2: 创建Dashboard.vue**

```vue
<template>
  <Layout>
    <div class="dashboard">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background-color: #409EFF">
              <el-icon size="24"><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.sensorCount }}</div>
              <div class="stat-label">传感器数量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background-color: #E6A23C">
              <el-icon size="24"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.anomalyCount }}</div>
              <div class="stat-label">异常事件</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background-color: #F56C6C">
              <el-icon size="24"><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.alarmCount }}</div>
              <div class="stat-label">告警数量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background-color: #67C23A">
              <el-icon size="24"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.causalCount }}</div>
              <div class="stat-label">因果关系</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>环境趋势</template>
            <div ref="trendChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>异常分布</template>
            <div ref="pieChart" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import Layout from '../components/Layout.vue'

const trendChart = ref(null)
const pieChart = ref(null)
const stats = ref({
  sensorCount: 128,
  anomalyCount: 23,
  alarmCount: 15,
  causalCount: 45
})

onMounted(() => {
  initTrendChart()
  initPieChart()
})

const initTrendChart = () => {
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['温度', '湿度', '氨气浓度'] },
    xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
    yAxis: [
      { type: 'value', name: '温度(°C)' },
      { type: 'value', name: '湿度(%)' }
    ],
    series: [
      { name: '温度', type: 'line', data: [25, 26, 24, 27, 28, 26, 25] },
      { name: '湿度', type: 'line', data: [60, 65, 58, 70, 72, 68, 62] },
      { name: '氨气浓度', type: 'line', data: [15, 18, 14, 20, 22, 19, 16] }
    ]
  })
}

const initPieChart = () => {
  const chart = echarts.init(pieChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 10, name: '环境异常' },
        { value: 5, name: '行为异常' },
        { value: 3, name: '复合异常' }
      ],
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      }
    }]
  })
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
```

- [ ] **Step 3: 创建CausalAnalysis.vue**

```vue
<template>
  <Layout>
    <div class="causal-analysis">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>因果关系图</span>
            <el-button type="primary" @click="analyzeCausal" :loading="loading">
              执行分析
            </el-button>
          </div>
        </template>
        <div ref="causalGraph" style="height: 500px"></div>
      </el-card>
      
      <el-card style="margin-top: 20px">
        <template #header>因果路径</template>
        <el-table :data="causalPaths" style="width: 100%">
          <el-table-column prop="cause" label="原因变量" />
          <el-table-column prop="effect" label="结果变量" />
          <el-table-column prop="strength" label="因果强度">
            <template #default="{ row }">
              <el-progress :percentage="Math.round(row.strength * 100)" />
            </template>
          </el-table-column>
          <el-table-column prop="direction" label="方向">
            <template #default="{ row }">
              <el-tag :type="row.direction === 'positive' ? 'success' : 'danger'">
                {{ row.direction === 'positive' ? '正向' : '负向' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import Layout from '../components/Layout.vue'

const causalGraph = ref(null)
const loading = ref(false)
const causalPaths = ref([
  { cause: 'ammonia_level', effect: 'temperature', strength: 0.85, direction: 'positive' },
  { cause: 'temperature', effect: 'humidity', strength: 0.72, direction: 'negative' },
  { cause: 'ammonia_level', effect: 'humidity', strength: 0.68, direction: 'positive' }
])

onMounted(() => {
  initCausalGraph()
})

const initCausalGraph = () => {
  const chart = echarts.init(causalGraph.value)
  
  const nodes = [
    { name: 'temperature', symbolSize: 50, itemStyle: { color: '#409EFF' }, label: { show: true } },
    { name: 'humidity', symbolSize: 50, itemStyle: { color: '#67C23A' }, label: { show: true } },
    { name: 'ammonia_level', symbolSize: 50, itemStyle: { color: '#E6A23C' }, label: { show: true } }
  ]
  
  const links = [
    { source: 'ammonia_level', target: 'temperature', lineStyle: { width: 3 } },
    { source: 'temperature', target: 'humidity', lineStyle: { width: 2 } },
    { source: 'ammonia_level', target: 'humidity', lineStyle: { width: 2 } }
  ]
  
  chart.setOption({
    tooltip: {},
    series: [{
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      force: { repulsion: 300 },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 10],
      label: { position: 'right' }
    }]
  })
}

const analyzeCausal = async () => {
  loading.value = true
  try {
    // 调用API执行分析
    console.log('执行因果分析')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

- [ ] **Step 4: 创建AlarmManagement.vue**

```vue
<template>
  <Layout>
    <div class="alarm-management">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>告警管理</span>
            <el-button type="primary" @click="refreshAlarms">刷新</el-button>
          </div>
        </template>
        
        <el-table :data="alarms" style="width: 100%">
          <el-table-column prop="alarmId" label="告警ID" width="100" />
          <el-table-column prop="barnId" label="棚舍ID" width="100" />
          <el-table-column prop="alarmType" label="告警类型" width="120">
            <template #default="{ row }">
              <el-tag>{{ row.alarmType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmLevel" label="告警级别" width="100">
            <template #default="{ row }">
              <el-tag :type="getLevelType(row.alarmLevel)">{{ row.alarmLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmContent" label="告警内容" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="acknowledge(row)" v-if="row.status === 'pending'">
                确认
              </el-button>
              <el-button size="small" type="success" @click="resolve(row)" v-if="row.status !== 'resolved'">
                解决
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const alarms = ref([
  { alarmId: 1, barnId: 1, alarmType: 'temperature', alarmLevel: 'high', alarmContent: '温度过高：35.5°C', status: 'pending', createTime: '2024-01-15 10:30:00' },
  { alarmId: 2, barnId: 2, alarmType: 'ammonia', alarmLevel: 'medium', alarmContent: '氨气浓度超标：28ppm', status: 'acknowledged', createTime: '2024-01-15 11:00:00' },
  { alarmId: 3, barnId: 1, alarmType: 'humidity', alarmLevel: 'low', alarmContent: '湿度过低：35%', status: 'resolved', createTime: '2024-01-15 09:00:00' }
])

const getLevelType = (level) => {
  const types = { low: 'info', medium: 'warning', high: 'danger' }
  return types[level] || 'info'
}

const getStatusType = (status) => {
  const types = { pending: 'warning', acknowledged: 'primary', resolved: 'success' }
  return types[status] || 'info'
}

const refreshAlarms = () => {
  console.log('刷新告警')
}

const acknowledge = (row) => {
  row.status = 'acknowledged'
  console.log('确认告警', row.alarmId)
}

const resolve = (row) => {
  row.status = 'resolved'
  console.log('解决告警', row.alarmId)
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

- [ ] **Step 5: 创建其他页面（简化版）**

```vue
<!-- SensorData.vue -->
<template>
  <Layout>
    <el-card>
      <template #header>传感器数据</template>
      <el-table :data="sensorData" style="width: 100%">
        <el-table-column prop="sensorId" label="传感器ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="temperature" label="温度" />
        <el-table-column prop="humidity" label="湿度" />
        <el-table-column prop="ammoniaLevel" label="氨气浓度" />
        <el-table-column prop="timestamp" label="时间" />
      </el-table>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const sensorData = ref([
  { sensorId: 'S001', barnId: 1, temperature: 25.5, humidity: 65, ammoniaLevel: 15, timestamp: '2024-01-15 10:00:00' },
  { sensorId: 'S002', barnId: 1, temperature: 26.2, humidity: 68, ammoniaLevel: 18, timestamp: '2024-01-15 10:05:00' }
])
</script>

<!-- BehaviorData.vue -->
<template>
  <Layout>
    <el-card>
      <template #header>行为数据</template>
      <el-table :data="behaviorData" style="width: 100%">
        <el-table-column prop="recordId" label="记录ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="behaviorType" label="行为类型" />
        <el-table-column prop="confidenceScore" label="置信度" />
        <el-table-column prop="timestamp" label="时间" />
      </el-table>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const behaviorData = ref([
  { recordId: 1, barnId: 1, behaviorType: 'normal', confidenceScore: 0.95, timestamp: '2024-01-15 10:00:00' },
  { recordId: 2, barnId: 1, behaviorType: 'stress', confidenceScore: 0.88, timestamp: '2024-01-15 10:05:00' }
])
</script>

<!-- AnomalyDetection.vue -->
<template>
  <Layout>
    <el-card>
      <template #header>异常检测</template>
      <el-table :data="anomalies" style="width: 100%">
        <el-table-column prop="eventId" label="事件ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="eventType" label="事件类型" />
        <el-table-column prop="severityLevel" label="严重程度" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="timestamp" label="时间" />
      </el-table>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const anomalies = ref([
  { eventId: 1, barnId: 1, eventType: 'environmental', severityLevel: 'high', description: '温度异常升高', timestamp: '2024-01-15 10:30:00' },
  { eventId: 2, barnId: 2, eventType: 'behavioral', severityLevel: 'medium', description: '动物应激行为', timestamp: '2024-01-15 11:00:00' }
])
</script>

<!-- Traceability.vue -->
<template>
  <Layout>
    <el-card>
      <template #header>溯源分析</template>
      <div ref="traceChart" style="height: 400px"></div>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'

const traceChart = ref(null)

onMounted(() => {
  // 初始化溯源路径图
  console.log('初始化溯源路径图')
})
</script>

<!-- Report.vue -->
<template>
  <Layout>
    <el-card>
      <template #header>报告中心</template>
      <el-table :data="reports" style="width: 100%">
        <el-table-column prop="reportId" label="报告ID" />
        <el-table-column prop="eventId" label="事件ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="reportContent" label="报告内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const reports = ref([
  { reportId: 1, eventId: 1, barnId: 1, reportContent: '温度异常升高导致氨气浓度上升，建议加强通风', createTime: '2024-01-15 11:00:00' }
])
</script>
```

- [ ] **Step 6: Commit**

```bash
git add frontend/src/views/
git commit -m "feat: 创建所有页面组件"
```

---

## Task 5: 测试和调试

- [ ] **Step 1: 启动前端服务**

```bash
cd frontend
npm run dev
```

- [ ] **Step 2: 测试所有页面**

访问 http://localhost:3000，测试以下页面：
- 登录页面
- 仪表盘
- 传感器数据
- 因果分析
- 告警管理

- [ ] **Step 3: Commit**

```bash
git add frontend/
git commit -m "feat: 完成前端页面开发"
```
