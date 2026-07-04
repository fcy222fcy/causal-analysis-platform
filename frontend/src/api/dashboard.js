import api from './index'

export const getDashboardStats = () => {
  return api.get('/dashboard/stats')
}

export const getDashboardTrend = (limit = 50) => {
  return api.get('/dashboard/trend', { params: { limit } })
}

export const getAnomalyDistribution = () => {
  return api.get('/dashboard/anomaly-distribution')
}
