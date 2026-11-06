import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api'

const BRANCH_MAP = {
  1: '朝阳总院', 2: '海淀分院', 3: '浦东分院', 4: '天河分院', 5: '南山分院',
}

const ROLE_MAP = {
  NURSE:               { key: 'nurse',  label: '护士',        home: '/nurse/checkin' },
  EXAM_DOCTOR:         { key: 'doctor', label: '体检医生',     home: '/d/lab' },
  EXAM_DOCTOR_LAB:     { key: 'doctor', label: '检验科医生',   home: '/d/lab',             departmentId: 1 },
  EXAM_DOCTOR_IMAGING: { key: 'doctor', label: '影像科医生',   home: '/d/imaging',         departmentId: 2 },
  EXAM_DOCTOR_INTERNAL:{ key: 'doctor', label: '内科医生',     home: '/d/internal-medicine', departmentId: 3 },
  EXAM_DOCTOR_SURGERY: { key: 'doctor', label: '外科医生',     home: '/d/surgery',          departmentId: 4 },
  EXAM_DOCTOR_OPHTHALM:{ key: 'doctor', label: '眼科医生',     home: '/d/ophthalmology',    departmentId: 5 },
  EXAM_DOCTOR_ENT:     { key: 'doctor', label: '耳鼻喉科医生', home: '/d/ent',              departmentId: 6 },
  EXAM_DOCTOR_DENTAL:  { key: 'doctor', label: '口腔科医生',   home: '/d/dentistry',        departmentId: 7 },
  EXAM_DOCTOR_ECG:     { key: 'doctor', label: '心电图医生',   home: '/d/ecg',              departmentId: 9 },
  EXAM_DOCTOR_ULTRASOUND:{key: 'doctor', label: '超声科医生',  home: '/d/ultrasound',       departmentId: 8 },
  EXAM_DOCTOR_BMD:     { key: 'doctor', label: '骨密度医生',   home: '/d/bmd',              departmentId: 10 },
  EXAM_DOCTOR_BLOOD:   { key: 'doctor', label: '采血室医生',   home: '/d/blood',            departmentId: 11 },
  REVIEW_DOCTOR:       { key: 'review', label: '审核医生',     home: '/review/pending' },
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => user.value?.roles || [])

  const DOCTOR_ROLES = ['EXAM_DOCTOR_LAB', 'EXAM_DOCTOR_IMAGING', 'EXAM_DOCTOR_INTERNAL',
    'EXAM_DOCTOR_SURGERY', 'EXAM_DOCTOR_OPHTHALM', 'EXAM_DOCTOR_ENT', 'EXAM_DOCTOR_DENTAL',
    'EXAM_DOCTOR_ECG', 'EXAM_DOCTOR_ULTRASOUND', 'EXAM_DOCTOR_BMD', 'EXAM_DOCTOR_BLOOD',
    'EXAM_DOCTOR']

  const currentRoleConfig = computed(() => {
    const userRoles = roles.value
    // 优先级：医生 > 审核医生 > 护士
    for (const code of DOCTOR_ROLES) {
      if (userRoles.includes(code)) {
        const entry = ROLE_MAP[code]
        if (entry) return { ...entry, code }
      }
    }
    if (userRoles.includes('REVIEW_DOCTOR')) {
      return { ...ROLE_MAP['REVIEW_DOCTOR'], code: 'REVIEW_DOCTOR' }
    }
    if (userRoles.includes('NURSE')) {
      return { ...ROLE_MAP['NURSE'], code: 'NURSE' }
    }
    return null
  })

  const currentRole = computed(() => currentRoleConfig.value?.key || '')
  const roleLabel = computed(() => currentRoleConfig.value?.label || '')
  const departmentId = computed(() => currentRoleConfig.value?.departmentId || null)
  const roleHome = computed(() => currentRoleConfig.value?.home || '/login')

  /** 当前用户所属分院ID */
  const currentBranchId = computed(() => {
    // 1. JWT中的branchId优先级最高
    if (user.value?.branchId != null) return Number(user.value.branchId)
    // 2. 从手机号推导
    const phone = user.value?.phone || ''
    const m1 = phone.match(/^13800001([1-4])\d{2}$/)
    if (m1) return Number(m1[1])
    const m2 = phone.match(/^1380000([1-4])\d{3}$/)
    if (m2) return Number(m2[1])
    // 3. 默认总院
    return 1
  })

  const branchName = computed(() => {
    // 1. JWT中的branchId优先级最高
    if (user.value?.branchId != null) return BRANCH_MAP[user.value.branchId] || ('分院' + user.value.branchId)
    // 2. 从手机号推导
    const phone = user.value?.phone || ''
    const m1 = phone.match(/^13800001([1-4])\d{2}$/)
    if (m1) return BRANCH_MAP[m1[1]] || ''
    const m2 = phone.match(/^1380000([1-4])\d{3}$/)
    if (m2) return BRANCH_MAP[m2[1]] || ''
    // 3. 固定账号归属
    if (phone === '13800000100') return '总院'
    if (phone === '13800000200') return '总院'
    if (phone === '13800000300') return '总院'
    return ''
  })

  const displayName = computed(() => user.value?.displayName || roleLabel.value || '用户')

  function parseJWT(t) {
    if (!t || t.split('.').length !== 3) return null
    try {
      const enc = t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
      const json = decodeURIComponent(
        Array.from(atob(enc)).map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join('')
      )
      return JSON.parse(json)
    } catch { return null }
  }

  function init() {
    const saved = localStorage.getItem('xixin_staff_token')
    if (saved) {
      token.value = saved
      const payload = parseJWT(saved)
      if (payload) {
        const roles = payload.roles || []
        if (!roles.some(r => ROLE_MAP[r])) {
          localStorage.removeItem('xixin_staff_token')
          return
        }
        user.value = { id: payload.sub, phone: payload.phone, displayName: payload.displayName, roles, branchId: payload.branchId || null }
      }
    }
  }

  async function doLogin(loginId, password) {
    const res = await loginApi(loginId, password)
    if (res?.data?.accessToken) {
      token.value = res.data.accessToken
      const payload = parseJWT(token.value)
      const roles = payload?.roles || []
      if (!roles.some(r => ROLE_MAP[r])) {
        token.value = ''
        user.value = null
        localStorage.removeItem('xixin_staff_token')
        return false
      }
      localStorage.setItem('xixin_staff_token', token.value)
      user.value = { id: payload.sub, phone: payload.phone, displayName: payload.displayName, roles, branchId: payload.branchId || null }
      return true
    }
    return false
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('xixin_staff_token')
  }

  return { token, user, isLoggedIn, roles, currentRole, roleLabel, displayName, departmentId, currentBranchId, branchName, roleHome, init, doLogin, logout, parseJWT }
})
