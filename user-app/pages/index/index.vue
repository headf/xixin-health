<template>
	<view class="page">
		<view class="unlogin" v-if="!isLoggedIn">
			<view class="hero-section">
				<view class="hero-glow"></view>
				<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
				<text class="welcome-title">熙心健康</text>
				<text class="welcome-desc">专业体检管理，守护您的健康</text>
				<view class="action-buttons">
					<button class="cta-btn" type="default" @tap="goToLogin">登录</button>
					<text class="link-btn" @tap="goToRegister">注册账号</text>
				</view>
			</view>
		</view>

		<view class="logged-in" v-if="isLoggedIn">
			<view class="user-card">
				<view class="user-avatar"><text class="avatar-text">{{ userInitial }}</text></view>
				<view class="user-info">
					<text class="user-name">用户</text>
					<text class="user-phone">{{ maskedPhone }}</text>
				</view>
				<text class="logout-btn" @tap="handleLogout">退出</text>
			</view>

			<view class="menu-grid">
				<view class="menu-item" @tap="goToAppointments">
					<view class="menu-icon appointments">预</view>
					<text class="menu-label">我的预约</text>
				</view>
				<view class="menu-item" @tap="goToPackages">
					<view class="menu-icon packages">套</view>
					<text class="menu-label">体检套餐</text>
				</view>
				<view class="menu-item" @tap="goToOrders">
					<view class="menu-icon reports">订</view>
					<text class="menu-label">订单管理</text>
				</view>
				<view class="menu-item" @tap="goToProfiles">
					<view class="menu-icon profile">档</view>
					<text class="menu-label">个人档案</text>
				</view>
			</view>

			<view class="tip-section">
				<text class="tip-text">更多功能正在开发中，敬请期待</text>
			</view>
		</view>

		<view class="footer">
			<text class="copyright">熙心健康体检管理系统 v1.0</text>
		</view>
	</view>
</template>

<script>
import { tokenManager } from '../../common/api.js'

export default {
	data() {
		return { isLoggedIn: false, maskedPhone: '' }
	},
	computed: {
		userInitial() {
			if (this.maskedPhone != null && this.maskedPhone.length > 0)
				return this.maskedPhone.charAt(0)
			return 'U'
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.checkLoginStatus()
	},
	methods: {
		checkLoginStatus() {
			this.isLoggedIn = tokenManager.isLoggedIn()
			if (this.isLoggedIn) {
				this.maskedPhone = uni.getStorageSync('userPhone') || '138****0000'
			}
		},
		goToLogin() { uni.navigateTo({ url: '/pages/login/login' }) },
		goToRegister() { uni.navigateTo({ url: '/pages/register/register' }) },
		goToPackages() { uni.navigateTo({ url: '/pages/packages/packages' }) },
		goToProfiles() { uni.navigateTo({ url: "/pages/profile/list" }) },
		goToOrders() { uni.navigateTo({ url: "/pages/order/list" }) },
		goToNotifications() { uni.navigateTo({ url: "/pages/notification/list" }) },
		goToAppointments() { uni.navigateTo({ url: "/pages/appointment/list" }) },
		handleLogout() {
			uni.showModal({
				title: '提示', content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						tokenManager.clearToken()
						uni.removeStorageSync('userPhone')
						uni.removeStorageSync('userId')
						this.isLoggedIn = false
						uni.showToast({ title: '已退出', icon: 'success' })
					}
				}
			})
		},
		showComingSoon() { uni.showToast({ title: '功能开发中', icon: 'none', duration: 1500 }) }
	}
}
</script>

