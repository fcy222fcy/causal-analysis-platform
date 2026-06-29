<template>
  <Layout>
    <div class="causal-analysis">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>因果关系图</span>
            <el-button type="primary" @click="analyzeCausal" :loading="loading">
              执行分析
            </el-button>
          </div>
        </template>
        <div ref="causalGraph" style="height: 500px"></div>
      </el-card>

      <el-card style="margin-top: 20px">
        <template #header>因果路径</template>
        <el-table :data="causalPaths" style="width: 100%">
          <el-table-column prop="cause" label="原因变量" />
          <el-table-column prop="effect" label="结果变量" />
          <el-table-column prop="strength" label="因果强度">
            <template #default="{ row }">
              <el-progress :percentage="Math.round(row.strength * 100)" />
            </template>
          </el-table-column>
          <el-table-column prop="direction" label="方向">
            <template #default="{ row }">
              <el-tag :type="row.direction === 'positive' ? 'success' : 'danger'">
                {{ row.direction === 'positive' ? '正向' : '负向' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import Layout from '../components/Layout.vue'

const causalGraph = ref(null)
const loading = ref(false)
const causalPaths = ref([
  { cause: 'ammonia_level', effect: 'temperature', strength: 0.85, direction: 'positive' },
  { cause: 'temperature', effect: 'humidity', strength: 0.72, direction: 'negative' },
  { cause: 'ammonia_level', effect: 'humidity', strength: 0.68, direction: 'positive' }
])

onMounted(() => {
  initCausalGraph()
})

const initCausalGraph = () => {
  const chart = echarts.init(causalGraph.value)

  const nodes = [
    { name: 'temperature', symbolSize: 50, itemStyle: { color: '#409EFF' }, label: { show: true } },
    { name: 'humidity', symbolSize: 50, itemStyle: { color: '#67C23A' }, label: { show: true } },
    { name: 'ammonia_level', symbolSize: 50, itemStyle: { color: '#E6A23C' }, label: { show: true } }
  ]

  const links = [
    { source: 'ammonia_level', target: 'temperature', lineStyle: { width: 3 } },
    { source: 'temperature', target: 'humidity', lineStyle: { width: 2 } },
    { source: 'ammonia_level', target: 'humidity', lineStyle: { width: 2 } }
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
      label: { position: 'right' }
    }]
  })
}

const analyzeCausal = async () => {
  loading.value = true
  try {
    // 调用API执行分析
    console.log('执行因果分析')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
