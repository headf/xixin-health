<template>
  <div class="checkin-root">
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>患者签到</h2>
        <p class="desc">到院患者签到登记，启动体检流程</p>
      </div>
      <div class="page-hd-right" v-if="polling">
        <span class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 自动刷新中
        </span>
      </div>
    </div>

    <!-- 简约统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-num">{{ overview.totalAppointments }}</div>
        <div class="stat-label">今日预约总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ overview.waitingAppointments }}</div>
        <div class="stat-label">已支付待签到</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ overview.checkedInAppointments }}</div>
        <div class="stat-label">已签到</div>
      </div>
    </div>

    <!-- 签到分布 + 办理签到 两列布局 -->
    <div class="two-col" style="margin-bottom:20px">
      <div class="card">
        <div class="card-hd"><span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg> 签到分布</span></div>
        <div class="card-bd" style="padding:12px 24px;display:flex;align-items:center;justify-content:center">
          <div ref="donutChart" style="width:100%;height:200px" />
        </div>
      </div>
      <div class="card">
        <div class="card-hd">
          <span class="card-hd-title"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg> 办理签到</span>
          <span class="tag tag-blue">护士</span>
        </div>
        <div class="card-bd">
          <div class="form-grid form-2">
            <div class="field">
              <label>预约编号</label>
              <input v-model="form.appointmentId" class="finput" placeholder="选择患者后自动填入" readonly />
            </div>
            <div class="field">
              <label>患者姓名</label>
              <select v-model="selectedPatient" class="fselect" @change="onPatientChange">
                <option v-for="p in patients" :key="p.id" :value="p.id">{{ p.name }} ({{ maskPhone(p.phone) }}){{ p.hasPaid ? '' : ' ⏳未支付' }}</option>
              </select>
            </div>
            <div class="field">
              <label>健康档案</label>
              <select v-model="form.profileId" class="fselect" :disabled="!selectedPatient">
                <option value="">请先选择患者</option>
                <option v-for="p in patientProfileList" :key="p.id" :value="String(p.id)">
                  {{ p.profileName || '档案#' + p.id }}
                </option>
              </select>
            </div>
            <div class="field">
              <label>体检分院</label>
              <select v-model="form.branchId" class="fselect" disabled>
                <option :value="String(auth.currentBranchId || 1)">{{ auth.branchName || '熙心健康' }}</option>
              </select>
            </div>
            <div v-if="selectedPatient" class="field">
              <label>支付状态</label>
              <span :class="['tag', paymentStatus === 'PAID' ? 'tag-green' : 'tag-orange']">
                {{ paymentStatus === 'PAID' ? '已支付' : '未支付' }}
              </span>
            </div>
          </div>
          <!-- 检前问卷摘要 - 选中患者时显示 -->
          <div v-if="questionnaireData" class="questionnaire-summary" style="margin-top:12px;padding:12px 14px;background:#f0f9ff;border-radius:10px;border:1px solid #bae6fd;">
            <div class="questionnaire-header" style="display:flex;justify-content:space-between;align-items:center;cursor:pointer;user-select:none;" @click="showQuestionnaire = !showQuestionnaire">
              <span style="font-weight:600;font-size:13px;color:#0369a1;">📋 检前问卷摘要</span>
              <span :class="['tag', showQuestionnaire ? 'tag-blue' : 'tag-gray']" style="font-size:11px;padding:1px 8px;">
                {{ showQuestionnaire ? '收起' : '展开' }}
              </span>
            </div>
            <template v-if="showQuestionnaire">
              <!-- 过敏史（高亮） -->
              <div v-if="questionnaireData.allergicHistory && questionnaireData.allergicHistory !== '无' && questionnaireData.allergicHistory !== ''" style="margin-top:8px;padding:8px 10px;background:#fef2f2;border-radius:6px;border:1px solid #fecaca;">
                <span style="font-size:12px;font-weight:600;color:#dc2626;">⚠️ 过敏史</span>
                <p style="font-size:12px;color:#991b1b;margin:2px 0 0;">{{ questionnaireData.allergicHistory }}</p>
              </div>
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:8px;">
                <div v-if="questionnaireData.pastMedicalHistory && questionnaireData.pastMedicalHistory !== '无'" style="padding:6px 8px;background:#fff;border-radius:6px;">
                  <span style="font-size:11px;font-weight:600;color:#475569;">既往病史</span>
                  <p style="font-size:11px;color:#1e293b;margin:2px 0 0;">{{ questionnaireData.pastMedicalHistory }}</p>
                </div>
                <div v-if="questionnaireData.medicationHistory && questionnaireData.medicationHistory !== '无'" style="padding:6px 8px;background:#fff;border-radius:6px;">
                  <span style="font-size:11px;font-weight:600;color:#475569;">用药情况</span>
                  <p style="font-size:11px;color:#1e293b;margin:2px 0 0;">{{ questionnaireData.medicationHistory }}</p>
                </div>
              </div>
              <div style="display:flex;flex-wrap:wrap;gap:4px;margin-top:6px;">
                <span v-if="questionnaireData.smokingHistory" style="font-size:11px;padding:2px 8px;background:#e2e8f0;border-radius:9999px;color:#475569;">
                  🚬 {{ {NEVER:'不吸烟',FORMER:'已戒烟',CURRENT:'吸烟中'}[questionnaireData.smokingHistory] || questionnaireData.smokingHistory }}
                </span>
                <span v-if="questionnaireData.drinkingHistory" style="font-size:11px;padding:2px 8px;background:#e2e8f0;border-radius:9999px;color:#475569;">
                  🍺 {{ {NEVER:'不饮酒',OCCASIONAL:'偶尔饮酒',REGULAR:'常饮酒'}[questionnaireData.drinkingHistory] || questionnaireData.drinkingHistory }}
                </span>
                <span v-if="questionnaireData.exerciseHabit" style="font-size:11px;padding:2px 8px;background:#e2e8f0;border-radius:9999px;color:#475569;">
                  🏃 {{ {RARELY:'几乎不运动',OCCASIONAL:'偶尔运动',REGULAR:'规律运动',DAILY:'每天运动'}[questionnaireData.exerciseHabit] || questionnaireData.exerciseHabit }}
                </span>
              </div>
            </template>
          </div>
          <div class="btn-group">
            <button class="btn btn-primary" :disabled="loading" @click="handleCheckin">
              <SvgIcon name="check" :size="16" /> 确认签到
            </button>
            <button class="btn btn-secondary btn-sm" @click="loadCheckedIn">
              <SvgIcon name="refresh" :size="14" /> 刷新列表
            </button>
          </div>
          <transition name="fade">
            <div v-if="result" class="alert alert-success">
              <div class="alert-cell">
                <SvgIcon name="check" :size="15" />
                <div>
                  <span> 签到成功 · <strong>{{ result.patientName }}</strong> · 体检编号 <strong>{{ result.examNo }}</strong></span>
                  <div v-if="result.packageName" class="alert-detail">套餐：{{ result.packageName }}</div>
                  <el-button v-if="showPrintBtn" size="small" type="primary" style="margin-top:8px" @click="handlePrintGuide">
                    <el-icon><Printer /></el-icon> 打印导检单
                  </el-button>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- 今日已签到 — 全宽 -->
    <div class="card" style="margin-bottom:20px">
      <div class="card-hd">
        <span class="card-hd-title"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg> 今日已签到</span>
        <span class="tag tag-green">{{ checkedInList.length }}人</span>
      </div>
      <div class="card-bd pad-0">
        <div class="table-wrap" v-if="checkedInList.length">
          <table class="tbl">
            <thead>
              <tr><th>体检编号</th><th>患者</th><th class="col-package">套餐</th><th>状态</th><th class="col-time">签到时间</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="row in checkedInList" :key="row.id">
                <td class="td-exam-no">{{ row.examNo }}</td>
                <td>{{ clinic.examRecords[row.id]?.patientName || '-' }}</td>
                <td class="td-package">{{ row.packageName || '基础套餐' }}</td>
                <td><span class="tag tag-green">已签到</span></td>
                <td class="td-time">{{ formatTime(row.checkedInAt) }}</td>
                <td>
                  <el-button size="small" type="primary" @click="handleAddItem(row)">加项</el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state">
          <SvgIcon name="clipboard" :size="36" style="color:var(--text-secondary);margin-bottom:12px" />
          <span class="empty-text">暂无签到记录</span>
          <button class="btn btn-secondary btn-sm" style="margin-top:8px" @click="loadCheckedIn"><SvgIcon name="refresh" :size="14" /> 刷新</button>
        </div>
      </div>
    </div>

    <div class="card" style="margin-top:20px">
      <div class="card-hd"><span class="card-hd-title"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--text-secondary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg> 护士操作指引</span></div>
      <div class="card-bd">
        <div class="steps">
          <div class="step-item"><SvgIcon name="clipboard" :size="14" /> 签到登记</div>
          <span class="step-arrow"><SvgIcon name="arrow-right" :size="14" /></span>
          <div class="step-item"><SvgIcon name="heart" :size="14" /> 体征测量</div>
          <span class="step-arrow"><SvgIcon name="arrow-right" :size="14" /></span>
          <div class="step-item"><SvgIcon name="phone" :size="14" /> 排队叫号</div>
          <span class="step-arrow"><SvgIcon name="arrow-right" :size="14" /></span>
          <div class="step-item"><SvgIcon name="clock" :size="14" /> 初始化进度</div>
        </div>
      </div>
    </div>
  </div>

  <!-- 加项弹窗 -->
  <el-dialog v-model="addonDialogVisible" title="套餐加项" width="500px" :close-on-click-modal="false">
    <el-form label-width="100px">
      <el-form-item label="体检编号">
        <el-input :model-value="addonForm.examNo" disabled />
      </el-form-item>
      <el-form-item label="患者姓名">
        <el-input :model-value="addonForm.patientName" disabled />
      </el-form-item>
      <el-form-item label="加项项目" required>
        <el-select v-model="addonForm.itemId" placeholder="请选择加项项目" style="width:100%" filterable>
          <el-option
            v-for="item in addonItems"
            :key="item.id"
            :label="item.itemName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="金额(元)">
        <el-input-number v-model="addonForm.amountYuan" :min="0" :precision="2" style="width:100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="addonDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="addonSubmitting" @click="submitAddon">确认加项</el-button>
    </template>
  </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Printer } from '@element-plus/icons-vue'
