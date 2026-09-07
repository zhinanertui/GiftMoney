<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>礼单管理</span>
        <div class="actions">
          <el-button v-permission="'gift:delete'" type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">
            批量删除{{ selectedIds.length ? ` (${selectedIds.length})` : '' }}
          </el-button>
          <el-button @click="handleDownloadTemplate">下载模板</el-button>
          <el-upload
            :show-file-list="false"
            :before-upload="handleImport"
            accept=".xlsx,.xls,.csv"
          >
            <el-button v-permission="'gift:import'" type="success">导入礼单</el-button>
          </el-upload>
          <el-button v-permission="'gift:add'" type="primary" @click="openCreate">新增记录</el-button>
        </div>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form inline>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="姓名" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="方向">
        <el-select v-model="query.direction" placeholder="全部" clearable style="width: 130px" @change="load">
          <el-option label="送出" :value="1" />
          <el-option label="收到(还礼)" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="归还状态">
        <el-select v-model="query.repayStatus" placeholder="全部" clearable style="width: 130px" @change="load">
          <el-option label="未归还" :value="1" />
          <el-option label="部分归还" :value="2" />
          <el-option label="已还清" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="年份">
        <el-date-picker
          v-model="yearPicker"
          type="year"
          placeholder="全部年份"
          value-format="YYYY"
          style="width: 130px"
          @change="load"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="rows" v-loading="loading" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="方向" width="110">
        <template #default="{ row }">
          <el-tag :type="row.direction === 1 ? 'danger' : 'success'">
            {{ row.direction === 1 ? '送出' : '收到' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="归还状态" width="160">
        <template #default="{ row }">
          <el-tag v-if="repayStatus(row)" :type="repayStatus(row).type" size="small">
            {{ repayStatus(row).text }}
          </el-tag>
          <span v-else class="dash">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="giftDate" label="日期" width="120" />
      <el-table-column prop="occasion" label="事由" width="90" />
      <el-table-column prop="relation" label="关系" width="90" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'gift:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'gift:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑记录' : '新增记录'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="金额" required>
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="方向" required>
          <el-radio-group v-model="form.direction">
            <el-radio :value="1">送出</el-radio>
            <el-radio :value="2">收到(还礼)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="form.giftDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="事由" required>
          <el-select v-model="form.occasion" allow-create filterable placeholder="必填：结婚/满月/生日等" style="width: 100%">
            <el-option v-for="o in occasions" :key="o" :label="o" :value="o" />
          </el-select>
        </el-form-item>
        <el-form-item label="关系">
          <el-input v-model="form.relation" />
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
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { batchDeleteGifts, createGift, deleteGift, downloadTemplate, getContacts, importGifts, pageGifts, updateGift } from '../api/gift'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const yearPicker = ref(null)
const selectedIds = ref([])
const contacts = ref({}) // name -> { sent, received, balance }
const query = reactive({ current: 1, size: 10, keyword: '', direction: null, year: null, repayStatus: null })

const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ name: '', amount: 0, direction: 1, giftDate: '', occasion: '', relation: '', remark: '' })

const occasions = ['结婚', '满月', '乔迁', '生日', '白事', '升学', '其他']

function formatMoney(v) {
  return Number(v || 0).toFixed(2)
}

async function load() {
  loading.value = true
  query.year = yearPicker.value ? Number(yearPicker.value) : null
  try {
    const [pageRes, contactRes] = await Promise.all([pageGifts(query), getContacts()])
    rows.value = pageRes.data.records
    total.value = Number(pageRes.data.total)
    // 构建 姓名+事由 -> 往来 映射
    const map = {}
    contactRes.data.forEach((c) => {
      map[`${c.name}__${c.occasion}`] = { sent: Number(c.sent || 0), received: Number(c.received || 0), balance: Number(c.balance || 0) }
    })
    contacts.value = map
  } finally {
    loading.value = false
  }
}

// 归还状态：仅送出记录显示，按 姓名+事由 匹配
function repayStatus(row) {
  if (row.direction !== 1) return null
  const c = contacts.value[`${row.name}__${row.occasion}`]
  if (!c || c.sent <= 0) return null
  if (c.received >= c.sent) return { type: 'success', text: '已还清' }
  if (c.received > 0) return { type: 'warning', text: `部分归还(还差 ¥${(c.sent - c.received).toFixed(2)})` }
  return { type: 'danger', text: '未归还' }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', amount: 0, direction: 1, giftDate: '', occasion: '', relation: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    amount: Number(row.amount),
    direction: row.direction,
    giftDate: row.giftDate,
    occasion: row.occasion,
    relation: row.relation,
    remark: row.remark
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name) return ElMessage.warning('请填写姓名')
  if (!form.amount) return ElMessage.warning('请填写金额')
  if (!form.occasion) return ElMessage.warning('请选择或填写事由')
  if (!form.giftDate) return ElMessage.warning('请选择日期')
  saving.value = true
  try {
    const payload = { ...form, amount: form.amount }
    if (editingId.value) {
      await updateGift(editingId.value, payload)
    } else {
      await createGift(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.name}」这条记录吗？`, '提示', { type: 'warning' })
  await deleteGift(row.id)
  ElMessage.success('删除成功')
  await load()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map((r) => r.id)
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return ElMessage.warning('请先选择要删除的记录')
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条记录吗？删除后不可恢复。`, '提示', { type: 'warning' })
  await batchDeleteGifts(selectedIds.value)
  ElMessage.success('删除成功')
  selectedIds.value = []
  await load()
}

async function handleImport(file) {
  const res = await importGifts(file)
  ElMessage.success(res.data.message)
  await load()
  return false // 阻止 el-upload 自动上传
}

async function handleDownloadTemplate() {
  const res = await downloadTemplate()
  const blob = new Blob([res.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '礼单导入模板.xlsx'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(load)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.actions { display: flex; gap: 8px; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.dash { color: #c0c4cc; }
</style>
