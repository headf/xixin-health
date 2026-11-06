<template>
  <div class="page">
    <div class="page-header">
      <h2>健康统计</h2>
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
          <template #header><span class="card-title">异常等级分布</span></template>
          <div ref="abnormalChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">报告风险分布</span></template>
          <div ref="riskChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Notebook, Warning, CircleClose } from '@element-plus/icons-vue'
import { getHealthStatsQuery, getBranches } from '@/api'
import * as echarts from 'echarts'

const loading = ref(false)
const statsData = ref(null)
const branches = ref([])
const abnormalChartRef = ref(null)
const riskChartRef = ref(null)
let abnormalChart = null
let riskChart = null

const searchForm = reactive({
  dateRange: null,
  branchId: '',
})

const kpis = computed(() => {
  const d = statsData.value
  if (!d) return []
  return [
    { label: '报告总数', value: d.totalReports ?? '-', color: '#6366f1', icon: 'Notebook' },
    { label: '异常报告', value: d.abnormalReports ?? '-', color: '#f59e0b', icon: 'Warning' },
    { label: '危急报告', value: d.criticalReports ?? '-', color: '#ef4444', icon: 'CircleClose' },
    { label: '异常率', value: d.totalReports > 0 ? (d.abnormalReports / d.totalReports * 100).toFixed(1) + '%' : '-', color: '#8b5cf6', icon: 'Warning' }
  ]
})

const chartColors = {
  NORMAL: '#22c55e',
  ABNORMAL: '#f59e0b',
  CRITICAL: '#ef4444',
  LOW: '#22c55e',
  MEDIUM: '#f59e0b',
  HIGH: '#ef4444'
}

function initCharts() {
  nextTick(() => {
    if (abnormalChartRef.value) abnormalChart = echarts.init(abnormalChartRef.value)
    if (riskChartRef.value) riskChart = echarts.init(riskChartRef.value)
    renderCharts()
  })
}

function renderCharts() {
  const d = statsData.value
  if (!d) return

  const abDist = d.abnormalDistribution || []
  const riskDist = d.riskDistribution || []

  if (abnormalChart) {
    abnormalChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '50%'],
        label: { formatter: '{b}\n{d}%', fontSize: 12 },
        data: abDist.map(i => ({ name: levelLabel(i.name), value: i.value, itemStyle: { color: chartColors[i.name] || '#ccc' } })),
        emphasis: { scaleSize: 8 }
      }]
    })
  }

  if (riskChart) {
    riskChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '50%'],
        label: { formatter: '{b}\n{d}%', fontSize: 12 },
        data: riskDist.map(i => ({ name: riskLabel(i.name), value: i.value, itemStyle: { color: chartColors[i.name] || '#ccc' } })),
        emphasis: { scaleSize: 8 }
      }]
    })
  }
}

function levelLabel(name) {
  const map = { NORMAL: '正常', ABNORMAL: '异常', CRITICAL: '危急' }
  return map[name] || name
}

function riskLabel(name) {
  const map = { NORMAL: '正常', ABNORMAL: '异常', CRITICAL: '危急', LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险' }
  return map[name] || name
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
    const res = await getHealthStatsQuery(params)
    statsData.value = res.data || {}
    nextTick(() => { initCharts() })
  } catch { ElMessage.error('查询失败') }
  finally { loading.value = false }
}

function handleReset() {
  searchForm.dateRange = null
  searchForm.branchId = ''
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
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
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
