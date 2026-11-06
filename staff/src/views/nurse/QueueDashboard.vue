<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>科室排队导诊看板</h2>
        <p class="desc">实时查看各科室排队情况，引导患者合理安排检查顺序</p>
      </div>
      <div class="page-hd-right">
        <span v-if="autoRefresh" class="tag tag-green"><SvgIcon name="circle" :size="8" style="margin-right:4px" /> 实时刷新中</span>
        <span class="tag tag-blue">{{ new Date().toLocaleTimeString('zh-CN') }}</span>
      </div>
    </div>

    <div v-for="group in groupedDepts" :key="'branch-'+group.branchId" style="margin-bottom:24px">
      <div class="branch-header">
        <span class="branch-icon">🏥</span>
        <span class="branch-name">{{ group.branchName }}</span>
        <span class="branch-count">{{ group.departments.length }}个科室</span>
      </div>
      <div class="dashboard-grid">
        <div v-for="d in group.departments" :key="d.id"
          :class="['dash-card', queueLevel(d.id)]"
          @click="clickDept(d.id)">
          <div class="dash-card-bg"></div>
          <div class="dash-card-body">
            <div class="dash-dept-name">{{ d.departmentName || d.name }}</div>
            <div class="dash-count">{{ getWaiting(d.id) }}</div>
            <div class="dash-label">等待人数</div>
            <div class="dash-meta">
              <span v-if="d.fastingRequired" class="tag tag-warning">需空腹</span>
              <span class="tag tag-gray" v-if="getCompleted(d.id) > 0">{{ getCompleted(d.id) }}已检</span>
            </div>
            <div v-if="d.fastingNote && highlightDept === d.id" class="dash-tip">{{ d.fastingNote }}</div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="highlightDept && queueDetails[highlightDept]" class="card" style="margin-top:16px">
        <div class="card-hd">
          <span><SvgIcon name="list" :size="16" style="margin-right:6px;color:var(--blue)" /> {{ getDeptName(highlightDept) }} — 排队详情</span>
          <span class="tag tag-orange">{{ (queueDetails[highlightDept] || []).filter(q => q.queueStatus === 'WAITING').length }}人等待</span>
        </div>
        <div class="card-bd pad-0">
          <template v-if="queueDetails[highlightDept] && queueDetails[highlightDept].length">
            <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th class="col-nowrap">排队号</th><th class="col-nowrap">患者</th><th class="col-nowrap">状态</th><th class="col-nowrap">操作</th></tr></thead>
              <tbody>
                <tr v-for="row in queueDetails[highlightDept]" :key="row.id">
                  <td style="font-weight:700;font-size:18px;color:var(--n-800)">{{ row.queueNo }}</td>
                  <td>{{ getPatientNameFromQueue(row) }}</td>
                  <td><span :class="['tag', row.queueStatus === 'WAITING' ? 'tag-orange' : row.queueStatus === 'CALLED' ? 'tag-blue' : row.queueStatus === 'COMPLETED' ? 'tag-green' : 'tag-gray']">{{ queueStatusLabel(row.queueStatus) }}</span></td>
                  <td v-if="row.queueStatus === 'SKIPPED' || row.queueStatus === 'COMPLETED'">
                    <button class="btn btn-sm btn-outline" @click="handleReQueue(row)">重排</button>
                  </td>
                  <td v-else>—</td>
                </tr>
              </tbody>
            </table>
            </div>
          </template>
          <div v-else class="empty-state"><SvgIcon name="check" :size="36" style="color:var(--n-300)" /><span class="empty-text">暂无排队</span></div>
        </div>
      </div>
    </transition>

    <div class="card" style="margin-top:16px">
      <div class="card-hd"><span><SvgIcon name="map" :size="16" style="margin-right:6px;color:var(--blue)" /> 智能推荐路线</span></div>
      <div class="card-bd">
        <div v-if="recommendedRoute.length" class="route-steps">
          <div v-for="(step, idx) in recommendedRoute" :key="idx" class="route-step">
            <span class="route-num">{{ idx + 1 }}</span>
            <div class="route-info">
              <strong>{{ step.name }}</strong>
              <span class="route-detail">{{ step.location }} · {{ step.fasting ? '需空腹' : '无需空腹' }}</span>
              <span class="route-wait">等待 {{ step.waiting }} 人</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state" style="padding:16px 0">暂无推荐路线</div>
      </div>
    </div>

    <div class="card" style="margin-top:16px">
      <div class="card-hd"><span><SvgIcon name="info" :size="16" style="margin-right:6px;color:var(--blue)" /> 导诊指引</span></div>
      <div class="card-bd">
        <div class="guide-steps">
          <div class="guide-step"><span class="step-num">1</span> 患者签到后，先到<strong>基础测量区</strong>测身高体重血压</div>
          <div class="guide-step"><span class="step-num">2</span> 去<strong>采血区</strong>完成静脉采血和尿液留取</div>
          <div class="guide-step"><span class="step-num">3</span> 根据看板选择<strong>等待人数最少</strong>的科室先去报到</div>
          <div class="guide-step"><span class="step-num">4</span> ⚠️ 需空腹项目（检验科、腹部彩超）建议优先完成</div>
          <div class="guide-step"><span class="step-num">5</span> 所有项目完成后到前台确认收尾</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllQueueStats, getDepartments, getQueueWithNames, getPatients, getCheckedIn, getBranches } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'

