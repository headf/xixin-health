<template>
	<view class="page">
		<!-- 未登录状态 -->
		<view class="unlogin" v-if="!isLoggedIn">
			<view class="unlogin-card">
				<view class="unlogin-avatar">👤</view>
				<text class="unlogin-tip">登录后享受更多服务</text>
				<button class="unlogin-btn" @tap="goToLogin">登录 / 注册</button>
			</view>
		</view>
		<!-- 已登录状态 -->
		<scroll-view class="logged-in" v-if="isLoggedIn" scroll-y>
			<!-- ===== 用户资产信息卡 ===== -->
			<view class="profile-section">
				<view class="profile-bg"></view>
				<view class="profile-content">
					<view class="profile-avatar">👤</view>
					<view class="profile-info">
						<text class="profile-name">{{ displayName || '用户' }}</text>
						<text class="profile-phone">{{ maskedPhone }}</text>
					</view>
					<text class="profile-edit" @tap="goToProfileList">编辑 ›</text>
				</view>
			</view>
			<!-- ===== 预约/订单/报告 数据概览 ===== -->
			<view class="stats-card">
				<view class="stats-grid">
					<view class="stats-item" @tap="goToAppointments">
						<text class="stats-num">{{ stats.appointmentCount }}</text>
						<text class="stats-unit">次</text>
						<text class="stats-label">体检预约</text>
					</view>
					<view class="stats-item" @tap="goToOrders">
						<text class="stats-num">{{ stats.orderCount }}</text>
						<text class="stats-unit">单</text>
						<text class="stats-label">订单</text>
					</view>
					<view class="stats-item" @tap="goToReports">
						<text class="stats-num">{{ stats.reportCount }}</text>
						<text class="stats-unit">份</text>
						<text class="stats-label">体检报告</text>
					</view>
				</view>
			</view>
			<!-- ===== 菜单：核心服务 ===== -->
			<view class="menu-group">
				<view class="menu-header">核心服务</view>
				<view class="menu-item" @tap="goToPackages">
					<view class="menu-icon mi-pkg"></view>
					<text class="menu-text">体检套餐</text>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item" @tap="goToAppointments">
					<view class="menu-icon mi-appt"></view>
					<text class="menu-text">我的预约</text>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item" @tap="goToReports">
					<view class="menu-icon mi-report"></view>
					<text class="menu-text">体检报告</text>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item no-border" @tap="goToOrders">
					<view class="menu-icon mi-order"></view>
					<text class="menu-text">订单管理</text>
					<text class="menu-arrow">›</text>
				</view>
			</view>
			<!-- ===== 菜单：我的资产 ===== -->
			<view class="menu-group">
				<view class="menu-header">我的资产</view>
				<view class="menu-item" @tap="goToProfileList">
					<view class="menu-icon mi-profile"></view>
					<text class="menu-text">就诊人管理</text>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item no-border" @tap="goToGuide">
					<view class="menu-icon mi-guide"></view>
					<text class="menu-text">体检导诊</text>
					<text class="menu-arrow">›</text>
				</view>
			</view>
			<!-- ===== 菜单：更多服务 ===== -->
			<view class="menu-group">
				<view class="menu-header">更多服务</view>
				<view class="menu-item" @tap="goToConsult">
					<view class="menu-icon mi-consult"></view>
					<text class="menu-text">在线咨询</text>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item" @tap="goToNotifications">
					<view class="menu-icon mi-notif"></view>
					<text class="menu-text">消息通知</text>
					<view class="menu-badge" v-if="stats.unreadCount > 0">{{ stats.unreadCount > 99 ? '99+' : stats.unreadCount }}</view>
					<text class="menu-arrow">›</text>
				</view>
				<view class="menu-item" @tap="goToContact">
						<view class="menu-icon mi-contact"></view>
						<text class="menu-text">联系客服</text>
						<text class="menu-arrow">›</text>
					</view>
					<view class="menu-item no-border" @tap="showAbout">
					<view class="menu-icon mi-about"></view>
					<text class="menu-text">关于熙心健康</text>
					<text class="menu-arrow">›</text>
				</view>
			</view>
			<!-- ===== 退出登录 ===== -->
			<view class="logout-section">
				<button class="logout-btn" @tap="handleLogout">退出登录</button>
			</view>
		</scroll-view>
	</view>
