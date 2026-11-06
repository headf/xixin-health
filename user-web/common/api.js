/* ===== 熙心健康 — API Layer ===== */
const BASE_URL = ''  // 通过 Vite 代理转发到后端

/* ---------- Token 管理 ---------- */
export function getToken() {
  return localStorage.getItem('accessToken') || ''
}

export function setToken(data) {
  localStorage.setItem('accessToken', data.accessToken)
  localStorage.setItem('refreshToken', data.refreshToken)
  localStorage.setItem('tokenExpiresIn', data.expiresInSeconds)
  localStorage.setItem('tokenType', data.tokenType)
  if (data.userId != null) localStorage.setItem('userId', data.userId)
  if (data.phone) localStorage.setItem('userPhone', data.phone)
  // 登录时 loginId 即是手机号或邮箱
  if (data.loginId && !data.phone) localStorage.setItem('userPhone', data.loginId)
  if (data.displayName) localStorage.setItem('userDisplayName', data.displayName)
}

export function clearToken() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('tokenExpiresIn')
  localStorage.removeItem('tokenType')
  localStorage.removeItem('userId')
  localStorage.removeItem('userPhone')
  localStorage.removeItem('userDisplayName')
}

export function isLoggedIn() {
  return !!(getToken())
}

/* ---------- 通用请求 ---------- */
export async function request(method, url, data, needAuth = true) {
  const headers = { 'Content-Type': 'application/json' }
  if (needAuth) {
    const token = getToken()
    if (token) headers['Authorization'] = 'Bearer ' + token
  }
  const opts = { method, headers }
  if (data && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
    opts.body = JSON.stringify(data)
  }
  const res = await fetch(BASE_URL + url, opts)
  const json = await res.json()
  if (json.code === '0') return json.data
  throw { code: json.code, message: json.message || '请求失败' }
}

/* ---------- 状态常量 ---------- */
export const APPOINTMENT_STATUS = {
  CREATED:    { label: '待支付', cls: 'badge badge-warm' },
  PAID:       { label: '已支付', cls: 'badge badge-cool' },
  CHECKED_IN: { label: '已登记', cls: 'badge badge-blue' },
  COMPLETED:  { label: '已完成', cls: 'badge badge-green' },
  CANCELLED:  { label: '已取消', cls: 'badge badge-gray' },
}

export const ORDER_STATUS = {
  PENDING_PAYMENT:  { label: '待支付', cls: 'badge badge-warm' },
  PAID:             { label: '已支付', cls: 'badge badge-green' },
  REFUND_REQUESTED: { label: '退款中', cls: 'badge badge-warm' },
  REFUNDED:         { label: '已退款', cls: 'badge badge-gray' },
  CLOSED:           { label: '已取消', cls: 'badge badge-gray' },
}

/* ---------- 认证 API ---------- */
export const authApi = {
  login(loginId, password) {
    return request('POST', '/api/v1/auth/login', { loginId, password }, false)
  },
  register(phone, displayName, password, email, idCard) {
    const data = { phone, displayName, password }
    if (email) data.email = email
    if (idCard) data.idCard = idCard
    return request('POST', '/api/v1/auth/register', data, false)
  }
}

