<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>操作日志</h2>
    </div>

    <el-card shadow="never" style="margin-bottom:20px;background:#f8fafc">
      <el-form :model="searchForm" inline label-width="80px">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operatorId" placeholder="输入用户名/ID" style="width:150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部操作" style="width:140px" clearable>
            <el-option v-for="(label, key) in typeMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-if="loading" style="text-align:center;padding:40px 0;color:#999">加载中...</div>

    <template v-else-if="data.length === 0">
      <el-empty description="暂无操作日志" />
    </template>

    <div v-else class="log-list">
      <div v-for="row in data" :key="row.id" class="log-item" @click="showDetail(row)">
        <div class="log-icon" :class="iconClass(row)">{{ iconText(row) }}</div>
        <div class="log-body">
          <div class="log-text">
            <strong>{{ row.operatorName || row.operatorId || '系统' }}</strong>
            {{ typeLabel(row.operationType) }}
            <span v-if="targetLabel(row.targetType)">
              <strong>{{ targetLabel(row.targetType) }}</strong>
              <span v-if="row.targetId">（编号: {{ row.targetId }}）</span>
            </span>
          </div>
          <div class="log-meta">
            <span>{{ formatTime(row.operationTime) }}</span>
            <span :class="'log-badge ' + (row.result === 'SUCCESS' ? 'log-success' : 'log-fail')">
              {{ row.result === 'SUCCESS' ? '✓ 成功' : '✗ 失败' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next" :page-sizes="[15, 30, 50, 100]"
      @current-change="handlePageChange"
      style="margin-top:16px;justify-content:center"
    />
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditLogs } from '@/api'

const data = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const searchForm = reactive({ operatorId: '', operationType: '' })

const typeMap = {
  LOGIN: '登录系统', LOGOUT: '退出系统',
  CONFIG_CREATE: '新增配置', CONFIG_UPDATE: '更新配置', CONFIG_STATUS: '修改状态', CONFIG_DISABLE: '禁用配置',
  CREATE: '新增', UPDATE: '更新', DELETE: '删除', QUERY: '查询', EXPORT: '导出报表',
  STAT_QUERY: '查看统计',
  APPOINTMENT_CREATE: '创建预约', APPOINTMENT_CANCEL: '取消预约',
  CHECK_IN: '签到登记',
  RESULT_ENTRY: '录入结果', RESULT_REVIEW: '审核结果',
  REPORT_GENERATE: '生成报告', REPORT_SEND: '发送报告',
  COMPREHENSIVE_AUDIT: '综合终审',
}

const targetMap = {
  business_statistics: '业务统计', health_statistics: '健康统计',
  t_exam_item: '体检项目', t_exam_package: '套餐',
  t_branch: '分院', t_department: '科室',
  t_business_rule: '业务规则', t_flow_node: '流程节点',
  t_report_template: '报告模板',
  sys_user: '用户', sys_role: '角色',
  t_appointment: '预约', t_exam_record: '体检记录',
  t_health_profile: '健康档案', t_order: '订单',
  t_contact_message: '客服留言',
}

function typeLabel(t) { return typeMap[t] || t }
function targetLabel(t) { return targetMap[t] || t }

function iconClass(row) {
  if (row.result !== 'SUCCESS') return 'icon-fail'
  if (['LOGIN', 'LOGOUT'].includes(row.operationType)) return 'icon-auth'
  if (['CREATE', 'CONFIG_CREATE', 'APPOINTMENT_CREATE'].includes(row.operationType)) return 'icon-create'
  if (['DELETE', 'CONFIG_DISABLE'].includes(row.operationType)) return 'icon-delete'
  return 'icon-default'
}
function iconText(row) {
  if (row.result !== 'SUCCESS') return '!'
  if (row.operationType === 'LOGIN') return '→'
  if (row.operationType === 'LOGOUT') return '←'
  if (['CREATE', 'CONFIG_CREATE', 'APPOINTMENT_CREATE'].includes(row.operationType)) return '+'
  if (['DELETE', 'CONFIG_DISABLE'].includes(row.operationType)) return '×'
  return '•'
}
function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

async function load() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchForm.operatorId) params.operatorId = searchForm.operatorId
    if (searchForm.operationType) params.type = searchForm.operationType
    const res = await getAuditLogs(params)
    data.value = res.data?.content || res.data?.records || res.data || []
    total.value = res.data?.totalElements || res.data?.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleSearch() { currentPage.value = 1; load() }
function handleReset() { searchForm.operatorId = ''; searchForm.operationType = ''; currentPage.value = 1; load() }
function handlePageChange(p) { currentPage.value = p; load() }

function showDetail(row) {
  const desc = typeLabel(row.operationType)
    + (row.targetType ? '\n目标: ' + targetLabel(row.targetType) : '')
    + (row.targetId ? '\n编号: ' + row.targetId : '')
    + (row.detail ? '\n说明: ' + row.detail : '')
    + '\n时间: ' + formatTime(row.operationTime)
    + '\n结果: ' + (row.result === 'SUCCESS' ? '成功' : '失败')
  ElMessageBox.alert(desc, '操作详情', { confirmButtonText: '关闭' })
}

onMounted(() => load())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }

.log-list { display: flex; flex-direction: column; gap: 2px; }
.log-item {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 12px 16px; border-radius: 8px;
  cursor: pointer; transition: background 0.15s;
}
.log-item:hover { background: #f5f7fa; }

.log-icon {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 700; flex-shrink: 0;
}
.log-icon.icon-default { background: #e8f8f5; color: #00B894; }
.log-icon.icon-auth   { background: #e8f0fe; color: #165DFF; }
.log-icon.icon-create { background: #dcfce7; color: #10b981; }
.log-icon.icon-delete { background: #ffe8e8; color: #F53F3F; }
.log-icon.icon-fail   { background: #fef3c7; color: #f59e0b; }

.log-body { flex: 1; min-width: 0; }
.log-text { font-size: 14px; color: #1D2129; line-height: 1.5; }
.log-text strong { font-weight: 600; }

.log-meta {
  display: flex; align-items: center; gap: 10px;
  margin-top: 4px; font-size: 12px; color: #86909C;
}
.log-badge { padding: 1px 8px; border-radius: 4px; font-size: 11px; font-weight: 500; }
.log-success { background: #e8f8f5; color: #00B894; }
.log-fail    { background: #fef3c7; color: #d97706; }
</style>
