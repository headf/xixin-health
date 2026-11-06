<template>
	<view class="login-page">
		<view class="brand-section">
			<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
			<text class="app-name">熙心健康</text>
			<text class="app-desc">体检管理系统</text>
		</view>

		<view class="form-section">
			<view class="form-card">
				<view class="input-group">
					<input
						class="input-field"
						v-model="form.loginId"
						type="text"
						placeholder="请输入手机号或邮箱"
						maxlength="50"
						:disabled="form.loading"
						@input="clearError('loginId')"
					/>
				</view>
				<text class="error-text" v-if="errors.loginId != null && errors.loginId.length > 0">
					{{ errors.loginId }}
				</text>

				<view class="input-group">
					<input
						class="input-field"
						v-model="form.password"
						:type="form.showPassword ? 'text' : 'password'"
						placeholder="请输入密码"
						maxlength="64"
						:disabled="form.loading"
						@input="clearError('password')"
					/>
					<text class="toggle-pwd" @tap="togglePassword">{{ form.showPassword ? '隐藏' : '显示' }}</text>
				</view>
				<text class="error-text" v-if="errors.password != null && errors.password.length > 0">
					{{ errors.password }}
				</text>

				<view class="remember-row">
					<label class="remember-label" @tap="form.rememberPassword=!form.rememberPassword">
						<text class="remember-checkbox" :class="{ checked: form.rememberPassword }">✓</text>
						<text class="remember-text">记住密码</text>
					</label>
				</view>

				<button
					class="login-btn"
					type="default"
					@tap="handleLogin"
					:loading="form.loading"
					:disabled="form.loading"
				>
					{{ form.loading ? '登录中...' : '登 录' }}
				</button>

				<view class="footer-actions">
					<text class="link-text" @tap="goToRegister">还没有账号？立即注册</text>
				</view>
				<view class="forgot-row">
					<text class="link-text" @tap="goForgot">忘记密码？</text>
				</view>
				<view class="agree-row">
					<label class="agree-label" @tap="form.agreed=!form.agreed">
						<text class="agree-checkbox" :class="{ checked: form.agreed }">✓</text>
						<text class="agree-text">已阅读并同意<text class="agree-link" @tap.stop="goAgreement('user')">《用户协议》</text>和<text class="agree-link" @tap.stop="goAgreement('privacy')">《隐私政策》</text></text>
					</label>
				</view>
			</view>
		</view>

		<view class="footer">
			<text class="copyright">熙心健康体检管理系统 v1.0</text>
		</view>
	</view>
</template>

<script>
import { authApi, tokenManager } from '../../common/api.js'

function parseJwt(token) {
	try {
		const parts = token.split('.')
		if (parts.length !== 3) return null
		const payload = parts[1]
		const decoded = decodeURIComponent(atob(payload).split('').map(c =>
			'%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
		).join(''))
		return JSON.parse(decoded)
	} catch (e) {
		return null
	}
}

