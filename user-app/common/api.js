/**
 * API 工具模块 — 熙心健康体检管理系统
 * 基于 API 文档封装网络请求
 */

// 开发环境 Base URL（微信开发者工具需要填写本机局域网 IP）
const BASE_URL = 'http://localhost:8080'

/**
 * 通用请求封装
 * @param {string} method - 请求方法 GET/POST/PUT/PATCH/DELETE
 * @param {string} url - 接口路径（相对路径，如 /api/v1/auth/login）
 * @param {object} data - 请求体数据
 * @param {object} options - 额外配置 { needAuth: boolean, header: object }
 * @returns {Promise}
 */
function request(method, url, data = null, options = {}) {
	const { needAuth = false, header = {} } = options

	return new Promise((resolve, reject) => {
		// 构建请求头
		const requestHeader = {
			'Content-Type': 'application/json',
			...header
		}

		// 如果需要认证，添加 Authorization
		if (needAuth) {
			const token = uni.getStorageSync('accessToken')
			if (token) {
				requestHeader['Authorization'] = 'Bearer ' + token
			}
		}

		uni.request({
			url: BASE_URL + url,
			method: method,
			data: data,
			header: requestHeader,
			success: (res) => {
				// 统一响应格式: { code, message, data }
				const response = res.data
				if (response.code === '0') {
					resolve(response.data)
				} else {
					// 业务错误
					reject({ code: response.code, message: response.message || '请求失败' })
				}
			},
			fail: (err) => {
				// 网络错误
				reject({ code: '-1', message: '网络连接失败，请检查网络设置' })
			}
		})
	})
}

/**
 * 状态常量
 */
export const APPOINTMENT_STATUS = {
	CREATED:    { label: '待支付' },
	PAID:       { label: '已支付' },
	CHECKED_IN: { label: '已登记' },
	COMPLETED:  { label: '已完成' },
	CANCELLED:  { label: '已取消' },
}

export const ORDER_STATUS = {
	PENDING_PAYMENT:  { label: '待支付' },
	PAID:             { label: '已支付' },
	REFUND_REQUESTED: { label: '退款中' },
	REFUNDED:         { label: '已退款' },
	CLOSED:           { label: '已取消' },
}

/**
 * 认证模块 API
 */
export const authApi = {
	login(loginId, password) {
		return request('POST', '/api/v1/auth/login', { loginId, password })
	},

	register(phone, displayName, password, email) {
		const data = { phone, displayName, password }
		if (email) {
			data.email = email
		}
		return request('POST', '/api/v1/auth/register', data)
	}
}

/**
 * 套餐与分院 API
 */
export const packageApi = {
	getPackages() {
		return request('GET', '/api/v1/packages', null, { needAuth: true })
	},
	getBranches() {
		return request('GET', '/api/v1/branches', null, { needAuth: true })
	},
	getPackageItems(packageId) {
		return request('GET', '/api/v1/packages/' + packageId + '/items', null, { needAuth: true })
	}
}

/**
 * 档案 API
 */
export const profileApi = {
	getProfiles(userId) {
		return request('GET', '/api/v1/profiles/by-user?userId=' + userId, null, { needAuth: true })
	},
	getProfileDetail(id) {
		return request('GET', '/api/v1/profiles/' + id, null, { needAuth: true })
	},
	saveProfile(userId, data) {
		return request('POST', '/api/v1/profiles/me?ownerUserId=' + userId, data, { needAuth: true })
	}
}

/**
 * 预约 API
 */
export const appointmentApi = {
	create(data) {
		return request('POST', '/api/v1/appointments', data, { needAuth: true })
	},
	getByUser(userId, status) {
		let url = '/api/v1/appointments/user/' + userId
		if (status) url += '?status=' + status
		return request('GET', url, null, { needAuth: true })
	},
	cancel(id) {
		return request('POST', '/api/v1/appointments/' + id + '/cancel', null, { needAuth: true })
	}
}

/**
 * Token 管理
 */
export const tokenManager = {
	saveToken(authResult) {
		uni.setStorageSync('accessToken', authResult.accessToken)
		uni.setStorageSync('refreshToken', authResult.refreshToken)
		uni.setStorageSync('tokenExpiresIn', authResult.expiresInSeconds)
		uni.setStorageSync('tokenType', authResult.tokenType)
	},

	clearToken() {
		uni.removeStorageSync('accessToken')
		uni.removeStorageSync('refreshToken')
		uni.removeStorageSync('tokenExpiresIn')
		uni.removeStorageSync('tokenType')
	},

	getToken() {
		return uni.getStorageSync('accessToken') || null
	},

	isLoggedIn() {
		const token = this.getToken()
		return token != null && token.length > 0
	}
}

/**
 * 订单 API
 */
