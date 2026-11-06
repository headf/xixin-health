<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">消息详情</text>
			<view class="header-spacer"></view>
		</view>

		<view class="content" v-if="notification">
			<!-- 消息图标 + 标题 -->
			<view class="msg-hero">
				<view class="msg-icon" :class="iconClass(notification.notificationType)"></view>
				<text class="msg-title">{{ notification.title }}</text>
				<text class="msg-time">{{ formatTime(notification.createdAt) }}</text>
			</view>

			<!-- 消息内容 -->
			<view class="msg-body">
				<text class="msg-content">{{ notification.content }}</text>
			</view>

			<!-- 底部信息 -->
			<view class="msg-footer">
				<text class="footer-label">消息类型</text>
				<text class="footer-value">{{ typeLabel(notification.notificationType) }}</text>
			</view>
		</view>

		<view class="loading-state" v-if="!notification">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { notificationApi } from '../../common/api.js'

export default {
	data() {
		return {
			notification: null
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadDetail()
	},
	methods: {
		loadDetail() {
			const data = uni.getStorageSync('selectedNotification')
			if (data) {
				this.notification = data
				// 标记已读
				if (!data.readFlag) {
					notificationApi.markRead(data.id).catch(() => {})
				}
			}
		},
		goBack() { uni.navigateBack() },
		iconClass(type) {
			return 'icon-' + ({
				APPOINTMENT_REMINDER: 'cal',
				APPOINTMENT_CANCELLED: 'close',
				REFUND_NOTICE: 'money',
				SYSTEM: 'gear',
				REPORT_READY: 'doc'
			}[type] || 'chat')
		},
		typeLabel(type) {
			return ({
				APPOINTMENT_REMINDER: '预约提醒',
				APPOINTMENT_CANCELLED: '取消通知',
				REFUND_NOTICE: '退款通知',
				SYSTEM: '系统消息',
				REPORT_READY: '报告通知'
			}[type] || '其他')
		},
		formatTime(t) {
			if (!t) return ''
			return t.substring(0, 16).replace('T', ' ')
		}
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f7f7f8; display: flex; flex-direction: column; padding-top: calc(var(--status-bar-height, 44px)); }

.header { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 16rpx 32rpx; background: #ffffff; position: relative; }
.back-btn { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; letter-spacing: -0.25rpx; padding: 12rpx 8rpx; }
.back-btn:active { opacity: 0.6; }
.header-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 40rpx; font-weight: 400; color: #17191c; letter-spacing: -0.3rpx; line-height: 1.1; position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap; }
.header-spacer { width: 80rpx; }
.loading-state { flex: 1; display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 28rpx; color: #777b86; }

.content { flex: 1; padding: 32rpx 32rpx 40rpx; }

.msg-hero { display: flex; flex-direction: column; align-items: center; padding: 40rpx 0 32rpx; }
.msg-icon { width: 80rpx; height: 80rpx; background-size: contain; background-position: center; background-repeat: no-repeat; margin-bottom: 20rpx; }
.icon-cal { background-image: url('../../static/notif_cal.png'); }
.icon-close { background-image: url('../../static/notif_close.png'); }
.icon-money { background-image: url('../../static/notif_money.png'); }
.icon-gear { background-image: url('../../static/notif_gear.png'); }
.icon-doc { background-image: url('../../static/notif_doc.png'); }
.icon-chat { background-image: url('../../static/notif_chat.png'); }
.msg-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 40rpx; font-weight: 400; color: #17191c; text-align: center; margin-bottom: 8rpx; }
.msg-time { font-size: 24rpx; color: #a3a6af; }

.msg-body { background: #ffffff; border-radius: 48rpx; padding: 36rpx 32rpx; margin-bottom: 20rpx; box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx; }
.msg-content { font-size: 30rpx; color: #17191c; line-height: 1.7; display: block; white-space: pre-wrap; word-break: break-word; }

.msg-footer { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 20rpx 8rpx; }
.footer-label { font-size: 24rpx; color: #a3a6af; }
.footer-value { font-size: 24rpx; color: #777b86; }
</style>
