<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>流程节点管理</h2>
      <el-button type="primary" @click="handleCreate">新增节点</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="nodeCode" label="编码" min-width="80" />
      <el-table-column prop="nodeName" label="名称" min-width="80" />
      <el-table-column prop="nodeType" label="类型" min-width="60" />
      <el-table-column prop="sortNo" label="排序" min-width="50" />
      <el-table-column prop="requiredPermission" label="权限" min-width="70" show-overflow-tooltip />
      <el-table-column prop="approvalRoleCode" label="审批角色" min-width="80" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑节点' : '新增节点'" width="520px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="编码" required>
          <el-input v-model="form.nodeCode" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.nodeName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.nodeType" clearable placeholder="请选择节点类型" style="width:100%">
            <el-option label="登记" value="CHECK_IN" />
            <el-option label="检查" value="EXAM" />
            <el-option label="审核" value="REVIEW" />
            <el-option label="报告" value="REPORT" />
            <el-option label="完成" value="COMPLETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="权限">
          <el-select v-model="form.requiredPermission" clearable filterable placeholder="请选择或输入权限" style="width:100%">
            <el-option label="检查-读取" value="exam:read" />
            <el-option label="检查-写入" value="exam:write" />
            <el-option label="报告-读取" value="report:read" />
            <el-option label="报告-写入" value="report:write" />
            <el-option label="排班-读取" value="schedule:read" />
            <el-option label="排班-写入" value="schedule:write" />
            <el-option label="订单-读取" value="order:read" />
            <el-option label="订单-写入" value="order:write" />
            <el-option label="管理-读取" value="admin:read" />
            <el-option label="管理-写入" value="admin:write" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批角色">
          <el-select v-model="form.approvalRoleCode" clearable filterable placeholder="请选择或输入角色" style="width:100%">
            <el-option label="医生" value="DOCTOR" />
            <el-option label="检查医生" value="EXAM_DOCTOR" />
            <el-option label="审核医生" value="REVIEW_DOCTOR" />
            <el-option label="护士" value="NURSE" />
            <el-option label="前台" value="RECEPTIONIST" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
          </el-select>
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
  nodeCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  nodeName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}
const form = ref({
  nodeCode: '',
  nodeName: '',
  nodeType: '',
  sortNo: 0,
  requiredPermission: '',
  approvalRoleCode: '',
  enabled: true
})

async function load() {
  loading.value = true
  try {
    const res = await api.getFlowNodes()
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleCreate() {
  editing.value = null
  form.value = { nodeCode: '', nodeName: '', nodeType: '', sortNo: 0, requiredPermission: '', approvalRoleCode: '', enabled: true }
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
      await api.updateFlowNode(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.createFlowNode(form.value)
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
    await api.toggleFlowNode(row.id, !row.enabled)
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
