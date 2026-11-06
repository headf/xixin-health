import request from '@/utils/request'

// ==================== Auth ====================
export const adminLogin = (loginId, password) =>
  request.post('/auth/login', { loginId, password })

// ==================== Overview ====================
export const getBusinessStats = (dimension, branchId) => {
  let url = `/admin/statistics/business?dimension=${dimension}`
  if (branchId) url += `&branchId=${branchId}`
  return request.get(url)
}
export const getHealthStats = (branchId) => {
  let url = '/admin/statistics/health'
  if (branchId) url += `?branchId=${branchId}`
  return request.get(url)
}
export const getWeeklyTrend = (branchId) =>
  request.get('/appointments/weekly-trend', { params: branchId ? { branchId } : {} })
export const getCheckedInRecords = (branchId) =>
  request.get(`/exams/checked-in/${branchId || 1}`)
// 别名 — 统一命名
export const getCheckedIn = getCheckedInRecords
export const getPatients = () =>
  request.get('/patients')

// ==================== Appointments ====================
export const getAppointments = (params) =>
  request.get('/admin/appointments', { params })

// ==================== Queue Stats ====================
export const getAllQueueStats = () => request.get('/exams/queue/stats/all')

// ==================== Config ====================
export const getExamItems = () => request.get('/admin/exam-items')
export const createExamItem = (data) => request.post('/admin/exam-items', data)
export const updateExamItem = (id, data) => request.put(`/admin/exam-items/${id}`, data)
export const toggleExamItem = (id, enabled) => request.patch(`/admin/exam-items/${id}/status`, { enabled })
export const deleteExamItem = (id) => request.delete(`/admin/exam-items/${id}`)

export const getPackages = () => request.get('/admin/packages')
export const createPackage = (data) => request.post('/admin/packages', data)
export const updatePackage = (id, data) => request.put(`/admin/packages/${id}`, data)
export const togglePackage = (id, enabled) => request.patch(`/admin/packages/${id}/status`, { enabled })
export const deletePackage = (id) => request.delete(`/admin/packages/${id}`)

export const getBranches = () => request.get('/admin/branches')
export const createBranch = (data) => request.post('/admin/branches', data)
export const updateBranch = (id, data) => request.put(`/admin/branches/${id}`, data)
export const toggleBranch = (id, enabled) => request.patch(`/admin/branches/${id}/status`, { enabled })

export const getDepartments = (params) => request.get('/admin/departments', { params })
export const createDepartment = (data) => request.post('/admin/departments', data)
export const updateDepartment = (id, data) => request.put(`/admin/departments/${id}`, data)
export const toggleDepartment = (id, enabled) => request.patch(`/admin/departments/${id}/status`, { enabled })

export const getRules = () => request.get('/admin/rules')
export const createRule = (data) => request.post('/admin/rules', data)
export const updateRule = (id, data) => request.put(`/admin/rules/${id}`, data)
export const toggleRule = (id, enabled) => request.patch(`/admin/rules/${id}/status`, { enabled })
export const deleteRule = (id) => request.delete(`/admin/rules/${id}`)

export const getFlowNodes = () => request.get('/admin/flow-nodes')
export const createFlowNode = (data) => request.post('/admin/flow-nodes', data)
export const updateFlowNode = (id, data) => request.put(`/admin/flow-nodes/${id}`, data)
export const toggleFlowNode = (id, enabled) => request.patch(`/admin/flow-nodes/${id}/status`, { enabled })

// ==================== Accounts & Roles ====================
export const getAccounts = (params) => request.get('/admin/accounts', { params })
export const createAccount = (data) => request.post('/admin/accounts', data)
export const updateAccount = (id, data) => request.put(`/admin/accounts/${id}`, data)
export const toggleAccount = (id, enabled) => request.patch(`/admin/accounts/${id}/status`, { enabled })
export const deleteAccount = (id) => request.delete(`/admin/accounts/${id}`)

export const getRoles = () => request.get('/admin/roles')
export const createRole = (data) => request.post('/admin/roles', data)
export const updateRole = (code, data) => request.put(`/admin/roles/${code}`, data)
export const deleteRole = (code) => request.delete(`/admin/roles/${code}`)

// ==================== Exam ====================
export const checkIn = (data) => request.post('/exams/check-in', data)
export const getGuideSheet = (examRecordId) => request.get(`/guide-sheets/by-exam/${examRecordId}`)
export const getDepartmentQueue = (deptId) => request.get(`/admin/departments/${deptId}/queue`)
export const updateQueueStatus = (id, status) => request.patch(`/admin/department-queues/${id}/status`, { queueStatus: status })
export const getExamProgress = (examRecordId) => request.get(`/exams/${examRecordId}/progress`)

// ==================== Reports ====================
export const getReportTemplates = () => request.get('/admin/report-templates')
export const createReportTemplate = (data) => request.post('/admin/report-templates', data)
export const updateReportTemplate = (id, data) => request.put(`/admin/report-templates/${id}`, data)
export const toggleReportTemplate = (id, enabled) => request.patch(`/admin/report-templates/${id}/status`, { enabled })
export const deleteReportTemplate = (id) => request.delete(`/admin/report-templates/${id}`)

export const getReportWarnings = () => request.get('/admin/reports/warnings')
export const generateReport = (data) => request.post('/admin/reports/generate', data)
export const publishReport = (id, data) => request.post(`/admin/reports/${id}/publish`, data)

// ==================== Management ====================
export const getHealthManagement = () => request.get('/admin/health-management')
export const createHealthManagement = (data) => request.post('/admin/health-management', data)
export const createFollowUp = (id, data) => request.post(`/admin/health-management/${id}/follow-up`, data)
export const updateHealthManagement = (id, data) => request.put(`/admin/health-management/${id}`, data)

// ==================== Audit ====================
export const getAuditLogs = (params) => request.get('/admin/audit-logs', { params })

// ==================== Content ====================
export const getContent = (keys) => request.get(`/content/batch?keys=${keys}`)
export const updateContent = (key, value) => request.put(`/admin/content/${key}`, { value })
export const getContactMessages = () => request.get('/admin/contact-messages')
export const markMessageRead = (id) => request.put(`/admin/contact-messages/${id}/read`)
export const replyMessage = (id, reply) => request.put(`/admin/contact-messages/${id}/reply`, { reply })
export const deleteMessage = (id) => request.delete(`/admin/contact-messages/${id}`)

// ==================== Schedules ====================
export const getSchedules = (params) => request.get('/admin/schedules', { params })
export const createSchedule = (data) => request.post('/admin/schedules', data)
export const updateSchedule = (id, data) => request.put(`/admin/schedules/${id}`, data)
export const deleteSchedule = (id) => request.delete(`/admin/schedules/${id}`)
export const getDoctors = () => request.get('/admin/schedules/doctors')

// ==================== Statistics ====================
export const getBusinessStatsQuery = (params) => request.get('/admin/statistics/business', { params })
export const getHealthStatsQuery = (params) => request.get('/admin/statistics/health', { params })
export const getStaffStats = () => request.get('/admin/statistics/staff')
export const getExportUrl = (params) => `/admin/statistics/export?${new URLSearchParams(params)}`

// ==================== Cache Utility ====================
// 缓存工具，用于存储经常调用但变化不大的配置数据（如 departments, branches）
const cache = new Map()
export function withCache(key, fetcher, ttlMs = 60000) {
  const cached = cache.get(key)
  if (cached && Date.now() - cached.time < ttlMs) return cached.data
  return fetcher().then(data => { cache.set(key, { data, time: Date.now() }); return data })
}