<template>
  <div class="empty-wrap">
    <el-empty description="暂无菜单权限">
      <p class="tip">你的账号尚未分配任何角色或菜单，请联系管理员分配权限。</p>
      <el-button type="primary" @click="handleLogout">重新登录</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { clearToken, clearUser } from '../utils/auth'
import { resetRoutes } from '../router'
import { permissionStore } from '../store/permission'

const router = useRouter()

function handleLogout() {
  clearToken()
  clearUser()
  resetRoutes()
  router.push('/login')
}
</script>

<style scoped>
.empty-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.tip {
  color: #909399;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
