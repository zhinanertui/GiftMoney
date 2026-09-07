<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>角色管理</span>
        <el-button type="primary" @click="openCreate">新增角色</el-button>
      </div>
    </template>

    <el-table :data="roles" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="200" />
      <el-table-column prop="roleCode" label="角色编码" width="140" />
      <el-table-column prop="roleName" label="角色名称" width="140" />
      <el-table-column prop="remark" label="备注" min-width="160" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openAssign(row)">分配菜单</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.roleCode === 'admin'" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色编码" required>
          <el-input v-model="form.roleCode" :disabled="!!editingId" placeholder="如 manager" />
        </el-form-item>
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" placeholder="如 经理" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog v-model="assignVisible" :title="`分配菜单 - ${currentRole?.roleName || ''}`" width="480px">
      <el-tree
        ref="treeRef"
        :data="treeData"
        show-checkbox
        node-key="id"
        :props="{ label: 'label', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAssign">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  assignRoleMenus, createRole, deleteRole, getMenuTree, getRoleMenuIds, listRoles, updateRole
} from '../api/system'

const loading = ref(false)
const saving = ref(false)
const roles = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ roleCode: '', roleName: '', remark: '' })

const assignVisible = ref(false)
const currentRole = ref(null)
const treeRef = ref(null)
const treeData = ref([])

function toTreeData(menus) {
  return (menus || []).map((m) => ({
    id: m.id,
    label: m.icon ? `${m.icon} ${m.menuName}` : m.menuName,
    children: m.children ? toTreeData(m.children) : []
  }))
}

async function load() {
  loading.value = true
  try {
    const res = await listRoles()
    roles.value = res.data
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { roleCode: '', roleName: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { roleCode: row.roleCode, roleName: row.roleName, remark: row.remark })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.roleCode || !form.roleName) return ElMessage.warning('请填写角色编码和名称')
  saving.value = true
  try {
    if (editingId.value) {
      await updateRole(editingId.value, form)
    } else {
      await createRole(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  await load()
}

async function openAssign(row) {
  currentRole.value = row
  assignVisible.value = true
  // 加载菜单树
  const menuRes = await getMenuTree()
  treeData.value = toTreeData(menuRes.data)
  // 回显已勾选
  const roleMenuRes = await getRoleMenuIds(row.id)
  const ids = roleMenuRes.data || []
  // 等待树渲染后设置勾选
  setTimeout(() => {
    treeRef.value.setCheckedKeys(ids)
  }, 50)
}

async function handleAssign() {
  const checked = treeRef.value.getCheckedKeys()
  const half = treeRef.value.getHalfCheckedKeys()
  const menuIds = [...checked, ...half]
  saving.value = true
  try {
    await assignRoleMenus(currentRole.value.id, menuIds)
    ElMessage.success('分配成功')
    assignVisible.value = false
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