export const orderApi = {
	create(appointmentId, amountCent) {
		return request('POST', '/api/v1/orders', { appointmentId, amountCent }, { needAuth: true })
	},

	simulatePayment(orderNo, paymentNo) {
		return request('POST', '/api/v1/orders/simulate-payment', { orderNo, paymentNo }, { needAuth: true })
	},

	getByUser(userId) {
		return request('GET', '/api/v1/orders/user/' + userId, null, { needAuth: true })
	},

	requestRefund(orderNo, amountCent, reason) {
		return request('POST', '/api/v1/orders/' + orderNo + '/refund', { amountCent, reason }, { needAuth: true })
	}
}

/**
 * 通知 API
 */
export const notificationApi = {
	getByUser(userId) {
		return request('GET', '/api/v1/notifications?userId=' + userId, null, { needAuth: true })
	},
	markRead(notificationId) {
		return request('PATCH', '/api/v1/notifications/' + notificationId + '/read', null, { needAuth: true })
	}
}

/**
 * 导诊 API
 */
export const guideApi = {
	getByExam(examRecordId) {
		return request('GET', '/api/v1/guide-sheets/by-exam/' + examRecordId, null, { needAuth: true })
	},
	getByAppointment(appointmentId) {
		return request('GET', '/api/v1/guide-sheets/by-appointment/' + appointmentId, null, { needAuth: true })
	},
	getByUser(userId) {
		return request('GET', '/api/v1/guide-sheets/by-user/' + userId, null, { needAuth: true })
	},
	getDepartments() {
		return request('GET', '/api/v1/departments', null, { needAuth: false })
	}
}

/**
 * 报告 API
 */
export const reportApi = {
	getByUser(userId) {
		return request('GET', '/api/v1/reports/by-user/' + userId, null, { needAuth: true })
	},
	getById(reportId) {
		return request('GET', '/api/v1/reports/' + reportId, null, { needAuth: true })
	},
	getResults(examRecordId) {
		return request('GET', '/api/v1/results/public/by-exam/' + examRecordId, null, { needAuth: true })
	},
	getTrendByItem(userId) {
		return request('GET', '/api/v1/reports/trend/by-item/' + userId, null, { needAuth: true })
	},
	getDetail(reportId) {
		return request('GET', '/api/v1/reports/' + reportId + '/detail', null, { needAuth: true })
	},
	getTrendAnalysis(userId) {
		return request('GET', '/api/v1/reports/trend/analysis/' + userId, null, { needAuth: true })
	}
}

/**
 * 号源查询 API
 */
export const slotApi = {
	getAvailable(branchId, date) {
		let url = '/api/v1/slots/available?branchId=' + branchId
		if (date) url += '&date=' + date
		return request('GET', url, null, { needAuth: false })
	},
	getCalendar(branchId, startDate, endDate) {
		let url = '/api/v1/slots/calendar?branchId=' + branchId + '&startDate=' + startDate + '&endDate=' + endDate
		return request('GET', url, null, { needAuth: true })
	}
}

/**
 * 检前问卷 API
 */
export const questionnaireApi = {
	create(appointmentId, userId) {
		return request('POST', '/api/v1/questionnaire', { appointmentId, userId }, { needAuth: true })
	},
	getByAppointment(appointmentId) {
		return request('GET', '/api/v1/questionnaire/by-appointment/' + appointmentId, null, { needAuth: true })
	},
	getByUser(userId) {
		return request('GET', '/api/v1/questionnaire/by-user/' + userId, null, { needAuth: true })
	},
	update(id, data) {
		return request('PUT', '/api/v1/questionnaire/' + id, data, { needAuth: true })
	}
}

/**
 * 排队/队列 API
 */
export const queueApi = {
	getMyView(userId) {
		return request('GET', '/api/v1/exams/queue/my-view?userId=' + userId, null, { needAuth: true })
	},
	getWithNames(deptId) {
		return request('GET', '/api/v1/exams/queue/with-names/' + deptId, null, { needAuth: true })
	}
}

/**
 * 进度监控 API
 */
export const examProgressApi = {
	getMyProgress(userId) {
		return request('GET', '/api/v1/exams/my-progress?userId=' + userId, null, { needAuth: true })
	}
}
export const mealApi = {
	getStatus(examRecordId) {
		return request('GET', '/api/v1/meals/status/' + examRecordId, null, { needAuth: true })
	},
	claim(examRecordId) {
		return request('POST', '/api/v1/meals/claim/' + examRecordId, null, { needAuth: true })
	}
}


/**
 * 内容/联系我们 API
 */
export const contentApi = {
	batch(keys) {
		return request('GET', '/api/v1/content/batch?keys=' + keys, null, { needAuth: false })
	},
	submitContact(data) {
		return request('POST', '/api/v1/contact', data, { needAuth: false })
	}
}

/**
 * 格式化函数
 */
export function formatPrice(cent) {
	if (cent == null) return '0.00'
	return (cent / 100).toFixed(2)
}

export function formatTime(t) {
	if (!t) return ''
	return t.substring(0, 16).replace('T', ' ')
}
