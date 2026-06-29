import api from './index'

export const getSensorData = (params) => {
  return api.get('/sensor/data', { params })
}

export const getSensorList = () => {
  return api.get('/sensor/list')
}

export const addSensorData = (data) => {
  return api.post('/sensor/data', data)
}

export const batchImportSensorData = (data) => {
  return api.post('/sensor/batch', data)
}
