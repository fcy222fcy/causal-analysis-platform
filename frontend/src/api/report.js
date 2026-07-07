import request from './index'

// 获取报告列表（分页）
export const getReportList = (params) => {
  return request.get('/report/list', { params })
}

// 获取所有报告
export const getAllReports = (params) => {
  return request.get('/report/all', { params })
}

// 获取报告详情
export const getReportDetail = (reportId) => {
  return request.get(`/report/${reportId}`)
}

// 生成溯源报告
export const generateReport = (data) => {
  return request.post('/report/generate', data)
}
