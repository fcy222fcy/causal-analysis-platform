<template>
  <Layout>
    <el-card>
      <template #header>报告中心</template>
      <el-table :data="reports" style="width: 100%">
        <el-table-column prop="reportId" label="报告ID" />
        <el-table-column prop="eventId" label="事件ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="reportContent" label="报告内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewReport(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import Layout from '../components/Layout.vue'

const reports = ref([
  { reportId: 1, eventId: 1, barnId: 1, reportContent: '温度异常升高导致氨气浓度上升，建议加强通风', createTime: '2024-01-15 11:00:00' },
  { reportId: 2, eventId: 2, barnId: 2, reportContent: '动物应激行为与环境变化相关', createTime: '2024-01-15 12:00:00' }
])

const viewReport = (row) => {
  console.log('查看报告', row.reportId)
}
</script>
