<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">{{ isEdit ? '编辑档案' : '添加档案' }}</text>
			<view class="header-spacer"></view>
		</view>

		<view class="content">
			<view class="form-card">
				<!-- 姓名 -->
				<view class="form-group">
					<text class="form-label">姓名 <text class="required">*</text></text>
					<input class="input-field" v-model="form.name" placeholder="请输入姓名" maxlength="64" />
				</view>
				<text class="error-text" v-if="errors.name">{{ errors.name }}</text>

				<!-- 档案类型 -->
				<view class="form-group">
					<text class="form-label">档案类型 <text class="required">*</text></text>
					<view class="type-group">
						<text class="type-option" :class="{ active: form.profileType === 'SELF' }" @tap="form.profileType = 'SELF'">本人</text>
						<text class="type-option" :class="{ active: form.profileType === 'FAMILY' }" @tap="form.profileType = 'FAMILY'">家人</text>
					</view>
				</view>

				<!-- 身份证号 -->
				<view class="form-group">
					<text class="form-label">身份证号 <text class="required">*</text></text>
					<input class="input-field" v-model="form.idCardNo" placeholder="请输入18位身份证号" maxlength="18" />
				</view>
				<text class="error-text" v-if="errors.idCardNo">{{ errors.idCardNo }}</text>

				<!-- 手机号 -->
				<view class="form-group">
					<text class="form-label">手机号 <text class="required">*</text></text>
					<input class="input-field" v-model="form.phone" placeholder="请输入11位手机号" maxlength="11" type="number" />
				</view>
				<text class="error-text" v-if="errors.phone">{{ errors.phone }}</text>

				<!-- 过敏史 -->
				<view class="form-group">
					<text class="form-label">过敏史</text>
					<input class="input-field" v-model="form.allergyHistory" placeholder="如：青霉素过敏" />
				</view>

				<!-- 既往病史 -->
				<view class="form-group">
					<text class="form-label">既往病史</text>
					<input class="input-field" v-model="form.medicalHistory" placeholder="如：高血压、糖尿病" />
				</view>

				<!-- 紧急联系人 -->
				<view class="form-group">
					<text class="form-label">紧急联系人 <text class="required">*</text></text>
					<input class="input-field" v-model="form.emergencyContactName" placeholder="请输入联系人姓名" maxlength="32" />
				</view>
				<text class="error-text" v-if="errors.emergencyContactName">{{ errors.emergencyContactName }}</text>

				<!-- 紧急联系人电话 -->
				<view class="form-group">
					<text class="form-label">紧急联系人电话 <text class="required">*</text></text>
					<input class="input-field" v-model="form.emergencyContactPhone" placeholder="请输入11位手机号" maxlength="11" type="number" />
				</view>
				<text class="error-text" v-if="errors.emergencyContactPhone">{{ errors.emergencyContactPhone }}</text>
			</view>

			<text class="form-error" v-if="errorMsg">{{ errorMsg }}</text>
		</view>

		<view class="bottom-bar">
			<button class="save-btn" type="default" :loading="submitting" :disabled="submitting" @tap="handleSave">
				{{ submitting ? '保存中...' : '保存档案' }}
			</button>
		</view>
	</view>
</template>

<script>
import { profileApi } from '../../common/api.js'

