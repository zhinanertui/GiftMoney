import request from './request'

// ---------- 角色管理 ----------
export function listRoles() {
  return request.get('/roles')
}

export function createRole(data) {
  return request.post('/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/roles/${id}`)
}

export function getRoleMenuIds(id) {
  return request.get(`/roles/${id}/menus`)
}

export function assignRoleMenus(id, menuIds) {
  return request.put(`/roles/${id}/menus`, { menuIds })
}

// ---------- 菜单管理 ----------
export function getMenuTree() {
  return request.get('/menus/tree')
}

export function createMenu(data) {
  return request.post('/menus', data)
}

export function updateMenu(id, data) {
  return request.put(`/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/menus/${id}`)
}

// ---------- 用户角色 ----------
export function getUserRole(userId) {
  return request.get(`/users/${userId}/role`)
}

export function assignUserRole(userId, roleId) {
  return request.put(`/users/${userId}/role`, { roleId })
}