/* ---------- 套餐 & 分院 API ---------- */
const mockPackages = [
  { id:1, packageCode:'PKG001', packageName:'入职体检套餐', icon:'💼', summary:'涵盖基础项目，快速出报告', category:'BASIC', genderLimit:'ALL', ageRange:'18-60', priceCent:19900, enabled:true },
  { id:2, packageCode:'PKG002', packageName:'全面体检套餐（男）', icon:'🛡️', summary:'全身深度筛查，含肿瘤标志物', category:'FULL', genderLimit:'MALE', ageRange:'18-70', priceCent:59900, enabled:true },
  { id:3, packageCode:'PKG003', packageName:'全面体检套餐（女）', icon:'🌸', summary:'女性专属全面检查，含乳腺宫颈', category:'FULL', genderLimit:'FEMALE', ageRange:'18-70', priceCent:69900, enabled:true },
  { id:4, packageCode:'PKG004', packageName:'老年体检套餐', icon:'🧓', summary:'心脑肝肾重点筛查，关爱长辈', category:'SENIOR', genderLimit:'ALL', ageRange:'50-85', priceCent:89900, enabled:true },
  { id:5, packageCode:'PKG005', packageName:'妇科专项体检', icon:'🌺', summary:'妇科炎症、宫颈乳腺专项筛查', category:'GYN', genderLimit:'FEMALE', ageRange:'20-65', priceCent:39900, enabled:true },
  { id:6, packageCode:'PKG006', packageName:'心血管专项体检', icon:'❤️', summary:'心脏彩超+颈动脉+血脂全套', category:'CARDIO', genderLimit:'ALL', ageRange:'35-80', priceCent:79900, enabled:true },
  { id:7, packageCode:'PKG007', packageName:'孕前体检套餐', icon:'🤰', summary:'优生优育全套，含基因检测', category:'PREMIUM', genderLimit:'FEMALE', ageRange:'22-42', priceCent:129900, enabled:true },
  { id:8, packageCode:'PKG008', packageName:'学生健康体检', icon:'🎒', summary:'基础体格检查，关注成长发育', category:'STUDENT', genderLimit:'ALL', ageRange:'6-22', priceCent:9900, enabled:true },
]

const mockBranches = [
  { id:1, branchCode:'BJ001', branchName:'北京朝阳分院', address:'北京市朝阳区建国路88号', servicePhone:'010-88886666', enabled:true },
  { id:2, branchCode:'SH001', branchName:'上海浦东分院', address:'上海市浦东新区陆家嘴环路100号', servicePhone:'021-66668888', enabled:true },
  { id:3, branchCode:'GZ001', branchName:'广州天河分院', address:'广州市天河区体育西路120号', servicePhone:'020-33338888', enabled:true },
]

/** 为后端数据补充前端展示用的 icon/summary 默认值 */
function fillPackageMeta(list) {
  if (!Array.isArray(list)) return list
  return list.map(p => ({
    ...p,
    icon: p.icon || '📋',
    summary: p.summary || '',
  }))
}

export const packageApi = {
  async getPackages() {
    try { return fillPackageMeta(await request('GET', '/api/v1/packages')) }
    catch(e) { console.warn('API不可用，使用Mock套餐数据'); return mockPackages }
  },
  async getBranches() {
    try { return await request('GET', '/api/v1/branches') }
    catch(e) { console.warn('API不可用，使用Mock分院数据'); return mockBranches }
  }
}

/* ---------- 预约 API ---------- */
const mockAppointments = [
  { id:1, appointmentNo:'APT20260625120001', userId:1, profileId:1, packageId:2, branchId:1, appointmentDate:'2026-07-05', timeSlot:'上午', status:'PAID', packageName:'全面体检套餐（男）', branchName:'北京朝阳分院' },
  { id:2, appointmentNo:'APT20260625120002', userId:1, profileId:1, packageId:6, branchId:2, appointmentDate:'2026-07-12', timeSlot:'下午', status:'CREATED', packageName:'心血管专项体检', branchName:'上海浦东分院' },
]

export const appointmentApi = {
  create(data) { return request('POST', '/api/v1/appointments', data) },
  async getByUser(userId, status) {
    let url = '/api/v1/appointments/user/' + userId
    if (status) url += '?status=' + status
    try { return await request('GET', url) }
    catch(e) { console.warn('API不可用，使用Mock预约数据'); return mockAppointments }
  }
}

/* ---------- 进度监控 API ---------- */
export const examProgressApi = {
  getMyProgress(userId) {
    return request('GET', '/api/v1/exams/my-progress?userId=' + userId)
  }
}

/* ---------- 档案 API ---------- */
const mockProfiles = [
  { id:1, name:'张三', profileType:'SELF', phone:'13800000001' },
  { id:2, name:'李丽', profileType:'FAMILY', phone:'13900000002' },
  { id:3, name:'张小明', profileType:'FAMILY', phone:'13800000001' },
  { id:4, name:'张大海', profileType:'FAMILY', phone:'13700000003' },
]