export default {
	data() {
		return {
			isEdit: false,
			submitting: false,
			userId: null,
			form: {
				name: '',
				profileType: 'FAMILY',
				idCardNo: '',
				phone: '',
				allergyHistory: '',
				medicalHistory: '',
				emergencyContactName: '',
				emergencyContactPhone: ''
			},
			errors: {},
			errorMsg: ''
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.userId = uni.getStorageSync('userId')
		// 每次进入先重置表单
		this.resetForm()

		// 从缓存读编辑数据
		const edit = uni.getStorageSync('editProfile')
		if (edit) {
			this.isEdit = true
			// 优先从API加载完整数据（确保获取到所有字段）
			if (edit.id) {
				this.loadEditProfile(edit)
			} else {
				this.fillForm(edit)
			}
		}
	},

	methods: {
		goBack() { uni.navigateBack() },

		resetForm() {
			this.form = {
				name: '',
				profileType: 'FAMILY',
				idCardNo: '',
				phone: '',
				allergyHistory: '',
				medicalHistory: '',
				emergencyContactName: '',
				emergencyContactPhone: ''
			}
			this.isEdit = false
			this.errors = {}
			this.errorMsg = ''
		},

		fillForm(data) {
			this.form.name = data.name || ''
			this.form.profileType = data.profileType || 'SELF'
			this.form.idCardNo = data.idCardNo || data.idCard || ''
			this.form.phone = data.phone || ''
			this.form.allergyHistory = data.allergyHistory || ''
			this.form.medicalHistory = data.medicalHistory || ''
			this.form.emergencyContactName = data.emergencyContactName || ''
			this.form.emergencyContactPhone = data.emergencyContactPhone || ''
		},

		async loadEditProfile(fallback) {
			try {
				const detail = await profileApi.getProfileDetail(fallback.id)
				this.fillForm(detail)
			} catch (e) {
				console.error('加载编辑档案数据失败，使用缓存', e)
				this.fillForm(fallback)
			}
		},

		async handleSave() {
			this.errorMsg = ''
			this.errors = {}

			// 姓名校验
			if (!this.form.name || this.form.name.trim() === '') {
				this.errors.name = '请输入姓名'
				return
			}

			// 身份证号校验
			const idCard = this.form.idCardNo ? this.form.idCardNo.trim() : ''
			if (!idCard) {
				this.errors.idCardNo = '请输入身份证号'
				return
			}
			if (!/^\d{17}[\dXx]$/.test(idCard) && !/^\d{15}$/.test(idCard)) {
				this.errors.idCardNo = '身份证号格式不正确'
				return
			}

			// 手机号校验
			const phone = this.form.phone ? this.form.phone.trim() : ''
			if (!phone) {
				this.errors.phone = '请输入手机号'
				return
			}
			if (!/^1\d{10}$/.test(phone)) {
				this.errors.phone = '手机号格式不正确'
				return
			}

			// 紧急联系人校验
			if (!this.form.emergencyContactName || this.form.emergencyContactName.trim() === '') {
				this.errors.emergencyContactName = '请输入紧急联系人'
				return
			}

			// 紧急联系人电话校验
			const ePhone = this.form.emergencyContactPhone ? this.form.emergencyContactPhone.trim() : ''
			if (!ePhone) {
				this.errors.emergencyContactPhone = '请输入紧急联系人电话'
				return
			}
			if (!/^1\d{10}$/.test(ePhone)) {
				this.errors.emergencyContactPhone = '手机号格式不正确'
				return
			}

			this.submitting = true
			try {
				const result = await profileApi.saveProfile(this.userId, {
					name: this.form.name.trim(),
					profileType: this.form.profileType,
					idCardNo: idCard,
					phone: phone,
					allergyHistory: this.form.allergyHistory || undefined,
					medicalHistory: this.form.medicalHistory || undefined,
					emergencyContactName: this.form.emergencyContactName.trim(),
					emergencyContactPhone: ePhone
				})

				// 保存成功后把完整数据存入 selectedProfile
				if (result && result.id) {
					uni.setStorageSync('selectedProfile', result)
				}

				uni.showToast({ title: '保存成功', icon: 'success', duration: 1500 })
				uni.removeStorageSync('editProfile')
				// 用URL传ID直接跳转到详情页查看完整数据
				setTimeout(() => {
					uni.redirectTo({ url: '/pages/profile/detail?id=' + (result ? result.id : '') })
				}, 1500)

			} catch (err) {
				this.errorMsg = err.message || '保存失败，请重试'
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

.content { flex: 1; padding: 24rpx 32rpx 160rpx; }

.form-card {
	background: #ffffff; border-radius: 48rpx; padding: 36rpx 32rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

.form-group { margin-bottom: 28rpx; }
.form-group:last-child { margin-bottom: 0; }

.form-label {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 26rpx; font-weight: 500; color: #17191c;
	display: block; margin-bottom: 12rpx;
}

.required { color: #e53e3e; }

.input-field {
	width: 100%; height: 88rpx; background: #f7f7f8; border-radius: 24rpx;
	padding: 0 24rpx; font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 450; color: #17191c;
	border: 2rpx solid transparent; box-sizing: border-box;
}

.input-field::placeholder { color: #a3a6af; font-size: 26rpx; }

.error-text { font-size: 22rpx; color: #e53e3e; display: block; margin: -16rpx 0 16rpx 8rpx; }
.form-error { font-size: 24rpx; color: #e53e3e; display: block; margin-top: 20rpx; padding: 0 8rpx; }

/* 档案类型选择 */
.type-group { display: flex; flex-direction: row; gap: 16rpx; }

.type-option {
	flex: 1; height: 88rpx; line-height: 88rpx; text-align: center;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 430; color: #777b86;
	background: #f7f7f8; border-radius: 24rpx; border: 2rpx solid transparent;
}

.type-option.active {
	color: #17191c; background: #ffffff; border-color: #17191c; font-weight: 450;
}

.bottom-bar {
	position: fixed; bottom: 0; left: 0; right: 0;
	padding: 16rpx 32rpx 36rpx;
	background: linear-gradient(to top, #ffffff 60%, rgba(255,255,255,0));
	pointer-events: none;
	display: flex;
	justify-content: center;
}

.bottom-bar .save-btn { pointer-events: auto; }

.save-btn {
	height: 80rpx; line-height: 80rpx; padding: 0 60rpx;
	background: linear-gradient(135deg, #00B894, #00A080) !important;
	border-radius: 40rpx;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 600; color: #ffffff !important;
	letter-spacing: 1rpx; border: none !important;
	box-shadow: 0 4rpx 16rpx rgba(0,184,148,0.3);
	margin: 0;
	width: auto !important;
	display: inline-flex !important;
	align-items: center;
	justify-content: center;
}
button.save-btn::after { border: none !important; }

.save-btn:active { opacity: 0.9; transform: scale(0.97); }
.save-btn[disabled] { opacity: 0.5; }
</style>
