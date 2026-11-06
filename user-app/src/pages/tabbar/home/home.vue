<template>
	<view class="page">
		<!-- 顶部导航栏（自定义） -->
		<view class="nav-bar">
			<view class="nav-left">
				<text class="nav-logo">熙心健康</text>
			</view>
		</view>

		<scroll-view class="scroll-wrap" scroll-y show-scrollbar="false">
			<!-- ===== 轮播 Banner ===== -->
			<swiper class="banner-swiper" circular autoplay interval="4000" duration="400" @change="onSwiperChange">
				<swiper-item class="banner-item" v-for="(banner, idx) in banners" :key="idx">
					<view class="banner-card" :style="bannerBg(banner)" @tap="bannerTap(banner && banner.action)">
						<view class="banner-content">
							<text class="banner-title">{{ banner.title }}</text>
							<text class="banner-desc">{{ banner.desc }}</text>
							<text class="banner-btn">{{ banner.btnText }}</text>
						</view>
					</view>
				</swiper-item>
			</swiper>
			<!-- 轮播指示器 -->
			<view class="swiper-dots">
				<view class="swiper-dot" :class="{ active: swiperIndex === idx }" v-for="(banner, idx) in banners" :key="'dot-' + idx"></view>
			</view>

			<!-- ===== 快捷功能入口（2列 x 4行 = 8项） ===== -->
			<view class="quick-section">
				<view class="quick-grid">
						<view class="quick-item" @tap="goToPackages">
							<view class="quick-icon quick-icon-pkg"><text class="qi-char">检</text></view>
							<text class="quick-label">体检套餐</text>
						</view>
						<view class="quick-item" @tap="goToAppointments">
							<view class="quick-icon quick-icon-appt"><text class="qi-char">约</text></view>
							<text class="quick-label">我的预约</text>
						</view>
						<view class="quick-item" @tap="goToOrders">
							<view class="quick-icon quick-icon-order"><text class="qi-char">单</text></view>
							<text class="quick-label">订单管理</text>
						</view>
						<view class="quick-item" @tap="goToReports">
							<view class="quick-icon quick-icon-report"><text class="qi-char">报</text></view>
							<text class="quick-label">体检报告</text>
						</view>
						<view class="quick-item" @tap="goToGuide">
							<view class="quick-icon quick-icon-guide"><text class="qi-char">导</text></view>
							<text class="quick-label">体检导诊</text>
						</view>
						<view class="quick-item" @tap="goToConsult">
							<view class="quick-icon quick-icon-consult"><text class="qi-char">咨</text></view>
							<text class="quick-label">在线咨询</text>
						</view>
						<view class="quick-item" @tap="goToProfileList">
							<view class="quick-icon quick-icon-profile"><text class="qi-char">人</text></view>
							<text class="quick-label">就诊人管理</text>
						</view>
						<view class="quick-item" @tap="goToContact">
							<view class="quick-icon quick-icon-more"><text class="qi-char">服</text></view>
							<text class="quick-label">联系客服</text>
						</view>
					</view>
				</view>

			<!-- ===== 数据概览卡片（仅登录） ===== -->
			<view class="stats-section" v-if="isLoggedIn">
				<view class="stats-row">
					<view class="stat-item" @tap="goToAppointments">
						<text class="stat-num">{{ stats.appointmentCount }}</text>
						<text class="stat-label">预约</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item" @tap="goToOrders">
						<text class="stat-num">{{ stats.orderCount }}</text>
						<text class="stat-label">订单</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item" @tap="goToReports">
						<text class="stat-num">{{ stats.reportCount }}</text>
						<text class="stat-label">报告</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item" @tap="goToNotifications">
						<text class="stat-num">{{ stats.unreadCount }}</text>
						<text class="stat-label">消息</text>
					</view>
				</view>
			</view>

			<!-- ===== 近期预约提醒（仅登录 + 有近日预约） ===== -->
			<view class="upcoming-section" v-if="isLoggedIn && upcomingAppt">
				<view class="section-header">
					<text class="section-title">最近预约</text>
					<text class="section-more" @tap="goToAppointments">全部 ›</text>
				</view>
				<view class="upcoming-card" @tap="showAppointment(upcomingAppt)">
					<view class="upcoming-left">
						<text class="upcoming-day">{{ dayOfMonth(upcomingAppt.appointmentDate) }}</text>
						<text class="upcoming-month">{{ monthOfYear(upcomingAppt.appointmentDate) }}</text>
					</view>
					<view class="upcoming-body">
						<view class="upcoming-top">
							<text class="upcoming-name ellipsis">{{ packageName(upcomingAppt.packageId) }}</text>
							<text class="upcoming-status" :class="'st-' + upcomingAppt.status">{{ statusLabel(upcomingAppt.status) }}</text>
						</view>
						<text class="upcoming-branch">{{ branchName(upcomingAppt.branchId) }}</text>
						<view class="upcoming-time-row">
							<text class="upcoming-time-icon">·</text>
							<text class="upcoming-time">{{ upcomingAppt.appointmentDate }} {{ upcomingAppt.timeSlot }}</text>
						</view>
					</view>
					<text class="upcoming-arrow">›</text>
				</view>
			</view>

			<!-- ===== 热门套餐推荐 ===== -->
			<view class="hot-section">
				<view class="section-header">
					<text class="section-title">热门套餐</text>
					<text class="section-more" @tap="goToPackages">全部 ›</text>
				</view>
				<scroll-view class="hot-scroll" scroll-x show-scrollbar="false">
					<view class="hot-card" v-for="pkg in hotPackages" :key="pkg.id" @tap="showPackageDetail(pkg)">
						<view class="hot-card-cover" :style="{ background: pkg.coverBg || 'linear-gradient(135deg, #7c9fff, #a0b8ff)' }">
							<text class="hot-card-badge">推荐</text>
						</view>
						<view class="hot-card-body">
							<text class="hot-card-name">{{ pkg.packageName }}</text>
							<view class="hot-card-tags">
								<text class="hot-tag">{{ pkg.categoryLabel }}</text>
								<text class="hot-tag" v-if="pkg.sales">已售 {{ pkg.sales }}</text>
							</view>
							<text class="hot-card-desc ellipsis">{{ pkg.features }}</text>
							<view class="hot-card-footer">
								<text class="hot-price">
									<text class="price-symbol">¥</text>
									<text class="price-val">{{ formatPrice(pkg.priceCent) }}</text>
								</text>
								<text class="hot-book-btn">立即预约</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>

			<!-- ===== 未登录时注册引导 ===== -->
			<view class="login-guide" v-if="!isLoggedIn">
				<text class="login-guide-text">登录后查看预约、报告等信息</text>
				<button class="login-guide-btn" @tap="goToLogin">登录 / 注册</button>
			</view>

			<view class="bottom-safe"></view>
		</scroll-view>
	</view>
