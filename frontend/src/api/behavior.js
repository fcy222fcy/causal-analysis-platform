import api from './index'

export const getBehaviorList = (params) => {
  return api.get('/behavior/list', { params })
}

export const getAllBehaviors = (params) => {
  return api.get('/behavior/all', { params })
}

export const addBehavior = (data) => {
  return api.post('/behavior/add', data)
}
