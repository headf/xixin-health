<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">支付</text>
			<view class="header-spacer"></view>
		</view>

		<view class="content">
			<!-- 金额卡片 -->
			<view class="amount-card">
				<text class="amount-label">支付金额</text>
				<text class="amount-value">¥{{ formatPrice(amountCent) }}</text>
			</view>

			<!-- 支付方式 -->
			<view class="method-card">
				<text class="card-title">支付方式</text>
				<view class="method-item" @tap="selectedMethod = 'simulate'">
					<view class="method-left">
						<text class="method-icon"></text>
						<view class="method-info">
							<text class="method-name">模拟支付</text>
							<text class="method-desc">开发环境测试支付</text>
						</view>
					</view>
					<text class="check" :class="{ checked: selectedMethod === 'simulate' }">✓</text>
				</view>
			</view>

			<!-- 预约信息 -->
			<view class="info-card">
				<text class="card-title">预约信息</text>
				<view class="info-row">
					<text class="info-label">预约编号</text>
					<text class="info-value mono">{{ appointmentNo }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">套餐金额</text>
					<text class="info-value">¥{{ formatPrice(amountCent) }}</text>
				</view>
			</view>

			<text class="form-error" v-if="errorMsg">{{ errorMsg }}</text>
			<text class="success-text" v-if="successMsg">{{ successMsg }}</text>
		</view>

		<view class="bottom-bar">
			<button class="pay-btn" type="default" :loading="paying" :disabled="paying" @tap="handlePay">
				{{ paying ? '支付中...' : '确认支付 ¥' + formatPrice(amountCent) }}
			</button>
		</view>
	</view>
</template>

<script>
import { orderApi } from '../../common/api.js'

export default {
	data() {
		return {
			appointmentId: null,
			appointmentNo: '',
			amountCent: 0,
			selectedMethod: 'simulate',
			paying: false,
			errorMsg: '',
			successMsg: ''
		}
	},

	onLoad(options) {
		if (options.appointmentId) {
			this.appointmentId = Number(options.appointmentId)
			this.amountCent = Number(options.amountCent) || 29900
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		const data = uni.getStorageSync('paymentInfo')
		if (data) {
			this.appointmentId = data.appointmentId || this.appointmentId
			this.appointmentNo = data.appointmentNo || ''
			this.amountCent = data.amountCent || this.amountCent
		}
		uni.removeStorageSync('paymentInfo')
	},

	methods: {
		goBack() { uni.navigateBack() },

		formatPrice(cent) {
			if (cent == null) return '0.00'
			return (cent / 100).toFixed(2)
		},

		async handlePay() {
			this.errorMsg = ''
			this.successMsg = ''

			if (!this.appointmentId) {
				this.errorMsg = '预约信息缺失'
				return
			}

			this.paying = true
			try {
				// 1. 创建订单
				const order = await orderApi.create(this.appointmentId, this.amountCent)
				console.log('订单创建成功:', JSON.stringify(order))

				// 2. 模拟支付
				const paymentNo = 'PAY' + Date.now()
				await orderApi.simulatePayment(order.orderNo, paymentNo)

				this.successMsg = '支付成功！'
				uni.showToast({ title: '支付成功', icon: 'success', duration: 1500 })

				setTimeout(() => uni.navigateBack(), 1500)

			} catch (err) {
				console.error('支付失败:', JSON.stringify(err))
				this.errorMsg = err.message || '支付失败，请重试'
			} finally {
				this.paying = false
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

.amount-card {
	background: #17191c; border-radius: 48rpx; padding: 40rpx 36rpx;
	display: flex; flex-direction: column; align-items: center; margin-bottom: 24rpx;
}

.amount-label { font-size: 26rpx; color: rgba(255,255,255,0.65); margin-bottom: 12rpx; }

.amount-value {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 64rpx; font-weight: 500; color: #ffffff; letter-spacing: -1rpx;
}

.method-card, .info-card {
	background: #ffffff; border-radius: 48rpx; padding: 32rpx 32rpx;
	margin-bottom: 20rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

.card-title {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 500; color: #17191c;
	display: block; margin-bottom: 20rpx;
}

.method-item {
	display: flex; flex-direction: row; align-items: center;
	justify-content: space-between; padding: 20rpx 0;
	border-top: 1px solid #f7f7f8;
}

.method-item:first-child { border-top: none; }

.method-left { display: flex; flex-direction: row; align-items: center; gap: 20rpx; }
.method-icon { font-size: 36rpx; }
.method-name { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; display: block; }
.method-desc { font-size: 22rpx; color: #a3a6af; margin-top: 4rpx; }

.check {
	width: 40rpx; height: 40rpx; border-radius: 9999px;
	border: 2rpx solid #a3a6af; text-align: center; line-height: 40rpx;
	font-size: 24rpx; color: transparent;
}

.check.checked { background: #17191c; border-color: #17191c; color: #ffffff; }

.info-row {
	display: flex; flex-direction: row; align-items: center;
	justify-content: space-between; padding: 14rpx 0;
	border-bottom: 1px solid #f7f7f8;
}
.info-row:last-child { border-bottom: none; }

.info-label { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 430; color: #777b86; }
.info-value { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; text-align: right; }
.mono { font-size: 24rpx; font-family: monospace; letter-spacing: 0.2rpx; }

.form-error { font-size: 24rpx; color: #5d2a1a; display: block; text-align: center; margin-top: 16rpx; }
.success-text { font-size: 28rpx; color: #17191c; display: block; text-align: center; margin-top: 16rpx; font-weight: 500; }

.bottom-bar {
	position: fixed; bottom: 0; left: 0; right: 0;
	padding: 24rpx 32rpx 40rpx;
	background: linear-gradient(to top, #ffffff 60%, rgba(255,255,255,0));
	pointer-events: none;
}

.bottom-bar .pay-btn { pointer-events: auto; }

.pay-btn {
	height: 96rpx; line-height: 96rpx; background: #17191c; border-radius: 9999px;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx; font-weight: 450; color: #ffffff;
	letter-spacing: -0.27rpx; border: none; box-shadow: none; margin: 0;
}

.pay-btn:active { opacity: 0.85; transform: scale(0.97); }
.pay-btn[disabled] { opacity: 0.5; }
</style>
