<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>生命体征测量</h2>
        <p class="desc">录入患者身高、体重、血压、心率等基础体征数据</p>
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
          <span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg> 体征录入</span>
          <span class="tag tag-blue">护士</span>
        </div>
        <div class="card-bd">
          <div class="form-grid form-2">
            <div class="field">
              <label>选择患者</label>
              <select v-model="selectedExamId" class="fselect" @change="onPatientSelect">
                <option value="" disabled>─ 请选择已签到患者 ─</option>
                <option v-for="r in clinic.checkedInList" :key="r.id" :value="r.id">
                  {{ getPatientName(r) }} — {{ r.examNo }}
                </option>
              </select>
              <div v-if="!clinic.checkedInList.length" style="font-size:12px;color:var(--n-400);margin-top:4px">
                暂无已签到患者，请先到签到页办理
              </div>
            </div>
            <div class="field">
              <label>记录护士</label>
              <input value="小王" class="finput" readonly />
            </div>
          </div>

          <div class="divider"></div>
          <div class="section-label" style="margin-bottom:10px">体征数据录入</div>
          <div class="vital-grid">
            <div class="vital-field"><span class="vital-label">身高</span><input v-model="f.height" class="vital-input" placeholder="175" /><span class="vital-unit">cm</span></div>
            <div class="vital-field"><span class="vital-label">体重</span><input v-model="f.weight" class="vital-input" placeholder="70" /><span class="vital-unit">kg</span></div>
            <div class="vital-field"><span class="vital-label">腰围</span><input v-model="f.waistline" class="vital-input" placeholder="85" /><span class="vital-unit">cm</span></div>
            <div class="vital-field"><span class="vital-label">收缩压</span><input v-model="f.systolic" class="vital-input" placeholder="120" /><span class="vital-unit">mmHg</span></div>
            <div class="vital-field"><span class="vital-label">舒张压</span><input v-model="f.diastolic" class="vital-input" placeholder="80" /><span class="vital-unit">mmHg</span></div>
            <div class="vital-field"><span class="vital-label">心率</span><input v-model="f.heartRate" class="vital-input" placeholder="72" /><span class="vital-unit">bpm</span></div>
            <div class="vital-field"><span class="vital-label">体温</span><input v-model="f.temperature" class="vital-input" placeholder="36.5" /><span class="vital-unit">℃</span></div>
            <div class="vital-field"><span class="vital-label">左眼视力</span><input v-model="f.visionLeft" class="vital-input" placeholder="5.0" /></div>
            <div class="vital-field"><span class="vital-label">右眼视力</span><input v-model="f.visionRight" class="vital-input" placeholder="5.0" /></div>
          </div>

          <div class="btn-group">
            <button class="btn btn-primary" :disabled="saving || !selectedExamId" @click="handleSave">
              <SvgIcon name="save" :size="16" /> 保存体征数据
            </button>
            <button class="btn btn-secondary btn-sm" @click="handleQuery" :disabled="!selectedExamId">
              <SvgIcon name="search" :size="14" /> 查询现有数据
            </button>
          </div>
          <transition name="fade">
            <div v-if="saveMsg" :class="['alert', saveMsg.type]">{{ saveMsg.text }}</div>
          </transition>
        </div>
      </div>

      <div class="card">
        <div class="card-hd">
          <span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg> 体征数据</span>
        </div>
        <div class="card-bd">
          <div v-if="vitalData" class="result-grid">
            <div class="result-item"><span class="r-label">身高</span><span class="r-value">{{ vitalData.height }} cm</span></div>
            <div class="result-item"><span class="r-label">体重</span><span class="r-value">{{ vitalData.weight }} kg</span></div>
            <div class="result-item"><span class="r-label">BMI</span><span class="r-value">{{ vitalData.bmi != null ? vitalData.bmi : '-' }}</span></div>
            <div class="result-item"><span class="r-label">腰围</span><span class="r-value">{{ vitalData.waistline || '-' }} cm</span></div>
            <div class="result-item"><span class="r-label">收缩压</span><span class="r-value">{{ vitalData.bloodPressureSystolic }} mmHg</span></div>
            <div class="result-item"><span class="r-label">舒张压</span><span class="r-value">{{ vitalData.bloodPressureDiastolic }} mmHg</span></div>
            <div class="result-item"><span class="r-label">心率</span><span class="r-value">{{ vitalData.heartRate }} bpm</span></div>
            <div class="result-item"><span class="r-label">体温</span><span class="r-value">{{ vitalData.temperature }} ℃</span></div>
            <div class="result-item"><span class="r-label">左眼视力</span><span class="r-value">{{ vitalData.visionLeft || '-' }}</span></div>
            <div class="result-item"><span class="r-label">右眼视力</span><span class="r-value">{{ vitalData.visionRight || '-' }}</span></div>
          </div>
          <div v-else class="empty-state" style="padding:16px 0">
            <SvgIcon name="heart" :size="28" style="color:var(--n-300);margin-bottom:6px" />
            <span class="empty-text">暂无体征数据</span>
          </div>
          <div v-if="vitalData" class="divider"></div>
          <div v-if="vitalData" style="font-size:12px;color:var(--n-400)">记录时间: {{ vitalData.recordedAt?.replace('T',' ') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { recordVitalSigns, getVitalSigns, getCheckedIn, getCheckedInWithDetails } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const clinic = useClinicStore()
const auth = useAuthStore()
const f = reactive({ height:'170', weight:'65', systolic:'120', diastolic:'80', heartRate:'72', temperature:'36.5', visionLeft:'5.0', visionRight:'5.0', waistline:'85' })
const saving = ref(false); const vitalData = ref(null); const saveMsg = ref(null)
const selectedExamId = ref('')
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
    let list = []
    // 先尝试获取详细签到列表（含患者姓名）
    const detailRes = await getCheckedInWithDetails(auth.currentBranchId)
    if (detailRes?.data?.length) {
      list = detailRes.data
    } else {
      const res = await getCheckedIn(auth.currentBranchId)
      list = res?.data || []
    }
    clinic.setCheckedInList(list)
    list.forEach(r => {
      const examId = r.record?.id || r.id
      if (examId && !clinic.examRecords[examId]) {
        clinic.addExamRecord({ id: examId, patientName: r.patientName || '患者#' + (r.record?.userId || r.userId), userId: r.record?.userId || r.userId })
      }
    })
  } catch { /* polling error, ignore */ }
}

