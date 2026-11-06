<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="nav-bar">
			<text class="nav-title">我的预约</text>
		</view>

		<!-- 状态分类 Tab -->
		<scroll-view class="status-scroll" scroll-x show-scrollbar="false">
			<view class="status-row">
				<view
					v-for="tab in statusTabs"
					:key="tab.key"
					class="status-tab"
					:class="{ active: activeTab === tab.key }"
					@tap="activeTab = tab.key"
				>
					<text>{{ tab.label }}</text>
					<text class="tab-count" v-if="tab.count > 0">{{ tab.count }}</text>
				</view>
			</view>
		</scroll-view>

		<!-- 加载状态 -->
		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 内容列表 -->
		<scroll-view class="list-scroll" v-if="!loading" scroll-y>
			<view class="appointment-list">
				<view
					class="appt-card"
					v-for="item in filteredAppointments"
					:key="item.id"
					@tap="showDetail(item)"
				>
					<!-- 卡片顶部：编号 + 状态 -->
					<view class="card-top">
						<text class="appt-no">{{ item.appointmentNo || '预约单' }}</text>
						<view class="status-badge" :class="'sb-' + item.status">
							<text>{{ statusLabel(item.status) }}</text>
						</view>
					</view>

					<!-- 卡片主体 -->
					<view class="card-body">
						<view class="info-line">
							<text class="info-icon">📅</text>
							<text class="info-label">体检时间</text>
							<text class="info-value">{{ item.appointmentDate || '待定' }} {{ item.timeSlot || '' }}</text>
						</view>
						<view class="info-line">
							<text class="info-icon">🏥</text>
							<text class="info-label">体检机构</text>
							<text class="info-value">{{ branchName(item.branchId) }}</text>
						</view>
						<view class="info-line">
							<text class="info-icon">·</text>
							<text class="info-label">体检套餐</text>
							<text class="info-value">{{ packageName(item.packageId) }}</text>
						</view>
						<view class="info-line" v-if="item.profileId">
							<text class="info-icon">👤</text>
							<text class="info-label">体检人</text>
							<text class="info-value">{{ profileName(item.profileId) }}</text>
						</view>
					</view>

					<!-- 卡片底部：创建时间 + 操作按钮 -->
					<view class="card-footer">
						<view class="footer-left">
							<text class="created-label">创建于</text>
							<text class="created-time">{{ formatTime(item.createdAt) }}</text>
						</view>
						<view class="footer-actions">
							<button
								v-if="item.status === 'CREATED'"
								class="action-btn pay-btn"
								@tap.stop="goPay(item)"
							>去支付</button>
							<button
								v-if="item.status === 'PAID' || item.status === 'CHECKED_IN'"
								class="action-btn guide-btn"
								@tap.stop="goGuide(item)"
							>导诊路线</button>
							<button
								v-if="item.status === 'COMPLETED'"
								class="action-btn report-btn"
								@tap.stop="goReport(item)"
							>查看报告</button>
							<button
								v-if="item.status === 'CREATED' || item.status === 'PAID'"
								class="action-btn cancel-btn"
								@tap.stop="handleCancel(item)"
							>取消</button>
						</view>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="filteredAppointments.length === 0">
				<text class="empty-icon">📅</text>
				<text class="empty-text">暂无{{ activeTab === 'ALL' ? '' : statusLabel(activeTab) }}预约</text>
				<text class="empty-desc">选择一个套餐开始预约体检吧</text>
				<button class="empty-btn" @tap="goToPackages">去选购套餐</button>
			</view>

			<view class="bottom-safe"></view>
		</scroll-view>
	</view>
</template>

