/**
 * API 工具模块 — 熙心健康体检管理系统
 * 基于 API 文档封装网络请求
 */

// 开发环境 Base URL（微信开发者工具需要填写本机局域网 IP）
const BASE_URL = 'http://192.168.72.1:8080'

/**
 * 通用请求封装
 * @param {string} method - 请求方法 GET/POST/PUT/PATCH/DELETE
 * @param {string} url - 接口路径（相对路径，如 /api/v1/auth/login）
 * @param {object} data - 请求体数据
 * @param {object} options - 额外配置 { needAuth: boolean, header: object }
 * @returns {Promise}
 */
export function request(method, url, data = null, options = {}) {
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
				const statusCode = res.statusCode
				// 401 → 登录过期，清除 token 并跳转登录页
				// 401 → 登录过期（排除登录接口本身）
				if (statusCode === 401 && url !== '/api/v1/auth/login') {
					uni.reLaunch({ url: '/pages/login/login' })
					reject({ code: '401', message: '登录已过期，请重新登录' })
					return
				}
				// 统一响应格式: { code, message, data }
				const response = res.data
				if (response && response.code === '0') {
					resolve(response.data)
				} else {
					reject({ code: response?.code || 'ERROR', message: response?.message || '请求失败' })
				}
			},
			fail: (err) => {
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
	},
	/**
	 * 下载体检报告 PDF
	 * @param {number} reportId 报告 ID
	 * @returns {Promise}
	 */
	downloadPdf(reportId) {
		const token = uni.getStorageSync('accessToken')
		const url = BASE_URL + '/api/v1/reports/' + reportId + '/pdf'
		return new Promise((resolve, reject) => {
			uni.downloadFile({
				url: url,
				header: {
					'Authorization': token ? 'Bearer ' + token : ''
				},
				success: (res) => {
					if (res.statusCode === 200) {
						// 打开 PDF 文件
						uni.openDocument({
							filePath: res.tempFilePath,
							success: () => resolve(res.tempFilePath),
							fail: (err) => reject(err)
						})
					} else {
						reject({ code: res.statusCode, message: '下载失败' })
					}
				},
				fail: (err) => reject(err)
			})
		})
	}
}

/**
 * 号源查询 API
 */
export const slotApi = {
	getAvailable(branchId, date) {
		let url = '/api/v1/slots/available?branchId=' + branchId
		if (date) url += '&date=' + date
		return request('GET', url, null, { needAuth: true })
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

/**
 * 身份证号工具：根据身份证号推算性别
 * 第17位（索引16）奇数为男、偶数为女
 */
export function getIdCardGender(idCard) {
	if (!idCard || idCard.length < 18) return '未知'
	const digit = parseInt(idCard.charAt(16), 10)
	return (digit % 2 === 1) ? '男' : '女'
}

/**
 * 身份证号工具：根据身份证号推算年龄
 * 出生日期在第6-14位（yyyyMMdd）
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
		if (mDiff < 0 || (mDiff === 0 && now.getDate() < birth.getDate())) {
			age--
		}
		return age
	} catch (e) {
		return 0
	}
}

/**
 * 身份证号工具：脱敏显示（前6后4）
 */
export function maskIdCard(idCard) {
	if (!idCard) return '-'
	if (idCard.length <= 10) {
		const half = Math.floor(idCard.length / 2)
		return idCard.slice(0, Math.max(half, 2)) + '****' + idCard.slice(-Math.max(half, 2))
	}
	return idCard.slice(0, 6) + '****' + idCard.slice(-4)
}
