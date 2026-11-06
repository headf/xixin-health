<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">科室排队</text>
			<view class="header-spacer"></view>
		</view>

		<view class="live-bar" v-if="!loading">
			<view class="live-dot"></view>
			<text class="live-text">实时刷新中</text>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="content" v-if="!loading">
			<!-- 空状态 -->
			<view class="empty-state" v-if="departments.length === 0">
				<text class="empty-icon">·</text>
				<text class="empty-title">暂无活跃体检</text>
				<text class="empty-desc">完成签到后即可查看各科室排队情况</text>
			</view>

			<!-- 排队列表 -->
			<view class="queue-grid" v-else>
				<view class="queue-card" :class="queueLevel(d)" v-for="d in sortedDepts" :key="d.department_id" @tap="toggleDetail(d.department_id)">
					<view class="queue-card-body">
						<text class="queue-dept-name">{{ d.department_name }}</text>
						<text class="queue-count">{{ d.waiting }}</text>
						<text class="queue-label">等待人数</text>
						<view class="queue-meta">
							<text class="fasting-tag" v-if="d.fasting_required">需空腹</text>
							<text class="done-tag" v-if="d.completed > 0">{{ d.completed }}已检</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 排队详情弹出 -->
			<view class="overlay" v-if="detailDeptId && queueDetail[detailDeptId]" @tap="closeDetail">
				<view class="popup" @tap.stop>
					<view class="popup-header">
						<text class="popup-title">{{ getDeptName(detailDeptId) }} — 排队详情</text>
						<text class="popup-close" @tap="closeDetail">关闭</text>
					</view>
					<scroll-view class="popup-list" scroll-y>
						<view class="detail-row" v-for="row in waitingList" :key="row.id">
							<text class="detail-queue-no">{{ row.queueNo }}</text>
							<text class="detail-patient">{{ row.patientName || '#' + row.examRecordId }}</text>
							<view class="detail-status">
								<text class="status-tag-wait">等待中</text>
							</view>
						</view>
						<view class="popup-empty" v-if="!waitingList || waitingList.length === 0">
							<text class="popup-empty-text">暂无排队</text>
						</view>
					</scroll-view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { queueApi, tokenManager } from '../../common/api.js'

export default {
	data() {
		return {
			departments: [],
			queueDetail: {},
			detailDeptId: null,
			loading: true,
			pollTimer: null
		}
	},

	computed: {
		sortedDepts() {
			return [...this.departments].sort((a, b) => {
				if (a.fasting_required && !b.fasting_required) return -1
				if (!a.fasting_required && b.fasting_required) return 1
				return (a.department_name || '').localeCompare(b.department_name || '')
			})
		},
		waitingList() {
			const entries = this.queueDetail[this.detailDeptId] || []
			return entries.filter(q => q.queueStatus === 'WAITING')
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadData()
		// 启动轮询
		this.pollTimer = setInterval(() => this.loadData(), 8000)
	},

	onHide() {
		if (this.pollTimer) {
			clearInterval(this.pollTimer)
			this.pollTimer = null
		}
	},

	methods: {
		async loadData() {
			const userId = uni.getStorageSync('userId')
			if (!userId) { this.loading = false; return }
			try {
				const data = await queueApi.getMyView(userId)
				this.departments = Array.isArray(data) ? data : []
			} catch (e) {
				console.error('加载排队数据失败', e)
				this.departments = []
			} finally {
				this.loading = false
			}
		},

		queueLevel(d) {
			const w = d.waiting || 0
			if (w === 0) return 'level-green'
			if (w <= 3) return 'level-yellow'
			if (w <= 8) return 'level-orange'
			return 'level-red'
		},

		getDeptName(id) {
			const d = this.departments.find(d => d.department_id === id)
			return d ? d.department_name : '未知'
		},

		async toggleDetail(id) {
			if (this.detailDeptId === id) {
				this.closeDetail()
				return
			}
			this.detailDeptId = id
			await this.loadDetail(id)
		},

		async loadDetail(deptId) {
			try {
				const data = await queueApi.getWithNames(deptId)
				this.queueDetail[deptId] = Array.isArray(data) ? data : []
				// 触发更新
				this.$forceUpdate()
			} catch (e) {
				console.error('加载排队详情失败', e)
				this.queueDetail[deptId] = []
			}
		},

		closeDetail() {
			this.detailDeptId = null
		},

		goBack() {
			uni.navigateBack()
		}
	}
}
</script>

<style>
.page {
	min-height: 100vh;
	background: #f5f5f6;
	display: flex;
	flex-direction: column;
}

.header {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 32rpx;
	background: #ffffff;
	position: relative;
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
	font-weight: 400;
	color: #17191c;
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
	white-space: nowrap;
}

.header-spacer { width: 80rpx; }

.live-bar {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 12rpx 32rpx 4rpx;
}

.live-dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background: #22c55e;
	margin-right: 8rpx;
	animation: livePulse 1.5s ease-in-out infinite;
}

