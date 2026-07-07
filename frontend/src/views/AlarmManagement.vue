<template>
  <Layout>
    <div class="alarm-management">
      <!-- WebSocket 实时告警通知 -->
      <el-alert
        v-if="wsNotification"
        :title="wsNotification.alarmContent"
        type="error"
        show-icon
        closable
        @close="wsNotification = null"
        style="margin-bottom: 16px;"
      />

      <el-card>
        <template #header>
          <div class="card-header">
            <span>告警管理</span>
            <div class="filters">
              <el-tag v-if="wsConnected" type="success" size="small" style="margin-right: 10px;">
                WebSocket 已连接
              </el-tag>
              <el-tag v-else type="info" size="small" style="margin-right: 10px;">
                WebSocket 未连接
              </el-tag>
              <el-select v-model="filter.barnId" placeholder="棚舍" clearable style="width:140px;margin-right:10px" @change="onFilterChange">
                <el-option v-for="n in 5" :key="n" :label="'棚舍 ' + n" :value="n" />
              </el-select>
              <el-select v-model="filter.status" placeholder="状态" clearable style="width:140px;margin-right:10px" @change="onFilterChange">
                <el-option label="待处理" value="pending" />
                <el-option label="已确认" value="acknowledged" />
                <el-option label="已解决" value="resolved" />
              </el-select>
              <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
            </div>
          </div>
        </template>

        <el-table :data="alarms" style="width: 100%" v-loading="loading">
          <el-table-column prop="alarmId" label="告警ID" width="90" />
          <el-table-column prop="barnId" label="棚舍ID" width="90" />
          <el-table-column prop="alarmType" label="告警类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getAlarmTypeTag(row.alarmType)">{{ getAlarmTypeLabel(row.alarmType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmLevel" label="告警级别" width="100">
            <template #default="{ row }">
              <el-tag :type="getLevelType(row.alarmLevel)">{{ getLevelLabel(row.alarmLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmContent" label="告警内容" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                plain
                @click="acknowledge(row)"
                v-if="row.status === 'pending'"
                :loading="row._loading">
                确认
              </el-button>
              <el-button
                size="small"
                type="success"
                @click="resolve(row)"
                v-if="row.status !== 'resolved'"
                :loading="row._loading"
                style="margin-left:4px">
                解决
              </el-button>
            </template>
          </el-table-column>
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
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import Layout from '../components/Layout.vue'
import { getAlarms, acknowledgeAlarm, resolveAlarm } from '../api/alarm'
import { ElMessage } from 'element-plus'

const alarms = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const filter = reactive({ barnId: null, status: null })

// WebSocket 相关
const wsConnected = ref(false)
const wsNotification = ref(null)
let ws = null
let pollTimer = null

const getAlarmTypeTag = (type) => {
  const types = {
    temperature: 'danger',
    humidity: 'warning',
    ammonia: 'primary',
    behavior: 'info',
    composite: 'danger'
  }
  return types[type] || 'info'
}

const getAlarmTypeLabel = (type) => {
  const labels = {
    temperature: '温度告警',
    humidity: '湿度告警',
    ammonia: '氨气告警',
    behavior: '行为告警',
    composite: '复合告警'
  }
  return labels[type] || type
}

const getLevelType = (level) => {
  const types = { low: 'info', medium: 'warning', high: 'danger' }
  return types[level] || 'info'
}

const getLevelLabel = (level) => {
  const labels = { low: '低', medium: '中', high: '高' }
  return labels[level] || level
}

const getStatusType = (status) => {
  const types = { pending: 'warning', acknowledged: 'primary', resolved: 'success' }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = { pending: '待处理', acknowledged: '已确认', resolved: '已解决' }
  return labels[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filter.barnId) params.barnId = filter.barnId
    if (filter.status) params.status = filter.status
    const res = await getAlarms(params)
    alarms.value = (res.list || []).map(a => ({ ...a, _loading: false }))
    total.value = res.total || 0
  } catch (e) {
    console.error('加载告警失败', e)
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  page.value = 1
  loadData()
}

const acknowledge = async (row) => {
  row._loading = true
  try {
    await acknowledgeAlarm(row.alarmId)
    row.status = 'acknowledged'
    ElMessage.success('告警已确认')
  } catch (e) {
    console.error('确认失败', e)
  } finally {
    row._loading = false
  }
}

const resolve = async (row) => {
  row._loading = true
  try {
    await resolveAlarm(row.alarmId)
    row.status = 'resolved'
    ElMessage.success('告警已解决')
  } catch (e) {
    console.error('解决失败', e)
  } finally {
    row._loading = false
  }
}

/**
 * 建立 WebSocket 连接
 * 订阅 /topic/alarms 接收实时告警推送
 */
const connectWebSocket = () => {
  try {
    // 使用原生 WebSocket 连接 STOMP 端点
    const socket = new WebSocket('ws://localhost:8080/ws')

    socket.onopen = () => {
      wsConnected.value = true
      // 发送 STOMP CONNECT 帧
      socket.send('CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0')
    }

    socket.onmessage = (event) => {
      const message = event.data
      // 处理 STOMP 消息
      if (message.includes('MESSAGE') && message.includes('/topic/alarms')) {
        // 提取消息体（JSON）
        const bodyMatch = message.match(/\n\n(.+)/s)
        if (bodyMatch) {
          try {
            const alarm = JSON.parse(bodyMatch[1])
            wsNotification.value = alarm
            ElMessage.error(`实时告警: ${alarm.alarmContent}`)

            // 将新告警插入列表顶部
            alarms.value.unshift({ ...alarm, _loading: false })
            total.value++
          } catch (e) {
            console.error('解析告警消息失败', e)
          }
        }
      }
    }

    socket.onclose = () => {
      wsConnected.value = false
      // 5秒后重连
      setTimeout(connectWebSocket, 5000)
    }

    socket.onerror = (error) => {
      console.error('WebSocket 连接错误:', error)
      wsConnected.value = false
    }

    ws = socket
  } catch (e) {
    console.error('WebSocket 连接失败:', e)
    wsConnected.value = false
  }
}

/**
 * 断开 WebSocket 连接
 */
const disconnectWebSocket = () => {
  if (ws) {
    ws.close()
    ws = null
  }
}

onMounted(() => {
  loadData()
  // 轮询间隔改为30秒（配合WebSocket使用）
  pollTimer = setInterval(() => {
    if (!loading.value) loadData()
  }, 30000)
  // 建立 WebSocket 连接
  connectWebSocket()
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
  disconnectWebSocket()
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
