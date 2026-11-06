<template>
	<view class="register-page">
		<!-- 注册表单 -->
		<view class="form-section">
			<text class="page-title">创建账号</text>
			<view class="form-card">
				<!-- 手机号 -->
				<view class="input-group">
					<input
						class="input-field"
						v-model="form.phone"
						type="text"
						placeholder="请输入手机号"
						maxlength="11"
						:disabled="form.loading"
						@input="clearError('phone')"
					/>
				</view>
				<text class="error-text" v-if="errors.phone != null && errors.phone.length > 0">
					{{ errors.phone }}
				</text>

				<!-- 姓名 -->
				<view class="input-group">
					<input
						class="input-field"
						v-model="form.displayName"
						type="text"
						placeholder="请输入姓名"
						maxlength="64"
						:disabled="form.loading"
						@input="clearError('displayName')"
					/>
				</view>
				<text class="error-text" v-if="errors.displayName != null && errors.displayName.length > 0">
					{{ errors.displayName }}
				</text>

				<!-- 邮箱（选填） -->
				<view class="input-group">
					<input
						class="input-field"
						v-model="form.email"
						type="text"
						placeholder="请输入邮箱（选填）"
						maxlength="50"
						:disabled="form.loading"
						@input="clearError('email')"
					/>
				</view>
				<text class="error-text" v-if="errors.email != null && errors.email.length > 0">
					{{ errors.email }}
				</text>

				<!-- 密码 -->
				<view class="input-group">
					<input
						class="input-field"
						v-model="form.password"
						:type="form.showPassword ? 'text' : 'password'"
						placeholder="请输入密码（8-64位）"
						maxlength="64"
						:disabled="form.loading"
						@input="clearError('password')"
					/>
					<text class="toggle-pwd" @tap="togglePassword">{{ form.showPassword ? '隐藏' : '显示' }}</text>
				</view>
				<text class="error-text" v-if="errors.password != null && errors.password.length > 0">
					{{ errors.password }}
				</text>

				<!-- 确认密码 -->
				<view class="input-group">
					<input
						class="input-field"
						v-model="form.confirmPassword"
						type="password"
						placeholder="请再次输入密码"
						maxlength="64"
						:disabled="form.loading"
						@input="clearError('confirmPassword')"
					/>
				</view>
				<text class="error-text" v-if="errors.confirmPassword != null && errors.confirmPassword.length > 0">
					{{ errors.confirmPassword }}
				</text>

				<!-- 注册按钮 — Steep Ink CTA -->
				<button
					class="register-btn"
					type="default"
					@tap="handleRegister"
					:loading="form.loading"
					:disabled="form.loading"
				>
					{{ form.loading ? '注册中...' : '注 册' }}
				</button>

				<!-- 底部操作区 -->
				<view class="footer-actions">
					<text class="link-text" @tap="goToLogin">已有账号？立即登录</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { authApi } from '../../common/api.js'

