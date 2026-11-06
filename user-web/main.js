import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'

// Layout
import SidebarLayout from './layout/SidebarLayout.vue'

// Full-screen pages (no sidebar)
import PageLogin from './pages/login/login.vue'
import PageRegister from './pages/register/register.vue'
import PageForgotPassword from './pages/forgot-password/forgot.vue'

// Sidebar pages
import PageHome from './pages/index/index.vue'
import PageMine from './pages/tabbar/mine/mine.vue'
import PagePackages from './pages/packages/packages.vue'
import PagePackageDetail from './pages/packages/detail.vue'
import PageAppointments from './pages/appointment/list.vue'
import PageAppointmentCreate from './pages/appointment/create.vue'
import PageAppointmentDetail from './pages/appointment/detail.vue'
import PageOrders from './pages/order/list.vue'
import PageOrderDetail from './pages/order/detail.vue'
import PageOrderPayment from './pages/order/payment.vue'
import PageReports from './pages/report/list.vue'
import PageReportDetail from './pages/report/detail.vue'
import PageNotifications from './pages/notification/list.vue'
import PageProfiles from './pages/profile/list.vue'
import PageProfileDetail from './pages/profile/detail.vue'
import PageProfileEdit from './pages/profile/edit.vue'
import PageAbout from './pages/about/index.vue'
import PageContact from './pages/contact/index.vue'
import PageQueue from './pages/queue/index.vue'
import PageGuide from './pages/guide/index.vue'
import PageConsult from './pages/consult/index.vue'
import PageUserAgreement from './pages/agreement/user-agreement.vue'
import PagePrivacyPolicy from './pages/agreement/privacy-policy.vue'

const routes = [
  // Full-screen auth pages (no sidebar)
  { path: '/login', name: 'login', component: PageLogin },
  { path: '/register', name: 'register', component: PageRegister },
  { path: '/forgot-password', name: 'forgotPassword', component: PageForgotPassword },
  { path: '/user-agreement', name: 'userAgreement', component: PageUserAgreement },
  { path: '/privacy-policy', name: 'privacyPolicy', component: PagePrivacyPolicy },

  // All app pages with sidebar layout
  {
    path: '/',
    component: SidebarLayout,
    children: [
      { path: '', name: 'home', component: PageHome, meta: { title: '首页' } },
      { path: 'mine', name: 'mine', component: PageMine, meta: { title: '我的' } },
      { path: 'packages', name: 'packages', component: PagePackages, meta: { title: '体检套餐' } },
      { path: 'package-detail', name: 'packageDetail', component: PagePackageDetail, meta: { title: '套餐详情' } },
      { path: 'appointments', name: 'appointments', component: PageAppointments, meta: { title: '我的预约' } },
      { path: 'appointment-create', name: 'appointmentCreate', component: PageAppointmentCreate, meta: { title: '填写预约' } },
      { path: 'appointment-detail', name: 'appointmentDetail', component: PageAppointmentDetail, meta: { title: '预约详情' } },
      { path: 'orders', name: 'orders', component: PageOrders, meta: { title: '订单管理' } },
      { path: 'order-detail', name: 'orderDetail', component: PageOrderDetail, meta: { title: '订单详情' } },
      { path: 'order-payment', name: 'orderPayment', component: PageOrderPayment, meta: { title: '支付' } },
      { path: 'reports', name: 'reports', component: PageReports, meta: { title: '体检报告' } },
      { path: 'report-detail', name: 'reportDetail', component: PageReportDetail, meta: { title: '报告详情' } },
      { path: 'notifications', name: 'notifications', component: PageNotifications, meta: { title: '消息通知' } },
      { path: 'profiles', name: 'profiles', component: PageProfiles, meta: { title: '就诊人管理' } },
      { path: 'profile-detail', name: 'profileDetail', component: PageProfileDetail, meta: { title: '就诊人详情' } },
      { path: 'profile-edit', name: 'profileEdit', component: PageProfileEdit, meta: { title: '编辑信息' } },
      { path: 'about', name: 'about', component: PageAbout, meta: { title: '关于我们' } },
      { path: 'contact', name: 'contact', component: PageContact, meta: { title: '联系客服' } },
      { path: 'queue', name: 'queue', component: PageQueue, meta: { title: '科室排队' } },
      { path: 'guide', name: 'guide', component: PageGuide, meta: { title: '体检导诊' } },
      { path: 'consult', name: 'consult', component: PageConsult, meta: { title: '在线咨询' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const pub = ['login', 'register', 'forgotPassword', 'userAgreement', 'privacyPolicy', 'about', 'contact']
  // Login/register pages are always accessible
  if (pub.includes(to.name)) return true
  // Protected routes require auth
  if (!(localStorage.getItem('accessToken') || '')) {
    return '/login'
  }
  return true
})

import App from './App.vue'

const app = createApp(App)
app.use(router)
app.mount('#app')
