<template>
  <div class="ub-layout">
    <aside class="ub-sidebar">
      <div class="ub-sidebar-hd">
        <div class="ub-logo">熙心健康</div>
        <div class="ub-logo-sub">体检管理系统</div>
      </div>
      <nav class="ub-nav">
        <div class="ub-nav-group">导航菜单</div>
        <div v-for="item in menuItems" :key="item.path" :class="['ub-nav-item', { active: currentPath === item.path }]" @click="navigate(item.path)">
          <span class="ub-nav-icon" v-html="navIcon(item.icon)"></span>
          <span class="ub-nav-label">{{ item.label }}</span>
          <span v-if="item.icon === 'notice' && unreadCount > 0" class="ub-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </div>
      </nav>
      <div class="ub-sidebar-ft">
        <div class="ub-user-area" @click="navigate('/mine')">
          <div class="ub-user-avatar">{{ (userDisplayName || '?').slice(0, 1) }}</div>
          <div class="ub-user-info">
            <div class="ub-user-name">{{ userDisplayName || '用户' }}</div>
            <div class="ub-user-role">{{ userPhone || '未绑定手机' }}</div>
            <div class="ub-user-dept">熙心健康</div>
          </div>
        </div>
        <button class="ub-icon-btn" @click="handleLogout" title="退出登录">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </aside>
    <div class="ub-main">
      <header class="ub-topbar">
        <div class="ub-topbar-left">
          <span class="ub-topbar-title">{{ currentTitle }}</span>
        </div>
        <div class="ub-topbar-right">
          <span class="ub-status-tag">系统正常运行</span>
        </div>
      </header>
      <main class="ub-content">
        <router-view />
      </main>
    </div>

    <transition name="toast-slide">
      <div v-if="toastVisible" class="user-toast" @click="handleToastClick">
        <span style="font-size:18px;margin-right:8px">🔔</span>
        <div style="flex:1;min-width:0">
          <div class="user-toast-title">{{ toastTitle }}</div>
          <div class="user-toast-text">{{ toastMsg }}</div>
        </div>
      </div>
    </transition>

    <div v-if="detailVisible" class="notif-overlay" @click.self="detailVisible = false">
      <div class="notif-card">
        <div class="notif-card-hd">
          <span>{{ detailTitle }}</span>
          <button class="notif-close" @click="detailVisible = false">&times;</button>
        </div>
        <div class="notif-card-bd">
          <div class="notif-time">{{ detailTime }}</div>
          <div class="notif-content">{{ detailContent }}</div>
        </div>
        <div class="notif-card-ft">
          <button class="btn btn-primary btn-block" @click="goNotifications">查看全部通知</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { isLoggedIn, clearToken } from '../common/api.js'
let notifInterval = null

