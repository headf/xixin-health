<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>危急值预警专档</h2>
        <p class="desc">集中管理所有危急值检查结果，跟踪处理情况</p>
      </div>
      <div style="display:flex;gap:12px;align-items:center">
        <div class="stat-card" style="padding:10px 18px;margin:0;width:auto">
          <div class="stat-num" style="font-size:20px;color:var(--red)">{{ criticalList.length }}</div>
          <div class="stat-label">危急值</div>
        </div>
        <button class="btn btn-secondary btn-sm" @click="loadCritical"><SvgIcon name="refresh" :size="14" /> 刷新</button>
        <span v-if="loading" style="font-size:12px;color:var(--n-400)">加载中...</span>
      </div>
    </div>

    <!-- Critical value alert banner -->
    <div v-if="criticalList.length" class="critical-banner">
      <el-icon style="margin-right:8px;font-size:20px"><WarningFilled /></el-icon>
      <span>发现 <strong>{{ criticalList.length }}</strong> 条危急值结果，涉及 <strong>{{ patientCount }}</strong> 名患者，请立即处理！</span>
    </div>

    <!-- Patient group cards -->
    <div v-if="groupedPatients.length" class="critical-list">
      <div v-for="group in groupedPatients" :key="group.patientName" :class="['card', 'critical-group', group.levelClass]">
        <div class="card-hd">
          <div class="patient-info">
            <span class="patient-avatar">{{ group.patientName[0] }}</span>
            <div>
              <span class="patient-name">{{ group.patientName }}</span>
              <span class="exam-no">{{ group.examNo }}</span>
            </div>
          </div>
          <div style="display:flex;gap:8px;align-items:center">
            <span class="tag tag-red">{{ group.items.length }}项危急</span>
            <button class="btn btn-sm btn-ghost" @click="group.expanded = !group.expanded">
              <SvgIcon :name="group.expanded ? 'chevron-up' : 'chevron-down'" :size="14" />
            </button>
          </div>
        </div>
        <div v-show="group.expanded" class="card-bd pad-0">
          <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th class="col-nowrap">检查项目</th><th>结果值</th><th class="col-nowrap">参考范围</th><th class="col-nowrap">判定</th><th class="col-nowrap">审核状态</th><th class="col-nowrap">录入时间</th></tr></thead>
              <tbody>
                <tr v-for="item in group.items" :key="item.id" class="critical-row">
                  <td class="col-nowrap">{{ item.itemName || clinic.itemNames[item.itemId] }}</td>
                  <td>
                    <span class="critical-value">{{ formatValue(item) }}</span>
                  </td>
                  <td class="col-nowrap">{{ formatRange(item) }}</td>
                  <td><span class="tag tag-red">危急值</span></td>
                  <td>
                    <span :class="['tag', item.reviewStatus==='APPROVED'?'tag-green':'tag-orange']">
                      {{ item.reviewStatus === 'APPROVED' ? '已审核' : '待审核' }}
                    </span>
                  </td>
                  <td style="font-size:12px;color:var(--n-400)" class="col-nowrap">{{ item.createdAt?.replace('T',' ') || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="card">
      <div class="empty-state">
        <SvgIcon name="check" :size="48" style="color:var(--green-dark);margin-bottom:12px" />
        <span class="empty-text">✅ 暂无危急值结果，所有检查数据均在正常范围内</span>
      </div>
    </div>

    <!-- Clinical guidance -->
    <div class="card" style="margin-top:16px">
      <div class="card-hd"><span><SvgIcon name="info" :size="16" style="margin-right:6px;color:var(--blue)" /> 危急值处理指引</span></div>
      <div class="card-bd">
        <div class="guide-steps">
          <div class="guide-step"><span class="step-badge-red">1</span> <strong>确认结果</strong> — 审核检查结果，确认危急值是否准确</div>
          <div class="guide-step"><span class="step-badge-red">2</span> <strong>通知医生</strong> — 联系主检医生或临床科室，告知危急值详情</div>
          <div class="guide-step"><span class="step-badge-red">3</span> <strong>登记档案</strong> — 在报告中标记危急值，写入诊断建议</div>
          <div class="guide-step"><span class="step-badge-red">4</span> <strong>跟踪复查</strong> — 建议患者尽快复查，持续跟踪指标变化</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCriticalResults } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

const clinic = useClinicStore()
const criticalList = ref([])
const loading = ref(false)

const patientCount = computed(() => {
  const names = new Set(criticalList.value.map(r => r.patientName))
  return names.size
})

/** Group critical results by patient */
const groupedPatients = computed(() => {
  const groups = {}
  for (const r of criticalList.value) {
    const key = r.patientName + '|' + r.examNo
    if (!groups[key]) {
      groups[key] = {
        patientName: r.patientName,
        examNo: r.examNo,
        examRecordId: r.examRecordId,
        items: [],
        expanded: true,
        levelClass: r.abnormalLevel === 'CRITICAL' ? 'level-critical' : 'level-abnormal'
      }
    }
    groups[key].items.push(r)
  }
  return Object.values(groups)
})

function formatValue(r) {
  if (r.numericValue != null) return r.numericValue + (r.unit || '')
  if (r.textValue) return r.textValue.substring(0, 30)
  return '-'
}

function formatRange(r) {
  if (r.referenceMin != null && r.referenceMax != null) {
    return `${r.referenceMin} ~ ${r.referenceMax} ${r.unit || ''}`
  }
  if (r.criticalMax != null) return `危急值 > ${r.criticalMax}`
  return '-'
}

async function loadCritical() {
  loading.value = true
  try {
    const res = await getCriticalResults()
    criticalList.value = res?.data || []
    // Enrich clinic store with exam record data
    criticalList.value.forEach(r => {
      clinic.addExamRecord({ id: r.examRecordId, patientName: r.patientName, examNo: r.examNo })
    })
  } catch {
    criticalList.value = []
    ElMessage.error('加载危急值数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadCritical())
</script>

<style scoped>
.critical-list { display: flex; flex-direction: column; gap: 12px; }
.critical-group { border-left: 4px solid var(--n-300); }
.critical-group.level-critical { border-left-color: var(--red); }
.critical-group.level-abnormal { border-left-color: var(--orange); }

.patient-info { display: flex; align-items: center; gap: 10px; }
.patient-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: #fee2e2; color: var(--red);
  display: flex; align-items: center; justify-content: center;
  font-size: 15px; font-weight: 700;
}
.patient-name { font-size: 15px; font-weight: 600; color: var(--n-800); }
.exam-no { font-size: 12px; color: var(--n-400); margin-left: 6px; }

.critical-row td { padding: 10px 16px; }
.critical-row:hover td { background: #fef2f2; }

.critical-value {
  color: var(--red); font-weight: 700; font-size: 15px;
  background: #fee2e2; padding: 2px 10px; border-radius: 4px;
  display: inline-block;
}

.guide-steps { display: flex; flex-direction: column; gap: 10px; }
.guide-step {
  display: flex; align-items: center; gap: 10px;
  font-size: 14px; color: var(--n-600); padding: 6px 0;
}
.step-badge-red {
  width: 26px; height: 26px; border-radius: 50%;
  background: #fee2e2; color: var(--red);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}

@media (max-width: 700px) {
  .critical-value { font-size: 13px; padding: 1px 6px; }
}

.critical-banner {
  background: #fee2e2; color: #dc2626; padding: 12px 20px;
  border-radius: 12px; margin-bottom: 20px;
  display: flex; align-items: center; font-size: 14px;
  border: 1px solid #fecaca;
}
</style>
