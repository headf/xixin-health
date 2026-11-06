<template>
  <div class="overview-page">
    <!-- ===== 顶部欢迎横幅 ===== -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-title">今日工作台</div>
        <div class="welcome-desc">欢迎回来，以下是今日体检中心运营概况</div>
      </div>
      <el-button text size="small" @click="refreshAll" style="color:rgba(255,255,255,0.7);flex-shrink:0">
        <el-icon style="margin-right:4px"><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- ===== 加载骨架屏 ===== -->
    <template v-if="loading">
      <div class="stats-row">
        <div v-for="n in 4" :key="n" class="skeleton" style="height:96px;border-radius:var(--radius-lg)"></div>
      </div>
      <div class="two-col">
        <div class="skeleton" style="height:380px;border-radius:var(--radius-lg)"></div>
        <div class="skeleton" style="height:380px;border-radius:var(--radius-lg)"></div>
      </div>
    </template>

    <!-- ===== 数据已加载 ===== -->
    <template v-else>
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stat-cards-row">
        <el-col :xs="12" :sm="12" :md="6" v-for="stat in statCards" :key="stat.label">
          <div class="stat-card" :style="{ borderLeft: `4px solid ${stat.color}` }">
            <div class="stat-card-icon" :style="{ background: stat.bg, color: stat.color }">
              <el-icon :size="22"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-value">{{ stat.value }}</div>
              <div class="stat-card-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表 -->
      <el-row :gutter="16" class="charts-row">
        <el-col :xs="24" :md="14">
          <el-card shadow="never">
            <template #header>
              <div class="chart-card-header">
                <div>
                  <span class="chart-card-title">本周趋势</span>
                  <el-tag size="small" class="tag-chart">近 7 日</el-tag>
                </div>
              </div>
            </template>
            <div ref="barChartRef" class="chart-container" style="height:300px"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="10">
          <el-card shadow="never">
            <template #header>
              <div class="chart-card-header">
                <div>
                  <span class="chart-card-title">套餐销售分布</span>
                  <el-tag size="small" class="tag-chart">占比</el-tag>
                </div>
              </div>
            </template>
            <div ref="donutChartRef" class="chart-container" style="height:300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 今日签到记录 -->
      <el-card shadow="never" class="records-card">
        <template #header>
          <div class="chart-card-header">
            <span class="chart-card-title">今日签到记录</span>
            <el-button text type="primary" size="small" @click="refreshAll">
              <el-icon style="margin-right:4px"><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        <el-empty v-if="!tableLoading && checkedInList.length === 0" description="暂无签到记录" />
        <el-table v-else :data="pagedCheckedInList" v-loading="tableLoading" stripe style="width:100%">
          <el-table-column prop="id" label="登记号" width="100" />
          <el-table-column label="患者姓名" width="120">
            <template #default="{ row }">
              <span class="patient-name">{{ row.patientName || row.patient?.name || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="套餐" min-width="160">
            <template #default="{ row }">
              {{ row.packageName || row.package?.name || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small" effect="plain">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="签到时间" width="170">
            <template #default="{ row }">
              {{ formatTime(row.checkedInAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default>
              <el-button text type="primary" size="small">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="checkedInList.length > 10" style="display:flex;justify-content:center;margin-top:12px">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="checkedInList.length"
            :page-sizes="[10,20,50]"
            layout="total, sizes, prev, pager, next"
            small
          />
        </div>
      </el-card>

      <!-- ===== 今日统计概览 ===== -->
      <div class="section-title">今日统计概览</div>
      <el-row :gutter="16" class="stat-cards-row">
        <el-col :xs="12" :sm="8" :md="6">
          <div class="stat-card" style="border-left:4px solid var(--primary)">
            <div class="stat-card-icon" style="background:var(--primary-light);color:var(--primary)">
              <el-icon :size="22"><UserFilled /></el-icon>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-value">{{ todayTotal }}</div>
              <div class="stat-card-label">今日总人数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <div class="stat-card" style="border-left:4px solid var(--secondary)">
            <div class="stat-card-icon" style="background:var(--secondary-light);color:var(--secondary)">
              <el-icon :size="22"><Select /></el-icon>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-value">{{ todayCheckedIn }}</div>
              <div class="stat-card-label">已检</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <div class="stat-card" style="border-left:4px solid var(--warning)">
            <div class="stat-card-icon" style="background:var(--warning-light);color:var(--warning)">
              <el-icon :size="22"><WarningFilled /></el-icon>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-value">{{ todayUnchecked }}</div>
              <div class="stat-card-label">未检</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- ===== 分院科室排队压力 ===== -->
      <div class="section-title">科室排队压力</div>
      
      <!-- 按分院分组 -->
      <div v-for="branch in branchDeptList" :key="branch.id" class="branch-group">
        <div class="branch-group-title">
          <el-icon :size="18"><School /></el-icon>
          {{ branch.name }}
          <span class="branch-group-count">{{ branch.depts.length }}个科室</span>
        </div>
        <el-row :gutter="16" class="dept-queue-row">
          <el-col
            v-for="dept in branch.depts"
            :key="dept.id"
            :xs="12" :sm="8" :md="6" :lg="4"
          >
            <div
              class="pressure-card"
              :class="'pressure-' + dept.level"
            >
              <div class="pressure-card-name">{{ dept.name }}</div>
              <div class="pressure-card-count">{{ dept.waiting }}人等待</div>
              <div class="pressure-card-bar">
                <div class="pressure-card-fill" :style="{ width: dept.barPercent + '%' }"></div>
              </div>
              <div class="pressure-card-tag">
                <el-tag
                  :type="dept.level === 'high' ? 'danger' : dept.level === 'mid' ? 'warning' : 'success'"
                  size="small"
                  effect="dark"
                >
                  {{ dept.levelLabel }}
                </el-tag>
              </div>
            </div>
          </el-col>
          <el-col v-if="branch.depts.length === 0" :span="24">
            <el-empty description="暂无科室数据" :image-size="60" />
          </el-col>
        </el-row>
      </div>
      <div v-if="branchDeptList.length === 0">
        <el-empty description="暂无排队数据" />
      </div>

    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getBusinessStats, getHealthStats, getWeeklyTrend, getCheckedInRecords, getAllQueueStats, getDepartments, getBranches } from '@/api'
import { useAuthStore } from '@/stores/auth'

// ==================== State ====================
const auth = useAuthStore()
const loading = ref(true)
const biz = reactive({ totalAppointments: 0, checkedInAppointments: 0, paidOrders: 0, totalRevenueCent: 0, packageSales: [], branchPerformance: [] })
const health = reactive({ totalReports: 0, abnormalReports: 0, criticalReports: 0 })
const weeklyTrend = ref([])
const checkedInList = ref([])
const tableLoading = ref(false)

const barChartRef = ref(null)
const donutChartRef = ref(null)
let barChart = null, donutChart = null

// ==================== Today Stats & Queue Pressure ====================
const deptList = ref([])
const branchList = ref([])
const deptQueueStats = reactive({})

const todayTotal = computed(() => biz.totalAppointments || 0)
const todayCheckedIn = computed(() => biz.checkedInAppointments || 0)
const todayUnchecked = computed(() => Math.max(0, todayTotal.value - todayCheckedIn.value))

const deptPressureList = computed(() => {
  return deptList.value.map(d => {
    const stats = deptQueueStats[d.id] || { waiting: 0 }
    const waiting = stats.waiting || 0
    let level, levelLabel, barPercent
    if (waiting >= 15) {
      level = 'high'; levelLabel = '高拥堵'; barPercent = Math.min(100, waiting * 5)
    } else if (waiting >= 6) {
      level = 'mid'; levelLabel = '中等'; barPercent = 30 + (waiting - 6) * 5
    } else {
      level = 'low'; levelLabel = '畅通'; barPercent = waiting * 10
    }
    return { id: d.id, name: d.departmentName || d.name || '-', branchId: d.branchId, waiting, level, levelLabel, barPercent }
  })
})

const branchDeptList = computed(() => {
  const branchMap = {}
  branchList.value.forEach(b => { branchMap[b.id] = b.branchName || b.name || '未知分院' })
  const groups = {}
  deptPressureList.value.forEach(d => {
    const bName = branchMap[d.branchId] || '其他分院'
    if (!groups[bName]) groups[bName] = { id: d.branchId || 0, name: bName, depts: [] }
    groups[bName].depts.push(d)
  })
  return Object.values(groups).map(g => {
    // Get actual branch info
    const b = branchList.value.find(x => x.id === g.id)
    if (b) { g.name = b.branchName || b.name || g.name }
    return g
  })
})

const statCards = computed(() => [
  { icon: 'UserFilled', label: '总预约',  value: biz.totalAppointments,  color: 'var(--primary)',  bg: 'var(--primary-light)' },
  { icon: 'Select',     label: '已签到',  value: biz.checkedInAppointments, color: 'var(--secondary)', bg: 'var(--secondary-light)' },
  { icon: 'WarningFilled', label: '异常报告', value: health.abnormalReports, color: 'var(--warning)', bg: 'var(--warning-light)' },
  { icon: 'Money',      label: '营收(元)', value: formatRevenue(biz.totalRevenueCent), color: 'var(--info)', bg: 'var(--info-light)' },
])

// ==================== Helpers ====================

const pageNum = ref(1)
const pageSize = ref(10)
const pagedCheckedInList = computed(() => checkedInList.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
function formatRevenue(cents) {
  if (cents == null) return '¥0'
  return '¥' + (cents / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function formatTime(t) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
function statusType(status) {
  return { checked_in: 'success', in_progress: 'warning', completed: '', cancelled: 'info' }[status] || 'info'
}
function statusLabel(status) {
  return { checked_in: '已签到', in_progress: '进行中', completed: '已完成', cancelled: '已取消' }[status] || status || '-'
}

// ==================== Data Loading ====================
async function refreshAll() {
  await Promise.all([loadBizStats(), loadHealthStats(), loadWeeklyTrend(), loadCheckedInRecords(), loadDeptQueueStats()])
  updateCharts()
}

async function loadBizStats() {
  try { const res = await getBusinessStats('daily', auth.currentBranchId); if (res?.data) Object.assign(biz, res.data) } catch (e) { console.error(e); ElMessage.error('加载业务统计数据失败') }
}
async function loadHealthStats() {
  try { const res = await getHealthStats(auth.currentBranchId); if (res?.data) Object.assign(health, res.data) } catch (e) { console.error(e); ElMessage.error('加载健康统计数据失败') }
}
async function loadWeeklyTrend() {
  try { const res = await getWeeklyTrend(auth.currentBranchId); weeklyTrend.value = Array.isArray(res?.data) ? res.data : [] } catch (e) { console.error(e); ElMessage.error('加载周趋势数据失败'); weeklyTrend.value = [] }
}
async function loadCheckedInRecords() {
  tableLoading.value = true
  try {
    const results = []
    const branchIds = auth.currentBranchId ? [auth.currentBranchId] : [1, 2, 3, 4, 5]
    for (const b of branchIds) {
      try {
        const res = await getCheckedInRecords(b)
        if (Array.isArray(res?.data)) results.push(...res.data)
      } catch { /* skip unreachable branch */ }
    }
    checkedInList.value = results
  } catch (e) { console.error(e); ElMessage.error('加载签到记录失败'); checkedInList.value = [] }
  finally { tableLoading.value = false }
}

async function loadDeptQueueStats() {
  try {
    const [deptRes, queueRes, branchRes] = await Promise.all([getDepartments(), getAllQueueStats(), getBranches()])
    if (Array.isArray(deptRes?.data)) {
      let depts = deptRes.data
      if (auth.currentBranchId) depts = depts.filter(d => d.branchId === auth.currentBranchId)
      deptList.value = depts
    }
    if (Array.isArray(branchRes?.data)) {
      let branches = branchRes.data
      if (auth.currentBranchId) branches = branches.filter(b => b.id === auth.currentBranchId)
      branchList.value = branches
    }
    if (queueRes?.data) Object.assign(deptQueueStats, queueRes.data)
  } catch (e) {
    console.error('加载科室排队数据失败', e)
  }
}

// ==================== Charts ====================
function initCharts() {
  if (barChartRef.value) barChart = echarts.init(barChartRef.value)
  if (donutChartRef.value) donutChart = echarts.init(donutChartRef.value)
  updateCharts()
}

function updateCharts() { updateBarChart(); updateDonutChart() }

function updateBarChart() {
  if (!barChart) return
  const data = weeklyTrend.value.length > 0 ? weeklyTrend.value : []
  const empty = data.length === 0
  barChart.setOption({
    title: { text: empty ? '暂无趋势数据' : '', left: 'center', top: 'center', textStyle: { color: '#bbb', fontSize: 14, fontWeight: 'normal' } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['预约数', '签到数'], bottom: 0 },
    grid: { top: 20, right: 20, bottom: 40, left: 40 },
    xAxis: { type: 'category', data: data.map(d => d.dayLabel || '') },
    yAxis: { type: 'value' },
    series: [
      { name: '预约数', type: 'bar', barWidth: '28%', barGap: '20%', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#6366f1' }, { offset: 1, color: '#a5b4fc' }]), borderRadius: [4, 4, 0, 0] }, data: data.map(d => d.appointments || 0) },
      { name: '签到数', type: 'bar', barWidth: '28%', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#00B894' }, { offset: 1, color: '#69DBC4' }]), borderRadius: [4, 4, 0, 0] }, data: data.map(d => d.checkins || 0) },
    ]
  }, { notMerge: true })
}

function updateDonutChart() {
  if (!donutChart) return
  const sales = biz.packageSales && biz.packageSales.length > 0 ? biz.packageSales : []
  const empty = sales.length === 0
  const colors = ['#00B894', '#0d9488', '#f97316', '#6366f1', '#ec4899', '#eab308', '#F53F3F', '#14b8a6']
  const pieData = empty
    ? []
    : sales.map((p, i) => ({ value: p.value || 0, name: p.name || p.packageName || `套餐${i+1}`, itemStyle: { color: colors[i % colors.length] } }))

  donutChart.setOption({
    title: { text: empty ? '暂无销售数据' : '', left: 'center', top: 'center', textStyle: { color: '#bbb', fontSize: 14, fontWeight: 'normal' } },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['45%', '70%'], avoidLabelOverlap: true, padAngle: 2,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: !empty && pieData.length <= 6, formatter: ({ name, percent }) => `${name}\n${percent}%` },
      emphasis: { scaleSize: 6 },
      data: pieData
    }]
  }, { notMerge: true })
}

// ==================== Lifecycle ====================
let timer = null
function handleResize() { barChart?.resize(); donutChart?.resize() }

onMounted(async () => {
  await refreshAll()
  loading.value = false
  await nextTick()
  initCharts()
  timer = setInterval(refreshAll, 10000)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  donutChart?.dispose()
})
</script>

<style scoped>
.overview-page { max-width: var(--content-max-w); }

/* Welcome Banner */
.welcome-banner {
  border-radius: var(--radius-lg);
  padding: var(--space-8) var(--space-8);
  background: linear-gradient(135deg, #1e3a5f 0%, #0d9488 100%);
  color: #fff;
  margin-bottom: var(--space-6);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  position: relative;
  overflow: hidden;
}
.welcome-banner::before {
  content: '';
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
}
.welcome-content { position: relative; }
.welcome-title { font-size: var(--fs-h1); font-weight: var(--fw-bold); }
.welcome-desc { font-size: var(--fs-body); opacity: 0.8; margin-top: var(--space-2); }

/* Stat Cards */
.stat-cards-row { margin-bottom: var(--space-5) !important; }
.stat-card {
  background: var(--card); border-radius: var(--radius-lg);
  padding: var(--space-5); box-shadow: var(--shadow-card);
  display: flex; align-items: center; gap: var(--space-4);
  transition: box-shadow var(--transition-base), transform var(--transition-fast);
  margin-bottom: var(--space-4);
}
.stat-card:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-1px); }
.stat-card-icon {
  width: 48px; height: 48px; border-radius: var(--radius-base);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-card-body { flex: 1; min-width: 0; }
.stat-card-value { font-size: 26px; font-weight: var(--fw-bold); color: var(--title); line-height: 1.2; }
.stat-card-label { font-size: var(--fs-sm); color: var(--text-secondary); margin-top: 4px; }

/* Charts */
.charts-row { margin-bottom: var(--space-5) !important; }
.chart-card-header { display: flex; align-items: center; gap: var(--space-2); }
.chart-card-title { font-size: 15px; font-weight: var(--fw-semibold); color: var(--title); }
.tag-chart { margin-left: var(--space-3); }
.chart-container { width: 100%; }

/* Records */
.records-card { margin-bottom: var(--space-6); }
.patient-name { font-weight: var(--fw-semibold); color: var(--title); }

/* Section Title */
.section-title {
  font-size: 16px;
  font-weight: var(--fw-semibold);
  color: var(--title);
  margin-bottom: var(--space-4);
  margin-top: var(--space-6);
  padding-left: 4px;
  border-left: 3px solid var(--primary);
  padding-left: 12px;
}

/* Queue Pressure Cards */
.branch-group { margin-bottom: 24px; }
.branch-group-title { font-size: 15px; font-weight: 600; color: var(--title); margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.branch-group-count { font-size: 12px; font-weight: 400; color: var(--text-secondary); }
.dept-queue-row { margin-bottom: var(--space-5) !important; }
.pressure-card {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-card);
  margin-bottom: var(--space-4);
  transition: box-shadow var(--transition-base), transform var(--transition-fast);
  text-align: center;
  border-top: 3px solid transparent;
}
.pressure-card:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-1px); }
.pressure-card.pressure-high { border-top-color: var(--el-color-danger); }
.pressure-card.pressure-mid  { border-top-color: var(--el-color-warning); }
.pressure-card.pressure-low  { border-top-color: var(--el-color-success); }
.pressure-card-name {
  font-size: 14px;
  font-weight: var(--fw-semibold);
  color: var(--title);
  margin-bottom: var(--space-2);
}
.pressure-card-count {
  font-size: 20px;
  font-weight: var(--fw-bold);
  color: var(--primary);
  margin-bottom: var(--space-3);
}
.pressure-card-bar {
  height: 6px;
  background: var(--bg-muted);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: var(--space-3);
}
.pressure-card-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}
.pressure-high .pressure-card-fill { background: var(--el-color-danger); }
.pressure-mid .pressure-card-fill  { background: var(--el-color-warning); }
.pressure-low .pressure-card-fill  { background: var(--el-color-success); }

/* Responsive */
@media (max-width: 1000px) {
  .welcome-banner { padding: var(--space-6) var(--space-5); }
  .welcome-title { font-size: var(--fs-h2); }
}
</style>
