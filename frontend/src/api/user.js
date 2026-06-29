import api from './index'

export const login = (data) => {
  return api.post('/user/login', data)
}

export const register = (data) => {
  return api.post('/user/register', data)
}

export const getUserInfo = () => {
  return api.get('/user/info')
}
