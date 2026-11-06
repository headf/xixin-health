<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">订单详情</text>
			<view class="header-spacer"></view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="content" v-if="!loading && order">
			<!-- 金额状态卡片 -->
			<view class="amount-card" :class="amountBgClass">
				<text class="amount-label">订单金额</text>
				<text class="amount-value">¥{{ formatPrice(order.amountCent) }}</text>
				<text class="status-badge" :class="statusBgClass">{{ statusLabel(order.status) }}</text>
			</view>

			<!-- 订单信息 -->
			<view class="info-card">
				<text class="card-title">订单信息</text>
				<view class="info-row">
					<text class="info-label">订单编号</text>
					<text class="info-value mono">{{ order.orderNo }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">订单状态</text>
					<text class="info-value">{{ statusLabel(order.status) }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">创建时间</text>
					<text class="info-value">{{ formatTime(order.createdAt) }}</text>
				</view>
			</view>

			<!-- 关联预约信息 -->
			<view class="info-card" v-if="appointmentNo">
				<text class="card-title">关联预约</text>
				<view class="info-row">
					<text class="info-label">预约编号</text>
					<text class="info-value mono">{{ appointmentNo }}</text>
				</view>
				<view class="info-row" v-if="appointmentDate">
					<text class="info-label">体检日期</text>
					<text class="info-value">{{ appointmentDate }}</text>
				</view>
			</view>

			<!-- 待支付时可去支付 -->
			<view class="notice" v-if="order.status === 'PENDING_PAYMENT'">
				<text class="notice-text">该订单尚未支付，请尽快完成支付</text>
			</view>
		</view>

		<view class="bottom-bar" v-if="order && order.status === 'PENDING_PAYMENT'">
			<button class="pay-btn" type="default" @tap="goPay">去支付</button>
		</view>
	</view>
</template>

<script>
import { orderApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			order: null,
			appointmentNo: '',
			appointmentDate: ''
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadDetail()
	},

	computed: {
		amountBgClass() {
			return this.order?.status === 'PAID' ? 'bg-cool' : this.order?.status === 'PENDING_PAYMENT' ? 'bg-warm' : 'bg-gray'
		},
		statusBgClass() {
			return this.order?.status === 'PAID' ? 'tag-paid' : this.order?.status === 'PENDING_PAYMENT' ? 'tag-pending' : 'tag-gray'
		}
	},

	methods: {
		loadDetail() {
			const data = uni.getStorageSync('selectedOrder')
			if (data) {
				this.order = data
				this.appointmentNo = data.appointmentNo || ''
				this.appointmentDate = data.appointmentDate || ''
			}
			this.loading = false
		},

		goBack() { uni.navigateBack() },

		goPay() {
			uni.setStorageSync('paymentInfo', {
				appointmentId: this.order.appointmentId,
				appointmentNo: this.appointmentNo,
				amountCent: this.order.amountCent
			})
			uni.navigateTo({ url: '/pages/order/payment' })
		},

		formatPrice(cent) {
			if (cent == null) return '0.00'
			return (cent / 100).toFixed(2)
		},

		statusLabel(s) {
			const map = {
				PENDING_PAYMENT: '待支付', PAID: '已支付',
				REFUND_REQUESTED: '退款中', REFUNDED: '已退款', CLOSED: '已关闭'
			}
			return map[s] || s
		},

		formatTime(t) {
			if (!t) return ''
			return t.substring(0, 16).replace('T', ' ')
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

.content { flex: 1; padding: 24rpx 32rpx 160rpx; }

.amount-card {
	border-radius: 48rpx; padding: 40rpx 36rpx;
	display: flex; flex-direction: column; align-items: center; margin-bottom: 24rpx;
}
.bg-warm { background: #fbe1d1; }
.bg-cool { background: #d3e3fc; }
.bg-gray { background: #f7f7f8; }

.amount-label { font-size: 26rpx; color: #777b86; margin-bottom: 12rpx; }
.amount-value {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 64rpx; font-weight: 500; color: #17191c; letter-spacing: -1rpx; margin-bottom: 16rpx;
}

.status-badge {
	font-size: 24rpx; font-weight: 450; padding: 8rpx 28rpx; border-radius: 9999px;
}
.tag-pending { color: #5d2a1a; background: rgba(255,255,255,0.7); }
.tag-paid { color: #17191c; background: rgba(255,255,255,0.7); }
.tag-gray { color: #777b86; background: rgba(255,255,255,0.7); }

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
.info-value { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; text-align: right; }
.mono { font-size: 24rpx; font-family: monospace; letter-spacing: 0.2rpx; }

.notice { margin-top: 8rpx; padding: 0 8rpx; }
.notice-text { font-size: 24rpx; color: #a3a6af; line-height: 1.5; }

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
</style>
