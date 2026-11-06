<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>报告管理</h2>
        <p class="desc">生成体检报告、填写诊断建议、终审签发</p>
      </div>
      <div class="page-hd-right" v-if="polling">
        <span class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 自动刷新中
        </span>
      </div>
    </div>

    <div class="two-col">
      <div style="display:flex;flex-direction:column;gap:16px">
        <div class="card">
          <div class="card-hd"><span><SvgIcon name="document" :size="16" style="margin-right:6px;color:var(--orange)" /> 生成体检报告</span><span class="tag tag-orange">审核医生</span></div>
          <div class="card-bd">
            <div class="form-grid form-2">
              <div class="field"><label>体检记录</label>
                <select v-model="gen.examRecordId" class="fselect">
                  <option value="" disabled>─ 请选择已签到患者 ─</option>
                  <option v-for="r in checkedInList" :key="r.id" :value="r.id">
                    {{ getPatientName(r) }} — {{ r.examNo }}
                  </option>
                </select>
              </div>
              <div class="field"><label>审核医生</label><input value="张主任 (ID:3)" class="finput" readonly /></div>
            </div>
            <button class="btn btn-primary" :disabled="genLoading" @click="handleGenerate"><SvgIcon name="document" :size="16" /> 生成报告</button>
            <transition name="fade">
              <div v-if="reportId" class="alert alert-success" style="margin-top:4px"><SvgIcon name="check" :size="14" style="margin-right:4px" /> 报告已生成 · 编号: <strong>{{ reportId }}</strong></div>
            </transition>
          </div>
        </div>

        <div class="card">
          <div class="card-hd"><span><SvgIcon name="edit" :size="16" style="margin-right:6px;color:var(--blue)" /> 诊断总结 & 签发</span></div>
          <div class="card-bd">
            <div class="field"><label>报告编号</label><input v-model="diag.reportId" class="finput" :placeholder="reportId ? `报告ID: ${reportId}` : '先生成报告'" /></div>
            <div class="field"><label>总结摘要</label><textarea v-model="diag.summary" class="ftextarea" rows="3"></textarea></div>
            <div class="field"><label>医生建议</label><textarea v-model="diag.advice" class="ftextarea" rows="3"></textarea></div>
            <div class="btn-group">
              <button class="btn btn-primary" :disabled="diagLoading" @click="handleDiagnosis"><SvgIcon name="save" :size="16" /> 保存诊断建议</button>
              <button class="btn btn-blue" :disabled="pubLoading" @click="handlePublish"><SvgIcon name="send" :size="16" /> 终审签发</button>
            </div>
            <transition name="fade">
              <div v-if="pubDone" class="alert alert-success" style="margin-top:4px"><SvgIcon name="check" :size="14" style="margin-right:4px" /> 报告已终审签发</div>
            </transition>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-hd"><span><SvgIcon name="search" :size="16" style="margin-right:6px;color:var(--blue)" /> 报告查询</span></div>
        <div class="card-bd">
          <div class="form-grid form-2">
            <div class="field"><label>报告ID</label><input v-model="q.reportId" class="finput" placeholder="输入报告ID" /></div>
            <div class="field"><label>患者姓名</label><input v-model="q.userId" class="finput" placeholder="输入用户ID或姓名" /></div>
          </div>
          <div class="btn-group">
            <button class="btn btn-secondary" @click="handleQuery"><SvgIcon name="search" :size="14" /> 查询</button>
            <button class="btn btn-secondary btn-sm" @click="handleUserReports"><SvgIcon name="user" :size="14" /> 按患者查询</button>
          </div>

          <template v-if="reportData">
            <div class="divider" style="margin:16px 0"></div>
            <div v-if="!Array.isArray(reportData)" style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
              <div class="detail-item" style="grid-column:1/-1"><span class="label">患者姓名</span><span class="value" style="font-size:18px">{{ reportData.patientName || reportData.userName || reportData.user?.name || reportData.user?.displayName || '#' + (reportData.userId || reportData.user_id) }}</span></div>
              <div class="detail-item"><span class="label">报告编号</span><span class="value">{{ reportData.reportNo }}</span></div>
              <div class="detail-item"><span class="label">状态</span><span :class="['tag', reportData.reportStatus==='PUBLISHED'?'tag-green':'tag-orange']">{{ {DRAFT:'草稿',CONFIRMED:'已确认',PUBLISHED:'已签发'}[reportData.reportStatus] || reportData.reportStatus }}</span></div>
              <div class="detail-item" style="grid-column:1/-1"><span class="label">异常等级</span><span :class="['tag', reportData.abnormalStatus==='CRITICAL'?'tag-red':reportData.abnormalStatus==='ABNORMAL'?'tag-orange':'tag-green']">{{ abnormalLabel(reportData.abnormalStatus) }}</span></div>
              <div class="detail-item" style="grid-column:1/-1"><span class="label">生成时间</span><span class="value">{{ reportData.generatedAt?.replace('T',' ') }}</span></div>
            </div>
            <div style="margin-top:16px;text-align:right">
              <el-button size="small" type="warning" @click="handleAppeal" :disabled="!reportData || reportData.reportStatus !== 'PUBLISHED'">申诉</el-button>
              <el-button size="small" @click="loadHistory" :disabled="!reportData">历史对比</el-button>
              <el-button size="small" @click="handleDownloadPdf" :disabled="!reportData">
                <el-icon><Download /></el-icon> 下载PDF
              </el-button>
              <el-button type="primary" @click="handlePrintReport" :disabled="!reportData">
                <el-icon><Printer /></el-icon> 打印报告
              </el-button>
            </div>

            <!-- 历史对比结果 -->
            <template v-if="historyData.length">
              <div class="divider" style="margin:16px 0"></div>
              <div style="margin-top:12px">
                <h4 style="margin-bottom:8px;font-size:14px;color:var(--n-600)">历史记录对比</h4>
                <div v-for="h in historyData" :key="h.id" style="padding:10px;margin-bottom:8px;border:1px solid var(--n-200);border-radius:6px;background:#f9fafb">
                  <div style="display:flex;justify-content:space-between;font-size:12px;color:var(--n-500);margin-bottom:6px">
                    <span>报告编号: {{ h.reportNo }}</span>
                    <span>{{ h.generatedAt?.replace('T', ' ') }}</span>
                  </div>
                  <div v-if="h.summary" style="font-size:13px;color:var(--n-700);margin-bottom:4px"><strong>总结:</strong> {{ h.summary }}</div>
                  <div v-if="h.doctorAdvice" style="font-size:13px;color:var(--n-700)"><strong>建议:</strong> {{ h.doctorAdvice }}</div>
                </div>
              </div>
            </template>
            <div v-if="Array.isArray(reportData)">
              <div class="table-wrap" v-if="reportData.length">
                <table class="tbl"><thead><tr><th class="col-nowrap">患者姓名</th><th class="col-nowrap">报告编号</th><th class="col-nowrap">状态</th><th class="col-nowrap">生成时间</th></tr></thead>
                  <tbody><tr v-for="r in reportData" :key="r.id"><td><strong>{{ r.patientName || r.userName || r.user?.name || r.user?.displayName || '#' + (r.userId || r.user_id) }}</strong></td><td>{{ r.reportNo || r.id }}</td><td><span :class="['tag', r.reportStatus==='PUBLISHED'?'tag-green':'tag-orange']">{{ r.reportStatus }}</span></td><td style="font-size:12px;color:var(--n-400)" class="col-nowrap">{{ r.generatedAt?.replace('T',' ') }}</td></tr></tbody>
                </table>
              </div>
              <div v-if="reportData.length === 0" class="empty-state"><SvgIcon name="document" :size="36" style="color:var(--n-300);margin-bottom:12px" /><span class="empty-text">该患者暂无报告</span></div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { generateReport, writeDiagnosis, publishReport, getReport, getReportsByUser, getCompletedRecords, getPatients } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Printer, Download } from '@element-plus/icons-vue'

