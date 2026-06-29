<template>
  <Layout>
    <el-card>
      <template #header>行为数据</template>
      <el-table :data="behaviorData" style="width: 100%">
        <el-table-column prop="recordId" label="记录ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="behaviorType" label="行为类型">
          <template #default="{ row }">
            <el-tag :type="getBehaviorType(row.behaviorType)">{{ row.behaviorType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confidenceScore" label="置信度" />
        <el-table-column prop="timestamp" label="时间" />
      </el-table>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const behaviorData = ref([
  { recordId: 1, barnId: 1, behaviorType: 'normal', confidenceScore: 0.95, timestamp: '2024-01-15 10:00:00' },
  { recordId: 2, barnId: 1, behaviorType: 'stress', confidenceScore: 0.88, timestamp: '2024-01-15 10:05:00' },
  { recordId: 3, barnId: 2, behaviorType: 'abnormal', confidenceScore: 0.72, timestamp: '2024-01-15 10:10:00' }
])

const getBehaviorType = (type) => {
  const types = { normal: 'success', stress: 'warning', abnormal: 'danger' }
  return types[type] || 'info'
}
</script>
