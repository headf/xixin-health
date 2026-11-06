<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>预约列表</h2>
        <p class="desc">查看所有体检预约，支持筛选和搜索</p>
      </div>
      <div class="page-hd-right">
        <span v-if="polling" class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 自动刷新中
        </span>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <input v-model="filters.search" class="finput" placeholder="搜索患者姓名..." style="width:200px" @input="onFilterChange" />
      <select v-model="filters.status" class="fselect" style="width:150px" @change="onFilterChange">
        <option value="">全部状态</option>
        <option value="CREATED">待支付</option>
        <option value="PAID">已支付</option>
        <option value="CHECKED_IN">已签到</option>
        <option value="COMPLETED">已完成</option>
        <option value="CANCELLED">已取消</option>
      </select>
      <input v-model="filters.date" class="finput" type="date" style="width:160px" @change="onFilterChange" />
      <select v-model.number="pageSize" class="fselect" style="width:90px" @change="page = 0; loadData()">
        <option :value="10">10条/页</option>
        <option :value="15">15条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
      <button class="btn btn-secondary btn-sm" @click="resetFilters">
        <SvgIcon name="refresh" :size="14" /> 重置
      </button>
    </div>

    <div class="card" style="margin-top:16px">
      <div class="card-hd">
        <span>预约记录</span>
        <span class="tag tag-blue">{{ total }} 条</span>
      </div>
      <div class="card-bd pad-0">
        <div class="table-wrap" v-if="list.length">
          <table class="tbl">
            <thead>
              <tr>
                <th>预约编号</th>
                <th>患者</th>
                <th>套餐</th>
                <th>分院</th>
                <th>预约日期</th>
                <th>时段</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in list" :key="a.id">
                <td style="font-weight:500;font-size:12px;color:var(--n-500)">{{ a.appointmentNo }}</td>
                <td style="font-weight:600;color:var(--title)">{{ a.userName }}</td>
                <td style="font-size:13px;color:var(--n-600)">{{ a.packageName }}</td>
                <td><span class="tag tag-blue" style="font-size:11px">{{ a.branchName }}</span></td>
                <td>{{ a.appointmentDate }}</td>
                <td>{{ a.timeSlot }}</td>
                <td><span :class="statusTag(a.status)">{{ statusLabel(a.status) }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state" style="padding:60px 0">
          <span class="empty-text">暂无匹配的预约记录</span>
        </div>
      </div>

      <!-- 分页 -->
      <div class="card-ft" v-if="totalPages > 1">
        <div class="pagination">
          <button class="btn btn-sm" :disabled="page <= 0" @click="goPage(page - 1)">上一页</button>
          <span v-for="p in visiblePages" :key="p" :class="['page-num', { active: p === page }]" @click="goPage(p)">{{ p + 1 }}</span>
          <button class="btn btn-sm" :disabled="page >= totalPages - 1" @click="goPage(page + 1)">下一页</button>
          <span class="page-info">共 {{ totalPages }} 页</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { getAllAppointments } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const list = ref([])
const total = ref(0)
const page = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)
const polling = ref(false)
let pollTimer = null

const filters = reactive({
  search: '',
  status: '',
  date: ''
})

function statusTag(s) {
  const m = { CREATED: 'tag tag-orange', PAID: 'tag tag-warning', CHECKED_IN: 'tag tag-blue', COMPLETED: 'tag tag-green', CANCELLED: 'tag tag-gray' }
  return m[s] || 'tag tag-gray'
}
function statusLabel(s) {
  const m = { CREATED: '待支付', PAID: '已支付', CHECKED_IN: '已签到', COMPLETED: '已完成', CANCELLED: '已取消' }
  return m[s] || s || '-'
}

const visiblePages = computed(() => {
  const pages = []
  const tp = totalPages.value
  const cp = page.value
  let start = Math.max(0, cp - 2)
  let end = Math.min(tp, cp + 3)
  if (end - start < 5) {
    if (start === 0) end = Math.min(tp, 5)
    else start = Math.max(0, tp - 5)
  }
  for (let i = start; i < end; i++) pages.push(i)
  return pages
})

let filterTimer = null
function onFilterChange() {
  if (filterTimer) clearTimeout(filterTimer)
  filterTimer = setTimeout(() => { page.value = 0; loadData() }, 300)
}

function resetFilters() {
  filters.search = ''
  filters.status = ''
  filters.date = ''
  page.value = 0
  loadData()
}

function goPage(p) {
  if (p < 0 || p >= totalPages.value) return
  page.value = p
  loadData()
}

async function loadData() {
  try {
    const params = {
      page: page.value,
      size: pageSize.value || 10,
      branchId: auth.currentBranchId
    }
    if (filters.status) params.status = filters.status
    if (filters.date) params.date = filters.date
    if (filters.search) params.search = filters.search

    const res = await getAllAppointments(params)
    // 后端返回 { content, totalElements, totalPages, number }
    if (res.data) {
      list.value = Array.isArray(res.data.content) ? res.data.content : (Array.isArray(res.data) ? res.data : [])
      total.value = res.data.totalElements ?? list.value.length
      totalPages.value = res.data.totalPages ?? 1
      if (Array.isArray(res.data) && !res.data.content) {
        // 兼容非分页返回
        total.value = list.value.length
        totalPages.value = 1
      }
    } else if (Array.isArray(res)) {
      list.value = res
      total.value = res.length
      totalPages.value = 1
    } else {
      list.value = []
      total.value = 0
      totalPages.value = 0
    }
  } catch {
    list.value = []
    total.value = 0
    totalPages.value = 0
  }
}

function startPolling() {
  polling.value = true
  pollTimer = setInterval(() => { loadData() }, 5000)
}
function stopPolling() {
  polling.value = false
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

let visibilityTimer = null
function onVisibilityChange() {
  if (document.visibilityState === 'visible') {
    if (visibilityTimer) clearTimeout(visibilityTimer)
    visibilityTimer = setTimeout(() => loadData(), 300)
  }
}

onMounted(() => {
  loadData()
  startPolling()
  document.addEventListener('visibilitychange', onVisibilityChange)
})
onUnmounted(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', onVisibilityChange)
  if (visibilityTimer) clearTimeout(visibilityTimer)
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.live-dot {
  display:inline-block; width:6px; height:6px; border-radius:50%;
  background:#22c55e; margin-right:4px;
  animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle;
}
@keyframes livePulse { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:.5;transform:scale(.8)} }
.card-ft {
  padding: 12px 16px;
  border-top: 1px solid var(--border, #e2e8f0);
  display: flex;
  justify-content: center;
}
.pagination {
  display: flex;
  align-items: center;
  gap: 6px;
}
.page-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text, #475569);
  cursor: pointer;
  user-select: none;
  transition: all 0.15s;
}
.page-num:hover { background: var(--bg-secondary, #f1f5f9); }
.page-num.active {
  background: var(--primary, #7c9fff);
  color: #fff;
  font-weight: 600;
}
.page-info {
  font-size: 12px;
  color: var(--text-secondary, #94a3b8);
  margin-left: 8px;
}
</style>
