<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-hd">
        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
        </svg>
        <div class="logo-text">
          <div class="logo-title">熙心健康</div>
          <div class="logo-sub">体检管理系统</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <template v-for="group in menuGroups" :key="group.label">
          <div class="nav-group">{{ group.label }}</div>
          <a v-for="item in group.items" :key="item.path"
             :class="['nav-item', { active: route.path === item.path }]"
             @click="router.push(item.path)"
             role="button"
             tabindex="0"
             @keydown.enter="router.push(item.path)"
             @keydown.space.prevent="router.push(item.path)">
            <SvgIcon :name="item.icon" :size="17" />
            <span class="nav-label">{{ item.label }}</span>
          </a>
        </template>
      </nav>

      <div class="sidebar-ft">
        <div class="user-area">
          <div class="user-avatar">{{ auth.displayName?.[0] || '?' }}</div>
          <div class="user-info">
            <div class="user-name">
              {{ auth.displayName }}
              <span v-if="auth.branchName" class="user-branch-tag">{{ auth.branchName }}</span>
            </div>
            <div class="user-role">{{ auth.roleLabel }}</div>
          </div>
        </div>
        <button class="btn-ft-logout" @click="handleLogout" title="退出登录" aria-label="退出登录">
          <SvgIcon name="logout" :size="16" />
        </button>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <SvgIcon name="home" :size="16" style="color:var(--text-secondary);margin-right:6px" />
          <span class="topbar-title">{{ route.meta?.title || '工作台' }}</span>
        </div>
        <div class="topbar-right">
          <span v-if="auth.branchName" class="branch-tag">{{ auth.branchName }}</span>
          <span class="status-tag">所有系统运行正常</span>
          <span class="breadcrumb">
            <span class="bc-home">熙心健康</span>
            <span class="bc-sep">/</span>
            <span class="bc-current">{{ route.meta?.title || '工作台' }}</span>
          </span>
        </div>
      </header>
      <main class="content">
        <router-view v-slot="{ Component, route: r }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="r.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const DEPARTMENTS = {
  1: { icon: 'list',  label: '检验科',     path: '/doctor/lab' },
  2: { icon: 'heart', label: '影像科',     path: '/doctor/imaging' },
  3: { icon: 'user',  label: '内科',       path: '/doctor/internal-medicine' },
  4: { icon: 'user',  label: '外科',       path: '/doctor/surgery' },
  5: { icon: 'eye',   label: '眼科',       path: '/doctor/ophthalmology' },
  6: { icon: 'eye',   label: '耳鼻喉科',    path: '/doctor/ent' },
  7: { icon: 'edit',  label: '口腔科',     path: '/doctor/dentistry' },
  8: { icon: 'heart', label: '超声科',     path: '/doctor/ultrasound' },
  9: { icon: 'heart', label: '心电图室',    path: '/doctor/ecg' },
  10:{ icon: 'heart', label: '骨密度',     path: '/doctor/bmd' },
  11:{ icon: 'heart', label: '采血室',     path: '/doctor/blood' },
}

const menus = {
  nurse: [
    { label: '日常操作', items: [
      { icon: 'clipboard', label: '签到登记', path: '/nurse/checkin' },
      { icon: 'list', label: '预约列表', path: '/nurse/appointments' },
      { icon: 'heart', label: '体征测量', path: '/nurse/vital-signs' },
      { icon: 'heart', label: '采血管理', path: '/nurse/blood-draw' },
      { icon: 'heart', label: '标本送检', path: '/nurse/specimen' },
      { icon: 'clock', label: '检查进度', path: '/nurse/progress' },
      { icon: 'heart', label: '排队看板', path: '/nurse/dashboard' },
    ]},
    { label: '患者自助', items: [
      { icon: 'phone', label: '科室报到', path: '/nurse/kiosk' },
      { icon: 'check', label: '收尾确认', path: '/nurse/checkout' },
    ]}
  ],
  doctor: [],
  review: [
    { label: '审核工作', items: [
      { icon: 'flag', label: '待审核', path: '/review/pending' },
      { icon: 'document', label: '报告管理', path: '/review/report' },
      { icon: 'check', label: '综合终审', path: '/review/comprehensive-audit' },
      { icon: 'flag', label: '发送报告', path: '/review/send' },
      { icon: 'heart', label: '随访干预', path: '/review/follow-up' },
    ]},
    { label: '预警专档', items: [
      { icon: 'flag', label: '危急值预警', path: '/review/critical' },
    ]}
  ]
}

const menuGroups = computed(() => {
  if (auth.currentRole === 'doctor' && auth.departmentId) {
    const dept = DEPARTMENTS[auth.departmentId]
    if (dept) {
      return [{ label: auth.roleLabel, items: [{ ...dept }] }]
    }
    return []
  }
  return menus[auth.currentRole] || []
})

function handleLogout() { auth.logout(); router.push('/login') }
</script>