const clinic = useClinicStore()
const auth = useAuthStore()

// 当前用户所属分院ID：JWT中的branchId优先，手机号模式为后备
const userBranchId = computed(() => {
  if (auth.user?.branchId) return Number(auth.user.branchId)
  const phone = auth.user?.phone || ''
  const m1 = phone.match(/^13800001([1-4])\d{2}$/)
  if (m1) return Number(m1[1])
  const m2 = phone.match(/^1380000([1-4])\d{3}$/)
  if (m2) return Number(m2[1])
  if (phone === '13800000100' || phone === '13800000200' || phone === '13800000300') return 1
  return null
})
const departments = ref([])
const branches = ref([])
const queueStats = reactive({})
const queueDetails = reactive({})
const highlightDept = ref(null)
const autoRefresh = ref(false)
let pollTimer = null

const patientNames = {}

const branchMap = computed(() => {
  const map = {}
  branches.value.forEach(b => { map[b.id] = b.branchName || '未知分院' })
  return map
})

const groupedDepts = computed(() => {
  const groups = []
  let branchIds = [...new Set(departments.value.filter(d => d.id !== 1).map(d => d.branchId))]
  // 如果当前用户有分院归属，只显示该分院
  if (userBranchId.value) {
    branchIds = branchIds.filter(id => id === userBranchId.value)
  }
  branchIds.sort((a, b) => a - b)
  for (const bid of branchIds) {
    const depts = departments.value
      .filter(d => d.branchId === bid && d.id !== 1)
      .sort((a, b) => {
        if (a.fastingRequired && !b.fastingRequired) return -1
        if (!a.fastingRequired && b.fastingRequired) return 1
        return (a.departmentName || a.name || '').localeCompare(b.departmentName || b.name || '')
      })
    if (depts.length > 0) {
      groups.push({ branchId: bid, branchName: branchMap.value[bid] || ('分院' + bid), departments: depts })
    }
  }
  return groups
})

const recommendedRoute = computed(() => {
  const depts = departments.value.filter(d => d.fastingRequired || getWaiting(d.id) > 0)
  const queueSorted = [...depts].sort((a, b) => {
    if (a.fastingRequired && !b.fastingRequired) return -1
    if (!a.fastingRequired && b.fastingRequired) return 1
    return (getWaiting(a.id) - getWaiting(b.id))
  })
  return queueSorted.map(d => ({
    name: d.departmentName || d.name,
    location: d.location || '',
    fasting: d.fastingRequired,
    waiting: getWaiting(d.id)
  })).slice(0, 8)
})

function getWaiting(id) { return queueStats[id]?.waiting || 0 }
function getCompleted(id) { return queueStats[id]?.completed || 0 }
function getDeptName(id) { const d = departments.value.find(d => d.id === id); return d ? (d.departmentName || d.name) : '未知' }
function getPatientNameFromQueue(row) {
  const er = clinic.examRecords[row.examRecordId]
  if (er?.patientName) return er.patientName
  if (er?.userId && patientNames[er.userId]) return patientNames[er.userId]
  return '#' + row.examRecordId
}
function queueStatusLabel(s) { return { WAITING:'等待中', CALLED:'已叫号', ARRIVED:'检查中', COMPLETED:'已完成', SKIPPED:'已跳过' }[s] || s }

function queueLevel(id) {
  const w = getWaiting(id)
  if (w === 0) return 'level-green'
  if (w <= 3) return 'level-yellow'
  if (w <= 8) return 'level-orange'
  return 'level-red'
}

async function clickDept(id) {
  if (highlightDept.value === id) {
    highlightDept.value = null
  } else {
    highlightDept.value = id
    await loadQueueDetail(id)
  }
}

async function loadDepartments() {
  try { const res = await getDepartments(); departments.value = res?.data || [] } catch {}
}

async function loadStats() {
  try { const res = await getAllQueueStats(); Object.assign(queueStats, res?.data || {}) } catch {}
}

