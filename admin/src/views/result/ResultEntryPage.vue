<template>
  <div class="result-entry-page">
    <el-card>
      <template #header>
        <span>结果录入</span>
      </template>
      <el-form :model="form" label-width="120px" label-position="left">
        <el-form-item label="体检记录编号">
          <el-input v-model="form.examRecordId" placeholder="请输入体检记录编号" clearable />
        </el-form-item>
        <el-form-item label="项目名称">
          <el-select v-model="form.itemId" filterable clearable placeholder="请选择检查项目" style="width:100%" @change="onItemSelect">
            <el-option
              v-for="item in examItems"
              :key="item.id"
              :label="`${item.itemName} (${item.itemCode || item.id})`"
              :value="String(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="例如：mmol/L" clearable />
        </el-form-item>
        <el-form-item label="数值结果">
          <el-input v-model="form.numericValue" placeholder="请输入数值结果" clearable />
        </el-form-item>
        <el-form-item label="参考下限">
          <el-input v-model="form.referenceMin" placeholder="参考范围下限" clearable />
        </el-form-item>
        <el-form-item label="参考上限">
          <el-input v-model="form.referenceMax" placeholder="参考范围上限" clearable />
        </el-form-item>
        <el-form-item label="危急上限">
          <el-input v-model="form.criticalMax" placeholder="危急值上限" clearable />
        </el-form-item>
        <el-form-item label="记录人">
          <el-select v-model="form.recorderId" filterable clearable placeholder="请选择记录人" style="width:100%">
            <el-option
              v-for="a in accounts"
              :key="a.id"
              :label="`${a.displayName} (${a.phone})`"
              :value="String(a.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            {{ loading ? '提交中...' : '提交' }}
          </el-button>
          <el-button size="large" @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result" style="margin-top: 20px;">
      <template #header>
        <span>录入结果</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="结果编号">{{ result.resultId }}</el-descriptions-item>
        <el-descriptions-item label="数值结果">{{ result.numericValue }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ result.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ result.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { recordNumericResult, getAccounts, getExamItems } from '@/api'

const form = reactive({
  examRecordId: '',
  itemId: '',
  itemName: '',
  unit: '',
  numericValue: '',
  referenceMin: '',
  referenceMax: '',
  criticalMax: '',
  recorderId: ''
})

const loading = ref(false)
const result = ref(null)
const accounts = ref([])
const examItems = ref([])

function onItemSelect(id) {
  const item = examItems.value.find(i => String(i.id) === String(id))
  if (item) {
    form.itemName = item.itemName || ''
    form.unit = item.unit || ''
    form.referenceMin = item.referenceMin ?? ''
    form.referenceMax = item.referenceMax ?? ''
    form.criticalMax = item.criticalMax ?? ''
  }
}

async function loadAccounts() {
  try {
    const res = await getAccounts()
    accounts.value = res.data?.content || res.data || []
  } catch { /* ignore */ }
}

async function loadExamItems() {
  try {
    const res = await getExamItems()
    examItems.value = res.data || []
  } catch { /* ignore */ }
}

onMounted(() => { loadAccounts(); loadExamItems() })

function resetForm() {
  form.examRecordId = ''
  form.itemId = ''
  form.itemName = ''
  form.unit = ''
  form.numericValue = ''
  form.referenceMin = ''
  form.referenceMax = ''
  form.criticalMax = ''
  form.recorderId = ''
  result.value = null
}

async function handleSubmit() {
  if (!form.examRecordId || !form.itemId || !form.numericValue) {
    ElMessage.warning('请填写体检记录编号、选择项目和数值结果')
    return
  }

  const payload = {}
  for (const key of Object.keys(form)) {
    if (form[key] !== '') {
      payload[key] = key === 'numericValue' || key === 'referenceMin' || key === 'referenceMax' || key === 'criticalMax'
        ? Number(form[key])
        : form[key]
    }
  }

  loading.value = true
  try {
    const res = await recordNumericResult(payload)
    result.value = res.data
    ElMessage.success('录入成功')
  } catch (e) {
    ElMessage.error(e.message || '录入失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.result-entry-page {
  max-width: 720px;
}
</style>
