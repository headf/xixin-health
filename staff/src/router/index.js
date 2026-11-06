import { createRouter, createWebHashHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'
import DoctorLayout from '@/layout/DoctorLayout.vue'
import Login from '@/views/Login.vue'
import HomeRedirect from '@/views/DashboardHome.vue'

const NurseCheckin = () => import('@/views/nurse/Checkin.vue')
const NurseVitalSigns = () => import('@/views/nurse/VitalSigns.vue')
const NurseProgress = () => import('@/views/nurse/Progress.vue')
const NurseBloodDraw = () => import('@/views/nurse/BloodDraw.vue')
const NurseSpecimenSend = () => import('@/views/nurse/SpecimenSend.vue')
const NurseKiosk = () => import('@/views/nurse/KioskCheckin.vue')
const NurseDashboard = () => import('@/views/nurse/QueueDashboard.vue')
const NurseAppointmentList = () => import('@/views/nurse/AppointmentList.vue')
const NurseCheckout = () => import('@/views/nurse/ExamCheckout.vue')
const DepartmentDoctor = () => import('@/views/doctor/DepartmentDoctor.vue')
const ReviewPending = () => import('@/views/review/PendingList.vue')
const ReviewReport = () => import('@/views/review/ReportManage.vue')
const ReviewAudit = () => import('@/views/review/ComprehensiveAudit.vue')
const ReviewCritical = () => import('@/views/review/CriticalArchive.vue')
const ReviewSend = () => import('@/views/review/SendReport.vue')
const ReviewFollowUp = () => import('@/views/review/FollowUp.vue')

// 简易 JWT 解析 — 避免在路由守卫中依赖 Pinia
function parseJWT(token) {
  try {
    const enc = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      Array.from(atob(enc)).map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join('')
    )
    return JSON.parse(json)
  } catch { return null }
}

const DOCTOR_ROUTE_ROLES = ['EXAM_DOCTOR_LAB','EXAM_DOCTOR_IMAGING','EXAM_DOCTOR_INTERNAL',
  'EXAM_DOCTOR_SURGERY','EXAM_DOCTOR_OPHTHALM','EXAM_DOCTOR_ENT','EXAM_DOCTOR_DENTAL',
  'EXAM_DOCTOR_ECG','EXAM_DOCTOR_ULTRASOUND','EXAM_DOCTOR_BMD','EXAM_DOCTOR_BLOOD',
  'EXAM_DOCTOR']

const ROLE_HOMES = {
  EXAM_DOCTOR:         '/d/lab',
  EXAM_DOCTOR_LAB:     '/d/lab',
  EXAM_DOCTOR_IMAGING: '/d/imaging',
  EXAM_DOCTOR_INTERNAL:'/d/internal-medicine',
  EXAM_DOCTOR_SURGERY: '/d/surgery',
  EXAM_DOCTOR_OPHTHALM:'/d/ophthalmology',
  EXAM_DOCTOR_ENT:     '/d/ent',
  EXAM_DOCTOR_DENTAL:  '/d/dentistry',
  EXAM_DOCTOR_ECG:     '/d/ecg',
  EXAM_DOCTOR_ULTRASOUND:'/d/ultrasound',
  EXAM_DOCTOR_BMD:     '/d/bmd',
  EXAM_DOCTOR_BLOOD:   '/d/blood',
  REVIEW_DOCTOR:       '/review/pending',
  NURSE:               '/nurse/checkin',
}

function roleHomeFromToken() {
  const token = localStorage.getItem('xixin_staff_token')
  if (!token) return '/login'
  const payload = parseJWT(token)
  if (!payload?.roles?.length) return '/login'
  const userRoles = payload.roles
  // 优先级：医生 > 审核医生 > 护士
  for (const role of DOCTOR_ROUTE_ROLES) {
    if (userRoles.includes(role)) return ROLE_HOMES[role]
  }
  if (userRoles.includes('REVIEW_DOCTOR')) return '/review/pending'
  if (userRoles.includes('NURSE')) return '/nurse/checkin'
  return '/login'
}

