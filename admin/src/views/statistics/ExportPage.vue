<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>报表导出</h2>
    </div>

    <el-form :model="form" label-width="100px" style="max-width:500px">
      <el-form-item label="导出类型" required>
        <el-select v-model="form.type" placeholder="选择导出类型" style="width:100%">
          <el-option label="业务统计" value="business" />
          <el-option label="健康统计" value="health" />
        </el-select>
      </el-form-item>
      <el-form-item label="导出格式" required>
        <el-select v-model="form.format" placeholder="选择导出格式" style="width:100%">
          <el-option label="Excel" value="EXCEL" />
          <el-option label="PDF" value="PDF" />
          <el-option label="CSV" value="CSV" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="form.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width:100%"
        />
      </el-form-item>
      <el-form-item label="分院">
        <el-select v-model="form.branchId" placeholder="选择分院" clearable style="width:100%">
          <el-option v-for="b in branches" :key="b.id" :label="b.branchName" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="exporting" @click="handleExport">
          导出
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBranches } from '@/api'

const exporting = ref(false)
const branches = ref([])
const form = reactive({
  type: 'business',
  format: 'EXCEL',
  dateRange: null,
  branchId: ''
})

async function loadBranches() {
  try {
    const res = await getBranches()
    branches.value = res.data || []
  } catch { /* ignore */ }
}

onMounted(() => { loadBranches() })

async function handleExport() {
  if (!form.type || !form.format) {
    ElMessage.warning('请选择导出类型和格式')
    return
  }
  exporting.value = true
  try {
    const params = {
      type: form.type,
      format: form.format
    }
    if (form.dateRange && form.dateRange.length === 2) {
      params.startDate = form.dateRange[0]
      params.endDate = form.dateRange[1]
    }
    if (form.branchId) params.branchId = form.branchId

    params.token = localStorage.getItem('xixin_admin_token') || ''
    const qs = Object.entries(params).map(([k,v]) => encodeURIComponent(k)+'='+encodeURIComponent(v)).join('&')
    // 格式化扩展名
    const extMap = { EXCEL: 'xlsx', PDF: 'pdf', CSV: 'csv' }
    const link = document.createElement('a')
    link.href = '/api/v1/admin/statistics/export?' + qs
    link.download = form.type + '_export.' + (extMap[form.format] || form.format.toLowerCase())
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('导出任务已提交')
  } catch { ElMessage.error('导出失败') }
  finally { exporting.value = false }
}

function handleReset() {
  form.type = 'business'
  form.format = 'EXCEL'
  form.dateRange = null
  form.branchId = ''
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
