<template>
  <el-container class="layout">
    <!-- 桌面端：固定侧边栏 -->
    <el-aside v-if="!isMobile" width="220px" class="aside">
      <div class="brand">
        <span class="brand-icon">🎁</span>
        <span class="brand-name">礼单送还系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#9aa8c0"
        active-text-color="#ffffff"
      >
        <MenuItem v-for="m in permissionStore.menus" :key="m.id" :menu="m" />
      </el-menu>
    </el-aside>

    <!-- 移动端：抽屉菜单 -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="240px" :with-header="false" class="mobile-drawer">
      <div class="drawer-body">
        <div class="brand">
          <span class="brand-icon">🎁</span>
          <span class="brand-name">礼单送还系统</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="transparent"
          text-color="#9aa8c0"
          active-text-color="#ffffff"
          @select="drawerVisible = false"
        >
          <MenuItem v-for="m in permissionStore.menus" :key="m.id" :menu="m" />
        </el-menu>
      </div>
    </el-drawer>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button v-if="isMobile" class="hamburger" text @click="drawerVisible = true">
            <el-icon :size="20"><Menu /></el-icon>
          </el-button>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="user-box">
          <el-tag size="small" type="primary">{{ roleName }}</el-tag>
          <span class="username">{{ user?.nickname || user?.username }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Menu } from '@element-plus/icons-vue'
import MenuItem from '../components/MenuItem.vue'
import { getUser, clearToken, clearUser } from '../utils/auth'
import { permissionStore } from '../store/permission'
import { resetRoutes } from '../router'
import { logout } from '../api/auth'

const route = useRoute()
const router = useRouter()
const user = getUser()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '')
const roleName = computed(() => {
  if (permissionStore.roles.includes('admin')) return '管理员'
  if (permissionStore.roles.includes('tenant')) return '租户'
  return permissionStore.roles[0] || ''
})

// 响应式：<992px 视为移动端（手机/平板竖屏）
const isMobile = ref(false)
const drawerVisible = ref(false)

function handleResize() {
  isMobile.value = window.innerWidth < 992
}

async function handleLogout() {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  try {
    await logout()
  } catch {
    // 忽略登出接口异常
  }
  clearToken()
  clearUser()
  resetRoutes()
  ElMessage.success('已退出')
  router.push('/login')
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.layout { min-height: 100vh; }
.aside {
  background: linear-gradient(180deg, #16213e 0%, #111a33 100%);
}
.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 22px 12px 18px;
  margin: 0 12px 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.brand-icon { font-size: 22px; }
.brand-name { color: #fff; font-size: 16px; font-weight: 600; letter-spacing: 1px; }

.aside :deep(.el-menu) { border-right: none; }
.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  margin: 4px 12px;
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
}
.aside :deep(.el-menu-item:hover),
.aside :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.06);
}
.aside :deep(.el-menu-item.is-active) {
  background: var(--sidebar-active-bg);
  color: #fff;
}
.aside :deep(.el-sub-menu .el-menu) { background: transparent; }

/* 移动端抽屉 */
.mobile-drawer :deep(.el-drawer__body) { padding: 0; }
.drawer-body {
  height: 100%;
  background: linear-gradient(180deg, #16213e 0%, #111a33 100%);
}
.drawer-body .brand { margin-bottom: 8px; }
.drawer-body :deep(.el-menu) { border-right: none; }
.drawer-body :deep(.el-menu-item),
.drawer-body :deep(.el-sub-menu__title) {
  margin: 4px 12px;
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
}
.drawer-body :deep(.el-menu-item.is-active) {
  background: var(--sidebar-active-bg);
  color: #fff;
}
.drawer-body :deep(.el-sub-menu .el-menu) { background: transparent; }

.header {
  background: #fff;
  border-bottom: 1px solid #eef1f6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.hamburger { margin-right: 4px; }
.page-title { font-size: 16px; font-weight: 600; color: #1f2d3d; }
.user-box { display: flex; align-items: center; gap: 10px; }
.username { color: #333; }
.main { background: var(--app-bg); padding: 16px; }

@media (max-width: 768px) {
  .username { display: none; }
  .main { padding: 12px; }
  .header { padding: 0 12px; }
}
</style>
