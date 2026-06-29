import api from './index'

export const getCausalAnalysis = (params) => {
  return api.post('/causal/analyze', params)
}

export const runCausalAnalysis = (barnId) => {
  return api.post('/causal/analyze', { barn_id: barnId })
}

export const detectAnomalies = (barnId) => {
  return api.post('/causal/detect-anomalies', null, { params: { barn_id: barnId } })
}

export const traceCausalPath = (cause, effect) => {
  return api.get('/causal/trace-path', { params: { cause, effect } })
}

export const getCorrelation = (barnId) => {
  return api.get('/causal/correlation', { params: { barn_id: barnId } })
}
