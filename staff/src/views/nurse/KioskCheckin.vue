<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>科室报到（自助机模拟）</h2>
        <p class="desc">患者持导诊单到各科室门口自助机扫码报到，排入该科室队列</p>
      </div>
    </div>

    <div class="two-col">
      <div class="card" style="background:var(--blue-light);border:2px dashed var(--blue)">
        <div class="card-hd">
          <span><SvgIcon name="phone" :size="18" style="margin-right:6px;color:var(--blue)" /> 📟 自助报到终端</span>
          <span class="tag tag-blue">患者自助</span>
        </div>
        <div class="card-bd">
          <!-- Step 1: Select patient -->
          <div class="kiosk-step">
            <div class="step-badge">1</div>
            <div class="step-content">
              <label>扫描导诊单条形码（选择体检记录）</label>
              <select v-model="selectedExamId" class="fselect kiosk-select" @change="onPatientSelect">
                <option value="" disabled>─ 请选择已签到患者 ─</option>
                <option v-for="r in checkedInList" :key="r.id" :value="r.id">
                  {{ getPatientName(r) }} · {{ r.examNo }}
                  <template v-if="r.packageName"> · [{{ r.packageName }}]</template>
                </option>
              </select>
              <div v-if="selectedExamId && patientDepts.length" style="margin-top:8px;font-size:12px;color:var(--n-500)">
                该患者套餐包含 <strong>{{ patientDepts.length }}</strong> 个科室：
                <span v-for="(d, i) in patientDepts" :key="d.id" style="margin-right:4px">
                  {{ d.name }}<span v-if="deptDone(d.id)" style="color:var(--green-dark)">✓</span>{{ i < patientDepts.length - 1 ? ',' : '' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Step 2: Choose department (only from patient's package) -->
          <div class="kiosk-step">
            <div class="step-badge">2</div>
            <div class="step-content">
              <label>选择要报到的科室</label>
              <template v-if="!selectedExamId">
                <div class="empty-state" style="padding:16px 0"><span class="empty-text">请先选择患者</span></div>
              </template>
              <template v-else-if="patientDepts.length === 0">
                <div class="empty-state" style="padding:16px 0"><span class="empty-text">该患者套餐中无科室项目</span></div>
              </template>
              <div v-else class="dept-grid">
                <button v-for="d in patientDepts" :key="d.id"
                  :class="['dept-btn', { active: selectedDeptId === d.id }, 'status-' + d.status]"
                  @click="selectedDeptId = d.id"
                  :disabled="d.status === 'done' || d.status === 'examining'">
                  <SvgIcon :name="d.status === 'done' ? 'check' : 'list'" :size="16" />
                  {{ d.name }}
                  <span v-if="d.status === 'done'" class="status-done">✓ 已完成</span>
                  <span v-else-if="d.status === 'examining'" class="status-calling">检查中</span>
                  <span v-else-if="d.status === 'queued'" class="status-queued">已报到</span>
                  <span v-else class="status-pending">待报到</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Step 3: Confirm -->
          <div class="kiosk-step">
            <div class="step-badge">3</div>
            <div class="step-content">
              <label>确认报到</label>
              <div style="display:flex;align-items:center;gap:12px;padding-top:4px">
                <button class="btn btn-primary btn-lg" :disabled="!selectedExamId || !selectedDeptId || scanning"
                  @click="handleScan">
                  <SvgIcon name="phone" :size="20" /> 扫码报到
                </button>
                <span v-if="selectedExamId && selectedDeptId" style="font-size:13px;color:var(--n-500)">
                  {{ getPatientName(selectedExamId) }} → {{ deptName(selectedDeptId) }}
                </span>
              </div>
            </div>
          </div>

          <transition name="fade">
            <div v-if="msg" :class="['alert', msg.type]" style="margin-top:12px">
              <SvgIcon :name="msg.icon" :size="16" style="margin-right:6px" /> {{ msg.text }}
            </div>
          </transition>
        </div>
      </div>

      <!-- Right panel -->
      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="list" :size="16" style="margin-right:6px;color:var(--blue)" /> 各科室报到人数</span>
        </div>
        <div class="card-bd">
          <div style="display:grid;gap:8px">
            <div v-for="d in departments" :key="d.id" class="dept-stat">
              <div class="dept-stat-name">{{ d.name }}</div>
              <div class="dept-stat-nums">
                <span class="tag tag-orange">{{ queueStats[d.id]?.waiting || 0 }}等待</span>
                <span class="tag tag-gray">{{ queueStats[d.id]?.completed || 0 }}完成</span>
              </div>
            </div>
          </div>
          <div class="divider"></div>
          <div class="section-label">已报到记录（当前患者）</div>
          <div v-if="recentScans.length" style="display:grid;gap:4px;margin-top:8px">
            <div v-for="(s, i) in recentScans" :key="i" class="scan-row">
              <SvgIcon name="check" :size="14" style="color:var(--green-dark)" />
              <span>{{ s.patient }}</span>
              <span style="color:var(--n-400);font-size:12px">{{ s.dept }}</span>
              <span style="color:var(--n-400);font-size:11px">{{ s.time }}</span>
            </div>
          </div>
          <div v-else class="empty-state" style="padding:16px 0">
            <SvgIcon name="phone" :size="28" style="color:var(--n-300)" />
            <span class="empty-text" style="font-size:13px">暂无报到记录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { addToQueue, getCheckedIn, getAllQueueStats, getExamProgress, getQueue, getTodayQueueRegistrations } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const clinic = useClinicStore()
const auth = useAuthStore()

const checkedInList = ref([])
const selectedExamId = ref('')
const selectedDeptId = ref(null)
const scanning = ref(false)
const msg = ref(null)
const recentScans = ref([])
const queueStats = reactive({})
const patientProgress = ref([])

// Live queue entries for the selected patient (from backend)
const patientQueueEntries = ref([])
let pollTimer = null

const departments = [
  { id: 1, name: '检验科' }, { id: 2, name: '影像科' },
  { id: 3, name: '内科' }, { id: 4, name: '外科' },
  { id: 5, name: '眼科' }, { id: 6, name: '耳鼻喉科' },
  { id: 7, name: '口腔科' }, { id: 8, name: '心电图室' },
  { id: 9, name: '彩超室' }, { id: 10, name: '骨密度室' },
  { id: 11, name: '采血室' },
]

function getPatientName(r) {
  if (!r) return '未知'
  const examId = r.id || r
  return r.patientName || r.userName || clinic.examRecords[examId]?.patientName || '#' + examId
}
function deptName(id) { return departments.find(d => d.id === id)?.name || '未知' }

/** Departments this patient needs to visit (from their package/progress items) */
const patientDepts = computed(() => {
  if (!patientProgress.value.length) return []
  const deptIds = [...new Set(patientProgress.value.map(p => p.departmentId).filter(id => id != null))]

  // Build queue status map from backend data
  const progressDone = new Set()
  const progressAllDone = new Set()
  for (const p of patientProgress.value) {
    if (p.progressStatus === 'COMPLETED' && p.departmentId) {
      progressDone.add(p.departmentId)
    }
  }
  // A department is "all done" when ALL its progress items are completed
  for (const p of patientProgress.value) {
    if (p.departmentId && p.progressStatus === 'COMPLETED') {
      // Check if ALL items for this dept are completed
      const allItems = patientProgress.value.filter(x => x.departmentId === p.departmentId)
      if (allItems.every(x => x.progressStatus === 'COMPLETED')) {
        progressAllDone.add(p.departmentId)
      }
    }
  }

  // Which departments has this patient queued in? (only WAITING status means actively waiting)
  const backendQueued = new Set(patientQueueEntries.value
    .filter(q => q.queueStatus === 'WAITING')
    .map(q => q.departmentId))

  // Which departments has this patient been called by the doctor?
  const backendCalled = new Set(patientQueueEntries.value
    .filter(q => q.queueStatus === 'CALLED')
    .map(q => q.departmentId))

  return deptIds.map(id => {
    const dept = departments.find(d => d.id === id)
    if (!dept) return null
    let status = 'pending'
    if (progressAllDone.has(id)) status = 'done'
    else if (backendQueued.has(id)) status = 'queued'
    else if (backendCalled.has(id)) status = 'examining'
    return { ...dept, status }
  }).filter(Boolean)
})

function deptDone(id) { return patientProgress.value.some(p => p.departmentId === id && p.progressStatus === 'COMPLETED') }

async function loadPatientProgress() {
  if (!selectedExamId.value) { patientProgress.value = []; return }
  try {
    const res = await getExamProgress(Number(selectedExamId.value))
    patientProgress.value = res?.data || []
  } catch { patientProgress.value = [] }
}

/** Load which departments the selected patient has queued in (from all departments) */
async function loadPatientQueues() {
  if (!selectedExamId.value) { patientQueueEntries.value = []; return }
  const allEntries = []
  for (const d of departments) {
    try {
      const res = await getQueue(d.id)
      const entries = (res?.data || []).filter(q => q.examRecordId === Number(selectedExamId.value))
      allEntries.push(...entries)
    } catch {}
  }
  patientQueueEntries.value = allEntries
}

function onPatientSelect() {
  selectedDeptId.value = null
  msg.value = null
  Promise.all([loadPatientProgress(), loadPatientQueues()])
}

async function loadCheckedIn() {
  try {
    const res = await getCheckedIn(auth.currentBranchId)
    checkedInList.value = res?.data || []
    clinic.setCheckedInList(checkedInList.value)
    checkedInList.value.forEach(r => { clinic.addExamRecord(r) })
  } catch {}
}

async function loadAllQueueStats() {
  try {
    const res = await getAllQueueStats()
    const data = res?.data || {}
    for (const d of departments) { queueStats[d.id] = data[d.id] || { waiting: 0, completed: 0 } }
  } catch {
    for (const d of departments) { queueStats[d.id] = { waiting: 0, completed: 0 } }
  }
}

async function handleScan() {
  if (!selectedExamId.value || !selectedDeptId.value) { ElMessage.warning('请选择患者和科室'); return }
  scanning.value = true; msg.value = null
  try {
    const res = await addToQueue({
      examRecordId: Number(selectedExamId.value),
      departmentId: selectedDeptId.value
    })
    if (res?.data) {
      const patient = getPatientName(selectedExamId.value)
      const dept = deptName(selectedDeptId.value)
      ElMessage.success(`✅ ${patient} 已在「${dept}」报到成功 · 排队号: ${res.data.queueNo}`)
      msg.value = { type: 'alert-success', icon: 'check', text: `${patient} → ${dept} 报到成功！排队号 ${res.data.queueNo}` }
      selectedDeptId.value = null
      // Refresh stats + queue data + today's registrations
      await Promise.all([loadAllQueueStats(), loadPatientQueues(), loadTodayRegistrations()])
    }
  } catch (e) {
    const err = e.response?.data?.message || ''
    if (err.includes('vital signs')) {
      msg.value = { type: 'alert-warn', icon: 'flag', text: '⚠️ 请先完成体征测量才能报到' }
    } else {
      msg.value = { type: 'alert-error', icon: 'flag', text: '❌ 报到失败：' + (err || '请重试') }
    }
  } finally { scanning.value = false }
}

/** Polling: auto-refresh stats + selected patient's queue status every 5s */
function startPolling() {
  pollTimer = setInterval(async () => {
    await loadAllQueueStats()
    if (selectedExamId.value) {
      await loadPatientQueues()
      // Also refresh progress to pick up COMPLETED changes
      await loadPatientProgress()
    }
  }, 5000)
}

async function loadTodayRegistrations() {
  try {
    const res = await getTodayQueueRegistrations()
    const list = res?.data || []
    // Deduplicate: keep only the latest entry per (examRecordId, departmentId) pair
    const seen = new Set()
    const scans = []
    for (const q of list) {
      const key = q.examRecordId + '-' + q.departmentId
      if (seen.has(key)) continue
      seen.add(key)
      const patient = clinic.examRecords[q.examRecordId]?.patientName || clinic.examRecords[q.examRecordId]?.userName || q.patientName || '#' + q.examRecordId
      const dept = departments.find(d => d.id === q.departmentId)?.name || '未知'
      scans.push({
        examId: q.examRecordId,
        patient,
        dept,
        deptId: q.departmentId,
        time: q.createdAt?.replace('T', ' ').slice(5, 16) || '-'
      })
    }
    recentScans.value = scans
  } catch {}
}

let visibilityTimer = null
function onVisibilityChange() {
  if (document.visibilityState === 'visible') {
    if (visibilityTimer) clearTimeout(visibilityTimer)
    visibilityTimer = setTimeout(() => {
      loadCheckedIn()
      loadAllQueueStats()
      loadTodayRegistrations()
    }, 300)
  }
}

onMounted(async () => {
  await loadCheckedIn()
  loadAllQueueStats()
  loadTodayRegistrations()
  startPolling()
  document.addEventListener('visibilitychange', onVisibilityChange)
})
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  document.removeEventListener('visibilitychange', onVisibilityChange)
  if (visibilityTimer) clearTimeout(visibilityTimer)
})
</script>

<style scoped>
.kiosk-step { display: flex; gap: 16px; margin-bottom: 20px; padding: 12px 0; border-bottom: 1px solid rgba(0,0,0,0.06); }
.kiosk-step:last-child { border-bottom: none; margin-bottom: 0; }
.step-badge { width: 32px; height: 32px; border-radius: 50%; background: var(--blue); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.step-content { flex: 1; }
.step-content label { font-size: 13px; font-weight: 600; color: var(--n-700); display: block; margin-bottom: 8px; }
.kiosk-select { height: 40px; font-size: 14px; }

.dept-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 6px; }
.dept-btn { display: flex; align-items: center; gap: 6px; padding: 8px 10px; border: 1px solid var(--n-300); border-radius: 8px; background: var(--card); cursor: pointer; font-size: 12px; color: var(--n-600); transition: all 0.12s; position: relative; }
.dept-btn:hover:not(:disabled) { border-color: var(--blue); color: var(--blue); }
.dept-btn.active { background: var(--blue); color: #fff; border-color: var(--blue); }
.dept-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.dept-btn.status-done { background: #ecfdf5; border-color: #6ee7b7; color: var(--green-dark); }
.dept-btn.status-examining { background: #dbeafe; border-color: #93c5fd; color: var(--blue); }
.status-done { font-size: 10px; color: var(--green-dark); }
.status-queued { font-size: 10px; color: var(--blue); }
.status-calling { font-size: 10px; color: var(--blue); font-weight: 500; }
.status-pending { font-size: 10px; color: var(--n-400); }

.btn-lg { height: 48px; font-size: 16px; padding: 0 32px; }
.dept-stat { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.dept-stat-name { font-size: 13px; color: var(--n-700); font-weight: 500; }
.dept-stat-nums { display: flex; gap: 6px; }
.section-label { font-size: 12px; font-weight: 600; color: var(--n-500); letter-spacing: 0.5px; margin-top: 8px; }
.scan-row { display: flex; align-items: center; gap: 8px; padding: 4px 0; font-size: 13px; color: var(--n-600); }
</style>
