<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">预约详情</text>
			<view class="header-spacer"></view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="content" v-if="!loading && appt">
			<!-- 状态卡片 -->
			<view class="status-card" :class="statusBgClass">
				<view class="status-icon" :class="statusIconClass"></view>
				<text class="status-text">{{ statusLabel(appt.status) }}</text>
				<text class="status-desc">{{ statusDesc(appt.status) }}</text>
			</view>

			<!-- 预约编号 -->
			<view class="info-card">
				<view class="info-row">
					<text class="info-label">预约编号</text>
					<text class="info-value mono">{{ appt.appointmentNo }}</text>
				</view>
			</view>

			<!-- 详情卡片 -->
			<view class="info-card">
				<text class="card-title">预约信息</text>
				<view class="info-row">
					<view class="info-label-row"><view class="row-icon icon-cal"></view><text class="info-label">体检日期</text></view>
					<text class="info-value">{{ appt.appointmentDate }}</text>
				</view>
				<view class="info-row">
					<view class="info-label-row"><view class="row-icon icon-clock"></view><text class="info-label">体检时段</text></view>
					<text class="info-value">{{ appt.timeSlot }}</text>
				</view>
				<view class="info-row">
					<view class="info-label-row"><view class="row-icon icon-hosp"></view><text class="info-label">体检分院</text></view>
					<text class="info-value">{{ branchName }}</text>
				</view>
				<view class="info-row">
					<view class="info-label-row"><view class="row-icon icon-list"></view><text class="info-label">体检套餐</text></view>
					<text class="info-value">{{ packageName }}</text>
				</view>
				<view class="info-row">
					<view class="info-label-row"><view class="row-icon icon-person"></view><text class="info-label">体检人</text></view>
					<text class="info-value">{{ profileName || '未知' }}</text>
				</view>
				<view class="info-row">
					<view class="info-label-row"><view class="row-icon icon-time"></view><text class="info-label">创建时间</text></view>
					<text class="info-value">{{ formatTime(appt.createdAt) }}</text>
				</view>
			</view>

			<!-- 操作提示 -->
			<view class="notice" v-if="appt.status === 'CREATED'">
				<text class="notice-text">请及时完成支付，逾期预约将自动取消</text>
			</view>
			<view class="notice" v-if="appt.status === 'PAID'">
				<text class="notice-text">请在预约当日按时到达体检分院</text>
			</view>
		</view>

		<view class="bottom-bar" v-if="appt && (appt.status === 'CREATED' || appt.status === 'PAID')">
			<button class="cancel-text-btn" type="default" @tap="handleCancel">取消预约</button>
			<button class="guide-text-btn" type="default" @tap="goGuide">导诊路线</button>
			<button class="prep-text-btn" type="default" @tap="goPreparation">检前准备</button>
			<button class="questionnaire-btn" type="default" @tap="goQuestionnaire" v-if="appt.status === 'PAID'">检前问卷</button>
			<button class="pay-btn" type="default" @tap="goPay">{{ appt.status === 'CREATED' ? '去支付' : '已完成支付' }}</button>
		</view>
	</view>
</template>

