<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">订单管理</text>
			<view class="header-spacer"></view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="content" v-if="!loading">
			<view class="section-title">全部订单</view>

			<view class="order-card" v-for="item in orders" :key="item.id" @tap="showDetail(item)">
				<view class="card-top">
					<text class="order-no">{{ item.orderNo }}</text>
					<text class="status-tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</text>
				</view>

				<view class="card-body">
					<view class="info-line">
						<text class="info-label">订单金额</text>
						<text class="info-value price">¥{{ formatPrice(item.amountCent) }}</text>
					</view>
					<view class="info-line">
						<text class="info-label">预约编号</text>
						<text class="info-value mono">{{ item.appointmentId }}</text>
					</view>
				</view>

				<view class="card-footer">
					<text class="created-at">创建于 {{ formatTime(item.createdAt) }}</text>
				</view>
			</view>

			<view class="empty-state" v-if="orders.length === 0">
				<text class="empty-icon"></text>
				<text class="empty-text">暂无订单记录</text>
			</view>
		</view>
	</view>
</template>

<script>
import { orderApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			orders: [],
			userId: null
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadOrders()
	},

	methods: {
		async loadOrders() {
			this.loading = true
			this.userId = uni.getStorageSync('userId')
			try {
				const data = await orderApi.getByUser(this.userId)
				this.orders = data || []
			} catch (err) {
				console.error('加载订单失败', err)
			} finally {
				this.loading = false
			}
		},

		goBack() { uni.navigateBack() },

			showDetail(item) {
				uni.setStorageSync("selectedOrder", item)
				uni.navigateTo({ url: "/pages/order/detail" })
			},

		formatPrice(cent) {
			if (cent == null) return '0.00'
			return (cent / 100).toFixed(2)
		},

		statusLabel(s) {
			const map = {
				PENDING_PAYMENT: '待支付',
				PAID: '已支付',
				REFUND_REQUESTED: '退款中',
				REFUNDED: '已退款',
				CLOSED: '已关闭'
			}
			return map[s] || s
		},

		statusClass(s) {
			return ({
				PENDING_PAYMENT: 'status-pending',
				PAID: 'status-paid',
				REFUND_REQUESTED: 'status-warn',
				REFUNDED: 'status-gray',
				CLOSED: 'status-gray'
			})[s] || ''
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

.content { flex: 1; padding: 24rpx 32rpx 40rpx; }
.section-title { font-size: 28rpx; font-weight: 500; color: #777b86; letter-spacing: 0.5rpx; margin: 8rpx 8rpx 24rpx; }

.order-card {
	background: #ffffff; border-radius: 48rpx; padding: 28rpx 32rpx;
	margin-bottom: 20rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

.card-top { display: flex; flex-direction: row; align-items: center; justify-content: space-between; margin-bottom: 20rpx; }

.order-no {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 24rpx; font-weight: 430; color: #a3a6af; letter-spacing: 0.2rpx;
}

.status-tag { font-size: 22rpx; font-weight: 450; padding: 6rpx 20rpx; border-radius: 9999px; line-height: 1.4; }
.status-pending { color: #5d2a1a; background: #fbe1d1; }
.status-paid { color: #17191c; background: #d3e3fc; }
.status-warn { color: #5d2a1a; background: #fbe1d1; }
.status-gray { color: #a3a6af; background: #f7f7f8; }

.card-body { border-top: 1px solid #f7f7f8; padding-top: 16rpx; }
.info-line { display: flex; flex-direction: row; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.info-line:last-child { margin-bottom: 0; }
.info-label { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 430; color: #777b86; }
.info-value { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; }
.info-value.price { font-size: 32rpx; font-weight: 480; }
.mono { font-size: 24rpx; font-family: monospace; }

.card-footer { margin-top: 16rpx; padding-top: 12rpx; border-top: 1px solid #f7f7f8; }
.created-at { font-size: 22rpx; color: #a3a6af; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; }
.empty-icon { font-size: 64rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #17191c; margin-bottom: 8rpx; }
</style>
