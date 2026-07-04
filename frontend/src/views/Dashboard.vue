<template>
  <Layout>
    <div class="dashboard">
      <el-row :gutter="20" class="stat-row">
        <el-col :span="6" v-for="(item, idx) in cardList" :key="idx">
          <el-card shadow="hover" class="stat-card" :body-style="{ padding: '16px 20px' }">
            <div class="stat-inner">
              <div class="stat-icon" :style="{ backgroundColor: item.color }">
                <el-icon size="22"><component :is="item.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ item.label }}</div>
                <div class="stat-value">{{ stats[item.key] || 0 }}</div>
                <div class="sub-line">
                  <span v-if="item.subKey && stats[item.subKey] > 0" class="sub-label" :style="{ color: item.subColor || '#F56C6C' }">
                    待处理 {{ stats[item.subKey] }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

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
            <template #header>异常分布</template>
            <div ref="pieChart" style="height: 300px"></div>
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
import { Monitor, Warning, Bell, Connection } from '@element-plus/icons-vue'

const cardList = [
  { label: '传感器数量', key: 'sensorCount',       icon: markRaw(Monitor),    color: '#409EFF' },
  { label: '异常事件',   key: 'anomalyCount',      icon: markRaw(Warning),    color: '#E6A23C' },
  { label: '告警数量',   key: 'alarmCount',        icon: markRaw(Bell),       color: '#F56C6C', subKey: 'pendingAlarmCount' },
  { label: '因果关系',   key: 'causalCount',       icon: markRaw(Connection), color: '#67C23A' }
]

const trendChart = ref(null)
const pieChart = ref(null)
let trendChartInstance = null
let pieChartInstance = null

const stats = ref({
  sensorCount: 0,
  anomalyCount: 0,
  alarmCount: 0,
  causalCount: 0,
  pendingAlarmCount: 0,
  reportCount: 0
})

const trendLoading = ref(false)

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = { ...stats.value, ...res }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadTrendData = async () => {
  trendLoading.value = true
  try {
    const res = await getDashboardTrend(50)
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
          { name: '温度(°C)', type: 'line', smooth: true, data: res.temperature || [], itemStyle: { color: '#F56C6C' } },
          { name: '湿度(%)', type: 'line', smooth: true, data: res.humidity || [], itemStyle: { color: '#409EFF' } },
          { name: '氨气(ppm)', type: 'line', smooth: true, yAxisIndex: 1, data: res.ammoniaLevel || [], itemStyle: { color: '#E6A23C' } }
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

const initCharts = async () => {
  await nextTick()
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
  }
  if (pieChart.value) {
    pieChartInstance = echarts.init(pieChart.value)
  }
  await Promise.all([loadTrendData(), loadPieData()])
}

const handleResize = () => {
  trendChartInstance?.resize()
  pieChartInstance?.resize()
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