<script>
import { appointmentApi, packageApi, profileApi, reportApi, orderApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			appointments: [],
			filteredAppointments: [],
			packages: [],
			branches: [],
			profiles: [],
			userId: null,
			activeTab: 'ALL',
			statusTabs: [
				{ key: 'ALL', label: '全部', count: 0 },
				{ key: 'CREATED', label: '待支付', count: 0 },
				{ key: 'PAID', label: '待体检', count: 0 },
				{ key: 'CHECKED_IN', label: '体检中', count: 0 },
				{ key: 'COMPLETED', label: '已完成', count: 0 },
				{ key: 'CANCELLED', label: '已取消', count: 0 },
			]
		}
	},
	watch: {
		activeTab() { this.filterAppointments() }
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadData()
	},
	methods: {
		async loadData() {
			this.loading = true
			this.userId = uni.getStorageSync('userId')
			try {
				const [apps, pkgs, brs, profiles] = await Promise.all([
					appointmentApi.getByUser(this.userId),
					packageApi.getPackages(),
					packageApi.getBranches(),
					this.userId ? profileApi.getProfiles(this.userId) : Promise.resolve([])
				])
				this.appointments = (apps || []).sort((a, b) => new Date(b.createdAt || b.id) - new Date(a.createdAt || a.id))
				this.packages = pkgs || []
				this.branches = brs || []
				this.profiles = profiles || []

				// 更新各状态计数
				this.statusTabs.forEach(tab => {
					tab.count = tab.key === 'ALL'
						? this.appointments.length
						: this.appointments.filter(a => a.status === tab.key).length
				})
				this.filterAppointments()
			} catch (err) {
				console.error('加载预约失败', JSON.stringify(err))
			} finally { this.loading = false }
		},
		filterAppointments() {
			if (this.activeTab === 'ALL') {
				this.filteredAppointments = [...this.appointments]
			} else {
				this.filteredAppointments = this.appointments.filter(a => a.status === this.activeTab)
			}
		},

		// 格式化
		formatTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' },
		branchName(id) { const b = this.branches.find(x => x.id === id); return b ? b.branchName : '未知分院' },
		packageName(id) { const p = this.packages.find(x => x.id === id); return p ? p.packageName : '未知套餐' },
		profileName(id) { const p = this.profiles.find(x => x.id === id); return p ? p.name : '未知' },
		statusLabel(s) {
			return ({ CREATED: '待支付', PAID: '已支付', CHECKED_IN: '已签到', CANCELLED: '已取消', COMPLETED: '已完成' })[s] || s
		},

		// 导航
		showDetail(item) { uni.setStorageSync('selectedAppointment', item); uni.navigateTo({ url: '/pages/appointment/detail' }) },
		goToPackages() { uni.switchTab({ url: '/pages/packages/packages' }) },
		goPay(item) {
			const pkg = this.packages.find(x => x.id === item.packageId)
			const paymentInfo = {
				appointmentId: item.id,
				appointmentNo: item.appointmentNo,
				amountCent: pkg ? pkg.priceCent : 29900
			}
			uni.setStorageSync('paymentInfo', paymentInfo)
			uni.navigateTo({ url: '/pages/order/payment' })
		},
		goGuide() { uni.navigateTo({ url: '/pages/guide/index' }) },
		goReport(item) {
			// 尝试通过 examRecordId 跳转报告
			uni.setStorageSync('selectedReport', { appointmentId: item.id })
			uni.navigateTo({ url: '/pages/report/list' })
		},
		async handleCancel(item) {
			uni.showModal({
				title: '取消预约',
				content: '确定要取消此预约吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							await appointmentApi.cancel(item.id)
							item.status = 'CANCELLED'
							this.filterAppointments()
							uni.showToast({ title: '已取消', icon: 'success' })
						} catch (err) {
							uni.showToast({ title: err.message || '取消失败', icon: 'error' })
						}
					}
				}
			})
		}
	}
}
</script>

