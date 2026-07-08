<template>
  <div class="login-container">
    <div class="brand-panel">
      <div class="brand-bg-decor">
        <div class="circle c1"></div>
        <div class="circle c2"></div>
        <div class="circle c3"></div>
      </div>
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="32" color="#409EFF"><DataBoard /></el-icon>
          <span>智慧养殖溯源</span>
        </div>
        <div class="brand-title">
          环境异常智能溯源系统
        </div>
        <div class="brand-subtitle">
          多维度传感器数据 · 异常检测 · 因果分析 · 全链路溯源
        </div>

        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon icon-blue">
              <el-icon :size="18"><Monitor /></el-icon>
            </div>
            <div class="feature-text">
              <div class="feature-title">实时数据感知</div>
              <div class="feature-desc">温湿度 / 氨气 / 动物行为 多维度采集</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon icon-orange">
              <el-icon :size="18"><Warning /></el-icon>
            </div>
            <div class="feature-text">
              <div class="feature-title">智能异常检测</div>
              <div class="feature-desc">阈值判定 + 行为模式识别 主动告警</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon icon-green">
              <el-icon :size="18"><Connection /></el-icon>
            </div>
            <div class="feature-text">
              <div class="feature-title">因果链路溯源</div>
              <div class="feature-desc">变量级因果关系挖掘 一键生成报告</div>
            </div>
          </div>
        </div>

        <div class="brand-footer">© {{ year }} Smart Farming Platform</div>
      </div>
    </div>

    <div class="login-panel">
      <div class="login-area">
        <el-card shadow="never" class="login-card" :body-style="{ padding: '40px 44px 32px' }">
          <div class="form-title">
            注册账号
            <div class="form-tip">创建您的账号以使用系统</div>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" size="large" @keyup.enter="handleRegister">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                clearable>
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                :type="pwdVisible ? 'text' : 'password'"
                placeholder="请输入密码"
                @keyup.enter="handleRegister">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
                <template #suffix>
                  <el-icon
                    style="cursor:pointer;color:#909399"
                    @click="pwdVisible = !pwdVisible">
                    <component :is="pwdVisible ? View : Hide" />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                :type="pwdVisible ? 'text' : 'password'"
                placeholder="请确认密码"
                @keyup.enter="handleRegister">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-options">
              <span class="link-login" @click="goToLogin">已有账号？立即登录</span>
            </div>

            <el-form-item style="margin-top: 20px; margin-bottom: 12px">
              <el-button
                type="primary"
                @click="handleRegister"
                :loading="loading"
                style="width:100%; height: 44px; font-size: 15px; letter-spacing: 2px; border-radius: 6px">
                注 册
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <div class="panel-footer">
          如无法注册，请确认后端服务已启动 · Base URL: http://localhost:8080
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DataBoard, Monitor, Warning, Connection,
  User, Lock, View, Hide
} from '@element-plus/icons-vue'
import { register } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const pwdVisible = ref(false)

const year = computed(() => new Date().getFullYear())

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' },
             { min: 3, message: '用户名至少 3 位', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' },
             { min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' },
                    { validator: validateConfirmPassword, trigger: 'blur' }]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await register({ username: form.username, password: form.password })
    ElMessage.success('注册成功，请登录')
    setTimeout(() => router.push('/login'), 1000)
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}

/* ====== 左侧品牌区（和侧边栏同色 #304156） ====== */
.brand-panel {
  position: relative;
  flex: 0 0 44%;
  background: #304156;
  color: #fff;
  overflow: hidden;
}
.brand-bg-decor {
  position: absolute;
  inset: 0;
  overflow: hidden;
  opacity: .35;
}
.brand-bg-decor .circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(64, 158, 255, .9), rgba(64, 158, 255, 0));
  filter: blur(20px);
}
.circle.c1 { width: 360px; height: 360px; top: -100px; right: -80px; }
.circle.c2 { width: 280px; height: 280px; bottom: 80px;  left: 60px; opacity: .7;
  background: radial-gradient(circle at 50% 50%, rgba(103, 194, 58, .8), rgba(103, 194, 58, 0)); }
.circle.c3 { width: 200px; height: 200px; top: 40%;   left: 45%; opacity: .6;
  background: radial-gradient(circle at 50% 50%, rgba(230, 162, 60, .8), rgba(230, 162, 60, 0)); }

.brand-content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: 64px 56px 40px;
  display: flex;
  flex-direction: column;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  background: rgba(255, 255, 255, .06);
  padding: 10px 16px;
  border-radius: 10px;
  width: fit-content;
  border: 1px solid rgba(255,255,255,.08);
  backdrop-filter: blur(6px);
}

.brand-title {
  margin-top: 72px;
  font-size: 38px;
  line-height: 1.25;
  font-weight: 800;
  letter-spacing: 1px;
}
.brand-subtitle {
  margin-top: 14px;
  color: rgba(255, 255, 255, .65);
  font-size: 15px;
  line-height: 1.7;
  max-width: 460px;
}

.feature-list {
  margin-top: 56px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(255, 255, 255, .05);
  border: 1px solid rgba(255, 255, 255, .07);
  transition: background .2s;
}
.feature-item:hover {
  background: rgba(255, 255, 255, .09);
}
.feature-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.icon-blue   { background: rgba(64, 158, 255, .25); color: #409EFF; }
.icon-orange { background: rgba(230, 162, 60, .25); color: #E6A23C; }
.icon-green  { background: rgba(103, 194, 58, .25); color: #67C23A; }
.feature-title { font-size: 15px; font-weight: 600; }
.feature-desc  { margin-top: 4px; font-size: 13px; color: rgba(255,255,255,.6); line-height: 1.6; }

.brand-footer {
  margin-top: auto;
  font-size: 12px;
  color: rgba(255,255,255,.35);
}

/* ====== 右侧登录区 ====== */
.login-panel {
  flex: 1;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.login-panel::before {
  content: '';
  position: absolute;
  top: 0; right: 0;
  width: 420px; height: 420px;
  background: radial-gradient(circle at 70% 0%, rgba(64,158,255,.08), transparent 70%);
  pointer-events: none;
}

.login-area {
  width: 100%;
  max-width: 480px;
  padding: 0 40px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.login-card {
  width: 100%;
  border-radius: 10px;
  border: 1px solid #eef0f3;
  box-shadow: 0 10px 30px -12px rgba(48, 65, 86, .12);
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 28px;
}
.form-tip {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 400;
  color: #909399;
  letter-spacing: 0;
}

.form-options {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 13px;
  color: #606266;
  margin-top: -4px;
}

.link-login {
  color: #409EFF;
  cursor: pointer;
  font-size: 13px;
}
.link-login:hover {
  text-decoration: underline;
}

.panel-footer {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}

/* 响应式：窄屏隐藏左侧品牌区 */
@media (max-width: 900px) {
  .brand-panel { display: none; }
}
</style>
