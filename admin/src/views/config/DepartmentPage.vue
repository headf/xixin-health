<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>科室管理</h2>
      <el-button type="primary" @click="handleCreate">新增科室</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="departmentCode" label="编码" min-width="80" />
      <el-table-column prop="departmentName" label="名称" min-width="80" />
      <el-table-column label="分院" min-width="100">
        <template #default="{ row }">
          {{ branchName(row.branchId) }}
        </template>
      </el-table-column>
      <el-table-column prop="location" label="位置" min-width="80" show-overflow-tooltip />
      <el-table-column prop="capacity" label="容量" min-width="60" />
      <el-table-column label="空腹" min-width="60">
        <template #default="{ row }">
          <el-tag :type="row.fastingRequired ? 'warning' : 'info'" size="small">
            {{ row.fastingRequired ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="65" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            @change="handleToggle(row)"
            :loading="togglingId === row.id"
            size="small"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
      <div style="display:flex;justify-content:center;margin-top:12px">
        <el-pagination
          v-if="data.length > 10"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="data.length"
          :page-sizes="[10,20,50,100]"
          layout="total, sizes, prev, pager, next"
          small
        />
      </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑科室' : '新增科室'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="编码" required>
          <el-input v-model="form.departmentCode" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.departmentName" />
        </el-form-item>
        <el-form-item label="所属分院" prop="branchId">
          <el-select v-if="!auth.isBranchAdmin" v-model="form.branchId" placeholder="请选择分院" style="width:100%">
            <el-option v-for="b in branches" :key="b.id" :label="b.branchName" :value="b.id" />
          </el-select>
          <el-tag v-else>{{ branchName(form.branchId) }}</el-tag>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="如 3楼东侧" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="空腹">
          <el-switch v-model="form.fastingRequired" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as api from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const data = ref([])

const pageNum = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => data.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const togglingId = ref(null)
const formRef = ref(null)
const formRules = {
  departmentCode: [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
  departmentName: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  branchId: [{ required: true, message: '请选择所属分院', trigger: 'change' }]
}
const branches = ref([])
const form = ref({
  departmentCode: '',
  departmentName: '',
  branchId: 0,
  location: '',
  capacity: 0,
  fastingRequired: false,
  enabled: true
})

async function load() {
  loading.value = true
  try {
    const params = {}
    if (auth.currentBranchId) params.branchId = auth.currentBranchId
    const res = await api.getDepartments(params)
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function loadBranches() {
  try {
    const res = await api.getBranches()
    branches.value = res.data || []
  } catch {}
}

function branchName(id) {
  const b = branches.value.find(b => b.id === id)
  return b ? b.branchName : id
}

function handleCreate() {
  editing.value = null
  form.value = { departmentCode: '', departmentName: '', branchId: auth.currentBranchId || 0, location: '', capacity: 0, fastingRequired: false, enabled: true }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleEdit(row) {
  editing.value = row
  form.value = { ...row }
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
      await api.updateDepartment(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.createDepartment(form.value)
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
    await api.toggleDepartment(row.id, !row.enabled)
    ElMessage.success('状态已更新')
    await load()
  } catch { ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}

onMounted(() => { load(); loadBranches() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
