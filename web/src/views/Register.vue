<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <h2 class="title">注册账号</h2>
      <p class="subtitle">只需账号密码，邮箱选填用于找回密码</p>
      <el-form :model="form" @submit.prevent="handleRegister">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><span>👤</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password>
            <template #prefix><span>🔒</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirm" type="password" placeholder="确认密码" size="large" show-password>
            <template #prefix><span>🔒</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱（选填，用于找回密码）" size="large">
            <template #prefix><span>✉️</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" native-type="submit">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="links">
        <el-link type="primary" @click="router.push('/login')">已有账号，去登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'
import { encryptPassword } from '../utils/rsa'
import { setToken, setUser } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirm: '', email: '' })

async function handleRegister() {
  if (!form.username.trim()) return ElMessage.warning('请输入用户名')
  if (!form.password) return ElMessage.warning('请输入密码')
  if (form.password.length < 6) return ElMessage.warning('密码至少 6 位')
  if (form.password !== form.confirm) return ElMessage.warning('两次输入的密码不一致')

  loading.value = true
  try {
    const encrypted = await encryptPassword(form.password)
    const res = await register({
      username: form.username.trim(),
      password: encrypted,
      email: form.email.trim()
    })
    setToken(res.data.tokenValue)
    setUser(res.data.user)
    ElMessage.success('注册成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(150deg, #409eff 0%, #2b5fd9 60%, #1e3a8a 100%);
}
.auth-card { width: 400px; padding: 12px 8px; }
.title { text-align: center; margin: 8px 0 4px; }
.subtitle { text-align: center; color: #909399; font-size: 13px; margin-bottom: 24px; }
.links { display: flex; justify-content: center; margin-top: 4px; }
</style>
