<template>
  <Layout>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>异常检测</span>
          <div class="filters">
            <el-select v-model="filter.barnId" placeholder="棚舍" clearable style="width:140px;margin-right:10px" @change="onFilterChange">
              <el-option v-for="n in 5" :key="n" :label="'棚舍 ' + n" :value="n" />
            </el-select>
            <el-button type="warning" @click="runDetection" :loading="detectLoading">执行检测</el-button>
            <el-button type="primary" @click="loadData" :loading="loading" style="margin-left:8px">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="eventId" label="事件ID" width="100" />
        <el-table-column prop="barnId" label="棚舍ID" width="100" />
        <el-table-column prop="eventType" label="事件类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getEventType(row.eventType)">{{ getEventLabel(row.eventType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severityLevel" label="严重程度" width="120">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severityLevel)">{{ getSeverityLabel(row.severityLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="timestamp" label="时间" width="180" />
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end;display:flex"
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </Layout>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import Layout from '../components/Layout.vue'
import { getAnomalyList, runAnomalyDetection } from '../api/anomaly'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const detectLoading = ref(false)
const filter = reactive({ barnId: null })

const getEventType = (type) => {
  const types = { environmental: 'danger', behavioral: 'warning', composite: 'primary' }
  return types[type] || 'info'
}

const getEventLabel = (type) => {
  const labels = { environmental: '环境异常', behavioral: '行为异常', composite: '复合异常' }
  return labels[type] || type
}

const getSeverityType = (level) => {
  const types = { low: 'info', medium: 'warning', high: 'danger' }
  return types[level] || 'info'
}

const getSeverityLabel = (level) => {
  const labels = { low: '低', medium: '中', high: '高' }
  return labels[level] || level
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filter.barnId) params.barnId = filter.barnId
    const res = await getAnomalyList(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载失败：' + e.message)
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  page.value = 1
  loadData()
}

const runDetection = async () => {
  detectLoading.value = true
  try {
    const res = await runAnomalyDetection(filter.barnId)
    ElMessage.success(`检测完成，发现 ${res.detected || 0} 条新异常`)
    await loadData()
  } catch (e) {
    ElMessage.error('检测失败：' + e.message)
  } finally {
    detectLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filters {
  display: flex;
  align-items: center;
}
</style>