export const profileApi = {
  async getByUser(userId) {
    try { return await request('GET', '/api/v1/profiles/by-user?userId=' + userId) }
    catch(e) { console.warn('API不可用，使用Mock档案数据'); return mockProfiles }
  },
  getProfileDetail(id) {
    return request('GET', '/api/v1/profiles/' + id)
  },
  save(userId, data) {
    // 映射前端字段到后端 ProfileRequest: name, idCardNo, phone, allergyHistory, medicalHistory, emergencyContactName, emergencyContactPhone, profileType
    const payload = {
      name: data.name,
      idCardNo: data.idCardNo || data.idNumber || '',
      phone: data.phone || '',
      allergyHistory: data.allergyHistory || '',
      medicalHistory: data.medicalHistory || '',
      emergencyContactName: data.emergencyContactName || '',
      emergencyContactPhone: data.emergencyContactPhone || '',
      profileType: data.profileType || 'SELF',
    }
    return request('POST', '/api/v1/profiles/me?ownerUserId=' + userId, payload)
  }
}

/* ---------- 订单 API ---------- */
const mockOrders = [
  { id:1, orderNo:'ORD20260625120001', appointmentId:1, userId:1, amountCent:59900, status:'PAID', packageName:'全面体检套餐（男）', createdAt:'2026-06-25T10:00:00' },
]

export const orderApi = {
  create(appointmentId, amountCent) { return request('POST', '/api/v1/orders', { appointmentId, amountCent }) },
  simulatePayment(orderNo, paymentNo) { return request('POST', '/api/v1/orders/simulate-payment', { orderNo, paymentNo }) },
  async getByUser(userId) {
    try { return await request('GET', '/api/v1/orders/user/' + userId) }
    catch(e) { console.warn('API不可用，使用Mock订单数据'); return mockOrders }
  },
  requestRefund(orderNo, amountCent, reason) { return request('POST', `/api/v1/orders/${orderNo}/refund`, { amountCent, reason }) }
}

/* ---------- 报告 API ---------- */
const mockReports = [
  { id:1, reportNo:'RPT20260620001', userId:1, summary:'各项指标基本正常，血压略偏高，建议低盐饮食。', abnormalStatus:'ABNORMAL', doctorAdvice:'控制盐摄入量，每周运动3次以上，3个月后复查血压。', generatedAt:'2026-06-20' },
]

export const reportApi = {
  async getByUser(userId) {
    try { return await request('GET', '/api/v1/reports/by-user/' + userId) }
    catch(e) { console.warn('API不可用，使用Mock报告数据'); return mockReports }
  },
  async getById(reportId) {
    try { return await request('GET', '/api/v1/reports/' + reportId) }
    catch(e) { console.warn('API不可用，使用Mock报告数据'); return mockReports[0] }
  },
  async getResults(examRecordId) {
    return await request('GET', '/api/v1/results/public/by-exam/' + examRecordId)
  },
  async getDetail(reportId) {
    return await request('GET', '/api/v1/reports/' + reportId + '/detail')
  },
  async getTrendByItem(userId) {
    return await request('GET', '/api/v1/reports/trend/by-item/' + userId)
  },
  async getTrendAnalysis(userId) {
    return await request('GET', '/api/v1/reports/trend/analysis/' + userId)
  },
  /**
   * 下载体检报告 PDF
   * 用 XMLHttpRequest 下载二进制流，创建 Blob 链接触发下载
   */
  downloadPdf(reportId) {
    const token = getToken()
    const xhr = new XMLHttpRequest()
    xhr.open('GET', '/api/v1/reports/' + reportId + '/pdf')
    if (token) xhr.setRequestHeader('Authorization', 'Bearer ' + token)
    xhr.responseType = 'blob'
    xhr.onload = function () {
      if (xhr.status === 200) {
        const blob = xhr.response
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = 'health_report_' + reportId + '.pdf'
        document.body.appendChild(link)
        link.click()
        window.URL.revokeObjectURL(link.href)
        document.body.removeChild(link)
      } else {
        console.error('PDF下载失败', xhr.status, xhr.statusText)
        // 尝试读取错误信息
        if (xhr.response instanceof Blob) {
          xhr.response.text().then(txt => {
            console.error('错误详情:', txt)
          })
        }
        $toast('下载失败 (' + xhr.status + ')', true)
      }
    }
    xhr.onerror = function () {
      $toast('网络错误', true)
    }
    xhr.send()
  }
}

