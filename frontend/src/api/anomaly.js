import api from './index'

export const getAnomalyList = (params) => {
  return api.get('/anomaly/list', { params })
}

export const getAllAnomalies = (params) => {
  return api.get('/anomaly/all', { params })
}

export const runAnomalyDetection = (barnId) => {
  const params = barnId ? { barnId } : {}
  return api.post('/anomaly/detect', null, { params })
}

export const getAnomalyStats = () => {
  return api.get('/anomaly/stats')
}
