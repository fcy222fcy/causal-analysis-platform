<template>
  <Layout>
    <div class="dashboard">
      <el-row :gutter="16" class="stat-row" justify="space-between">
        <el-col :span="4" v-for="(item, idx) in cardList" :key="idx">
          <el-card shadow="hover" class="stat-card" :body-style="{ padding: '16px 20px' }">
            <div class="stat-inner">
              <div class="stat-icon" :style="{ backgroundColor: item.color }">
                <el-icon size="22"><component :is="item.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ item.label }}</div>
                <div class="stat-value">
                  {{ stats[item.key] || 0 }}<span v-if="item.key === 'healthPercent'" style="font-size:16px">%</span>
                </div>
                <div class="sub-line">
                  <span v-if="item.key === 'healthPercent'" class="sub-label" :style="{ color: stats.healthPercent >= 80 ? '#67C23A' : stats.healthPercent >= 50 ? '#E6A23C' : '#F56C6C' }">
                    {{ stats.healthStatus }}
                  </span>
                  <span v-else-if="item.subKey && stats[item.subKey] > 0" class="sub-label" :style="{ color: item.subColor || '#F56C6C' }">
                    待处理 {{ stats[item.subKey] }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 第一行图表：系统健康度 + 异常分布 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>系统健康度</template>
            <div ref="gaugeChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>异常分布</template>
            <div ref="pieChart" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 第二行图表：环境趋势 + 行为统计 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>环境趋势</span>
                <el-button size="small" type="primary" plain @click="loadTrendData" :loading="trendLoading">刷新</el-button>
              </div>
            </template>
            <div ref="trendChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>行为类型统计</template>
            <div ref="behaviorChart" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount, markRaw } from 'vue'
import * as echarts from 'echarts'
import Layout from '../components/Layout.vue'
import { getDashboardStats, getDashboardTrend, getAnomalyDistribution } from '../api/dashboard'
import { ElMessage } from 'element-plus'
import { Monitor, Warning, Bell, Connection, Document, Timer, OfficeBuilding } from '@element-plus/icons-vue'
import request from '../api/index'

const cardList = [
  { label: '传感器数量', key: 'sensorCount',       icon: markRaw(Monitor),        color: '#409EFF' },
  { label: '异常事件',   key: 'anomalyCount',      icon: markRaw(Warning),        color: '#E6A23C' },
  { label: '告警数量',   key: 'alarmCount',        icon: markRaw(Bell),           color: '#F56C6C', subKey: 'pendingAlarmCount' },
  { label: '行为记录',   key: 'behaviorCount',     icon: markRaw(Timer),          color: '#909399' },
  { label: '溯源报告',   key: 'reportCount',       icon: markRaw(Document),       color: '#E6A23A' }
]

const trendChart = ref(null)
const pieChart = ref(null)
const behaviorChart = ref(null)
const gaugeChart = ref(null)
let trendChartInstance = null
let pieChartInstance = null
let behaviorChartInstance = null
let gaugeChartInstance = null

const stats = ref({
  sensorCount: 0,
  anomalyCount: 0,
  alarmCount: 0,
  causalCount: 0,
  pendingAlarmCount: 0,
  reportCount: 0,
  behaviorCount: 0,
  barnCount: 5,
  healthPercent: 100,
  healthStatus: '健康'
})

const trendLoading = ref(false)

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    const total = (res.anomalyCount || 0) + (res.alarmCount || 0)
    const pending = res.pendingAlarmCount || 0
    const health = total === 0 ? 100 : Math.max(0, Math.round(100 - (pending / Math.max(total, 1)) * 100))
    res.healthPercent = health
    res.healthStatus = health >= 80 ? '健康' : health >= 50 ? '一般' : '警告'
    stats.value = { ...stats.value, ...res }
    updateGauge()
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadTrendData = async () => {
  trendLoading.value = true
  try {
    const res = await getDashboardTrend(100)
    if (trendChartInstance) {
      trendChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['温度(°C)', '湿度(%)', '氨气(ppm)'] },
        grid: { left: 50, right: 60, top: 40, bottom: 40 },
        xAxis: { type: 'category', data: res.timestamps || [], axisLabel: { fontSize: 10 } },
        yAxis: [
          { type: 'value', name: '温度/湿度' },
          { type: 'value', name: '氨气' }
        ],
        series: [
          { name: '温度(°C)', type: 'line', smooth: true, data: res.temperature || [], itemStyle: { color: '#F56C6C' }, areaStyle: { color: 'rgba(245,108,108,0.1)' } },
          { name: '湿度(%)', type: 'line', smooth: true, data: res.humidity || [], itemStyle: { color: '#409EFF' }, areaStyle: { color: 'rgba(64,158,255,0.1)' } },
          { name: '氨气(ppm)', type: 'line', smooth: true, yAxisIndex: 1, data: res.ammoniaLevel || [], itemStyle: { color: '#E6A23C' }, areaStyle: { color: 'rgba(230,162,60,0.1)' } }
        ]
      })
    }
  } catch (e) {
    ElMessage.warning('加载趋势数据失败：' + e.message)
  } finally {
    trendLoading.value = false
  }
}

