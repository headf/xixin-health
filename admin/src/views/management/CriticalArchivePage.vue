<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>危急档案管理</h2>
    </div>

    <el-alert
      title="自动捕获机制说明"
      type="info"
      show-icon
      :closable="false"
      description="系统会根据检验结果中的危急阈值自动捕获异常数据并归档至本模块。当某项指标超出危急上限或低于危急下限时，系统将自动创建危急档案记录，并通知相关医护人员进行跟进处理。"
      style="margin-bottom:16px"
    />

    <el-table :data="pagedData" v-loading="loading" stripe border style="width:100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column label="患者" min-width="100">
        <template #default="{ row }">
          {{ row.patientName || row.userName || '#' + row.userId }}
        </template>
      </el-table-column>
      <el-table-column prop="reportId" label="报告编号" width="100" />
      <el-table-column label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag :type="riskTagType(row.riskLevel)" :color="riskTagColor(row.riskLevel)" size="small">
            {{ riskLabel(row.riskLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="管理状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.managementStatus)" size="small">
            {{ statusLabel(row.managementStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="followUpPlan" label="随访计划" min-width="160" show-overflow-tooltip />
      <el-table-column prop="interventionPlan" label="干预方案" min-width="160" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="更新时间" width="170" />
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
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHealthManagement } from '@/api'

const data = ref([])

const pageNum = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => data.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const loading = ref(false)

function riskTagType(level) {
  if (level === 'CRITICAL') return ''  // 自定义色
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM' || level === 'MODERATE') return 'warning'
  if (level === 'LOW') return 'info'
  return ''
}

function riskTagColor(level) {
  if (level === 'CRITICAL') return '#b91c1c'
  return ''
}

function riskLabel(level) {
  if (level === 'CRITICAL') return '危急'
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM' || level === 'MODERATE') return '中风险'
  if (level === 'LOW') return '低风险'
  return level || '-'
}

function statusTagType(status) {
  if (status === 'FOLLOW_UP_PLANNED' || status === 'FOLLOW_UP') return 'success'
  if (status === 'INTERVENTION') return 'warning'
  if (status === 'COMPLETED') return 'info'
  if (status === 'OPEN' || status === 'PENDING') return ''
  return ''
}

function statusLabel(status) {
  if (status === 'FOLLOW_UP_PLANNED' || status === 'FOLLOW_UP') return '随访中'
  if (status === 'INTERVENTION') return '干预中'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'OPEN' || status === 'PENDING') return '待处理'
  return status || '-'
}

async function load() {
  loading.value = true
  try {
    const res = await getHealthManagement()
    data.value = res.data || []
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

onMounted(() => load())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
</style>
