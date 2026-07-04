import api from './index'

export const getSensorData = (params) => {
  return api.get('/sensor/data', { params })
}

export const getAllSensorData = (params) => {
  return api.get('/sensor/data/all', { params })
}

export const getSensorList = (params) => {
  return api.get('/sensor/list', { params })
}

export const getSensorStats = () => {
  return api.get('/sensor/stats')
}

export const getSensorTrend = (params) => {
  return api.get('/sensor/trend', { params })
}

export const addSensorData = (data) => {
  return api.post('/sensor/data', data)
}

export const batchImportSensorData = (data) => {
  return api.post('/sensor/batch', data)
}
