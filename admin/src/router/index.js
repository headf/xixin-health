import { createRouter, createWebHashHistory } from 'vue-router'
import AdminLayout from '@/layout/AdminLayout.vue'
import Login from '@/views/Login.vue'

const Overview = () => import('@/views/Overview.vue')
const ExamItems = () => import('@/views/config/ExamItemPage.vue')
const Packages = () => import('@/views/config/PackagePage.vue')
const Branches = () => import('@/views/config/BranchPage.vue')
const Departments = () => import('@/views/config/DepartmentPage.vue')
const Rules = () => import('@/views/config/RulePage.vue')
const FlowNodes = () => import('@/views/config/FlowNodePage.vue')
const Schedule = () => import('@/views/config/SchedulePage.vue')
const Accounts = () => import('@/views/iam/AccountPage.vue')
const Roles = () => import('@/views/iam/RolePage.vue')
const CheckIn = () => import('@/views/exam/CheckInPage.vue')
const Queue = () => import('@/views/exam/DepartmentQueuePage.vue')
const Templates = () => import('@/views/report/TemplatePage.vue')
const Warnings = () => import('@/views/report/ReportWarningPage.vue')
const RiskUsers = () => import('@/views/management/RiskUserPage.vue')
const Intervention = () => import('@/views/management/InterventionPage.vue')
const BizStats = () => import('@/views/statistics/BusinessStatisticsPage.vue')
const HealthStats = () => import('@/views/statistics/HealthStatisticsPage.vue')
const ExportPage = () => import('@/views/statistics/ExportPage.vue')
const AuditLogs = () => import('@/views/audit/AuditLogPage.vue')
const Appointments = () => import('@/views/exam/AppointmentsPage.vue')
const ContentEdit = () => import('@/views/system/ContentEditPage.vue')
const ContactMessages = () => import('@/views/system/ContactMessagesPage.vue')

function roleHome() {
  const token = localStorage.getItem('xixin_admin_token')
  if (!token) return '/login'
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    if (payload.roles?.includes('SYSTEM_ADMIN') || payload.roles?.includes('OPERATIONS_ADMIN')) return '/overview'
  } catch {}
  return '/login'
}

const routes = [
  { path: '/login', name: 'Login', component: Login },
  {
    path: '/',
    component: AdminLayout,
    children: [
      { path: '', redirect: '/overview' },
      { path: 'overview', component: Overview, meta: { title: '总览' } },
      { path: 'config/exam-items', component: ExamItems, meta: { title: '体检项目' } },
      { path: 'config/packages', component: Packages, meta: { title: '套餐配置' } },
      { path: 'config/branches', component: Branches, meta: { title: '分院管理' } },
      { path: 'config/departments', component: Departments, meta: { title: '科室管理' } },
      { path: 'config/rules', component: Rules, meta: { title: '规则配置' } },
      { path: 'config/flow-nodes', component: FlowNodes, meta: { title: '流程节点' } },
      { path: 'config/schedules', component: Schedule, meta: { title: '排班管理' } },
      { path: 'iam/roles', component: Roles, meta: { title: '角色管理' } },
      { path: 'iam/accounts', component: Accounts, meta: { title: '账号管理' } },
      { path: 'exam/check-in', component: CheckIn, meta: { title: '签到登记' } },
      { path: 'exam/queues', component: Queue, meta: { title: '科室队列' } },
      { path: 'exam/appointments', component: Appointments, meta: { title: '预约列表' } },
      { path: 'report/templates', component: Templates, meta: { title: '报告模板' } },
      { path: 'report/warnings', component: Warnings, meta: { title: '异常预警' } },
      { path: 'management/risk-users', component: RiskUsers, meta: { title: '高危人群' } },      { path: 'management/intervention', component: Intervention, meta: { title: '随访干预' } },
      { path: 'statistics/business', component: BizStats, meta: { title: '业务统计' } },
      { path: 'statistics/health', component: HealthStats, meta: { title: '健康统计' } },
      { path: 'statistics/export', component: ExportPage, meta: { title: '报表导出' } },
      { path: 'audit/logs', component: AuditLogs, meta: { title: '操作日志' } },
      { path: 'system/content-edit', component: ContentEdit, meta: { title: '内容编辑' } },
      { path: 'system/contact-messages', component: ContactMessages, meta: { title: '客服留言' } },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('xixin_admin_token')
  if (to.path !== '/login' && !token) return next('/login')
  if (to.path === '/login' && token) return next(roleHome())
  if (to.path === '/' && token) return next(roleHome())
  next()
})

export default router
