<template>
  <Layout>
    <div class="causal-analysis">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>因果关系图</span>
            <div class="filters">
              <el-select v-model="barnId" placeholder="棚舍" clearable style="width:140px;margin-right:10px">
                <el-option v-for="n in 10" :key="n" :label="'棚舍 ' + n" :value="n" />
              </el-select>
              <el-button type="primary" @click="analyzeCausal" :loading="loading">
                执行分析
              </el-button>
            </div>
          </div>
        </template>
        <div ref="causalGraph" style="height: 500px"></div>
      </el-card>

      <el-card style="margin-top: 20px">
        <template #header>因果路径</template>
        <el-table :data="causalPaths" style="width: 100%" v-loading="loading">
          <el-table-column prop="cause" label="原因变量">
            <template #default="{ row }">{{ getVarLabel(row.cause) }}</template>
          </el-table-column>
          <el-table-column prop="effect" label="结果变量">
            <template #default="{ row }">{{ getVarLabel(row.effect) }}</template>
          </el-table-column>
          <el-table-column prop="strength" label="因果强度" width="200">
            <template #default="{ row }">
              <el-progress :percentage="Math.round((row.strength || 0) * 100)" :stroke-width="16" />
            </template>
          </el-table-column>
          <el-table-column prop="direction" label="方向" width="140">
            <template #default="{ row }">
              <el-tag :type="row.direction === 'positive' ? 'success' : 'danger'">
                {{ row.direction === 'positive' ? '正向 +' : '负向 -' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import Layout from '../components/Layout.vue'
import { runCausalAnalysis } from '../api/causal'
import { ElMessage } from 'element-plus'

const causalGraph = ref(null)
const loading = ref(false)
const barnId = ref(1)
const causalPaths = ref([])
let chartInstance = null

const varLabels = {
  temperature: '温度',
  humidity: '湿度',
  ammonia_level: '氨气浓度',
  stress: '应激反应',
  abnormal_behavior: '异常行为'
}
const varColors = {
  temperature: '#409EFF',
  humidity: '#67C23A',
  ammonia_level: '#E6A23C',
  stress: '#F56C6C',
  abnormal_behavior: '#909399'
}
const getVarLabel = (v) => varLabels[v] || v

const initCausalGraph = (graphData) => {
  if (!causalGraph.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(causalGraph.value)
  }
  const nodes = (graphData?.nodes || []).map(n => ({
    name: n.name,
    symbolSize: 82,
    itemStyle: Object.assign(
      { borderColor: '#ffffff', borderWidth: 3, shadowBlur: 10, shadowColor: 'rgba(0,0,0,.15)' },
      n.itemStyle || { color: varColors[n.name] || '#8e44ad' }
    ),
    label: {
      show: true,
      position: 'inside',
      color: '#ffffff',
      fontSize: 14,
      fontWeight: 700,
      lineHeight: 18,
      align: 'center',
      verticalAlign: 'middle',
      formatter: (p) => getVarLabel(p.name)
    }
  }))
  const edges = (graphData?.edges || []).map(e => ({
    source: e.source,
    target: e.target,
    strength: e.strength,
    lineStyle: { width: Math.max(2, (e.strength || 0.5) * 5), curveness: 0.1, color: '#9aa7bd' },
    label: {
      show: true,
      position: 'middle',
      backgroundColor: '#ffffff',
      padding: [3, 7],
      borderRadius: 4,
      color: '#606266',
      fontSize: 11,
      formatter: e.strength ? (Math.round(e.strength * 100) + '%') : ''
    }
  }))

  chartInstance.setOption({
    tooltip: {
      formatter: (p) => {
        if (p.dataType === 'edge') {
          return `${getVarLabel(p.data.source)} → ${getVarLabel(p.data.target)}<br/>强度: ${Math.round((p.data.strength || 0) * 100)}%`
        }
        return getVarLabel(p.name)
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      focusNodeAdjacency: true,
      data: nodes,
      links: edges,
      force: { repulsion: 800, edgeLength: 200, gravity: 0.08 },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 14],
      label: { fontSize: 14, fontWeight: 700 }
    }]
  }, true)
}

const analyzeCausal = async () => {
  if (!barnId.value) {
    ElMessage.warning('请先选择棚舍')
    return
  }
  loading.value = true
  try {
    const res = await runCausalAnalysis(barnId.value)
    causalPaths.value = res.causal_paths || []
    initCausalGraph(res.causal_graph || res.graph || null)
    ElMessage.success(res.message || '分析完成')
  } catch (e) {
    ElMessage.error('分析失败：' + e.message)
  } finally {
    loading.value = false
  }
}

const handleResize = () => chartInstance?.resize()

onMounted(async () => {
  await nextTick()
  if (barnId.value) {
    analyzeCausal()
  }
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
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
