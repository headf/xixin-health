<template>
  <router-view />
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { onMounted } from 'vue'
const auth = useAuthStore()
onMounted(() => auth.init())
</script>

<style>
/* ============================================================
   熙心健康 · 医护工作站 — 设计系统 v3.0
   医疗青绿色主题 · 匹配心电网络系统设计规范
   ============================================================ */
:root {
  /* === 品牌色（医疗青绿） === */
  --primary: #00B894;
  --primary-light: #E8F8F5;
  --primary-lighter: #F0FDF4;
  --primary-dark: #00A080;
  --primary-darker: #008C70;

  --secondary: #0d9488;
  --secondary-light: #ccfbf1;
  --secondary-dark: #0f766e;

  /* === 功能色 === */
  --edit: #165DFF;
  --edit-light: #E8F0FE;
  --success: #10b981;
  --success-light: #d1fae5;
  --success-dark: #059669;
  --warning: #f59e0b;
  --warning-light: #fef3c7;
  --warning-dark: #d97706;
  --danger: #F53F3F;
  --danger-light: #FFE8E8;
  --danger-dark: #dc2626;
  --info: #6366f1;
  --info-light: #e0e7ff;
  --pink: #ec4899;
  --pink-light: #fce7f3;

  /* === 中性色 === */
  --title: #1D2129;
  --text: #4E5969;
  --text-secondary: #86909C;
  --text-placeholder: #C9CDD4;
  --text-disabled: #C9CDD4;
  --card: #ffffff;
  --bg: #F5F7FA;
  --bg-secondary: #F2F3F5;
  --border: #E5E6EB;
  --border-light: #F2F3F5;
  --border-dark: #C9CDD4;
  --table-selected: #E8F8F5;
  --overlay: rgba(29, 33, 41, 0.35);

  /* === 字号 === */
  --fs-hero: 32px;
  --fs-h1: 28px;
  --fs-h2: 22px;
  --fs-h3: 18px;
  --fs-h4: 16px;
  --fs-body: 14px;
  --fs-sm: 13px;
  --fs-xs: 12px;
  --fs-tiny: 11px;

  /* === 字重 === */
  --fw-light: 300;
  --fw-regular: 400;
  --fw-medium: 500;
  --fw-semibold: 600;
  --fw-bold: 700;

  /* === 间距 === */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;

  /* === 圆角 === */
  --radius-sm: 8px;
  --radius-md: 10px;
  --radius-base: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --radius-pill: 9999px;
  --radius-full: 50%;

  /* === 阴影 === */
  --shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-card: 0 4px 20px rgba(0, 0, 0, 0.04);
  --shadow-card-hover: 0 8px 30px rgba(0, 0, 0, 0.06);
  --shadow-dropdown: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-modal: 0 8px 32px rgba(0, 0, 0, 0.12);
  --shadow-input-focus: 0 0 0 3px rgba(0, 184, 148, 0.12);

  --transition-fast: 0.12s ease;
  --transition-base: 0.2s ease;

  --font: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;

  --sidebar-w: 240px;
  --topbar-h: 52px;

  /* 向后兼容 */
  --blue: var(--primary);
  --blue-light: var(--primary-light);
  --blue-hover: var(--primary-dark);
  --green: var(--primary);
  --green-light: var(--primary-light);
  --green-dark: var(--primary-dark);
  --orange: var(--warning);
  --orange-light: var(--warning-light);
  --red: var(--danger);
  --red-light: var(--danger-light);
  --purple: var(--info);
  --purple-light: var(--info-light);
  --teal: var(--secondary);
  --teal-light: var(--secondary-light);
  --teal-dark: var(--secondary-dark);

  --n-50: #F7F8FA;
  --n-100: #F2F3F5;
  --n-200: #E5E6EB;
  --n-300: #C9CDD4;
  --n-400: #A9AEB8;
  --n-500: #86909C;
  --n-600: #4E5969;
  --n-700: #1D2129;
  --n-800: #1D2129;
  --n-900: #1D2129;

  --radius-card: var(--radius-lg);
  --radius-btn: 10px;
}

/* ===== 全局重置 ===== */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
html { -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale; }
body {
  font-family: var(--font); font-size: var(--fs-body); line-height: 1.6;
  color: var(--text); background: var(--bg); min-height: 100vh;
}
a { text-decoration: none; color: inherit; }
img { max-width: 100%; height: auto; }
:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
:focus:not(:focus-visible) { outline: none; }

::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { background: var(--text-secondary); border-radius: 2px; }
::-webkit-scrollbar-track { background: transparent; }

