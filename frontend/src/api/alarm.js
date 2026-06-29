import api from './index'

export const getAlarms = (params) => {
  return api.get('/alarm/list', { params })
}

export const acknowledgeAlarm = (alarmId) => {
  return api.put(`/alarm/${alarmId}/acknowledge`)
}

export const resolveAlarm = (alarmId) => {
  return api.put(`/alarm/${alarmId}/resolve`)
}

export const getAlarmStats = () => {
  return api.get('/alarm/stats')
}
