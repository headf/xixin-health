<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>排班管理</h2>
      <el-button type="primary" @click="handleCreate">新增排班</el-button>
    </div>

    <!-- Filters -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <template v-if="auth.isBranchAdmin">
          <el-tag size="large" style="font-size:14px;padding:8px 16px">
            {{ branchName(filterBranchId) }}
          </el-tag>
        </template>
        <el-select v-else v-model="filterBranchId" placeholder="选择分院" clearable style="width:100%" @change="load">
          <el-option v-for="b in branches" :key="b.id" :label="b.branchName" :value="b.id" />
        </el-select>
      </el-col>
      <el-col :span="6">
        <el-date-picker v-model="filterDate" type="date" placeholder="选择日期" clearable style="width:100%"
          value-format="YYYY-MM-DD" @change="load" />
      </el-col>
    </el-row>

    <el-table :data="data" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="doctor_name" label="医生姓名" min-width="120" />
      <el-table-column prop="branch_name" label="分院" min-width="120" />
      <el-table-column prop="department_name" label="科室" min-width="120" />
      <el-table-column prop="schedule_date" label="日期" width="120" />
      <el-table-column prop="time_slot" label="时段" width="120">
        <template #default="{ row }">
          <el-tag :type="timeSlotTagType(row.time_slot)" size="small">
            {{ timeSlotLabel(row.time_slot) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="号源" width="160" align="center">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;justify-content:center;gap:4px">
            <el-progress
              :percentage="row.max_patients ? Math.round(row.booked_count / row.max_patients * 100) : 0"
              :status="slotProgressStatus(row)"
              :stroke-width="12"
              :width="80"
              style="flex:1;max-width:90px"
            />
            <span style="font-size:13px;white-space:nowrap">
              <span :style="{ color: row.remaining_slots <= 0 ? '#f56c6c' : row.remaining_slots <= 3 ? '#e6a23c' : '#67c23a', fontWeight: 600 }">
                {{ row.remaining_slots }}
              </span>
              <span style="color:#999">/{{ row.max_patients }}</span>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="号源状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="slotStatusType(row)" size="small" effect="dark" style="width:100%;text-align:center">
            {{ slotStatusText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用" width="70" align="center">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="handleToggle(row)" :loading="togglingId === row.id" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑排班' : '新增排班'" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="form.doctorId" placeholder="选择医生" style="width:100%" filterable>
            <el-option v-for="d in doctors" :key="d.id" :label="d.display_name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分院" prop="branchId">
          <el-select v-model="form.branchId" placeholder="选择分院" style="width:100%" @change="onBranchChange">
            <el-option v-for="b in branches" :key="b.id" :label="b.branchName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="选择科室" style="width:100%" filterable>
            <el-option v-for="d in filteredDepartments" :key="d.id" :label="d.departmentName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="scheduleDate">
          <el-date-picker v-model="form.scheduleDate" type="date" placeholder="选择日期" style="width:100%"
            value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot">
          <el-select v-model="form.timeSlot" placeholder="选择时段" style="width:100%">
            <el-option label="上午" value="MORNING" />
            <el-option label="下午" value="AFTERNOON" />
            <el-option label="全天" value="ALL_DAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数" prop="maxPatients">
          <el-input-number v-model="form.maxPatients" :min="1" :max="200" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-top:16px" v-if="totalElements > 0">
      <span style="font-size:13px;color:#94a3b8">共 {{ totalElements }} 条记录</span>
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="totalElements"
        :page-sizes="[15, 30, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as api from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const data = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(15)
const totalElements = ref(0)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const togglingId = ref(null)
const formRef = ref(null)

const filterBranchId = ref(auth.currentBranchId || null)
const filterDate = ref(null)

const branches = ref([])
const departments = ref([])
const doctors = ref([])

const filteredDepartments = computed(() => {
  if (!form.value.branchId) return departments.value
  return departments.value.filter(d => d.branchId === form.value.branchId)
})

const formRules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  branchId: [{ required: true, message: '请选择分院', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  maxPatients: [{ required: true, message: '请输入最大人数', trigger: 'blur' }]
}

const form = ref({
  doctorId: null,
  branchId: null,
  departmentId: null,
  scheduleDate: '',
  timeSlot: '',
  maxPatients: 20
})

function timeSlotTagType(slot) {
  if (slot === 'MORNING') return 'warning'
  if (slot === 'AFTERNOON') return 'primary'
  if (slot === 'ALL_DAY') return 'success'
  return 'info'
}

function timeSlotLabel(slot) {
  if (slot === 'MORNING') return '上午'
  if (slot === 'AFTERNOON') return '下午'
  if (slot === 'ALL_DAY') return '全天'
  return slot || '-'
}

function slotProgressStatus(row) {
  if (!row.max_patients || row.booked_count >= row.max_patients) return 'exception'
  if (row.remaining_slots <= 3) return 'warning'
  if (row.booked_count === 0) return ''
  return 'success'
}

function slotStatusType(row) {
  if (row.slot_status === 'FULL' || (row.max_patients && row.booked_count >= row.max_patients)) return 'danger'
  if (row.slot_status === 'TIGHT' || row.remaining_slots <= 3) return 'warning'
  return 'success'
}

function slotStatusText(row) {
  if (row.slot_status === 'FULL' || (row.max_patients && row.booked_count >= row.max_patients)) return '已满'
  if (row.slot_status === 'TIGHT' || row.remaining_slots <= 3) return '紧张'
  if (row.remaining_slots > 0) return '充足'
  return '未配置'
}

async function load() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (filterBranchId.value) params.branchId = filterBranchId.value
    if (filterDate.value) params.date = filterDate.value
    const res = await api.getSchedules(params)
    if (res.data?.content) {
      data.value = res.data.content
      totalElements.value = res.data.totalElements || 0
    } else if (Array.isArray(res.data)) {
      data.value = res.data
      totalElements.value = res.data.length
    } else {
      data.value = []
    }
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function branchName(id) {
  const b = branches.value.find(b => b.id === id)
  return b ? b.branchName : `分院#${id}`
}

function handlePageChange(p) {
  currentPage.value = p
  load()
}
function handleSizeChange(s) {
  pageSize.value = s
  currentPage.value = 1
  load()
}

async function loadDropdowns() {
  try {
    const [brRes, deptRes, docRes] = await Promise.all([
      api.getBranches(),
      api.getDepartments(),
      api.getDoctors()
    ])
    branches.value = brRes.data || []
    departments.value = deptRes.data || []
    doctors.value = docRes.data || []
  } catch { ElMessage.error('加载选项失败') }
}

function onBranchChange() {
  form.value.departmentId = null
}

function handleCreate() {
  editing.value = null
  form.value = {
    doctorId: null,
    branchId: null,
    departmentId: null,
    scheduleDate: '',
    timeSlot: '',
    maxPatients: 20
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleEdit(row) {
  editing.value = row
  form.value = {
    doctorId: row.doctor_id,
    branchId: row.branch_id,
    departmentId: row.department_id,
    scheduleDate: row.schedule_date,
    timeSlot: row.time_slot,
    maxPatients: row.max_patients
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }
  saving.value = true
  try {
    if (editing.value) {
      await api.updateSchedule(editing.value.id, {
        timeSlot: form.value.timeSlot,
        maxPatients: form.value.maxPatients,
        enabled: editing.value.enabled
      })
      ElMessage.success('更新成功')
    } else {
      await api.createSchedule(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await load()
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

async function handleToggle(row) {
  togglingId.value = row.id
  try {
    await api.updateSchedule(row.id, {
      timeSlot: row.time_slot,
      maxPatients: row.max_patients,
      enabled: !row.enabled
    })
    ElMessage.success('状态已更新')
    await load()
  } catch { ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}


onMounted(async () => {
  await loadDropdowns()
  await load()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
