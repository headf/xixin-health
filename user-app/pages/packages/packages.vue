<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="nav-bar">
			<text class="nav-title">体检套餐</text>
		</view>

		<!-- 分类 Tab 筛选 -->
		<scroll-view class="category-scroll" scroll-x show-scrollbar="false">
			<view class="category-row">
				<view
					class="category-tab"
					:class="{ active: activeCategory === 'ALL' }"
					@tap="activeCategory = 'ALL'; filterPackages()"
				>全部</view>
				<view
					class="category-tab"
					:class="{ active: activeCategory === cat.code }"
					v-for="cat in categories" :key="cat.code"
					@tap="activeCategory = cat.code; filterPackages()"
				>{{ cat.name }}</view>
			</view>
		</scroll-view>

		<!-- 排序栏 -->
		<view class="sort-bar">
			<view
				class="sort-item"
				:class="{ active: sortBy === 'default' }"
				@tap="sortBy = 'default'; filterPackages()"
			>推荐</view>
			<view
				class="sort-item"
				:class="{ active: sortBy === 'price' }"
				@tap="sortBy = sortBy === 'price' && sortOrder === 'asc' ? (sortOrder = 'desc', sortBy = 'price') : (sortBy = 'price', sortOrder = 'asc'); filterPackages()"
			>
				价格
				<text class="sort-arrow">{{ sortBy === 'price' ? (sortOrder === 'asc' ? '↑' : '↓') : '↕' }}</text>
			</view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 错误状态 -->
		<view class="error-state" v-if="errorMsg">
			<text class="error-text">{{ errorMsg }}</text>
			<text class="retry-btn" @tap="loadPackages">重新加载</text>
		</view>

		<!-- 套餐列表 -->
		<scroll-view class="list-scroll" v-if="!loading && !errorMsg" scroll-y>
			<view class="package-list">
				<view class="package-card" v-for="pkg in filteredPackages" :key="pkg.id" @tap="showPackageDetail(pkg)">
					<!-- 封面区 -->
					<view class="card-cover" :style="{ background: pkg.coverBg || 'linear-gradient(135deg, #0ea5e9, #38bdf8)' }">
						<text class="card-emoji">{{ pkg.emoji || '🏥' }}</text>
						<text class="card-category">{{ pkg.categoryLabel }}</text>
					</view>

					<!-- 信息区 -->
					<view class="card-body">
						<text class="card-name">{{ pkg.packageName }}</text>

						<!-- 标签行 -->
						<view class="card-tags">
							<text class="card-tag tag-gender">{{ genderLabel(pkg.genderLimit) }}</text>
							<text class="card-tag tag-age">{{ ageLabel(pkg.ageRange) }}</text>
						</view>

						<!-- 核心卖点 -->
						<text class="card-features">{{ pkg.features || '涵盖常规体检项目' }}</text>

						<!-- 底部 -->
						<view class="card-footer">
							<view class="price-area">
								<text class="price-symbol">¥</text>
								<text class="price-value">{{ formatPrice(pkg.priceCent) }}</text>
								<text class="price-original" v-if="pkg.originalPrice">¥{{ formatPrice(pkg.originalPrice) }}</text>
							</view>
							<button class="book-btn">立即预约</button>
						</view>
					</view>
				</view>
			</view>

			<view class="empty-list" v-if="filteredPackages.length === 0">
				<text class="empty-text">暂无符合条件的套餐</text>
			</view>

			<view class="bottom-safe"></view>
		</scroll-view>
	</view>
</template>

