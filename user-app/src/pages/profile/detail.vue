<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">个人档案</text>
			<view class="header-spacer"></view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="content" v-if="!loading && profile">
			<!-- 头像和姓名 -->
			<view class="avatar-section">
				<view class="avatar">{{ profile.name.charAt(0) }}</view>
				<text class="avatar-name">{{ profile.name }}</text>
				<text class="avatar-type">{{ profile.profileType === 'SELF' ? '本人' : '家人' }}</text>
			</view>

			<!-- 基本信息 -->
			<view class="info-card">
				<text class="card-title">基本信息</text>
				<view class="info-row">
					<text class="info-label">姓名</text>
					<text class="info-value">{{ profile.name }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">性别</text>
					<text class="info-value">{{ profile.idCardNo ? getIdCardGender(profile.idCardNo) : "未知" }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">年龄</text>
					<text class="info-value">{{ getIdCardAge(profile.idCardNo) }}岁</text>
				</view>
				<view class="info-row" v-if="profile.idCardNo">
					<text class="info-label">身份证号</text>
					<view class="info-value info-value-row">
						<text>{{ showIdCard ? profile.idCardNo : maskIdCard(profile.idCardNo) }}</text>
						<view class="eye-btn" @tap.stop="showIdCard=!showIdCard">
							<svg class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
								<template v-if="showIdCard">
									<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
									<circle cx="12" cy="12" r="3"/>
								</template>
								<template v-else>
									<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
									<path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
									<line x1="1" y1="1" x2="23" y2="23"/>
								</template>
							</svg>
						</view>
					</view>
				</view>
				<view class="info-row" v-else>
					<text class="info-label">身份证号</text>
					<text class="info-value empty">-</text>
				</view>
				<view class="info-row" v-if="profile.phone">
					<text class="info-label">联系电话</text>
					<text class="info-value">{{ profile.phone }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">档案类型</text>
					<text class="info-value">{{ profile.profileType === 'SELF' ? '本人' : '家人' }}</text>
				</view>
			</view>

			<!-- 健康信息 -->
			<view class="info-card">
				<text class="card-title">健康信息</text>
				<view class="info-row">
					<text class="info-label">过敏史</text>
					<text class="info-value" :class="{ empty: !profile.allergyHistory }">{{ profile.allergyHistory || '无' }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">既往病史</text>
					<text class="info-value" :class="{ empty: !profile.medicalHistory }">{{ profile.medicalHistory || '无' }}</text>
				</view>
			</view>

			<!-- 紧急联系人 -->
			<view class="info-card">
				<text class="card-title">紧急联系人</text>
				<view class="info-row" v-if="profile.emergencyContactName">
					<text class="info-label">联系人</text>
					<view class="info-value info-value-row">
						<text>{{ showContact ? profile.emergencyContactName : maskName(profile.emergencyContactName) }}</text>
						<view class="eye-btn" @tap.stop="showContact=!showContact">
							<svg class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
								<template v-if="showContact">
									<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
									<circle cx="12" cy="12" r="3"/>
								</template>
								<template v-else>
									<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
									<path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
									<line x1="1" y1="1" x2="23" y2="23"/>
								</template>
							</svg>
						</view>
					</view>
				</view>
				<view class="info-row" v-if="profile.emergencyContactPhone">
					<text class="info-label">联系电话</text>
					<view class="info-value info-value-row">
						<text>{{ showContactPhone ? profile.emergencyContactPhone : maskPhone(profile.emergencyContactPhone) }}</text>
						<view class="eye-btn" @tap.stop="showContactPhone=!showContactPhone">
							<svg class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
								<template v-if="showContactPhone">
									<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
									<circle cx="12" cy="12" r="3"/>
								</template>
								<template v-else>
									<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
									<path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
									<line x1="1" y1="1" x2="23" y2="23"/>
								</template>
							</svg>
						</view>
					</view>
				</view>
				<view class="info-row" v-if="!profile.emergencyContactName && !profile.emergencyContactPhone">
					<text class="info-value empty">未设置</text>
				</view>
			</view>

			<view class="edit-btn-wrap">
				<button class="edit-btn" type="default" @tap="goEdit">编辑档案</button>
			</view>
		</view>
	</view>
</template>

<script>
import { profileApi, getIdCardGender, getIdCardAge, maskIdCard } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			profile: null,
			profileId: null,
			showIdCard: false,
			showContact: false,
			showContactPhone: false
		}
	},

	onLoad(options) {
		if (options && options.id) {
			this.profileId = parseInt(options.id, 10)
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })

		// 直接从存储读取完整数据（编辑页保存后存入）
		const saved = uni.getStorageSync('selectedProfile')
		if (saved && saved.idCardNo) {
			this.profile = saved
			this.loading = false
		}

		// 不管有没有缓存，都从API拉最新全量数据
		const id = this.profileId || (saved ? saved.id : null)
		if (id) {
			this.loadProfile(id)
		} else if (!this.profile) {
			// 没有ID也没有缓存
			const data = uni.getStorageSync('selectedProfile')
			if (data && data.id) {
				this.profileId = data.id
				this.loadProfile(data.id)
			} else {
				this.loading = false
			}
		}
	},

	methods: {
		getIdCardGender(idCard) { return getIdCardGender(idCard) },
		getIdCardAge(idCard) { return getIdCardAge(idCard) },
		async loadProfile(id) {
			if (!id) id = this.profileId
			if (!id) { this.loading = false; return }
			try {
				const detail = await profileApi.getProfileDetail(id)
				if (detail) {
					this.profile = detail
					uni.setStorageSync('selectedProfile', detail)
				}
			} catch (e) {
				console.error('加载档案详情失败', e)
			} finally {
				this.loading = false
			}
		},

		goBack() { uni.navigateBack() },

		goEdit() {
			uni.setStorageSync('editProfile', this.profile)
			uni.navigateTo({ url: '/pages/profile/edit' })
		},

		maskIdCard(val) {
			if (!val) return '-'
			if (val.length <= 10) {
				const half = Math.floor(val.length / 2)
				return val.slice(0, Math.max(half, 2)) + '****' + val.slice(-Math.max(half, 2))
			}
			return val.slice(0, 6) + '****' + val.slice(-4)
		},

		maskName(val) {
			if (!val) return '-'
			if (val.length <= 1) return '*'
			return val[0] + '*'.repeat(val.length - 1)
		},

		maskPhone(val) {
			if (!val) return '-'
			if (val.length <= 7) return val.slice(0, 3) + '****'
			return val.slice(0, 3) + '****' + val.slice(-4)
		}
	}
}
</script>

