<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>体检报告</h2>
        <p class="desc">查看已完成体检的详细报告</p>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="reports.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>
      <div class="empty-title">暂无报告</div>
      <div class="empty-desc">完成体检后报告将在此显示</div>
    </div>

    <div v-else class="card" style="margin:0 20px">
      <div class="card-bd pad-0">
        <div v-for="r in reports" :key="r.id" class="list-item" @click="goDetail(r)">
          <div class="list-row">
            <span style="font-size:15px;font-weight:500;color:var(--title)">{{ r.reportNo || '体检报告' }}</span>
            <span :class="riskTagClass(r.abnormalStatus)">{{ riskLabel(r.abnormalStatus) }}</span>
          </div>
          <div class="list-row" style="margin-top:4px">
            <span style="font-size:12px;color:var(--text-secondary)">编号：{{ r.reportNo || 'RPT-' + String(r.id).padStart(5,'0') }}</span>
            <span style="font-size:12px;color:var(--text-secondary)">{{ r.generatedAt || r.createdAt || '' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reportApi } from '../../common/api.js'

export default {
  name: 'PageReports',
  data() {
    return {
      reports: [],
      loading: true
    }
  },
  methods: {
    async load() {
      const uid = localStorage.getItem('userId') || '0'
      this.loading = true
      try {
        const raw = await reportApi.getByUser(uid)
		      // 同次体检只保留最新报告
		      const map = new Map()
		      for (const r of (raw || [])) {
		        const key = r.examRecordId || r.id
		        if (!map.has(key) || new Date(r.generatedAt || 0) > new Date(map.get(key).generatedAt || 0)) {
		          map.set(key, r)
		        }
		      }
		      this.reports = [...map.values()]
      } catch (e) { /* ignore */ }
      finally { this.loading = false }
    },
    goDetail(r) {
      localStorage.setItem('selReport', JSON.stringify(r))
      this.$router.push({ name: 'reportDetail' })
    },
    riskLabel(l) {
      const m = { NORMAL: '正常', ABNORMAL: '异常', CRITICAL: '危急' }
      return m[l] || '正常'
    },
    riskTagClass(l) {
      const m = { NORMAL: 'tag tag-green', ABNORMAL: 'tag tag-orange', CRITICAL: 'tag tag-red' }
      return m[l] || 'tag tag-green'
    }
  },
  mounted() { this.load() }
}
</script>
