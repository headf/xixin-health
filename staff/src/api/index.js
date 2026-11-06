import request from '@/utils/request'

// ==================== Auth ====================
export const login = (loginId, password) =>
  request.post('/auth/login', { loginId, password })

// ==================== Exam (Nurse) ====================
export const checkIn = (data) =>
  request.post('/exams/check-in', data)

export const getCheckedIn = (branchId) =>
  request.get(`/exams/checked-in/${branchId}`)

export const getCompletedRecords = (branchId) =>
  request.get(`/exams/completed/${branchId}`)

export const getCheckedInWithDetails = (branchId) =>
  request.get(`/exams/checked-in-with-details/${branchId}`)

export const getPendingReviewRecords = (branchId) =>
  request.get(`/exams/pending-review/${branchId}`)

export const getExamRecord = (id) =>
  request.get(`/exams/${id}`)

export const getExamRecordsByBranch = (branchId) =>
  request.get(`/exams/branch/${branchId}`)

export const recordVitalSigns = (data) =>
  request.post('/exams/vital-signs', data)

export const getVitalSigns = (examRecordId) =>
  request.get(`/exams/${examRecordId}/vital-signs`)

export const addToQueue = (data) =>
  request.post('/exams/queue/add', data)

export const callNext = (deptId) =>
  request.post(`/exams/queue/call-next/${deptId}`)

export const markQueueArrived = (queueId) =>
  request.put(`/exams/queue/${queueId}/arrived`)

export const completeQueue = (queueId) =>
  request.put(`/exams/queue/${queueId}/complete`)

export const completeQueueByExam = (examRecordId, departmentId) =>
  request.put(`/exams/queue/complete-by-exam/${examRecordId}/${departmentId}`)

export const getQueue = (deptId) =>
  request.get(`/exams/queue/${deptId}`)

export const getQueueWithNames = (deptId) =>
  request.get(`/exams/queue/with-names/${deptId}`)

export const getWaitingCount = (deptId) =>
  request.get(`/exams/queue/${deptId}/waiting-count`)

export const getQueueStats = (deptId) =>
  request.get(`/exams/queue/stats/${deptId}`)

export const getAllQueueStats = () =>
  request.get('/exams/queue/stats/all')

export const getTodayQueueRegistrations = () =>
  request.get('/exams/queue/today')

export const initProgress = (data) =>
  request.post('/exams/progress/init', data)

export const getExamProgress = (examRecordId) =>
  request.get(`/exams/${examRecordId}/progress`)

export const getPendingProgress = (deptId) =>
  request.get(`/exams/progress/pending/${deptId}`)

export const updateProgress = (progressId, action) =>
  request.put(`/exams/progress/${progressId}/${action}`)

export const getDashboard = (branchId) =>
  request.get(`/exams/dashboard/${branchId}`)

// ==================== Specimen (Nurse) ====================
export const collectSpecimen = (data) =>
  request.post('/exams/specimen/collect', data)

export const labelSpecimen = (specimenId, barcode) =>
  request.put(`/exams/specimen/${specimenId}/label`, { barcode })

export const sendSpecimen = (specimenId, sentBy) =>
  request.put(`/exams/specimen/${specimenId}/send`, { sentBy })

export const getSpecimens = (examRecordId) =>
  request.get(`/exams/${examRecordId}/specimens`)

export const getPendingSpecimens = () =>
  request.get('/exams/specimens/pending')

// ==================== Completion (Nurse) ====================
export const confirmCompletion = (examRecordId, nurseId) =>
  request.post(`/exams/${examRecordId}/confirm-completion`, { nurseId })

export const forceComplete = (examRecordId, nurseId) =>
  request.post(`/exams/${examRecordId}/force-complete`, { nurseId })

export const getCompletionStatus = (examRecordId) =>
  request.get(`/exams/${examRecordId}/completion-status`)

// ==================== Department Conclusion (Doctor) ====================
export const writeDepartmentConclusion = (progressId, conclusion) =>
  request.put(`/exams/progress/${progressId}/conclusion`, { conclusion })

// ==================== Result (Doctor) ====================
export const recordNumericResult = (data) =>
  request.post('/results/numeric', data)

export const recordTextResult = (data) =>
  request.post('/results/text', data)

export const recordBatchResults = (data) =>
  request.post('/results/batch', data)

export const getPendingReview = () =>
  request.get('/results/pending-review')

export const getPendingReviewByBranch = (branchId) =>
  request.get(`/results/pending-review/by-branch/${branchId}`)

export const getResultsByExam = (examRecordId) =>
  request.get(`/results/by-exam/${examRecordId}`)

export const getPendingByRecorder = (recorderId) =>
  request.get(`/results/pending-review/by-recorder?recorderId=${recorderId}`)

export const reviewResult = (resultId, data) =>
  request.put(`/results/${resultId}/review`, data)

// ==================== Report (Review Doctor) ====================
export const generateReport = (data) =>
  request.post('/reports/generate', data)

export const writeDiagnosis = (reportId, data) =>
  request.put(`/reports/${reportId}/diagnosis`, data)

export const publishReport = (reportId, data) =>
  request.post(`/reports/${reportId}/publish`, data)

export const getPublishedReports = () =>
  request.get('/reports/published')

export const sendReport = (reportId) =>
  request.post(`/reports/${reportId}/send`)

export const getReportById = (reportId) =>
  request.get(`/reports/${reportId}`)

export const comprehensiveAudit = (reportId, data) =>
  request.put(`/reports/${reportId}/comprehensive-audit`, data)

export const getReport = (reportId) =>
  request.get(`/reports/${reportId}`)

export const getReportsByExam = (examRecordId) =>
  request.get(`/reports/by-exam/${examRecordId}`)

export const getReportsByUser = (userId) =>
  request.get(`/reports/by-user/${userId}`)

// ==================== Catalog ====================
export const getPackages = () =>
  request.get('/packages')

export const getBranches = () =>
  request.get('/branches')

export const getExamItems = () =>
  request.get('/items')

// ==================== Addons ====================
export const createAddon = (data) =>
  request.post('/addons', data)

export const getAddonsByExam = (examRecordId) =>
  request.get(`/addons/by-exam/${examRecordId}`)

// ==================== Patients ====================
export const getPatients = (params) =>
  request.get('/patients', { params })

export const getProfilesByUser = (userId) =>
  request.get(`/profiles/by-user?userId=${userId}`)

// ==================== Departments ====================
export const getDepartments = () =>
  request.get('/departments')

// ==================== Critical Results (Review Doctor) ====================
export const getCriticalResults = () =>
  request.get('/results/critical')

// ==================== Health Management (Review Doctor) ====================
export const getHealthManagement = () => request.get('/admin/health-management')
export const createHealthManagement = (data) => request.post('/admin/health-management', data)
export const createFollowUp = (id, data) => request.post(`/admin/health-management/${id}/follow-up`, data)
export const updateHealthManagement = (id, data) => request.put(`/admin/health-management/${id}`, data)

// ==================== Weekly Trend ====================
export const getWeeklyTrend = (branchId) =>
  request.get("/appointments/weekly-trend", { params: branchId ? { branchId } : {} })

export const getUserAppointments = (userId) =>
  request.get(`/appointments/user/${userId}`)

export const getAllAppointments = (params) =>
  request.get('/appointments/list', { params })

// ==================== Questionnaire (Staff view) ====================
export const getQuestionnaireByAppointment = (appointmentId) =>
  request.get(`/questionnaire/staff/by-appointment/${appointmentId}`)
