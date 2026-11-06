<template>
  <div class="mine-page fade-in">
    <div class="card mx-20 mb-16 text-center" style="padding:28px 20px">
      <div style="width:64px;height:64px;border-radius:32px;background:#f1f5f9;color:#64748b;display:flex;align-items:center;justify-content:center;font-size:24px;font-weight:600;margin:0 auto 12px">{{ phone?phone.slice(-2):'?' }}</div>
      <div style="font-size:18px;font-weight:600;color:var(--title)">{{ phone||'未登录' }}</div>
      <div class="text-sm text-secondary" style="margin-top:2px">随时关注您的健康</div>
      <div style="margin-top:16px;display:flex;gap:20px;justify-content:center">
        <div style="text-align:center;cursor:pointer" @click="$router.push({name:'appointments'})">
          <div style="font-size:22px;font-weight:700;color:var(--title)">{{ stats.appointments }}</div>
          <div class="text-sm text-secondary">预约</div>
        </div>
        <div style="width:1px;background:var(--border)"></div>
        <div style="text-align:center;cursor:pointer" @click="$router.push({name:'reports'})">
          <div style="font-size:22px;font-weight:700;color:var(--title)">{{ stats.reports }}</div>
          <div class="text-sm text-secondary">报告</div>
        </div>
        <div style="width:1px;background:var(--border)"></div>
        <div style="text-align:center;cursor:pointer" @click="$router.push({name:'profiles'})">
          <div style="font-size:22px;font-weight:700;color:var(--title)">{{ stats.profiles }}</div>
          <div class="text-sm text-secondary">档案</div>
        </div>
      </div>
    </div>

    <div class="mine-menu mx-20">
      <div class="mine-menu-item" @click="$router.push({name:'appointments'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/></svg>
        <span class="mm-label">我的预约</span><span class="mm-arrow">›</span>
      </div>
      <div class="mine-menu-item" @click="$router.push({name:'orders'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        <span class="mm-label">订单管理</span><span class="mm-arrow">›</span>
      </div>
      <div class="mine-menu-item" @click="$router.push({name:'reports'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>
        <span class="mm-label">体检报告</span><span class="mm-arrow">›</span>
      </div>
      <div class="mine-menu-item" @click="$router.push({name:'guide'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><circle cx="12" cy="10" r="3"/><path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"/></svg>
        <span class="mm-label">体检导诊</span><span class="mm-arrow">›</span>
      </div>
    </div>

    <div class="mine-menu mx-20">
      <div class="mine-menu-item" @click="$router.push({name:'profiles'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
        <span class="mm-label">就诊人管理</span><span class="mm-arrow">›</span>
      </div>
      <div class="mine-menu-item" @click="$router.push({name:'profileEdit'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        <span class="mm-label">个人信息</span><span class="mm-arrow">›</span>
      </div>
      <div class="mine-menu-item" @click="$router.push({name:'notifications'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        <span class="mm-label">消息通知</span><span class="mm-arrow">›</span>
      </div>
    </div>

    <div class="mine-menu mx-20">
      <div class="mine-menu-item" @click="$router.push({name:'about'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        <span class="mm-label">关于我们</span><span class="mm-arrow">›</span>
      </div>
      <div class="mine-menu-item" @click="$router.push({name:'contact'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <span class="mm-label">联系客服</span><span class="mm-arrow">›</span>
      </div>
    </div>

    <div class="mine-logout"><button @click="handleLogout">退出登录</button></div>
  </div>
</template>

<script>
import { clearToken, isLoggedIn, appointmentApi, reportApi, profileApi } from '../../../common/api.js'
export default {
  name: 'TabbarMine',
  data() { return { stats:{ appointments:0, reports:0, profiles:0 } } },
  computed: { phone() { return localStorage.getItem('userPhone')||'' } },
  methods: {
    handleLogout() { clearToken(); this.$router.replace({name:'home'}) },
    async loadStats() {
      const uid=localStorage.getItem('userId')||'0'
      try{const[apps,reps,profs]=await Promise.allSettled([appointmentApi.getByUser(uid),reportApi.getByUser(uid),profileApi.getByUser(uid)]);this.stats={appointments:apps.value?.length||0,reports:reps.value?.length||0,profiles:profs.value?.length||0}}catch{}
    }
  },
  mounted() { this.loadStats() }
}
</script>
