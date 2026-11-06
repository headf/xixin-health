<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>异常预警</h2>
    </div>

    <el-card shadow="never" style="margin-bottom:20px;background:#f8fafc">
      <h3 style="margin:0 0 12px;font-size:15px;font-weight:600">生成报告</h3>
      <el-form :model="genForm" inline label-width="100px">
        <el-form-item label="体检记录ID" required>
          <el-input v-model="genForm.examRecordId" placeholder="请输入体检记录ID" />
        </el-form-item>
        <el-form-item label="审核人">
          <el-select v-model="genForm.reviewerId" filterable clearable placeholder="请选择审核人" style="width:100%">
            <el-option
              v-for="a in accounts"
              :key="a.id"
              :label="`${a.displayName} (${a.phone})`"
              :value="String(a.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐ID">
          <el-input-number v-model="genForm.packageId" :min="0" style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="generating" @click="handleGenerate">生成报告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="pagedWarnings" v-loading="loadingWarnings" stripe border style="width:100%">
      <el-table-column prop="reportNo" label="报告编号" width="150" />
      <el-table-column prop="examRecordId" label="体检编号" width="100" />
      <el-table-column label="患者" min-width="100">
        <template #default="{ row }">
          {{ row.userName || row.patientName || '#' + row.userId }}
        </template>
      </el-table-column>
      <el-table-column label="异常状态" width="100">
        <template #default="{ row }">
          <el-tag :type="abnormalTagType(row.abnormalStatus)" size="small">
            {{ abnormalLabel(row.abnormalStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报告状态" width="100">
        <template #default="{ row }">
          <el-tag :type="reportStatusTagType(row.reportStatus)" size="small">
            {{ reportStatusLabel(row.reportStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="generatedAt" label="生成时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="success"
            :disabled="row.reportStatus === 'PUBLISHED'"
            :loading="publishingId === row.id"
            @click="handlePublish(row)"
          >
            {{ row.reportStatus === 'PUBLISHED' ? '已发布' : '发布' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
      <div style="display:flex;justify-content:center;margin-top:12px">
        <el-pagination
          v-if="warnings.length > 10"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="warnings.length"
          :page-sizes="[10,20,50,100]"
          layout="total, sizes, prev, pager, next"
          small
        />
      </div>
  </el-card>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReportWarnings, generateReport, publishReport, getAccounts } from '@/api'

const warnings = ref([])

const pageNum = ref(1)
const pageSize = ref(10)
const pagedWarnings = computed(() => warnings.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const loadingWarnings = ref(false)
const generating = ref(false)
const publishingId = ref(null)
const accounts = ref([])
const genForm = reactive({
  examRecordId: '',
  reviewerId: '',
  packageId: 0
})

function abnormalTagType(status) {
  if (status === 'ABNORMAL') return 'danger'
  if (status === 'NORMAL') return 'success'
  if (status === 'PENDING') return 'warning'
  return 'info'
}

function abnormalLabel(status) {
  if (status === 'ABNORMAL') return '异常'
  if (status === 'NORMAL') return '正常'
  if (status === 'PENDING') return '待判定'
  return status || '-'
}

function reportStatusTagType(status) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'DRAFT') return 'info'
  if (status === 'PENDING_REVIEW') return 'warning'
  return 'info'
}

function reportStatusLabel(status) {
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'DRAFT') return '草稿'
  if (status === 'PENDING_REVIEW') return '待审核'
  return status || '-'
}

async function loadWarnings() {
  loadingWarnings.value = true
  try {
    const res = await getReportWarnings()
    warnings.value = res.data || []
  } catch { ElMessage.error('加载预警列表失败') }
  finally { loadingWarnings.value = false }
}

async function handleGenerate() {
  if (!genForm.examRecordId) {
    ElMessage.warning('请输入体检记录ID')
    return
  }
  generating.value = true
  try {
    await generateReport({
      examRecordId: genForm.examRecordId,
      reviewerId: genForm.reviewerId || undefined,
      packageId: genForm.packageId || undefined
    })
    ElMessage.success('报告生成成功')
    await loadWarnings()
  } catch { ElMessage.error('生成报告失败') }
  finally { generating.value = false }
}

async function handlePublish(row) {
  publishingId.value = row.id
  try {
    await publishReport(row.id, { reportNo: row.reportNo })
    ElMessage.success('报告已发布')
    await loadWarnings()
  } catch { ElMessage.error('发布失败') }
  finally { publishingId.value = null }
}

async function loadAccounts() {
  try {
    const res = await getAccounts()
    accounts.value = res.data?.content || res.data || []
  } catch { /* ignore */ }
}

onMounted(() => { loadWarnings(); loadAccounts() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
