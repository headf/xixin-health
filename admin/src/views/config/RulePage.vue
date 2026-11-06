<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>规则配置</h2>
      <el-button type="primary" @click="handleCreate">新增规则</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="ruleCode" label="编码" min-width="80" />
      <el-table-column prop="ruleName" label="名称" min-width="80" />
      <el-table-column prop="ruleType" label="类型" min-width="70" />
      <el-table-column prop="ruleContent" label="内容" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="rule-content">{{ row.ruleContent }}</span>
        </template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑规则' : '新增规则'" width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="编码" required>
          <el-input v-model="form.ruleCode" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.ruleName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.ruleType" clearable placeholder="请选择规则类型" style="width:100%">
            <el-option label="内部转诊规则" value="INTERNAL_REFERRAL" />
            <el-option label="危急值规则" value="CRITICAL_VALUE" />
            <el-option label="医学默认规则" value="MEDICAL_DEFAULT" />
            <el-option label="年龄相关规则" value="MEDICAL_AGE" />
            <el-option label="性别相关规则" value="MEDICAL_GENDER" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.ruleContent" type="textarea" :rows="4" />
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
import { ref, computed, nextTick, onMounted } from 'vue'
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
const formRules = {
  ruleCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}
const form = ref({
  ruleCode: '',
  ruleName: '',
  ruleType: '',
  ruleContent: '',
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
    const res = await api.getRules()
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleCreate() {
  editing.value = null
  form.value = { ruleCode: '', ruleName: '', ruleType: '', ruleContent: '', enabled: true }
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
  try { await formRef.value.validate() } catch { return }
  saving.value = true
  try {
    if (editing.value) {
      await api.updateRule(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.createRule(form.value)
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
    await api.toggleRule(row.id, !row.enabled)
    ElMessage.success('状态已更新')
    await load()
  } catch { ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}


onMounted(() => load())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.rule-content { display: inline-block; max-width: 400px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
