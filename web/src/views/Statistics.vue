<template>
  <div>
    <!-- 总览卡片 -->
    <el-row :gutter="16" class="cards">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-label">累计送出</div>
          <div class="stat-value sent">¥{{ formatMoney(overview.sent) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-label">累计收到(还礼)</div>
          <div class="stat-value received">¥{{ formatMoney(overview.received) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-label">收送礼差额</div>
          <div class="stat-value" :class="balanceClass">¥{{ formatMoney(overview.balance) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-label">记录总数</div>
          <div class="stat-value">{{ overview.count }} 笔</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 礼金往来汇总 -->
    <el-card class="charts">
      <template #header>
        <div class="card-head">
          <span>礼金往来（谁还了 / 谁没还）</span>
          <div class="table-toolbar">
            <el-input
              v-model="contactKeyword"
              placeholder="搜索姓名/事由"
              clearable
              style="width: 200px"
              @keyup.enter="onContactSearch"
              @clear="onContactSearch"
            />
            <el-button type="primary" @click="onContactSearch">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="contactRows" border stripe size="small">
        <el-table-column prop="name" label="姓名" min-width="110" />
        <el-table-column prop="occasion" label="事由" min-width="90" />
        <el-table-column label="我送出" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.sent) }}</template>
        </el-table-column>
        <el-table-column label="对方还礼" width="130" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.received) }}</template>
        </el-table-column>
        <el-table-column label="差额" width="140" align="right">
          <template #default="{ row }">
            <span :class="row.balance >= 0 ? 'received' : 'sent'">¥{{ formatMoney(row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="归还状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" size="small">{{ statusText(row) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <MiniPager
        class="contact-pager"
        :total="contactTotal"
        :current-page="contactCurrent"
        :page-size="contactSize"
        :page-sizes="[5, 10, 20, 50]"
        @current-change="onContactPage"
        @size-change="onContactSize"
      />
    </el-card>

    <!-- 饼图 -->
    <el-row :gutter="16" class="charts">
      <el-col :xs="24" :sm="12">
        <el-card>
          <template #header>送出 vs 收到 占比</template>
          <div ref="directionChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card>
          <template #header>按事由分布</template>
          <div ref="occasionChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 柱状图 -->
    <el-card class="charts">
      <template #header>每年每月送 / 还礼金额（柱状图）</template>
      <div ref="monthlyChart" class="chart monthly"></div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getDirection, getMonthly, getOccasion, getOverview } from '../api/stats'
import { getContacts } from '../api/gift'
import MiniPager from '../components/MiniPager.vue'

const overview = reactive({ sent: 0, received: 0, balance: 0, count: 0 })
const contactRows = ref([])
const contactTotal = ref(0)
const contactCurrent = ref(1)
const contactSize = ref(10)
const contactKeyword = ref('')
const directionChart = ref(null)
const occasionChart = ref(null)
const monthlyChart = ref(null)

let charts = []

const balanceClass = computed(() => (overview.balance >= 0 ? 'received' : 'sent'))

function formatMoney(v) {
  return Number(v || 0).toFixed(2)
}

function statusType(row) {
  if (row.sent <= 0) return 'info'
  if (row.received >= row.sent) return 'success'
  if (row.received > 0) return 'warning'
  return 'danger'
}

function statusText(row) {
  if (row.sent <= 0) return '仅收到'
  if (row.received >= row.sent) return '已还清'
  if (row.received > 0) return '部分归还'
  return '未归还'
}

function initChart(el) {
  const chart = echarts.init(el)
  charts.push(chart)
  return chart
}

async function loadData() {
  const [ov, dir, occ, monthly] = await Promise.all([
    getOverview(),
    getDirection(),
    getOccasion(),
    getMonthly()
  ])
  Object.assign(overview, ov.data)

  // 方向饼图
  const c1 = echarts.getInstanceByDom(directionChart.value) || initChart(directionChart.value)
  c1.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0 },
    color: ['#f56c6c', '#67c23a'],
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      data: dir.data,
      label: { formatter: '{b}\n¥{c}' }
    }]
  })

  // 事由饼图
  const c2 = echarts.getInstanceByDom(occasionChart.value) || initChart(occasionChart.value)
  c2.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: '65%',
      data: occ.data,
      label: { formatter: '{b}: {d}%' }
    }]
  })

  // 柱状图：每年每月送/还礼金额
  const c3 = echarts.getInstanceByDom(monthlyChart.value) || initChart(monthlyChart.value)
  const labels = monthly.data.map((m) => m.label)
  const sentData = monthly.data.map((m) => Number(m.sent || 0))
  const receivedData = monthly.data.map((m) => Number(m.received || 0))
  c3.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      valueFormatter: (v) => '¥' + Number(v || 0).toFixed(2)
    },
    legend: { data: ['送出', '收到(还礼)'], top: 0 },
    grid: { left: 16, right: 16, top: 40, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { rotate: labels.length > 12 ? 45 : 0 }
    },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      {
        name: '送出',
        type: 'bar',
        barMaxWidth: 28,
        itemStyle: { color: '#f56c6c', borderRadius: [6, 6, 0, 0] },
        data: sentData
      },
      {
        name: '收到(还礼)',
        type: 'bar',
        barMaxWidth: 28,
        itemStyle: { color: '#67c23a', borderRadius: [6, 6, 0, 0] },
        data: receivedData
      }
    ]
  })
}

async function loadContacts() {
  const res = await getContacts({
    current: contactCurrent.value,
    size: contactSize.value,
    keyword: contactKeyword.value || undefined
  })
  const page = res.data || {}
  contactRows.value = (page.records || []).map((c) => ({
    name: c.name,
    occasion: c.occasion,
    sent: Number(c.sent || 0),
    received: Number(c.received || 0),
    balance: Number(c.balance || 0)
  }))
  contactTotal.value = Number(page.total || 0)
}

function onContactPage(page) {
  contactCurrent.value = page
  loadContacts()
}

function onContactSize(size) {
  contactSize.value = size
  contactCurrent.value = 1
  loadContacts()
}

function onContactSearch() {
  contactCurrent.value = 1
  loadContacts()
}

function handleResize() {
  charts.forEach((c) => c.resize())
}

onMounted(() => {
  loadData()
  loadContacts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach((c) => c.dispose())
  charts = []
})
</script>

<style scoped>
.cards { margin-bottom: 16px; }
.stat-label { color: #909399; font-size: 13px; margin-bottom: 8px; }
.stat-value { font-size: 22px; font-weight: 700; }
.stat-value.sent { color: #f56c6c; }
.stat-value.received { color: #67c23a; }
.charts { margin-bottom: 16px; }
.chart { height: 360px; }
.monthly { height: 420px; }
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.table-toolbar { display: flex; align-items: center; gap: 8px; }
.contact-pager { margin-top: 12px; }
</style>
