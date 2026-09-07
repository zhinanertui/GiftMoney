import { createRouter, createWebHistory } from 'vue-router'
import { getToken, clearToken, clearUser } from '../utils/auth'
import { permissionStore } from '../store/permission'
import { getMenus } from '../api/auth'

// 预加载 views 下所有组件，供动态菜单映射
const viewModules = import.meta.glob('../views/**/*.vue')

const publicPaths = ['/login', '/register', '/forgot-password']

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('../views/ForgotPassword.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    children: []
  },
  {
    path: '/no-access',
    name: 'no-access',
    component: () => import('../views/NoAccess.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 记录动态添加的路由名，退出时移除
const dynamicRouteNames = []

function addDynamicMenus(menus) {
  menus.forEach((m) => {
    if (m.menuType === 2 && m.routePath && m.component) {
      const comp = viewModules[`../views/${m.component}.vue`]
      if (comp) {
        router.addRoute('Layout', {
          path: m.routePath,
          name: m.routePath,
          component: comp,
          meta: { title: m.menuName }
        })
        dynamicRouteNames.push(m.routePath)
      }
    }
    if (m.children && m.children.length) {
      addDynamicMenus(m.children)
    }
  })
}

/**
 * 拉取用户菜单/权限并动态注册路由
 */
export async function buildRoutes() {
  const res = await getMenus()
  permissionStore.menus = res.data.menus || []
  permissionStore.perms = res.data.perms || []
  permissionStore.roles = res.data.roles || []
  permissionStore.loaded = true
  addDynamicMenus(permissionStore.menus)
  return permissionStore.menus.length > 0
}

/**
 * 清除动态路由（退出登录/切换角色时调用）
 */
export function resetRoutes() {
  dynamicRouteNames.forEach((name) => {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  })
  dynamicRouteNames.length = 0
  permissionStore.reset()
}

// 登录守卫
router.beforeEach(async (to) => {
  const token = getToken()
  if (!publicPaths.includes(to.path) && !token) {
    return '/login'
  }
  if (publicPaths.includes(to.path) && token) {
    return '/'
  }
  if (token && !permissionStore.loaded) {
    try {
      const hasMenus = await buildRoutes()
      if (!hasMenus) {
        // 无任何菜单权限：进入提示页，避免空白页
        return '/no-access'
      }
      // 重新进入目标路由，确保动态路由已注册
      return { ...to, replace: true }
    } catch (e) {
      clearToken()
      clearUser()
      return '/login'
    }
  }
  // 根路径重定向到第一个可见菜单
  if (to.path === '/' && permissionStore.loaded) {
    const first = firstMenuRoute(permissionStore.menus)
    if (first) {
      return first
    }
  }
  return true
})

/**
 * 取菜单树中第一个菜单路由
 */
function firstMenuRoute(menus) {
  for (const m of menus || []) {
    if (m.menuType === 2 && m.routePath) {
      return m.routePath
    }
    if (m.children && m.children.length) {
      const found = firstMenuRoute(m.children)
      if (found) return found
    }
  }
  return null
}

export default router
