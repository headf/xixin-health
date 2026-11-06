<template>
	<view class="page">
		<!-- Header -->
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">智能导检</text>
			<view class="header-spacer"></view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<scroll-view class="content" scroll-y v-else>
			<!-- 空状态 -->
			<view class="empty-state" v-if="!hasData">
				<text class="empty-icon"></text>
				<text class="empty-text">暂无导检信息</text>
				<text class="empty-desc">签到后即可查看智能导检路线</text>
				<view class="empty-btn" @tap="loadData">重新加载</view>
			</view>

			<template v-if="hasData">
				<!-- 当前位置 & 下一站 -->
				<view class="location-card">
					<view class="location-row">
						<text class="loc-icon"></text>
						<view class="loc-text-group">
							<text class="loc-label">当前位置</text>
							<text class="loc-value">{{ currentDeptName }}</text>
						</view>
					</view>
					<view class="location-divider"></view>
					<view class="location-row" v-if="nextDept">
						<text class="loc-icon"></text>
						<view class="loc-text-group">
							<text class="loc-label">下一站</text>
							<text class="loc-value">{{ nextDept.department_name }}
								<text class="loc-location"> ({{ nextDept.location || '院内' }})</text>
							</text>
						</view>
						<text class="waiting-badge" v-if="nextDept.waiting > 0">{{ nextDept.waiting }}人排队</text>
					</view>
					<view class="location-row" v-else>
						<text class="loc-icon"></text>
						<view class="loc-text-group">
							<text class="loc-label">下一站</text>
							<text class="loc-value" style="color:#10b981">全部完成，请前往前台确认</text>
						</view>
					</view>
				</view>

				<!-- 当前科室等待信息 -->
				<view class="waiting-card" v-if="currentDept && currentDept.waiting > 0">
					<text class="waiting-icon"></text>
					<view class="waiting-text-group">
						<text class="waiting-title">{{ currentDept.department_name }} 等待人数</text>
						<text class="waiting-count">{{ currentDept.waiting }}<text class="waiting-unit">人</text></text>
					</view>
				</view>


				<!-- 科室详情列表 -->
				<view class="section-title">科室详情</view>
				<view class="dept-list">
					<view class="dept-card" v-for="(dept, idx) in sortedDepts" :key="dept.department_id"
						:class="{ 'dept-current': isCurrentDept(dept), 'dept-done': dept.dept_status === 'DONE' }">
						<view class="dept-left">
							<view class="dept-status-icon" :class="getStepClass(dept)">
								<text>{{ getStepIcon(dept) }}</text>
							</view>
						</view>
						<view class="dept-content">
							<view class="dept-title-row">
								<text class="dept-name">{{ dept.department_name }}</text>
								<text class="dept-badge" :class="getStepClass(dept)">{{ getDeptBadge(dept) }}</text>
								<text class="dept-current-tag" v-if="isCurrentDept(dept)">当前</text>
							</view>
							<text class="dept-location"> {{ dept.location || '院内' }}</text>
							<view class="dept-meta">
								<text class="dept-meta-item" v-if="dept.fasting_required">需空腹</text>
								<text class="dept-meta-item" v-if="dept.waiting > 0"> {{ dept.waiting }}人排队</text>
								<text class="dept-meta-item" v-if="dept.total_items > 0">{{ dept.done_items }}/{{ dept.total_items }}项</text>
							</view>
							<!-- 科室内部进度条 -->
							<view class="dept-progress-bar" v-if="dept.total_items > 1">
								<view class="dept-progress-fill" :style="{ width: (dept.done_items / dept.total_items * 100) + '%' }"></view>
							</view>
						</view>
						<view class="dept-right" v-if="dept.waiting > 0 && isCurrentDept(dept)">
							<text class="dept-waiting-num">{{ dept.waiting }}</text>
							<text class="dept-waiting-label">人排队</text>
						</view>
					</view>
			</view>
			</template>

			<view class="bottom-spacer"></view>
		</scroll-view>
	</view>
</template>

