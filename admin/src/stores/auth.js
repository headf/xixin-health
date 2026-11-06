import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminLogin } from '@/api'

const BRANCH_MAP = {
  1: '朝阳总院', 2: '海淀分院', 3: '浦东分院', 4: '天河分院', 5: '南山分院',
}

const ROLE_MAP = {
  SYSTEM_ADMIN:    { key: 'admin',  label: '系统管理员', home: '/overview' },
  OPERATIONS_ADMIN:{ key: 'admin',  label: '运营管理员', home: '/overview' },
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(null)
  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => user.value?.roles || [])

  const roleConfig = computed(() => {
    for (const code of roles.value) {
      if (ROLE_MAP[code]) return ROLE_MAP[code]
    }
    return null
  })

  const roleLabel = computed(() => roleConfig.value?.label || '管理员')
  const roleHome = computed(() => roleConfig.value?.home || '/login')

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
    const saved = localStorage.getItem('xixin_admin_token')
    if (saved) {
      token.value = saved
      const payload = parseJWT(saved)
      if (payload) {
        const roles = payload.roles || []
        // 已登录但角色不是管理员，清除
        if (!roles.some(r => r === 'SYSTEM_ADMIN' || r === 'OPERATIONS_ADMIN')) {
          localStorage.removeItem('xixin_admin_token')
          return
        }
        user.value = { id: payload.sub, phone: payload.phone, displayName: payload.displayName, roles, branchId: payload.branchId || null }
      }
    }
  }

  async function doLogin(loginId, password) {
    const res = await adminLogin(loginId, password)
    if (res?.data?.accessToken) {
      token.value = res.data.accessToken
      const payload = parseJWT(token.value)
      const roles = payload?.roles || []
      // 只允许管理员角色登录管理端
      if (!roles.some(r => r === 'SYSTEM_ADMIN' || r === 'OPERATIONS_ADMIN')) {
        token.value = ''
        user.value = null
        localStorage.removeItem('xixin_admin_token')
        return false
      }
      localStorage.setItem('xixin_admin_token', token.value)
      user.value = { id: payload.sub, phone: payload.phone, displayName: payload.displayName, roles, branchId: payload.branchId || null }
      return true
    }
    return false
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('xixin_admin_token')
  }

  const branchId = computed(() => user.value?.branchId || null)
  const isBranchAdmin = computed(() => roles.value.includes('OPERATIONS_ADMIN') && !roles.value.includes('SYSTEM_ADMIN') && !!branchId.value)
  const currentBranchId = computed(() => isBranchAdmin.value ? branchId.value : null)
  const branchName = computed(() => branchId.value ? (BRANCH_MAP[branchId.value] || '分院' + branchId.value) : '')
  const displayName = computed(() => user.value?.displayName || roleLabel.value || '管理员')

  return { token, user, isLoggedIn, roles, roleLabel, roleHome, branchId, isBranchAdmin, currentBranchId, branchName, displayName, init, doLogin, logout }
})
