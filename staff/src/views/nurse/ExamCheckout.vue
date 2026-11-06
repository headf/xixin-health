<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>体检收尾确认</h2>
        <p class="desc">核对所有检查项目是否全部完成，确认无误后结束体检流程</p>
      </div>
      <div class="page-hd-right" v-if="polling">
        <span class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 自动刷新中
        </span>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="clipboard" :size="16" style="margin-right:6px;color:var(--blue)" /> 收尾操作</span>
          <span class="tag tag-blue">护士</span>
        </div>
        <div class="card-bd">
          <div class="form-grid form-2">
            <div class="field">
              <label>选择患者</label>
              <select v-model="selectedExamId" class="fselect" @change="loadStatus">
                <option value="" disabled>─ 请选择已签到患者 ─</option>
                <option v-for="r in clinic.checkedInList" :key="r.id" :value="r.id">
                  {{ getPatientName(r) }} — {{ r.examNo }}
                </option>
              </select>
            </div>
            <div class="field">
              <label>确认护士</label>
              <input value="小王" class="finput" readonly />
            </div>
          </div>

          <div class="divider"></div>
          <div class="section-label">核对清单</div>
          <div class="checklist">
            <div class="checklist-item" :class="{ done: statusData?.vitalSignsTaken }">
              <span class="check-icon"><SvgIcon :name="statusData?.vitalSignsTaken ? 'check' : 'circle'" :size="18" /></span>
              <span>基础体征测量完成（身高/体重/BMI/血压/脉搏）</span>
            </div>
            <div class="checklist-item" :class="{ done: statusData?.specimenCollected }">
              <span class="check-icon"><SvgIcon :name="statusData?.specimenCollected ? 'check' : 'circle'" :size="18" /></span>
              <span>采血、尿液标本已采集</span>
            </div>
            <div class="checklist-item" :class="{ done: statusData?.allItemsChecked }">
              <span class="check-icon"><SvgIcon :name="statusData?.allItemsChecked ? 'check' : 'circle'" :size="18" /></span>
              <span>所有检查项目已完成、无遗漏</span>
            </div>
            <div class="checklist-item" :class="{ done: statusData?.nurseConfirmed }">
              <span class="check-icon"><SvgIcon :name="statusData?.nurseConfirmed ? 'check' : 'circle'" :size="18" /></span>
              <span>护士确认收尾</span>
            </div>
            <div v-if="statusData?.nurseConfirmed && statusData?.needsReview" class="checklist-item done" style="border-left:3px solid #165DFF">
              <span class="check-icon"><SvgIcon name="flag" :size="18" style="color:#165DFF" /></span>
              <span><strong>需送总检审核</strong> — 该套餐需要审核医生综合评审</span>
            </div>
          </div>

          <div class="btn-group" style="margin-top:16px">
            <button class="btn btn-primary btn-lg" :disabled="saving || !selectedExamId || statusData?.nurseConfirmed" @click="handleConfirm">
              <SvgIcon name="check" :size="18" /> {{ statusData?.nurseConfirmed ? (statusData?.needsReview ? '已送总检审核' : '已确认完成') : '确认完成' }}
            </button>
            <button class="btn btn-danger btn-lg" :disabled="saving || !selectedExamId || statusData?.nurseConfirmed" @click="handleForceComplete" style="margin-left:8px">
              <SvgIcon name="flag" :size="18" /> 强制完成（测试）
            </button>
          </div>
          <transition name="fade">
            <div v-if="msg" :class="['alert', msg.type]">{{ msg.text }}</div>
          </transition>
        </div>
      </div>

      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="info" :size="16" style="margin-right:6px;color:var(--blue)" /> 当前检查进度</span>
        </div>
        <div class="card-bd">
          <div v-if="progressList.length" style="display:grid;gap:6px">
            <div v-for="row in progressList" :key="row.id" class="progress-item-row">
              <span class="progress-item-name">{{ itemName(row.itemId) }}</span>
              <span :class="['tag', statusClass(row.progressStatus)]">{{ statusLabel(row.progressStatus) }}</span>
            </div>
          </div>
          <div v-else class="empty-state">
            <SvgIcon name="clock" :size="36" style="color:var(--n-300);margin-bottom:12px" />
            <span class="empty-text">暂无进度数据</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getExamProgress, getCompletionStatus, confirmCompletion, forceComplete, getCheckedIn } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const clinic = useClinicStore()