export default {
  name: 'SidebarLayout',
  data() {
    return {
      menuItems: [
        { icon: 'home', label: '首页', path: '/' },
        { icon: 'package', label: '体检套餐', path: '/packages' },
        { icon: 'calendar', label: '我的预约', path: '/appointments' },
        { icon: 'order', label: '订单管理', path: '/orders' },
        { icon: 'list', label: '排队导诊', path: '/queue' },
        { icon: 'map', label: '体检导诊', path: '/guide' },
        { icon: 'report', label: '体检报告', path: '/reports' },
        { icon: 'profile', label: '就诊人管理', path: '/profiles' },
        { icon: 'notice', label: '消息通知', path: '/notifications' },
        { icon: 'chat', label: '在线咨询', path: '/consult' },
        { icon: 'mine', label: '个人中心', path: '/mine' },
      ],
      unreadCount: 0,
      lastNotifList: [],
      lastMaxId: -1,
      toastMsg: '',
      toastTitle: '',
      toastNotifIds: [],
      toastVisible: false,
      toastTimer: null,
      detailVisible: false,
      detailTitle: '',
      detailContent: '',
      detailTime: ''
    }
  },
  computed: {
    currentPath() { return this.$route.path },
    currentTitle() { return this.$route.meta?.title || '首页' },
    userPhone() { return localStorage.getItem('userPhone') || '' },
    userDisplayName() { return localStorage.getItem('userDisplayName') || '' }
  },
  methods: {
    showToast(title, msg, notifIds) {
      this.toastTitle = title
      this.toastMsg = msg
      this.toastNotifIds = notifIds || []
      this.toastVisible = true
      if (this.toastTimer) clearTimeout(this.toastTimer)
      this.toastTimer = setTimeout(() => { this.toastVisible = false }, 6000)
    },
    async handleToastClick() {
      this.toastVisible = false
      if (this.toastTimer) clearTimeout(this.toastTimer)
      // Find the full notification data for the first new notification
      const firstId = this.toastNotifIds[0]
      if (firstId && this.lastNotifList.length > 0) {
        const notif = this.lastNotifList.find(n => n.id === firstId)
        if (notif) {
          this.detailTitle = notif.title || ''
          this.detailContent = notif.content || ''
          const d = notif.createdAt ? new Date(notif.createdAt) : null
          this.detailTime = d ? `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` : ''
        }
      }
      // Mark as read
      for (const id of this.toastNotifIds) {
        try { await fetch('/api/v1/notifications/' + id + '/read', { method: 'PATCH' }) } catch {}
      }
      this.unreadCount = Math.max(0, this.unreadCount - this.toastNotifIds.length)
      this.detailVisible = true
    },
    goNotifications() { this.detailVisible = false; if (this.$route.path !== '/notifications') this.$router.push('/notifications') },
    async pollNotifications() {
      const uid = localStorage.getItem('userId')
      if (!uid) return
      try {
        const res = await fetch('/api/v1/notifications?userId=' + uid)
        const json = await res.json()
        if (json.code === '0' && Array.isArray(json.data)) {
          this.lastNotifList = json.data
          this.unreadCount = json.data.filter(n => !n.readFlag).length
          const currentMax = json.data.length > 0 ? Math.max(...json.data.map(n => n.id || 0)) : 0
          if (this.lastMaxId >= 0 && currentMax > this.lastMaxId) {
            const newOnes = json.data.filter(n => (n.id || 0) > this.lastMaxId && !n.readFlag)
            if (newOnes.length > 0) {
              const ids = newOnes.map(n => n.id)
              const title = newOnes.length === 1 ? newOnes[0].title : newOnes.length + ' 条新消息'
              const msg = newOnes.length === 1 ? (newOnes[0].content ? newOnes[0].content.substring(0, 60) : '') : '点击查看详情'
              this.showToast(title, msg, ids)
            }
          }
          this.lastMaxId = currentMax
        }
      } catch (e) { /* ignore */ }
    },
    navigate(path) { if (this.$route.path !== path) this.$router.push(path) },
    navIcon(name) { return '' },
    handleLogout() { clearToken(); this.$router.replace('/login') }
  },
  mounted() {
    if (!isLoggedIn()) { this.$router.replace('/login'); return }
    if (notifInterval) clearInterval(notifInterval)
    this.pollNotifications()
    notifInterval = setInterval(() => { this.pollNotifications() }, 10000)
  },
  unmounted() { if (notifInterval) { clearInterval(notifInterval); notifInterval = null } }
}
</script>

