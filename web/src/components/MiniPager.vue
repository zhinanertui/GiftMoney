<template>
  <div class="mini-pager">
    <span class="mp-total">共 {{ total }} 条</span>

    <div class="mp-nav">
      <button
        type="button"
        class="mp-btn"
        :disabled="currentPage <= 1"
        aria-label="上一页"
        @click="go(currentPage - 1)"
      >‹</button>

      <div ref="pagesRef" class="mp-pages">
        <template v-for="(p, idx) in pages" :key="idx">
          <span v-if="p === '...'" class="mp-ellipsis">…</span>
          <button
            v-else
            type="button"
            class="mp-page"
            :class="{ active: p === currentPage }"
            @click="go(p)"
          >{{ p }}</button>
        </template>
      </div>

      <button
        type="button"
        class="mp-btn"
        :disabled="currentPage >= totalPages"
        aria-label="下一页"
        @click="go(currentPage + 1)"
      >›</button>
    </div>

    <el-select
      :model-value="pageSize"
      class="mp-size"
      size="small"
      @change="onSizeChange"
    >
      <el-option v-for="s in pageSizes" :key="s" :label="`${s} 条/页`" :value="s" />
    </el-select>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  pageSizes: { type: Array, default: () => [5, 10, 20, 50] }
})
const emit = defineEmits(['current-change', 'size-change'])

const pagesRef = ref(null)

const totalPages = computed(() => Math.max(1, Math.ceil((props.total || 0) / props.pageSize)))

// 滑动窗口页码：始终显示首尾，中间围绕当前页 ±1，缺页用省略号
const pages = computed(() => {
  const t = totalPages.value
  const c = props.currentPage
  const win = []
  if (t <= 7) {
    for (let i = 1; i <= t; i++) win.push(i)
    return win
  }
  const set = new Set([1, t])
  for (let i = c - 1; i <= c + 1; i++) {
    if (i >= 1 && i <= t) set.add(i)
  }
  const sorted = [...set].sort((a, b) => a - b)
  const out = []
  let prev = 0
  for (const n of sorted) {
    if (n - prev > 1) out.push('...')
    out.push(n)
    prev = n
  }
  return out
})

function go(p) {
  if (p < 1 || p > totalPages.value || p === props.currentPage) return
  emit('current-change', p)
}

function onSizeChange(s) {
  emit('size-change', s)
}

// 页码切换后把当前页滑动到可视区域中间
watch(
  () => props.currentPage,
  () => {
    nextTick(() => {
      const el = pagesRef.value
      if (!el) return
      const active = el.querySelector('.mp-page.active')
      if (active) {
        active.scrollIntoView({ block: 'nearest', inline: 'center', behavior: 'smooth' })
      }
    })
  }
)
</script>

<style scoped>
.mini-pager {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}
.mp-total {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}
.mp-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}
.mp-pages {
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: 220px;
  overflow-x: auto;
  padding: 2px;
  scrollbar-width: none; /* Firefox */
}
.mp-pages::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}
.mp-btn,
.mp-page {
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.mp-btn:hover:not(:disabled),
.mp-page:hover {
  color: var(--el-color-primary, #409eff);
  border-color: var(--el-color-primary, #409eff);
}
.mp-page.active {
  background: var(--el-color-primary, #409eff);
  border-color: var(--el-color-primary, #409eff);
  color: #fff;
}
.mp-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.mp-ellipsis {
  color: #909399;
  padding: 0 2px;
  user-select: none;
}
.mp-size {
  width: 104px;
}
</style>
