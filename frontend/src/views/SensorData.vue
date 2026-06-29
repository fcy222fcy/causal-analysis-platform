<template>
  <Layout>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>传感器数据</span>
          <el-button type="primary" @click="refreshData">刷新</el-button>
        </div>
      </template>
      <el-table :data="sensorData" style="width: 100%">
        <el-table-column prop="sensorId" label="传感器ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="temperature" label="温度(°C)" />
        <el-table-column prop="humidity" label="湿度(%)" />
        <el-table-column prop="ammoniaLevel" label="氨气浓度(ppm)" />
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
  { sensorId: 'S002', barnId: 1, temperature: 26.2, humidity: 68, ammoniaLevel: 18, timestamp: '2024-01-15 10:05:00' },
  { sensorId: 'S003', barnId: 2, temperature: 24.8, humidity: 62, ammoniaLevel: 12, timestamp: '2024-01-15 10:10:00' }
])

const refreshData = () => {
  console.log('刷新数据')
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
