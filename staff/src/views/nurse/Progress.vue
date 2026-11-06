<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>检查进度管理</h2>
        <p class="desc">按科室查看患者各检查项目的完成进度</p>
      </div>
      <div class="page-hd-right" v-if="polling">
        <span class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 自动刷新中
        </span>
      </div>
      <div class="stat-card" style="width:200px;padding:14px 20px;margin:0">
        <div class="stat-num" style="font-size:22px">{{ completedCount }}/{{ totalCount }}</div>
        <div class="stat-label">已完成项目</div>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <div class="card-hd"><span><SvgIcon name="search" :size="16" style="margin-right:6px;color:var(--blue)" /> 查询检查进度</span></div>
        <div class="card-bd">
          <div class="form-grid form-2">
            <div class="field"><label>选择患者</label>
              <select v-model="examRecordId" class="fselect" @change="loadProgress">
                <option value="" disabled>─ 请选择已签到患者 ─</option>
                <option v-for="r in clinic.checkedInList" :key="r.id" :value="r.id">
                  {{ getPatientName(r) }} — {{ r.examNo }}
                </option>
              </select>
            </div>
            <div class="field"><label>&nbsp;</label><button class="btn btn-primary" @click="loadProgress" style="margin-top:16px"><SvgIcon name="search" :size="16" /> 查询进度</button></div>
          </div>
          <!-- Package info -->
          <div v-if="selectedExam && selectedExam.packageName" class="package-badge">
            <SvgIcon name="check" :size="13" style="margin-right:4px" /> 套餐：{{ selectedExam.packageName }}
          </div>
        </div>
      </div>
      <div class="card" style="background:var(--blue-light)">
        <div class="card-hd"><span><SvgIcon name="chart" :size="16" style="margin-right:6px;color:var(--blue)" /> 进度概览</span></div>
        <div class="card-bd">
          <div style="height:8px;background:rgba(0,0,0,0.06);border-radius:4px;overflow:hidden">
            <div :style="{ width: pct + '%', height:'100%', background:'var(--blue)', borderRadius:'4px', transition:'width 0.4s' }"></div>
          </div>
          <div style="display:flex;justify-content:space-between;font-size:13px;color:var(--n-500);margin-top:4px">
            <span>已检查 {{ completedCount }} 项</span>
            <span>总进度 {{ pct }}%</span>
          </div>
          <!-- Fasting reminder -->
          <div v-if="hasFastingDept" class="fasting-reminder">
            <SvgIcon name="flag" :size="14" style="margin-right:4px" /> ⚠️ 该套餐包含需空腹项目（检验科、腹部彩超），建议优先完成
          </div>
        </div>
      </div>
    </div>

    <!-- Progress by department groups -->
    <div v-if="deptGroups.length" class="dept-groups">
      <div v-for="group in deptGroups" :key="group.deptId" class="card dept-group">
        <div class="card-hd">
          <span>
            <SvgIcon name="list" :size="16" style="margin-right:6px;color:var(--blue)" />
            {{ group.deptName }}
          </span>
          <div style="display:flex;gap:6px;align-items:center">
            <span v-if="group.fastingRequired" class="tag tag-warning">需空腹</span>
            <span :class="['tag', group.completed === group.items.length ? 'tag-green' : 'tag-orange']">
              {{ group.completed }}/{{ group.items.length }}
            </span>
          </div>
        </div>
        <div class="card-bd pad-0">
          <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th class="col-nowrap">检查项目</th><th class="col-nowrap">状态</th><th class="col-nowrap">操作</th><th class="col-nowrap">完成时间</th></tr></thead>
              <tbody>
                <tr v-for="row in group.items" :key="row.id">
                  <td>{{ itemName(row.itemId) }}</td>
                  <td><span :class="['tag', statusClass(row.progressStatus)]">{{ statusLabel(row.progressStatus) }}</span></td>
                  <td>
                    <button class="btn btn-sm btn-secondary" @click="update(row.id,'START')" :disabled="row.progressStatus!=='PENDING'"><SvgIcon name="play" :size="13" /> 开始</button>
                    <button class="btn btn-sm btn-secondary" @click="handleSkip(row)" :disabled="row.progressStatus!=='PENDING'" style="margin-left:4px"><SvgIcon name="close" :size="13" /> 弃检</button>
                    <button class="btn btn-sm btn-primary" @click="update(row.id,'COMPLETE')" :disabled="row.progressStatus!=='IN_PROGRESS'" style="margin-left:4px"><SvgIcon name="check" :size="13" /> 完成</button>
                  </td>
                  <td style="font-size:13px;color:var(--n-400)">{{ row.completedAt?.replace('T',' ') || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="card" style="margin-top:0">
      <div class="empty-state" style="padding:40px">
        <SvgIcon name="clock" :size="36" style="color:var(--n-300);margin-bottom:12px" />
        <span class="empty-text">暂无进度数据，请先完成患者签到</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { getExamProgress, updateProgress, getCheckedIn, getDepartments } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const clinic = useClinicStore()
const auth = useAuthStore()

const examRecordId = ref('')
const progressList = ref([])
const deptInfoMap = reactive({})  // deptId -> { name, fastingRequired }
const polling = ref(false)
let pollTimer = null

function startPolling() {
  polling.value = true
  pollTimer = setInterval(() => { loadCheckedIn() }, 10000)
}

function stopPolling() {
  polling.value = false
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

const getPatientName = (r) => r.patientName || r.record?.patientName || clinic.examRecords[r.id || r.record?.id]?.patientName || '#' + (r.userId || r.record?.userId || r.id)
const selectedExam = computed(() => examRecordId.value ? clinic.examRecords[Number(examRecordId.value)] : null)
const completedCount = computed(() => progressList.value.filter(p => p.progressStatus === 'COMPLETED').length)
const totalCount = computed(() => progressList.value.length)
const pct = computed(() => totalCount.value ? Math.round(completedCount.value / totalCount.value * 100) : 0)
const hasFastingDept = computed(() => deptGroups.value.some(g => g.fastingRequired))

const itemName = (id) => clinic.itemNames[id] || '#' + id

// Group progress items by department
const deptGroups = computed(() => {
  const groups = {}
  for (const p of progressList.value) {
    const deptId = p.departmentId || 0
    if (!groups[deptId]) {
      const info = deptInfoMap[deptId] || { name: '#' + deptId, fastingRequired: false }
      groups[deptId] = { deptId, deptName: info.name, fastingRequired: info.fastingRequired, items: [], completed: 0 }
    }
    groups[deptId].items.push(p)
    if (p.progressStatus === 'COMPLETED') groups[deptId].completed++
  }
  return Object.values(groups).sort((a, b) => a.deptId - b.deptId)
})

function statusClass(s) { return { PENDING:'tag-gray', IN_PROGRESS:'tag-blue', COMPLETED:'tag-green', SKIPPED:'tag-orange' }[s] || 'tag-gray' }
function statusLabel(s) { return { PENDING:'待检查', IN_PROGRESS:'检查中', COMPLETED:'已完成', SKIPPED:'已跳过' }[s] || s }

async function loadProgress() {
  try {
    const res = await getExamProgress(Number(examRecordId.value))
    progressList.value = res?.data || []
  } catch { progressList.value = [] }
}

async function update(id, action) {
  try {
    await updateProgress(id, action)
    ElMessage.success(action === 'START' ? '检查已开始' : '检查已完成')
    await loadProgress()
  } catch {}
}

async function handleSkip(row) {
  try {
    await ElMessageBox.confirm('确认弃检该项？弃检后不可恢复', '提示', { type: 'warning', confirmButtonText: '确认弃检', cancelButtonText: '取消' })
    await updateProgress(row.id, 'SKIP')
    ElMessage.success('已弃检')
    await loadProgress()
  } catch {
    // cancelled or error
  }
}

async function loadDepartmentInfo() {
  try {
    const res = await getDepartments()
    if (res?.data) {
      res.data.forEach(d => { deptInfoMap[d.id] = { name: d.name, fastingRequired: d.fastingRequired } })
    }
  } catch {}
}

async function loadCheckedIn() {
  try {
    const res = await getCheckedIn(auth.currentBranchId)
    clinic.setCheckedInList(res?.data || [])
    ;(res?.data || []).forEach(r => { if(!clinic.examRecords[r.id]) clinic.addExamRecord(r) })
  } catch {}
}

onMounted(() => {
  loadDepartmentInfo()
  loadCheckedIn()
  clinic.getItemNamesFromApi()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.dept-groups { display: flex; flex-direction: column; gap: 12px; }
.dept-group { margin-top: 0; }
.tag-warning { background: #fef3c7; color: #b45309; }
.package-badge { margin-top: 8px; font-size: 12px; color: var(--blue); display: flex; align-items: center; }
.fasting-reminder { margin-top: 8px; font-size: 12px; color: #b45309; background: #fef3c7; padding: 6px 10px; border-radius: 6px; display: flex; align-items: center; }
.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100% { opacity:1; transform:scale(1) } 50% { opacity:0.5; transform:scale(0.8) } }
</style>
