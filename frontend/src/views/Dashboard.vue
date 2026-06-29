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
