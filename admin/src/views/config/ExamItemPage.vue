<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>体检项目管理</h2>
      <el-button type="primary" @click="handleCreate">新增项目</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="itemCode" label="编码" min-width="70" />
      <el-table-column prop="itemName" label="名称" min-width="70" />
      <el-table-column prop="departmentName" label="科室" min-width="60" />
      <el-table-column prop="unit" label="单位" min-width="60" />
      <el-table-column prop="referenceMin" label="参考下限" min-width="70" />
      <el-table-column prop="referenceMax" label="参考上限" min-width="70" />
      <el-table-column prop="criticalMin" label="危急下限" min-width="70" />
      <el-table-column prop="criticalMax" label="危急上限" min-width="70" />
      <el-table-column label="审核" width="100">
        <template #default="{ row }">
          <el-tag :type="reviewTagType(row.reviewStatus)" size="small">
            {{ reviewLabel(row.reviewStatus) }}
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑项目' : '新增项目'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="编码" required>
          <el-input v-model="form.itemCode" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.itemName" />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="form.departmentId" placeholder="选择科室" style="width:100%" filterable>
            <el-option v-for="d in departments" :key="d.id" :label="d.departmentName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="参考下限">
          <el-input-number v-model="form.referenceMin" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="参考上限">
          <el-input-number v-model="form.referenceMax" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="危急下限">
          <el-input-number v-model="form.criticalMin" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="危急上限">
          <el-input-number v-model="form.criticalMax" :min="0" :precision="2" style="width:100%" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import * as api from '@/api'

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
const departments = ref([])
const formRules = {
  itemCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}
const form = ref({
  itemCode: '',
  itemName: '',
  departmentId: null,
  unit: '',
  referenceMin: null,
  referenceMax: null,
  criticalMin: null,
  criticalMax: null,
  enabled: true
})

function reviewTagType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'PENDING') return 'warning'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

function reviewLabel(status) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'PENDING') return '待审核'
  if (status === 'REJECTED') return '已拒绝'
  return status || '-'
}

async function load() {
  loading.value = true
  try {
    const res = await api.getExamItems()
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleCreate() {
  editing.value = null
  form.value = { itemCode: '', itemName: '', departmentId: null, unit: '', referenceMin: null, referenceMax: null, criticalMin: null, criticalMax: null, enabled: true }
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
      await api.updateExamItem(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.createExamItem(form.value)
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
    await api.toggleExamItem(row.id, !row.enabled)
    ElMessage.success('状态已更新')
    await load()
  } catch { ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}

async function loadDepartments() {
  try {
    const res = await api.getDepartments()
    departments.value = res.data || []
  } catch { /* ignore */ }
}


onMounted(() => { load(); loadDepartments() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
