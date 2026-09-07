import request from './request'

export function getPublicKey() {
  return request.get('/auth/public-key')
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getMe() {
  return request.get('/auth/me')
}

export function getMenus() {
  return request.get('/auth/menus')
}
