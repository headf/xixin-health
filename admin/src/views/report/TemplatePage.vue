<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>报告模板管理</h2>
      <el-button type="primary" @click="handleCreate">新增模板</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="templateCode" label="模板编码" width="140" />
      <el-table-column prop="templateName" label="模板名称" min-width="140" />
      <el-table-column label="所属套餐" min-width="120">
        <template #default="{ row }">
          {{ packageName(row.packageId) }}
        </template>
      </el-table-column>
      <el-table-column prop="versionNo" label="版本号" width="80" />
      <el-table-column prop="templateContent" label="模板内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="reviewTagType(row.reviewStatus)" size="small">
            {{ reviewLabel(row.reviewStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            @change="handleToggle(row)"
            :loading="togglingId === row.id"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑模板' : '新增模板'" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="模板编码" required>
          <el-input v-model="form.templateCode" />
        </el-form-item>
        <el-form-item label="模板名称" required>
          <el-input v-model="form.templateName" />
        </el-form-item>
        <el-form-item label="套餐ID">
          <el-input-number v-model="form.packageId" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input v-model="form.templateContent" type="textarea" :rows="6" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportTemplates, createReportTemplate, updateReportTemplate, toggleReportTemplate, deleteReportTemplate, getPackages } from '@/api'

const data = ref([])

const pageNum = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => data.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const togglingId = ref(null)
const packages = ref([])
const form = ref({
  templateCode: '',
  templateName: '',
  packageId: 0,
  templateContent: '',
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
    const [res, pkgRes] = await Promise.all([getReportTemplates(), getPackages()])
    data.value = res.data || []
    packages.value = pkgRes.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function packageName(id) {
  if (!id) return '-'
  const p = packages.value.find(p => p.id === id)
  return p ? p.packageName || p.name : `#${id}`
}

function handleCreate() {
  editing.value = null
  form.value = { templateCode: '', templateName: '', packageId: 0, templateContent: '', enabled: true }
  dialogVisible.value = true
}

function handleEdit(row) {
  editing.value = row
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.templateCode || !form.value.templateName) {
    ElMessage.warning('请填写模板编码和名称')
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateReportTemplate(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createReportTemplate(form.value)
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
    await toggleReportTemplate(row.id, !row.enabled)
    ElMessage.success('状态已更新')
    await load()
  } catch { ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该模板吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteReportTemplate(row.id)
    ElMessage.success('已删除')
    await load()
  } catch { /* cancelled or error */ }
}

onMounted(() => load())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