const routes = [
  { path: '/login', name: 'Login', component: Login },
  // 护士 & 审核医生 — 侧边栏布局
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', component: HomeRedirect },
      { path: 'nurse/appointments', component: NurseAppointmentList, meta: { role: 'nurse', title: '预约列表' } },
      { path: 'nurse/checkin', component: NurseCheckin, meta: { role: 'nurse', title: '签到登记' } },
      { path: 'nurse/vital-signs', component: NurseVitalSigns, meta: { role: 'nurse', title: '体征测量' } },
      { path: 'nurse/progress', component: NurseProgress, meta: { role: 'nurse', title: '检查进度' } },
      { path: 'nurse/blood-draw', component: NurseBloodDraw, meta: { role: 'nurse', title: '采血管理' } },
      { path: 'nurse/specimen', component: NurseSpecimenSend, meta: { role: 'nurse', title: '标本送检' } },
      { path: 'nurse/kiosk', component: NurseKiosk, meta: { role: 'nurse', title: '科室报到' } },
      { path: 'nurse/dashboard', component: NurseDashboard, meta: { role: 'nurse', title: '排队看板' } },
      { path: 'nurse/checkout', component: NurseCheckout, meta: { role: 'nurse', title: '收尾确认' } },
      { path: 'review/pending', component: ReviewPending, meta: { role: 'review', title: '待审核' } },
      { path: 'review/report', component: ReviewReport, meta: { role: 'review', title: '报告管理' } },
      { path: 'review/comprehensive-audit', component: ReviewAudit, meta: { role: 'review', title: '综合终审' } },
      { path: 'review/critical', component: ReviewCritical, meta: { role: 'review', title: '危急值预警' } },
      { path: 'review/send', component: ReviewSend, meta: { role: 'review', title: '发送报告' } },
      { path: 'review/follow-up', component: ReviewFollowUp, meta: { role: 'review', title: '随访干预' } },
    ]
  },
  // 医生专用布局 — 无侧边栏，全屏工作区
  {
    path: '/d',
    component: DoctorLayout,
    children: [
      { path: 'lab', component: DepartmentDoctor, meta: { role: 'doctor', title: '检验科', departmentId: 1 } },
      { path: 'imaging', component: DepartmentDoctor, meta: { role: 'doctor', title: '影像科', departmentId: 2 } },
      { path: 'internal-medicine', component: DepartmentDoctor, meta: { role: 'doctor', title: '内科', departmentId: 3 } },
      { path: 'surgery', component: DepartmentDoctor, meta: { role: 'doctor', title: '外科', departmentId: 4 } },
      { path: 'ophthalmology', component: DepartmentDoctor, meta: { role: 'doctor', title: '眼科', departmentId: 5 } },
      { path: 'ent', component: DepartmentDoctor, meta: { role: 'doctor', title: '耳鼻喉科', departmentId: 6 } },
      { path: 'dentistry', component: DepartmentDoctor, meta: { role: 'doctor', title: '口腔科', departmentId: 7 } },
      { path: 'ultrasound', component: DepartmentDoctor, meta: { role: 'doctor', title: '超声科', departmentId: 8 } },
      { path: 'ecg', component: DepartmentDoctor, meta: { role: 'doctor', title: '心电图室', departmentId: 9 } },
      { path: 'bmd', component: DepartmentDoctor, meta: { role: 'doctor', title: '骨密度室', departmentId: 10 } },
      { path: 'blood', component: DepartmentDoctor, meta: { role: 'doctor', title: '采血室', departmentId: 11 } },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('xixin_staff_token')

  // 未登录 → 登录页
  if (to.path !== '/login' && !token) {
    return next('/login')
  }

  // 已登录去登录页 → 首页
  if (to.path === '/login' && token) {
    return next(roleHomeFromToken())
  }

  // 根路径 → 首页
  if (to.path === '/' && token) {
    return next(roleHomeFromToken())
  }

  // 角色校验
  if (to.meta.role) {
    const payload = parseJWT(token)
    const roles = payload?.roles || []
    // 医生账户（EXAM_DOCTOR_*）统一映射为 doctor 角色
    let matched = false
    for (const r of roles) {
      if (r === 'EXAM_DOCTOR_LAB' ||
          r === 'EXAM_DOCTOR_IMAGING' || r === 'EXAM_DOCTOR_INTERNAL' ||
          r === 'EXAM_DOCTOR_SURGERY' || r === 'EXAM_DOCTOR_OPHTHALM' ||
          r === 'EXAM_DOCTOR_ENT' || r === 'EXAM_DOCTOR_DENTAL' ||
          r === 'EXAM_DOCTOR_ECG' || r === 'EXAM_DOCTOR_ULTRASOUND' ||
          r === 'EXAM_DOCTOR_BMD' || r === 'EXAM_DOCTOR_BLOOD' ||
          r === 'EXAM_DOCTOR') {
        if (to.meta.role === 'doctor') matched = true
      } else if (r === 'NURSE' && to.meta.role === 'nurse') matched = true
      else if (r === 'REVIEW_DOCTOR' && to.meta.role === 'review') matched = true
    }
    if (!matched) {
      return next(roleHomeFromToken())
    }
  }

  next()
})

export default router
