<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">联系客服</text>
			<view class="header-spacer"></view>
		</view>

		<view class="content">
			<!-- 联系方式 -->
			<view class="card">
				<text class="card-title">客服联系方式</text>
				<view class="contact-list">
					<view class="contact-item" v-for="item in contactMethods" :key="item.label">
						<text class="contact-label">{{ item.label }}</text>
						<text class="contact-value">{{ item.value }}</text>
					</view>
				</view>
			</view>

			<!-- 在线留言 -->
			<view class="card">
				<text class="card-title">在线留言</text>
				<view class="form-group">
					<text class="form-label">您的称呼</text>
					<input class="form-input" v-model="form.name" placeholder="请输入姓名" />
				</view>
				<view class="form-group">
					<text class="form-label">联系方式</text>
					<input class="form-input" v-model="form.phone" placeholder="手机号" type="number" />
				</view>
				<view class="form-group">
					<text class="form-label">留言内容</text>
					<textarea class="form-textarea" v-model="form.content" placeholder="请描述您的问题或建议..." />
				</view>
				<button class="submit-btn" type="default" :loading="submitting" :disabled="submitting || !form.name || !form.phone || !form.content" @tap="handleSubmit">
					{{ submitting ? '提交中...' : '提交留言' }}
				</button>
				<view class="success-msg" v-if="submitted">
					<text class="success-text">留言已提交，客服回复将通过消息通知发送给您！</text>
				</view>
			</view>

			<!-- 服务时间 -->
			<view class="card">
				<text class="card-title">服务时间</text>
				<view class="info-row">
					<text class="info-label">工作日</text>
					<text class="info-value">周一至周五 8:00 - 18:00</text>
				</view>
				<view class="info-row">
					<text class="info-label">周末</text>
					<text class="info-value">周六 8:00 - 12:00</text>
				</view>
				<view class="info-row">
					<text class="info-label">节假日</text>
					<text class="info-value">另行通知</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { contentApi } from '../../common/api.js'

export default {
	data() {
		return {
			submitting: false,
			submitted: false,
			form: { name: '', phone: '', content: '' },
			contactMethods: [
				{ label: '客服热线', value: '加载中...' },
				{ label: '电子邮件', value: '加载中...' },
				{ label: '办公地址', value: '加载中...' },
			]
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadContactInfo()
	},

	methods: {
		async loadContactInfo() {
			try {
				const keys = ['contact_phone', 'contact_email', 'contact_address'].join(',')
				const data = await contentApi.batch(keys)
				if (data) {
					this.contactMethods = [
						{ label: '客服热线', value: data.contact_phone || '400-100-0001' },
						{ label: '电子邮件', value: data.contact_email || 'service@xixin-health.com' },
						{ label: '办公地址', value: data.contact_address || '北京市朝阳区建国路88号' },
					]
				}
			} catch (e) {
				this.contactMethods = [
					{ label: '客服热线', value: '400-100-0001' },
					{ label: '电子邮件', value: 'service@xixin-health.com' },
					{ label: '办公地址', value: '北京市朝阳区建国路88号' },
				]
			}
		},

		async handleSubmit() {
			if (!this.form.name || !this.form.phone || !this.form.content) {
				uni.showToast({ title: '请填写完整信息', icon: 'none' })
				return
			}
			this.submitting = true
			try {
				const userId = uni.getStorageSync('userId') || ''
				await contentApi.submitContact({
					name: this.form.name,
					phone: this.form.phone,
					content: this.form.content,
					userId: userId
				})
				this.submitted = true
				this.form = { name: '', phone: '', content: '' }
				uni.showToast({ title: '留言已提交', icon: 'success' })
			} catch (e) {
				uni.showToast({ title: e.message || '提交失败', icon: 'none' })
			} finally {
				this.submitting = false
			}
		},

		goBack() { uni.navigateBack() }
	}
}
</script>

<style>
.page {
	min-height: 100vh;
	background: #f5f5f6;
	display: flex;
	flex-direction: column;
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
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	padding: 12rpx 8rpx;
}
.back-btn:active { opacity: 0.6; }

.header-title {
	font-size: 40rpx;
	font-weight: 400;
	color: #17191c;
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
	white-space: nowrap;
}

.header-spacer { width: 80rpx; }

.content {
	flex: 1;
	padding: 24rpx 24rpx 140rpx;
}

.card {
	background: #ffffff;
	border-radius: 32rpx;
	padding: 32rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 1rpx 3rpx rgba(0,0,0,0.04);
}

.card-title {
	font-size: 28rpx;
	font-weight: 500;
	color: #17191c;
	display: block;
	margin-bottom: 20rpx;
}

.contact-list {
	display: flex;
	flex-direction: column;
	gap: 0;
}

.contact-item {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 16rpx 0;
	border-bottom: 1px solid #f7f7f8;
}
.contact-item:last-child { border-bottom: none; }

.contact-label {
	font-size: 26rpx;
	font-weight: 450;
	color: #777b86;
	width: 160rpx;
	flex-shrink: 0;
}

.contact-value {
	font-size: 26rpx;
	color: #17191c;
	flex: 1;
}

.form-group {
	margin-bottom: 24rpx;
}

.form-label {
	font-size: 26rpx;
	font-weight: 450;
	color: #17191c;
	display: block;
	margin-bottom: 10rpx;
}

.form-input {
	width: 100%;
	height: 80rpx;
	background: #f7f7f8;
	border-radius: 20rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
	color: #17191c;
	box-sizing: border-box;
}

.form-textarea {
	width: 100%;
	height: 180rpx;
	background: #f7f7f8;
	border-radius: 20rpx;
	padding: 20rpx 24rpx;
	font-size: 28rpx;
	color: #17191c;
	box-sizing: border-box;
}

.submit-btn {
	height: 88rpx;
	line-height: 88rpx;
	background: #17191c;
	border-radius: 9999px;
	font-size: 28rpx;
	font-weight: 450;
	color: #ffffff;
	border: none;
	margin: 0;
	width: 100%;
}

.submit-btn:active { opacity: 0.85; }
.submit-btn[disabled] { opacity: 0.5; }

.success-msg {
	margin-top: 20rpx;
	padding: 24rpx;
	background: #ecfdf5;
	border-radius: 20rpx;
}

.success-text {
	font-size: 24rpx;
	color: #059669;
	line-height: 1.6;
}

.info-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 0;
	border-bottom: 1px solid #f7f7f8;
}
.info-row:last-child { border-bottom: none; }

.info-label {
	font-size: 26rpx;
	font-weight: 430;
	color: #777b86;
}

.info-value {
	font-size: 26rpx;
	font-weight: 450;
	color: #17191c;
}
</style>