<script>
import { packageApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			errorMsg: '',
			packages: [],
			filteredPackages: [],
			activeCategory: 'ALL',
			sortBy: 'default',
			sortOrder: 'asc',
			categories: [
				{ code: 'BASIC', name: '基础体检' },
				{ code: 'FULL', name: '全面体检' },
				{ code: 'DEEP', name: '深度体检' },
				{ code: 'SENIOR', name: '老年体检' },
				{ code: 'STUDENT', name: '学生体检' },
			]
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.loadPackages()
	},
	methods: {
		async loadPackages() {
			this.loading = true; this.errorMsg = ''
			try {
				const data = await packageApi.getPackages()
				const catMap = { BASIC: '基础体检', FULL: '全面体检', SENIOR: '老年体检', DEEP: '深度体检', STUDENT: '学生体检', PREMIUM: '至尊体检' }
				const emojiMap = { BASIC: '📋', FULL: '🏥', SENIOR: '👴', DEEP: '🔬', STUDENT: '🎒', PREMIUM: '👑' }
				const featuresMap = [
					'含血常规、尿常规、内外科、心电图等基础项目',
					'含CT、彩超、肿瘤筛查等20+项目全面检查',
					'基础项目全覆盖，高性价比之选',
					'含心脏彩超、无痛胃肠镜等深度筛查',
					'专为学生定制的入学/毕业体检方案',
					'VIP通道+专家解读报告'
				]
				const bgMap = [
					'linear-gradient(135deg, #0ea5e9, #38bdf8)',
					'linear-gradient(135deg, #6366f1, #a78bfa)',
					'linear-gradient(135deg, #0d9488, #34d399)',
					'linear-gradient(135deg, #8b5cf6, #c084fc)',
					'linear-gradient(135deg, #f59e0b, #fbbf24)',
					'linear-gradient(135deg, #ef4444, #f87171)'
				]

				this.packages = (data || []).map((p, i) => ({
					...p,
					categoryLabel: catMap[p.category] || p.category || '其他',
					emoji: emojiMap[p.category] || '🏥',
					features: featuresMap[i % featuresMap.length],
					coverBg: bgMap[i % bgMap.length],
					originalPrice: p.originalPrice || (p.priceCent || 0) * 1.3
				}))
				this.filterPackages()
			} catch (err) {
				this.errorMsg = err.message || '加载失败，请重试'
			} finally { this.loading = false }
		},
		filterPackages() {
			let list = [...this.packages]
			// 分类筛选
			if (this.activeCategory !== 'ALL') {
				list = list.filter(p => p.category === this.activeCategory)
			}
			// 排序
			if (this.sortBy === 'price') {
				list.sort((a, b) => this.sortOrder === 'asc' ? (a.priceCent || 0) - (b.priceCent || 0) : (b.priceCent || 0) - (a.priceCent || 0))
			}
			this.filteredPackages = list
		},
		formatPrice(cent) { return cent == null ? '0.00' : (cent / 100).toFixed(2) },
		genderLabel(g) { return ({ ALL: '不限性别', MALE: '男性', FEMALE: '女性' })[g] || g || '不限' },
		ageLabel(r) { return (r == null || r === '' || r === 'null') ? '不限年龄' : r + '岁' },
		showPackageDetail(pkg) {
			uni.setStorageSync('selectedPackage', pkg)
			uni.navigateTo({ url: '/pages/packages/detail' })
		}
	}
}
</script>

