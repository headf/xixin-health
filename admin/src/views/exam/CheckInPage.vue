<template>
  <div class="checkin-page">
    <!-- 头部统计 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ borderTop: `3px solid ${stat.color}` }">
          <div class="stat-num">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 今日已签到列表 全宽 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>今日已签到</span>
          <span class="tag tag-green">{{ checkedInList.length }}人</span>
        </div>
      </template>

      <el-empty v-if="checkedInList.length === 0" description="暂无签到记录" :image-size="80" />
      <div v-else class="checkin-list">
        <div
          v-for="item in checkedInList"
          :key="item.id"
          class="checkin-item"
        >
          <div class="checkin-avatar">{{ (item.patientName || '?')[0] }}</div>
          <div class="checkin-info">
            <div class="checkin-name">{{ item.patientName || '-' }}</div>
            <div class="checkin-no">编号 {{ item.examNo || '-' }}</div>
          </div>
          <div class="checkin-time">{{ formatTime(item.checkedInAt) }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCheckedIn } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const checkedInList = ref([])

const todayCheckedIn = ref(0)

const stats = computed(() => [
  { label: '今日签到', value: todayCheckedIn.value, color: 'var(--secondary)' },
])

function formatTime(t) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour:'2-digit', minute:'2-digit' })
}

async function loadCheckedIn() {
  try {
    const branchIds = auth.currentBranchId ? [auth.currentBranchId] : [1, 2, 3, 4, 5]
    const results = []
    for (const b of branchIds) {
      const res = await getCheckedIn(b)
      if (res?.data) results.push(...res.data)
    }
    checkedInList.value = results
    todayCheckedIn.value = checkedInList.value.length
  } catch (e) { console.error(e); ElMessage.error('加载签到记录失败') }
}

onMounted(() => { loadCheckedIn() })
</script>

<style scoped>
.stats-row {
  display: flex;
  width: 100%;
  gap: 16px;
  margin-bottom: 14px;
}
.stats-row .el-col {
  max-width: 200px;
  flex: 1;
}

.stat-card {
  background: var(--card); border-radius: var(--radius-lg);
  padding: 32px 24px;
  box-shadow: var(--shadow-card); text-align: center;
  transition: box-shadow var(--transition-base);
}
.stat-card:hover { box-shadow: var(--shadow-card-hover); }
.stat-num { font-size: 36px; font-weight: 700; color: var(--title); line-height: 1.2; }
.stat-label { font-size: 15px; color: var(--text-secondary); margin-top: 8px; font-weight: 500; }

.card-header {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 15px; font-weight: var(--fw-semibold);
}

.success-banner {
  display: flex; align-items: center; gap: 12px;
  background: var(--primary-light);
  border: 1px solid var(--primary-light);
  border-radius: var(--radius-base);
  padding: 14px 18px; margin-top: 16px;
}
.success-icon { font-size: 28px; }
.success-title { font-weight: 600; font-size: 15px; color: var(--primary-dark); }
.success-desc { font-size: 13px; margin-top: 4px; color: var(--primary-dark); opacity: 0.8; }

/* Checkin List */
.checkin-list { max-height: 480px; overflow-y: auto; }
.checkin-item {
  display: flex; align-items: center;
  gap: 12px;
  padding: 10px 18px;
  border-bottom: 1px solid var(--border);
  transition: background var(--transition-fast);
}
.checkin-item:last-child { border-bottom: none; }
.checkin-item:hover { background: var(--bg); }
.checkin-avatar {
  width: 36px; height: 36px; border-radius: var(--radius-full);
  background: var(--primary-light); color: var(--primary);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 13px;
  flex-shrink: 0;
}
.checkin-info { flex: 1; min-width: 0; }
.checkin-name { font-size: var(--fs-body); font-weight: var(--fw-medium); color: var(--title); }
.checkin-no { font-size: var(--fs-xs); color: var(--text-secondary); margin-top: 1px; }
.checkin-time { font-size: var(--fs-xs); color: var(--text-secondary); flex-shrink: 0; }
</style>
