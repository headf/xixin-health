<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>结果审核</h2>
        <p class="desc">审核体检医生录入的检查结果，确认或驳回</p>
      </div>
      <div style="display:flex;gap:12px">
        <div class="stat-card" style="padding:14px 20px;margin:0;width:140px">
          <div class="stat-num" style="font-size:22px;color:var(--orange)">{{ pendingList.length }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-card" style="padding:14px 20px;margin:0;width:140px">
          <div class="stat-num" style="font-size:22px;color:var(--green-dark)">{{ approvedCount }}</div>
          <div class="stat-label">已审核</div>
        </div>
        <div style="display:flex;align-items:center">
          <span v-if="polling" style="font-size:11px;color:var(--green-dark);display:flex;align-items:center;gap:4px">
            <SvgIcon name="circle" :size="8" /> 自动刷新中
          </span>
        </div>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="flag" :size="16" style="margin-right:6px;color:var(--blue)" /> 待审核列表</span>
          <button class="btn btn-secondary btn-sm" @click="loadPending"><SvgIcon name="refresh" :size="14" /> 刷新</button>
        </div>
        <div class="card-bd pad-0">
          <template v-if="pendingList.length">
            <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th class="col-nowrap">患者</th><th class="col-nowrap">检查项目</th><th class="col-nowrap">结果值</th><th class="col-nowrap">判定</th><th class="col-nowrap">审核状态</th><th class="col-nowrap">操作</th></tr></thead>
              <tbody>
                <tr v-for="row in pagedList" :key="row.id"
                    :style="{ cursor:'pointer', background: selectedId === row.id ? 'var(--blue-light)' : '' }"
                    @click="selectResult(row)">
                  <td style="font-weight:600;font-size:13px">{{ getPatientName(row) }}</td>
                  <td>{{ row.itemName || itemName(row.itemId) }}</td>
                  <td style="color:var(--n-600);max-width:120px;overflow:hidden;text-overflow:ellipsis">{{ formatValue(row) }}</td>
                  <td><span :class="['tag', row.abnormalLevel==='NORMAL'?'tag-green':row.abnormalLevel==='ABNORMAL'?'tag-orange':'tag-red']">{{ abnormalLabel(row.abnormalLevel) }}</span></td>
                  <td><span class="tag tag-orange">待审核</span></td>
                  <td><button class="btn btn-sm btn-blue" @click.stop="selectResult(row)"><SvgIcon name="eye" :size="13" /> 审核</button></td>
                </tr>
              </tbody>
            </table>
            </div>
            <!-- 分页 -->
            <div class="pagination-bar" v-if="totalPages > 1">
              <button class="btn btn-secondary btn-sm" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
              <span style="font-size:12px;color:var(--n-500);margin:0 12px">{{ currentPage }} / {{ totalPages }} 页 · 共 {{ pendingList.length }} 条</span>
              <button class="btn btn-secondary btn-sm" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页</button>
            </div>
          </template>
          <div v-else class="empty-state">
            <SvgIcon name="check" :size="36" style="color:var(--n-300);margin-bottom:12px" />
            <span class="empty-text">暂无待审核结果</span>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-hd"><span><SvgIcon name="check" :size="16" style="margin-right:6px;color:var(--green)" /> 审核操作</span><span class="tag tag-orange">审核医生</span></div>
        <div class="card-bd">
          <template v-if="!selectedId">
            <div class="empty-state" style="padding:30px 0">
              <SvgIcon name="eye" :size="36" style="color:var(--n-300);margin-bottom:12px" />
              <span class="empty-text">请从左侧选择一条结果进行审核</span>
            </div>
          </template>
          <template v-else>
            <div style="background:var(--n-50);border-radius:8px;padding:14px;margin-bottom:16px">
              <div style="font-size:12px;color:var(--n-500)">当前审核项目</div>
              <div style="font-size:16px;font-weight:600;margin-top:4px">{{ itemName(selectedItem?.itemId) }}</div>
              <div style="font-size:12px;color:var(--n-500);margin-top:2px">患者：{{ getPatientName(selectedItem) }}</div>
              <div style="font-size:13px;color:var(--n-500);margin-top:6px">
                结果值: <strong style="color:var(--n-800)">{{ formatValue(selectedItem) }}</strong>
                &nbsp;·&nbsp; 判定: <span :class="['tag', selectedItem?.abnormalLevel==='NORMAL'?'tag-green':'tag-orange']">{{ abnormalLabel(selectedItem?.abnormalLevel) }}</span>
              </div>
            </div>
            <div class="field">
              <label>审核意见</label>
              <textarea v-model="form.comment" class="ftextarea" rows="3" placeholder="输入审核意见..."></textarea>
            </div>
            <div class="field" style="flex-direction:row;align-items:center;gap:12px">
              <label style="white-space:nowrap">审核医生ID</label>
              <input v-model="form.reviewerId" class="finput" style="width:80px;height:34px;font-size:13px" readonly />
            </div>
            <div class="btn-group" style="margin-top:8px">
              <button class="btn btn-primary" style="flex:1" :disabled="reviewLoading" @click="handleApprove"><SvgIcon name="check" :size="16" /> 审核通过</button>
              <button class="btn btn-ghost" style="flex:1;border-color:var(--red);color:var(--red)" :disabled="reviewLoading" @click="handleReject"><SvgIcon name="x" :size="16" /> 驳回</button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { getPendingReviewByBranch, reviewResult, getPatients, getExamRecord } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed } from 'vue'

