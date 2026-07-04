<template>
  <Layout>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>溯源分析</span>
          <el-button type="primary" @click="generateReport" :loading="genLoading">生成溯源报告</el-button>
        </div>
      </template>
      <div ref="traceChart" style="height: 400px"></div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>溯源报告</span>
          <el-button type="primary" plain size="small" @click="loadReports" :loading="loading">刷新</el-button>
        </div>
      </template>
      <el-table :data="reports" style="width: 100%" v-loading="loading">
        <el-table-column prop="reportId" label="报告ID" width="100" />
        <el-table-column prop="eventId" label="事件ID" width="100" />
        <el-table-column prop="barnId" label="棚舍ID" width="100" />
        <el-table-column prop="causeChain" label="原因链" show-overflow-tooltip />
        <el-table-column prop="reportContent" label="内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewReport(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="reportDialogVisible" title="溯源报告详情" width="600px">
      <div v-if="currentReport">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="报告ID">{{ currentReport.reportId }}</el-descriptions-item>
          <el-descriptions-item label="关联事件">{{ currentReport.eventId }}</el-descriptions-item>
          <el-descriptions-item label="棚舍">{{ currentReport.barnId }}</el-descriptions-item>
          <el-descriptions-item label="原因链">{{ currentReport.causeChain }}</el-descriptions-item>
          <el-descriptions-item label="内容">
            <div style="white-space: pre-line">{{ currentReport.reportContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentReport.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import Layout from '../components/Layout.vue'
import { getReportList, generateReport as apiGenerate, getReportDetail } from '../api/traceability'
import { ElMessage } from 'element-plus'

const traceChart = ref(null)
const reports = ref([])
const loading = ref(false)
const genLoading = ref(false)
const reportDialogVisible = ref(false)
const currentReport = ref(null)
let chartInstance = null

const initTraceChart = () => {
  if (!traceChart.value) return
  if (!chartInstance) chartInstance = echarts.init(traceChart.value)

  const nodes = [
    { name: '氨气浓度↑', symbolSize: 60, itemStyle: { color: '#E6A23C' }, category: 0 },
    { name: '通风不足', symbolSize: 50, itemStyle: { color: '#909399' }, category: 0 },
    { name: '温度↑', symbolSize: 60, itemStyle: { color: '#F56C6C' }, category: 1 },
    { name: '湿度↑', symbolSize: 60, itemStyle: { color: '#409EFF' }, category: 1 },
    { name: '动物应激', symbolSize: 60, itemStyle: { color: '#E6A23C' }, category: 2 },
    { name: '异常行为', symbolSize: 70, itemStyle: { color: '#9b59b6' }, category: 3 }
  ]
  const links = [
    { source: '通风不足', target: '氨气浓度↑', lineStyle: { width: 3 } },
    { source: '通风不足', target: '温度↑', lineStyle: { width: 3 } },
    { source: '氨气浓度↑', target: '温度↑', lineStyle: { width: 2 } },
    { source: '温度↑', target: '湿度↑', lineStyle: { width: 2 } },
    { source: '温度↑', target: '动物应激', lineStyle: { width: 3 } },
    { source: '湿度↑', target: '动物应激', lineStyle: { width: 2 } },
    { source: '氨气浓度↑', target: '动物应激', lineStyle: { width: 2 } },
    { source: '动物应激', target: '异常行为', lineStyle: { width: 4 } }
  ]

  chartInstance.setOption({
    tooltip: {},
    legend: [{ data: ['源头', '中间', '传导', '结果'] }],
    series: [{
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      categories: [
        { name: '源头' }, { name: '中间' }, { name: '传导' }, { name: '结果' }
      ],
      roam: true,
      draggable: true,
      force: { repulsion: 700, edgeLength: 140 },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 12],
      label: { show: true, fontSize: 13, fontWeight: 600 }
    }]
  })
}

const loadReports = async () => {
  loading.value = true
  try {
    const res = await getReportList({ page: 1, size: 50 })
    reports.value = res.list || []
  } catch (e) {
    ElMessage.error('加载报告失败：' + e.message)
  } finally {
    loading.value = false
  }
}

const generateReport = async () => {
  genLoading.value = true
  try {
    await apiGenerate({})
    ElMessage.success('报告生成成功')
    await loadReports()
  } catch (e) {
    ElMessage.error('生成失败：' + e.message)
  } finally {
    genLoading.value = false
  }
}

const viewReport = async (row) => {
  try {
    currentReport.value = await getReportDetail(row.reportId)
    reportDialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败：' + e.message)
  }
}

const handleResize = () => chartInstance?.resize()

onMounted(async () => {
  await nextTick()
  initTraceChart()
  loadReports()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
