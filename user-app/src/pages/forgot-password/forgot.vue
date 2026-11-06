<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">忘记密码</text>
			<view class="header-spacer"></view>
		</view>

		<view class="content">
			<view class="form-card">
				<view class="step-indicator">
					<text class="step-dot" :class="{ active: step === 1 }">1</text>
					<text class="step-line"></text>
					<text class="step-dot" :class="{ active: step === 2 }">2</text>
				</view>

				<!-- 第一步：输入账号 -->
				<template v-if="step === 1">
					<text class="form-label">登录账号</text>
					<input class="input-field" v-model="loginId" placeholder="请输入手机号或邮箱" maxlength="50" />
					<text class="error-text" v-if="error">{{ error }}</text>
					<button class="next-btn" type="default" @tap="goStep2">下一步</button>
				</template>

				<!-- 第二步：重置密码 -->
				<template v-if="step === 2">
					<text class="form-label">新密码</text>
					<input class="input-field" v-model="password" :type="showPwd ? 'text' : 'password'" placeholder="请设置新密码（至少8位）" maxlength="64" />

					<text class="form-label" style="margin-top: 20rpx;">确认密码</text>
					<input class="input-field" v-model="confirmPassword" :type="showPwd ? 'text' : 'password'" placeholder="请再次输入新密码" maxlength="64" />

					<view class="eye-row">
						<label class="eye-label" @tap="showPwd=!showPwd">
							<text class="eye-checkbox" :class="{ checked: showPwd }">✓</text>
							<text class="eye-text">显示密码</text>
						</label>
					</view>

					<text class="error-text" v-if="error">{{ error }}</text>
					<button class="next-btn" type="default" :loading="submitting" :disabled="submitting" @tap="handleReset">
						{{ submitting ? '重置中...' : '重置密码' }}
					</button>
				</template>

				<!-- 成功状态 -->
				<template v-if="step === 3">
					<view class="success-section">
						<text class="success-icon">✓</text>
						<text class="success-title">密码重置成功</text>
						<text class="success-desc">请使用新密码重新登录</text>
						<button class="next-btn" type="default" @tap="goLogin">返回登录</button>
					</view>
				</template>
			</view>
		</view>
	</view>
</template>

<script>
import { authApi, request } from '../../common/api.js'

export default {
	data() {
		return {
			step: 1,
			loginId: '',
			password: '',
			confirmPassword: '',
			showPwd: false,
			submitting: false,
			error: ''
		}
	},
	methods: {
		goBack() { uni.navigateBack() },
		goLogin() { uni.redirectTo({ url: '/pages/login/login' }) },

		goStep2() {
			this.error = ''
			if (!this.loginId.trim()) {
				this.error = '请输入手机号或邮箱'
				return
			}
			this.step = 2
		},

		async handleReset() {
			this.error = ''
			if (!this.password || this.password.length < 8) {
				this.error = '密码长度不能少于8位'
				return
			}
			if (this.password !== this.confirmPassword) {
				this.error = '两次密码输入不一致'
				return
			}

			this.submitting = true
			try {
				await request('POST', '/api/v1/auth/reset-password', {
					loginId: this.loginId.trim(),
					newPassword: this.password
				}, false)
				this.step = 3
			} catch (e) {
				this.error = e.message || '重置失败，请检查账号是否存在'
			} finally {
				this.submitting = false
			}
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
	font-size: 28rpx; font-weight: 450; color: #17191c;
	padding: 12rpx 8rpx;
}
.back-btn:active { opacity: 0.6; }

.header-title {
	font-size: 40rpx; font-weight: 400; color: #17191c;
	position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap;
}

.header-spacer { width: 80rpx; }

.content { flex: 1; padding: 40rpx 32rpx; }

.form-card {
	background: #ffffff; border-radius: 48rpx; padding: 48rpx 40rpx;
	box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}

.step-indicator {
	display: flex; align-items: center; justify-content: center;
	margin-bottom: 48rpx; gap: 24rpx;
}

.step-dot {
	width: 48rpx; height: 48rpx; border-radius: 50%;
	display: flex; align-items: center; justify-content: center;
	background: #f1f5f9; color: #94a3b8; font-size: 24rpx; font-weight: 600;
}
.step-dot.active { background: #17191c; color: #ffffff; }

.step-line {
	width: 80rpx; height: 2rpx; background: #e2e8f0;
}

.form-label {
	font-size: 28rpx; font-weight: 500; color: #17191c;
	display: block; margin-bottom: 16rpx;
}

.input-field {
	width: 100%; height: 88rpx; background: #f7f7f8; border-radius: 24rpx;
	padding: 0 24rpx; font-size: 28rpx; color: #17191c;
	border: 2rpx solid transparent; box-sizing: border-box;
}
.input-field::placeholder { color: #a3a6af; }

.error-text { font-size: 24rpx; color: #e53e3e; display: block; margin-top: 16rpx; }

.eye-row {
	display: flex; align-items: center; margin-top: 16rpx;
}
.eye-label {
	display: flex; align-items: center; gap: 12rpx;
}
.eye-checkbox {
	width: 32rpx; height: 32rpx; border-radius: 8rpx;
	border: 2rpx solid #cbd5e1; display: flex; align-items: center; justify-content: center;
	font-size: 20rpx; color: transparent; background: #ffffff;
}
.eye-checkbox.checked { background: #17191c; border-color: #17191c; color: #ffffff; }
.eye-text { font-size: 24rpx; color: #64748b; }

.next-btn {
	margin-top: 40rpx; height: 88rpx; line-height: 88rpx;
	background: #17191c; border-radius: 9999px;
	font-size: 28rpx; font-weight: 500; color: #ffffff;
	border: none; width: 100%; text-align: center;
}
.next-btn:active { opacity: 0.85; }
.next-btn[disabled] { opacity: 0.5; }

.success-section { display: flex; flex-direction: column; align-items: center; padding: 40rpx 0; }
.success-icon {
	width: 96rpx; height: 96rpx; border-radius: 50%;
	background: #22c55e; color: #ffffff; font-size: 48rpx;
	display: flex; align-items: center; justify-content: center;
	margin-bottom: 24rpx;
}
.success-title { font-size: 36rpx; font-weight: 600; color: #17191c; margin-bottom: 12rpx; }
.success-desc { font-size: 28rpx; color: #64748b; margin-bottom: 40rpx; }
</style>