const auth = useAuthStore()
const selectedExamId = ref('')
const statusData = ref(null)
const progressList = ref([])
const saving = ref(false)
const msg = ref(null)
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

async function loadCheckedIn() {
  try {
    const res = await getCheckedIn(auth.currentBranchId)
    clinic.setCheckedInList(res?.data || [])
    ;(res?.data || []).forEach(r => { if(!clinic.examRecords[r.id]) clinic.addExamRecord(r) })
  } catch {}
}

const itemName = (id) => clinic.itemNames[id] || '#' + id
const getPatientName = (r) => r.patientName || r.record?.patientName || clinic.examRecords[r.id || r.record?.id]?.patientName || '#' + (r.userId || r.record?.userId || r.id)
function statusClass(s) { return { PENDING:'tag-gray', IN_PROGRESS:'tag-blue', COMPLETED:'tag-green', SKIPPED:'tag-orange' }[s] || 'tag-gray' }
function statusLabel(s) { return { PENDING:'待检查', IN_PROGRESS:'检查中', COMPLETED:'已完成', SKIPPED:'已跳过' }[s] || s }

async function loadStatus() {
  if (!selectedExamId.value) return
  try {
    const [statusRes, progressRes] = await Promise.all([
      getCompletionStatus(Number(selectedExamId.value)),
      getExamProgress(Number(selectedExamId.value))
    ])
    statusData.value = statusRes?.data || null
    progressList.value = progressRes?.data || []
  } catch { statusData.value = null; progressList.value = [] }
}

async function handleConfirm() {
  if (!selectedExamId.value) { ElMessage.warning('请先选择患者'); return }
  saving.value = true; msg.value = null
  try {
    const res = await confirmCompletion(Number(selectedExamId.value), 1)
    if (res?.data) {
      statusData.value = res.data
      if (res.data?.needsReview) {
        ElMessage.success('体检已送总检审核')
        msg.value = { type: 'alert-success', text: '所有项目已完成，已送总检审核医生处等待综合评审 ✓' }
      } else {
        ElMessage.success('体检收尾确认成功！患者可以离开')
        msg.value = { type: 'alert-success', text: '所有项目已完成，体检流程结束 ✓' }
      }
    }
  } catch (e) {
    const errMsg = e.response?.data?.message || '确认失败，请检查是否有未完成项目'
    ElMessage.error(errMsg)
    msg.value = { type: 'alert-error', text: errMsg }
  } finally { saving.value = false }
}

async function handleForceComplete() {
  if (!selectedExamId.value) { ElMessage.warning('请先选择患者'); return }
  saving.value = true; msg.value = null
  try {
    const res = await forceComplete(Number(selectedExamId.value), 1)
    statusData.value = res?.data || null
    if (res?.data?.needsReview) {
      ElMessage.success('✅ 强制完成！已送总检审核')
      msg.value = { type: 'alert-success', text: '已强制完成所有项目（测试模式），已送总检审核 ✓' }
    } else {
      ElMessage.success('✅ 强制完成！体检结束')
      msg.value = { type: 'alert-success', text: '已强制完成所有项目（测试模式），体检流程结束 ✓' }
    }
  } catch (e) {
    const errMsg = e.response?.data?.message || '操作失败'
    ElMessage.error(errMsg)
    msg.value = { type: 'alert-error', text: errMsg }
  } finally { saving.value = false }
}

onMounted(() => {
  clinic.getItemNamesFromApi()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.section-label { font-size: 12px; font-weight: 600; color: var(--n-500); letter-spacing: 0.5px; margin-bottom: 8px; }
.checklist { display: flex; flex-direction: column; gap: 12px; }
.checklist-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px; border-radius: 8px;
  background: var(--n-50); font-size: 14px; color: var(--n-500);
  transition: all 0.2s;
}
.checklist-item.done { background: #ecfdf5; color: var(--green-dark); }
.check-icon { color: var(--n-300); }
.checklist-item.done .check-icon { color: var(--green-dark); }
.btn-lg { height: 48px; font-size: 15px; padding: 0 28px; width: 100%; }
.progress-item-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; border-radius: 6px; background: var(--n-50);
}
.progress-item-name { font-size: 13px; color: var(--n-700); }
.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100% { opacity:1; transform:scale(1) } 50% { opacity:0.5; transform:scale(0.8) } }
</style>
