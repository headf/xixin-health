<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>我的预约</h2>
        <p class="desc">查看和管理您的体检预约记录</p>
      </div>
    </div>

    <div class="tabs" style="display:flex;gap:8px;padding:12px 16px;overflow-x:auto">
      <span v-for="s in statusFilters" :key="s.key" :class="['tab', activeFilter === s.key ? 'tab-active' : '']" @click="activeFilter = s.key; loadAppts()">{{ s.label }}</span>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="appointments.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
      <div class="empty-title">暂无预约</div>
      <div class="empty-desc">选择套餐，立即预约体检</div>
      <button class="btn btn-primary" @click="$router.push({name:'packages'})">去选套餐</button>
    </div>

    <div v-else class="card mx-20">
      <div class="card-bd pad-0">
        <div v-for="a in appointments" :key="a.id" :class="['list-item', statusBarClass(a.status)]" @click="goDetail(a)">
          <div class="list-row">
            <span :class="statusTag(a.status)">{{ statusLabel(a.status) }}</span>
            <span class="text-sm text-secondary">{{ a.appointmentDate || '' }}</span>
          </div>
          <div class="list-row" style="margin-top:8px">
            <span style="font-size:15px;font-weight:500;color:var(--title)">{{ a.packageName || '体检套餐' }}</span>
          </div>
          <div class="list-row" style="margin-top:4px">
            <span class="text-sm text-secondary">{{ a.branchName || '' }}</span>
            <span class="text-sm text-secondary">{{ a.timeSlot || '' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { appointmentApi, APPOINTMENT_STATUS } from '../../common/api.js'
export default {
  name: 'PageAppointments',
  data() { return { appointments: [], loading: true, statusFilters: [
      { key: '', label: '全部' },
      { key: 'CREATED', label: '待支付' },
      { key: 'PAID', label: '已支付' },
      { key: 'CHECKED_IN', label: '已签到' },
      { key: 'COMPLETED', label: '已完成' },
    ], activeFilter: '' } },
  methods: {
    async load() {
      const uid = localStorage.getItem('userId') || '0'
      this.loading = true
      try { this.appointments = await appointmentApi.getByUser(uid, this.activeFilter) } catch {}
      finally { this.loading = false }
    },
    async loadAppts() {
      await this.load()
    },
    goDetail(a) { localStorage.setItem('selAppt', JSON.stringify(a)); this.$router.push({ name: 'appointmentDetail' }) },
    statusLabel(s) { return APPOINTMENT_STATUS[s]?.label || s || '未知' },
    statusTag(s) { const m = { CREATED:'tag tag-orange', PAID:'tag tag-blue', CHECKED_IN:'tag tag-blue', COMPLETED:'tag tag-green', CANCELLED:'tag tag-gray' }; return m[s]||'tag tag-gray' },
    statusBarClass(s) {
      const m = { CREATED:'bar-created', PAID:'bar-paid', CHECKED_IN:'bar-checkedin', COMPLETED:'bar-completed', CANCELLED:'bar-cancelled' }
      return m[s] || ''
    }
  },
  mounted() { this.load() }
}
</script>

<style scoped>
.bar-created { border-left: 4px solid #f59e0b; padding-left: 12px; background: #FFF8F0; }
.bar-paid { border-left: 4px solid var(--primary, #00B894); padding-left: 12px; background: #F0FDF4; }
.bar-checkedin { border-left: 4px solid #165DFF; padding-left: 12px; background: #E8F0FE; }
.bar-completed { border-left: 4px solid #10b981; padding-left: 12px; background: #E8F8F5; }
.bar-cancelled { border-left: 4px solid #C9CDD4; padding-left: 12px; background: #F2F3F5; }
</style>
