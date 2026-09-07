<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
        <el-button v-permission="'user:add'" type="primary" @click="openCreate">新增用户</el-button>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form inline>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="用户名 / 昵称" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 用户表格 -->
    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.roleName" size="small" :type="row.roleName === '管理员' ? 'danger' : 'info'">
            {{ row.roleName }}
          </el-tag>
          <span v-else class="no-role">未分配</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" @click="openAssignRole(row)">分配角色</el-button>
          <el-button v-permission="'user:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'user:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      class="pagination"
      background
      layout="total, prev, pager, next, sizes"
      :total="total"
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :page-sizes="[10, 20, 50]"
      @change="load"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="角色">
          <el-select v-model="form.roleId" placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="assignVisible" :title="`分配角色 - ${currentUser?.username || ''}`" width="400px">
      <el-form label-width="80px">
        <el-form-item label="角色">
          <el-select v-model="assignRoleId" placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAssignRole">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, pageUsers, updateUser } from '../api/user'
import { assignUserRole, getUserRole, listRoles } from '../api/system'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const roles = ref([])
const query = reactive({ current: 1, size: 10, keyword: '' })

const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ username: '', nickname: '', email: '', status: 1, roleId: null })

const assignVisible = ref(false)
const currentUser = ref(null)
const assignRoleId = ref(null)

async function load() {
  loading.value = true
  try {
    const [pageRes, roleRes] = await Promise.all([pageUsers(query), listRoles()])
    roles.value = roleRes.data
    rows.value = pageRes.data.records
    total.value = Number(pageRes.data.total)

    // 为每个用户填充角色名
    const roleMap = Object.fromEntries(roles.value.map((r) => [r.id, r.roleName]))
    await Promise.all(rows.value.map(async (row) => {
      const r = await getUserRole(row.id)
      row.roleId = r.data
      row.roleName = r.data ? roleMap[r.data] : null
    }))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { username: '', nickname: '', email: '', status: 1, roleId: null })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { username: row.username, nickname: row.nickname, email: row.email, status: row.status })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.username) return ElMessage.warning('请填写用户名')
  saving.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, { nickname: form.nickname, email: form.email, status: form.status })
    } else {
      const res = await createUser(form)
      if (form.roleId) {
        await assignUserRole(res.data.id, form.roleId)
      }
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  await load()
}

function openAssignRole(row) {
  currentUser.value = row
  assignRoleId.value = row.roleId
  assignVisible.value = true
}

async function handleAssignRole() {
  saving.value = true
  try {
    await assignUserRole(currentUser.value.id, assignRoleId.value)
    ElMessage.success('分配成功')
    assignVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.no-role { color: #c0c4cc; font-size: 12px; }
</style>
