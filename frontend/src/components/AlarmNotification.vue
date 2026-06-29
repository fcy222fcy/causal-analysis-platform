<template>
  <div class="alarm-notification" v-if="visible">
    <el-alert
      :title="alarm.content"
      :type="getAlertType(alarm.level)"
      show-icon
      closable
      @close="visible = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  alarm: {
    type: Object,
    default: () => ({ content: '', level: 'low' })
  }
})

const visible = ref(true)

const getAlertType = (level) => {
  const types = { low: 'info', medium: 'warning', high: 'error' }
  return types[level] || 'info'
}

onMounted(() => {
  // 5秒后自动关闭
  setTimeout(() => {
    visible.value = false
  }, 5000)
})
</script>

<style scoped>
.alarm-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  width: 350px;
}
</style>