import { checkIn, getCheckedIn, getPatients, getProfilesByUser, getUserAppointments, createAddon, getExamItems, getQuestionnaireByAppointment } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const clinic = useClinicStore()
const auth = useAuthStore()
const form = reactive({ appointmentId: '', userId: '', profileId: '', branchId: String(auth.currentBranchId || 1) })
const loading = ref(false); const result = ref(null); const checkedInList = ref([])
const showPrintBtn = ref(false)
const patients = ref([])
const selectedPatient = ref(null)
const paymentStatus = ref('')
const questionnaireData = ref(null)
const showQuestionnaire = ref(false)
const patientProfiles = ref({})
const patientProfileList = ref([])
const donutChart = ref(null)
let donutInstance = null

const polling = ref(false)
let pollTimer = null

// 加项相关
const addonDialogVisible = ref(false)
const addonSubmitting = ref(false)
const addonItems = ref([])
const addonForm = reactive({
  examRecordId: null,
  examNo: '',
  patientName: '',
  itemId: null,
  amountYuan: 0
})

function startPolling() {
  polling.value = true
  pollTimer = setInterval(() => { loadCheckedIn(); loadPatients() }, 5000)
}

function stopPolling() {
  polling.value = false
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

const todayStr = new Date().toISOString().slice(0, 10)
const overview = computed(() => {
  const waiting = patients.value.length            // 办理签到列表 = 已支付未签到
  const checkedIn = checkedInList.value.length      // 今日已签到
  return {
    totalAppointments: waiting + checkedIn,         // 今日预约总数 = 待签到 + 已签到
    waitingAppointments: waiting,                    // 已支付待签到
    checkedInAppointments: checkedIn                // 已签到
  }
})

function formatTime(t) { return t?.replace('T', ' ') || '-' }
function maskPhone(p) { return p ? p.slice(0,3) + '****' + p.slice(-3) : '' }

function initCharts() {
  setTimeout(() => {
    if (donutChart.value && !donutInstance) {
      donutInstance = echarts.init(donutChart.value)
    }
    if (donutInstance) {
      donutInstance.setOption({
        tooltip: { trigger: 'item', backgroundColor: '#fff', borderWidth: 0, borderRadius: 12 },
        series: [{
          type: 'pie', radius: ['50%','75%'], center: ['50%','50%'],
          label: { show: false },
          data: [
            { value: overview.value.checkedInAppointments, name: '已签到', itemStyle: { color: '#00B894' } },
            { value: overview.value.waitingAppointments, name: '待签到', itemStyle: { color: '#f97316' } }
          ]
        }]
      })
    }
  }, 200)
}


watch([() => overview.value.checkedInAppointments, () => overview.value.waitingAppointments], () => {
  if (donutInstance) {
    donutInstance.setOption({ series: [{ data: [
      { value: Math.max(overview.value.checkedInAppointments, 1), name: '已签到', itemStyle: { color: '#0ea5e9' } },
      { value: overview.value.waitingAppointments, name: '待签到', itemStyle: { color: '#f97316' } }
    ]}] })
  }
}, { immediate: true })

async function onPatientChange() {
  if (selectedPatient.value) {
    form.userId = String(selectedPatient.value)
    // 从后端查询该用户的预约ID
    try {
      const res = await getUserAppointments(selectedPatient.value)
      if (res?.data?.length) {
        form.appointmentId = String(res.data[0].id)
        paymentStatus.value = res.data[0].status || ''
        // 加载检前问卷
        try {
          const q = await getQuestionnaireByAppointment(res.data[0].id)
          questionnaireData.value = q?.data || null
        } catch {
          questionnaireData.value = null
        }
      }
    } catch {
      form.appointmentId = ''
      form.profileId = ''
      paymentStatus.value = ''
      ElMessage.error('查询预约信息失败')
    }
    if (patientProfiles.value[selectedPatient.value]) {
      patientProfileList.value = patientProfiles.value[selectedPatient.value]
      form.profileId = patientProfileList.value.length ? String(patientProfileList.value[0].id) : ''
    } else {
      try {
        const res = await getProfilesByUser(selectedPatient.value)
        if (res?.data?.length) {
          patientProfiles.value[selectedPatient.value] = res.data
          patientProfileList.value = res.data
          form.profileId = String(res.data[0].id)
        } else {
          patientProfileList.value = []
          form.profileId = ''
        }
      } catch {
        patientProfileList.value = []
        form.profileId = ''
        ElMessage.error('查询档案信息失败')
      }
    }
  }
}

async function handleCheckin() {
  if (!form.userId || !form.appointmentId || Number(form.appointmentId) === 0) { ElMessage.warning('请选择患者并输入预约编号'); return }
  if (checkedInList.value.some(r => r.appointmentId === Number(form.appointmentId))) {
    ElMessage.warning('该预约已签到，不能重复签到'); return
  }
  // 缴费确认弹窗
  if (paymentStatus.value !== 'PAID') {
    try {
      await ElMessageBox.confirm('该预约尚未支付，确认要签到吗？', '缴费确认', { confirmButtonText: '确认签到', cancelButtonText: '先缴费', type: 'warning' })
    } catch { return }
  }
  loading.value = true; result.value = null
  try {
    const patient = patients.value.find(p => p.id === Number(form.userId))
    const res = await checkIn({ appointmentId: Number(form.appointmentId), userId: Number(form.userId), profileId: Number(form.profileId), branchId: Number(form.branchId) })
    if (res?.data?.id) { const data = { ...res.data, patientName: patient?.name || '未知' }; result.value = data; showPrintBtn.value = true; clinic.addExamRecord(data); ElMessage.success(`签到成功 · ${data.patientName} · 编号: ${data.examNo}`) }
    await loadCheckedIn()
  } catch { ElMessage.error('签到失败，请确认预约已支付') } finally { loading.value = false }
}

let guideDepartures = []
let guideBranchName = ''

async function loadGuideData() {
  try {
    const [deptRes, branchRes] = await Promise.all([
      fetch('/api/v1/departments', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('xixin_staff_token') } }).then(r => r.json()),
      fetch('/api/v1/branches', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('xixin_staff_token') } }).then(r => r.json())
    ])
    guideDepartures = deptRes.data || []
    const branches = branchRes.data || []
    if (branches.length > 0) guideBranchName = branches[0].branchName || '中心分院'
  } catch {}
}