export default {
	data() {
		return {
			form: { loginId: '', password: '', showPassword: false, loading: false, rememberPassword: false, agreed: false },
			errors: { loginId: '', password: '' }
		}
	},

	onLoad() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		if (tokenManager.isLoggedIn()) {
			uni.switchTab({ url: '/pages/tabbar/home/home' })
		}
		this.loadRemembered()
	},

	methods: {
		validateForm() {
			let isValid = true
			this.errors = { loginId: '', password: '' }
			if (this.form.loginId == null || this.form.loginId.length === 0) {
				this.errors.loginId = '请输入手机号或邮箱'; isValid = false
			}
			const pwd = this.form.password
			if (pwd == null || pwd.length === 0) {
				this.errors.password = '请输入密码'; isValid = false
			} else if (pwd.length < 8) {
				this.errors.password = '密码长度不能少于8位'; isValid = false
			}
			return isValid
		},

		clearError(field) {
			if (this.errors[field] != null && this.errors[field].length > 0) {
				this.errors[field] = ''
			}
		},

		togglePassword() { this.form.showPassword = !this.form.showPassword },

		loadRemembered() {
			const savedId = uni.getStorageSync('remembered_login_id')
			const savedPwd = uni.getStorageSync('remembered_password')
			if (savedId) {
				this.form.loginId = savedId
				this.form.rememberPassword = !!savedPwd
				if (savedPwd) {
					this.form.password = savedPwd
				}
			}
		},

		async handleLogin() {
			if (!this.validateForm()) return
			if (!this.form.agreed) {
				uni.showToast({ title: '请先阅读并同意用户协议和隐私政策', icon: 'none', duration: 2000 })
				return
			}

			this.form.loading = true
			try {
				const result = await authApi.login(this.form.loginId, this.form.password)
				tokenManager.saveToken(result)

				// 从 JWT 中解析用户信息并保存
				const claims = parseJwt(result.accessToken)
				console.log('JWT claims:', JSON.stringify(claims))
				if (claims) {
					if (claims.sub != null) {
						uni.setStorageSync('userId', Number(claims.sub))
					}
					if (claims.phone != null) {
						uni.setStorageSync('userPhone', claims.phone)
					}
					if (claims.displayName != null && claims.displayName.length > 0) {
						uni.setStorageSync('userName', claims.displayName)
					}
				}

				// 兜底：如果 JWT 中没有 phone，用登录输入
				if (!uni.getStorageSync('userPhone')) {
					uni.setStorageSync('userPhone', this.form.loginId)
				}
				// 兜底：如果 JWT 中没有 displayName，用手机号掩码显示
				if (!uni.getStorageSync('userName')) {
					const phone = uni.getStorageSync('userPhone') || ''
					uni.setStorageSync('userName', phone.length > 0 ? phone.substring(0, 3) + '****' + phone.substring(7) : '用户')
				}

				// 记住密码处理
				if (this.form.rememberPassword) {
					uni.setStorageSync('remembered_login_id', this.form.loginId)
					uni.setStorageSync('remembered_password', this.form.password)
				} else {
					uni.removeStorageSync('remembered_password')
					uni.removeStorageSync('remembered_login_id')
				}

				uni.showToast({ title: '登录成功', icon: 'success', duration: 1500 })
				setTimeout(() => uni.switchTab({ url: '/pages/tabbar/home/home' }), 1500)

			} catch (err) {
				let msg = '登录失败'
				if (err != null && err.message != null) {
					if (err.code === 'AUTH-001') msg = '账号或密码错误'
					else if (err.code === 'AUTH-002') msg = '账号已被禁用'
				}
				uni.showToast({ title: msg, icon: 'error', duration: 2000 })
			} finally {
				this.form.loading = false
			}
		},

		goToRegister() {
			uni.navigateTo({ url: '/pages/register/register' })
		},
		goForgot() {
			uni.navigateTo({ url: '/pages/forgot-password/forgot' })
		},
		goAgreement(type) {
			const url = type === 'user' ? '/pages/agreement/user-agreement' : '/pages/agreement/privacy-policy'
			uni.navigateTo({ url })
		}
	}
}
</script>

<style>
.login-page {
	min-height: 100vh;
	background: radial-gradient(ellipse at 50% 20%, rgba(107, 159, 255, 0.12) 0%, rgba(107, 159, 255, 0) 60%), var(--surface-page, #f8fafc);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40rpx 0 120rpx;
	position: relative;
}

.brand-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-bottom: 40rpx;
}

.logo {
	width: 128rpx; height: 128rpx; border-radius: 32rpx; margin-bottom: 24rpx;
}

