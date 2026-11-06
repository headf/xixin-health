<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>科室排队</h2>
        <p class="desc">实时查看各科室排队情况</p>
      </div>
      <div class="page-hd-right">
        <span class="tag tag-green" style="font-size:11px;padding:2px 10px">
          <span class="live-dot"></span> 实时刷新中
        </span>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="departments.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
      <div class="empty-title">暂无活跃体检</div>
      <div class="empty-desc">完成签到后即可查看各科室排队情况</div>
    </div>

    <div v-else class="queue-grid">
      <div v-for="d in sortedDepts" :key="d.department_id"
        :class="['queue-card', queueLevel(d)]"
        @click="toggleDetail(d.department_id)">
        <div class="queue-card-body">
          <div class="queue-dept-name">{{ d.department_name }}</div>
          <div class="queue-count">{{ d.waiting }}</div>
          <div class="queue-label">等待人数</div>
          <div class="queue-meta">
            <span v-if="d.fasting_required" class="fasting-tag">需空腹</span>
            <span v-if="d.completed > 0" class="done-tag">{{ d.completed }}已检</span>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="detailDeptId && queueDetail[detailDeptId]" class="card mx-20 mt-16">
        <div class="card-hd">
          <span>{{ getDeptName(detailDeptId) }} — 排队详情</span>
          <span class="tag tag-orange">{{ waitCount(detailDeptId) }}人等待</span>
        </div>
        <div class="card-bd pad-0">
          <div v-if="queueDetail[detailDeptId] && queueDetail[detailDeptId].length" class="table-wrap">
            <table class="tbl">
              <thead><tr><th>排队号</th><th>患者</th><th>状态</th></tr></thead>
              <tbody>
                <tr v-for="row in queueDetail[detailDeptId].filter(q => q.queueStatus === 'WAITING')" :key="row.id">
                  <td style="font-weight:700;font-size:16px;color:var(--n-800)">{{ row.queueNo }}</td>
                  <td>{{ row.patientName || '患者#' + row.examRecordId }}</td>
                  <td><span class="tag tag-orange">等待中</span></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state" style="padding:30px 0">
            <span class="empty-text">暂无排队</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { request } from '../../common/api.js'
export default {
  name: 'PageQueue',
  data() { return { departments: [], queueDetail: {}, detailDeptId: null, loading: true, pollTimer: null } },
  computed: {
    sortedDepts() {
      return [...this.departments].sort((a, b) => {
        if (a.fasting_required && !b.fasting_required) return -1
        if (!a.fasting_required && b.fasting_required) return 1
        return (a.department_name || '').localeCompare(b.department_name || '')
      })
    }
  },
  methods: {
    queueLevel(d) {
      const w = d.waiting || 0
      if (w === 0) return 'level-green'
      if (w <= 3) return 'level-yellow'
      if (w <= 8) return 'level-orange'
      return 'level-red'
    },
    getDeptName(id) {
      const d = this.departments.find(d => d.department_id === id)
      return d ? d.department_name : '未知'
    },
    waitCount(id) {
      const entries = this.queueDetail[id] || []
      return entries.filter(q => q.queueStatus === 'WAITING').length
    },
    toggleDetail(id) {
      if (this.detailDeptId === id) { this.detailDeptId = null; return }
      this.detailDeptId = id
      this.loadDetail(id)
    },
    async loadData() {
      const uid = localStorage.getItem('userId')
      if (!uid) { this.loading = false; return }
      try {
        // 先尝试获取签到后的排队数据
        const data = await request('GET', '/api/v1/exams/queue/my-view?userId=' + uid, null, true)
        if (Array.isArray(data) && data.length > 0) {
          this.departments = data
        } else {
          // 如果 my-view 为空（可能尚未签到），尝试从 my-progress 获取预约和套餐信息
          const progress = await request('GET', '/api/v1/exams/my-progress?userId=' + uid, null, true).catch(() => [])
          if (Array.isArray(progress) && progress.length > 0) {
            // 从 progress 数据中提取科室信息
            const deptMap = {}
            for (const row of progress) {
              const key = row.department_id
              if (!deptMap[key]) {
                deptMap[key] = {
                  department_id: row.department_id,
                  department_name: row.department_name,
                  location: row.location,
                  fasting_required: row.fasting_required,
                  waiting: row.waiting || 0,
                  completed: row.queue_completed || 0
                }
              }
            }
            this.departments = Object.values(deptMap)
          } else {
            // 连 progress 也为空，检查是否有待签到的有效预约
            const apps = await request('GET', '/api/v1/appointments/user/' + uid, null, true).catch(() => [])
            const active = Array.isArray(apps) ? apps.filter(a => a.status !== 'CANCELLED' && a.status !== 'COMPLETED') : []
            if (active.length > 0) {
              // 有有效预约但未签到，提示签到
              this.departments = []  // 保持空，显示提示
            }
          }
        }
      } catch { this.departments = [] }
      finally { this.loading = false }
    },
    async loadDetail(deptId) {
      try {
        const token = localStorage.getItem('accessToken')
        const res = await fetch('/api/v1/exams/queue/with-names/' + deptId, {
          headers: { 'Authorization': 'Bearer ' + token }
        })
        const json = await res.json()
        if (json.code === '0') this.queueDetail[deptId] = json.data || []
      } catch {}
    }
  },
  mounted() {
    this.loadData()
    this.pollTimer = setInterval(() => this.loadData(), 8000)
  },
  unmounted() { if (this.pollTimer) clearInterval(this.pollTimer) }
}
</script>

<style scoped>
.live-dot { display:inline-block; width:6px; height:6px; border-radius:50%; background:#22c55e; margin-right:4px; animation:livePulse 1.5s ease-in-out infinite; vertical-align:middle; }
@keyframes livePulse { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:.5;transform:scale(.8)} }

.queue-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; margin: 0 16px; }
.queue-card { border-radius: 14px; padding: 18px 16px; cursor: pointer; text-align: center; border: 1px solid transparent; transition: all 0.2s; }
.queue-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.06); }
.queue-card.level-green { background: #ecfdf5; border-color: #a7f3d0; }
.queue-card.level-yellow { background: #fffbeb; border-color: #fde68a; }
.queue-card.level-orange { background: #fff7ed; border-color: #fed7aa; }
.queue-card.level-red { background: #fef2f2; border-color: #fecaca; }
.queue-dept-name { font-size: 14px; font-weight: 600; color: #0f172a; margin-bottom: 6px; }
.queue-count { font-size: 30px; font-weight: 800; line-height: 1; margin-bottom: 2px; }
.level-green .queue-count { color: #059669; }
.level-yellow .queue-count { color: #d97706; }
.level-orange .queue-count { color: #ea580c; }
.level-red .queue-count { color: #dc2626; }
.queue-label { font-size: 11px; color: #94a3b8; }
.queue-meta { display: flex; gap: 4px; justify-content: center; margin-top: 6px; flex-wrap: wrap; }
.fasting-tag { font-size: 10px; padding: 1px 6px; border-radius: 4px; background: #fef3c7; color: #b45309; }
.done-tag { font-size: 10px; padding: 1px 6px; border-radius: 4px; background: #f1f5f9; color: #64748b; }
</style>
