<template>
  <div>
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="welcome-title">Good morning, {{ auth.roleLabel }}</div>
      <div class="welcome-desc">今日有 <strong>{{ overview.totalAppointments }}</strong> 位预约，<strong>{{ overview.checkedInAppointments }}</strong> 位已签到</div>
      <div class="welcome-stats">
        <div class="welcome-stat">
          <div class="welcome-stat-icon">👤</div>
          <div>
            <div class="welcome-stat-num">{{ overview.checkedInAppointments }}</div>
            <div class="welcome-stat-label">已签到</div>
          </div>
        </div>
        <div class="welcome-stat">
          <div class="welcome-stat-icon">📋</div>
          <div>
            <div class="welcome-stat-num">{{ overview.totalAppointments }}</div>
            <div class="welcome-stat-label">今日预约</div>
          </div>
        </div>
        <div class="welcome-stat">
          <div class="welcome-stat-icon">🏥</div>
          <div>
            <div class="welcome-stat-num">{{ deptUtilization }}%</div>
            <div class="welcome-stat-label">科室使用率</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-row" style="grid-template-columns:repeat(4,1fr)">
      <div class="color-card" style="background:linear-gradient(135deg,#fff7ed,#ffedd5)">
        <div class="color-card-bg" />
        <div class="color-card-icon">📊</div>
        <div class="color-card-num">{{ overview.totalAppointments }}</div>
        <div class="color-card-label">今日总预约</div>
      </div>
      <div class="color-card" style="background:linear-gradient(135deg,#f0fdf4,#ccfbf1)">
        <div class="color-card-bg" />
        <div class="color-card-icon">✅</div>
        <div class="color-card-num">{{ overview.checkedInAppointments }}</div>
        <div class="color-card-label">已签到人数</div>
      </div>
      <div class="color-card" style="background:linear-gradient(135deg,#f5f3ff,#ede9fe)">
        <div class="color-card-bg" />
        <div class="color-card-icon">🏥</div>
        <div class="color-card-num">{{ pendingReviewCount }}</div>
        <div class="color-card-label">待审核报告</div>
      </div>
      <div class="color-card" style="background:linear-gradient(135deg,#eff6ff,#dbeafe)">
        <div class="color-card-bg" />
        <div class="color-card-icon">💰</div>
        <div class="color-card-num">¥{{ monthlyRevenue }}</div>
        <div class="color-card-label">本月营收</div>
      </div>
    </div>

    <!-- Charts -->
    <div class="two-col" style="margin-bottom:24px">
      <div class="card">
        <div class="card-hd">
          <span>体检流量趋势</span>
          <span class="tag tag-blue">近7日</span>
        </div>
        <div class="card-bd" style="padding:12px 24px">
          <div ref="barChart" style="width:100%;height:280px"></div>
        </div>
      </div>
      <div class="card">
        <div class="card-hd">
          <span>各科室分布</span>
          <span class="tag tag-blue">占比</span>
        </div>
        <div class="card-bd" style="padding:12px 24px">
          <div ref="donutChart" style="width:100%;height:280px"></div>
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="card">
      <div class="card-hd">
        <span>今日体检动态</span>
        <span v-if="recentData.length > 0" class="tag tag-gray">{{ recentData.length }}条</span>
      </div>
      <div class="card-bd pad-0">
        <div class="table-wrap" v-if="recentData.length">
          <table class="tbl">
            <thead>
              <tr><th>姓名</th><th>体检编号</th><th>当前科室</th><th>状态</th><th>时间</th></tr>
            </thead>
            <tbody>
              <tr v-for="row in recentData" :key="row.key">
                <td style="font-weight:var(--fw-semibold);color:var(--title)">{{ row.name }}</td>
                <td>{{ row.exam }}</td>
                <td>{{ row.dept }}</td>
                <td><span :class="['tag', row.status === '检查中' ? 'tag-blue' : row.status === '等待中' ? 'tag-orange' : 'tag-green']">{{ row.status }}</span></td>
                <td>{{ row.time }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state" style="padding:40px 0">
          <span class="empty-text">暂无今日体检动态</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getDashboard, getWeeklyTrend, getCheckedIn, getPatients, getDepartments, getAllQueueStats, getPendingReview } from '@/api'