const clinic = useClinicStore()
const auth = useAuthStore()
const pendingList = ref([])
const reviewLoading = ref(false)
const selectedId = ref(null)
const selectedItem = ref(null)
const approvedCount = ref(0)
const polling = ref(false)
const patientNameMap = ref({})
const currentPage = ref(1)
const pageSize = 10
let pollTimer = null

const totalPages = computed(() => Math.max(1, Math.ceil(pendingList.value.length / pageSize)))
const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return pendingList.value.slice(start, start + pageSize)
})

const form = reactive({ resultId: '', reviewerId: String(auth.user?.id || 3), comment: '' })

const itemName = (id) => clinic.itemNames[id] || '#' + id
const abnormalLabel = (l) => clinic.abnormalLabels[l] || l

function formatValue(row) {
  if (row.numericValue != null) return row.numericValue + (row.unit||'')
  if (row.textValue) return row.textValue.substring(0,25) + (row.textValue.length>25?'...':'')
  return '-'
}

function getPatientName(row) {
  if (!row) return '-'
  // 后端返回的PendingReviewItemDTO直接包含patientName
  if (row.patientName) return row.patientName
  // 兼容旧数据：从clinic缓存查找
  const record = clinic.examRecords[row.examRecordId]
  if (record?.patientName) return record.patientName
  return '#' + row.examRecordId
}

async function loadPatientNames() {
  try {
    const res = await getPatients()
    if (res?.data) {
      res.data.forEach(p => { patientNameMap.value[p.id] = p.name })
    }
    // Also load exam records to get patient names
    for (const r of pendingList.value) {
      if (!clinic.examRecords[r.examRecordId] && r.examRecordId) {
        try {
          const er = await getExamRecord(r.examRecordId)
          if (er?.data) clinic.addExamRecord(er.data)
        } catch { ElMessage.error('加载检查记录失败') }
      }
    }
  } catch { ElMessage.error('加载患者信息失败') }
}

async function loadPending() {
  try {
    // 使用当前审核医生的所属分院ID过滤（若没有分院ID则查询全部）
    const branchId = auth.currentBranchId
    let res
    if (branchId) {
      res = await getPendingReviewByBranch(branchId)
    } else {
      // 系统管理员无分院限制，查看全部
      res = await getPendingReview()
    }
    pendingList.value = res?.data || []
    currentPage.value = 1
    approvedCount.value = 0
    for (const r of pendingList.value) {
      if (r.reviewStatus === 'APPROVED' || r.reviewStatus === 'REJECTED') {
        approvedCount.value++
      }
    }
    clinic.setResults(pendingList.value)
    await loadPatientNames()
  } catch { ElMessage.error('加载待审核列表失败') }
}

function selectResult(row) {
  selectedId.value = row.id
  selectedItem.value = row
  form.resultId = String(row.id)
  form.comment = row.abnormalLevel === 'ABNORMAL' ? '异常指标已确认，建议临床复查确认。' : '结果在正常范围内，审核通过。'
}

function goPage(page) { currentPage.value = Math.max(1, Math.min(page, totalPages.value)) }

async function handleApprove() {
  if(!form.resultId) return
  reviewLoading.value = true
  try {
    const res = await reviewResult(Number(form.resultId), { reviewerId:Number(form.reviewerId), approved:true, comment:form.comment })
    ElMessage.success('审核已通过')
    approvedCount.value = (res?.data?.approvedCount != null) ? res.data.approvedCount : approvedCount.value + 1
    form.resultId=''; selectedId.value=null; selectedItem.value=null
    await loadPending()
  } catch { ElMessage.error('审核失败') }
  finally { reviewLoading.value = false }
}

async function handleReject() {
  if(!form.resultId) return
  try {
    await ElMessageBox.confirm('确定驳回该审核结果吗？驳回后需要医生重新提交。', '驳回确认', { confirmButtonText: '确定驳回', cancelButtonText: '取消', type: 'warning' })
  } catch { return }
  reviewLoading.value = true
  try {
    const res = await reviewResult(Number(form.resultId), { reviewerId:Number(form.reviewerId), approved:false, comment:form.comment })
    ElMessage.success('已驳回')
    approvedCount.value = (res?.data?.approvedCount != null) ? res.data.approvedCount : approvedCount.value
    form.resultId=''; selectedId.value=null; selectedItem.value=null
    await loadPending()
  } catch { ElMessage.error('驳回失败') } finally { reviewLoading.value = false }
}

function startPolling() {
  polling.value = true
  pollTimer = setInterval(() => { loadPending() }, 10000)
}
function stopPolling() { polling.value = false; if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }

onMounted(() => { loadPending(); startPolling() })
onUnmounted(() => stopPolling())
</script>
