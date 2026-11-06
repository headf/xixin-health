<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>分院管理</h2>
      <el-button type="primary" @click="handleCreate">新增分院</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="branchCode" label="编码" min-width="80" />
      <el-table-column prop="branchName" label="名称" min-width="100" />
      <el-table-column prop="address" label="地址" min-width="120" show-overflow-tooltip />
      <el-table-column prop="servicePhone" label="电话" min-width="120" />
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑分院' : '新增分院'" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="编码" required>
          <el-input v-model="form.branchCode" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.branchName" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.servicePhone" />
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
  branchCode: [{ required: true, message: '请输入分院编码', trigger: 'blur' }],
  branchName: [{ required: true, message: '请输入分院名称', trigger: 'blur' }],
  servicePhone: [{ pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}
const form = ref({
  branchCode: '',
  branchName: '',
  address: '',
  servicePhone: '',
  enabled: true
})

async function load() {
  loading.value = true
  try {
    const res = await api.getBranches()
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleCreate() {
  editing.value = null
  form.value = { branchCode: '', branchName: '', address: '', servicePhone: '', enabled: true }
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
      await api.updateBranch(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.createBranch(form.value)
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
    await api.toggleBranch(row.id, !row.enabled)
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
</style>