<script>
import { guideApi, examProgressApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			departments: [],
			progressData: [],
			queueStats: {},
			appointment: null,
			userId: null,
			sortedDepts: []
		}
	},

	computed: {
		hasData() {
			return this.sortedDepts.length > 0
		},
		totalCount() {
			return this.sortedDepts.length
		},
		completedCount() {
			return this.sortedDepts.filter(d => d.dept_status === 'DONE').length
		},
		inProgressCount() {
			return this.sortedDepts.filter(d => d.dept_status === 'IN_PROGRESS').length
		},
		pendingCount() {
			return this.sortedDepts.filter(d => d.dept_status === 'PENDING').length
		},
		overallPercent() {
			if (this.totalCount === 0) return 0
			return Math.round((this.completedCount / this.totalCount) * 100)
		},
		currentDept() {
			return this.sortedDepts.find(d => d.dept_status === 'IN_PROGRESS') || null
		},
		currentDeptName() {
			if (this.currentDept) return this.currentDept.department_name + ' (' + (this.currentDept.location || '院内') + ')'
			if (this.completedCount > 0) return '已完成所有检查'
			if (this.sortedDepts.length > 0) return this.sortedDepts[0].department_name + ' (' + (this.sortedDepts[0].location || '院内') + ')'
			return '暂无检查数据'
		},
		nextDept() {
			return this.sortedDepts.find(d => d.dept_status === 'PENDING') || null
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadData()
	},

	methods: {
		async loadData() {
			this.loading = true
			try {
				this.appointment = uni.getStorageSync('selectedAppointment') || null
				this.userId = uni.getStorageSync('userId') || null

				// 并行加载数据
				const [depts, progressResult, queueResult] = await Promise.all([
					this.loadDepartments(),
					this.loadProgress(),
					this.loadQueueStats()
				])

				this.departments = Array.isArray(depts) ? depts : []
				this.progressData = Array.isArray(progressResult) ? progressResult : []
				this.queueStats = (queueResult && typeof queueResult === 'object') ? queueResult : {}

				// 构建科室进度列表
				this.buildDeptList()
			} catch (e) {
				console.error('加载导检数据失败', e)
				// 尝试降级加载
				try {
					await this.loadFallback()
				} catch (e2) {
					console.error('降级加载也失败', e2)
				}
			} finally {
				this.loading = false
			}
		},

		async loadDepartments() {
			try {
				return await guideApi.getDepartments()
			} catch (e) {
				console.error('加载科室列表失败', e)
				return []
			}
		},

		async loadProgress() {
			if (!this.userId) return []
			try {
				return await examProgressApi.getMyProgress(this.userId)
			} catch (e) {
				console.error('加载进度数据失败', e)
				return []
			}
		},

		async loadQueueStats() {
			return new Promise(resolve => {
				uni.request({
					url: '/api/v1/exams/queue/stats/all',
					method: 'GET',
					success: r => resolve(r.data?.data || {}),
					fail: () => resolve({})
				})
			})
		},

		async loadFallback() {
			// 从 guideSheet 降级加载
			if (this.appointment && this.appointment.id) {
				const sheet = await guideApi.getByAppointment(this.appointment.id)
				if (sheet) {
					const depts = await this.loadDepartments()
					this.departments = Array.isArray(depts) ? depts : []
					// 构建简易列表
					this.progressData = []
					this.sortedDepts = (Array.isArray(depts) ? depts : []).map(d => ({
						department_id: d.id,
						department_name: d.department_name,
						location: d.location,
						fasting_required: d.fastingRequired || false,
						waiting: 0,
						queue_completed: 0,
						dept_status: 'PENDING',
						total_items: 1,
						done_items: 0
					}))
				}
			}
		},

		buildDeptList() {
			const appt = this.appointment

			// 从 progressData 构建科室进度映射
			const deptProgressMap = {}
			let apptPackageId = appt ? appt.packageId : null

			for (const row of this.progressData) {
				const deptId = row.department_id
				if (!deptProgressMap[deptId]) {
					deptProgressMap[deptId] = {
						department_id: deptId,
						department_name: row.department_name,
						location: row.location || '院内',
						fasting_required: row.fasting_required || false,
						waiting: Number(row.waiting || 0),
						queue_completed: Number(row.queue_completed || 0),
						dept_status: row.dept_status || 'PENDING',
						total_items: Number(row.total_items || 1),
						done_items: Number(row.done_items || 0)
					}
				}
				// 取预约中的套餐ID
				if (!apptPackageId && row.package_id) {
					apptPackageId = row.package_id
				}
			}

			// 如果没有 progressData（降级模式），从 departments 构建
			if (Object.keys(deptProgressMap).length === 0) {
				for (const d of this.departments) {
					if (d.enabled !== false) {
						const stats = this.queueStats[d.id] || {}
						deptProgressMap[d.id] = {
							department_id: d.id,
							department_name: d.department_name,
							location: d.location || '院内',
							fasting_required: d.fastingRequired || false,
							waiting: Number(stats.waiting || 0),
							queue_completed: Number(stats.completed || 0),
							dept_status: 'PENDING',
							total_items: 1,
							done_items: 0
						}
					}
				}
			}

			// 转为数组并排序：空腹优先，已完成放最后
			let deptList = Object.values(deptProgressMap)
			deptList.sort((a, b) => {
				// 未完成的排在已完成前面
				if (a.dept_status === 'DONE' && b.dept_status !== 'DONE') return 1
				if (a.dept_status !== 'DONE' && b.dept_status === 'DONE') return -1
				// 进行中的排最前
				if (a.dept_status === 'IN_PROGRESS' && b.dept_status !== 'IN_PROGRESS') return -1
				if (a.dept_status !== 'IN_PROGRESS' && b.dept_status === 'IN_PROGRESS') return 1
				// 空腹优先
				if (a.fasting_required && !b.fasting_required) return -1
				if (!a.fasting_required && b.fasting_required) return 1
				// 按科室ID排序
				return (a.department_id || 0) - (b.department_id || 0)
			})

			this.sortedDepts = deptList
		},

		isCurrentDept(dept) {
			return dept.dept_status === 'IN_PROGRESS'
		},

		getStepClass(dept) {
			if (dept.dept_status === 'DONE') return 'step-done'
			if (dept.dept_status === 'IN_PROGRESS') return 'step-active'
			return 'step-pending'
		},

		getStepIcon(dept) {
			if (dept.dept_status === 'DONE') return '✓'
			if (dept.dept_status === 'IN_PROGRESS') return '◉'
			return (this.sortedDepts.indexOf(dept) + 1).toString()
		},

		getStepStatus(dept) {
			if (dept.dept_status === 'DONE') return '已完成'
			if (dept.dept_status === 'IN_PROGRESS') return '进行中'
			return '待完成'
		},

		getDeptBadge(dept) {
			if (dept.dept_status === 'DONE') return '已完成'
			if (dept.dept_status === 'IN_PROGRESS') return '进行中'
			return '待完成'
		},

		getShortName(name) {
			if (!name) return ''
			if (name.length <= 4) return name
			// 取关键字的简称
			const short = name.replace(/体检|检查|科室/gi, '')
			return short.length >= 2 ? short.substring(0, 4) : name.substring(0, 4)
		},

		scrollToDept(idx) {
			// 点击步骤圆点时滚动到对应科室卡片
			const query = uni.createSelectorQuery().in(this)
			query.select('.dept-list').boundingClientRect(rect => {
				if (rect) {
					uni.pageScrollTo({ scrollTop: rect.top + idx * 120, duration: 300 })
				}
			}).exec()
		},

		goBack() {
			uni.navigateBack()
		}
	}
}
</script>

