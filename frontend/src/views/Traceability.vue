<template>
  <Layout>
    <el-card>
      <template #header>溯源分析</template>
      <div ref="traceChart" style="height: 400px"></div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>溯源报告</template>
      <el-table :data="reports" style="width: 100%">
        <el-table-column prop="reportId" label="报告ID" />
        <el-table-column prop="eventId" label="事件ID" />
        <el-table-column prop="barnId" label="棚舍ID" />
        <el-table-column prop="causeChain" label="原因链" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
    </el-card>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import Layout from '../components/Layout.vue'

const traceChart = ref(null)
const reports = ref([
  { reportId: 1, eventId: 1, barnId: 1, causeChain: '氨气浓度上升 → 温度升高 → 动物应激', createTime: '2024-01-15 11:00:00' }
])

onMounted(() => {
  initTraceChart()
})

const initTraceChart = () => {
  const chart = echarts.init(traceChart.value)

  const nodes = [
    { name: '氨气浓度', symbolSize: 50, itemStyle: { color: '#E6A23C' } },
    { name: '温度', symbolSize: 50, itemStyle: { color: '#409EFF' } },
    { name: '动物应激', symbolSize: 50, itemStyle: { color: '#F56C6C' } }
  ]

  const links = [
    { source: '氨气浓度', target: '温度' },
    { source: '温度', target: '动物应激' }
  ]

  chart.setOption({
    tooltip: {},
    series: [{
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      force: { repulsion: 300 },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 10],
      label: { show: true }
    }]
  })
}
</script>
