<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-hd">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0">
          <path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
        </svg>
        <div style="display:flex;flex-direction:column;gap:1px">
          <span class="logo-main">熙心健康</span>
          <span class="logo-sub">体检管理系统</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <template v-for="group in menuGroups" :key="group.label">
          <div class="nav-group">{{ group.label }}</div>
          <div
            v-for="item in group.items"
            :key="item.path"
            :class="['nav-item', { active: route.path === item.path }]"
            @click="router.push(item.path)"
            role="button"
            tabindex="0"
            @keydown.enter="router.push(item.path)"
          >
            <span v-if="item.label === '客服留言'" style="position:relative;display:flex;align-items:center;gap:6px;width:100%">
              <el-icon v-if="item.icon" :size="17"><component :is="item.icon" /></el-icon>
              <span class="nav-label">{{ item.label }}</span>
              <span v-if="unreadMessages > 0" class="badge-dot">{{ unreadMessages > 99 ? '99+' : unreadMessages }}</span>
            </span>
            <template v-else>
              <el-icon v-if="item.icon" :size="17"><component :is="item.icon" /></el-icon>
              <span class="nav-label">{{ item.label }}</span>
            </template>
          </div>
        </template>
      </nav>

      <div class="sidebar-ft">
        <div class="user-area">
          <div class="user-avatar">{{ auth.displayName?.[0] || '?' }}</div>
          <div class="user-info">
            <div class="user-name">{{ auth.displayName }}</div>
            <div class="user-dept">{{ auth.isBranchAdmin ? (auth.branchName || '分院管理员') : '熙心健康' }}</div>
          </div>
        </div>
        <button class="btn-ft-logout" @click="handleLogout" title="退出登录" aria-label="退出登录">
          <el-icon><SwitchButton /></el-icon>
        </button>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <el-icon style="margin-right:6px;color:var(--text-secondary)"><HomeFilled /></el-icon>
          <span class="topbar-title">{{ route.meta?.title || '工作台' }}</span>
        </div>
        <div class="topbar-right">
          <span class="status-tag">所有系统运行正常</span>
        </div>
      </header>
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- Toast for new messages -->
    <transition name="toast-slide">
      <div v-if="toastVisible" class="admin-toast" @click="handleToastClick" role="alert">
        <span style="font-size:18px;margin-right:8px">💬</span>
        <div>
          <div style="font-size:13px;font-weight:600;color:var(--title)">新客服留言</div>
          <div style="font-size:12px;color:var(--text-secondary)">{{ toastMsg }}</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const unreadMessages = ref(0)
const prevUnread = ref(-1)
const toastVisible = ref(false)
const toastMsg = ref('')
let pollTimer = null

const ALL_MENUS = [
  { label: '概览', items: [
    { icon: 'DataAnalysis', label: '总览', path: '/overview' },
  ]},
  { label: '基础配置', items: [
    { icon: 'Setting', label: '体检项目', path: '/config/exam-items' },
    { icon: 'Setting', label: '套餐配置', path: '/config/packages' },
    { icon: 'Setting', label: '分院管理', path: '/config/branches', adminOnly: true },
    { icon: 'Setting', label: '科室管理', path: '/config/departments' },
    { icon: 'Setting', label: '规则配置', path: '/config/rules' },
    { icon: 'Setting', label: '流程节点', path: '/config/flow-nodes' },
    { icon: 'Setting', label: '排班管理', path: '/config/schedules' },
  ]},
  { label: '权限与账号', items: [
    { icon: 'UserFilled', label: '角色管理', path: '/iam/roles', adminOnly: true },
    { icon: 'UserFilled', label: '账号管理', path: '/iam/accounts' },
  ]},
  { label: '体检过程', items: [
    { icon: 'List', label: '预约列表', path: '/exam/appointments' },
    { icon: 'List', label: '签到登记', path: '/exam/check-in' },
    { icon: 'List', label: '科室队列', path: '/exam/queues' },
  ]},
  { label: '报告管理', items: [
    { icon: 'Notebook', label: '报告模板', path: '/report/templates' },
    { icon: 'Notebook', label: '异常预警', path: '/report/warnings' },
  ]},
  { label: '健康干预', items: [
    { icon: 'User', label: '高危人群', path: '/management/risk-users' },
    { icon: 'User', label: '随访干预', path: '/management/intervention' },
  ]},
  { label: '数据统计', items: [
    { icon: 'DataBoard', label: '业务统计', path: '/statistics/business' },
    { icon: 'DataBoard', label: '健康统计', path: '/statistics/health' },
    { icon: 'DataBoard', label: '报表导出', path: '/statistics/export' },
  ]},
  { label: '系统管理', items: [
    { icon: 'Edit', label: '内容编辑', path: '/system/content-edit', adminOnly: true },
    { icon: 'ChatDotSquare', label: '客服留言', path: '/system/contact-messages' },
  ]},
  { label: '操作审计', items: [
    { icon: 'Search', label: '操作日志', path: '/audit/logs' },
  ]},
]

const menuGroups = computed(() => {
  if (!auth.isBranchAdmin) return ALL_MENUS
  return ALL_MENUS.map(group => ({
    ...group,
    items: group.items.filter(item => !item.adminOnly)
  })).filter(group => group.items.length > 0)
})