<style>
/* ========== Global ========== */
.page {
	min-height: 100vh;
	background: #f5f7fa;
	display: flex;
	flex-direction: column;
	padding-top: calc(44px);
}

.header {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 32rpx;
	background: #ffffff;
	position: relative;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.back-btn {
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	padding: 12rpx 8rpx;
}

.back-btn:active { opacity: 0.6; }

.header-title {
	font-size: 40rpx;
	font-weight: 500;
	color: #17191c;
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
	white-space: nowrap;
}

.header-spacer { width: 80rpx; }

.loading-state {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

.loading-text { font-size: 28rpx; color: #777b86; }

.content {
	flex: 1;
	padding: 24rpx 24rpx;
	height: 0;
}

/* ========== Empty ========== */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 100rpx 0;
}

.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 32rpx; color: #17191c; margin-bottom: 8rpx; }
.empty-desc { font-size: 26rpx; color: #a3a6af; text-align: center; }

.empty-btn {
	margin-top: 32rpx;
	padding: 16rpx 48rpx;
	background: var(--brand);
	color: #ffffff;
	border-radius: 48rpx;
	font-size: 28rpx;
}

/* ========== Location Card ========== */
.location-card {
	background: var(--surface, #ffffff);
	border-radius: 24rpx;
	padding: 28rpx 28rpx;
	margin-bottom: 20rpx;
	box-shadow: var(--shadow-sm);
	border-left: 8rpx solid var(--brand, #7c9fff);
}

.location-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 8rpx 0;
}

.location-divider {
	height: 1px;
	background: var(--border-light, #f0f2f5);
	margin: 12rpx 0;
}

.loc-icon {
	font-size: 36rpx;
	margin-right: 16rpx;
	width: 48rpx;
	text-align: center;
}

.loc-text-group {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.loc-label {
	font-size: 22rpx;
	color: var(--text-secondary, #7a8ca0);
	margin-bottom: 4rpx;
}

.loc-value {
	font-size: 30rpx;
	font-weight: 600;
	color: var(--text, #2c3e50);
}

.loc-location {
	font-size: 26rpx;
	font-weight: 400;
	color: var(--text-secondary, #7a8ca0);
}

.waiting-badge {
	background: var(--brand-light, #eef1ff);
	color: var(--brand, #7c9fff);
	font-size: 22rpx;
	padding: 6rpx 16rpx;
	border-radius: 24rpx;
	white-space: nowrap;
	font-weight: 500;
}

/* ========== Waiting Info ========== */
.waiting-card {
	background: #fef3c7;
	border-radius: 20rpx;
	padding: 24rpx 28rpx;
	margin-bottom: 20rpx;
	display: flex;
	flex-direction: row;
	align-items: center;
	border: 1px solid #fde68a;
}

.waiting-icon { font-size: 40rpx; margin-right: 20rpx; }

.waiting-text-group { flex: 1; }

.waiting-title { font-size: 26rpx; color: #92400e; display: block; }

.waiting-count {
	font-size: 48rpx;
	font-weight: 700;
	color: #f59e0b;
	display: block;
	line-height: 1.2;
}

.waiting-unit { font-size: 24rpx; font-weight: 400; color: #b45309; }

/* ========== Section Title ========== */
.section-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #17191c;
	margin-bottom: 16rpx;
	margin-top: 24rpx;
}

/* ========== Progress Steps ========== */
.progress-steps {
	display: flex;
	flex-direction: row;
	white-space: nowrap;
	padding: 8rpx 0 16rpx 0;
	margin-bottom: 8rpx;
}

.step-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-right: 20rpx;
	min-width: 100rpx;
	cursor: pointer;
}

.step-item:last-child { margin-right: 0; }

.step-circle {
	width: 72rpx;
	height: 72rpx;
	border-radius: 36rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 8rpx;
	border: 3px solid #e5e7eb;
	background: #ffffff;
	transition: all 0.3s ease;
}

.step-circle.step-done {
	background: #10b981;
	border-color: #10b981;
}

.step-circle.step-active {
	background: var(--brand);
	border-color: var(--brand);
	box-shadow: 0 4rpx 16rpx rgba(22, 93, 255, 0.35);
	transform: scale(1.08);
}

.step-circle.step-pending {
	background: #ffffff;
	border-color: #d1d5db;
}

.step-icon {
	font-size: 28rpx;
	font-weight: 600;
	color: #ffffff;
}

.step-circle.step-pending .step-icon {
	color: #9ca3af;
}

.step-label {
	font-size: 22rpx;
	color: #6b7280;
	text-align: center;
	max-width: 100rpx;
	overflow: hidden;
	text-overflow: ellipsis;
}

.step-label.step-done { color: #10b981; }
.step-label.step-active { color: var(--brand); font-weight: 600; }

.step-status {
	font-size: 20rpx;
	margin-top: 2rpx;
}

.step-status.step-done { color: #10b981; }
.step-status.step-active { color: var(--brand); }
.step-status.step-pending { color: #9ca3af; }

/* ========== Department List ========== */
.dept-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.dept-card {
	display: flex;
	flex-direction: row;
	background: #ffffff;
	border-radius: 20rpx;
	padding: 24rpx 24rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	transition: all 0.2s;
	position: relative;
	overflow: hidden;
}

.dept-card.dept-current {
	box-shadow: 0 4rpx 20rpx rgba(22, 93, 255, 0.15);
	border: 2px solid var(--brand);
}

.dept-card.dept-done {
	opacity: 0.75;
}

.dept-left {
	margin-right: 16rpx;
	display: flex;
	align-items: flex-start;
	padding-top: 4rpx;
}

.dept-status-icon {
	width: 48rpx;
	height: 48rpx;
	border-radius: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 22rpx;
	font-weight: 600;
	flex-shrink: 0;
}

.dept-status-icon.step-done {
	background: #d1fae5;
	color: #10b981;
}

.dept-status-icon.step-active {
	background: #dbeafe;
	color: var(--brand);
}

.dept-status-icon.step-pending {
	background: #f3f4f6;
	color: #9ca3af;
}

.dept-content {
	flex: 1;
	min-width: 0;
}

.dept-title-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin-bottom: 6rpx;
	flex-wrap: wrap;
	gap: 8rpx;
}

.dept-name {
	font-size: 28rpx;
	font-weight: 600;
	color: #17191c;
}

.dept-badge {
	font-size: 20rpx;
	padding: 2rpx 14rpx;
	border-radius: 20rpx;
	white-space: nowrap;
}

.dept-badge.step-done {
	background: #d1fae5;
	color: #065f46;
}

.dept-badge.step-active {
	background: #dbeafe;
	color: #1e40af;
}

.dept-badge.step-pending {
	background: #f3f4f6;
	color: #6b7280;
}

.dept-current-tag {
	background: var(--brand);
	color: #ffffff;
	font-size: 20rpx;
	padding: 2rpx 14rpx;
	border-radius: 20rpx;
	font-weight: 500;
}

.dept-location {
	font-size: 24rpx;
	color: #6b7280;
	display: block;
	margin-bottom: 8rpx;
}

.dept-meta {
	display: flex;
	flex-direction: row;
	gap: 12rpx;
	flex-wrap: wrap;
}

.dept-meta-item {
	font-size: 22rpx;
	color: #777b86;
	background: #f9fafb;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

.dept-progress-bar {
	height: 6rpx;
	background: #e5e7eb;
	border-radius: 3rpx;
	margin-top: 12rpx;
	overflow: hidden;
}

.dept-progress-fill {
	height: 100%;
	background: #10b981;
	border-radius: 3rpx;
	transition: width 0.5s ease;
}

.dept-right {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	margin-left: 12rpx;
	padding-left: 16rpx;
	border-left: 1px solid #e5e7eb;
	min-width: 60rpx;
}

.dept-waiting-num {
	font-size: 36rpx;
	font-weight: 700;
	color: #f59e0b;
}

.dept-waiting-label {
	font-size: 20rpx;
	color: #9ca3af;
}

/* ========== Overall Progress ========== */
.overall-progress-card {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 28rpx 28rpx;
	margin-top: 24rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}

.progress-header {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.progress-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #17191c;
}

.progress-text {
	font-size: 26rpx;
	color: var(--brand);
	font-weight: 500;
}

.progress-track {
	height: 16rpx;
	background: #e5e7eb;
	border-radius: 8rpx;
	overflow: hidden;
	margin-bottom: 16rpx;
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, #10b981, #34d399);
	border-radius: 8rpx;
	transition: width 0.6s ease;
}

.progress-stats {
	display: flex;
	flex-direction: row;
	gap: 24rpx;
}

.stat-item {
	font-size: 24rpx;
	color: #6b7280;
	display: flex;
	align-items: center;
}

.stat-dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 7rpx;
	margin-right: 8rpx;
	display: inline-block;
}

.done-dot { background: #10b981; }
.active-dot { background: var(--brand); }
.pending-dot { background: #d1d5db; }

/* ========== Contact ========== */
.contact-card {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 24rpx 28rpx;
	margin-top: 24rpx;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}

.contact-title {
	font-size: 26rpx;
	color: #17191c;
	font-weight: 450;
}

.contact-phone {
	font-size: 26rpx;
	color: var(--brand);
	font-weight: 500;
}

.bottom-spacer { height: 48rpx; }
</style>
