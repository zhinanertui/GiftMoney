import { permissionStore } from '../store/permission'

/**
 * 按钮级权限指令
 * 用法：v-permission="'gift:add'" —— 无该权限时移除元素
 */
export default {
  mounted(el, binding) {
    const perm = binding.value
    if (perm && !permissionStore.perms.includes(perm)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
