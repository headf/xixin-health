<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">就诊人档案</text>
			<view class="header-spacer"></view>
		</view>

		<!-- 加载态 -->
		<!-- 未登录 -->
		<view class="unlogin" v-if="!isLoggedIn && !loading">
			<view class="unlogin-card">
				<text class="unlogin-icon">👤</text>
				<text class="unlogin-tip">登录后管理就诊人</text>
				<button class="unlogin-btn" @tap="goLogin">登录 / 注册</button>
			</view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 列表 -->
		<view class="content" v-if="isLoggedIn && !loading">
			<view class="section-title">我的就诊人</view>

			<view class="profile-card" v-for="item in profiles" :key="item.id" @tap="showDetail(item)">
				<view class="card-row">
					<view class="avatar">{{ item.name.charAt(0) }}</view>
					<view class="info">
						<view class="name-row">
							<text class="name">{{ item.name }}</text>
							<text class="type-tag">{{ item.type === 'SELF' ? '本人' : '家人' }}</text>
						</view>
						<text class="phone" v-if="item.phone">{{ item.phone }}</text>
					</view>
					<text class="arrow">›</text>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="profiles.length === 0">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无就诊人档案</text>
				<text class="empty-desc">添加档案后可快速预约体检</text>
			</view>
		</view>

		<!-- 底部添加按钮 -->
		<view class="bottom-bar">
			<button class="add-btn" type="default" @tap="addProfile">添加就诊人</button>
		</view>
	</view>
</template>

<script>
import { profileApi } from '../../common/api.js'

export default {
	data() {
		return {
			isLoggedIn: false,
			loading: true,
			profiles: [],
			userId: null
		}
	},

	onShow() {
		this.isLoggedIn = uni.getStorageSync('accessToken') ? true : false
		uni.setNavigationBarColor({
			frontColor: '#000000',
			backgroundColor: '#ffffff'
		})
		if (this.isLoggedIn) { this.loadProfiles() } else { this.loading = false }
	},

	methods: {
		async loadProfiles() {
			this.loading = true
			this.userId = uni.getStorageSync('userId')
			try {
				const data = await profileApi.getProfiles(this.userId)
				this.profiles = data || []
			} catch (err) {
				console.error('加载档案失败', err)
			} finally {
				this.loading = false
			}
		},

		goBack() {
			uni.navigateBack()
		},

		addProfile() {
			// 清除残留的编辑缓存，确保添加页是空白表单
			uni.removeStorageSync("editProfile")
			uni.navigateTo({ url: "/pages/profile/edit" })
		},

		showDetail(item) {
			uni.navigateTo({ url: "/pages/profile/detail?id=" + item.id })
		},
			goLogin() { uni.navigateTo({ url: '/pages/login/login' }) }
	}
}
</script>

<style>
.page {
	min-height: 100vh;
	background: #f7f7f8;
	display: flex;
	flex-direction: column;
	padding-top: calc(var(--status-bar-height, 44px));
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
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	letter-spacing: -0.25rpx;
	padding: 12rpx 8rpx;
}

.back-btn:active { opacity: 0.6; }

.header-title {
	font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif;
	font-size: 40rpx;
	font-weight: 400;
	color: #17191c;
	letter-spacing: -0.3rpx;
	line-height: 1.1;
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
	padding: 24rpx 32rpx 160rpx;
}

.section-title {
	font-size: 28rpx;
	font-weight: 500;
	color: #777b86;
	letter-spacing: 0.5rpx;
	margin: 8rpx 8rpx 24rpx;
}

.profile-card {
	background: #ffffff;
	border-radius: 48rpx;
	padding: 28rpx 32rpx;
	margin-bottom: 20rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

.profile-card:active { opacity: 0.85; }

.card-row {
	display: flex;
	flex-direction: row;
	align-items: center;
}

.avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 9999px;
	background: #fbe1d1;
	display: flex;
	align-items: center;
	justify-content: center;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 32rpx;
	font-weight: 500;
	color: #17191c;
	margin-right: 24rpx;
	flex-shrink: 0;
}

.info { flex: 1; }

.name-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 6rpx;
}

.name {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 32rpx;
	font-weight: 500;
	color: #17191c;
}

.type-tag {
	font-size: 22rpx;
	font-weight: 430;
	color: #777b86;
	background: #f7f7f8;
	padding: 4rpx 16rpx;
	border-radius: 9999px;
}

.phone {
	font-size: 26rpx;
	color: #777b86;
}

.arrow {
	font-size: 36rpx;
	color: #a3a6af;
	margin-left: 16rpx;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 0;
}

.empty-icon { font-size: 64rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #17191c; margin-bottom: 8rpx; }
.empty-desc { font-size: 26rpx; color: #a3a6af; }

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 24rpx 32rpx 40rpx;
	background: linear-gradient(to top, #ffffff 60%, rgba(255,255,255,0));
	pointer-events: none;
}

.bottom-bar .add-btn { pointer-events: auto; }

.add-btn {
	height: 96rpx;
	line-height: 96rpx;
	background: #17191c;
	border-radius: 9999px;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx;
	font-weight: 450;
	color: #ffffff;
	letter-spacing: -0.27rpx;
	border: none !important;
	box-shadow: none;
	margin: 0;
}

.add-btn:active { opacity: 0.85; transform: scale(0.97); }

.unlogin { flex: 1; display: flex; align-items: center; justify-content: center; padding: 0 48rpx; }
.unlogin-card { display: flex; flex-direction: column; align-items: center; width: 100%; }
.unlogin-icon { font-size: 72rpx; margin-bottom: 24rpx; }
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