@keyframes livePulse {
	0%, 100% { opacity: 1; transform: scale(1); }
	50% { opacity: 0.5; transform: scale(0.8); }
}

.live-text { font-size: 22rpx; color: #22c55e; }

.loading-state {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}
.loading-text { font-size: 28rpx; color: #777b86; }

.content { flex: 1; padding: 16rpx 24rpx 140rpx; }

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 0;
}
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-title { font-size: 30rpx; color: #17191c; margin-bottom: 8rpx; }
.empty-desc { font-size: 26rpx; color: #a3a6af; }

/* 排队网格 */
.queue-grid {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	gap: 16rpx;
}

.queue-card {
	width: calc(50% - 8rpx);
	border-radius: 24rpx;
	padding: 28rpx 16rpx;
	text-align: center;
	border: 2rpx solid transparent;
	box-sizing: border-box;
}

.queue-card:active { opacity: 0.85; }
.queue-card.level-green { background: #ecfdf5; border-color: #a7f3d0; }
.queue-card.level-yellow { background: #fffbeb; border-color: #fde68a; }
.queue-card.level-orange { background: #fff7ed; border-color: #fed7aa; }
.queue-card.level-red { background: #fef2f2; border-color: #fecaca; }

.queue-dept-name { font-size: 26rpx; font-weight: 600; color: #0f172a; margin-bottom: 6rpx; }
.queue-count { font-size: 56rpx; font-weight: 800; line-height: 1.2; margin-bottom: 2rpx; }
.level-green .queue-count { color: #059669; }
.level-yellow .queue-count { color: #d97706; }
.level-orange .queue-count { color: #ea580c; }
.level-red .queue-count { color: #dc2626; }
.queue-label { font-size: 22rpx; color: #94a3b8; }
.queue-meta { display: flex; gap: 8rpx; justify-content: center; margin-top: 8rpx; flex-wrap: wrap; }
.fasting-tag { font-size: 20rpx; padding: 2rpx 12rpx; border-radius: 4rpx; background: #fef3c7; color: #b45309; }
.done-tag { font-size: 20rpx; padding: 2rpx 12rpx; border-radius: 4rpx; background: #f1f5f9; color: var(--text-secondary); }

/* 弹出层 */
.overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(23, 25, 28, 0.4);
	z-index: 100;
	display: flex;
	align-items: flex-end;
}

.popup {
	width: 100%;
	background: #ffffff;
	border-radius: 48rpx 48rpx 0 0;
	max-height: 60vh;
	display: flex;
	flex-direction: column;
}

.popup-header {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx 32rpx 16rpx;
	border-bottom: 1px solid #f7f7f8;
}

.popup-title {
	font-size: 30rpx;
	font-weight: 500;
	color: #17191c;
}

.popup-close {
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	padding: 8rpx 16rpx;
}

.popup-close:active { opacity: 0.6; }

.popup-list {
	padding: 8rpx 0;
	max-height: 50vh;
}

.detail-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 24rpx 32rpx;
	border-bottom: 1px solid #f7f7f8;
}

.detail-queue-no {
	font-size: 32rpx;
	font-weight: 700;
	color: #17191c;
	width: 120rpx;
}

.detail-patient {
	font-size: 28rpx;
	color: #17191c;
	flex: 1;
}

.detail-status { flex-shrink: 0; }

.status-tag-wait {
	font-size: 22rpx;
	font-weight: 450;
	padding: 4rpx 16rpx;
	border-radius: 9999px;
	color: #d97706;
	background: #fffbeb;
}

.popup-empty {
	padding: 48rpx 32rpx;
	text-align: center;
}

.popup-empty-text {
	font-size: 26rpx;
	color: #a3a6af;
}
</style>
