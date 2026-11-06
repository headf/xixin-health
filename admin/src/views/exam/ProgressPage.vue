<template>
  <div class="progress-page">
    <el-card>
      <template #header>
        <span>进度跟踪</span>
      </template>
      <div class="query-bar">
        <el-input
          v-model="examRecordId"
          placeholder="请输入体检记录编号"
          clearable
          style="width: 320px; margin-right: 12px;"
        />
        <el-button type="primary" :loading="loading" @click="handleLoad">
          {{ loading ? '加载中...' : '加载进度' }}
        </el-button>
      </div>
    </el-card>

    <el-card v-if="hasQueried && progress.length > 0" style="margin-top: 20px;">
      <template #header>
        <span>检查进度（共 {{ progress.length }} 项）</span>
      </template>
      <el-table :data="progress" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="itemName" label="项目名称" min-width="160" />
        <el-table-column label="科室" width="120">
          <template #default="{ row }">
            {{ departmentMap[row.departmentId] || row.departmentId }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.progressStatus || row.status)" size="small">
              {{ statusLabel(row.progressStatus || row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" width="180">
          <template #default="{ row }">
            {{ row.completedAt || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty
      v-if="hasQueried && progress.length === 0 && !loading"
      description="未查询到相关进度数据"
      :image-size="80"
      style="margin-top:60px"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getExamProgress, getDepartments, getExamItems } from '@/api'

const examRecordId = ref('')
const loading = ref(false)
const progress = ref([])
const hasQueried = ref(false)
const departmentMap = ref({})
const itemNameMap = ref({})

function statusType(status) {
  const map = {
    PENDING: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    SKIPPED: 'warning'
  }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = {
    PENDING: '待检查',
    IN_PROGRESS: '检查中',
    COMPLETED: '已完成',
    SKIPPED: '已跳过'
  }
  return map[status] || status
}

async function handleLoad() {
  if (!examRecordId.value) {
    ElMessage.warning('请输入体检记录编号')
    return
  }
  loading.value = true
  hasQueried.value = true
  try {
    const [res, deptRes] = await Promise.all([getExamProgress(examRecordId.value), getDepartments()])
    const raw = res.data || []
    if (deptRes?.data) {
      const map = {}
      deptRes.data.forEach(d => { if (d.id) map[d.id] = d.departmentName || d.name })
      departmentMap.value = map
    }
    // 拉取检查项目名称做本地映射（失败不影响主流程）
    const iMap = {}
    try {
      const itemRes = await getExamItems()
      if (Array.isArray(itemRes?.data)) {
        itemRes.data.forEach(item => { if (item.id && item.itemName) iMap[item.id] = item.itemName })
      }
    } catch (e) {
      console.warn('获取检查项目列表失败，使用本地回退', e)
    }
    itemNameMap.value = iMap
    // 把名称注入每行
    progress.value = raw.map(p => ({
      ...p,
      itemName: p.itemName || iMap[p.itemId] || ('项目#' + p.itemId)
    }))
    ElMessage.success(`加载成功，共 ${progress.value.length} 项`)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.progress-page {
  max-width: 860px;
}
.query-bar {
  display: flex;
  align-items: center;
}
</style>
