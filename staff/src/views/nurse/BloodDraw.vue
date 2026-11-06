<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>采血与标本采集</h2>
        <p class="desc">静脉采血、留取尿液标本，贴条码，分类整理</p>
      </div>
      <div class="page-hd-right" v-if="polling">
        <span class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 自动刷新中
        </span>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="heart" :size="16" style="margin-right:6px;color:var(--blue)" /> 采血操作</span>
          <span class="tag tag-blue">护士</span>
        </div>
        <div class="card-bd">
          <div class="form-grid form-2">
            <div class="field">
              <label>选择患者</label>
              <select v-model="selectedExamId" class="fselect" @change="onPatientSelect">
                <option value="" disabled>─ 请选择已签到患者 ─</option>
                <option v-for="r in clinic.checkedInList" :key="r.id" :value="r.id">
                  {{ getPatientName(r) }} — {{ r.examNo }}
                </option>
              </select>
            </div>
            <div class="field">
              <label>操作护士</label>
              <input value="小王" class="finput" readonly />
            </div>
          </div>
          <div class="divider"></div>
          <div class="section-label">标本采集</div>
          <div class="field">
            <div style="display:flex;gap:12px;padding-top:4px">
              <label class="checkbox-item" :class="{ checked: collectBlood }">
                <input type="checkbox" v-model="collectBlood" style="display:none" />
                <SvgIcon :name="collectBlood?'check':'plus'" :size="14" style="margin-right:4px" />
                已采血（静脉血）
              </label>
              <label class="checkbox-item" :class="{ checked: collectUrine }">
                <input type="checkbox" v-model="collectUrine" style="display:none" />
                <SvgIcon :name="collectUrine?'check':'plus'" :size="14" style="margin-right:4px" />
                已留尿（尿液）
              </label>
            </div>
          </div>
          <div v-if="collectBlood" class="field">
            <label>条码号（血液）</label>
            <input v-model="bloodBarcode" class="finput" placeholder="如 BD-20240624-001" />
          </div>
          <div v-if="collectUrine" class="field">
            <label>条码号（尿液）</label>
            <input v-model="urineBarcode" class="finput" placeholder="如 UR-20240624-001" />
          </div>
          <div class="btn-group">
            <button class="btn btn-primary" :disabled="saving || !selectedExamId" @click="handleCollect">
              <SvgIcon name="save" :size="16" /> 保存标本采集记录
            </button>
          </div>
          <transition name="fade">
            <div v-if="msg" :class="['alert', msg.type]">{{ msg.text }}</div>
          </transition>
        </div>
      </div>

      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="list" :size="16" style="margin-right:6px;color:var(--blue)" /> 已采集标本</span>
          <button class="btn btn-secondary btn-sm" @click="loadSpecimens" :disabled="!selectedExamId">
            <SvgIcon name="refresh" :size="14" /> 刷新
          </button>
        </div>
        <div class="card-bd pad-0">
          <template v-if="specimens.length">
            <div class="table-wrap">
              <table class="tbl">
                <thead><tr><th class="col-nowrap">标本编号</th><th class="col-nowrap">类型</th><th class="col-nowrap">条码</th><th class="col-nowrap">状态</th><th class="col-nowrap">采集时间</th></tr></thead>
                <tbody>
                  <tr v-for="s in specimens" :key="s.id">
                    <td style="font-weight:600;font-size:13px">{{ s.specimenNo }}</td>
                    <td><span class="tag" :class="s.specimenType==='BLOOD'?'tag-red':'tag-orange'">{{ s.specimenType==='BLOOD'?'血液':'尿液' }}</span></td>
                    <td>{{ s.barcode || '-' }}</td>
                    <td><span :class="['tag', statusTag(s.status)]">{{ statusLabel(s.status) }}</span></td>
                    <td style="font-size:12px;color:var(--n-400)">{{ s.collectedAt?.replace('T',' ') }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
          <div v-else class="empty-state">
            <SvgIcon name="heart" :size="36" style="color:var(--n-300);margin-bottom:12px" />
            <span class="empty-text">暂无标本记录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { collectSpecimen, getSpecimens, labelSpecimen, getCheckedIn } from '@/api'
import { useClinicStore } from '@/stores/clinic'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const clinic = useClinicStore()
const auth = useAuthStore()
const selectedExamId = ref('')
const collectBlood = ref(false)
const collectUrine = ref(false)
const bloodBarcode = ref('')
const urineBarcode = ref('')
const saving = ref(false)
const specimens = ref([])
const msg = ref(null)
const polling = ref(false)
let pollTimer = null

function startPolling() {
  polling.value = true
  pollTimer = setInterval(() => { loadCheckedIn() }, 10000)
}

function stopPolling() {
  polling.value = false
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

async function loadCheckedIn() {
  try {
    const res = await getCheckedIn(auth.currentBranchId)
    clinic.setCheckedInList(res?.data || [])
    ;(res?.data || []).forEach(r => { if(!clinic.examRecords[r.id]) clinic.addExamRecord(r) })
  } catch {}
}

const getPatientName = (r) => r.patientName || r.record?.patientName || clinic.examRecords[r.id || r.record?.id]?.patientName || '#' + (r.userId || r.record?.userId || r.id)
function statusTag(s) { return { COLLECTED:'tag-orange', LABELED:'tag-blue', SENT:'tag-green', ANALYZED:'tag-gray' }[s] || 'tag-gray' }
function statusLabel(s) { return { COLLECTED:'已采集', LABELED:'已贴条码', SENT:'已送检', ANALYZED:'已化验' }[s] || s }

/** 根据已有标本数量自动生成条码号 */
function generateBarcode(type) {
  const now = new Date()
  const prefix = type === 'BLOOD' ? 'BL' : 'UR'
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const existing = specimens.value.filter(s => s.specimenType === type)
  const seq = String(existing.length + 1).padStart(3, '0')
  return `${prefix}-${y}${m}${d}-${seq}`
}

/** 选中标本类型时自动生成条码 */
watch(collectBlood, (v) => { if (v) bloodBarcode.value = generateBarcode('BLOOD'); else bloodBarcode.value = '' })
watch(collectUrine, (v) => { if (v) urineBarcode.value = generateBarcode('URINE'); else urineBarcode.value = '' })

function onPatientSelect() {
  if (selectedExamId.value) {
    collectBlood.value = false
    collectUrine.value = false
    bloodBarcode.value = ''
    urineBarcode.value = ''
    loadSpecimens()
  }
}

async function loadSpecimens() {
  try {
    const res = await getSpecimens(Number(selectedExamId.value))
    specimens.value = res?.data || []
  } catch { specimens.value = [] }
}

async function handleCollect() {
  if (!selectedExamId.value) { ElMessage.warning('请先选择患者'); return }
  if (!collectBlood && !collectUrine) { ElMessage.warning('请至少选择一种标本类型'); return }
  saving.value = true; msg.value = null
  try {
    const results = []
    // Collect blood specimen
    if (collectBlood) {
      const res = await collectSpecimen({ examRecordId: Number(selectedExamId.value), specimenType: 'BLOOD', collectedBy: 1 })
      if (res?.data) {
        results.push(res.data)
        // If barcode provided, label it
        if (bloodBarcode.value) {
          await labelSpecimen(res.data.id, bloodBarcode.value)
        }
      }
    }
    // Collect urine specimen
    if (collectUrine) {
      const res = await collectSpecimen({ examRecordId: Number(selectedExamId.value), specimenType: 'URINE', collectedBy: 1 })
      if (res?.data) {
        results.push(res.data)
        if (urineBarcode.value) {
          await labelSpecimen(res.data.id, urineBarcode.value)
        }
      }
    }
    ElMessage.success(`标本采集成功，共 ${results.length} 项`)
    msg.value = { type: 'alert-success', text: `标本采集成功（${results.length}项），已贴条码` }
    // Reset form
    collectBlood.value = false; collectUrine.value = false
    bloodBarcode.value = ''; urineBarcode.value = ''
    await loadSpecimens()
  } catch (e) {
    msg.value = { type: 'alert-error', text: '标本采集失败' }
    ElMessage.error('标本采集失败')
  } finally { saving.value = false }
}

onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.section-label { font-size: 12px; font-weight: 600; color: var(--n-500); letter-spacing: 0.5px; margin-bottom: 8px; }
.checkbox-item {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 14px; border: 1px solid var(--n-300); border-radius: var(--radius-pill);
  font-size: 13px; color: var(--n-600); cursor: pointer;
  background: var(--card); user-select: none; transition: all 0.12s;
}
.checkbox-item:hover { border-color: var(--blue); color: var(--blue); }
.checkbox-item.checked { background: var(--blue-light); border-color: var(--blue); color: var(--blue); font-weight: 500; }
.tag-red { background: #fee2e2; color: #dc2626; }
.tag-orange { background: #ffedd5; color: #ea580c; }
.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100% { opacity:1; transform:scale(1) } 50% { opacity:0.5; transform:scale(0.8) } }
</style>