::selection { background: var(--primary); color: #fff; }

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after { animation-duration: 0.01ms !important; animation-iteration-count: 1 !important; transition-duration: 0.01ms !important; }
}

/* ===== 通用组件 ===== */

/* Card */
.card {
  background: var(--card); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card); overflow: hidden;
  transition: box-shadow var(--transition-base);
}
.card:hover { box-shadow: var(--shadow-card-hover); }
.card-hd {
  padding: var(--space-4) var(--space-6) var(--space-3);
  display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid var(--border);
  font-size: 15px; font-weight: var(--fw-semibold); color: var(--title);
}
.card-bd { padding: var(--space-5) var(--space-6) var(--space-6); display: flex; flex-direction: column; gap: var(--space-4); flex: 1; }
.card-bd.pad-0 { padding: var(--space-2) 0 var(--space-6) 0; }
.card-bd.pad-0 .table-wrap { flex: 1; }
.card-bd.pad-0 .empty-state { padding: var(--space-3) var(--space-6) var(--space-6); flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; }

/* Form */
.form-grid { display: grid; gap: var(--space-4); }
.form-2 { grid-template-columns: 1fr 1fr; }
.field { display: flex; flex-direction: column; gap: var(--space-2); }
.field label { font-size: var(--fs-sm); font-weight: var(--fw-medium); color: var(--text); }

.finput, .fselect {
  height: 40px; padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--card);
  font-family: var(--font); font-size: var(--fs-body); color: var(--title);
  outline: none; transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.finput:hover, .fselect:hover { border-color: var(--primary); }
.finput:focus, .fselect:focus { border-color: var(--primary); box-shadow: var(--shadow-input-focus); }
.finput::placeholder { color: var(--text-placeholder); }
.finput:disabled, .fselect:disabled { background: var(--bg-secondary); cursor: not-allowed; }

.fselect {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='8' viewBox='0 0 12 8' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1.5L6 6.5L11 1.5' stroke='%2386909C' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat; background-position: right 12px center;
  padding-right: 36px; cursor: pointer;
}
.ftextarea {
  width: 100%; min-height: 80px; padding: 10px 14px;
  border: 1px solid var(--border); border-radius: var(--radius-md);
  font-family: var(--font); font-size: var(--fs-body); color: var(--title);
  outline: none; resize: vertical; transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.ftextarea:hover, .ftextarea:focus { border-color: var(--primary); box-shadow: var(--shadow-input-focus); }

/* Buttons */
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: var(--space-2);
  height: 40px; padding: 0 20px;
  border: none; border-radius: 10px;
  font-family: var(--font); font-size: var(--fs-body); font-weight: var(--fw-semibold);
  cursor: pointer; white-space: nowrap; transition: all var(--transition-fast);
}
.btn:active { transform: scale(0.98); }
.btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { background: var(--primary-dark); box-shadow: 0 2px 8px rgba(0,184,148,0.2); }
.btn-secondary { background: var(--bg-secondary); color: var(--text); }
.btn-secondary:hover { background: var(--border-dark); }
.btn-danger { background: var(--danger); color: #fff; }
.btn-danger:hover { background: var(--danger-dark); }
.btn-ghost { background: transparent; color: var(--text-secondary); border: 1px solid var(--border); }
.btn-ghost:hover { background: var(--bg-secondary); }
.btn-sm { height: 32px; padding: 0 14px; font-size: var(--fs-sm); }
.btn-group { display: flex; gap: var(--space-2); flex-wrap: wrap; }

/* Table */
.table-wrap { overflow-x: auto; }
table.tbl { width: 100%; border-collapse: collapse; font-size: var(--fs-body); }
table.tbl thead th {
  padding: 12px 16px; text-align: left;
  font-weight: var(--fw-medium); font-size: var(--fs-sm);
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border);
  background: var(--bg);
}
table.tbl tbody td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
}
table.tbl tbody tr:last-child td { border-bottom: none; }
table.tbl tbody tr:hover { background: var(--primary-light); }
table.tbl tbody tr.selected { background: var(--table-selected); }

/* Tags */
.tag {
  display: inline-flex; align-items: center;
  padding: 2px 10px; border-radius: 8px;
  font-size: var(--fs-xs); font-weight: var(--fw-medium);
  white-space: nowrap; border: none;
}
.tag-blue { background: var(--edit-light); color: var(--edit); }
.tag-green { background: var(--primary-light); color: var(--primary-dark); }
.tag-orange { background: var(--warning-light); color: var(--warning-dark); }
.tag-red { background: var(--danger-light); color: var(--danger); }
.tag-purple { background: var(--info-light); color: var(--info); }
.tag-pink { background: var(--pink-light); color: var(--pink); }
.tag-gray { background: var(--bg-secondary); color: var(--text-secondary); }