<style>
/* ===== 页面容器 ===== */
.page {
	min-height: 100vh;
	background: var(--surface-page, #f8fafc);
	display: flex;
	flex-direction: column;
}

/* ===== 顶部导航 ===== */
.nav-bar {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	padding: 18rpx 28rpx 8rpx;
	padding-top: calc(var(--status-bar-height, 44px) + 24rpx);
	background: var(--surface-page, #f8fafc);
}
.nav-title {
	font-size: 34rpx;
	font-weight: 700;
	color: var(--text, #1e293b);
}

/* ===== 分类 Tab ===== */
.category-scroll {
	padding: 12rpx 0 8rpx;
	background: var(--surface-page, #f8fafc);
}
.category-row {
	display: flex;
	flex-direction: row;
	padding: 0 20rpx;
	gap: 12rpx;
}
.category-tab {
	padding: 12rpx 28rpx;
	font-size: 26rpx;
	color: var(--text-secondary, #64748b);
	background: var(--surface, #ffffff);
	border-radius: 9999px;
	border: 2rpx solid var(--border, #e2e8f0);
	white-space: nowrap;
}
.category-tab:active { opacity: 0.7; }
.category-tab.active {
	color: var(--brand, #0ea5e9);
	background: var(--brand-light, #e0f2fe);
	border-color: var(--brand-light, #e0f2fe);
	font-weight: 600;
}

/* ===== 排序栏 ===== */
.sort-bar {
	display: flex;
	flex-direction: row;
	padding: 6rpx 24rpx 12rpx;
	gap: 24rpx;
	background: var(--surface-page, #f8fafc);
	border-bottom: 2rpx solid var(--border-light, #f1f5f9);
}
.sort-item {
	font-size: 24rpx;
	color: var(--text-secondary, #64748b);
	padding: 4rpx 0;
}
.sort-item:active { opacity: 0.7; }
.sort-item.active {
	color: var(--brand, #0ea5e9);
	font-weight: 600;
}
.sort-arrow { font-size: 20rpx; margin-left: 4rpx; }

/* ===== 加载/错误状态 ===== */
.loading-state, .error-state {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}
.loading-text { font-size: 26rpx; color: var(--text-secondary, #64748b); }
.error-text { font-size: 26rpx; color: var(--text-secondary, #64748b); margin-bottom: 24rpx; }
.retry-btn {
	font-size: 26rpx; font-weight: 500; color: var(--brand, #0ea5e9);
	padding: 14rpx 40rpx; border: 2rpx solid var(--brand, #0ea5e9); border-radius: 9999px;
}

/* ===== 套餐列表 ===== */
.list-scroll { flex: 1; }
.package-list {
	padding: 16rpx 24rpx 140rpx;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

/* ===== 套餐卡片 ===== */
.package-card {
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	overflow: hidden;
	box-shadow: var(--shadow-sm);
}
.package-card:active { opacity: 0.92; }

/* 封面 */
.card-cover {
	height: 180rpx;
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 24rpx 28rpx;
	position: relative;
}
.card-emoji { font-size: 60rpx; margin-right: 16rpx; }
.card-category {
	font-size: 24rpx;
	color: rgba(255,255,255,0.9);
	background: rgba(255,255,255,0.2);
	padding: 6rpx 20rpx;
	border-radius: 9999px;
	position: absolute;
	top: 20rpx;
	right: 20rpx;
}

/* 信息 */
.card-body { padding: 24rpx 28rpx 28rpx; }
.card-name { font-size: 30rpx; font-weight: 700; color: var(--text, #1e293b); display: block; margin-bottom: 14rpx; }

.card-tags {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	gap: 8rpx;
	margin-bottom: 12rpx;
}
.card-tag {
	font-size: 20rpx;
	padding: 4rpx 14rpx;
	border-radius: 9999px;
	font-weight: 450;
}
.tag-gender { color: var(--brand, #0ea5e9); background: var(--brand-light, #e0f2fe); }
.tag-age { color: var(--secondary, #0d9488); background: var(--secondary-light, #ccfbf1); }
.tag-sale { color: #d97706; background: var(--warning-bg, #fef3c7); }
.card-features {
	font-size: 24rpx;
	color: var(--text-secondary, #64748b);
	display: block;
	margin-bottom: 20rpx;
	line-height: 1.5;
}

.card-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}
.price-area {
	display: flex;
	flex-direction: row;
	align-items: baseline;
	gap: 4rpx;
}
.price-symbol { font-size: 24rpx; font-weight: 500; color: var(--danger, #ef4444); }
.price-value { font-size: 36rpx; font-weight: 700; color: var(--danger, #ef4444); }
.price-original { font-size: 22rpx; color: var(--text-disabled, #cbd5e1); text-decoration: line-through; margin-left: 8rpx; }
.book-btn {
	height: 60rpx;
	line-height: 60rpx;
	padding: 0 32rpx;
	background: var(--brand, #0ea5e9);
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 500;
	border-radius: 9999px;
}
.book-btn:active { opacity: 0.85; }

/* ===== 空状态 ===== */
.empty-list {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
}
.empty-text { font-size: 26rpx; color: var(--text-placeholder, #94a3b8); }

.bottom-safe { height: 40rpx; }
</style>