async function handlePrintGuide() {
  if (!result.value) return
  await loadGuideData()
  const r = result.value

  // 按分院分组科室
  const fastingDepts = guideDepartures.filter(d => d.fastingRequired)
  const normalDepts = guideDepartures.filter(d => !d.fastingRequired)
  const routeDepts = [...fastingDepts, ...normalDepts].slice(0, 12)

  const printWindow = window.open('', '_blank', 'width=800,height=600')
  printWindow.document.write(`
    <html><head><title>导检单</title>
    <style>
      body { font-family: "Microsoft YaHei", "PingFang SC", sans-serif; padding: 40px; color: #333; }
      h2 { text-align: center; color: #00B894; font-size: 22px; margin-bottom: 6px; }
      .sub-title { text-align: center; color: #86909C; font-size: 13px; margin-bottom: 24px; }
      .info-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
      .info-table td { padding: 8px 12px; border: 1px solid #E5E6EB; font-size: 14px; }
      .info-table td:first-child { background: #F5F7FA; font-weight: 600; width: 110px; color: #4E5969; }
      h3 { font-size: 15px; color: #00B894; margin: 20px 0 10px; padding-bottom: 6px; border-bottom: 2px solid #00B894; }
      .route-table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
      .route-table th { background: #00B894; color: #fff; padding: 8px 12px; font-size: 13px; text-align: left; }
      .route-table td { padding: 10px 12px; border: 1px solid #E5E6EB; font-size: 13px; }
      .route-table tr:nth-child(even) td { background: #F5F7FA; }
      .step-num { display: inline-block; width: 24px; height: 24px; border-radius: 50%; background: #00B894; color: #fff; text-align: center; line-height: 24px; font-size: 12px; font-weight: 700; margin-right: 6px; }
      .tag-fasting { display: inline-block; background: #fef3c7; color: #b45309; padding: 1px 8px; border-radius: 9999px; font-size: 11px; }
      .tag-normal { display: inline-block; background: #E8F8F5; color: #00B894; padding: 1px 8px; border-radius: 9999px; font-size: 11px; }
      .notice { background: #F0FDF4; border-left: 4px solid #00B894; padding: 12px 16px; border-radius: 4px; font-size: 13px; color: #334155; margin-top: 20px; }
      .notice p { margin: 4px 0; }
      .footer { text-align: center; color: #94a3b8; font-size: 12px; margin-top: 32px; border-top: 1px solid #e2e8f0; padding-top: 16px; }
    </style>
    </head><body>
    <h2>🏥 熙心健康体检导检单</h2>
    <div class="sub-title">请按以下路线指引逐项完成检查</div>
    <table class="info-table">
      <tr><td>体检编号</td><td>${r.examNo || '-'}</td></tr>
      <tr><td>患者姓名</td><td>${r.patientName || '-'}</td></tr>
      <tr><td>体检套餐</td><td>${r.packageName || '基础套餐'}</td></tr>
      <tr><td>签到时间</td><td>${new Date().toLocaleString('zh-CN')}</td></tr>
      <tr><td>体检分院</td><td>${guideBranchName}</td></tr>
    </table>

    <h3>📋 检查路线</h3>
    <table class="route-table">
      <thead><tr><th style="width:50px">序号</th><th>科室名称</th><th style="width:100px">位置</th><th style="width:90px">状态</th></tr></thead>
      <tbody>
        ${routeDepts.map((d, i) => `
          <tr>
            <td><span class="step-num">${i + 1}</span></td>
            <td><strong>${d.name || d.departmentName || '-'}</strong></td>
            <td>${d.location || '院内'}</td>
            <td>${d.fastingRequired ? '<span class="tag-fasting">需空腹</span>' : '<span class="tag-normal">无需空腹</span>'}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>

    <div class="notice">
      <p><strong>温馨提示：</strong></p>
      <p>🔴 <strong>需空腹项目</strong>（标"需空腹"的科室）请优先完成，完成后可进食</p>
      <p>📱 请携带身份证以便核验身份</p>
      <p>👕 建议穿着宽松衣物，避免佩戴金属饰品</p>
      <p>✅ 所有项目完成后请到前台确认收尾</p>
    </div>
    <div class="footer">
      <p>熙心健康体检中心 祝您身体健康</p>
      <p>${guideBranchName} · 服务电话：400-100-0001</p>
    </div>
    </body></html>
  `)
  printWindow.document.close()
  printWindow.focus()
  setTimeout(() => { printWindow.print() }, 300)
}

function handleAddItem(row) {
  addonForm.examRecordId = row.id
  addonForm.examNo = row.examNo || ''
  addonForm.patientName = clinic.examRecords[row.id]?.patientName || ''
  addonForm.itemId = null
  addonForm.amountYuan = 0
  addonDialogVisible.value = true
  loadAddonItems()
}

async function loadAddonItems() {
  try {
    const res = await getExamItems()
    addonItems.value = res?.data || []
  } catch {
    ElMessage.error('加载项目列表失败')
  }
}

async function submitAddon() {
  if (!addonForm.itemId) {
    ElMessage.warning('请选择加项项目')
    return
  }
  addonSubmitting.value = true
  try {
    await createAddon({
      examRecordId: addonForm.examRecordId,
      itemId: addonForm.itemId,
      amountCent: Math.round(addonForm.amountYuan * 100)
    })
    ElMessage.success('加项成功')
    addonDialogVisible.value = false
  } catch {
    ElMessage.error('加项失败')
  } finally {
    addonSubmitting.value = false
  }
}

async function loadCheckedIn() {
  try {
    const res = await getCheckedIn(auth.currentBranchId || 1)
    checkedInList.value = res?.data || []
    clinic.setCheckedInList(checkedInList.value)
    ;(checkedInList.value).forEach(r => clinic.addExamRecord(r))
  } catch { ElMessage.error('加载已签到列表失败') }
}

async function loadPatients() {
  try {
    const pts = await getPatients({ branchId: auth.currentBranchId })
    if (pts?.data?.length) {
      // 同一用户的多个就诊人去重
      const seen = new Set()
      patients.value = pts.data.filter(p => {
        const key = p.id + '-' + p.name
        if (seen.has(key)) return false
        seen.add(key)
        return true
      })
      if (!selectedPatient.value && patients.value.length > 0) {
        selectedPatient.value = patients.value[0].id
        form.userId = String(patients.value[0].id)
        await onPatientChange()
      }
    } else {
      patients.value = []
    }
  } catch { ElMessage.error('加载患者列表失败') }
}


onMounted(() => {
  loadCheckedIn()
  loadPatients()
  initCharts()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
  donutInstance?.dispose()
})
</script>

<style scoped>
.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: #fff; border-radius: 16px; padding: 20px 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
  text-align: center; border: 1px solid #f1f5f9;
}
.stat-card .stat-num { font-size: 28px; font-weight: 700; color: var(--title); }
.stat-card .stat-label { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }

.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.td-exam-no { font-weight: 600; color: var(--title); }
.td-package { font-size: 12px; color: var(--text-secondary); }
.td-time { color: var(--text-secondary); font-size: 13px; }

.steps { display: flex; align-items: center; flex-wrap: nowrap; gap: 0; }
.step-item { display: inline-flex; align-items: center; gap: 8px; padding: 6px 12px; border-radius: 12px; white-space: nowrap; }
.step-item:hover { background: #f1f5f9; }
.step-arrow { display: inline-flex; align-items: center; color: #cbd5e1; font-size: 14px; margin: 0 4px; user-select: none; }
.alert-cell { display: flex; align-items: flex-start; gap: 4px; }
.alert-detail { font-size: 12px; margin-top: 2px; }
.btn-group { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
:deep(table.tbl) .col-package { min-width: 100px; }
:deep(table.tbl) .col-time { min-width: 120px; }

.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100% { opacity:1; transform:scale(1) } 50% { opacity:0.5; transform:scale(0.8) } }

@media (max-width: 1000px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .two-col { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .stats-row { grid-template-columns: 1fr; }
}
</style>