<script>
import { appointmentApi, packageApi, profileApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			appt: null,
			branchName: '',
			packageName: '',
			profileName: ''
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadDetail()
	},

	computed: {
		statusBgClass() {
			const map = {
				CREATED: 'bg-warm', PAID: 'bg-cool', CHECKED_IN: 'bg-cool',
				CANCELLED: 'bg-gray', COMPLETED: 'bg-gray'
			}
			return map[this.appt?.status] || ''
		},
		statusIconClass() {
			const map = {
				CREATED: 'si-clock', PAID: 'si-check', CHECKED_IN: 'si-list',
				CANCELLED: 'si-close', COMPLETED: 'si-done'
			}
			return map[this.appt?.status] || ''
		}
	},

	methods: {
		async loadDetail() {
			this.appt = uni.getStorageSync('selectedAppointment')
			if (!this.appt) { this.loading = false; return }

			const userId = uni.getStorageSync('userId')
			try {
				const [pkgs, brs, profiles] = await Promise.all([
					packageApi.getPackages(), packageApi.getBranches(),
					userId ? profileApi.getProfiles(userId) : Promise.resolve([])
				])
				const p = (pkgs || []).find(x => x.id === this.appt.packageId)
				this.packageName = p ? p.packageName : '未知套餐'
				this.packagePrice = p ? p.priceCent : 0
				const b = (brs || []).find(x => x.id === this.appt.branchId)
				this.branchName = b ? b.branchName : '未知分院'
				const pr = (profiles || []).find(x => x.id === this.appt.profileId)
				this.profileName = pr ? pr.name : '未知'
			} catch (e) { console.error('加载关联数据失败', e)
			} finally { this.loading = false }
		},

		goBack() { uni.navigateBack() },

			handleCancel() {
				uni.showModal({
					title: '取消预约',
					content: '确定要取消此预约吗？',
					success: async (res) => {
						if (res.confirm) {
							try {
								uni.showLoading({ title: '取消中...' })
								await appointmentApi.cancel(this.appt.id)
								uni.hideLoading()
								uni.showToast({ title: '已取消', icon: 'success' })
								setTimeout(() => uni.navigateBack(), 1000)
							} catch (err) {
								uni.hideLoading()
								let msg = '取消失败'
								if (err && err.message) msg = err.message
								uni.showToast({ title: msg, icon: 'error' })
							}
						}
					}
				})
			},

		goGuide() { uni.navigateTo({ url: "/pages/guide/index" }) },
		goPreparation() { uni.navigateTo({ url: "/pages/preparation/index" }) },
		goPay() {
			const paymentInfo = {
				appointmentId: this.appt.id, appointmentNo: this.appt.appointmentNo,
				amountCent: this.packagePrice || 29900
			}
			uni.setStorageSync("paymentInfo", paymentInfo)
			uni.navigateTo({ url: "/pages/order/payment" })
		},

		goQuestionnaire() {
			uni.navigateTo({ url: "/pages/questionnaire/index" })
		},

		statusLabel(s) {
			return ({ CREATED: '待支付', PAID: '已支付', CHECKED_IN: '已签到', CANCELLED: '已取消', COMPLETED: '已完成' })[s] || s
		},
		statusDesc(s) {
			return ({
				CREATED: '请尽快完成支付以确认预约',
				PAID: '请在预约日携带证件到院体检',
				CHECKED_IN: '已到达体检分院，开始体检',
				CANCELLED: '该预约已取消',
				COMPLETED: '体检已完成，可查看报告'
			})[s] || ''
		},
		formatTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' }
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

.status-card { border-radius: 48rpx; padding: 40rpx 36rpx; display: flex; flex-direction: column; align-items: center; margin-bottom: 24rpx; }
.bg-warm { background: #fbe1d1; }
.bg-cool { background: #d3e3fc; }
.bg-gray { background: #f7f7f8; }
.status-icon { width: 64rpx; height: 64rpx; background-size: contain; background-position: center; background-repeat: no-repeat; margin-bottom: 16rpx; }
.si-clock { background-image: url('../../static/detail_clock.png'); }
.si-check { background-image: url('../../static/detail_check.png'); }
.si-list { background-image: url('../../static/detail_list.png'); }
.si-close { background-image: url('../../static/detail_close.png'); }
.si-done { background-image: url('../../static/detail_done.png'); }
.status-text { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 36rpx; font-weight: 500; color: #17191c; margin-bottom: 8rpx; }
.status-desc { font-size: 26rpx; color: #777b86; text-align: center; }

.info-card { background: #ffffff; border-radius: 48rpx; padding: 32rpx 32rpx; margin-bottom: 20rpx; box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx; }
.card-title { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 500; color: #17191c; display: block; margin-bottom: 24rpx; }
.info-row { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 14rpx 0; border-bottom: 1px solid #f7f7f8; }
.info-row:last-child { border-bottom: none; }
.info-label-row { display: flex; flex-direction: row; align-items: center; }
.row-icon { width: 26rpx; height: 26rpx; background-size: contain; background-position: center; background-repeat: no-repeat; margin-right: 12rpx; flex-shrink: 0; }
.icon-cal { background-image: url('../../static/appt_cal.png'); }
.icon-clock { background-image: url('../../static/detail_clock.png'); }
.icon-hosp { background-image: url('../../static/appt_hosp.png'); }
.icon-list { background-image: url('../../static/appt_list.png'); }
.icon-person { background-image: url('../../static/tab_mine_selected.png'); }
.icon-time { background-image: url('../../static/appt_pending.png'); }
.info-label { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 430; color: #777b86; }
.info-value { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; text-align: right; max-width: 60%; }
.mono { font-size: 24rpx; font-family: monospace; letter-spacing: 0.2rpx; }
.notice { margin-top: 8rpx; padding: 0 8rpx; }
.notice-text { font-size: 24rpx; color: #a3a6af; line-height: 1.5; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 24rpx 32rpx 40rpx; background: linear-gradient(to top, #ffffff 60%, rgba(255,255,255,0)); display: flex; flex-direction: row; gap: 12rpx; pointer-events: none; }
.bottom-bar button { pointer-events: auto; }
.cancel-text-btn { height: 96rpx; line-height: 96rpx; padding: 0 16rpx; background: transparent; border-radius: 9999px; font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 26rpx; font-weight: 450; color: #ff3b30; border: none; box-shadow: none; margin: 0; flex-shrink: 0; }
.cancel-text-btn:active { opacity: 0.6; }
.guide-text-btn { height: 96rpx; line-height: 96rpx; padding: 0 24rpx; background: #ffffff; border-radius: 9999px; font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; border: 2rpx solid #17191c; box-shadow: none; margin: 0; flex-shrink: 0; }
.guide-text-btn:active { opacity: 0.7; }
.prep-text-btn { height: 96rpx; line-height: 96rpx; padding: 0 24rpx; background: #ffffff; border-radius: 9999px; font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 26rpx; font-weight: 450; color: #17191c; border: 2rpx solid #17191c; box-shadow: none; margin: 0; flex-shrink: 0; }
.prep-text-btn:active { opacity: 0.7; }
.questionnaire-btn { height: 96rpx; line-height: 96rpx; padding: 0 24rpx; background: #0057b3; border-radius: 9999px; font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 26rpx; font-weight: 450; color: #ffffff; border: none; box-shadow: none; margin: 0; flex-shrink: 0; }
.questionnaire-btn:active { opacity: 0.8; }
.pay-btn { flex: 1; height: 96rpx; line-height: 96rpx; background: #17191c; border-radius: 9999px; font-family: "Sohne", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; font-size: 30rpx; font-weight: 450; color: #ffffff; letter-spacing: -0.27rpx; border: none; box-shadow: none; margin: 0; }
.pay-btn:active { opacity: 0.85; transform: scale(0.97); }
</style>