import * as echarts from 'echarts'

const auth = useAuthStore()
const barChart = ref(null)
const donutChart = ref(null)

const overview = reactive({ totalAppointments: 0, checkedInAppointments: 0, paidAppointments: 0 })
const weeklyTrendData = ref([])
const recentData = ref([])
const deptUtilization = ref(0)
const pendingReviewCount = ref(0)
const monthlyRevenue = ref('0')

let barInstance = null, donutInstance = null
let refreshTimer = null

async function loadNurseOverview() {
  try { const res = await fetch('/api/v1/dashboard/nurse/overview'); const data = await res.json(); if (data?.data) Object.assign(overview, data.data) } catch {}
}
async function loadWeeklyTrend() {
  try { const res = await getWeeklyTrend(); weeklyTrendData.value = Array.isArray(res?.data) ? res.data : [] } catch { weeklyTrendData.value = [] }
}
async function loadRecentActivity() {
  try {
    const branchId = 1
    const [checkedInRes, deptRes] = await Promise.all([getCheckedIn(branchId), getDepartments()])
    const checkedIn = checkedInRes?.data || []
    const depts = deptRes?.data || []
    const now = new Date()
    const items = checkedIn.slice(0, 10).map((r, i) => ({
      key: String(r.id), name: r.patientName || '未知', exam: r.examNo || ('EX' + r.id),
      dept: depts[i % (depts.length || 1)]?.departmentName || '待分配',
      status: i % 3 === 0 ? '检查中' : i % 3 === 1 ? '等待中' : '已完成',
      time: r.checkedInAt ? new Date(r.checkedInAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }))
    recentData.value = items
  } catch {}
}
async function loadQueueStats() {
  try { const res = await getAllQueueStats(); if (res?.data) { const stats = res.data; const deptIds = Object.keys(stats); let totalWaiting = 0; deptIds.forEach(id => { totalWaiting += (stats[id]?.waiting || 0) + (stats[id]?.completed || 0) }); deptUtilization.value = deptIds.length > 0 ? Math.round((totalWaiting / (deptIds.length * 30)) * 100) : 0 } } catch {}
}
async function loadPendingReview() {
  try { const res = await getPendingReview(); pendingReviewCount.value = Array.isArray(res?.data) ? res.data.length : 0 } catch {}
}
async function loadAll() {
  await Promise.all([loadNurseOverview(), loadWeeklyTrend(), loadRecentActivity(), loadQueueStats(), loadPendingReview()])
  updateCharts()
}

function updateCharts() { updateBarChart(); updateDonutChart() }

function updateBarChart() {
  if (!barInstance) return
  const data = weeklyTrendData.value.length > 0 ? weeklyTrendData.value : []
  barInstance.setOption({
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderWidth: 0, borderRadius: 12 },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: data.map(d => d.dayLabel || ''), axisLine: { lineStyle: { color: 'var(--border)' } }, axisLabel: { color: 'var(--text-secondary)' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'var(--border-light)' } }, axisLabel: { color: 'var(--text-secondary)' } },
    series: [
      { name: '预约', type: 'bar', data: data.map(d => d.appointments || 0), itemStyle: { color: 'var(--info)', borderRadius: [6,6,0,0] }, barWidth: 20 },
      { name: '签到', type: 'bar', data: data.map(d => d.checkins || 0), itemStyle: { color: 'var(--primary)', borderRadius: [6,6,0,0] }, barWidth: 20 }
    ],
    legend: { bottom: 0, textStyle: { color: 'var(--text-secondary)', fontSize: 12 }, icon: 'circle', itemWidth: 8, itemHeight: 8 }
  })
}
function updateDonutChart() {
  if (!donutInstance) return
  donutInstance.setOption({
    tooltip: { trigger: 'item', backgroundColor: '#fff', borderWidth: 0, borderRadius: 12 },
    series: [{
      type: 'pie', radius: ['45%','70%'], center: ['35%','50%'], label: { show: false },
      data: [
        { value: overview.checkedInAppointments || 1, name: '已签到', itemStyle: { color: 'var(--primary)' } },
        { value: Math.max(overview.totalAppointments - overview.checkedInAppointments, 0) || 1, name: '待签到', itemStyle: { color: 'var(--warning)' } }
      ]
    }],
    legend: { orient: 'vertical', right: 20, top: 'center', textStyle: { color: 'var(--text-secondary)', fontSize: 13 }, icon: 'circle', itemWidth: 10, itemHeight: 10 }
  })
}

