<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">体检报告</text>
			<view class="header-spacer"></view>
		</view>

		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="content" v-if="!loading">
			<view class="section-title">全部报告</view>

			<view class="report-card" v-for="item in reports" :key="item.id" @tap="showDetail(item)">
				<view class="card-top">
					<text class="report-no">{{ item.reportNo }}</text>
					<text class="status-tag" :class="statusClass(item.reportStatus)">{{ statusLabel(item.reportStatus) }}</text>
				</view>
				<view class="card-body">
					<view class="info-row">
						<text class="info-label">异常等级</text>
						<text class="info-value" :class="abnormalClass(item.abnormalStatus)">{{ abnormalLabel(item.abnormalStatus) }}</text>
					</view>
					<view class="info-row" v-if="item.summary">
						<text class="info-label">诊断摘要</text>
						<text class="info-value summary">{{ item.summary }}</text>
					</view>
				</view>
				<view class="card-footer">
					<text class="footer-text">生成时间 {{ formatTime(item.generatedAt) }}</text>
					<text class="card-arrow">›</text>
				</view>
			</view>

			<view class="empty-state" v-if="reports.length === 0">
				<text class="empty-icon"></text>
				<text class="empty-text">暂无体检报告</text>
				<text class="empty-desc">完成体检后即可查看报告</text>
			</view>
		</view>
	</view>
</template>

<script>
import { reportApi } from '../../common/api.js'

export default {
	data() { return { loading: true, reports: [], userId: null } },
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadReports()
	},
	methods: {
		async loadReports() {
			this.loading = true; this.userId = uni.getStorageSync('userId')
			try {
				const data = await reportApi.getByUser(this.userId)
				// 同次体检只保留最新报告
					const map = new Map()
					for (const r of (data || [])) {
						const key = r.examRecordId || r.id
						if (!map.has(key) || new Date(r.generatedAt || 0) > new Date(map.get(key).generatedAt || 0)) {
							map.set(key, r)
						}
					}
					this.reports = [...map.values()]
			} catch (e) { console.error('加载报告失败', e)
			} finally { this.loading = false }
		},
		goBack() { uni.navigateBack() },
		showDetail(item) { uni.setStorageSync('selectedReport', item); uni.navigateTo({ url: '/pages/report/detail' }) },
		statusLabel(s) { return ({ DRAFT: '草稿', CONFIRMED: '已确认', PUBLISHED: '已发布' })[s] || s },
		statusClass(s) { return ({ DRAFT: 'st-gray', CONFIRMED: 'st-blue', PUBLISHED: 'st-green' })[s] || '' },
		abnormalLabel(s) { return ({ NORMAL: '正常', ABNORMAL: '异常', CRITICAL: '危急' })[s] || s },
		abnormalClass(s) { return ({ NORMAL: 'ab-normal', ABNORMAL: 'ab-abnormal', CRITICAL: 'ab-critical' })[s] || '' },
		formatTime(t) { return t ? t.substring(0, 10) : '' }
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f7f7f8; display: flex; flex-direction: column; padding-top: calc(44px); }
.header {
	display: flex; flex-direction: row; align-items: center; justify-content: space-between;
	padding: 16rpx 32rpx; background: #ffffff; position: relative;
}
.back-btn { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; padding: 12rpx 8rpx; }
.back-btn:active { opacity: 0.6; }
.header-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 40rpx; font-weight: 400; color: #17191c; position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap; }
.header-spacer { width: 80rpx; }
.loading-state { flex: 1; display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 28rpx; color: #777b86; }

.content { flex: 1; padding: 24rpx 32rpx 40rpx; }
.section-title { font-size: 26rpx; font-weight: 500; color: #777b86; margin: 8rpx 8rpx 20rpx; }

.report-card {
	background: #ffffff; border-radius: 48rpx; padding: 24rpx 28rpx; margin-bottom: 16rpx;
	box-shadow: rgba(4,23,43,0.05) 0px 0px 0px 1px, rgba(0,0,0,0.1) 0px 20rpx 50rpx -10rpx, rgba(0,0,0,0.1) 0px 16rpx 20rpx -12rpx;
}
.report-card:active { opacity: 0.85; }
.card-top { display: flex; flex-direction: row; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.report-no { font-size: 22rpx; font-weight: 430; color: #a3a6af; }
.status-tag { font-size: 22rpx; font-weight: 450; padding: 4rpx 18rpx; border-radius: 9999px; }
.st-gray { color: #777b86; background: #f7f7f8; }
.st-blue { color: #17191c; background: #d3e3fc; }
.st-green { color: #5d2a1a; background: #fbe1d1; }

.card-body { border-top: 1px solid #f7f7f8; padding-top: 12rpx; }
.info-row { display: flex; flex-direction: row; align-items: flex-start; margin-bottom: 8rpx; }
.info-label { font-size: 26rpx; font-weight: 430; color: #777b86; width: 120rpx; flex-shrink: 0; }
.info-value { font-size: 26rpx; font-weight: 450; color: #17191c; flex: 1; }
.info-value.summary { line-height: 1.5; }
.ab-normal { color: #34c759; }
.ab-abnormal { color: #5d2a1a; }
.ab-critical { color: #dd524d; }

.card-footer { display: flex; flex-direction: row; align-items: center; justify-content: space-between; margin-top: 12rpx; padding-top: 10rpx; border-top: 1px solid #f7f7f8; }
.footer-text { font-size: 22rpx; color: #a3a6af; }
.card-arrow { font-size: 32rpx; color: #a3a6af; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; }
.empty-icon { font-size: 64rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #17191c; margin-bottom: 8rpx; }
.empty-desc { font-size: 26rpx; color: #a3a6af; }
</style>