<style>
/* ===== 页面容器 ===== */
.page {
	min-height: 100vh;
	background: var(--surface-page, #f8fafc);
	display: flex;
	flex-direction: column;
}

/* ===== 顶部导航 ===== */
.nav-bar {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	padding: 18rpx 28rpx 8rpx;
	padding-top: calc(var(--status-bar-height, 44px) + 24rpx);
	background: var(--surface-page, #f8fafc);
}
.nav-title {
	font-size: 34rpx;
	font-weight: 700;
	color: var(--text, #1e293b);
}

/* ===== 状态分类 Tab ===== */
.status-scroll {
	padding: 12rpx 0 8rpx;
	background: var(--surface-page, #f8fafc);
}
.status-row {
	display: flex;
	flex-direction: row;
	padding: 0 20rpx;
	gap: 10rpx;
}
.status-tab {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 12rpx 24rpx;
	font-size: 24rpx;
	color: var(--text-secondary, #64748b);
	background: var(--surface, #ffffff);
	border-radius: 9999px;
	border: 2rpx solid var(--border, #e2e8f0);
	white-space: nowrap;
	gap: 8rpx;
}
.status-tab:active { opacity: 0.7; }
.status-tab.active {
	color: var(--brand);
	background: var(--brand-light, #e0f2fe);
	border-color: var(--brand-light, #e0f2fe);
	font-weight: 600;
}
.tab-count {
	font-size: 18rpx;
	color: inherit;
	background: rgba(14,165,233,0.15);
	padding: 0 8rpx;
	min-width: 24rpx;
	height: 24rpx;
	line-height: 24rpx;
	border-radius: 12rpx;
	text-align: center;
}
.status-tab.active .tab-count { background: rgba(14,165,233,0.2); }

/* ===== 加载状态 ===== */
.loading-state { flex: 1; display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 26rpx; color: var(--text-secondary, #64748b); }

/* ===== 预约列表 ===== */
.list-scroll { flex: 1; }
.appointment-list { padding: 12rpx 24rpx 140rpx; display: flex; flex-direction: column; gap: 20rpx; }

/* ===== 预约卡片 ===== */
.appt-card {
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	overflow: hidden;
	box-shadow: var(--shadow-sm);
}
.appt-card:active { opacity: 0.92; }

/* 卡片顶栏 */
.card-top {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 24rpx 0;
}
.appt-no { font-size: 22rpx; color: var(--text-placeholder, #94a3b8); }

/* 状态标签 */
.status-badge {
	font-size: 22rpx;
	font-weight: 500;
	padding: 6rpx 18rpx;
	border-radius: 9999px;
}
.sb-CREATED { color: #d97706; background: var(--warning-bg, #fef3c7); }
.sb-PAID { color: var(--brand-dark, #0284c7); background: var(--brand-light, #e0f2fe); }
.sb-CHECKED_IN { color: var(--secondary, #0d9488); background: var(--secondary-light, #ccfbf1); }
.sb-CANCELLED { color: var(--text-disabled, #cbd5e1); background: var(--surface-page, #f8fafc); }
.sb-COMPLETED { color: var(--text-secondary, #64748b); background: var(--surface-hover, #f1f5f9); }

/* 卡片信息 */
.card-body { padding: 16rpx 24rpx 12rpx; }
.info-line {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin-bottom: 10rpx;
	gap: 10rpx;
}
.info-icon { font-size: 24rpx; width: 28rpx; text-align: center; flex-shrink: 0; }
.info-label { font-size: 24rpx; color: var(--text-secondary, #64748b); width: 100rpx; flex-shrink: 0; }
.info-value { font-size: 26rpx; color: var(--text, #1e293b); flex: 1; text-align: right; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 卡片底部 */
.card-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 12rpx 24rpx 20rpx;
	border-top: 2rpx solid var(--border-light, #f1f5f9);
}
.footer-left {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 6rpx;
}
.created-label { font-size: 20rpx; color: var(--text-placeholder, #94a3b8); }
.created-time { font-size: 20rpx; color: var(--text-placeholder, #94a3b8); }

.footer-actions {
	display: flex;
	flex-direction: row;
	gap: 12rpx;
}
.action-btn {
	height: 52rpx;
	line-height: 52rpx;
	padding: 0 20rpx;
	font-size: 22rpx;
	font-weight: 500;
	border-radius: 9999px;
}
.pay-btn { background: var(--brand); color: #ffffff; }
.pay-btn:active { opacity: 0.85; }
.guide-btn { background: var(--surface, #ffffff); color: var(--brand); border: 2rpx solid var(--brand, #7c9fff); }
.guide-btn:active { opacity: 0.7; }
.report-btn { background: var(--secondary, #0d9488); color: #ffffff; }
.report-btn:active { opacity: 0.85; }
.cancel-btn { background: transparent; color: var(--danger, #ef4444); border: 2rpx solid var(--danger-bg, #fee2e2); }
.cancel-btn:active { opacity: 0.7; }

/* ===== 空状态 ===== */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 100rpx 32rpx;
}
.empty-icon { font-size: 72rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 28rpx; color: var(--text, #1e293b); margin-bottom: 8rpx; }
.empty-desc { font-size: 24rpx; color: var(--text-secondary, #64748b); margin-bottom: 32rpx; }
.empty-btn {
	height: 72rpx;
	line-height: 72rpx;
	padding: 0 48rpx;
	background: var(--brand);
	color: #ffffff;
	font-size: 26rpx;
	font-weight: 500;
	border-radius: 9999px;
}
.empty-btn:active { opacity: 0.85; }

.bottom-safe { height: 40rpx; }
</style>
