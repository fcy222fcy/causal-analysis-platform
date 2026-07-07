<template>
  <Layout>
    <div class="page-wrap">
      <el-card class="page-card">
        <template #header>
          <div class="card-header">
            <span>行为数据</span>
            <div class="filters">
              <el-select v-model="filter.barnId" placeholder="棚舍" clearable style="width:140px;margin-right:10px" @change="onFilterChange">
                <el-option label="全部棚舍" :value="null" />
                <el-option v-for="n in 5" :key="n" :label="'棚舍 ' + n" :value="n" />
              </el-select>
              <el-select v-model="filter.behaviorType" placeholder="行为类型" clearable style="width:160px;margin-right:10px" @change="onFilterChange">
                <el-option label="正常" value="normal" />
                <el-option label="应激" value="stress" />
                <el-option label="异常" value="abnormal" />
                <el-option label="进食" value="feeding" />
                <el-option label="休息" value="resting" />
              </el-select>
              <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
            </div>
          </div>
        </template>

        <div class="card-body-wrap">
          <el-table
            :data="tableData"
            style="width: 100%;"
            :height="tableHeight"
            v-loading="loading"
            stripe>
            <el-table-column prop="recordId" label="记录ID" width="120" align="center" />
            <el-table-column prop="barnId" label="棚舍ID" width="120" align="center" />
            <el-table-column prop="behaviorType" label="行为类型" min-width="140" align="center">
              <template #default="{ row }">
                <el-tag :type="getBehaviorType(row.behaviorType)" effect="dark">{{ getBehaviorLabel(row.behaviorType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="confidenceScore" label="置信度" min-width="220" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="Math.round((row.confidenceScore || 0) * 100)"
                  :stroke-width="16"
                  :text-inside="true"
                  :color="progressColor((row.confidenceScore || 0) * 100)" />
              </template>
            </el-table-column>
            <el-table-column prop="timestamp" label="时间" min-width="220" align="center" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[20, 50, 100, 200]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="onPageSizeChange"
            @current-change="loadData"
          />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import Layout from '../components/Layout.vue'
import { getBehaviorList } from '../api/behavior'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(50)
const loading = ref(false)
const filter = reactive({ barnId: null, behaviorType: null })
const tableHeight = ref('100%')

const getBehaviorType = (type) => {
  const types = { normal: 'success', stress: 'warning', abnormal: 'danger', feeding: 'primary', resting: 'info' }
  return types[type] || 'info'
}
const getBehaviorLabel = (type) => {
  const labels = { normal: '正常', stress: '应激', abnormal: '异常', feeding: '进食', resting: '休息' }
  return labels[type] || type
}
const progressColor = (p) => {
  if (p >= 90) return '#67C23A'
  if (p >= 70) return '#409EFF'
  if (p >= 50) return '#E6A23C'
  return '#F56C6C'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filter.barnId) params.barnId = filter.barnId
    if (filter.behaviorType) params.behaviorType = filter.behaviorType
    const res = await getBehaviorList(params)
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
const onPageSizeChange = () => {
  page.value = 1
  loadData()
  nextTick(updateTableHeight)
}

const updateTableHeight = () => {
  const card = document.querySelector('.page-card')
  const header = document.querySelector('.page-card > .el-card__header')
  const pag = document.querySelector('.pagination-wrap')
  if (!card || !header || !pag) { return }
  const avail = card.clientHeight - header.offsetHeight - pag.offsetHeight - 24
  tableHeight.value = Math.max(220, avail)
}

let timer = null
let resizeHandler = null

onMounted(() => {
  loadData()
  timer = setInterval(() => { loadData() }, 12000)
  nextTick(() => {
    updateTableHeight()
    setTimeout(updateTableHeight, 200)
  })
  resizeHandler = updateTableHeight
  window.addEventListener('resize', resizeHandler)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
})
</script>

<style scoped>
.page-wrap {
  width: 100%;
  height: 100%;
}
.page-card {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}
.page-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  min-height: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filters {
  display: flex;
  align-items: center;
}
.card-body-wrap {
  flex: 1;
  padding: 16px 16px 0 16px;
  min-height: 0;
  overflow: hidden;
}
.pagination-wrap {
  padding: 12px 16px 16px 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-top: 1px solid #f0f2f5;
  background: #fafbfc;
  gap: 12px;
}
.pagination-text {
  font-size: 13px;
  color: #606266;
}
</style>