const clinic = useClinicStore()
const auth = useAuthStore()
const gen = reactive({ examRecordId:'', reviewerId:'3' })
const checkedInList = ref([])
const patientNameMap = ref({})
const getPatientName = (r) => r.patientName || r.userName || clinic.examRecords[r.id]?.patientName || patientNameMap.value[r.userId || r.user_id] || '患者#' + (r.userId || r.user_id || r.id)
const q = reactive({ reportId:'', userId:'' })
const diag = reactive({ reportId:'', summary:'受检者各项指标基本正常。ALT轻度偏高，可能与饮食或疲劳有关，建议复查。', advice:'1. 注意饮食清淡，避免高脂油腻食物；\n2. 规律作息，避免熬夜；\n3. 建议3个月后复查肝功能。' })
const genLoading = ref(false); const diagLoading = ref(false); const pubLoading = ref(false)
const reportId = ref(null); const reportData = ref(null); const pubDone = ref(false)

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

const abnormalLabel = (l) => clinic.abnormalLabels[l] || l

async function loadReport() {
  if (!reportData.value?.id) return
  try { const res = await getReport(reportData.value.id); reportData.value = res?.data || null } catch {}
}
async function handleGenerate() { genLoading.value=true; reportId.value=null; try { const res=await generateReport({ examRecordId:Number(gen.examRecordId), reviewerId:Number(gen.reviewerId) }); if(res?.data?.id) { reportId.value=res.data.id; diag.reportId=String(res.data.id); clinic.addReport(res.data); ElMessage.success('体检报告已生成') } } catch { ElMessage.error('报告生成失败，请确认所有结果已审核通过') } finally { genLoading.value=false } }
async function handleDiagnosis() { if(!diag.reportId) return ElMessage.warning('请先生成报告'); diagLoading.value=true; try { await writeDiagnosis(Number(diag.reportId), { summary:diag.summary, doctorAdvice:diag.advice }); ElMessage.success('诊断建议已保存') } catch {} finally { diagLoading.value=false } }
async function handlePublish() { if(!diag.reportId) return ElMessage.warning('请先生成报告'); pubLoading.value=true; pubDone.value=false; try { await publishReport(Number(diag.reportId), { summary:diag.summary, doctorAdvice:diag.advice }); pubDone.value=true; ElMessage.success('报告已终审签发！') } catch {} finally { pubLoading.value=false } }
async function handleQuery() { if(!q.reportId) return ElMessage.warning('请输入报告ID'); try { const res=await getReport(Number(q.reportId)); reportData.value=res?.data||null } catch { reportData.value=null } }
async function handleUserReports() { if(!q.userId) return ElMessage.warning('请输入用户ID'); try { const res=await getReportsByUser(Number(q.userId)); reportData.value=res?.data||[] } catch {} }

