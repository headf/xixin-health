<template>
  <div class="queue-page">
    <!-- 分院选择（分院管理员只显示其分院名称，不可切换） -->
    <div style="margin-bottom:16px;display:flex;align-items:center;gap:12px">
      <div class="section-title" style="margin-bottom:0">
        <el-icon style="margin-right:6px;color:var(--primary)"><School /></el-icon>
        当前分院
      </div>
      <template v-if="auth.isBranchAdmin">
        <el-tag size="large" style="font-size:14px;padding:8px 16px">
          {{ currentBranchName }}
        </el-tag>
      </template>
      <el-select v-else v-model="selectedBranchId" @change="onBranchChange" style="width:240px">
        <el-option
          v-for="b in branchList"
          :key="b.id"
          :label="b.branchName"
          :value="b.id"
        />
      </el-select>
      <span style="font-size:13px;color:var(--text-secondary);margin-left:8px">
        {{ deptStatsTotal }} 人排队等候
      </span>
    </div>

    <!-- 看板式科室概览 -->
    <div style="margin-bottom:20px">
      <div class="section-title">
        <el-icon style="margin-right:6px;color:var(--primary)"><DataBoard /></el-icon>
        {{ currentBranchName }} - 科室排队概览
      </div>
      <div v-if="departments.length === 0" style="text-align:center;padding:40px 0;color:var(--text-placeholder)">
        该分院暂无科室数据
      </div>
      <div v-else class="dept-grid">
        <div v-for="d in departments" :key="d.id"
          :class="['dept-card', deptLevelClass(d.id)]"
          @click="selectDept(d)">
          <div class="dept-name">{{ d.departmentName }}</div>
          <div class="dept-count" :style="{ color: deptCountColor(d.id) }">{{ deptStats[d.id]?.waiting || 0 }}</div>
          <div class="dept-label">等待人数</div>
          <div v-if="d.fastingRequired" class="fasting-tag">需空腹</div>
        </div>
      </div>
    </div>

    <!-- 选中科室的队列详情 -->
    <el-card v-if="selectedDept" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <span class="card-header-title">{{ selectedDept.departmentName }}</span>
            <span :class="['tag', queueCount > 0 ? 'tag-orange' : 'tag-gray']">{{ queueCount }}人排队</span>
          </div>
          <div style="display:flex;gap:8px">
            <el-button size="small" :loading="loading" @click="loadQueues">
              <el-icon style="margin-right:4px"><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 当前叫号 -->
      <div class="current-call" v-if="currentPatient" role="status" aria-live="polite">
        <div class="call-label">当前就诊</div>
        <div class="call-no">{{ currentPatient.queueNo }}</div>
        <div class="call-name">{{ getPatientName(currentPatient) }}</div>
        <el-button type="success" size="small" @click="completeCurrent(currentPatient)">
          完成检查
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table v-show="queueList.length > 0" v-loading="loading" :data="queueList" stripe style="width:100%">
        <el-table-column label="排队号" width="100">
          <template #default="{ row }">
            <span style="font-weight:var(--fw-bold);font-size:16px;color:var(--title)">{{ row.queueNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" width="120">
          <template #default="{ row }">{{ getPatientName(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.queueStatus)" size="small">{{ statusLabel(row.queueStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" :disabled="row.queueStatus==='CALLED'" @click="callPatient(row)">叫号</el-button>
            <el-button size="small" type="success" :disabled="row.queueStatus==='COMPLETED'" @click="completePatient(row)">完成</el-button>
            <el-button size="small" :disabled="row.queueStatus==='SKIPPED'" @click="skipPatient(row)">跳过</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && queueList.length === 0" description="暂无排队患者" :image-size="80" />
    </el-card>

    <el-empty v-if="!selectedDept && departments.length > 0" description="点击上方科室卡片查看详情" :image-size="80" style="margin-top:60px" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { getDepartments, getDepartmentQueue, updateQueueStatus, getAllQueueStats, getCheckedIn, getBranches } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const branchList = ref([])
const selectedBranchId = ref(auth.currentBranchId || 1)
const departments = ref([])
const selectedDept = ref(null)
const queueList = ref([])
const deptStats = reactive({})
const checkedInMap = ref({})
const currentPatient = ref(null)
const loading = ref(false)

const currentBranchName = computed(() => {
  const b = branchList.value.find(b => b.id === selectedBranchId.value)
  return b ? b.branchName : '分院'
})

const queueCount = computed(() => {
  return queueList.value.filter(q => q.queueStatus === 'WAITING').length
})

const deptStatsTotal = computed(() => {
  let total = 0
  for (const id of departments.value.map(d => d.id)) {
    total += deptStats[id]?.waiting || 0
  }
  return total
})

function statusType(s) {
  const m = { WAITING: 'info', CALLED: 'primary', ARRIVED: 'success', COMPLETED: '', SKIPPED: 'warning' }
  return m[s] || 'info'
}
function statusLabel(s) {
  const m = { WAITING: '等待中', CALLED: '已叫号', ARRIVED: '检查中', COMPLETED: '已完成', SKIPPED: '已跳过' }
  return m[s] || s || '-'
}

function deptLevelClass(id) {
  const w = deptStats[id]?.waiting || 0
  if (w === 0) return 'level-green'
  if (w <= 3) return 'level-yellow'
  if (w <= 8) return 'level-orange'
  return 'level-red'
}

function deptCountColor(id) {
  const w = deptStats[id]?.waiting || 0
  if (w === 0) return 'var(--success)'
  if (w <= 3) return 'var(--warning-dark)'
  if (w <= 8) return 'var(--warning)'
  return 'var(--danger)'
}

function getPatientName(row) {
  return checkedInMap.value[row.examRecordId] || `#${row.examRecordId}`
}

async function selectDept(dept) {
  selectedDept.value = dept
  await loadQueues()
}

async function loadQueues() {
  if (!selectedDept.value) return
  loading.value = true
  try {
    const res = await getDepartmentQueue(selectedDept.value.id)
    const list = res?.data || []
    queueList.value = list
    currentPatient.value = list.find(q => q.queueStatus === 'CALLED' || q.queueStatus === 'ARRIVED') || null
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadDeptStats() {
  if (loading.value) return
  try {
    const [res, checkedIn] = await Promise.all([getAllQueueStats(), getCheckedIn()])
    if (res?.data) Object.assign(deptStats, res.data)
    if (checkedIn?.data) {
      const m = {}
      checkedIn.data.forEach(r => { m[r.examRecordId || r.id] = r.patientName || '#' + r.userId })
      checkedInMap.value = m
    }
  } catch (e) { console.error(e) }
}

async function onBranchChange() {
  selectedDept.value = null
  queueList.value = []
  await loadBranchDepartments()
}

async function loadBranchDepartments() {
  try {
    const res = await getDepartments({ branchId: selectedBranchId.value })
    departments.value = res?.data?.filter(d => d.enabled !== false) || []
    if (departments.value.length > 0) {
      selectedDept.value = departments.value[0]
      await loadQueues()
    }
  } catch (e) { console.error(e) }
}

async function callPatient(row) {
  try { await updateQueueStatus(row.id, 'CALLED'); ElMessage.success(`已呼叫 ${row.queueNo} 号`); await loadQueues() }
  catch { ElMessage.error('操作失败') }
}
async function completePatient(row) {
  try { await updateQueueStatus(row.id, 'COMPLETED'); ElMessage.success(`${row.queueNo} 号已完成`); await loadQueues() }
  catch { ElMessage.error('操作失败') }
}
async function skipPatient(row) {
  try { await updateQueueStatus(row.id, 'SKIPPED'); ElMessage.info(`已跳过 ${row.queueNo} 号`); await loadQueues() }
  catch { ElMessage.error('操作失败') }
}
async function completeCurrent(row) { await completePatient(row) }

let timer = null
async function loadBranches() {
  try {
    const res = await getBranches()
    branchList.value = res?.data || []
  } catch (e) { console.error(e) }
}

onMounted(async () => {
  await loadBranches()
  await loadBranchDepartments()
  await loadDeptStats()
  timer = setInterval(loadDeptStats, 15000)
})
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.section-title {
  display: flex; align-items: center;
  font-size: var(--fs-body); font-weight: var(--fw-semibold);
  color: var(--title); margin-bottom: var(--space-3);
}
.dept-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: var(--space-3);
}
.dept-card {
  border-radius: var(--radius-base); padding: 18px 16px;
  cursor: pointer; text-align: center; position: relative;
  border: 1px solid transparent;
  transition: all var(--transition-base);
}
.dept-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.06); }
.dept-card.level-green { background: var(--secondary-light); border-color: #a7f3d0; }
.dept-card.level-yellow { background: var(--warning-light); border-color: #fde68a; }
.dept-card.level-orange { background: #fff7ed; border-color: #fed7aa; }
.dept-card.level-red { background: var(--danger-light); border-color: #fecaca; }
.dept-name { font-size: var(--fs-body); font-weight: var(--fw-semibold); color: var(--title); margin-bottom: 6px; }
.dept-count { font-size: var(--fs-h1); font-weight: 800; line-height: 1; margin-bottom: 2px; }
.dept-label { font-size: var(--fs-tiny); color: var(--text-secondary); }
.fasting-tag {
  position: absolute; top: 8px; right: 8px;
  font-size: 10px; background: var(--warning-light); color: var(--warning-dark);
  padding: 1px 6px; border-radius: var(--radius-pill);
}

.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-header-left { display: flex; align-items: center; gap: var(--space-3); }
.card-header-title { font-weight: var(--fw-semibold); font-size: 15px; }

.current-call {
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  color: #fff; border-radius: var(--radius-base); padding: var(--space-6);
  text-align: center; margin-bottom: var(--space-4);
}
.call-label { font-size: var(--fs-sm); opacity: 0.8; }
.call-no { font-size: 48px; font-weight: 800; line-height: 1; margin: var(--space-2) 0; }
.call-name { font-size: var(--fs-h4); margin-bottom: var(--space-3); }

@media (max-width: 640px) {
  .dept-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
