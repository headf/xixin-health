<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>随访干预</h2>
      <el-button type="primary" @click="openDialog(null)">新增随访</el-button>
    </div>

    <!-- Table -->
    <el-table :data="pagedRecords" border stripe style="width:100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="patientName" label="患者" width="140" />
      <el-table-column prop="patientPhone" label="电话" width="140" />
      <el-table-column prop="riskLevel" label="风险等级" width="120">
        <template #default="{ row }">
          <el-tag :type="riskTagType(row.riskLevel)" :color="riskTagColor(row.riskLevel)" size="small" effect="dark">
            {{ riskLevelLabel(row.riskLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="managementStatus" label="管理状态" width="130">
        <template #default="{ row }">
          <el-tag :type="row.managementStatus === 'FOLLOW_UP_PLANNED' ? 'success' : 'info'" size="small">
            {{ row.managementStatus === 'FOLLOW_UP_PLANNED' ? '已制定随访' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="followUpPlan" label="随访计划" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" plain @click="openDialog(row)">编辑</el-button>
          <el-button class="btn-theme" size="small" @click="openFollowUp(row)" v-if="row.managementStatus !== 'FOLLOW_UP_PLANNED'">随访</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex;justify-content:center;align-items:center;gap:12px;padding:12px 0" v-if="followTotalPages > 1">
      <el-button size="small" :disabled="followPage <= 1" @click="followPage = Math.max(1, followPage - 1)">上一页</el-button>
      <span style="font-size:13px;color:#64748b">{{ followPage }} / {{ followTotalPages }} 页 · 共 {{ records.length }} 条</span>
      <el-button size="small" :disabled="followPage >= followTotalPages" @click="followPage = Math.min(followTotalPages, followPage + 1)">下一页</el-button>
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理记录' : '新增管理记录'" width="560px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="患者" required>
          <el-select v-model="editForm.userId" filterable placeholder="请选择患者" style="width:100%">
            <el-option v-for="p in patientList" :key="p.id" :label="p.name + (p.phone ? '  ' + p.phone : '')" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" required>
          <el-select v-model="editForm.riskLevel" placeholder="请选择风险等级" style="width:100%">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
            <el-option label="危急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理状态" required>
          <el-select v-model="editForm.managementStatus" placeholder="请选择管理状态" style="width:100%">
            <el-option label="待处理" value="OPEN" />
            <el-option label="已制定随访" value="FOLLOW_UP_PLANNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访计划">
          <el-input v-model="editForm.followUpPlan" type="textarea" :rows="3" placeholder="请输入随访计划" />
        </el-form-item>
        <el-form-item label="干预方案">
          <el-input v-model="editForm.interventionPlan" type="textarea" :rows="3" placeholder="请输入干预方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Follow-up Dialog -->
    <el-dialog v-model="followUpVisible" title="制定随访计划" width="520px">
      <el-form :model="followUpForm" label-width="100px">
        <el-form-item label="随访计划">
          <el-input v-model="followUpForm.followUpPlan" type="textarea" :rows="3" placeholder="请输入随访计划内容" />
        </el-form-item>
        <el-form-item label="干预方案">
          <el-input v-model="followUpForm.interventionPlan" type="textarea" :rows="3" placeholder="请输入干预方案内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followUpVisible = false">取消</el-button>
        <el-button type="primary" :loading="followUpSaving" @click="handleFollowUpSubmit">提交随访</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHealthManagement, createHealthManagement, createFollowUp, updateHealthManagement, getPatients } from '@/api'

const loading = ref(false)
const records = ref([])
const followPage = ref(1)
const followPageSize = 10
const pagedRecords = computed(() => {
  const start = (followPage.value - 1) * followPageSize
  return records.value.slice(start, start + followPageSize)
})
const followTotalPages = computed(() => Math.max(1, Math.ceil(records.value.length / followPageSize)))
const patientList = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const editForm = reactive({
  userId: '',
  riskLevel: '',
  managementStatus: 'OPEN',
  followUpPlan: '',
  interventionPlan: ''
})

const followUpVisible = ref(false)
const followUpId = ref(null)
const followUpSaving = ref(false)
const followUpForm = reactive({
  followUpPlan: '',
  interventionPlan: ''
})

function riskLevelLabel(level) {
  const map = { LOW: '低风险', MEDIUM: '中风险', MODERATE: '中风险', HIGH: '高风险', CRITICAL: '危急' }
  return map[level] || level
}

function riskTagType(level) {
  const map = { LOW: 'success', MEDIUM: 'warning', MODERATE: 'warning', HIGH: 'danger', CRITICAL: 'danger' }
  return map[level] || 'danger'
}

function riskTagColor(level) {
  return ''
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await getHealthManagement()
    records.value = res?.data || []; followPage.value = 1
  } catch {
    ElMessage.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

async function loadPatients() {
  try {
    const res = await getPatients()
    patientList.value = res?.data || []
  } catch {}
}

function resetEditForm() {
  editForm.userId = ''
  editForm.riskLevel = ''
  editForm.managementStatus = 'OPEN'
  editForm.followUpPlan = ''
  editForm.interventionPlan = ''
}

function openDialog(row) {
  resetEditForm()
  if (row) {
    isEdit.value = true
    editId.value = row.id
    editForm.userId = row.userId
    editForm.riskLevel = row.riskLevel
    editForm.managementStatus = row.managementStatus
    editForm.followUpPlan = row.followUpPlan || ''
    editForm.interventionPlan = row.interventionPlan || ''
  } else {
    isEdit.value = false
    editId.value = null
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!editForm.userId) {
    ElMessage.warning('请选择患者')
    return
  }
  if (!editForm.riskLevel) {
    ElMessage.warning('请选择风险等级')
    return
  }
  saving.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateHealthManagement(editId.value, { ...editForm })
      ElMessage.success('更新成功')
    } else {
      await createHealthManagement({
        userId: editForm.userId,
        riskLevel: editForm.riskLevel,
        managementStatus: editForm.managementStatus,
        followUpPlan: editForm.followUpPlan,
        interventionPlan: editForm.interventionPlan
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadRecords()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

function openFollowUp(row) {
  followUpId.value = row.id
  followUpForm.followUpPlan = row.followUpPlan || ''
  followUpForm.interventionPlan = row.interventionPlan || ''
  followUpVisible.value = true
}

async function handleFollowUpSubmit() {
  if (!followUpForm.followUpPlan) {
    ElMessage.warning('请填写随访计划')
    return
  }
  if (!followUpForm.interventionPlan) {
    ElMessage.warning('请填写干预方案')
    return
  }
  followUpSaving.value = true
  try {
    await createFollowUp(followUpId.value, {
      followUpPlan: followUpForm.followUpPlan,
      interventionPlan: followUpForm.interventionPlan
    })
    ElMessage.success('随访计划已提交')
    followUpVisible.value = false
    await loadRecords()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally {
    followUpSaving.value = false
  }
}

onMounted(() => {
  loadRecords()
  loadPatients()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.btn-theme { background: var(--primary); color: #fff; border: none; }
.btn-theme:hover { background: var(--primary-dark); color: #fff; }
</style>
