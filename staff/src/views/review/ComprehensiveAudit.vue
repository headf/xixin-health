<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>综合终审</h2>
        <p class="desc">主检审核医生统一终审：核对数据、整合异常、分级判断、写总体结论、终审签字</p>
      </div>
      <div class="stat-card" style="width:180px;padding:14px 20px;margin:0;background:var(--blue-light)">
        <div class="stat-num" style="font-size:22px;color:var(--blue)">{{ reportId ? '待签发' : '请生成报告' }}</div>
        <div class="stat-label">当前状态</div>
      </div>
    </div>

    <div class="two-col">
      <div style="display:flex;flex-direction:column;gap:16px">
        <!-- Step 1: Select report -->
        <div class="card">
          <div class="card-hd">
            <span><SvgIcon name="search" :size="16" style="margin-right:6px;color:var(--blue)" /> 选择体检报告</span>
          </div>
          <div class="card-bd">
            <div class="form-grid form-2">
              <div class="field">
                <label>体检记录</label>
                <select v-model="selectedExamId" class="fselect" @change="loadExamResults">
                  <option value="" disabled>─ 选择已检查完成患者 ─</option>
                  <option v-for="r in examRecords" :key="r.id" :value="r.id">
                    {{ getPatientName(r) }} — {{ r.examNo }}
                  </option>
                </select>
              </div>
              <div class="field">
                <label>审核医生</label>
                <input value="张主任 (主检医生)" class="finput" readonly />
              </div>
            </div>
            <button class="btn btn-primary" :disabled="!selectedExamId" @click="generateReportForAudit" style="margin-top:8px">
              <SvgIcon name="document" :size="16" /> 生成/加载报告
            </button>
          </div>
        </div>

        <!-- Step 2: Review all check results -->
        <div class="card">
          <div class="card-hd">
            <span><SvgIcon name="list" :size="16" style="margin-right:6px;color:var(--blue)" /> 所有检查结果汇总</span>
            <span class="tag" :class="allApproved ? 'tag-green' : 'tag-orange'">
              {{ allApproved ? '全部正常/已审核' : '存在未审核项' }}
            </span>
          </div>
          <div class="card-bd pad-0">
            <template v-if="allResults.length">
            <div class="table-wrap">
              <table class="tbl">
                <thead><tr><th class="col-nowrap">项目</th><th class="col-nowrap">结果</th><th class="col-nowrap">判定</th><th class="col-nowrap">审核状态</th></tr></thead>
                <tbody>
                  <tr v-for="r in allResults" :key="r.id"
                      :class="{ 'row-abnormal': r.abnormalLevel !== 'NORMAL' }">
                    <td>{{ clinic.itemNames[r.itemId] || r.itemName }}</td>
                    <td style="color:var(--n-600)">
                      {{ r.numericValue != null ? r.numericValue + (r.unit||'') : (r.textValue?.substring(0,30)||'') }}
                    </td>
                    <td>
                      <span :class="['tag', r.abnormalLevel==='NORMAL'?'tag-green':r.abnormalLevel==='ABNORMAL'?'tag-orange':'tag-red']">
                        {{ clinic.abnormalLabels[r.abnormalLevel] || r.abnormalLevel }}
                      </span>
                    </td>
                    <td>
                      <span :class="['tag', r.reviewStatus==='APPROVED'?'tag-green':'tag-gray']">
                        {{ r.reviewStatus === 'APPROVED' ? '已通过' : '待审核' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            </template>
            <div v-else class="empty-state">
              <SvgIcon name="list" :size="36" style="color:var(--n-300);margin-bottom:12px" />
              <span class="empty-text">请先生成报告查看更多数据</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 3: Comprehensive audit form -->
      <div class="card audit-card">
        <div class="card-hd">
          <span><SvgIcon name="check" :size="16" style="margin-right:6px;color:var(--green)" /> 综合终审表单</span>
          <span class="tag tag-orange">主检医生</span>
        </div>
        <div class="card-bd">
          <template v-if="!reportId">
            <div class="empty-state" style="padding:30px 0">
              <SvgIcon name="document" :size="36" style="color:var(--n-300);margin-bottom:12px" />
              <span class="empty-text">请先生成报告后进行综合终审</span>
            </div>
          </template>
          <template v-else>
            <div class="section-label">1. 总体健康评估</div>
            <div class="field">
              <textarea v-model="audit.overallHealthAssessment" class="ftextarea" rows="3"
                placeholder="综合所有检查结果，给出总体健康评估。如：受检者各项指标基本正常，ALT轻度偏高..."></textarea>
            </div>

            <div class="section-label">2. 异常分级判断</div>
            <div class="field">
              <div class="classification-options">
                <label v-for="opt in classificationOptions" :key="opt.value"
                  :class="['class-btn', { active: audit.classification === opt.value }]"
                  @click="audit.classification = opt.value">
                  <SvgIcon :name="audit.classification === opt.value ? 'check' : 'circle'" :size="14" />
                  {{ opt.label }}
                </label>
              </div>
            </div>

            <div class="section-label">3. 个性化生活建议</div>
            <div class="field">
              <textarea v-model="audit.lifestyleRecommendations" class="ftextarea" rows="3"
                placeholder="如：1. 注意饮食清淡，避免高脂油腻食物；2. 规律作息，避免熬夜..."></textarea>
            </div>

            <div class="section-label">4. 复查建议</div>
            <div class="field">
              <textarea v-model="audit.followUpRecommendations" class="ftextarea" rows="3"
                placeholder="如：建议3个月后复查肝功能；建议每年进行一次健康体检..."></textarea>
            </div>

            <div class="section-label">5. 总结摘要</div>
            <div class="field">
              <textarea v-model="audit.summary" class="ftextarea" rows="2"
                placeholder="简洁的总结摘要..."></textarea>
            </div>

            <div class="section-label">6. 医生建议</div>
            <div class="field">
              <textarea v-model="audit.doctorAdvice" class="ftextarea" rows="2"
                placeholder="医生建议..."></textarea>
            </div>

            <div class="divider"></div>

            <div class="btn-group">
              <button class="btn btn-primary" :disabled="auditing || !reportId" @click="handleComprehensiveAudit">
                <SvgIcon name="check" :size="16" /> 终审签字生效
              </button>
            </div>
            <transition name="fade">
              <div v-if="auditMsg" :class="['alert', auditMsg.type]">{{ auditMsg.text }}</div>
            </transition>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { comprehensiveAudit, generateReport, getReportsByExam, getCompletedRecords, getResultsByExam } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const clinic = useClinicStore()
const auth = useAuthStore()

const selectedExamId = ref('')
const examRecords = ref([])
const allResults = ref([])
const reportId = ref(null)
const auditing = ref(false)
const auditMsg = ref(null)

const classificationOptions = [
  { value: 'NORMAL', label: '✅ 正常（生理性波动）' },
  { value: 'PHYSIOLOGICAL', label: '🟡 生理性波动（无需处理）' },
  { value: 'SUB_HEALTH', label: '🟠 亚健康状态（建议调整）' },
  { value: 'NEED_RECHECK', label: '🔶 需要复查（定期随访）' },
  { value: 'NEED_TREATMENT', label: '🔴 建议就医（临床诊疗）' },
]

const audit = reactive({
  summary: '',
  doctorAdvice: '',
  overallHealthAssessment: '',
  lifestyleRecommendations: '',
  followUpRecommendations: '',
  classification: 'NORMAL'
})

const allApproved = computed(() =>
  allResults.value.length > 0 && allResults.value.every(r => r.reviewStatus === 'APPROVED')
)

function getPatientName(r) { return r.patientName || clinic.examRecords[r.id]?.patientName || '患者' }

async function loadExamResults() {
  if (!selectedExamId.value) { allResults.value = []; return }
  try {
    const res = await getResultsByExam(Number(selectedExamId.value))
    allResults.value = res?.data || []
    clinic.setResults(allResults.value)
    // Also load report for this exam if exists
    const reportRes = await getReportsByExam(Number(selectedExamId.value))
    const reports = reportRes?.data || []
    if (reports.length > 0) {
      reportId.value = reports[0].id
      const report = reports[0]
      audit.summary = report.summary || ''
      audit.doctorAdvice = report.doctorAdvice || ''
      audit.overallHealthAssessment = report.overallHealthAssessment || ''
      audit.lifestyleRecommendations = report.lifestyleRecommendations || ''
      audit.followUpRecommendations = report.followUpRecommendations || ''
      audit.classification = report.classification || 'NORMAL'
    } else {
      reportId.value = null
    }
  } catch {}
}

async function generateReportForAudit() {
  if (!selectedExamId.value) { ElMessage.warning('请选择患者'); return }
  if (reportId.value) { ElMessage.info('报告已存在，可直接填写综合审核内容'); return }
  try {
    const res = await generateReport({ examRecordId: Number(selectedExamId.value), reviewerId: 3 })
    if (res?.data?.id) {
      reportId.value = res.data.id
      ElMessage.success('报告已生成，请填写综合审核内容')
      await loadExamResults()
    }
  } catch (e) {
    ElMessage.error('请先完成所有结果录入和审核')
  }
}

async function handleComprehensiveAudit() {
  if (!reportId.value) { ElMessage.warning('请先生成报告'); return }
  if (!audit.overallHealthAssessment) { ElMessage.warning('请填写总体健康评估'); return }
  if (!audit.lifestyleRecommendations) { ElMessage.warning('请填写生活建议'); return }
  if (!audit.followUpRecommendations) { ElMessage.warning('请填写复查建议'); return }

  auditing.value = true; auditMsg.value = null
  try {
    await comprehensiveAudit(Number(reportId.value), {
      summary: audit.summary,
      doctorAdvice: audit.doctorAdvice,
      overallHealthAssessment: audit.overallHealthAssessment,
      lifestyleRecommendations: audit.lifestyleRecommendations,
      followUpRecommendations: audit.followUpRecommendations,
      classification: audit.classification
    })
    ElMessage.success('✅ 终审签字成功！体检报告已生效')
    auditMsg.value = { type: 'alert-success', text: `报告已终审签发 · 分类: ${classificationOptions.find(o => o.value === audit.classification)?.label || audit.classification}` }
  } catch (e) {
    ElMessage.error('终审失败')
    auditMsg.value = { type: 'alert-error', text: '终审失败，请重试' }
  } finally { auditing.value = false }
}

// 动态加载待总检审核的患者（供审核医生选择）
async function loadCompletedExams() {
  try {
    const branchId = auth.currentBranchId || 1
    const res = await getCompletedRecords(branchId)
    examRecords.value = (res?.data || []).map(r => {
      const name = r.patientName || '#' + r.userId
      const enriched = { ...r, patientName: name }
      clinic.addExamRecord(enriched)
      return enriched
    })
    clinic.setCheckedInList(examRecords.value)
  } catch {}
}

onMounted(() => { loadCompletedExams() })
</script>

<style scoped>
.section-label { font-size: 12px; font-weight: 600; color: var(--n-500); letter-spacing: 0.5px; margin-bottom: 8px; margin-top: 12px; }
.section-label:first-of-type { margin-top: 0; }
.classification-options { display: flex; flex-direction: column; gap: 6px; }
.class-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; border-radius: 8px; border: 1px solid var(--n-300);
  cursor: pointer; font-size: 13px; color: var(--n-600);
  transition: all 0.15s; background: var(--card);
}
.class-btn:hover { border-color: var(--blue); }
.class-btn.active { border-color: var(--blue); background: var(--blue-light); color: var(--blue); font-weight: 600; }
.row-abnormal { background: #fef2f2 !important; }
.audit-card { min-width: 0; }
.audit-card .classification-options { display: flex; flex-direction: column; gap: 6px; }
.audit-card .class-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; border-radius: 8px; border: 1px solid var(--n-300);
  cursor: pointer; font-size: 13px; color: var(--n-600);
  transition: all 0.15s; background: var(--card);
  white-space: nowrap;
}
</style>