</template>
<script>
import { tokenManager, appointmentApi, orderApi, notificationApi, reportApi } from '../../../common/api.js'
export default {
	data() {
		return {
			isLoggedIn: false,
			maskedPhone: '',
			displayName: '',
			stats: { appointmentCount: 0, orderCount: 0, reportCount: 0, unreadCount: 0 },
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.checkLogin()
		if (this.isLoggedIn) this.loadData()
	},
	methods: {
		checkLogin() {
			this.isLoggedIn = tokenManager.isLoggedIn()
			this.maskedPhone = uni.getStorageSync('userPhone') || ''
			this.displayName = uni.getStorageSync('userName') || '用户'
		},
		async loadData() {
			const userId = uni.getStorageSync('userId')
			if (!userId) return
			try {
				const [apps, orders, notifs, reports] = await Promise.all([
					appointmentApi.getByUser(userId).catch(() => []),
					orderApi.getByUser(userId).catch(() => []),
					notificationApi.getByUser(userId).catch(() => []),
					reportApi.getByUser(userId).catch(() => [])
				])
				this.stats.appointmentCount = (apps || []).length
				this.stats.orderCount = (orders || []).length
				this.stats.reportCount = (reports || []).length
				this.stats.unreadCount = (notifs || []).filter(n => !n.readFlag).length
			} catch (e) { console.error('加载数据失败', e) }
		},
		goToLogin() { uni.navigateTo({ url: '/pages/login/login' }) },
		goToAppointments() { uni.switchTab({ url: '/pages/appointment/list' }) },
		goToPackages() { uni.switchTab({ url: '/pages/packages/packages' }) },
		goToOrders() { uni.navigateTo({ url: '/pages/order/list' }) },
		goToReports() { uni.navigateTo({ url: '/pages/report/list' }) },
		goToProfileList() { uni.navigateTo({ url: '/pages/profile/list' }) },
		goToConsult() { uni.navigateTo({ url: '/pages/consult/index' }) },
		goToContact() { uni.navigateTo({ url: '/pages/contact/index' }) },
		goToNotifications() { uni.navigateTo({ url: '/pages/notification/list' }) },
		goToGuide() { uni.navigateTo({ url: '/pages/guide/index' }) },
		showAbout() {
			uni.showToast({ title: '熙心健康 v3.0', icon: 'none' })
		},
		handleLogout() {
			uni.showModal({
				title: '提示',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						tokenManager.clearToken()
						uni.removeStorageSync('userPhone')
						uni.removeStorageSync('userId')
						uni.removeStorageSync('userName')
						this.isLoggedIn = false
						uni.showToast({ title: '已退出', icon: 'success' })
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
/* ===== 未登录状态 ===== */
.unlogin {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 48rpx;
	padding-top: calc(var(--status-bar-height, 44px) + 40rpx);
}
.unlogin-card {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 100%;
}
.unlogin-avatar {
	width: 144rpx;
	height: 144rpx;
	border-radius: 9999px;
	background: var(--surface-page, #f8fafc);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 64rpx;
	margin-bottom: 24rpx;
	border: 4rpx solid var(--border, #e2e8f0);
}
.unlogin-tip { font-size: 28rpx; color: var(--text-secondary, #64748b); margin-bottom: 36rpx; }
.unlogin-btn {
	width: 100%;
	max-width: 400rpx;
	height: 88rpx;
	line-height: 88rpx;
	background: linear-gradient(135deg, #00B894, #00A080) !important;
	border-radius: 44rpx;
	font-size: 28rpx;
	font-weight: 600;
	color: #ffffff;
	text-align: center;
	border: none !important;
	box-shadow: 0 8rpx 24rpx rgba(0,184,148,0.3);
	letter-spacing: 2rpx;
}
button.unlogin-btn::after { border: none !important; }
.unlogin-btn:active {
	opacity: 0.9;
	transform: scale(0.97);
	box-shadow: 0 4rpx 12rpx rgba(0,184,148,0.2);
}

</style>
