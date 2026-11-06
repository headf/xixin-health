<template>
  <div class="batch-review-page">
    <el-card>
      <template #header>
        <span>批量审核</span>
      </template>
      <el-form :model="form" label-width="120px" label-position="left">
        <el-form-item label="审核人">
          <el-select v-model="form.reviewerId" filterable clearable placeholder="请选择审核人" style="width:100%">
            <el-option
              v-for="a in accounts"
              :key="a.id"
              :label="`${a.displayName} (${a.phone})`"
              :value="String(a.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="结果编号列表">
          <el-input
            v-model="form.resultIds"
            placeholder="请输入结果编号，多个用逗号分隔"
            clearable
          />
          <div class="form-tip">多个结果编号请用英文逗号（,）分隔</div>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="form.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（可选）"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            {{ loading ? '提交中...' : '批量审核' }}
          </el-button>
          <el-button size="large" @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="result" style="margin-top: 20px;">
      <template #header>
        <span>审核结果</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="审核人编号">{{ form.reviewerId }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">已完成</el-descriptions-item>
        <el-descriptions-item label="通过数量">
          <el-tag type="success">{{ approvedCount }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="跳过数量">
          <el-tag type="warning">{{ skippedCount }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="approvedList.length > 0" style="margin-top: 16px;">
        <span style="font-weight: 600; font-size: 13px; color: var(--title);">通过的编号：</span>
        <span style="font-size: 13px; color: var(--text);">{{ approvedList.join(', ') }}</span>
      </div>
      <div v-if="skippedList.length > 0" style="margin-top: 8px;">
        <span style="font-weight: 600; font-size: 13px; color: var(--title);">跳过的编号：</span>
        <span style="font-size: 13px; color: var(--text);">{{ skippedList.join(', ') }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { batchReview, getAccounts } from '@/api'

const form = reactive({
  reviewerId: '',
  resultIds: '',
  comment: ''
})

const accounts = ref([])

const loading = ref(false)
const result = ref(null)

const approvedList = computed(() => (result.value?.approvedIds || []))
const skippedList = computed(() => (result.value?.skippedIds || []))
const approvedCount = computed(() => approvedList.value.length)
const skippedCount = computed(() => skippedList.value.length)

async function loadAccounts() {
  try {
    const res = await getAccounts()
    accounts.value = res.data?.content || res.data || []
  } catch { /* ignore */ }
}

function resetForm() {
  form.reviewerId = ''
  form.resultIds = ''
  form.comment = ''
  result.value = null
}

async function handleSubmit() {
  if (!form.reviewerId) {
    ElMessage.warning('请选择审核人')
    return
  }
  if (!form.resultIds) {
    ElMessage.warning('请输入结果编号列表')
    return
  }

  const ids = form.resultIds.split(',').map(s => s.trim()).filter(Boolean)

  loading.value = true
  try {
    const payload = {
      reviewerId: form.reviewerId,
      resultIds: ids,
      comment: form.comment
    }
    const res = await batchReview(payload)
    result.value = res.data
    ElMessage.success(`批量审核完成，通过 ${approvedCount.value} 项，跳过 ${skippedCount.value} 项`)
  } catch (e) {
    ElMessage.error(e.message || '批量审核失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadAccounts())
</script>

<style scoped>
.batch-review-page {
  max-width: 720px;
}
.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
