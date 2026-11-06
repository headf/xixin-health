<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>{{ deptName }}检查</h2>
        <p class="desc">体检专科医生一对一完成查体，填写检查结果与单项结论</p>
      </div>
      <div class="stat-card" style="width:180px;padding:14px 20px;margin:0;background:var(--blue-light)">
        <div class="stat-num" style="font-size:22px;color:var(--blue)">{{ waitingCount }}</div>
        <div class="stat-label">当前等待人数</div>
      </div>
    </div>

    <div class="two-col">
      <div style="display:flex;flex-direction:column;gap:16px">
        <!-- Queue & Call -->
        <div class="card">
          <div class="card-hd">
            <span><SvgIcon name="phone" :size="16" style="margin-right:6px;color:var(--blue)" /> 叫号 · {{ deptName }}</span>
            <div style="display:flex;gap:6px;align-items:center">
              <span v-if="pollingActive" style="font-size:11px;color:var(--green-dark)"><SvgIcon name="circle" :size="8" style="margin-right:2px" /> 自动刷新中</span>
              <button class="btn btn-secondary btn-sm" @click="loadAll"><SvgIcon name="refresh" :size="14" /> 刷新</button>
            </div>
          </div>
          <div class="card-bd">
            <div style="display:flex;gap:16px;align-items:center;justify-content:center;flex-wrap:wrap">
              <button class="btn btn-blue" :disabled="calling || waitingCount === 0" @click="handleCall" style="min-width:160px;height:44px">
                <SvgIcon name="phone" :size="16" /> 呼叫下一号
              </button>
              <div style="text-align:center">
                <div style="font-size:28px;font-weight:700;color:var(--blue)">{{ waitingCount }}</div>
                <div style="font-size:12px;color:var(--n-500)">等待中</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Department Exam Items Result Entry -->
        <div class="card">
          <div class="card-hd">
            <span><SvgIcon name="edit" :size="16" style="margin-right:6px;color:var(--green)" /> 检查结果录入 · {{ deptName }}</span>
            <span class="tag tag-green">专科医生</span>
          </div>
          <div class="card-bd">
            <div class="form-grid form-2">
              <div class="field">
                <label>选择体检记录</label>
                <select v-model="selectedExamRecordId" class="fselect">
                  <option value="" disabled>─ 选择患者 ─</option>
                  <option v-for="r in examRecords" :key="r.id" :value="String(r.id)">
                    {{ getPatientName(r) }} — {{ r.examNo }}
                  </option>
                </select>
              </div>
              <div class="field">
                <label>检查医生</label>
                <input value="李医生" class="finput" readonly />
              </div>
            </div>

            <!-- Items list for this department -->
            <div v-if="selectedExamRecordId" style="margin-top:12px">
              <!-- No items -->
              <div v-if="deptProgressItems.length === 0" class="empty-state" style="padding:20px 0">
                <SvgIcon name="clock" :size="32" style="color:var(--n-300);margin-bottom:8px" />
                <span class="empty-text">该患者套餐中不包含本科室项目</span>
                <div style="font-size:12px;color:var(--n-400);margin-top:4px">总进度 {{ progressItems.length }} 项，本科室 0 项</div>
              </div>

              <!-- Items with inputs -->
              <div v-for="item in deptProgressItems" :key="item.progressId"
                   class="exam-item" :class="{ 'is-conclusion': isConclusion(item), 'is-done': item.status === 'COMPLETED', 'is-gender-conflict': item.genderIssue }">
                <div class="exam-item-header">
                  <span class="exam-item-name">{{ item.itemName }}</span>
                  <span v-if="item.status === 'COMPLETED'" class="tag tag-green" style="font-size:11px">已完成</span>
                  <span v-else class="tag tag-orange" style="font-size:11px">待检查</span>
                </div>
                <div v-if="item.genderIssue" class="gender-warning">
                  <SvgIcon name="alert-triangle" :size="14" /> 患者性别与该项目不匹配，请确认
                </div>
                <div v-if="item.status !== 'COMPLETED'" class="exam-item-input" style="margin-top:6px">
                  <template v-if="isConclusion(item)">
                    <textarea v-model="itemValues[item.progressId]" class="ftextarea" rows="3"
                      :placeholder="conclusionPlaceholder(item)" style="font-size:13px;width:100%"></textarea>
                  </template>
                  <template v-else>
                    <input v-model="itemValues[item.progressId]" class="finput" style="width:100%"
                      :placeholder="'请输入' + item.itemName" />
                  </template>
                </div>
                <div v-else style="margin-top:4px;font-size:12px;color:var(--green-dark)">
                  ✓ 检查结果已录入
                </div>
              </div>
            </div>

            <!-- Department Conclusion -->
            <div class="divider" style="margin:16px 0"></div>
            <div class="section-label">本科室检查结论</div>
            <div class="field" style="margin-top:6px">
              <textarea v-model="departmentConclusion" class="ftextarea" rows="2"
                placeholder="综合本科室检查结果，给出单项结论。如：心肺听诊未见异常。"></textarea>
            </div>

            <div class="btn-group" style="margin-top:12px">
              <button class="btn btn-primary" :disabled="saving || !selectedExamRecordId" @click="handleSaveResults">
                <SvgIcon name="save" :size="16" /> 保存检查结果及结论
              </button>
            </div>
            <transition name="fade">
              <div v-if="msg" :class="['alert', msg.type]" style="margin-top:8px">{{ msg.text }}</div>
            </transition>
          </div>
        </div>
      </div>

      <!-- Queue list -->
      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="list" :size="16" style="margin-right:6px;color:var(--blue)" /> 排队列表</span>
          <span class="tag tag-orange">{{ waitingCount }}人</span>
        </div>
        <div class="card-bd pad-0">
          <template v-if="queueList.length">
            <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th class="col-nowrap">排队号</th><th class="col-nowrap">患者</th><th class="col-nowrap">状态</th></tr></thead>
              <tbody>
                <tr v-for="row in queueList" :key="row.id">
                  <td style="font-weight:700;font-size:16px;color:var(--n-800)" class="col-nowrap">{{ row.queueNo }}</td>
                  <td>{{ getPatientNameFromQueue(row) }}</td>
                  <td><span :class="['tag', statusClass(row.queueStatus)]">{{ statusLabel(row.queueStatus) }}</span></td>
                </tr>
              </tbody>
            </table>
            </div>
          </template>
          <div v-else class="empty-state">
            <SvgIcon name="list" :size="36" style="color:var(--n-300);margin-bottom:12px" />
            <span class="empty-text">队列为空</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 检前问卷信息 - 当患者选中时浮动显示 -->
    <div v-if="questionnaireData && selectedExamRecordId" class="questionnaire-sidebar">
      <div class="questionnaire-sidebar-header" @click="showQuestionnaire = !showQuestionnaire">
        <span>📋 检前问卷</span>
        <span style="font-size:11px;color:#64748b;">{{ showQuestionnaire ? '收起 ▲' : '展开 ▼' }}</span>
      </div>
      <div v-if="showQuestionnaire" class="questionnaire-sidebar-body">
        <!-- 过敏史 - 高亮 -->
        <div v-if="questionnaireData.allergicHistory && questionnaireData.allergicHistory !== '无' && questionnaireData.allergicHistory !== ''" class="qs-item qs-warning">
          <div class="qs-label">⚠️ 过敏史</div>
          <div class="qs-value">{{ questionnaireData.allergicHistory }}</div>
        </div>
        <div v-if="questionnaireData.pastMedicalHistory && questionnaireData.pastMedicalHistory !== '无'" class="qs-item">
          <div class="qs-label">🏥 既往病史</div>
          <div class="qs-value">{{ questionnaireData.pastMedicalHistory }}</div>
        </div>
        <div v-if="questionnaireData.medicationHistory && questionnaireData.medicationHistory !== '无'" class="qs-item">
          <div class="qs-label">💊 用药情况</div>
          <div class="qs-value">{{ questionnaireData.medicationHistory }}</div>
        </div>
        <div v-if="questionnaireData.familyDiseaseHistory && questionnaireData.familyDiseaseHistory !== '无'" class="qs-item">
          <div class="qs-label">👨‍👩‍👧 家族病史</div>
          <div class="qs-value">{{ questionnaireData.familyDiseaseHistory }}</div>
        </div>
        <div v-if="questionnaireData.surgicalHistory && questionnaireData.surgicalHistory !== '无'" class="qs-item">
          <div class="qs-label">🔪 手术史</div>
          <div class="qs-value">{{ questionnaireData.surgicalHistory }}</div>
        </div>
        <div class="qs-tags">
          <span v-if="questionnaireData.smokingHistory" class="qs-tag">
            🚬 {{ {NEVER:'不吸烟',FORMER:'已戒烟',CURRENT:'吸烟中'}[questionnaireData.smokingHistory] || questionnaireData.smokingHistory }}
          </span>
          <span v-if="questionnaireData.drinkingHistory" class="qs-tag">
            🍺 {{ {NEVER:'不饮酒',OCCASIONAL:'偶尔饮酒',REGULAR:'常饮酒'}[questionnaireData.drinkingHistory] || questionnaireData.drinkingHistory }}
          </span>
          <span v-if="questionnaireData.exerciseHabit" class="qs-tag">
            🏃 {{ {RARELY:'几乎不运动',OCCASIONAL:'偶尔运动',REGULAR:'规律运动',DAILY:'每天运动'}[questionnaireData.exerciseHabit] || questionnaireData.exerciseHabit }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { callNext, getQueue, getWaitingCount, getExamProgress, writeDepartmentConclusion, getCheckedIn, getPatients, getPackages, getQuestionnaireByAppointment } from '@/api'
import { recordNumericResult, recordTextResult, updateProgress } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { ElMessage } from 'element-plus'

const route = useRoute()
const clinic = useClinicStore()

// Department info
const deptId = computed(() => route.meta.departmentId || 3)
const deptName = computed(() => route.meta.title || '内科检查')

// State
const calling = ref(false)
const saving = ref(false)
const queueList = ref([])
const waitingCount = ref(0)
const questionnaireData = ref(null)
const showQuestionnaire = ref(false)
const examRecords = ref([])
const selectedExamRecordId = ref('')
const progressItems = ref([])
const itemValues = ref({})
const departmentConclusion = ref('')
const msg = ref(null)
const pollingActive = ref(false)
let pollTimer = null

// Computed: progress items filtered by current department
const deptProgressItems = computed(() => {
  const items = progressItems.value.filter(p => p.departmentId === deptId.value)
  return items.map(p => ({
    progressId: p.id,
    itemId: p.itemId,
    departmentId: p.departmentId,
    itemName: p.itemName || clinic.itemNames[p.itemId] || '未知项目(#' + p.itemId + ')',
    status: p.progressStatus,
    conclusion: p.departmentConclusion || null,
    genderIssue: checkGenderConflict(p.itemId, p.itemName || clinic.itemNames[p.itemId])
  }))
})

function checkGenderConflict(itemId, itemName) {
  const gender = patientGender.value
  if (!gender) return false
  if (gender === 'MALE' && (itemId === 25 || (itemName && itemName.includes('子宫')))) {
    return true
  }
  if (gender === 'FEMALE' && (itemId === 24 || (itemName && itemName.includes('前列腺')))) {
    return true
  }
  return false
}

const patientGender = computed(() => {
  if (!selectedExamRecordId.value) return null
  const record = clinic.examRecords[Number(selectedExamRecordId.value)]
  if (!record) return null
  return record.gender || null
})

function isConclusion(item) {
  // 文本结论类科室：影像科、内科、外科、眼科、耳鼻喉科、口腔科、超声科、心电图室
  const textDepts = [2, 3, 4, 5, 6, 7, 8, 9]
  return textDepts.includes(item.departmentId)
}
function conclusionPlaceholder(item) {
  const msgs = {
    2: '请输入影像检查所见及诊断意见', 3: '请输入内科检查结果及诊断意见',
    4: '请输入外科检查结果及诊断意见', 5: '请输入眼科检查结果',
    6: '请输入耳鼻喉检查结果及诊断意见', 7: '请输入口腔检查结果及诊断意见',
    8: '请输入超声检查所见及诊断意见', 9: '请输入心电图检查所见及诊断结果',
  }
  return msgs[item.departmentId] || '请输入' + item.itemName
}

function getPatientName(r) { return r.patientName || r.record?.patientName || clinic.examRecords[r.id || r.record?.id]?.patientName || '患者 ' + (r.userId || r.record?.userId || '?') }
function getPatientNameFromQueue(row) {
  return row.patientName || clinic.examRecords[row.examRecordId]?.patientName || '#' + row.examRecordId
}
function statusClass(s) { return { WAITING:'tag-orange', CALLED:'tag-green', ARRIVED:'tag-blue', COMPLETED:'tag-gray', SKIPPED:'tag-orange' }[s] || 'tag-gray' }
function statusLabel(s) { return { WAITING:'等待中', CALLED:'已呼叫', ARRIVED:'检查中', COMPLETED:'已完成', SKIPPED:'已跳过' }[s] || s }

// Load progress when patient is selected
watch(selectedExamRecordId, async (newVal) => {
  if (!newVal) { progressItems.value = []; return }
  try {
    const res = await getExamProgress(Number(newVal))
    progressItems.value = res?.data || []
    // Clear previous input values
    itemValues.value = {}
  } catch { progressItems.value = []; ElMessage.error('加载检查项目失败') }
})

async function loadQueueOnly() {
  try {
    const [q, w] = await Promise.all([
      getQueue(deptId.value),
      getWaitingCount(deptId.value)
    ])
    queueList.value = (q?.data || []).filter(entry => entry.queueStatus === 'WAITING')
    waitingCount.value = w?.data?.count || 0
  } catch { ElMessage.error('加载排队数据失败') }
}

async function loadAll() {
  try {
    const [checkedIn, patientsRes] = await Promise.all([
      getCheckedIn(1),
      getPatients()
    ])
    const patientMap = {}
    if (patientsRes?.data) {
      patientsRes.data.forEach(p => { patientMap[p.id] = p.name })
    }
    examRecords.value = (checkedIn?.data || []).map(r => {
      const name = r.patientName || patientMap[r.userId] || '#' + r.userId
      const enriched = { ...r, patientName: name }
      clinic.addExamRecord(enriched)
      return enriched
    })
    clinic.setCheckedInList(examRecords.value)
    await loadQueueOnly()
  } catch { ElMessage.error('加载数据失败') }
}

/** 兜底加载项目名称：从套餐接口获取 itemNames 并补充到 clinic.itemNames */
async function loadItemNames() {
  try {
    const res = await getPackages()
    if (res?.data?.length) {
      for (const pkg of res.data) {
        // try to get package items through the packages API format
        if (pkg.items) {
          pkg.items.forEach(item => {
            if (item.itemId && item.itemName && !clinic.itemNames[item.itemId]) {
              clinic.itemNames[item.itemId] = item.itemName
            }
          })
        }
      }
    }
  } catch { /* 兜底加载非关键，静默处理 */ }
}

function startPolling() {
  pollingActive.value = true
  pollTimer = setInterval(() => { loadQueueOnly() }, 5000)
}
function stopPolling() {
  pollingActive.value = false
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

async function handleCall() {
  calling.value = true
  try {
    const res = await callNext(deptId.value)
    if (res?.data) {
      ElMessage.success(`请 ${res.data.queueNo} 号到 ${deptName.value} 就诊`)
      if (!selectedExamRecordId.value) {
        selectedExamRecordId.value = String(res.data.examRecordId)
      }
    }
    await loadAll()
  } catch (e) {
    ElMessage.warning('暂无等待患者')
    await loadQueueOnly()
  }
  finally { calling.value = false }
}

async function handleSaveResults() {
  if (!selectedExamRecordId.value) { ElMessage.warning('请选择患者'); return }
  saving.value = true; msg.value = null
  const examRecordId = Number(selectedExamRecordId.value)
  let savedCount = 0

  try {
    // Save each item result
    for (const item of deptProgressItems.value) {
      const val = itemValues.value[item.progressId]
      if (!val || val === '' || item.status === 'COMPLETED') continue

      if (isConclusion(item)) {
        await recordTextResult({
          examRecordId, itemId: item.itemId, itemName: item.itemName,
          textValue: val, recorderId: 2
        })
        savedCount++
      } else {
        const num = parseFloat(val)
        if (!isNaN(num)) {
          await recordNumericResult({
            examRecordId, itemId: item.itemId, itemName: item.itemName,
            unit: '', numericValue: num,
            referenceMin: 0, referenceMax: 999, criticalMax: 0,
            recorderId: 2
          })
        } else {
          await recordTextResult({
            examRecordId, itemId: item.itemId, itemName: item.itemName,
            textValue: val, recorderId: 2
          })
        }
        savedCount++
      }
    }

    // Save department conclusion
    if (departmentConclusion.value) {
      const conclusionItems = progressItems.value.filter(
        p => p.departmentId === deptId.value && p.departmentConclusion == null
      )
      for (const p of conclusionItems) {
        await writeDepartmentConclusion(p.id, departmentConclusion.value)
      }
    }

    // Mark all progress items for this department as COMPLETED
    const deptProgress = progressItems.value.filter(p => p.departmentId === deptId.value)
    let progressUpdated = 0
    for (const p of deptProgress) {
      if (p.progressStatus !== 'COMPLETED') {
        try { await updateProgress(p.id, 'COMPLETE'); progressUpdated++ } catch { ElMessage.warning('部分进度更新失败') }
      }
    }

    if (savedCount > 0 || progressUpdated > 0) {
      ElMessage.success(`检查结果已保存（${savedCount}项），进度已完成（${progressUpdated}项）`)
      msg.value = { type: 'alert-success', text: `${deptName.value}检查完成，结果与进度已更新` }
      itemValues.value = {}
      departmentConclusion.value = ''
      // Reload progress to refresh status
      const res = await getExamProgress(examRecordId)
      progressItems.value = res?.data || []
    } else {
      ElMessage.warning('请至少填写一项检查结果')
    }
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message))
    msg.value = { type: 'alert-error', text: '保存失败，请重试' }
  } finally { saving.value = false }
}