<style>
.page { min-height: 100vh; background: #ffffff; display: flex; flex-direction: column; padding-top: calc(var(--status-bar-height, 44px)); }
.unlogin { display: flex; flex-direction: column; align-items: center; flex: 1; }
.hero-section {
	width: 100%; display: flex; flex-direction: column; align-items: center;
	justify-content: center; padding-top: 96rpx; padding-bottom: 80rpx;
	position: relative; overflow: hidden; flex: 1;
	background: radial-gradient(ellipse at 50% 30%, rgba(251, 225, 209, 0.4) 0%, rgba(251, 225, 209, 0) 70%);
}
.hero-glow {
	position: absolute; top: -160rpx; left: 50%; transform: translateX(-50%);
	width: 600rpx; height: 600rpx;
	background: radial-gradient(circle, rgba(251, 225, 209, 0.5) 0%, rgba(251, 225, 209, 0) 70%);
	pointer-events: none;
}
.logo { width: 160rpx; height: 160rpx; border-radius: 40rpx; margin-bottom: 32rpx; position: relative; z-index: 1; }
.welcome-title {
	font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif;
	font-size: 88rpx; font-weight: 400; color: #17191c;
	letter-spacing: -3.3rpx; line-height: 1.1; margin-bottom: 16rpx; position: relative; z-index: 1;
}
.welcome-desc {
	font-size: 36rpx; font-weight: 400; color: #4c4c4c;
	letter-spacing: -0.32rpx; line-height: 1.35; margin-bottom: 64rpx; position: relative; z-index: 1;
}
.action-buttons { display: flex; flex-direction: row; align-items: center; gap: 24rpx; position: relative; z-index: 1; }
.cta-btn {
	height: 96rpx; line-height: 96rpx; padding: 0 40rpx; background: #17191c;
	border-radius: 9999px; font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx; font-weight: 450; color: #ffffff;
	letter-spacing: -0.27rpx; border: none; box-shadow: none; margin: 0;
}
.cta-btn:active { opacity: 0.85; transform: scale(0.97); }
.link-btn {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx; font-weight: 450; color: #17191c; letter-spacing: -0.27rpx; padding: 16rpx 24rpx;
}
.link-btn:active { opacity: 0.6; }
.logged-in { padding-top: 32rpx; }
.user-card {
	margin: 0 40rpx 24rpx 40rpx; padding: 32rpx 36rpx; background: #17191c;
	border-radius: 48rpx; display: flex; flex-direction: row; align-items: center;
}
.user-avatar {
	width: 88rpx; height: 88rpx; border-radius: 9999px; background: rgba(255,255,255,0.15);
	display: flex; align-items: center; justify-content: center; margin-right: 24rpx;
}
.avatar-text {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 34rpx; font-weight: 500; color: #ffffff;
}
.user-info { flex: 1; }
.user-name {
	font-size: 34rpx; font-weight: 500; color: #ffffff; letter-spacing: -0.3rpx; display: block;
}
.user-phone { font-size: 26rpx; color: rgba(255,255,255,0.65); margin-top: 8rpx; display: block; }
.logout-btn {
	font-size: 26rpx; color: rgba(255,255,255,0.7); padding: 12rpx 24rpx;
	border: 1px solid rgba(255,255,255,0.2); border-radius: 9999px;
}
.menu-grid {
	margin: 0 40rpx; display: flex; flex-direction: row; flex-wrap: wrap;
	justify-content: space-between; background: #ffffff; border-radius: 48rpx;
	padding: 40rpx 32rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}
.menu-item { width: 25%; display: flex; flex-direction: column; align-items: center; padding: 8rpx 0; }
.menu-icon {
	width: 96rpx; height: 96rpx; border-radius: 48rpx; display: flex; align-items: center;
	justify-content: center; font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 36rpx; font-weight: 500; color: #17191c; margin-bottom: 16rpx;
}
.menu-icon.appointments { background: #fbe1d1; }
.menu-icon.packages { background: #d3e3fc; }
.menu-icon.reports { background: #fbe1d1; }
.menu-icon.profile { background: #d3e3fc; }
.menu-label { font-size: 26rpx; color: #17191c; font-weight: 450; letter-spacing: -0.23rpx; }
.msg-link {
	display: flex; flex-direction: row; align-items: center;
	margin: 0 40rpx; padding: 28rpx 32rpx;
	background: #ffffff; border-radius: 48rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}
.msg-link:active { opacity: 0.85; }
.msg-link-icon { font-size: 36rpx; margin-right: 20rpx; }
.msg-link-text { font-size: 28rpx; font-weight: 450; color: #17191c; flex: 1; letter-spacing: -0.25rpx; }
.msg-link-arrow { font-size: 36rpx; color: #a3a6af; }
.tip-section { margin: 48rpx 40rpx; text-align: center; }
.tip-text { font-size: 24rpx; color: #a3a6af; }
.footer { position: fixed; bottom: 30rpx; left: 0; right: 0; text-align: center; }
.copyright { font-size: 22rpx; color: #a3a6af; }
</style>
