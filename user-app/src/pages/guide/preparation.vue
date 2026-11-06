<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">检前准备</text>
			<view class="header-spacer"></view>
		</view>

		<scroll-view class="scroll" scroll-y>
			<!-- 饮食注意事项 -->
			<view class="section">
				<view class="section-icon"></view>
				<text class="section-title">饮食注意事项</text>
				<view class="card-list">
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">清淡饮食</text>
							<text class="tip-desc">体检前三天保持清淡饮食，避免油腻、高糖、高蛋白食物</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">禁酒</text>
							<text class="tip-desc">体检前三天禁酒，酒精会影响肝功能等多项检查结果</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon">⏰</text>
						<view class="tip-content">
							<text class="tip-title">空腹8-12小时</text>
							<text class="tip-desc">体检前一晚20:00后禁食，可少量饮水（不超过200ml）</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">药物服用</text>
							<text class="tip-desc">慢性病患者请按平常服药，体检时告知医生用药情况</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 女性特殊事项 -->
			<view class="section">
				<view class="section-icon">👩</view>
				<text class="section-title">女性特殊事项</text>
				<view class="card-list">
					<view class="tip-card">
						<text class="tip-icon">📅</text>
						<view class="tip-content">
							<text class="tip-title">避开经期</text>
							<text class="tip-desc">建议月经结束后3-7天进行体检，经期不宜做尿检和妇科检查</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">着装建议</text>
							<text class="tip-desc">避免穿连衣裙、连裤袜，建议穿分体式衣物方便检查</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">备孕及孕期</text>
							<text class="tip-desc">备孕或可能已怀孕者，请提前告知医护人员，避免X光等放射性检查</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 着装建议 -->
			<view class="section">
				<view class="section-icon">👔</view>
				<text class="section-title">着装建议</text>
				<view class="card-list">
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">宽松衣物</text>
							<text class="tip-desc">穿着宽松、舒适、易穿脱的衣物，方便各项检查</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">不戴首饰</text>
							<text class="tip-desc">体检时请勿佩戴首饰、金属饰品，以免影响影像检查结果</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">舒适鞋履</text>
							<text class="tip-desc">建议穿平底鞋或运动鞋，方便在体检中心走动</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 体检流程 -->
			<view class="section">
				<view class="section-icon">·</view>
				<text class="section-title">体检流程说明</text>
				<view class="flow-list">
					<view class="flow-step">
						<view class="step-num">1</view>
						<view class="step-content">
							<text class="step-title">前台签到</text>
							<text class="step-desc">到达体检中心后，先到前台出示身份证签到，领取导检单</text>
						</view>
					</view>
					<view class="flow-step">
						<view class="step-num">2</view>
						<view class="step-content">
							<text class="step-title">空腹项目检查</text>
							<text class="step-desc">先完成抽血、B超等需要空腹的检查项目</text>
						</view>
					</view>
					<view class="flow-step">
						<view class="step-num">3</view>
						<view class="step-content">
							<text class="step-title">领取早餐</text>
							<text class="step-desc">空腹项目完成后可领取免费营养早餐</text>
						</view>
					</view>
					<view class="flow-step">
						<view class="step-num">4</view>
						<view class="step-content">
							<text class="step-title">非空腹项目检查</text>
							<text class="step-desc">依次完成各科室非空腹检查项目</text>
						</view>
					</view>
					<view class="flow-step">
						<view class="step-num">5</view>
						<view class="step-content">
							<text class="step-title">交回导检单</text>
							<text class="step-desc">所有项目完成后，将导检单交回前台</text>
						</view>
					</view>
					<view class="flow-step">
						<view class="step-num">6</view>
						<view class="step-content">
							<text class="step-title">等待报告</text>
							<text class="step-desc">一般3-7个工作日后可在APP查看电子报告</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 携带证件 -->
			<view class="section">
				<view class="section-icon">🪪</view>
				<text class="section-title">携带证件提醒</text>
				<view class="card-list">
					<view class="tip-card">
						<text class="tip-icon">🆔</text>
						<view class="tip-content">
							<text class="tip-title">身份证</text>
							<text class="tip-desc">请携带本人有效身份证原件，用于签到登记</text>
						</view>
					</view>
					<view class="tip-card">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">手机</text>
							<text class="tip-desc">请确保手机畅通，以便接收体检相关通知</text>
						</view>
					</view>
					<view class="tip-card" v-if="hasInsurance">
						<text class="tip-icon"></text>
						<view class="tip-content">
							<text class="tip-title">既往病历</text>
							<text class="tip-desc">如有既往病历或检查报告，建议携带供医生参考</text>
						</view>
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
		return {
			hasInsurance: true
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
	},
	methods: {
		goBack() {
			uni.navigateBack()
		}
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f7f7f8; display: flex; flex-direction: column; padding-top: calc(44px); }
.header { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 16rpx 32rpx; background: #ffffff; position: relative; }
.back-btn { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; padding: 12rpx 8rpx; }
.back-btn:active { opacity: 0.6; }
.header-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 40rpx; font-weight: 400; color: #17191c; position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap; }
.header-spacer { width: 80rpx; }
.scroll { flex: 1; padding: 24rpx 32rpx 40rpx; }

.section { margin-bottom: 32rpx; }
.section-icon { font-size: 36rpx; margin-bottom: 8rpx; }
.section-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 34rpx; font-weight: 500; color: #17191c; display: block; margin-bottom: 16rpx; }

.card-list { display: flex; flex-direction: column; gap: 12rpx; }
.tip-card {
	display: flex; flex-direction: row; align-items: flex-start;
	background: #ffffff; border-radius: 32rpx; padding: 24rpx 28rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}
.tip-icon { font-size: 40rpx; margin-right: 20rpx; flex-shrink: 0; margin-top: 2rpx; }
.tip-content { flex: 1; }
.tip-title { font-size: 28rpx; font-weight: 500; color: #17191c; display: block; margin-bottom: 6rpx; }
.tip-desc { font-size: 24rpx; color: #777b86; line-height: 1.6; }

.flow-list { display: flex; flex-direction: column; gap: 0; background: #ffffff; border-radius: 32rpx; padding: 8rpx 0; box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx; }
.flow-step {
	display: flex; flex-direction: row; align-items: flex-start;
	padding: 24rpx 28rpx; border-bottom: 1px solid #f7f7f8;
}
.flow-step:last-child { border-bottom: none; }
.step-num {
	width: 44rpx; height: 44rpx; border-radius: 22rpx; background: #17191c;
	color: #ffffff; font-size: 22rpx; font-weight: 500; text-align: center; line-height: 44rpx;
	margin-right: 20rpx; flex-shrink: 0; margin-top: 2rpx;
}
.step-content { flex: 1; }
.step-title { font-size: 28rpx; font-weight: 500; color: #17191c; display: block; margin-bottom: 4rpx; }
.step-desc { font-size: 24rpx; color: #777b86; line-height: 1.5; }

.bottom-spacer { height: 40rpx; }
</style>