/* Alert */
.alert { padding: var(--space-3) var(--space-4); border-radius: var(--radius-base); font-size: var(--fs-sm); display: flex; align-items: flex-start; gap: var(--space-2); }
.alert-success { background: var(--primary-light); color: var(--primary-dark); }
.alert-error { background: var(--danger-light); color: var(--danger); }
.alert-warning { background: var(--warning-light); color: var(--warning-dark); }

/* Empty State */
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: var(--space-10) var(--space-5); color: var(--text-secondary); text-align: center; }
.empty-text { font-size: var(--fs-body); color: var(--text-secondary); margin-top: var(--space-2); }

/* Stats */
.stats-row { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: var(--space-5); margin-bottom: var(--space-6); }
.stat-card { background: var(--card); border-radius: var(--radius-lg); padding: var(--space-6); box-shadow: var(--shadow-card); display: flex; align-items: center; gap: var(--space-4); }
.stat-num { font-size: 26px; font-weight: var(--fw-bold); color: var(--title); line-height: 1.2; }
.stat-label { font-size: var(--fs-sm); color: var(--text-secondary); margin-top: 2px; }

/* Two Column */
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-5); }
@media (max-width: 1000px) { .two-col, .form-2 { grid-template-columns: 1fr; } }

/* Transition */
.fade-enter-active, .fade-leave-active { transition: opacity 0.12s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ===== Element Plus 消息弹窗 (简约大气) ===== */
.el-message {
  border-radius: 14px !important;
  box-shadow: 0 8px 40px rgba(0,0,0,0.10), 0 2px 8px rgba(0,0,0,0.04) !important;
  border: none !important;
  padding: 14px 24px !important;
  min-width: 200px !important;
  font-size: 14px !important;
  backdrop-filter: blur(12px);
  background: rgba(255,255,255,0.96) !important;
}
.el-message .el-message__icon { font-size: 18px !important; }
.el-message .el-message__content { font-size: 14px !important; color: var(--title) !important; font-weight: 500 !important; padding: 0 6px !important; }

.el-dialog {
  border-radius: 18px !important;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15), 0 4px 16px rgba(0,0,0,0.04) !important;
  padding: 0 !important;
}
.el-dialog__header { padding: 20px 24px 8px !important; border-bottom: none !important; margin: 0 !important; }
.el-dialog__title { font-size: 17px !important; font-weight: 600 !important; color: var(--title) !important; }
.el-dialog__headerbtn { font-size: 16px !important; top: 20px !important; right: 22px !important; }
.el-dialog__headerbtn:hover { background: var(--bg-secondary) !important; }
.el-dialog__body { padding: 12px 24px 8px !important; color: var(--text) !important; line-height: 1.6 !important; }
.el-dialog__footer { padding: 8px 24px 20px !important; border-top: none !important; display: flex !important; gap: 8px !important; justify-content: flex-end !important; }
.el-overlay { background-color: rgba(0,0,0,0.35) !important; backdrop-filter: blur(2px); }

.el-notification {
  border-radius: 16px !important;
  box-shadow: 0 12px 48px rgba(0,0,0,0.12), 0 4px 12px rgba(0,0,0,0.04) !important;
  border: none !important;
  padding: 20px 24px !important;
  width: 380px !important;
  backdrop-filter: blur(16px);
  background: rgba(255,255,255,0.98) !important;
}
.el-notification__title { font-size: 15px !important; font-weight: 600 !important; color: var(--title) !important; }
.el-notification__content { font-size: 13px !important; color: var(--text) !important; line-height: 1.6 !important; margin-top: 4px !important; }
.el-notification__icon { font-size: 20px !important; }

.el-message-box {
  border-radius: 18px !important;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15), 0 4px 16px rgba(0,0,0,0.04) !important;
  border: none !important;
  padding: 8px 0 !important;
  width: 420px !important;
}
.el-message-box__header { padding: 20px 24px 0 !important; }
.el-message-box__title { font-size: 17px !important; font-weight: 600 !important; color: var(--title) !important; }
.el-message-box__headerbtn { font-size: 16px !important; top: 20px !important; right: 20px !important; }
.el-message-box__content { padding: 16px 24px 8px !important; }
.el-message-box__message { font-size: 14px !important; color: var(--text) !important; line-height: 1.7 !important; }
.el-message-box__btns { padding: 12px 24px 20px !important; display: flex !important; gap: 8px !important; justify-content: flex-end !important; }
</style>