<style scoped>
.layout { display: flex; height: 100vh; overflow: hidden; }

/* Sidebar */
.sidebar {
  width: var(--sidebar-w);
  background: var(--card);
  display: flex; flex-direction: column; flex-shrink: 0;
  border-right: 1px solid var(--border);
  z-index: 10;
}
.sidebar-hd {
  padding: var(--space-5) var(--space-5) var(--space-4);
  display: flex; align-items: center; gap: var(--space-3);
  border-bottom: 1px solid var(--border);
  min-height: 64px;
}
.logo-title { font-size: 16px; font-weight: var(--fw-bold); color: var(--title); }
.logo-sub { font-size: 10px; color: var(--text-secondary); letter-spacing: 1px; }

.sidebar-nav { flex: 1; overflow-y: auto; padding: 8px 0; }
.nav-group {
  padding: var(--space-3) var(--space-5) 4px;
  font-size: var(--fs-tiny); font-weight: var(--fw-medium);
  color: var(--text-secondary); letter-spacing: 0.5px; text-transform: uppercase;
}
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 20px; margin: 2px 12px; border-radius: var(--radius-base);
  cursor: pointer; font-size: var(--fs-body); color: var(--text);
  transition: all var(--transition-fast); user-select: none;
}
.nav-item:hover { background: var(--bg-secondary); color: var(--title); }
.nav-item:active { transform: scale(0.98); }
.nav-item.active { background: var(--primary-light); color: var(--primary); font-weight: var(--fw-semibold); }
.nav-label { white-space: nowrap; }

.sidebar-ft {
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--border);
  display: flex; align-items: center; gap: var(--space-2);
}
.user-area { flex: 1; display: flex; align-items: center; gap: 10px; overflow: hidden; }
.user-avatar {
  width: 34px; height: 34px; border-radius: var(--radius-full);
  background: var(--primary-light); color: var(--primary);
  display: flex; align-items: center; justify-content: center;
  font-size: var(--fs-body); font-weight: var(--fw-bold); flex-shrink: 0;
}
.user-info { display: flex; flex-direction: column; gap: 1px; flex: 1; min-width: 0; }
.user-name { font-size: 13px; font-weight: var(--fw-semibold); color: var(--title); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: flex; align-items: center; gap: 6px; }
.user-branch-tag { font-size: 10px; color: #fff; background: var(--primary); padding: 0 6px; border-radius: 4px; font-weight: var(--fw-medium); flex-shrink: 0; }
.user-phone { font-size: 10px; color: var(--text-secondary); }
.user-status-dot { width: 6px; height: 6px; border-radius: var(--radius-full); background: #22c55e; }
.user-role { font-size: 10px; color: var(--text-secondary); margin-top: 1px; }
.user-dept { font-size: 9px; color: var(--text-placeholder); margin-top: 1px; }

.btn-ft-logout {
  width: 34px; height: 34px; border: none; border-radius: var(--radius-base);
  background: transparent; color: var(--text-secondary); font-size: 16px;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast); flex-shrink: 0;
}
.btn-ft-logout:hover { background: var(--danger-light); color: var(--danger); }

/* Main */
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: var(--bg); }
.topbar {
  height: var(--topbar-h); background: var(--card);
  border-bottom: 1px solid var(--border);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; flex-shrink: 0;
}
.topbar-left { display: flex; align-items: center; }
.topbar-title { font-size: 15px; font-weight: var(--fw-semibold); color: var(--title); }
.topbar-right { display: flex; align-items: center; gap: var(--space-3); }
.status-tag { font-size: var(--fs-tiny); color: var(--secondary-dark); background: var(--secondary-light); padding: 0 10px; border-radius: var(--radius-pill); font-weight: var(--fw-medium); height: 24px; display: flex; align-items: center; }
.branch-tag { font-size: var(--fs-tiny); color: var(--primary); background: var(--primary-light); padding: 0 10px; border-radius: var(--radius-pill); font-weight: var(--fw-semibold); height: 24px; display: flex; align-items: center; }
.breadcrumb { font-size: var(--fs-xs); color: var(--text-secondary); display: flex; align-items: center; gap: 6px; }
.bc-sep { color: var(--border-dark); }
.bc-current { color: var(--title); font-weight: var(--fw-medium); }

.content { flex: 1; overflow-y: auto; padding: var(--space-6); }

/* Responsive */
@media (max-width: 1000px) {
  .status-tag { display: none; }
}
@media (max-width: 640px) {
  .sidebar { width: 56px; }
  .sidebar-hd { justify-content: center; padding: var(--space-4) var(--space-2); }
  .logo-text { display: none; }
  .nav-item { justify-content: center; margin: 2px 4px; padding: 10px; }
  .nav-label { display: none; }
  .nav-group { display: none; }
  .sidebar-ft { justify-content: center; padding: var(--space-3) var(--space-2); }
  .user-area { display: none; }
  .content { padding: var(--space-4); }
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.12s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
