<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>预约列表</h2>
      <div class="page-header-actions">
        <el-select v-model="statusFilter" placeholder="状态筛选" size="small" style="width:140px" clearable @change="handleFilterChange">
          <el-option label="全部" value="" />
          <el-option label="待支付" value="CREATED" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已签到" value="CHECKED_IN" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button size="small" @click="loadData">
          <el-icon style="margin-right:4px"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-table :data="appointments" stripe v-loading="loading" empty-text="暂无预约记录" style="width:100%">
      <el-table-column type="index" label="序号" width="70" />
      <el-table-column prop="appointmentNo" label="预约编号" width="160" />
      <el-table-column prop="userName" label="患者" min-width="100" />
      <el-table-column prop="packageName" label="体检套餐" min-width="140" />
      <el-table-column prop="branchName" label="分院" min-width="120" />
      <el-table-column prop="appointmentDate" label="预约日期" width="110" />
      <el-table-column prop="timeSlot" label="时段" width="100" />
      <el-table-column label="状态" width="85" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small" effect="plain">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="150" />
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top:16px;justify-content:flex-end"
      @current-change="handlePageChange"
      @size-change="pageSize = arguments[0]; currentPage = 1; loadData()"
    />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAppointments } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const appointments = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(15)
const totalElements = ref(0)

const total = computed(() => totalElements.value)

function statusType(s) {
  const m = { CREATED: 'warning', PAID: 'success', CHECKED_IN: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return m[s] || 'info'
}
function statusLabel(s) {
  const m = { CREATED: '待支付', PAID: '已支付', CHECKED_IN: '已签到', COMPLETED: '已完成', CANCELLED: '已取消' }
  return m[s] || s || '-'
}
async function loadData() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (auth.currentBranchId) params.branchId = auth.currentBranchId
    const res = await getAppointments(params)
    if (res.data?.content) {
      appointments.value = res.data.content
      totalElements.value = res.data.totalElements || 0
    } else if (Array.isArray(res.data)) {
      appointments.value = res.data
      totalElements.value = res.data.length
    }
  } catch (e) {
    console.error(e)
    appointments.value = []
  } finally {
    loading.value = false
  }
}

function handleFilterChange(val) {
  statusFilter.value = val
  currentPage.value = 1
  loadData()
}

function handlePageChange(p) {
  currentPage.value = p
  loadData()
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--title);
  margin: 0;
}
.page-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

</style>