/* ---------- 号源查询 API ---------- */
export const slotApi = {
  /** 查询某分院某日期的可用时段和剩余号源 */
  getAvailable(branchId, date) {
    const params = new URLSearchParams({ branchId })
    if (date) params.set('date', date)
    return request('GET', '/api/v1/slots/available?' + params.toString())
  },
  getCalendar(branchId, startDate, endDate) {
    const params = new URLSearchParams({ branchId, startDate, endDate })
    return request('GET', '/api/v1/slots/calendar?' + params.toString())
  }
}

/* ---------- 通知 API ---------- */
const mockNotifications = [
  { id:1, userId:1, title:'预约确认通知', content:'您7月5日的全面体检预约已确认，请准时前往北京朝阳分院。', notificationType:'APPOINTMENT', readFlag:false, createdAt:'2026-06-25T08:00:00' },
  { id:2, userId:1, title:'报告已生成', content:'您的2026年度体检报告已生成，可在线查看。', notificationType:'REPORT', readFlag:true, createdAt:'2026-06-20T14:00:00' },
]

export const notificationApi = {
  async getByUser(userId) {
    try { return await request('GET', '/api/v1/notifications?userId=' + userId) }
    catch(e) { console.warn('API不可用，使用Mock通知数据'); return mockNotifications }
  },
  markRead(notificationId) { return request('PATCH', '/api/v1/notifications/' + notificationId + '/read') }
}

/* ---------- 工具函数 ---------- */
export const $fmt = v => ((v || 0) / 100).toFixed(2)

export const $cat = c => ({
  BASIC:'基础体检', FULL:'全面体检', SENIOR:'老年体检', GYN:'妇科体检',
  CARDIO:'心血管专项', DIABETES:'糖尿病筛查', LIVER:'肝脏健康',
  KIDNEY:'肾脏健康', STUDENT:'学生体检', PREMIUM:'至尊体检'
}[c] || c || '其他')

export const $gen = g => ({ ALL:'不限性别', MALE:'男', FEMALE:'女' })[g] || g || '不限'

export function $toast(msg, err) {
  const t = document.getElementById('__t') || (() => {
    const d = document.createElement('div'); d.id = '__t'; d.className = 'toast';
    document.body.appendChild(d); return d
  })()
  t.textContent = msg; t.className = 'toast' + (err ? ' error' : '') + ' show'
  clearTimeout(t._tid); t._tid = setTimeout(() => t.classList.remove('show'), 2500)
}

/* ---------- 身份证号工具函数 ---------- */

/**
 * 根据身份证号推算性别（第17位奇男偶女）
 */
export function getIdCardGender(idCard) {
  if (!idCard || idCard.length < 18) return '未知'
  const digit = parseInt(idCard.charAt(16), 10)
  return (digit % 2 === 1) ? '男' : '女'
}

/**
 * 根据身份证号推算年龄
 */
export function getIdCardAge(idCard) {
  if (!idCard || idCard.length < 14) return 0
  try {
    const year = parseInt(idCard.substring(6, 10), 10)
    const month = parseInt(idCard.substring(10, 12), 10) - 1
    const day = parseInt(idCard.substring(12, 14), 10)
    const birth = new Date(year, month, day)
    const now = new Date()
    let age = now.getFullYear() - birth.getFullYear()
    const mDiff = now.getMonth() - birth.getMonth()
    if (mDiff < 0 || (mDiff === 0 && now.getDate() < birth.getDate())) age--
    return age
  } catch (e) {
    return 0
  }
}

export function maskIdCard(idCard, show) {
  if (show) return idCard || '-'
  if (!idCard) return '-'
  if (idCard.length <= 10) {
    const half = Math.floor(idCard.length / 2)
    return idCard.slice(0, Math.max(half, 2)) + '****' + idCard.slice(-Math.max(half, 2))
  }
  return idCard.slice(0, 6) + '****' + idCard.slice(-4)
}

/* ---------- Shared State (reactive data store for cross-page data) ---------- */
export const sharedState = {
  checkedInList: [],
  notifications: [],
  addCheckedIn(item) { this.checkedInList.push(item) },
  setCheckedInList(list) { this.checkedInList = list }
}
