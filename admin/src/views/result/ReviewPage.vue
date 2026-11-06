<template>
  <div class="review-page">
    <el-card>
      <template #header>
        <span>单项审核</span>
      </template>

      <div class="review-form">
        <el-form label-width="120px" label-position="left">
          <el-form-item label="待审结果">
            <el-select
              v-model="selectedResultId"
              placeholder="请选择待审核的结果"
              style="width: 100%"
              clearable
              filterable
              @change="onResultChange"
            >
              <el-option
                v-for="item in pendingList"
                :key="item.id"
                :label="`${item.itemName} (${item.examRecordId})`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="审核人">
            <el-select
              v-model="reviewerId"
              placeholder="请选择审核人"
              style="width: 100%"
              clearable
              filterable
            >
              <el-option
                v-for="acct in reviewerList"
                :key="acct.id || acct.accountId"
                :label="acct.name || acct.loginId"
                :value="acct.id || acct.accountId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="审核结果">
            <el-switch
              v-model="approve"
              active-text="通过"
              inactive-text="驳回"
              :active-value="true"
              :inactive-value="false"
            />
          </el-form-item>

          <el-form-item label="审核意见">
            <el-input
              v-model="comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审核意见（可选）"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
              {{ loading ? '提交中...' : '提交审核' }}
            </el-button>
            <el-button size="large" @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card v-if="reviewResultData" style="margin-top: 20px;">
      <template #header>
        <span>审核结果</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="结果编号">{{ reviewResultData.resultId || selectedResultId }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ approve ? '已通过' : '已驳回' }}</el-descriptions-item>
        <el-descriptions-item label="审核人编号">{{ reviewerId }}</el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ reviewResultData.comment || comment || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingReview, reviewResult, getAccounts } from '@/api'

const pendingList = ref([])
const reviewerList = ref([])
const selectedResultId = ref('')
const reviewerId = ref('')
const approve = ref(true)
const comment = ref('')
const loading = ref(false)
const reviewResultDataData = ref(null)

onMounted(async () => {
  try {
    const [pendingRes, acctsRes] = await Promise.all([
      getPendingReview(),
      getAccounts()
    ])
    pendingList.value = pendingRes.data || []
    reviewerList.value = acctsRes.data || []
  } catch (e) {
    ElMessage.error('加载数据失败')
  }
})

function onResultChange(val) {
  if (!val) {
    reviewResultData.value = null
  }
}

function resetForm() {
  selectedResultId.value = ''
  reviewerId.value = ''
  approve.value = true
  comment.value = ''
  reviewResultData.value = null
}

async function handleSubmit() {
  if (!selectedResultId.value) {
    ElMessage.warning('请选择待审核的结果')
    return
  }
  if (!reviewerId.value) {
    ElMessage.warning('请选择审核人')
    return
  }

  loading.value = true
  try {
    const payload = {
      reviewerId: reviewerId.value,
      approved: approve.value,
      comment: comment.value
    }
    const res = await reviewResultData(selectedResultId.value, payload)
    reviewResultData.value = res.data || { status: 'ok' }
    ElMessage.success('审核已提交')
  } catch (e) {
    ElMessage.error(e.message || '提交审核失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.review-page {
  max-width: 720px;
}
</style>