<style scoped>
.ub-layout { display: flex; height: 100vh; overflow: hidden; }
.ub-sidebar { width: 240px; background: #fff; display: flex; flex-direction: column; flex-shrink: 0; border-right: 1px solid var(--border); }
.ub-sidebar-hd { padding: 20px 20px 16px; border-bottom: 1px solid var(--border); }
.ub-logo { font-size: 18px; font-weight: 700; color: var(--title); }
.ub-logo-sub { font-size: 10px; color: var(--text-secondary); letter-spacing: 1px; margin-top: 2px; }
.ub-nav { flex: 1; overflow-y: auto; padding: 8px 0; }
.ub-nav-group { padding: 12px 20px 6px; font-size: 11px; font-weight: 600; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px; }
.ub-nav-item { display: flex; align-items: center; gap: 10px; padding: 10px 20px; margin: 2px 12px; border-radius: 12px; cursor: pointer; font-size: 14px; color: var(--text); transition: all 0.12s; }
.ub-nav-item:hover { background: var(--n-50); color: var(--title); }
.ub-nav-item.active { background: var(--primary-light); color: var(--primary); font-weight: 600; }
.ub-nav-icon { width: 24px; text-align: center; flex-shrink: 0; }
.ub-nav-label { white-space: nowrap; }
.ub-badge { margin-left: auto; background: var(--red); color: #fff; font-size: 10px; font-weight: 600; min-width: 18px; height: 18px; border-radius: 9px; display: flex; align-items: center; justify-content: center; padding: 0 5px; }
.ub-sidebar-ft { padding: 12px 16px; border-top: 1px solid var(--border); display: flex; align-items: center; gap: 8px; }
.ub-user-area { flex: 1; display: flex; align-items: center; gap: 10px; overflow: hidden; cursor: pointer; }
.ub-user-avatar { width: 34px; height: 34px; border-radius: 50%; background: linear-gradient(135deg, var(--primary) 0%, var(--teal) 100%); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; }
.ub-user-name { font-size: 13px; font-weight: 600; color: var(--title); }
.ub-user-role { font-size: 10px; color: var(--text-secondary); margin-top: 1px; }
.ub-user-dept { font-size: 9px; color: var(--text-placeholder); margin-top: 1px; }
.ub-icon-btn { width: 34px; height: 34px; border: none; border-radius: 12px; background: transparent; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.ub-icon-btn:hover { background: var(--n-50); }
.ub-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: var(--bg); }
.ub-topbar { height: 52px; background: var(--card); border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; padding: 0 24px; flex-shrink: 0; }
.ub-topbar-title { font-size: 15px; font-weight: 600; color: var(--title); }
.ub-topbar-right { display: flex; align-items: center; gap: 12px; }
.ub-status-tag { font-size: 11px; color: var(--teal-dark); background: var(--teal-light); padding: 0 10px; border-radius: 9999px; font-weight: 500; height: 24px; display: flex; align-items: center; }
.ub-content { flex: 1; overflow-y: auto; padding: 24px; }
.user-toast { position: fixed; top: 16px; right: 16px; z-index: 9999; display: flex; align-items: flex-start; gap: 10px; background: #fff; border-radius: 12px; padding: 14px 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.12); cursor: pointer; max-width: 360px; border-left: 4px solid var(--primary); }
.user-toast-title { font-size: 13px; font-weight: 600; color: var(--title); margin-bottom: 2px; }
.user-toast-text { font-size: 12px; color: var(--text-secondary); line-height: 1.4; }
.toast-slide-enter-active { animation: slideInRight 0.3s ease; }
.toast-slide-leave-active { animation: slideInRight 0.25s ease reverse; }
@keyframes slideInRight { from { transform: translateX(120%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }

.notif-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 10000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.notif-card { background: #fff; border-radius: 16px; width: 100%; max-width: 500px; overflow: hidden; box-shadow: 0 8px 32px rgba(0,0,0,0.12); }
.notif-card-hd { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid var(--border); font-size: 15px; font-weight: 600; color: var(--title); }
.notif-close { background: none; border: none; font-size: 22px; color: #94a3b8; cursor: pointer; padding: 0; line-height: 1; }
.notif-card-bd { padding: 16px 20px; }
.notif-time { font-size: 12px; color: #94a3b8; margin-bottom: 8px; }
.notif-content { font-size: 14px; color: var(--title); line-height: 1.6; white-space: pre-wrap; }
.notif-card-ft { padding: 0 20px 16px; }
</style>
