<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <h2 class="title">找回密码</h2>
      <p class="subtitle">输入用户名和注册邮箱，验证通过后设置新密码</p>
      <el-form :model="form" @submit.prevent="handleReset">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><span>👤</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.email" placeholder="注册时填写的邮箱" size="large">
            <template #prefix><span>✉️</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.newPassword" type="password" placeholder="新密码" size="large" show-password>
            <template #prefix><span>🔒</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirm" type="password" placeholder="确认新密码" size="large" show-password>
            <template #prefix><span>🔒</span></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" native-type="submit">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>
      <div class="links">
        <el-link type="primary" @click="router.push('/login')">返回登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword } from '../api/auth'
import { encryptPassword } from '../utils/rsa'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', email: '', newPassword: '', confirm: '' })

async function handleReset() {
  if (!form.username.trim()) return ElMessage.warning('请输入用户名')
  if (!form.email.trim()) return ElMessage.warning('请输入邮箱')
  if (!form.newPassword) return ElMessage.warning('请输入新密码')
  if (form.newPassword.length < 6) return ElMessage.warning('新密码至少 6 位')
  if (form.newPassword !== form.confirm) return ElMessage.warning('两次输入的密码不一致')

  loading.value = true
  try {
    const encrypted = await encryptPassword(form.newPassword)
    await resetPassword({
      username: form.username.trim(),
      email: form.email.trim(),
      newPassword: encrypted
    })
    ElMessage.success('密码重置成功，请用新密码登录')
    router.push('/login')
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
