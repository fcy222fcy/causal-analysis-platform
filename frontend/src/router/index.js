import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/sensor-data',
    name: 'SensorData',
    component: () => import('../views/SensorData.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/behavior-data',
    name: 'BehaviorData',
    component: () => import('../views/BehaviorData.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/anomaly-detection',
    name: 'AnomalyDetection',
    component: () => import('../views/AnomalyDetection.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/causal-analysis',
    name: 'CausalAnalysis',
    component: () => import('../views/CausalAnalysis.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/traceability',
    name: 'Traceability',
    component: () => import('../views/Traceability.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/alarm-management',
    name: 'AlarmManagement',
    component: () => import('../views/AlarmManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/report',
    name: 'Report',
    component: () => import('../views/Report.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
