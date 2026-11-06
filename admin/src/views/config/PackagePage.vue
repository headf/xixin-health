<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>套餐管理</h2>
      <el-button type="primary" @click="handleCreate">新增套餐</el-button>
    </div>

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="packageCode" label="编码" min-width="80" />
      <el-table-column prop="packageName" label="名称" min-width="100" />
      <el-table-column prop="category" label="分类" min-width="60" />
      <el-table-column prop="genderLimit" label="性别" min-width="60" />
      <el-table-column prop="ageRange" label="年龄" min-width="70" />
      <el-table-column label="价格(元)" min-width="80">
        <template #default="{ row }">
          {{ (row.priceCent / 100).toFixed(2) }}
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑套餐' : '新增套餐'" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="编码" required>
          <el-input v-model="form.packageCode" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.packageName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" clearable placeholder="请选择分类" style="width:100%">
            <el-option label="基础体检" value="BASIC" />
            <el-option label="全面体检" value="FULL" />
            <el-option label="老年体检" value="SENIOR" />
            <el-option label="妇科体检" value="GYN" />
            <el-option label="心血管专项" value="CARDIO" />
            <el-option label="糖尿病筛查" value="DIABETES" />
            <el-option label="肝脏健康" value="LIVER" />
            <el-option label="肾脏健康" value="KIDNEY" />
            <el-option label="学生体检" value="STUDENT" />
            <el-option label="至尊体检" value="PREMIUM" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别限制">
          <el-select v-model="form.genderLimit" clearable placeholder="不限" style="width:100%">
            <el-option label="不限" value="" />
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄范围">
          <el-input v-model="form.ageRange" placeholder="如 18-60" />
        </el-form-item>
        <el-form-item label="价格(分)">
          <el-input-number v-model="form.priceCent" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="form.itemIds" multiple filterable placeholder="请选择检查项目" style="width:100%">
            <el-option v-for="item in examItems" :key="item.id" :label="item.itemName" :value="item.id" />
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
const examItems = ref([])
const formRules = {
  packageCode: [{ required: true, message: '请输入套餐编码', trigger: 'blur' }],
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }]
}
const form = ref({
  packageCode: '',
  packageName: '',
  category: '',
  genderLimit: '',
  ageRange: '',
  priceCent: 0,
  itemIds: [],
  enabled: true
})

async function load() {
  loading.value = true
  try {
    const res = await api.getPackages()
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleCreate() {
  editing.value = null
  form.value = { packageCode: '', packageName: '', category: '', genderLimit: '', ageRange: '', priceCent: 0, itemIds: [], enabled: true }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleEdit(row) {
  editing.value = row
  const ids = row.itemIds
  const itemIdsArr = ids ? (Array.isArray(ids) ? ids : String(ids).split(',').filter(Boolean).map(s => Number(s) || s)) : []
  form.value = { ...row, itemIds: itemIdsArr }
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
    const payload = { ...form.value, itemIds: Array.isArray(form.value.itemIds) ? form.value.itemIds.join(',') : form.value.itemIds }
    if (editing.value) {
      await api.updatePackage(editing.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await api.createPackage(payload)
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
    await api.togglePackage(row.id, !row.enabled)
    ElMessage.success('状态已更新')
    await load()
  } catch { ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}


async function loadExamItems() {
  try {
    const res = await api.getExamItems()
    examItems.value = res.data || []
  } catch { /* ignore */ }
}

onMounted(() => {
  load()
  loadExamItems()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