</template>

<script>
import { tokenManager, appointmentApi, orderApi, notificationApi, packageApi, reportApi } from '../../../common/api.js'

export default {
	data() {
		return {
			isLoggedIn: false,
			swiperIndex: 0,
			stats: { appointmentCount: 0, orderCount: 0, reportCount: 0, unreadCount: 0 },
			upcomingAppt: null,
			packages: [],
			branches: [],
			hotPackages: [],
			banners: [
				{ title: '春季体检特惠', desc: '全场套餐 8 折起，守护家人健康', btnText: '立即查看', bg: 'linear-gradient(135deg, #7c9fff, #a0b8ff)', action: 'goPackages' },
				{ title: '入职体检专场', desc: '快速出报告，当天可预约', btnText: '了解详情', bg: 'linear-gradient(135deg, #7cb8a0, #a0d0b8)', action: 'goPackages' },
				{ title: '关爱父母体检', desc: '中老年深度筛查套餐', btnText: '查看套餐', bg: 'linear-gradient(135deg, #9a8cff, #b8a8ff)', action: 'goPackages' },
			]
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.checkLogin()
		if (this.isLoggedIn) this.loadData()
		this.loadPackages()
	},
	methods: {
		checkLogin() {
			this.isLoggedIn = tokenManager.isLoggedIn()
		},
		async loadData() {
			const userId = uni.getStorageSync('userId')
			if (!userId) return
			try {
				const [apps, orders, notifs, reports] = await Promise.all([
					appointmentApi.getByUser(userId).catch(() => []),
					orderApi.getByUser(userId).catch(() => []),
					notificationApi.getByUser(userId).catch(() => []),
					reportApi.getByUser(userId).catch(() => [])
				])
				this.stats.appointmentCount = (apps || []).length
				this.stats.orderCount = (orders || []).length
				this.stats.reportCount = (reports || []).length
				this.stats.unreadCount = (notifs || []).filter(n => !n.readFlag).length

				const future = (apps || []).find(a => a.status === 'CREATED' || a.status === 'PAID' || a.status === 'CHECKED_IN')
				this.upcomingAppt = future || null
			} catch (e) { console.error('首页加载数据失败', e) }
		},
		async loadPackages() {
			try {
				const [pkgs, brs] = await Promise.all([
					packageApi.getPackages().catch(() => []),
					packageApi.getBranches().catch(() => [])
				])
				this.packages = pkgs || []
				this.branches = brs || []

				// 生成热门套餐（丰富展示数据）
				const catMap = { BASIC: '基础体检', FULL: '全面体检', SENIOR: '老年体检', DEEP: '深度体检' }
				const featuresMap = [
					'血常规、尿常规、内外科、心电图',
					'含CT、彩超、肿瘤筛查等20+项目',
					'基础项目全覆盖，高性价比之选',
					'含心脏彩超、无痛胃肠镜等深度项目'
				]
				const bgMap = [
					'linear-gradient(135deg, #7c9fff, #a0b8ff)',
					'linear-gradient(135deg, #9a8cff, #b8a8ff)',
					'linear-gradient(135deg, #7cb8a0, #a0d0b8)',
					'linear-gradient(135deg, #d4a85c, #e0c080)'
				]
				this.hotPackages = (pkgs || []).map((p, i) => ({
					...p,
					categoryLabel: catMap[p.category] || p.category || '其他',
					features: featuresMap[i % featuresMap.length],
					coverBg: bgMap[i % bgMap.length]
				}))
			} catch (e) { console.error('加载套餐失败', e) }
		},

		// 格式化
		bannerBg(b) { return b ? { background: b.bg } : {} },
		formatPrice(cent) { return cent == null ? '0.00' : (cent / 100).toFixed(2) },
		dayOfMonth(dateStr) {
			if (!dateStr) return ''
			const parts = dateStr.split('-')
			return parts.length === 3 ? parseInt(parts[2]) + '日' : ''
		},
		monthOfYear(dateStr) {
			if (!dateStr) return ''
			const parts = dateStr.split('-')
			return parts.length === 3 ? parseInt(parts[1]) + '月' : ''
		},
		packageName(id) { const p = this.packages.find(x => x.id === id); return p ? p.packageName : '未知套餐' },
		branchName(id) { const b = this.branches.find(x => x.id === id); return b ? b.branchName : '未知分院' },
		statusLabel(s) { return ({ CREATED: '待支付', PAID: '已支付', CHECKED_IN: '已签到', CANCELLED: '已取消', COMPLETED: '已完成' })[s] || s },

		// 导航
		showAppointment(item) { uni.setStorageSync('selectedAppointment', item); uni.navigateTo({ url: '/pages/appointment/detail' }) },
		showPackageDetail(pkg) { uni.setStorageSync('selectedPackage', pkg); uni.navigateTo({ url: '/pages/packages/detail' }) },
		goToLogin() { uni.navigateTo({ url: '/pages/login/login' }) },
		goToNotifications() { uni.navigateTo({ url: '/pages/notification/list' }) },
		goToAppointments() { uni.switchTab({ url: '/pages/appointment/list' }) },
		goToPackages() { uni.switchTab({ url: '/pages/packages/packages' }) },
		goToOrders() { uni.navigateTo({ url: '/pages/order/list' }) },
		goToReports() { uni.navigateTo({ url: '/pages/report/list' }) },
		goToProfileList() { uni.navigateTo({ url: '/pages/profile/list' }) },
		goToConsult() { uni.navigateTo({ url: '/pages/consult/index' }) },
		goToContact() { uni.navigateTo({ url: "/pages/contact/index" }) },
		goToGuide() { uni.navigateTo({ url: '/pages/guide/index' }) },
		onSwiperChange(e) { this.swiperIndex = e.detail.current },
		bannerTap(action) {
			if (action === 'goPackages') { uni.switchTab({ url: '/pages/packages/packages' }) }
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
	justify-content: space-between;
	padding: 18rpx 28rpx 8rpx;
	padding-top: calc(var(--status-bar-height, 44px) + 24rpx);
	background: var(--surface-page, #f8fafc);
}
.nav-logo {
	font-size: 40rpx;
	font-weight: 700;
	color: var(--brand);
	letter-spacing: 1rpx;
}
.nav-right {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 16rpx;
}

/* ===== 内容滚动 ===== */
.scroll-wrap { flex: 1; padding-bottom: 20rpx; }

/* ===== Banner 轮播 ===== */
.banner-swiper {
	height: 260rpx;
	margin: 8rpx 24rpx 16rpx;
	border-radius: var(--radius-card, 24rpx);
	overflow: hidden;
}
.banner-card {
	height: 100%;
	border-radius: var(--radius-card, 24rpx);
	padding: 36rpx 32rpx;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	overflow: hidden;
}
.banner-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}
.banner-title { font-size: 34rpx; font-weight: 700; color: #ffffff; }
.banner-desc { font-size: 24rpx; color: rgba(255,255,255,0.85); }
.banner-btn {
	display: inline-block;
	margin-top: 12rpx;
	padding: 10rpx 24rpx;
	border-radius: 9999px;
	background: rgba(255,255,255,0.25);
	color: #ffffff;
	font-size: 22rpx;
	font-weight: 500;
	width: fit-content;
}

/* ===== 轮播指示器 ===== */
.swiper-dots {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
	gap: 12rpx;
	margin: -32rpx 0 20rpx;
	position: relative;
	z-index: 2;
}
.swiper-dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	background: rgba(255,255,255,0.5);
	transition: all 0.3s ease;
}
.swiper-dot.active {
	width: 28rpx;
	border-radius: 7rpx;
	background: #ffffff;
}

/* ===== 快捷功能 ===== */
.quick-section {
	margin: 4rpx 24rpx 16rpx;
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	padding: 12rpx 8rpx;
	box-shadow: var(--shadow-sm);
}
.quick-grid {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
}
.quick-item {
	width: 25%;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20rpx 0;
}
.quick-item:active { opacity: 0.6; }
.quick-icon {
	width: 56rpx;
	height: 56rpx;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 12rpx;
}
.qi-char { font-size: 28rpx; font-weight: 600; color: #ffffff; }
.quick-icon-pkg { background: #7c9fff; }
.quick-icon-appt { background: #7cb8a0; }
.quick-icon-order { background: #d4a85c; }
.quick-icon-report { background: #9a8cff; }
.quick-icon-guide { background: #8cb8d4; }
.quick-icon-consult { background: #d48c9c; }
.quick-icon-profile { background: #7c9fff; }
.quick-icon-more { background: #d4a85c; }
.quick-label { font-size: 22rpx; font-weight: 450; color: var(--text, #1e293b); }

/* ===== 数据概览 ===== */
.stats-section {
	margin: 4rpx 24rpx 16rpx;
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	padding: 20rpx 8rpx;
	box-shadow: var(--shadow-sm);
}
.stats-row {
	display: flex;
	flex-direction: row;
	align-items: center;
}
.stat-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 8rpx 0;
}
.stat-item:active { opacity: 0.6; }
.stat-num { font-size: 36rpx; font-weight: 700; color: var(--text, #1e293b); }
.stat-label { font-size: 22rpx; color: var(--text-secondary, #64748b); margin-top: 4rpx; }
.stat-divider { width: 1px; height: 44rpx; background: var(--border-light, #f1f5f9); }

/* ===== 区块标题 ===== */
.section-header {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	margin: 0 24rpx 12rpx;
}
.section-title { font-size: 30rpx; font-weight: 600; color: var(--text, #1e293b); }
.section-more { font-size: 24rpx; color: var(--text-secondary, #64748b); padding: 8rpx; }
.section-more:active { opacity: 0.6; }

/* ===== 最近预约卡片 ===== */
.upcoming-section { margin-bottom: 8rpx; }
.upcoming-card {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin: 0 24rpx;
	padding: 24rpx 24rpx;
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	box-shadow: var(--shadow-sm);
}
.upcoming-card:active { opacity: 0.8; }
.upcoming-left {
	width: 88rpx;
	height: 96rpx;
	background: var(--brand-light, #e0f2fe);
	border-radius: 16rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	margin-right: 20rpx;
	flex-shrink: 0;
}
.upcoming-day { font-size: 32rpx; font-weight: 700; color: var(--brand); line-height: 1.2; }
.upcoming-month { font-size: 20rpx; color: var(--brand-dark, #0284c7); margin-top: 2rpx; }
.upcoming-body { flex: 1; min-width: 0; }
.upcoming-top { display: flex; flex-direction: row; align-items: center; margin-bottom: 6rpx; }
.upcoming-name { font-size: 28rpx; font-weight: 600; color: var(--text, #1e293b); flex: 1; margin-right: 12rpx; }
.upcoming-status { font-size: 20rpx; font-weight: 500; padding: 4rpx 14rpx; border-radius: 9999px; flex-shrink: 0; }
.st-CREATED { color: #d97706; background: var(--warning-bg, #fef3c7); }
.st-PAID { color: var(--brand-dark, #0284c7); background: var(--brand-light, #e0f2fe); }
.st-CHECKED_IN { color: var(--secondary, #0d9488); background: var(--secondary-light, #ccfbf1); }
.st-CANCELLED { color: var(--text-disabled, #cbd5e1); background: var(--surface-page, #f8fafc); }
.st-COMPLETED { color: var(--text-secondary, #64748b); background: var(--surface-hover, #f1f5f9); }
.upcoming-branch { font-size: 24rpx; color: var(--text-secondary, #64748b); display: block; margin-bottom: 4rpx; }
.upcoming-time-row { display: flex; flex-direction: row; align-items: center; }
.upcoming-time-icon { font-size: 22rpx; margin-right: 6rpx; }
.upcoming-time { font-size: 24rpx; color: var(--text-placeholder, #94a3b8); }
.upcoming-arrow { font-size: 32rpx; color: var(--text-disabled, #cbd5e1); margin-left: 12rpx; flex-shrink: 0; }

/* ===== 热门套餐 ===== */
.hot-section { margin: 8rpx 0 16rpx; }
.hot-scroll {
	display: flex;
	flex-direction: row;
	padding: 0 16rpx;
	white-space: nowrap;
}
.hot-card {
	display: inline-block;
	width: 420rpx;
	margin: 0 8rpx;
	background: var(--surface, #ffffff);
	border-radius: var(--radius-card, 24rpx);
	overflow: hidden;
	box-shadow: var(--shadow-sm);
	vertical-align: top;
}
.hot-card:active { opacity: 0.85; }
.hot-card-cover { height: 120rpx; display: flex; align-items: center; justify-content: center; }
.hot-card-badge { font-size: 22rpx; color: #ffffff; background: rgba(255,255,255,0.25); padding: 6rpx 20rpx; border-radius: 9999px; }
.hot-card-body { padding: 20rpx; white-space: normal; }
.hot-card-name { font-size: 28rpx; font-weight: 600; color: var(--text, #1e293b); display: block; margin-bottom: 10rpx; }
.hot-card-tags { display: flex; flex-direction: row; gap: 8rpx; margin-bottom: 10rpx; }
.hot-tag {
	font-size: 20rpx;
	color: var(--brand);
	background: var(--brand-light, #e0f2fe);
	padding: 4rpx 14rpx;
	border-radius: 9999px;
}
.hot-card-desc { font-size: 22rpx; color: var(--text-secondary, #64748b); display: block; margin-bottom: 14rpx; }
.hot-card-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}
.hot-price { display: flex; flex-direction: row; align-items: baseline; }
.price-symbol { font-size: 22rpx; font-weight: 500; color: var(--danger, #ef4444); }
.price-val { font-size: 30rpx; font-weight: 700; color: var(--danger, #ef4444); }
.hot-book-btn {
	font-size: 22rpx;
	font-weight: 500;
	color: var(--text-white, #ffffff);
	background: var(--brand);
	padding: 12rpx 24rpx;
	border-radius: 9999px;
}

/* ===== 登录引导 ===== */
.login-guide {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx 24rpx 20rpx;
	gap: 16rpx;
}
.login-guide-text { font-size: 26rpx; color: #000000; }
.login-guide-btn {
	width: 300rpx;
	height: 80rpx;
	line-height: 80rpx;
	background: var(--brand);
	border-radius: 9999px;
	font-size: 28rpx;
	font-weight: 500;
	color: #000000;
	text-align: center;
}
.login-guide-btn:active { opacity: 0.85; }

.bottom-safe { height: 40rpx; }
</style>
