<template>
  <Layout>
    <el-card>
      <template #header>异常检测</template>
      <el-table :data="anomalies" style="width: 100%">
        <el-table-column prop="eventId" label="事件ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="eventType" label="事件类型">
          <template #default="{ row }">
            <el-tag>{{ row.eventType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severityLevel" label="严重程度">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severityLevel)">{{ row.severityLevel }}</el-tag>
          </template>
        </el-table-column>
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
  { eventId: 2, barnId: 2, eventType: 'behavioral', severityLevel: 'medium', description: '动物应激行为', timestamp: '2024-01-15 11:00:00' },
  { eventId: 3, barnId: 1, eventType: 'environmental', severityLevel: 'low', description: '湿度轻微波动', timestamp: '2024-01-15 09:00:00' }
])

const getSeverityType = (level) => {
  const types = { low: 'info', medium: 'warning', high: 'danger' }
  return types[level] || 'info'
}
</script>
