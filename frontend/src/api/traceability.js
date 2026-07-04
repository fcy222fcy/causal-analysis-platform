import api from './index'

export const getReportList = (params) => {
  return api.get('/traceability/reports', { params })
}

export const getAllReports = (params) => {
  return api.get('/traceability/all', { params })
}

export const getReportDetail = (reportId) => {
  return api.get(`/traceability/report/${reportId}`)
}

export const generateReport = (data) => {
  const params = {}
  if (data?.eventId) params.eventId = data.eventId
  if (data?.barnId) params.barnId = data.barnId
  return api.post('/traceability/report', data, { params })
}
