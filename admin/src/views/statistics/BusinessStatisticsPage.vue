<template>
  <div class="page">
    <div class="page-header">
      <h2>业务统计</h2>
    </div>

    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="分院">
          <el-select v-model="searchForm.branchId" placeholder="全部分院" clearable style="width:180px">
            <el-option v-for="b in branches" :key="b.id" :label="b.branchName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="维度">
          <el-select v-model="searchForm.dimension" style="width:120px">
            <el-option label="按日" value="daily" />
            <el-option label="按月" value="monthly" />
            <el-option label="按年" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 概览卡片 -->
    <div v-if="statsData" class="kpi-row">
      <div class="kpi-card" v-for="kpi in kpis" :key="kpi.label" :style="{ borderTopColor: kpi.color }">
        <div class="kpi-icon"><el-icon :size="24"><component :is="kpi.icon" /></el-icon></div>
        <div class="kpi-body">
          <div class="kpi-value">{{ kpi.value }}</div>
          <div class="kpi-label">{{ kpi.label }}</div>
        </div>
      </div>
    </div>
    <el-empty v-else description="点击查询查看统计数据" :image-size="80" />

    <!-- 图表区 -->
    <el-row v-if="statsData" :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">套餐销售分布</span></template>
          <div ref="packageChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">分院业绩对比</span></template>
          <div ref="branchChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { DataBoard, User, CircleCheck, Finished, Money, ShoppingCart } from '@element-plus/icons-vue'
import { getBusinessStatsQuery, getBranches } from '@/api'
import * as echarts from 'echarts'

const loading = ref(false)
const statsData = ref(null)
const branches = ref([])
const packageChartRef = ref(null)
const branchChartRef = ref(null)
let packageChart = null
let branchChart = null

const searchForm = reactive({
  dateRange: null,
  branchId: '',
  dimension: 'daily'
})

const kpis = computed(() => {
  const d = statsData.value
  if (!d) return []
  return [
    { label: '预约总数', value: d.totalAppointments ?? '-', color: '#6366f1', icon: 'DataBoard' },
    { label: '已支付', value: d.paidAppointments ?? '-', color: '#22c55e', icon: 'ShoppingCart' },
    { label: '已签到', value: d.checkedInAppointments ?? '-', color: '#3b82f6', icon: 'CircleCheck' },
    { label: '已完成', value: d.completedAppointments ?? '-', color: '#f59e0b', icon: 'Finished' },
    { label: '付费订单', value: d.paidOrders ?? '-', color: '#8b5cf6', icon: 'Money' },
    { label: '总收入(元)', value: d.totalRevenueCent != null ? (d.totalRevenueCent / 100).toFixed(2) : '-', color: '#ef4444', icon: 'Money' }
  ]
})

function initCharts() {
  nextTick(() => {
    if (packageChartRef.value) {
      packageChart = echarts.init(packageChartRef.value)
    }
    if (branchChartRef.value) {
      branchChart = echarts.init(branchChartRef.value)
    }
    renderCharts()
  })
}

function renderCharts() {
  const d = statsData.value
  if (!d) return

  const packageSales = d.packageSales || []
  const branchPerf = d.branchPerformance || []

  const pkgNameMap = { '1': '基础', '2': '全面', '3': '精简', '4': '深度' }

  if (packageChart) {
    packageChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['55%', '80%'],
        center: ['50%', '50%'],
        label: { formatter: '{b}\n{d}%' },
        data: packageSales.map(i => ({
          name: pkgNameMap[i.name] || i.name,
          value: i.value
        })),
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 }
      }]
    })
  }

  if (branchChart) {
    const branchNameMap = { '1': '总院', '2': '浦东', '3': '南山', '4': '朝阳' }
    branchChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: branchPerf.map(i => branchNameMap[i.name] || i.name), axisLabel: { fontSize: 12 } },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: branchPerf.map(i => i.value),
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#6366f1' },
            { offset: 1, color: '#a5b4fc' }
          ])
        }
      }]
    })
  }
}

watch(statsData, () => { initCharts() })

async function loadBranches() {
  try {
    const res = await getBranches()
    branches.value = res.data || []
  } catch { /* ignore */ }
}

async function handleQuery() {
  loading.value = true
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    if (searchForm.branchId) params.branchId = searchForm.branchId
    if (searchForm.dimension) params.dimension = searchForm.dimension
    const res = await getBusinessStatsQuery(params)
    statsData.value = res.data || {}
    nextTick(() => { initCharts() })
  } catch { ElMessage.error('查询失败') }
  finally { loading.value = false }
}

function handleReset() {
  searchForm.dateRange = null
  searchForm.branchId = ''
  searchForm.dimension = 'daily'
  statsData.value = null
}

onMounted(() => { loadBranches() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.filter-card { margin-bottom: 20px; background: #f8fafc; }

.kpi-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}
.kpi-card {
  display: flex; align-items: center; gap: 14px;
  background: var(--card); border-radius: 12px;
  padding: 20px 18px;
  box-shadow: var(--shadow-card);
  border-top: 3px solid #ccc;
  transition: transform .2s, box-shadow .2s;
}
.kpi-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-card-hover); }
.kpi-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; background: var(--bg); color: var(--primary); flex-shrink: 0; }
.kpi-value { font-size: 26px; font-weight: 700; color: var(--title); line-height: 1.3; }
.kpi-label { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }

.card-title { font-size: 15px; font-weight: 600; }
</style>
