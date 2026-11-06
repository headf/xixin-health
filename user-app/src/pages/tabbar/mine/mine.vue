<template>
	<view class="page">
		<!-- 未登录状态 -->
		<view class="unlogin" v-if="!isLoggedIn">
			<view class="unlogin-card">
				<view class="unlogin-avatar"><text class="avatar-text">我</text></view>
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
					<view class="profile-avatar"><text class="avatar-text">我</text></view>
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
import { tokenManager, appointmentApi, orderApi, notificationApi, reportApi, profileApi, getIdCardGender, getIdCardAge, maskIdCard } from '../../../common/api.js'
export default {
	data() {
		return {
			isLoggedIn: false,
			maskedPhone: '',
			displayName: '',
			userProfile: null,
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
			let phone = uni.getStorageSync('userPhone') || ''
			let name = uni.getStorageSync('userName') || ''
			if (!phone || !name) {
				try {
					const token = uni.getStorageSync('accessToken')
					if (token) {
						const parts = token.split('.')
						if (parts.length === 3) {
							const decoded = decodeURIComponent(atob(parts[1]).split('').map(c =>
								'%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
							).join(''))
							const claims = JSON.parse(decoded)
							if (!phone) phone = claims.phone || claims.phoneNumber || claims.mobile || claims.sub || ''
							if (!name) name = claims.displayName || claims.name || claims.nickname || ''
						}
					}
				} catch (e) { /* ignore */ }
				if (phone) uni.setStorageSync('userPhone', phone)
				if (name) uni.setStorageSync('userName', name)
			}
			this.maskedPhone = phone.length === 11 ? phone.substring(0, 3) + '****' + phone.substring(7) : (phone || '')
			this.displayName = name || (phone ? phone.substring(0, 3) + '****' + phone.substring(7) : '用户')
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
	background: var(--brand);
	border-radius: 9999px;
	font-size: 28rpx;
	font-weight: 500;
	color: #ffffff;
	text-align: center;
}
.unlogin-btn:active { opacity: 0.85; }
/* ===== 已登录 ===== */
.logged-in { flex: 1; }
/* ===== 用户资产信息卡 ===== */
.profile-section {
	position: relative;
	overflow: hidden;
	padding-bottom: 24rpx;
}
.profile-bg {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 100%;
	background: linear-gradient(135deg, var(--brand) 0%, var(--secondary) 100%);
}
.profile-content {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 36rpx 28rpx 24rpx;
	padding-top: calc(var(--status-bar-height, 44px) + 28rpx);
}
.profile-avatar {
	width: 112rpx;
	height: 112rpx;
	border-radius: 9999px;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 48rpx;
	margin-right: 24rpx;
	background: rgba(255,255,255,0.25);
	border: 4rpx solid rgba(255,255,255,0.3);
	flex-shrink: 0;
}
.profile-info { flex: 1; }
.profile-name { font-size: 34rpx; font-weight: 700; color: #ffffff; display: block; margin-bottom: 6rpx; }
.profile-phone { font-size: 24rpx; color: rgba(255,255,255,0.7); }
.profile-idcard { font-size: 22rpx; color: rgba(255,255,255,0.5); margin-top: 4rpx; }
.profile-edit {
	font-size: 24rpx;
	color: rgba(255,255,255,0.8);
	padding: 10rpx 18rpx;
	border: 2rpx solid rgba(255,255,255,0.3);
	border-radius: 9999px;
}
.profile-edit:active { opacity: 0.7; }


/* ===== 数据概览卡片 ===== */
.stats-card {
	margin: -16rpx 24rpx 20rpx;
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	box-shadow: var(--shadow-sm);
	position: relative;
	z-index: 2;
}
.stats-grid {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 24rpx 8rpx;
}
.stats-item {
	flex: 1;
	display: flex;
	flex-direction: row;
	align-items: baseline;
	justify-content: center;
	flex-wrap: wrap;
	gap: 2rpx;
	padding: 8rpx 0;
}
.stats-item:active { opacity: 0.6; }
.stats-num { font-size: 40rpx; font-weight: 700; color: var(--brand, #0ea5e9); }
.stats-unit { font-size: 22rpx; color: var(--brand, #0ea5e9); margin-right: 8rpx; }
.stats-label { width: 100%; font-size: 22rpx; color: var(--text-secondary, #64748b); text-align: center; margin-top: 4rpx; }
/* ===== 菜单组 ===== */
.menu-group {
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	margin: 0 24rpx 20rpx;
	overflow: hidden;
	box-shadow: var(--shadow-sm);
}
.menu-header {
	font-size: 22rpx;
	color: var(--text-placeholder, #94a3b8);
	padding: 24rpx 28rpx 12rpx;
	font-weight: 500;
}
.menu-item {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 26rpx 28rpx;
	border-bottom: 2rpx solid var(--border-light, #f1f5f9);
	min-height: 48rpx;
}
.menu-item:active { background: var(--surface-hover, #f1f5f9); }
.menu-item.no-border { border-bottom: none; }
.menu-icon {
	width: 40rpx;
	height: 40rpx;
	background-size: contain;
	background-position: center;
	background-repeat: no-repeat;
	margin-right: 20rpx;
	flex-shrink: 0;
}
.mi-pkg    { background-image: url('../../static/menu_pkg.png'); }
.mi-appt   { background-image: url('../../static/menu_appt.png'); }
.mi-report { background-image: url('../../static/menu_report.png'); }
.mi-order  { background-image: url('../../static/menu_order.png'); }
.mi-profile { background-image: url('../../static/menu_profile.png'); }
.mi-guide  { background-image: url('../../static/menu_report.png'); }
.mi-consult { background-image: url('../../static/menu_consult.png'); }
.mi-notif  { background-image: url('../../static/menu_notif.png'); }
.mi-about  { background-image: url('../../static/menu_setting.png'); }
.mi-contact { background-image: url('../../static/menu_consult.png'); }
.menu-text {
	font-size: 28rpx;
	font-weight: 450;
	color: var(--text, #1e293b);
	flex: 1;
}
.menu-badge {
	min-width: 28rpx;
	height: 28rpx;
	line-height: 28rpx;
	padding: 0 8rpx;
	border-radius: 14rpx;
	background: var(--danger, #ef4444);
	color: #ffffff;
	font-size: 18rpx;
	font-weight: 600;
	text-align: center;
	margin-right: 8rpx;
}
.menu-arrow { font-size: 28rpx; color: var(--text-disabled, #cbd5e1); }
/* ===== 退出登录 ===== */
.logout-section {
	margin: 20rpx 24rpx 60rpx;
}
.logout-btn {
	width: 100%;
	height: 80rpx;
	line-height: 80rpx;
	background: var(--surface, #ffffff);
	border-radius: 9999px;
	font-size: 26rpx;
	font-weight: 450;
	color: var(--danger, #ef4444);
	border: 2rpx solid var(--danger-bg, #fee2e2);
	text-align: center;
}
.logout-btn:active { background: var(--surface-hover, #f1f5f9); }
</style>
