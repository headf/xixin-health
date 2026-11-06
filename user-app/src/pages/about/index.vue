<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">关于我们</text>
			<view class="header-spacer"></view>
		</view>

		<view class="content">
			<view class="loading-state" v-if="loading">
				<text class="loading-text">加载中...</text>
			</view>

			<template v-if="!loading">
				<!-- 介绍卡片 -->
				<view class="card">
					<view class="about-header">
						<view class="about-logo">熙</view>
						<text class="about-title">{{ content.about_title || '熙心健康体检管理系统' }}</text>
					</view>
					<text class="about-desc">{{ content.about_desc || '熙心健康是一套专业的体检管理平台，为用户提供便捷的体检预约、报告查询、健康管理等服务。' }}</text>
				</view>

				<!-- 核心功能 -->
				<view class="card" v-if="featureList.length">
					<text class="card-title">核心功能</text>
					<view class="feature-item" v-for="(f, i) in featureList" :key="i">
						<text class="feature-check">✓</text>
						<text class="feature-name">{{ f }}</text>
					</view>
				</view>

				<!-- 版本信息 -->
				<view class="card">
					<text class="card-title">版本信息</text>
					<view class="info-row">
						<text class="info-label">版本号</text>
						<text class="info-value">v1.0.0</text>
					</view>
					<view class="info-row">
						<text class="info-label">更新日期</text>
						<text class="info-value">2026年6月</text>
					</view>
				</view>

				<view class="copyright">
					<text class="copyright-text">熙心健康 © 2026 All Rights Reserved.</text>
				</view>
			</template>
		</view>
	</view>
</template>

<script>
import { contentApi } from '../../common/api.js'

export default {
	data() {
		return {
			content: {},
			featureList: [],
			loading: true
		}
	},

	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadContent()
	},

	methods: {
		async loadContent() {
			try {
				const keys = ['about_title', 'about_desc', 'about_features'].join(',')
				const data = await contentApi.batch(keys)
				if (data) {
					this.content = data
					try {
						const features = JSON.parse(data.about_features || '[]')
						this.featureList = Array.isArray(features) ? features : []
					} catch (e) {
						this.featureList = data.about_features ? data.about_features.split('\n') : []
					}
				}
			} catch (e) {
				console.warn('加载站点内容失败', e)
			} finally {
				this.loading = false
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

.loading-state {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}
.loading-text { font-size: 28rpx; color: #777b86; }

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

.about-header {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin-bottom: 20rpx;
}

.about-logo {
	width: 72rpx;
	height: 72rpx;
	border-radius: 20rpx;
	background: #f1f5f9;
	color: var(--text-secondary);
	font-size: 32rpx;
	font-weight: 600;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 20rpx;
	flex-shrink: 0;
}

.about-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #17191c;
	flex: 1;
}

.about-desc {
	font-size: 26rpx;
	color: #777b86;
	line-height: 1.7;
	display: block;
}

.feature-item {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 12rpx 0;
}

.feature-check {
	font-size: 28rpx;
	color: #10b981;
	margin-right: 16rpx;
	width: 36rpx;
	text-align: center;
}

.feature-name {
	font-size: 26rpx;
	color: #17191c;
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

.copyright {
	text-align: center;
	padding: 32rpx 0;
}

.copyright-text {
	font-size: 22rpx;
	color: #a3a6af;
}
</style>