function handlePrintReport() {
  if (!reportData.value) return
  const w = window.open('', '_blank', 'width=800,height=600')
  w.document.write(`
    <html><head><title>体检报告 - ${reportData.value.reportNo}</title>
    <style>
      body{font-family:微软雅黑;padding:30px;color:#333}
      h1{text-align:center;color:#00B894;font-size:22px}
      .section{margin:20px 0;padding:15px;border:1px solid #E5E6EB;border-radius:8px}
      .label{color:#86909C;font-size:12px}
      .value{font-size:16px;font-weight:600;color:#1D2129}
    </style></head><body>
    <h1>熙心健康体检报告</h1>
    <div class="section">
      <p><span class="label">报告编号：</span><span class="value">${reportData.value.reportNo}</span></p>
      <p><span class="label">检查日期：</span><span class="value">${reportData.value.generatedAt || ''}</span></p>
      ${reportData.value.summary ? '<h3>总结</h3><p>' + reportData.value.summary + '</p>' : ''}
      ${reportData.value.doctorAdvice ? '<h3>医生建议</h3><p>' + reportData.value.doctorAdvice + '</p>' : ''}
    </div>
    <p style="text-align:center;color:#94a3b8;font-size:12px;margin-top:40px">熙心健康体检管理系统</p>
    </body></html>
  `)
  w.document.close()
  w.print()
}

async function handleDownloadPdf() {
  if (!reportData.value) return
  const w = window.open('', '_blank')
  w.document.write('<html><head><title>报告-' + reportData.value.reportNo + '</title>')
  w.document.write('<style>body{font-family:微软雅黑;padding:30px;color:#333}h1{text-align:center;color:#0ea5e9}.section{margin:20px 0;padding:15px;border:1px solid #e2e8f0;border-radius:8px}.label{color:#94a3b8;font-size:12px}.value{font-size:16px;font-weight:600}</style></head><body>')
  w.document.write('<h1>熙心健康体检报告</h1>')
  w.document.write('<p>报告编号: ' + reportData.value.reportNo + '</p>')
  w.document.write('<p>生成时间: ' + (reportData.value.generatedAt || '').replace('T',' ') + '</p>')
  if (reportData.value.summary) w.document.write('<div class=section><h3>总结</h3><p>' + reportData.value.summary + '</p></div>')
  if (reportData.value.doctorAdvice) w.document.write('<div class=section><h3>医生建议</h3><p>' + reportData.value.doctorAdvice + '</p></div>')
  w.document.write('</body></html>')
  w.document.close()
  w.print()
}

async function handleAppeal() {
  try {
    const { value } = await ElMessageBox.prompt('请输入申诉原因', '报告申诉', { inputType: 'textarea', inputPlaceholder: '请详细描述需要修改的内容...' })
    await fetch('/api/v1/reports/' + reportData.value.id + '/appeal', { method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('xixin_staff_token'), 'Content-Type': 'application/json' }, body: JSON.stringify({ reason: value }) })
    ElMessage.success('申诉已提交')
    await loadReport()
  } catch {}
}

const historyData = ref([])
async function loadHistory() {
  if (!reportData.value) return
  try {
    const res = await fetch('/api/v1/reports/' + reportData.value.id + '/history', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('xixin_staff_token') } })
    const json = await res.json()
    historyData.value = json.data || []
  } catch { ElMessage.error('加载历史对比失败') }
}

// 动态加载已签到患者列表
async function loadCheckedIn() {
  try {
    const branchId = auth.currentBranchId || 1
    const [res, patientsRes] = await Promise.all([
      getCompletedRecords(branchId),
      getPatients()
    ])
    const map = {}
    if (patientsRes?.data) {
      patientsRes.data.forEach(p => { if (p.id && p.name) map[p.id] = p.name })
    }
    patientNameMap.value = map
    checkedInList.value = (res?.data || []).map(r => {
      const name = r.patientName || map[r.userId] || '患者#' + r.userId
      const enriched = { ...r, patientName: name }
      clinic.addExamRecord(enriched)
      return enriched
    })
    clinic.setCheckedInList(checkedInList.value)
  } catch {} }
onMounted(() => {
  loadCheckedIn()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100% { opacity:1; transform:scale(1) } 50% { opacity:0.5; transform:scale(0.8) } }
</style>
