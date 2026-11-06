<template>
  <div class="dl-layout">
    <!-- 简洁顶栏 -->
    <header class="dl-topbar">
      <div class="dl-topbar-left">
        <div class="dl-logo">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
          </svg>
          <span class="dl-brand">熙心健康</span>
          <span class="dl-divider">|</span>
          <span class="dl-dept">{{ route.meta?.title || '工作台' }}</span>
          <span v-if="auth.branchName" class="dl-branch-tag">{{ auth.branchName }}</span>
        </div>
      </div>
      <div class="dl-topbar-right">
        <span class="dl-status">所有系统运行正常</span>
        <span class="dl-breadcrumb">熙心健康 / <strong>{{ route.meta?.title || '工作台' }}</strong></span>
        <div class="dl-user" @click="handleLogout">
          <span class="dl-avatar">{{ auth.displayName?.[0] || '?' }}</span>
          <div class="dl-user-info">
            <div class="dl-user-name">
              {{ auth.displayName }}
              <span class="dl-user-tag">{{ auth.roleLabel }}</span>
            </div>
            <span class="dl-user-status">熙心健康 · 点击退出</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 工作区 -->
    <main class="dl-content">
      <router-view v-slot="{ Component, route: r }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="r.path" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

function handleLogout() { auth.logout(); router.push('/login') }
</script>

<style scoped>
.dl-layout { display: flex; flex-direction: column; height: 100vh; overflow: hidden; }

/* ===== Top Bar ===== */
.dl-topbar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  z-index: 10;
}
.dl-topbar-left { display: flex; align-items: center; }
.dl-logo { display: flex; align-items: center; gap: 10px; }
.dl-brand {
  font-size: 16px; font-weight: 700;
  background: linear-gradient(135deg, var(--primary), var(--teal));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.dl-divider { color: var(--border); font-size: 18px; font-weight: 200; }
.dl-dept { font-size: 14px; font-weight: 600; color: var(--title); }
.dl-branch-tag { font-size: 10px; color: #fff; background: var(--primary); padding: 1px 8px; border-radius: 4px; font-weight: 600; }
.dl-user-branch { font-size: 9px; color: #fff; background: var(--primary); padding: 0 5px; border-radius: 3px; font-weight: 600; margin-right: 2px; }
.dl-user-tag { font-size: 9px; color: var(--primary); background: var(--primary-light); padding: 0 6px; border-radius: 3px; font-weight: 600; margin-left: 4px; }


.dl-topbar-right { display: flex; align-items: center; gap: 16px; }
.dl-status {
  font-size: 11px; color: var(--teal-dark);
  background: var(--teal-light);
  padding: 0 12px; border-radius: 9999px;
  font-weight: 500; height: 24px;
  display: flex; align-items: center;
}
.dl-breadcrumb { font-size: 12px; color: var(--text-secondary); display: flex; align-items: center; gap: 4px; }
.dl-breadcrumb strong { color: var(--title); font-weight: 600; }

.dl-user {
  display: flex; align-items: center; gap: 10px;
  padding: 4px 10px 4px 4px;
  border-radius: 9999px;
  cursor: pointer;
  transition: background 0.15s;
}
.dl-user:hover { background: var(--n-50); }
.dl-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--primary-light); color: var(--primary);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}
.dl-user-info { line-height: 1.2; }
.dl-user-name { font-size: 12px; font-weight: 600; color: var(--title); }
.dl-user-status { font-size: 9px; color: var(--text-secondary); }

/* ===== Content ===== */
.dl-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg);
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.12s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .dl-status { display: none; }
  .dl-breadcrumb { display: none; }
  .dl-content { padding: 16px; }
}
</style>