function getPatientName(record) {
  return record?.patientName || record?.record?.patientName || clinic.examRecords[record?.id || record?.record?.id]?.patientName || '#' + (record?.record?.userId || record?.userId || '?')
}

function onPatientSelect() {
  saveMsg.value = null
  if (selectedExamId.value) {
    handleQuery()
  }
}

async function handleSave() {
  if (!selectedExamId.value) { ElMessage.warning('请先选择患者'); return }
  if (!f.height || !f.weight || !f.systolic || !f.diastolic || !f.heartRate) {
    ElMessage.warning('身高、体重、收缩压、舒张压、心率为必填项'); return
  }
  saving.value = true; saveMsg.value = null
  try {
    const body = { examRecordId: Number(selectedExamId.value), recordedBy: 1 }
    const map = [['height','height'],['weight','weight'],['waistline','waistline'],['systolic','bloodPressureSystolic'],['diastolic','bloodPressureDiastolic'],['heartRate','heartRate'],['temperature','temperature'],['visionLeft','visionLeft'],['visionRight','visionRight']]
    map.forEach(([k,bk]) => { if (f[k] !== '' && f[k] != null) body[bk] = Number(f[k]) })
    const res = await recordVitalSigns(body)
    if (res?.data) { vitalData.value = res.data; saveMsg.value = { type:'alert-success', text:'体征数据保存成功' }; ElMessage.success('体征录入成功') }
  } catch { saveMsg.value = { type:'alert-error', text:'保存失败' }; ElMessage.error('体征保存失败') }
  finally { saving.value = false }
}

async function handleQuery() {
  if (!selectedExamId.value) return
  try { const res = await getVitalSigns(Number(selectedExamId.value)); vitalData.value = res?.data || null } catch { vitalData.value = null; ElMessage.error('查询体征数据失败') }
}

onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.section-label { font-size: 12px; font-weight: 600; color: var(--text-secondary); }

.vital-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px; }
.vital-field {
  display: flex; flex-direction: column; gap: 2px;
  background: #f8fafc; border-radius: 12px; padding: 12px 14px 10px;
  border: 1px solid transparent; transition: all 0.15s;
}
.vital-field:focus-within { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 3px rgba(14,165,233,0.08); }
.vital-label { font-size: 11px; color: var(--text-secondary); font-weight: 500; }
.vital-input {
  border: none; background: transparent; font-size: 18px; font-weight: 600;
  color: var(--title); outline: none; font-family: var(--font); padding: 0; width: 100%;
}
.vital-input::placeholder { color: #cbd5e1; font-weight: 400; font-size: 14px; }
.vital-unit { font-size: 11px; color: var(--text-secondary); text-align: right; }

.result-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.result-item { text-align: center; padding: 10px 8px; background: #f8fafc; border-radius: 10px; }
.r-label { display: block; font-size: 11px; color: var(--text-secondary); margin-bottom: 2px; }
.r-value { display: block; font-size: 16px; font-weight: 600; color: var(--title); }

.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100% { opacity:1; transform:scale(1) } 50% { opacity:0.5; transform:scale(0.8) } }
</style>