onMounted(() => { loadAll(); loadItemNames(); startPolling() })
onUnmounted(() => { stopPolling() })
</script>

<style scoped>
.section-label { font-size: 12px; font-weight: 600; color: var(--n-500); letter-spacing: 0.5px; }
.exam-item {
  padding: 12px 14px; border-radius: 10px;
  background: var(--n-50); margin-bottom: 8px;
  border: 1px solid transparent;
}
.exam-item.is-conclusion { background: #fffbeb; border-color: #fde68a; }
.exam-item.is-done { background: #ecfdf5; border-color: #a7f3d0; }
.exam-item.is-gender-conflict { background: #fef2f2; border-color: #fca5a5; }
.gender-warning { display: flex; align-items: center; gap: 4px; margin-top: 6px; font-size: 12px; color: #dc2626; }
.exam-item-header { display: flex; justify-content: space-between; align-items: center; }
.exam-item-name { font-size: 14px; font-weight: 600; color: var(--n-700); }
.exam-item-input {}

/* 检前问卷侧边栏 */
.questionnaire-sidebar {
  position: fixed;
  right: 16px;
  top: 88px;
  width: 280px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
  border: 1px solid #e2e8f0;
  z-index: 100;
}
.questionnaire-sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #f0f9ff;
  border-bottom: 1px solid #bae6fd;
  border-radius: 12px 12px 0 0;
  font-weight: 600;
  font-size: 13px;
  color: #0369a1;
  cursor: pointer;
  user-select: none;
}
.questionnaire-sidebar-body {
  padding: 10px 14px;
}
.qs-item {
  padding: 6px 8px;
  margin-bottom: 6px;
  background: #f8fafc;
  border-radius: 6px;
}
.qs-item.qs-warning {
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.qs-label {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 1px;
}
.qs-item.qs-warning .qs-label {
  color: #dc2626;
}
.qs-value {
  font-size: 12px;
  color: #1e293b;
  line-height: 1.5;
}
.qs-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}
.qs-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #e2e8f0;
  border-radius: 9999px;
  color: #475569;
}
</style>