export default {
	data() {
		return {
			form: {
				phone: '',
				displayName: '',
				email: '',
				password: '',
				confirmPassword: '',
				showPassword: false,
				loading: false
			},
			errors: {
				phone: '',
				displayName: '',
				email: '',
				password: '',
				confirmPassword: ''
			}
		}
	},

	onShow() {
		// 设置状态栏文字为深色
		uni.setNavigationBarColor({
			frontColor: '#000000',
			backgroundColor: '#ffffff'
		})
	},

	methods: {
		clearError(field) {
			if (this.errors[field] != null && this.errors[field].length > 0) {
				this.errors[field] = ''
			}
		},

		togglePassword() {
			this.form.showPassword = !this.form.showPassword
		},

		validateForm() {
			let isValid = true
			this.errors = {
				phone: '',
				displayName: '',
				email: '',
				password: '',
				confirmPassword: ''
			}

			const phone = this.form.phone
			if (phone == null || phone.length === 0) {
				this.errors.phone = '请输入手机号'
				isValid = false
			} else if (!/^1[3-9]\d{9}$/.test(phone)) {
				this.errors.phone = '请输入正确的手机号格式'
				isValid = false
			}

			const displayName = this.form.displayName
			if (displayName == null || displayName.length === 0) {
				this.errors.displayName = '请输入姓名'
				isValid = false
			}

			const email = this.form.email
			if (email != null && email.length > 0) {
				if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
					this.errors.email = '请输入正确的邮箱格式'
					isValid = false
				}
			}

			const password = this.form.password
			if (password == null || password.length === 0) {
				this.errors.password = '请输入密码'
				isValid = false
			} else if (password.length < 8) {
				this.errors.password = '密码长度不能少于8位'
				isValid = false
			} else if (password.length > 64) {
				this.errors.password = '密码长度不能超过64位'
				isValid = false
			}

			const confirmPassword = this.form.confirmPassword
			if (confirmPassword == null || confirmPassword.length === 0) {
				this.errors.confirmPassword = '请再次输入密码'
				isValid = false
			} else if (confirmPassword !== password) {
				this.errors.confirmPassword = '两次输入的密码不一致'
				isValid = false
			}

			return isValid
		},

		async handleRegister() {
			if (!this.validateForm()) {
				return
			}

			this.form.loading = true

			try {
				const email = (this.form.email != null && this.form.email.length > 0)
					? this.form.email
					: undefined

				await authApi.register(
					this.form.phone,
					this.form.displayName,
					this.form.password,
					email
				)

				uni.setStorageSync('userName', this.form.displayName)

				uni.showToast({
					title: '注册成功',
					icon: 'success',
					duration: 1500
				})

				setTimeout(() => {
					uni.navigateBack()
				}, 1500)

			} catch (err) {
				let errorMsg = '注册失败，请重试'
				if (err != null && err.message != null) {
					if (err.code === 'AUTH-003') {
						errorMsg = '该手机号已注册'
					} else {
						errorMsg = err.message
					}
				}

				uni.showToast({
					title: errorMsg,
					icon: 'error',
					duration: 2000
				})
			} finally {
				this.form.loading = false
			}
		},

		goToLogin() {
			uni.navigateBack()
		}
	}
}
</script>

<style>
/* ===== Steep Design — Register Page ===== */

.register-page {
	min-height: 100vh;
	background: radial-gradient(ellipse at 50% 20%, rgba(251, 225, 209, 0.3) 0%, rgba(251, 225, 209, 0) 60%),
	              #ffffff;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60rpx 0;
	position: relative;
}

/* 页面标题 */
.page-title {
	font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif;
	font-size: 44rpx;
	font-weight: 400;
	color: #17191c;
	letter-spacing: -0.3rpx;
	line-height: 1.1;
	text-align: center;
	margin-bottom: 32rpx;
	display: block;
}

/* 表单区域 */
.form-section {
	width: 100%;
	padding: 0 60rpx;
	box-sizing: border-box;
}

.form-card {
	background: #ffffff;
	border-radius: 48rpx;
	padding: 48rpx 40rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

/* 输入组 */
.input-group {
	display: flex;
	flex-direction: row;
	align-items: center;
	background: #f7f7f8;
	border-radius: 32rpx;
	padding: 0 28rpx;
	height: 96rpx;
	border: 2rpx solid transparent;
	transition: all 0.2s ease;
	margin-bottom: 4rpx;
}

.input-group:focus-within {
	border-color: #17191c;
	background: #ffffff;
	box-shadow: 0 0 0 4rpx rgba(23, 25, 28, 0.06);
}

.input-field {
	flex: 1;
	height: 96rpx;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx;
	color: #17191c;
	background: transparent;
	border: none;
	outline: none;
}

.input-field::placeholder {
	color: #a3a6af;
	font-size: 26rpx;
}

/* 密码切换 */
.toggle-pwd {
	height: 96rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 24rpx;
	font-weight: 450;
	color: #777b86;
	padding: 0 8rpx;
}

/* 错误提示 */
.error-text {
	color: #5d2a1a;
	font-size: 22rpx;
	padding: 8rpx 16rpx 20rpx 16rpx;
	display: block;
}

/* 注册按钮 — Steep Ink Pill CTA */
.register-btn {
	margin-top: 40rpx;
	height: 96rpx;
	line-height: 96rpx;
	background: #17191c;
	border-radius: 9999px;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx;
	font-weight: 450;
	color: #ffffff;
	letter-spacing: -0.27rpx;
	border: none;
	box-shadow: none;
	transition: all 0.2s ease;
}

.register-btn:active {
	opacity: 0.85;
	transform: scale(0.97);
}

.register-btn[disabled] {
	opacity: 0.5;
}

/* 底部操作区 */
.footer-actions {
	display: flex;
	justify-content: center;
	margin-top: 40rpx;
}

.link-text {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	letter-spacing: -0.25rpx;
	padding: 10rpx;
}

.link-text:active {
	opacity: 0.6;
}
</style>
