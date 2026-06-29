<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #304156">
      <div class="logo">智慧养殖溯源</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/sensor-data">
          <el-icon><Monitor /></el-icon>
          <span>传感器数据</span>
        </el-menu-item>
        <el-menu-item index="/behavior-data">
          <el-icon><VideoCamera /></el-icon>
          <span>行为数据</span>
        </el-menu-item>
        <el-menu-item index="/anomaly-detection">
          <el-icon><Warning /></el-icon>
          <span>异常检测</span>
        </el-menu-item>
        <el-menu-item index="/causal-analysis">
          <el-icon><Connection /></el-icon>
          <span>因果分析</span>
        </el-menu-item>
        <el-menu-item index="/traceability">
          <el-icon><Share /></el-icon>
          <span>溯源分析</span>
        </el-menu-item>
        <el-menu-item index="/alarm-management">
          <el-icon><Bell /></el-icon>
          <span>告警管理</span>
        </el-menu-item>
        <el-menu-item index="/report">
          <el-icon><Document /></el-icon>
          <span>报告中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee">
        <span>{{ route.name }}</span>
        <el-dropdown>
          <span class="el-dropdown-link">
            {{ user?.username || '管理员' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background-color: #f5f7fa">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user'))
  } catch {
    return null
  }
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style>
