<template>
  <Layout>
    <div class="alarm-management">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>告警管理</span>
            <el-button type="primary" @click="refreshAlarms">刷新</el-button>
          </div>
        </template>

        <el-table :data="alarms" style="width: 100%">
          <el-table-column prop="alarmId" label="告警ID" width="100" />
          <el-table-column prop="barnId" label="棚舍ID" width="100" />
          <el-table-column prop="alarmType" label="告警类型" width="120">
            <template #default="{ row }">
              <el-tag>{{ row.alarmType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmLevel" label="告警级别" width="100">
            <template #default="{ row }">
              <el-tag :type="getLevelType(row.alarmLevel)">{{ row.alarmLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmContent" label="告警内容" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="acknowledge(row)" v-if="row.status === 'pending'">
                确认
              </el-button>
              <el-button size="small" type="success" @click="resolve(row)" v-if="row.status !== 'resolved'">
                解决
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const alarms = ref([
  { alarmId: 1, barnId: 1, alarmType: 'temperature', alarmLevel: 'high', alarmContent: '温度过高：35.5°C', status: 'pending', createTime: '2024-01-15 10:30:00' },
  { alarmId: 2, barnId: 2, alarmType: 'ammonia', alarmLevel: 'medium', alarmContent: '氨气浓度超标：28ppm', status: 'acknowledged', createTime: '2024-01-15 11:00:00' },
  { alarmId: 3, barnId: 1, alarmType: 'humidity', alarmLevel: 'low', alarmContent: '湿度过低：35%', status: 'resolved', createTime: '2024-01-15 09:00:00' }
])

const getLevelType = (level) => {
  const types = { low: 'info', medium: 'warning', high: 'danger' }
  return types[level] || 'info'
}

const getStatusType = (status) => {
  const types = { pending: 'warning', acknowledged: 'primary', resolved: 'success' }
  return types[status] || 'info'
}

const refreshAlarms = () => {
  console.log('刷新告警')
}

const acknowledge = (row) => {
  row.status = 'acknowledged'
  console.log('确认告警', row.alarmId)
}

const resolve = (row) => {
  row.status = 'resolved'
  console.log('解决告警', row.alarmId)
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
