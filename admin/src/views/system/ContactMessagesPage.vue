<template>
  <el-card shadow="never">
    <template #header>
      <div class="page-header">
        <div>
          <h2>
            <el-icon style="margin-right:6px;color:var(--primary)"><ChatDotSquare /></el-icon>
            客服留言
          </h2>
          <p class="page-subtitle">查看并回复用户的咨询留言</p>
        </div>
        <el-button size="small" :loading="loading" @click="load">
          <el-icon style="margin-right:4px"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>

    <!-- Loading -->
    <div v-if="loading && data.length === 0" style="padding:60px 0;text-align:center;color:var(--text-secondary)">
      <el-icon class="is-loading" :size="24" style="margin-bottom:12px"><Loading /></el-icon>
      <div>加载中...</div>
    </div>

    <!-- Empty -->
    <el-empty v-else-if="data.length === 0" description="暂无留言" :image-size="80" />

    <!-- Table -->
    <template v-else>
      <el-table :data="pagedData" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="内容" min-width="200">
          <template #default="{ row }">
            <span class="clickable-cell" @click="handleView(row)">{{ truncate(row.content, 40) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="回复" min-width="160">
          <template #default="{ row }">
            <span v-if="row.reply" class="clickable-cell" @click="handleView(row)" style="color:var(--secondary-dark)">{{ truncate(row.reply, 30) }}</span>
            <span v-else style="color:var(--text-placeholder);font-style:italic">未回复</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.is_read ? 'success' : 'info'" size="small" effect="plain">
              {{ row.is_read ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="留言时间" width="170" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="handleView(row)">查看</el-button>
            <el-button size="small" text type="primary" :disabled="row.is_read" @click="handleMarkRead(row)">
              标为已读
            </el-button>
            <el-button size="small" type="primary" @click="handleReply(row)">回复</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
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
    </template>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复留言" width="480px" :close-on-click-modal="false">
      <div class="reply-msg">
        <strong>{{ currentReply?.name }}</strong> 的留言：
        <p class="reply-msg-content">{{ currentReply?.content }}</p>
      </div>
      <el-input v-model="replyText" type="textarea" :rows="4" placeholder="请输入回复内容..." maxlength="500" show-word-limit />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleSendReply">发送回复</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="留言详情" width="520px">
      <div v-if="detailData" class="detail-wrap">
        <div class="detail-field">
          <span class="detail-label">姓名</span>
          <span class="detail-value">{{ detailData.name }}</span>
        </div>
        <div class="detail-field">
          <span class="detail-label">电话</span>
          <span class="detail-value">{{ detailData.phone }}</span>
        </div>
        <div class="detail-field">
          <span class="detail-label">留言时间</span>
          <span class="detail-value">{{ detailData.created_at }}</span>
        </div>
        <div class="detail-field" style="flex-direction:column;align-items:stretch;gap:6px">
          <span class="detail-label">留言内容</span>
          <div class="detail-box">{{ detailData.content }}</div>
        </div>
        <div class="detail-field" style="flex-direction:column;align-items:stretch;gap:6px" v-if="detailData.reply">
          <span class="detail-label">客服回复</span>
          <div class="detail-box detail-box-reply">{{ detailData.reply }}</div>
        </div>
        <div class="detail-field" v-else>
          <span class="detail-label">客服回复</span>
          <span style="color:var(--text-placeholder);font-style:italic">暂未回复</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" v-if="detailData && !detailData.reply" @click="handleReplyFromDetail">回复此留言</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContactMessages, markMessageRead, replyMessage, deleteMessage } from '@/api'

const data = ref([])

const pageNum = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => data.value.slice((pageNum.value-1)*pageSize.value, pageNum.value*pageSize.value))
const loading = ref(false)
const replyDialogVisible = ref(false)
const replying = ref(false)
const currentReply = ref(null)
const replyText = ref('')
const detailDialogVisible = ref(false)
const detailData = ref(null)
let pollTimer = null

async function load() {
  loading.value = true
  try { const res = await getContactMessages(); data.value = res.data || [] }
  catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleMarkRead(row) {
  try { await markMessageRead(row.id); ElMessage.success('已标记为已读'); await load() }
  catch { ElMessage.error('操作失败') }
}

function handleReply(row) {
  currentReply.value = row
  replyText.value = row.reply || ''
  replyDialogVisible.value = true
}

async function handleSendReply() {
  if (!replyText.value) { ElMessage.warning('请输入回复内容'); return }
  replying.value = true
  try {
    await replyMessage(currentReply.value.id, replyText.value)
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    await load()
  } catch { ElMessage.error('回复失败') }
  finally { replying.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该留言吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteMessage(row.id)
    ElMessage.success('已删除')
    await load()
  } catch {}
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '...' : text
}

function handleView(row) {
  detailData.value = row
  detailDialogVisible.value = true
}

function handleReplyFromDetail() {
  detailDialogVisible.value = false
  handleReply(detailData.value)
}

onMounted(() => { load(); pollTimer = setInterval(() => load(), 10000) })
onBeforeUnmount(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  gap: var(--space-4);
}
.page-header h2 { margin: 0; font-size: var(--fs-h3); font-weight: var(--fw-semibold); display: flex; align-items: center; }
.page-subtitle { font-size: var(--fs-sm); color: var(--text-secondary); margin-top: 2px; }

.reply-msg {
  background: var(--bg); padding: var(--space-4);
  border-radius: var(--radius-base); margin-bottom: var(--space-4);
  font-size: var(--fs-sm); color: var(--text); line-height: 1.6;
}
.reply-msg-content {
  margin-top: var(--space-2);
  padding: var(--space-3); background: var(--card);
  border-radius: var(--radius-sm); border: 1px solid var(--border);
  color: var(--title);
}

.clickable-cell {
  cursor: pointer;
  transition: color 0.12s;
}
.clickable-cell:hover { color: var(--primary) !important; }

.detail-wrap { display: flex; flex-direction: column; gap: 14px; }
.detail-field { display: flex; align-items: center; gap: 12px; }
.detail-label { font-size: 12px; color: var(--text-secondary); width: 70px; flex-shrink: 0; }
.detail-value { font-size: 14px; color: var(--title); font-weight: 500; }
.detail-box {
  background: var(--bg); padding: 14px 16px;
  border-radius: var(--radius-base); font-size: 14px;
  color: var(--text); line-height: 1.7; white-space: pre-wrap;
  border: 1px solid var(--border-light);
}
.detail-box-reply {
  background: var(--primary-lighter);
  border-color: var(--primary-light);
}
</style>
