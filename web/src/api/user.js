import request from './request'

/**
 * 用户接口
 */
export function pageUsers(params) {
  return request.get('/users', { params })
}

export function listUsers() {
  return request.get('/users/all')
}

export function getUser(id) {
  return request.get(`/users/${id}`)
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}