.app-name {
	font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif;
	font-size: 52rpx; font-weight: 400; color: var(--brand, #6b9fff);
	letter-spacing: -0.46rpx; line-height: 1.18; margin-bottom: 8rpx;
}

.app-desc { font-size: 28rpx; color: var(--text-secondary, #64748b); letter-spacing: -0.26rpx; }

.form-section { width: 100%; padding: 0 60rpx; box-sizing: border-box; }

.form-card {
	background: #ffffff; border-radius: 48rpx; padding: 48rpx 40rpx;
	box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}

.input-group {
	display: flex; flex-direction: row; align-items: center;
	background: var(--surface-hover, #f1f5f9); border-radius: 24rpx;
	padding: 0 28rpx; height: 96rpx;
	border: 2rpx solid transparent; transition: all 0.2s ease; margin-bottom: 20rpx;
}

.input-group:focus-within {
	border-color: #17191c; background: #ffffff;
	box-shadow: 0 0 0 4rpx rgba(23, 25, 28, 0.06);
}

.input-field {
	flex: 1; height: 96rpx;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx; color: #17191c; background: transparent; border: none; outline: none;
}

.input-field::placeholder { color: var(--text-placeholder, #94a3b8); font-size: 28rpx; }

.toggle-pwd {
	height: 96rpx; display: flex; align-items: center; justify-content: center;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 24rpx; font-weight: 450; color: var(--text-secondary, #64748b); padding: 0 8rpx;
}

.error-text { color: var(--danger, #e86868); font-size: 22rpx; padding: 8rpx 16rpx 20rpx 16rpx; display: block; }

.login-btn {
	margin-top: 40rpx; height: 96rpx; line-height: 96rpx;
	background: var(--brand, #6b9fff); border-radius: 9999px;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx; font-weight: 450; color: #ffffff;
	letter-spacing: -0.27rpx; border: none; box-shadow: none; transition: all 0.2s ease;
}

.login-btn:active { opacity: 0.85; transform: scale(0.97); }
.login-btn[disabled] { opacity: 0.5; }

.remember-row {
	display: flex; align-items: center; justify-content: flex-start;
	margin-top: -8rpx; margin-bottom: 8rpx;
}
.remember-label {
	display: flex; flex-direction: row; align-items: center;
	gap: 12rpx; padding: 8rpx 0;
}
.remember-checkbox {
	width: 32rpx; height: 32rpx; border-radius: 8rpx;
	border: 2rpx solid #cbd5e1; display: flex; align-items: center; justify-content: center;
	font-size: 20rpx; color: transparent; line-height: 1;
	background: #ffffff; transition: all 0.15s;
}
.remember-checkbox.checked {
	background: #17191c; border-color: #17191c; color: #ffffff;
}
.remember-text {
	font-size: 24rpx; font-weight: 430; color: #64748b;
}

.agree-row {
	display: flex; align-items: center; justify-content: center;
	margin-top: 12rpx; margin-bottom: -8rpx;
}
.agree-label {
	display: flex; flex-direction: row; align-items: center;
	gap: 10rpx;
}
.agree-checkbox {
	width: 30rpx; height: 30rpx; border-radius: 8rpx;
	border: 2rpx solid #cbd5e1; display: flex; align-items: center; justify-content: center;
	font-size: 18rpx; color: transparent; background: #ffffff; flex-shrink: 0;
}
.agree-checkbox.checked { background: #17191c; border-color: #17191c; color: #ffffff; }
.agree-text { font-size: 22rpx; color: #64748b; }
.agree-link { color: #0ea5e9; text-decoration: underline; }

.footer-actions { display: flex; justify-content: center; margin-top: 40rpx; }
.forgot-row { display: flex; justify-content: center; margin-top: 8rpx; }

.link-text {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 450; color: #17191c;
	letter-spacing: -0.25rpx; padding: 10rpx;
}

.link-text:active { opacity: 0.6; }

.footer { position: fixed; bottom: 40rpx; left: 0; right: 0; text-align: center; }
.copyright { font-size: 22rpx; color: var(--text-placeholder, #94a3b8); }
</style>
