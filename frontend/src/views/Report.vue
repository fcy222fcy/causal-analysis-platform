<template>
  <Layout>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报告中心</span>
          <div class="filters">
            <el-select v-model="filter.barnId" placeholder="棚舍" clearable style="width:140px;margin-right:10px" @change="onFilterChange">
              <el-option v-for="n in 5" :key="n" :label="'棚舍 ' + n" :value="n" />
            </el-select>
            <el-button type="primary" size="small" @click="loadData" :loading="loading">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="reports" style="width: 100%" v-loading="loading">
        <el-table-column prop="reportId" label="报告ID" width="100" />
        <el-table-column prop="eventId" label="事件ID" width="100" />
        <el-table-column prop="barnId" label="棚舍ID" width="100" />
        <el-table-column prop="causeChain" label="原因链" show-overflow-tooltip />
        <el-table-column prop="reportContent" label="报告内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewReport(row)">查看</el-button>
            <el-button size="small" @click="exportReport(row)">导出</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length === 0" description="暂无报告，可前往【溯源分析】生成" />

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

    <el-dialog v-model="dialogVisible" title="报告详情" width="680px">
      <div v-if="currentReport" class="report-detail">
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="报告ID">{{ currentReport.reportId }}</el-descriptions-item>
          <el-descriptions-item label="棚舍">{{ currentReport.barnId }}</el-descriptions-item>
          <el-descriptions-item label="关联事件ID">{{ currentReport.eventId }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentReport.createTime }}</el-descriptions-item>
          <el-descriptions-item label="原因链" :span="2">
            <el-tag v-for="(c, i) in causeList" :key="i" style="margin:2px" :type="i === causeList.length - 1 ? 'danger' : 'warning'">
              {{ c }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">报告分析</el-divider>
        <div class="content-box">{{ currentReport.reportContent }}</div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="exportReport(currentReport)">导出报告</el-button>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import Layout from '../components/Layout.vue'
import { getReportList, getReportDetail } from '../api/traceability'
import { ElMessage } from 'element-plus'

const reports = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const dialogVisible = ref(false)
const currentReport = ref(null)
const filter = reactive({ barnId: null })

const causeList = computed(() => {
  if (!currentReport.value?.causeChain) return []
  return currentReport.value.causeChain.split(/[→,，、\n]/).map(s => s.trim()).filter(Boolean)
})

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filter.barnId) params.barnId = filter.barnId
    const res = await getReportList(params)
    reports.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  page.value = 1
  loadData()
}

const viewReport = async (row) => {
  try {
    currentReport.value = await getReportDetail(row.reportId)
    dialogVisible.value = true
  } catch (e) {
    console.error('加载详情失败', e)
  }
}

const exportReport = (row) => {
  if (!row) return
  const text = `【智慧养殖溯源报告】
报告ID: ${row.reportId}
关联事件: ${row.eventId}
棚舍: ${row.barnId}
创建时间: ${row.createTime}
原因链: ${row.causeChain || '-'}
分析内容: ${row.reportContent || '-'}
`
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `report-${row.reportId}.txt`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(() => loadData())
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
.report-detail {
  padding: 10px;
}
.content-box {
  padding: 14px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
  color: #303133;
}
</style>
