<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>健康首页</h2>
        <p class="desc">关注健康，从现在开始</p>
      </div>
      <div class="page-hd-right"><span class="tag tag-gray">用户</span></div>
    </div>

    <div class="card mx-20 mb-16" style="padding:20px;border-left:4px solid var(--primary)">
      <div class="flex items-center gap-14">
        <div style="width:48px;height:48px;border-radius:24px;background:#f1f5f9;color:#64748b;display:flex;align-items:center;justify-content:center;font-size:20px;font-weight:600;flex-shrink:0">{{ phone?phone.slice(-2):'我' }}</div>
        <div>
          <div style="font-size:18px;font-weight:600;color:var(--title)">你好，{{ phone||'用户' }}</div>
          <div class="text-sm text-secondary" style="margin-top:2px">健康生活，从体检开始</div>
        </div>
      </div>
    </div>

    <div class="stats-row mx-20">
      <div class="stat-card" @click="$router.push({name:'appointments'})">
        <span class="stat-num">{{ stats.appointments }}</span>
        <span class="stat-label">我的预约</span>
      </div>
      <div class="stat-card" @click="$router.push({name:'reports'})">
        <span class="stat-num">{{ stats.reports }}</span>
        <span class="stat-label">体检报告</span>
      </div>
      <div class="stat-card" @click="$router.push({name:'profiles'})">
        <span class="stat-num">{{ stats.profiles }}</span>
        <span class="stat-label">就诊人</span>
      </div>
    </div>

    <div class="section-title" style="margin:20px 20px 10px">快捷操作</div>
    <div class="home-features">
      <div class="home-feature" @click="$router.push({name:'packages'})">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
        <span class="hf-title">体检套餐</span>
        <span class="hf-sub">精选优质套餐</span>
      </div>
      <div class="home-feature" @click="$router.push({name:'appointmentCreate'})">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/></svg>
        <span class="hf-title">快速预约</span>
        <span class="hf-sub">一键预约体检</span>
      </div>
      <div class="home-feature" @click="$router.push({name:'reports'})">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>
        <span class="hf-title">体检报告</span>
        <span class="hf-sub">查看健康趋势</span>
      </div>
      <div class="home-feature" @click="$router.push({name:'profiles'})">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
        <span class="hf-title">就诊人</span>
        <span class="hf-sub">管理个人档案</span>
      </div>
    </div>

    <div class="section-title" style="margin:0 20px 10px">更多服务</div>
    <div class="mine-menu mx-20">
      <div class="mine-menu-item" @click="$router.push({name:'orders'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        <span class="mm-label">订单管理</span><span class="mm-arrow">›</span>
      </div>
      <div class="mine-menu-item" @click="$router.push({name:'notifications'})">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.8"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        <span class="mm-label">消息通知</span><span class="mm-arrow">›</span>
      </div>
    </div>
    <div style="height:20px"></div>
  </div>
</template>

<script>
import { isLoggedIn } from '../../../common/api.js'
export default {
  name: 'TabbarHome',
  data() { return { stats:{appointments:0,reports:0,profiles:0} } },
  computed: { phone(){return localStorage.getItem('userPhone')||''} },
  mounted() { if(!isLoggedIn()){this.$router.replace({name:'home'})} }
}
</script>
