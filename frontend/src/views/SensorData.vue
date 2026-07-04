<template>
  <Layout>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>传感器数据</span>
          <div class="filters">
            <el-select v-model="filter.barnId" placeholder="棚舍" clearable style="width:140px;margin-right:10px" @change="onFilterChange">
              <el-option v-for="n in 5" :key="n" :label="'棚舍 ' + n" :value="n" />
            </el-select>
            <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="sensorId" label="传感器ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="temperature" label="温度(°C)" />
        <el-table-column prop="humidity" label="湿度(%)" />
        <el-table-column prop="ammoniaLevel" label="氨气(ppm)" />
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
import { getSensorData } from '../api/sensor'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const filter = reactive({ barnId: null })

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filter.barnId) params.barnId = filter.barnId
    const res = await getSensorData(params)
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

onMounted(() => {
  loadData()
  setInterval(() => {
    loadData()
  }, 10000)
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