async function pollMessages() {
  const token = localStorage.getItem('xixin_admin_token')
  if (!token) return
  try {
    const res = await axios.get('/api/v1/admin/contact-messages', {
      headers: { Authorization: 'Bearer ' + token }
    })
    const data = res.data?.data || []
    const unread = data.filter(m => !m.is_read).length
    if (prevUnread.value >= 0 && unread > prevUnread.value) {
      const diff = unread - prevUnread.value
      toastMsg.value = `有 ${diff} 条新留言待回复`
      toastVisible.value = true
      setTimeout(() => { toastVisible.value = false }, 6000)
    }
    prevUnread.value = unread
    unreadMessages.value = unread
  } catch (e) {
    if (e?.response?.status === 401) {
      auth.logout()
      router.push('/login')
    }
  }
}

function handleToastClick() { toastVisible.value = false; router.push('/system/contact-messages') }
function handleLogout() { auth.logout(); router.push('/login') }

onMounted(() => {
  pollMessages()
  pollTimer = setInterval(pollMessages, 10000)
})
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.layout { display: flex; height: 100vh; overflow: hidden; }

/* ===== Sidebar ===== */
.sidebar {
  width: var(--sidebar-w);
  background: var(--card);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-right: 1px solid var(--border);
  z-index: 10;
}
.sidebar-hd {
  padding: var(--space-5) var(--space-5) var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  border-bottom: 1px solid var(--border);
  min-height: 64px;
}
.logo-main {
  font-size: 16px;
  font-weight: var(--fw-bold);
  color: var(--title);
  background: linear-gradient(135deg, var(--primary), var(--success));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.logo-sub { font-size: 10px; color: var(--text-secondary); letter-spacing: 1px; }

.sidebar-nav { flex: 1; overflow-y: auto; padding: 8px 0; }
.nav-group {
  padding: var(--space-3) var(--space-5) 4px;
  font-size: var(--fs-tiny);
  font-weight: var(--fw-medium);
  color: var(--text-secondary);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  margin: 2px 12px;
  border-radius: var(--radius-base);
  cursor: pointer;
  font-size: var(--fs-body);
  color: var(--text);
  transition: all var(--transition-fast);
  user-select: none;
}
.nav-item:hover { background: var(--bg-secondary); color: var(--title); }
.nav-item:active { transform: scale(0.98); }
.nav-item.active {
  background: var(--primary-light);
  color: var(--primary);
  font-weight: var(--fw-semibold);
}
.nav-label { white-space: nowrap; }

.badge-dot {
  margin-left: auto;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  font-weight: var(--fw-semibold);
}

.sidebar-ft {
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.user-area { flex: 1; display: flex; align-items: center; gap: 10px; overflow: hidden; }
.user-avatar {
  width: 34px; height: 34px; border-radius: var(--radius-full);
  background: var(--primary-light); color: var(--primary);
  display: flex; align-items: center; justify-content: center;
  font-size: var(--fs-body); font-weight: var(--fw-bold);
  flex-shrink: 0;
}
.user-info { display: flex; flex-direction: column; gap: 1px; flex: 1; min-width: 0; }
.user-name { font-size: 13px; font-weight: var(--fw-semibold); color: var(--title); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-status-dot { width: 6px; height: 6px; border-radius: var(--radius-full); background: #22c55e; }
.user-dept { font-size: 10px; color: var(--text-secondary); margin-top: 1px; }

.btn-ft-logout {
  width: 34px; height: 34px;
  border: none; border-radius: var(--radius-base);
  background: transparent;
  color: var(--text-secondary);
  font-size: 16px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.btn-ft-logout:hover { background: var(--danger-light); color: var(--danger); }

/* ===== Main ===== */
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: var(--bg); }
.topbar {
  height: var(--topbar-h);
  background: var(--card);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}
.topbar-left { display: flex; align-items: center; }
.topbar-title { font-size: 15px; font-weight: var(--fw-semibold); color: var(--title); }
.topbar-right { display: flex; align-items: center; gap: var(--space-3); }
.status-tag {
  font-size: var(--fs-tiny);
  color: var(--success-dark);
  background: var(--success-light);
  padding: 0 10px;
  border-radius: var(--radius-pill);
  font-weight: var(--fw-medium);
  height: 24px;
  display: flex;
  align-items: center;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-6);
}

/* ===== Toast ===== */
.admin-toast {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: var(--card);
  border-radius: var(--radius-base);
  padding: 14px 16px;
  box-shadow: var(--shadow-dropdown);
  cursor: pointer;
  max-width: 360px;
  border-left: 4px solid var(--warning);
}
.toast-slide-enter-active { animation: slideInRight 0.3s ease; }
.toast-slide-leave-active { animation: slideInRight 0.25s ease reverse; }
@keyframes slideInRight {
  from { transform: translateX(120%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

/* ===== Responsive ===== */
@media (max-width: 1000px) {
  .sidebar { width: 200px; }
}
@media (max-width: 640px) {
  .sidebar { width: 56px; }
  .sidebar-hd { justify-content: center; padding: var(--space-4) var(--space-2); }
  .sidebar-hd > div { display: none; }
  .logo-sub { display: none; }
  .nav-item { justify-content: center; margin: 2px 4px; padding: 10px; }
  .nav-label { display: none; }
  .nav-group { display: none; }
  .sidebar-ft { justify-content: center; padding: var(--space-3) var(--space-2); }
  .user-area { display: none; }
  .status-tag { display: none; }
  .content { padding: var(--space-4); }
}
</style>
