<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">检前准备</text>
			<view class="header-spacer"></view>
		</view>

		<scroll-view class="scroll" scroll-y>
			<!-- 饮食指南 -->
			<view class="card">
				<view class="card-header">
					<text class="card-icon">🍽️</text>
					<text class="card-title">饮食指南</text>
				</view>
				<view class="card-body">
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">检前3天清淡饮食，避免油腻、高蛋白食物</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">避免饮酒及暴饮暴食</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">检前8-12小时空腹（不能吃东西，可少量饮水）</text>
					</view>
					<view class="card-item highlight">
						<text class="bullet">•</text>
						<text class="card-text">空腹项目：抽血、腹部B超、碳13呼气试验</text>
					</view>
				</view>
			</view>

			<!-- 女性注意事项 -->
			<view class="card">
				<view class="card-header">
					<text class="card-icon">👩</text>
					<text class="card-title">女性注意事项</text>
				</view>
				<view class="card-body">
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">避开月经期，建议月经结束3-7天后体检</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">不穿连衣裙、连裤袜，建议穿分体式衣物</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">怀孕或可能怀孕请告知医生，不做X光/CT</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">哺乳期妇女请提前说明</text>
					</view>
				</view>
			</view>

			<!-- 着装建议 -->
			<view class="card">
				<view class="card-header">
					<text class="card-icon">👔</text>
					<text class="card-title">着装建议</text>
				</view>
				<view class="card-body">
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">穿着宽松、舒适、方便穿脱的衣物</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">不穿戴金属饰品的衣物</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">不要佩戴首饰、手表</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">不要穿高跟鞋</text>
					</view>
				</view>
			</view>

			<!-- 体检当天 -->
			<view class="card">
				<view class="card-header">
					<text class="card-icon">📋</text>
					<text class="card-title">体检当天</text>
				</view>
				<view class="card-body">
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">携带身份证原件</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">如有既往病历/报告请携带</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">有慢性病者正常服药（少量饮水送服）</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">到院后先到前台签到登记</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">按照导检单指引逐项检查</text>
					</view>
				</view>
			</view>

			<!-- 温馨提示 -->
			<view class="card">
				<view class="card-header">
					<text class="card-icon">💡</text>
					<text class="card-title">温馨提示</text>
				</view>
				<view class="card-body">
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">全部项目完成后将导检单交回前台</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">体检报告一般3-7个工作日出</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">可在App上查看电子报告</text>
					</view>
					<view class="card-item">
						<text class="bullet">•</text>
						<text class="card-text">有任何疑问请联系客服</text>
					</view>
				</view>
			</view>

			<view class="bottom-spacer"></view>
		</scroll-view>
	</view>
</template>

<script>
export default {
	data() {
		return {}
	},
	methods: {
		goBack() {
			uni.navigateBack()
		}
	}
}
</script>

<style>
.page {
	min-height: 100vh;
	background: #f0f7fa;
	display: flex;
	flex-direction: column;
	padding-top: calc(48px);
}

/* 顶部导航 */
.header {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 32rpx;
	background: #ffffff;
	position: relative;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.back-btn {
	font-size: 28rpx;
	font-weight: 450;
	color: #1e293b;
	padding: 12rpx 8rpx;
}
.back-btn:active { opacity: 0.6; }
.header-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #1e293b;
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
	white-space: nowrap;
}
.header-spacer { width: 80rpx; }

.scroll {
	flex: 1;
	padding: 24rpx 28rpx;
}

/* 卡片 */
.card {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 28rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}
.card-header {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 16rpx;
	border-bottom: 2rpx solid #f0f7fa;
}
.card-icon {
	font-size: 40rpx;
	margin-right: 16rpx;
}
.card-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #1e293b;
}

.card-body {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}
.card-item {
	display: flex;
	flex-direction: row;
	align-items: flex-start;
	gap: 12rpx;
	padding: 4rpx 0;
}
.bullet {
	font-size: 28rpx;
	color: #5bb5c5;
	flex-shrink: 0;
	line-height: 1.6;
}
.card-text {
	font-size: 28rpx;
	color: #334155;
	line-height: 1.6;
	flex: 1;
}
.card-item.highlight {
	background: #e8f7fa;
	border-radius: 12rpx;
	padding: 12rpx;
	margin-top: 4rpx;
}
.card-item.highlight .bullet {
	color: #3a9bb0;
}
.card-item.highlight .card-text {
	color: #1e6b7a;
	font-weight: 500;
}

.bottom-spacer {
	height: 40rpx;
}
</style>
