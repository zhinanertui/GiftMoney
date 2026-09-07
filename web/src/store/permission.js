import { reactive } from 'vue'

/**
 * 全局权限状态（菜单树、按钮权限、角色）
 */
export const permissionStore = reactive({
  menus: [],   // 用户可见菜单树
  perms: [],   // 按钮权限标识列表
  roles: [],   // 角色编码列表
  loaded: false,

  hasPerm(perm) {
    return this.perms.includes(perm)
  },

  hasRole(role) {
    return this.roles.includes(role)
  },

  reset() {
    this.menus = []
    this.perms = []
    this.roles = []
    this.loaded = false
  }
})
