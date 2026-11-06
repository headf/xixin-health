<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">套餐详情</text>
			<view class="header-spacer"></view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 内容 -->
		<scroll-view class="content" v-if="!loading && pkg" scroll-y>
			<!-- 套餐名称与价格卡片 -->
			<view class="hero-card">
				<text class="pkg-name">{{ pkg.packageName }}</text>
				<text class="pkg-price">
					<text class="price-symbol">¥</text>
					<text class="price-value">{{ formatPrice(pkg.priceCent) }}</text>
				</text>
			</view>

			<!-- 标签 -->
			<view class="tags-row">
				<text class="tag">{{ categoryLabel(pkg.category) }}</text>
				<text class="tag">{{ genderLabel(pkg.genderLimit) }}</text>
				<text class="tag">{{ ageLabel(pkg.ageRange) }}</text>
			</view>

			<!-- 检查项目列表 -->
			<view class="section-title" v-if="items.length > 0">检查项目</view>
			<view class="items-card" v-if="items.length > 0">
				<view class="item-row" v-for="(item, idx) in items" :key="item.id">
					<view class="item-index">{{ idx + 1 }}</view>
					<view class="item-info">
						<text class="item-name">{{ item.itemName }}</text>
						<text class="item-meta" v-if="item.unit">参考范围: {{ formatRange(item) }}</text>
					</view>
				</view>
			</view>
			<view class="items-empty" v-else-if="!loadingItems">
				<text class="items-empty-text">暂无检查项目数据</text>
			</view>
			<view class="items-loading" v-if="loadingItems">
				<text class="loading-text">加载项目中...</text>
			</view>

			<!-- 基本信息卡片 -->
			<view class="info-card">
				<text class="info-title">套餐信息</text>
				<view class="info-row">
					<text class="info-label">套餐编码</text>
					<text class="info-value">{{ pkg.packageCode }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">适用性别</text>
					<text class="info-value">{{ genderLabel(pkg.genderLimit) }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">年龄范围</text>
					<text class="info-value">{{ ageLabel(pkg.ageRange) }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">套餐分类</text>
					<text class="info-value">{{ categoryLabel(pkg.category) }}</text>
				</view>
			</view>

			<!-- 说明文字 -->
			<view class="notice">
				<text class="notice-text">* 以上信息仅供参考，具体检查项目以到院为准</text>
			</view>

			<view class="bottom-spacer"></view>
		</scroll-view>

		<!-- 底部预约按钮 -->
		<view class="bottom-bar" v-if="!loading && pkg">
			<button class="appointment-btn" type="default" @tap="makeAppointment">立即预约</button>
		</view>
	</view>
</template>

<script>
import { packageApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			loadingItems: false,
			pkg: null,
			items: []
		}
	},

	onShow() {
		uni.setNavigationBarColor({
			frontColor: '#000000',
			backgroundColor: '#ffffff'
		})
		this.loadPackage()
	},

	methods: {
		async loadPackage() {
			this.loading = true
			try {
				const cached = uni.getStorageSync('selectedPackage')
				if (cached) {
					this.pkg = cached
					this.loading = false
					this.loadItems(cached.id)
					return
				}

				const list = await packageApi.getPackages()
				if (list && list.length > 0) {
					this.pkg = list[0]
					this.loading = false
					this.loadItems(list[0].id)
				} else {
					this.loading = false
				}
			} catch (err) {
				console.error('加载套餐详情失败', err)
				this.loading = false
			}
		},

		async loadItems(packageId) {
			this.loadingItems = true
			try {
				const data = await packageApi.getPackageItems(packageId)
				this.items = data || []
			} catch (err) {
				console.error('加载检查项目失败', err)
				this.items = []
			} finally {
				this.loadingItems = false
			}
		},

		goBack() {
			uni.navigateBack()
		},

		formatPrice(cent) {
			if (cent == null) return '0.00'
			return (cent / 100).toFixed(2)
		},

		formatRange(item) {
			if (item.referenceMin != null && item.referenceMax != null) {
				return item.referenceMin + ' ~ ' + item.referenceMax + ' ' + (item.unit || '')
			}
			return item.unit || ''
		},

		categoryLabel(cat) {
			const map = {
				BASIC: '基础体检', FULL: '全面体检', SENIOR: '老年体检',
				GYN: '妇科体检', CARDIO: '心血管专项', DIABETES: '糖尿病筛查',
				LIVER: '肝脏健康', KIDNEY: '肾脏健康', STUDENT: '学生体检', PREMIUM: '至尊体检'
			}
			return map[cat] || cat || '其他'
		},

		genderLabel(gender) {
			const map = { ALL: '不限性别', MALE: '男', FEMALE: '女' }
			return map[gender] || gender || '不限'
		},

		ageLabel(range) {
			if (range == null || range === '' || range === 'null') return '不限年龄'
			return range + '岁'
		},

		makeAppointment() {
			uni.navigateTo({
				url: '/pages/appointment/create'
			})
		}
	}
}
</script>

<style>
.page {
	min-height: 100vh;
	background: #f7f7f8;
	display: flex;
	flex-direction: column;
	padding-top: calc(var(--status-bar-height, 44px));
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

/* 套餐名 + 价格 */
.hero-card {
	background: #fbe1d1; border-radius: 48rpx;
	padding: 40rpx 36rpx; margin-bottom: 24rpx;
}
.pkg-name {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 40rpx; font-weight: 500; color: #17191c;
	letter-spacing: -0.36rpx; display: block; margin-bottom: 20rpx;
}
.pkg-price { display: flex; flex-direction: row; align-items: baseline; }
.price-symbol {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 32rpx; font-weight: 450; color: #17191c; margin-right: 4rpx;
}
.price-value {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 56rpx; font-weight: 500; color: #17191c; letter-spacing: -0.5rpx;
}

/* 标签 */
.tags-row { display: flex; flex-direction: row; flex-wrap: wrap; gap: 12rpx; margin-bottom: 24rpx; }
.tag {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 24rpx; font-weight: 430; color: #777b86;
	background: #ffffff; padding: 8rpx 24rpx; border-radius: 9999px;
	line-height: 1.4; box-shadow: rgba(4, 23, 43, 0.04) 0px 0px 0px 1px;
}

/* 区块标题 */
.section-title {
	font-size: 26rpx; font-weight: 500; color: #777b86;
	margin-bottom: 16rpx; padding: 0 4rpx;
}

/* 检查项目列表 */
.items-card {
	background: #ffffff; border-radius: 48rpx; padding: 8rpx 0;
	margin-bottom: 24rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}
.item-row {
	display: flex; flex-direction: row; align-items: center;
	padding: 22rpx 28rpx; border-bottom: 1px solid #f7f7f8;
}
.item-row:last-child { border-bottom: none; }
.item-index {
	width: 40rpx; height: 40rpx; border-radius: 20rpx;
	background: #f7f7f8; display: flex; align-items: center; justify-content: center;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 22rpx; font-weight: 500; color: #777b86;
	margin-right: 20rpx; flex-shrink: 0;
}
.item-info { flex: 1; }
.item-name {
	font-size: 28rpx; font-weight: 450; color: #17191c;
	display: block; margin-bottom: 4rpx;
}
.item-meta { font-size: 22rpx; color: #a3a6af; }
.items-empty {
	background: #ffffff; border-radius: 48rpx; padding: 40rpx;
	margin-bottom: 24rpx; display: flex; align-items: center; justify-content: center;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}
.items-empty-text { font-size: 26rpx; color: #a3a6af; }
.items-loading { padding: 20rpx 0; text-align: center; }

/* 信息卡片 */
.info-card {
	background: #ffffff; border-radius: 48rpx; padding: 36rpx 32rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}
.info-title {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 500; color: #17191c;
	letter-spacing: -0.25rpx; display: block; margin-bottom: 28rpx;
}
.info-row {
	display: flex; flex-direction: row; align-items: center; justify-content: space-between;
	padding: 16rpx 0; border-bottom: 1px solid #f7f7f8;
}
.info-row:last-child { border-bottom: none; }
.info-label {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 430; color: #777b86;
}
.info-value {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx; font-weight: 450; color: #17191c; text-align: right;
}

/* 说明 */
.notice { margin-top: 24rpx; padding: 0 8rpx; }
.notice-text { font-size: 24rpx; color: #a3a6af; line-height: 1.5; }
.bottom-spacer { height: 20rpx; }

/* 底部预约按钮 */
.bottom-bar {
	position: fixed; bottom: 0; left: 0; right: 0;
	padding: 24rpx 32rpx 40rpx;
	background: linear-gradient(to top, #ffffff 60%, rgba(255, 255, 255, 0));
	pointer-events: none;
}
.bottom-bar .appointment-btn { pointer-events: auto; }
.appointment-btn {
	height: 96rpx; line-height: 96rpx;
	background: #17191c; border-radius: 9999px;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx; font-weight: 450; color: #ffffff;
	letter-spacing: -0.27rpx; border: none; box-shadow: none; margin: 0;
}
.appointment-btn:active { opacity: 0.85; transform: scale(0.97); }
</style>