const loadPieData = async () => {
  try {
    const res = await getAnomalyDistribution()
    if (pieChartInstance) {
      pieChartInstance.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: true, formatter: '{b}\n{d}%' },
          data: res.data || [
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
  } catch (e) {
    console.error('加载分布失败', e)
  }
}

const loadBehaviorData = async () => {
  try {
    const res = await request.get('/behavior/all')
    const data = res || []
    const typeCount = {}
    data.forEach(b => {
      const label = { normal: '正常', stress: '应激', abnormal: '异常', feeding: '进食', resting: '休息' }[b.behaviorType] || b.behaviorType
      typeCount[label] = (typeCount[label] || 0) + 1
    })
    const colors = { '正常': '#67C23A', '应激': '#E6A23C', '异常': '#F56C6C', '进食': '#409EFF', '休息': '#909399' }
    const chartData = Object.entries(typeCount).map(([name, value]) => ({ name, value, itemStyle: { color: colors[name] || '#409EFF' } }))

    if (behaviorChartInstance) {
      behaviorChartInstance.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 80, right: 30, top: 20, bottom: 30 },
        xAxis: { type: 'value', name: '数量' },
        yAxis: { type: 'category', data: Object.keys(typeCount), axisLabel: { fontSize: 12 } },
        series: [{
          type: 'bar',
          barWidth: 30,
          data: chartData,
          label: { show: true, position: 'right', fontSize: 12, fontWeight: 'bold' }
        }]
      })
    }
  } catch (e) {
    console.error('加载行为数据失败', e)
  }
}

const updateGauge = () => {
  if (!gaugeChartInstance) return
  const total = (stats.value.anomalyCount || 0) + (stats.value.alarmCount || 0)
  const pending = stats.value.pendingAlarmCount || 0
  const health = total === 0 ? 100 : Math.max(0, Math.round(100 - (pending / Math.max(total, 1)) * 100))

  gaugeChartInstance.setOption({
    series: [{
      data: [{ value: health, name: '健康度' }]
    }]
  })
}

const initCharts = async () => {
  await nextTick()
  if (trendChart.value) trendChartInstance = echarts.init(trendChart.value)
  if (pieChart.value) pieChartInstance = echarts.init(pieChart.value)
  if (behaviorChart.value) behaviorChartInstance = echarts.init(behaviorChart.value)
  if (gaugeChart.value) {
    gaugeChartInstance = echarts.init(gaugeChart.value)
    gaugeChartInstance.setOption({
      series: [{
        type: 'gauge',
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        splitNumber: 10,
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.3, '#F56C6C'],
              [0.7, '#E6A23C'],
              [1, '#67C23A']
            ]
          }
        },
        pointer: { itemStyle: { color: 'auto' }, width: 6 },
        axisTick: { distance: -20, length: 6, lineStyle: { color: '#fff', width: 1 } },
        splitLine: { distance: -24, length: 16, lineStyle: { color: '#fff', width: 2 } },
        axisLabel: { color: 'inherit', distance: 30, fontSize: 11 },
        detail: {
          valueAnimation: true,
          formatter: '{value}%',
          color: 'inherit',
          fontSize: 28,
          fontWeight: 'bold',
          offsetCenter: [0, '70%']
        },
        title: { offsetCenter: [0, '90%'], fontSize: 14, color: '#909399' },
        data: [{ value: 100, name: '健康度' }]
      }]
    })
  }
  await Promise.all([loadTrendData(), loadPieData(), loadBehaviorData()])
}

const handleResize = () => {
  trendChartInstance?.resize()
  pieChartInstance?.resize()
  behaviorChartInstance?.resize()
  gaugeChartInstance?.resize()
}

onMounted(() => {
  initCharts()
  loadStats()
  window.addEventListener('resize', handleResize)
  setInterval(loadStats, 10000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance?.dispose()
  pieChartInstance?.dispose()
  behaviorChartInstance?.dispose()
  gaugeChartInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}
.stat-row .el-col {
  margin-bottom: 0;
}
.stat-card {
  width: 100%;
  box-sizing: border-box;
  min-height: 110px;
  display: flex;
  align-items: stretch;
}
.stat-card :deep(.el-card__body) {
  width: 100%;
  padding: 16px 20px !important;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}
.stat-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
}
.stat-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  line-height: 1.2;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  color: #303133;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}
.sub-line {
  min-height: 16px;
  line-height: 16px;
}
.sub-label {
  font-size: 12px;
  line-height: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
