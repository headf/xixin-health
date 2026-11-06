<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">消息</text>
			<view class="header-spacer"></view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="content" v-if="!loading">
			<view class="section-title">全部消息</view>

			<view
				class="msg-card"
				v-for="item in notifications"
				:key="item.id"
				:class="{ unread: !item.readFlag }"
				@tap="handleTap(item)"
			>
				<view class="msg-top">
					<view class="msg-left">
						<view class="msg-icon" :class="iconClass(item.notificationType)"></view>
						<view class="msg-info">
							<view class="msg-title-row">
								<text class="msg-title">{{ item.title }}</text>
								<text class="unread-dot" v-if="!item.readFlag">●</text>
							</view>
							<text class="msg-time">{{ formatTime(item.createdAt) }}</text>
						</view>
					</view>
				</view>
				<text class="msg-summary" v-if="item.content">{{ item.content }}</text>
			</view>

			<view class="empty-state" v-if="notifications.length === 0">
				<view class="empty-icon-box"></view>
				<text class="empty-text">暂无消息</text>
			</view>
		</view>
	</view>
</template>

<script>
import { notificationApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			notifications: [],
			userId: null
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadNotifications()
	},

	methods: {
		async loadNotifications() {
			this.loading = true
			this.userId = uni.getStorageSync('userId')
			try {
				const data = await notificationApi.getByUser(this.userId)
				this.notifications = data || []
			} catch (err) { console.error('加载消息失败', err)
			} finally { this.loading = false }
		},

		goBack() { uni.navigateBack() },

		handleTap(item) {
			if (!item.readFlag) {
				notificationApi.markRead(item.id).catch(() => {})
				item.readFlag = true
			}
			uni.setStorageSync('selectedNotification', item)
			uni.navigateTo({ url: '/pages/notification/detail' })
		},

		iconClass(type) {
			return 'icon-' + ({
				APPOINTMENT_REMINDER: 'cal',
				APPOINTMENT_CANCELLED: 'close',
				REFUND_NOTICE: 'money',
				SYSTEM: 'gear',
				REPORT_READY: 'doc'
			}[type] || 'chat')
		},

		formatTime(t) {
			if (!t) return ''
			return t.substring(0, 16).replace('T', ' ')
		}
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f7f7f8; display: flex; flex-direction: column; padding-top: calc(44px); }

.header { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 16rpx 32rpx; background: #ffffff; position: relative; }
.back-btn { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; letter-spacing: -0.25rpx; padding: 12rpx 8rpx; }
.back-btn:active { opacity: 0.6; }
.header-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 40rpx; font-weight: 400; color: #17191c; letter-spacing: -0.3rpx; line-height: 1.1; position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap; }
.header-spacer { width: 80rpx; }
.loading-state { flex: 1; display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 28rpx; color: #777b86; }
.content { flex: 1; padding: 24rpx 32rpx 40rpx; }
.section-title { font-size: 28rpx; font-weight: 500; color: #777b86; letter-spacing: 0.5rpx; margin: 8rpx 8rpx 24rpx; }

.msg-card { background: #ffffff; border-radius: 48rpx; padding: 28rpx 32rpx; margin-bottom: 16rpx; box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx; }
.msg-card:active { opacity: 0.85; }
.msg-top { display: flex; flex-direction: row; align-items: center; justify-content: space-between; margin-bottom: 8rpx; }
.msg-left { display: flex; flex-direction: row; align-items: center; flex: 1; gap: 20rpx; }
.msg-icon { width: 64rpx; height: 64rpx; background-size: contain; background-position: center; background-repeat: no-repeat; flex-shrink: 0; }
.icon-cal { background-image: url('../../static/notif_cal.png'); }
.icon-close { background-image: url('../../static/notif_close.png'); }
.icon-money { background-image: url('../../static/notif_money.png'); }
.icon-gear { background-image: url('../../static/notif_gear.png'); }
.icon-doc { background-image: url('../../static/notif_doc.png'); }
.icon-chat { background-image: url('../../static/notif_chat.png'); }
.msg-info { flex: 1; }
.msg-title-row { display: flex; flex-direction: row; align-items: center; gap: 8rpx; }
.msg-title { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 30rpx; font-weight: 500; color: #17191c; letter-spacing: -0.27rpx; }
.unread-dot { font-size: 20rpx; color: #ff3b30; }
.msg-time { font-size: 24rpx; color: #a3a6af; margin-top: 6rpx; display: block; }
.msg-summary { font-size: 24rpx; color: #a3a6af; line-height: 1.4; margin-top: 8rpx; padding-top: 10rpx; border-top: 1px solid #f7f7f8; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.unread .msg-title { font-weight: 500; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; }
.empty-icon-box { width: 80rpx; height: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #17191c; }
</style>