<style>
.page {
	min-height: 100vh; background: #f7f7f8; display: flex; flex-direction: column;
	padding-top: calc(44px);
}

.header {
	display: flex; flex-direction: row; align-items: center; justify-content: space-between;
	padding: 16rpx 32rpx; background: #ffffff; position: relative;
}

.back-btn {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 450; color: #17191c;
	letter-spacing: -0.25rpx; padding: 12rpx 8rpx;
}
.back-btn:active { opacity: 0.6; }

.header-title {
	font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif;
	font-size: 40rpx; font-weight: 400; color: #17191c;
	letter-spacing: -0.3rpx; line-height: 1.1;
	position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap;
}

.header-spacer { width: 80rpx; }
.loading-state { flex: 1; display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 28rpx; color: #777b86; }

.content { flex: 1; padding: 24rpx 32rpx 40rpx; }

.avatar-section {
	display: flex; flex-direction: column; align-items: center;
	padding: 40rpx 0 32rpx;
}

.avatar {
	width: 120rpx; height: 120rpx; border-radius: 9999px;
	background: #fbe1d1; display: flex; align-items: center; justify-content: center;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 48rpx; font-weight: 500; color: #17191c;
	margin-bottom: 20rpx;
}

.avatar-name {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 40rpx; font-weight: 500; color: #17191c; margin-bottom: 8rpx;
}

.avatar-type {
	font-size: 24rpx; font-weight: 430; color: #777b86;
	background: #f7f7f8; padding: 6rpx 24rpx; border-radius: 9999px;
}

.info-card {
	background: #ffffff; border-radius: 48rpx; padding: 32rpx 32rpx;
	margin-bottom: 20rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

.card-title {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 500; color: #17191c;
	display: block; margin-bottom: 24rpx;
}

.info-row {
	display: flex; flex-direction: row; align-items: center;
	justify-content: space-between; padding: 14rpx 0;
	border-bottom: 1px solid #f7f7f8;
}
.info-row:last-child { border-bottom: none; }

.info-label { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 430; color: #777b86; }
.info-value { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; text-align: right; flex: 1; margin-left: 24rpx; }
.info-value-row { display: flex; align-items: center; justify-content: flex-end; gap: 12rpx; }
.info-value.empty { color: #a3a6af; font-weight: 430; }

/* 眼睛切换按钮 — 密码框样式 */
.eye-btn {
	width: 48rpx; height: 48rpx; border-radius: 50%;
	display: flex; align-items: center; justify-content: center;
	background: #f1f5f9; flex-shrink: 0;
	cursor: pointer;
}
.eye-btn:active { background: #e2e8f0; }

.eye-icon {
	width: 28rpx; height: 28rpx;
	color: #94a3b8;
}

.edit-btn-wrap { margin-top: 32rpx; }
.edit-btn {
	height: 88rpx; line-height: 88rpx; background: #ffffff; border-radius: 9999px;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 450; color: #17191c;
	border: 2rpx solid #17191c; box-shadow: none; margin: 0;
}
.edit-btn:active { opacity: 0.7; }
</style>
