<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="login-left">
      <div class="left-brand">
        <span class="brand-icon">🎁</span>
        <span class="brand-name">礼单送还系统</span>
      </div>
      <div class="left-slogan">
        <h1>记录每一份礼金往来</h1>
        <p>送出去的礼，还回来的情，一目了然</p>
      </div>
      <ul class="left-features">
        <li><span class="dot"></span>礼单管理 · Excel 一键导入</li>
        <li><span class="dot"></span>还礼追踪 · 谁还了谁没还</li>
        <li><span class="dot"></span>统计分析 · 饼图柱状图</li>
      </ul>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-card">
        <h2 class="login-title">欢迎登录</h2>
        <p class="login-subtitle">登录以管理你的礼金往来</p>

        <el-form :model="form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" size="large">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="login-btn" :loading="loading" native-type="submit">
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="links">
          <el-link type="primary" :underline="false" @click="router.push('/register')">注册账号</el-link>
          <el-link type="info" :underline="false" @click="router.push('/forgot-password')">忘记密码</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { encryptPassword } from '../utils/rsa'
import { setToken, setUser } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const encrypted = await encryptPassword(form.password)
    const res = await login({ username: form.username, password: encrypted })
    setToken(res.data.tokenValue)
    setUser(res.data.user)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}
.login-left {
  flex: 0 0 45%;
  background: linear-gradient(150deg, #409eff 0%, #2b5fd9 60%, #1e3a8a 100%);
  color: #fff;
  padding: 60px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.login-left::after {
  content: '';
  position: absolute;
  right: -120px;
  bottom: -120px;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.login-left::before {
  content: '';
  position: absolute;
  right: 40px;
  top: -80px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}
.left-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 60px;
  position: relative;
  z-index: 1;
}
.brand-icon { font-size: 32px; }
.brand-name { font-size: 22px; font-weight: 700; letter-spacing: 1px; }
.left-slogan { position: relative; z-index: 1; }
.left-slogan h1 {
  font-size: 34px;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 16px;
}
.left-slogan p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 48px;
}
.left-features {
  list-style: none;
  position: relative;
  z-index: 1;
}
.left-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  margin-bottom: 18px;
  opacity: 0.92;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
  flex-shrink: 0;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
.login-card { width: 380px; }
.login-title { font-size: 26px; font-weight: 700; color: #1f2d3d; margin-bottom: 8px; }
.login-subtitle { color: #909399; font-size: 14px; margin-bottom: 32px; }
.login-btn {
  width: 100%;
  border-radius: 10px;
  font-size: 16px;
  letter-spacing: 4px;
}
.links {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .login-left { display: none; }
}
</style>