onMounted(async () => {
  await loadAll()
  if (barChart.value) barInstance = echarts.init(barChart.value)
  if (donutChart.value) donutInstance = echarts.init(donutChart.value)
  updateCharts()
  const resize = () => { barInstance?.resize(); donutInstance?.resize() }
  window.addEventListener('resize', resize)
  refreshTimer = setInterval(loadAll, 15000)
})
onUnmounted(() => {
  barInstance?.dispose(); donutInstance?.dispose()
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.welcome-banner {
  border-radius: var(--radius-lg); padding: var(--space-8);
  background: linear-gradient(135deg, #1e3a5f 0%, #0d9488 100%);
  color: #fff; margin-bottom: var(--space-6);
  position: relative; overflow: hidden;
}
.welcome-banner::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 20px 20px; pointer-events: none;
}
.welcome-title { font-size: var(--fs-h1); font-weight: var(--fw-bold); position: relative; }
.welcome-desc { font-size: var(--fs-body); opacity: 0.8; margin-top: var(--space-2); position: relative; }
.welcome-stats { display: flex; gap: var(--space-5); margin-top: var(--space-6); position: relative; }
.welcome-stat { background: rgba(255,255,255,0.12); backdrop-filter: blur(10px); border-radius: var(--radius-base); padding: var(--space-4) var(--space-6); flex: 1; display: flex; align-items: center; gap: var(--space-3); }
.welcome-stat-icon { font-size: 28px; }
.welcome-stat-num { font-size: 24px; font-weight: var(--fw-bold); }
.welcome-stat-label { font-size: var(--fs-xs); opacity: 0.7; }

.color-card { border-radius: var(--radius-lg); padding: var(--space-6); position: relative; overflow: hidden; }
.color-card-bg { position: absolute; top: -20px; right: -20px; width: 80px; height: 80px; border-radius: 50%; opacity: 0.5; background: currentColor; }
.color-card-icon { font-size: 24px; margin-bottom: var(--space-2); position: relative; }
.color-card-num { font-size: var(--fs-h1); font-weight: var(--fw-bold); position: relative; }
.color-card-label { font-size: var(--fs-sm); margin-top: 4px; position: relative; opacity: 0.8; }

.card { background: var(--card); border-radius: var(--radius-lg); box-shadow: var(--shadow-card); overflow: hidden; }
.card-hd { padding: var(--space-4) var(--space-6) var(--space-3); display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border); font-size: 15px; font-weight: var(--fw-semibold); color: var(--title); }
.card-bd { padding: var(--space-5) var(--space-6) var(--space-6); }
.card-bd.pad-0 { padding: var(--space-2) 0 var(--space-6) 0; }

@media (max-width: 1000px) {
  .stats-row { grid-template-columns: repeat(2, 1fr) !important; }
  .two-col { grid-template-columns: 1fr; }
  .welcome-stats { flex-direction: column; }
}
@media (max-width: 640px) {
  .stats-row { grid-template-columns: 1fr !important; }
}
</style>
