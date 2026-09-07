<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>菜单管理</span>
        <el-button type="primary" @click="openCreate(null)">新增菜单</el-button>
      </div>
    </template>

    <el-table
      :data="tree"
      row-key="id"
      default-expand-all
      v-loading="loading"
      border
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="menuName" label="菜单名称" min-width="180">
        <template #default="{ row }">{{ row.icon }} {{ row.menuName }}</template>
      </el-table-column>
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.menuType)">{{ typeName(row.menuType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="routePath" label="路由地址" width="120" />
      <el-table-column prop="component" label="组件名" width="130" />
      <el-table-column prop="perms" label="权限标识" width="130" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openCreate(row)">新增子项</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜单' : '新增菜单'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="父菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'label', children: 'children', value: 'id' }"
            check-strictly
            :render-after-expand="false"
            clearable
            placeholder="不选则为顶级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" required>
          <el-radio-group v-model="form.menuType">
            <el-radio-button :value="1">目录</el-radio-button>
            <el-radio-button :value="2">菜单</el-radio-button>
            <el-radio-button :value="3">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" required>
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 2" label="路由地址">
          <el-input v-model="form.routePath" placeholder="如 /gifts" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 2" label="组件名">
          <el-input v-model="form.component" placeholder="如 GiftList" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 3" label="权限标识">
          <el-input v-model="form.perms" placeholder="如 gift:add" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="emoji 图标，如 📋" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createMenu, deleteMenu, getMenuTree, updateMenu } from '../api/system'

const loading = ref(false)
const saving = ref(false)
const tree = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ parentId: null, menuType: 2, menuName: '', routePath: '', component: '', icon: '', perms: '', sort: 0 })

const parentOptions = ref([])

function typeName(t) {
  return t === 1 ? '目录' : t === 2 ? '菜单' : '按钮'
}
function typeTag(t) {
  return t === 1 ? 'warning' : t === 2 ? 'primary' : 'info'
}

// 供 el-tree-select 的父级选项（排除按钮）
function toOptions(menus) {
  return (menus || [])
    .filter((m) => m.menuType !== 3)
    .map((m) => ({
      id: m.id,
      label: m.icon ? `${m.icon} ${m.menuName}` : m.menuName,
      children: m.children ? toOptions(m.children) : []
    }))
}

async function load() {
  loading.value = true
  try {
    const res = await getMenuTree()
    tree.value = res.data
    parentOptions.value = toOptions(res.data)
  } finally {
    loading.value = false
  }
}

function openCreate(parent) {
  editingId.value = null
  Object.assign(form, {
    parentId: parent ? parent.id : null,
    menuType: parent && parent.menuType === 1 ? 2 : 2,
    menuName: '', routePath: '', component: '', icon: '', perms: '', sort: 0
  })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    parentId: Number(row.parentId) === 0 ? null : row.parentId,
    menuType: row.menuType,
    menuName: row.menuName,
    routePath: row.routePath,
    component: row.component,
    icon: row.icon,
    perms: row.perms,
    sort: row.sort
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.menuName) return ElMessage.warning('请填写菜单名称')
  if (form.menuType === 2 && (!form.routePath || !form.component)) {
    return ElMessage.warning('菜单类型需填写路由地址和组件名')
  }
  if (form.menuType === 3 && !form.perms) {
    return ElMessage.warning('按钮类型需填写权限标识')
  }
  saving.value = true
  try {
    const payload = { ...form, parentId: form.parentId || 0 }
    if (editingId.value) {
      await updateMenu(editingId.value, payload)
    } else {
      await createMenu(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.menuName}」吗？`, '提示', { type: 'warning' })
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
