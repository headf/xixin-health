<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>发送报告</h2>
        <p class="desc">将已签发的体检报告发送给用户</p>
      </div>
      <div class="page-hd-right">
        <span v-if="polling" class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 自动刷新中
        </span>
      </div>
    </div>

    <div class="card" style="margin-top:16px">
      <div class="card-hd">
        <span>已签发报告列表</span>
        <span class="tag tag-blue">{{ reportList.length }} 条</span>
      </div>
      <div class="card-bd pad-0">
        <template v-if="reportList.length">
        <div class="table-wrap" v-if="reportList.length">
          <table class="tbl">
            <thead>
              <tr>
                <th class="col-nowrap">报告编号</th>
                <th class="col-nowrap">患者</th>
                <th class="col-nowrap">状态</th>
                <th class="col-nowrap">生成时间</th>
                <th class="col-nowrap">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in pagedReports" :key="r.id">
                <td style="font-weight:500">{{ r.reportNo }}</td>
                <td>{{ r.userName || patientNameMap[r.userId] || '用户#' + r.userId }}</td>
                <td>
                  <span v-if="r.fileUrl === 'SENT'" class="tag tag-green">已发送</span>
                  <span v-else class="tag tag-orange">待发送</span>
                </td>
                <td style="font-size:12px;color:var(--n-400)">{{ r.generatedAt?.replace('T',' ') }}</td>
                <td style="display:flex;gap:6px">
                  <button class="btn btn-secondary btn-xs" @click="previewReport(r)">预览</button>
                  <button class="btn btn-primary btn-xs" :disabled="r.fileUrl === 'SENT' || sending[r.id]" @click="handleSend(r)">
                    {{ sending[r.id] ? '发送中...' : (r.fileUrl === 'SENT' ? '已发送' : '发送') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
          <!-- 分页 -->
          <div class="pagination-bar" v-if="totalReportPages > 1" style="padding:12px 16px;display:flex;align-items:center;justify-content:center;gap:12px">
            <button class="btn btn-secondary btn-sm" :disabled="reportPage <= 1" @click="goReportPage(reportPage - 1)">上一页</button>
            <span style="font-size:12px;color:var(--n-500)">{{ reportPage }} / {{ totalReportPages }} 页</span>
            <button class="btn btn-secondary btn-sm" :disabled="reportPage >= totalReportPages" @click="goReportPage(reportPage + 1)">下一页</button>
          </div>
        </template>
        <div v-else class="empty-state" style="padding:60px 0">
          <SvgIcon name="document" :size="36" style="color:var(--n-300);margin-bottom:12px" />
          <span class="empty-text">暂无已签发的报告</span>
          <div style="font-size:12px;color:var(--n-400);margin-top:4px">请先在综合终审页面完成报告终审签发</div>
        </div>
      </div>
    </div>

    <!-- Preview Modal -->
    <div v-if="previewData" class="modal-overlay" @click.self="previewData = null">
      <div class="modal-content">
        <div class="modal-header">
          <span>报告预览 — {{ previewData.reportNo }}</span>
          <button class="close-btn" @click="previewData = null">&times;</button>
        </div>
        <div class="modal-body">
          <div class="preview-section">
            <div class="preview-label">报告编号</div>
            <div class="preview-value">{{ previewData.reportNo }}</div>
          </div>
          <div class="preview-section">
            <div class="preview-label">状态</div>
            <span :class="['tag', previewData.fileUrl === 'SENT' ? 'tag-green' : 'tag-orange']">{{ previewData.fileUrl === 'SENT' ? '已发送' : '待发送' }}</span>
          </div>
          <div class="preview-section" v-if="previewData.summary">
            <div class="preview-label">总结摘要</div>
            <div class="preview-value">{{ previewData.summary }}</div>
          </div>
          <div class="preview-section" v-if="previewData.doctorAdvice">
            <div class="preview-label">医生建议</div>
            <div class="preview-value">{{ previewData.doctorAdvice }}</div>
          </div>
          <div class="preview-section" v-if="previewData.overallHealthAssessment">
            <div class="preview-label">总体健康评估</div>
            <div class="preview-value">{{ previewData.overallHealthAssessment }}</div>
          </div>
          <div class="preview-section" v-if="previewData.classification">
            <div class="preview-label">健康分类</div>
            <div class="preview-value">{{ classificationLabel(previewData.classification) }}</div>
          </div>
          <div class="preview-section">
            <div class="preview-label">生成时间</div>
            <div class="preview-value">{{ previewData.generatedAt?.replace('T',' ') }}</div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="msg" :class="['alert', msg.type]" style="margin-top:16px">{{ msg.text }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { getPublishedReports, sendReport, getReportById, getPatients } from '@/api'
import { ElMessage } from 'element-plus'

const reportList = ref([])
const sending = reactive({})
const previewData = ref(null)
const msg = ref(null)
const polling = ref(false)
const patientNameMap = ref({})
const reportPage = ref(1)
const reportPageSize = 10

const pagedReports = computed(() => {
  const start = (reportPage.value - 1) * reportPageSize
  return reportList.value.slice(start, start + reportPageSize)
})
const totalReportPages = computed(() => Math.max(1, Math.ceil(reportList.value.length / reportPageSize)))
let pollTimer = null

const classificationLabels = { NORMAL:'正常', PHYSIOLOGICAL:'生理性波动', SUB_HEALTH:'亚健康', NEED_RECHECK:'需要复查', NEED_TREATMENT:'建议就医' }
function classificationLabel(v) { return classificationLabels[v] || v }

function goReportPage(p) { reportPage.value = Math.max(1, Math.min(p, totalReportPages.value)) }

async function loadReports() {
  try {
    const [reportRes, patientRes] = await Promise.all([
      getPublishedReports(),
      getPatients().catch(() => ({ data: [] }))
    ])
    reportList.value = Array.isArray(reportRes.data) ? reportRes.data : []; reportPage.value = 1
    const map = {}
    if (patientRes?.data) {
      patientRes.data.forEach(p => { if (p.id && p.name) map[p.id] = p.name })
    }
    patientNameMap.value = map
  } catch { reportList.value = [] }
}

async function previewReport(report) {
  try {
    const res = await getReportById(report.id)
    previewData.value = res?.data || report
  } catch {
    previewData.value = report
  }
}

async function handleSend(report) {
  sending[report.id] = true; msg.value = null
  try {
    const res = await sendReport(report.id)
    if (res?.data) { report.fileUrl = 'SENT'; ElMessage.success(`报告 ${report.reportNo} 已发送`) }
  } catch { ElMessage.error('发送失败') }
  finally { sending[report.id] = false }
}

function startPolling() { polling.value = true; pollTimer = setInterval(() => { loadReports() }, 8000) }
function stopPolling() { polling.value = false; if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }

onMounted(() => { loadReports(); startPolling() })
onUnmounted(() => stopPolling())
</script>

<style scoped>
.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:.5;transform:scale(.8)} }

.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.4); z-index:100; display:flex; align-items:center; justify-content:center; padding:20px; }
.modal-content { background:#fff; border-radius:16px; width:100%; max-width:500px; max-height:80vh; overflow:hidden; display:flex; flex-direction:column; }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding:16px 20px; border-bottom:1px solid #e2e8f0; font-size:15px; font-weight:600; }
.close-btn { background:none; border:none; font-size:24px; color:#94a3b8; cursor:pointer; padding:0 4px; }
.modal-body { padding:16px 20px 20px; overflow-y:auto; }
.preview-section { margin-bottom:12px; }
.preview-label { font-size:12px; color:#94a3b8; margin-bottom:2px; }
.preview-value { font-size:14px; color:#1e293b; line-height:1.5; white-space:pre-wrap; }
</style>