async function loadQueueDetail(deptId) { try { const res = await getQueueWithNames(deptId); queueDetails[deptId] = res?.data || []; for (const e of (res?.data || [])) { clinic.addExamRecord({ id: e.examRecordId, patientName: e.patientName, userId: e.userId }) } } catch {} }

async function loadAll() {
  await Promise.all([loadStats(), ...(highlightDept.value ? [loadQueueDetail(highlightDept.value)] : [])])
}

function startPolling() {
  autoRefresh.value = true
  pollTimer = setInterval(() => loadAll(), 5000)
}
function stopPolling() { autoRefresh.value = false; if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }

async function loadPatients() {
  try {
    const branchId = userBranchId.value || 1
    const [checkedIn, p] = await Promise.all([getCheckedIn(branchId), getPatients()])
    if (p?.data) p.data.forEach(pp => patientNames[pp.id] = pp.name)
    ;(checkedIn?.data || []).forEach(r => {
      clinic.addExamRecord({ ...r, patientName: patientNames[r.userId] || '#' + r.userId })
    })
  } catch {}
}

async function loadBranches() {
  try { const res = await getBranches(); branches.value = res?.data || [] } catch {}
}

async function handleReQueue(row) {
  try {
    await fetch('/api/v1/exams/queue/' + row.id + '/re-queue', { method: 'PUT', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('xixin_staff_token'), 'Content-Type': 'application/json' } })
    ElMessage.success('已重新加入排队')
    await loadQueueDetail(highlightDept.value)
    await loadStats()
  } catch { ElMessage.error('重排失败') }
}

onMounted(() => { loadBranches(); loadDepartments(); loadAll(); loadPatients(); startPolling() })
onUnmounted(() => stopPolling())
</script>

<style scoped>
.dashboard-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 14px; }
.dash-card {
  position: relative; border-radius: 16px; padding: 20px 16px;
  cursor: pointer; overflow: hidden; transition: all 0.2s;
  border: 1px solid transparent;
}
.dash-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.dash-card.level-green { background: #ecfdf5; border-color: #a7f3d0; }
.dash-card.level-yellow { background: #fffbeb; border-color: #fde68a; }
.dash-card.level-orange { background: #fff7ed; border-color: #fed7aa; }
.dash-card.level-red { background: #fef2f2; border-color: #fecaca; }

.dash-card-bg { position: absolute; top: 0; right: 0; width: 80px; height: 80px; border-radius: 50%; opacity: 0.06; transform: translate(20px, -20px); }
.level-green .dash-card-bg { background: #10b981; }
.level-yellow .dash-card-bg { background: #f59e0b; }
.level-orange .dash-card-bg { background: #f97316; }
.level-red .dash-card-bg { background: #ef4444; }

.dash-card-body { position: relative; }
.dash-dept-name { font-size: 14px; font-weight: 600; color: var(--n-700); margin-bottom: 8px; }
.dash-count { font-size: 36px; font-weight: 800; line-height: 1; margin-bottom: 2px; }
.level-green .dash-count { color: #059669; }
.level-yellow .dash-count { color: #d97706; }
.level-orange .dash-count { color: #ea580c; }
.level-red .dash-count { color: #dc2626; }
.dash-label { font-size: 12px; color: var(--n-500); margin-bottom: 8px; }
.dash-meta { display: flex; gap: 4px; flex-wrap: wrap; }
.dash-tip { margin-top: 8px; padding: 6px 10px; background: rgba(0,0,0,0.04); border-radius: 6px; font-size: 11px; color: var(--n-500); }
.tag-warning { background: #fef3c7; color: #b45309; }

.guide-steps { display: flex; flex-direction: column; gap: 8px; }
.guide-step { display: flex; align-items: center; gap: 8px; font-size: 14px; color: var(--n-600); padding: 4px 0; }
.step-num { width: 24px; height: 24px; border-radius: 50%; background: var(--blue-light); color: var(--blue); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }

.branch-header { display: flex; align-items: center; gap: 6px; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid var(--border); }
.branch-icon { font-size: 18px; }
.branch-name { font-size: 15px; font-weight: 700; color: var(--title); }
.branch-count { font-size: 11px; color: var(--text-secondary); background: var(--bg-secondary); padding: 1px 10px; border-radius: 9999px; }

.route-steps { display: flex; flex-direction: column; gap: 8px; }
.route-step { display: flex; align-items: flex-start; gap: 10px; padding: 8px 12px; background: #f8fafc; border-radius: 10px; }
.route-num { width: 22px; height: 22px; border-radius: 50%; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; margin-top: 2px; }
.route-info { display: flex; flex-direction: column; gap: 2px; }
.route-info strong { font-size: 13px; color: var(--title); }
.route-detail { font-size: 11px; color: var(--text-secondary); }
.route-wait { font-size: 11px; color: var(--primary); font-weight: 600; }
</style>
