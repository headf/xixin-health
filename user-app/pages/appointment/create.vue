<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">填写预约</text>
			<view class="header-spacer"></view>
		</view>

		<view class="content" v-if="loaded">
			<!-- 套餐摘要卡片 -->
			<view class="summary-card" v-if="pkg">
				<text class="summary-label">已选套餐</text>
				<view class="summary-row">
					<text class="summary-name">{{ pkg.packageName }}</text>
					<text class="summary-price">¥{{ formatPrice(pkg.priceCent) }}</text>
				</view>
			</view>

			<!-- 表单卡片 -->
			<view class="form-card">
				<!-- 就诊人 -->
				<view class="form-group">
					<text class="form-label">就诊人</text>
					<view class="picker-box" @tap="showProfilePicker">
						<text class="picker-text" v-if="selectedProfile">{{ selectedProfile.name }}</text>
						<text class="picker-placeholder" v-else>请选择就诊人</text>
						<text class="picker-arrow">›</text>
					</view>
				</view>

				<!-- 分院 -->
				<view class="form-group">
					<text class="form-label">体检分院</text>
					<view class="picker-box" @tap="showBranchPicker">
						<text class="picker-text" v-if="selectedBranch">{{ selectedBranch.branchName }}</text>
						<text class="picker-placeholder" v-else>请选择分院</text>
						<text class="picker-arrow">›</text>
					</view>
				</view>

				<!-- 预约日期 -->
				<view class="form-group">
					<text class="form-label">预约日期</text>
					<picker class="picker-box" mode="date" :value="appointmentDate" :start="minDate" @change="onDateChange">
						<text class="picker-text" v-if="appointmentDate">{{ appointmentDate }}</text>
						<text class="picker-placeholder" v-else>请选择日期</text>
						<text class="picker-arrow">›</text>
					</picker>
				</view>

				<!-- 时段 + 号源 -->
				<view class="form-group">
					<text class="form-label">体检时段 <text v-if="slotsLoading" style="font-size:11px;color:#999;"> 查询中...</text></text>
					<view class="slot-group">
						<view class="slot-option" :class="{ active: timeSlot === '上午', disabled: slotInfo?.morning?.remainingSlots <= 0 && slotInfo }" @tap="selectSlot('上午')">
							<view class="slot-inner"><text>上午</text><text v-if="slotInfo?.morning != null" class="slot-badge" :class="{ full: slotInfo.morning.remainingSlots <= 0 }">余{{ slotInfo.morning.remainingSlots }}号</text><text v-else class="slot-badge slot-unknown">查询中</text></view>
						</view>
						<view class="slot-option" :class="{ active: timeSlot === '下午', disabled: slotInfo?.afternoon?.remainingSlots <= 0 && slotInfo }" @tap="selectSlot('下午')">
							<view class="slot-inner"><text>下午</text><text v-if="slotInfo?.afternoon != null" class="slot-badge" :class="{ full: slotInfo.afternoon.remainingSlots <= 0 }">余{{ slotInfo.afternoon.remainingSlots }}号</text><text v-else class="slot-badge slot-unknown">查询中</text></view>
						</view>
					</view>
				</view>
			</view>

			<!-- 错误提示 -->
			<text class="form-error" v-if="errorMsg">{{ errorMsg }}</text>
		</view>

		<!-- 加载状态 -->
		<view class="loading-state" v-if="!loaded">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 底部提交按钮 -->
		<view class="bottom-bar" v-if="loaded">
			<button class="submit-btn" type="default" :loading="submitting" :disabled="submitting" @tap="handleSubmit">
				{{ submitting ? '提交中...' : '提交预约' }}
			</button>
		</view>

		<!-- 就诊人选择弹出层 -->
		<view class="overlay" v-if="showProfile" @tap="showProfile = false">
			<view class="popup" @tap.stop>
				<view class="popup-header">
					<text class="popup-title">选择就诊人</text>
					<text class="popup-close" @tap="showProfile = false">完成</text>
				</view>
				<scroll-view class="popup-list" scroll-y>
					<view
						class="popup-item"
						v-for="item in profiles"
						:key="item.id"
						:class="{ active: selectedProfile && selectedProfile.id === item.id }"
						@tap="selectProfile(item)"
					>
						<text class="popup-item-name">{{ item.name }}</text>
						<text class="popup-item-type">{{ item.type === 'SELF' ? '本人' : '家人' }}</text>
					</view>
					<view class="popup-empty" v-if="profiles.length === 0">
						<text class="popup-empty-text">暂无就诊人档案</text>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 分院选择弹出层 -->
		<view class="overlay" v-if="showBranch" @tap="showBranch = false">
			<view class="popup" @tap.stop>
				<view class="popup-header">
					<text class="popup-title">选择分院</text>
					<text class="popup-close" @tap="showBranch = false">完成</text>
				</view>
				<scroll-view class="popup-list" scroll-y>
					<view
						class="popup-item"
						v-for="item in branches"
						:key="item.id"
						:class="{ active: selectedBranch && selectedBranch.id === item.id }"
						@tap="selectBranch(item)"
					>
						<view class="popup-item-col">
							<text class="popup-item-name">{{ item.branchName }}</text>
							<text class="popup-item-addr">{{ item.address }}</text>
						</view>
					</view>
					<view class="popup-empty" v-if="branches.length === 0">
						<text class="popup-empty-text">暂无分院数据</text>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script>
import { packageApi, profileApi, appointmentApi, tokenManager, slotApi } from '../../common/api.js'

function parseJwt(token) {
	try {
		const parts = token.split('.')
		if (parts.length !== 3) return null
		const payload = parts[1]
		const decoded = decodeURIComponent(atob(payload).split('').map(c =>
			'%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
		).join(''))
		return JSON.parse(decoded)
	} catch (e) {
		return null
	}
}

export default {
	data() {
		return {
			loaded: false,
			submitting: false,
			pkg: null,
			userId: null,

			// 表单数据
			selectedProfile: null,
			selectedBranch: null,
			appointmentDate: '',
			timeSlot: '上午',
			errorMsg: '',

			// 列表数据
			profiles: [],
			branches: [],

			// 弹出层
			showProfile: false,
			showBranch: false,

			// 日期限制
			minDate: ''
		}
	},

	onShow() {
		uni.setNavigationBarColor({
			frontColor: '#000000',
			backgroundColor: '#ffffff'
		})
		this.init()
	},

	methods: {
		init() {
			// 获取用户 ID
			const token = tokenManager.getToken()
			const claims = parseJwt(token)
			if (claims && claims.sub != null) {
				this.userId = Number(claims.sub)
			} else {
				// 兜底：从 storage 读取
				this.userId = uni.getStorageSync('userId') || null
			}

			// 获取预约套餐
			this.pkg = uni.getStorageSync('selectedPackage') || null

			// 设置最小日期（明天）
			const today = new Date()
			today.setHours(0, 0, 0, 0)
			this.minDate = today.toISOString().split('T')[0]
			this.appointmentDate = this.minDate

			// 加载数据
			this.loadData()
		},

		async loadData() {
			try {
				const [profiles, branches] = await Promise.all([
					this.userId ? profileApi.getProfiles(this.userId) : Promise.resolve([]),
					packageApi.getBranches()
				])
				this.profiles = profiles || []
				this.branches = branches || []
			} catch (err) {
				console.error('加载数据失败', err)
			} finally {
				this.loaded = true
				this.querySlots()
			}
		},

		goBack() {
			uni.navigateBack()
		},

		formatPrice(cent) {
			if (cent == null) return '0.00'
			return (cent / 100).toFixed(2)
		},

		onDateChange(e) {
			this.appointmentDate = e.detail.value
			this.querySlots()
		},

		// 就诊人选择
		showProfilePicker() {
			this.showProfile = true
		},
		selectProfile(item) {
			this.selectedProfile = item
			this.showProfile = false
		},

		// 分院选择
		showBranchPicker() {
			this.showBranch = true
		},
		selectBranch(item) {
			this.selectedBranch = item
			this.showBranch = false
			this.querySlots()
		},

		selectSlot(slot) {
			if (slot === '上午') {
				const remain = this.slotInfo?.morning?.remainingSlots
				if (remain == null || remain > 0) this.timeSlot = '上午'
			} else {
				const remain = this.slotInfo?.afternoon?.remainingSlots
				if (remain == null || remain > 0) this.timeSlot = '下午'
			}
		},

		// 查询号源
		async querySlots() {
			if (!this.selectedBranch || !this.appointmentDate) return
			this.slotsLoading = true
			try {
				console.log('查询号源:', this.selectedBranch.id, this.appointmentDate)
				const slots = await slotApi.getAvailable(this.selectedBranch.id, this.appointmentDate)
				console.log('号源返回:', JSON.stringify(slots))
				if (slots && slots.length > 0) {
					this.slotInfo = {}
					for (const s of slots) {
						if (s.slotCode === 'MORNING') this.slotInfo.morning = s
						if (s.slotCode === 'AFTERNOON') this.slotInfo.afternoon = s
					}
					const am = this.slotInfo.morning?.remainingSlots || 0
					const pm = this.slotInfo.afternoon?.remainingSlots || 0
					if (this.timeSlot === '上午' && am <= 0 && pm > 0) this.timeSlot = '下午'
					else if (this.timeSlot === '下午' && pm <= 0 && am > 0) this.timeSlot = '上午'
				} else {
					this.slotInfo = null
				}
			} catch (e) {
				console.error('号源查询失败:', e)
				this.slotInfo = null
			} finally {
				this.slotsLoading = false
			}
		},

		// 提交预约
		async handleSubmit() {
			this.errorMsg = ''

			if (!this.selectedProfile) {
				this.errorMsg = '请选择就诊人'
				return
			}
			if (!this.selectedBranch) {
				this.errorMsg = '请选择体检分院'
				return
			}
			if (!this.appointmentDate) {
				this.errorMsg = '请选择预约日期'
				return
			}
			if (!this.userId) {
				this.errorMsg = '用户信息缺失，请重新登录'
				return
			}
			if (!this.pkg) {
				this.errorMsg = '套餐信息缺失'
				return
			}

			this.submitting = true
			console.log('提交预约:', JSON.stringify({
				userId: this.userId,
				profileId: this.selectedProfile?.id,
				packageId: this.pkg?.id,
				branchId: this.selectedBranch?.id,
				appointmentDate: this.appointmentDate,
				timeSlot: this.timeSlot
			}))
			try {
				const res = await appointmentApi.create({
					userId: this.userId,
					profileId: this.selectedProfile.id,
					packageId: this.pkg.id,
					branchId: this.selectedBranch.id,
					appointmentDate: this.appointmentDate,
					timeSlot: this.timeSlot
				})

				uni.showToast({
					title: '预约成功',
					icon: 'success',
					duration: 1000
				})

				// 跳转支付页
				setTimeout(() => {
					const amountCent = this.pkg?.priceCent || 0
					uni.redirectTo({
						url: '/pages/order/payment?appointmentId=' + (res?.id || '') +
							'&amountCent=' + amountCent
					})
				}, 1000)

			} catch (err) {
				console.error('预约失败:', JSON.stringify(err))
				this.errorMsg = err.message || '预约失败，请重试'
			} finally {
				this.submitting = false
			}
		}
	}
}
</script>

<style>
/* ===== Steep Design — 预约填写 ===== */

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
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	letter-spacing: -0.25rpx;
	padding: 12rpx 8rpx;
}

.back-btn:active {
	opacity: 0.6;
}

.header-title {
	font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif;
	font-size: 40rpx;
	font-weight: 400;
	color: #17191c;
	letter-spacing: -0.3rpx;
	line-height: 1.1;
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
	white-space: nowrap;
}

.header-spacer {
	width: 80rpx;
}

/* 加载状态 */
.loading-state {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

.loading-text {
	font-size: 28rpx;
	color: #777b86;
}

/* 内容 */
.content {
	flex: 1;
	padding: 24rpx 32rpx 160rpx;
}

/* 套餐摘要 */
.summary-card {
	background: #ffffff;
	border-radius: 48rpx;
	padding: 28rpx 32rpx;
	margin-bottom: 24rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

.summary-label {
	font-size: 24rpx;
	font-weight: 430;
	color: #777b86;
	margin-bottom: 12rpx;
	display: block;
}

.summary-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.summary-name {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 32rpx;
	font-weight: 500;
	color: #17191c;
	letter-spacing: -0.28rpx;
	flex: 1;
	margin-right: 16rpx;
}

.summary-price {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx;
	font-weight: 480;
	color: #17191c;
}

/* 表单卡片 */
.form-card {
	background: #ffffff;
	border-radius: 48rpx;
	padding: 32rpx 32rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx;
}

.form-group {
	margin-bottom: 32rpx;
}

.form-group:last-child {
	margin-bottom: 0;
}

.form-label {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 26rpx;
	font-weight: 500;
	color: #17191c;
	display: block;
	margin-bottom: 12rpx;
}

/* 选择器 */
.picker-box {
	display: flex;
	flex-direction: row;
	align-items: center;
	background: #f7f7f8;
	border-radius: 24rpx;
	padding: 0 24rpx;
	height: 88rpx;
}

.picker-text {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	flex: 1;
}

.picker-placeholder {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx;
	font-weight: 430;
	color: #a3a6af;
	flex: 1;
}

.picker-arrow {
	font-size: 36rpx;
	color: #a3a6af;
	margin-left: 8rpx;
}

/* 号源标记 */
.slot-inner { display:flex; align-items:center; gap:4px; }
.slot-badge { display:inline-block; font-size:9px; color:#0ea5e9; padding:0 6px; border-radius:9999px; background:#e0f2fe; }
.slot-badge.full { color:#ef4444; background:#fef2f2; }
.slot-badge.slot-unknown { color:#94a3b8; background:#f1f5f9; }
.slot-option.disabled { opacity:0.4; }
/* 时段选择 */
.slot-group {
	display: flex;
	flex-direction: row;
	gap: 16rpx;
}

.slot-option {
	flex: 1;
	height: 88rpx;
	line-height: 88rpx;
	text-align: center;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx;
	font-weight: 430;
	color: #777b86;
	background: #f7f7f8;
	border-radius: 24rpx;
	border: 2rpx solid transparent;
}

.slot-option.active {
	color: #17191c;
	background: #ffffff;
	border-color: #17191c;
	font-weight: 450;
}

/* 错误提示 */
.form-error {
	font-size: 24rpx;
	color: #5d2a1a;
	display: block;
	margin-top: 20rpx;
	padding: 0 8rpx;
}

/* 底部按钮 */
.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 24rpx 32rpx 40rpx;
	background: linear-gradient(to top, #ffffff 60%, rgba(255, 255, 255, 0));
	pointer-events: none;
}

.bottom-bar .submit-btn {
	pointer-events: auto;
}

.submit-btn {
	height: 96rpx;
	line-height: 96rpx;
	background: #17191c;
	border-radius: 9999px;
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx;
	font-weight: 450;
	color: #ffffff;
	letter-spacing: -0.27rpx;
	border: none;
	box-shadow: none;
	margin: 0;
}

.submit-btn:active {
	opacity: 0.85;
	transform: scale(0.97);
}

.submit-btn[disabled] {
	opacity: 0.5;
}

/* 弹出层 */
.overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(23, 25, 28, 0.4);
	z-index: 100;
	display: flex;
	align-items: flex-end;
}

.popup {
	width: 100%;
	background: #ffffff;
	border-radius: 48rpx 48rpx 0 0;
	max-height: 60vh;
	display: flex;
	flex-direction: column;
}

.popup-header {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx 32rpx 16rpx;
	border-bottom: 1px solid #f7f7f8;
}

.popup-title {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx;
	font-weight: 500;
	color: #17191c;
}

.popup-close {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 28rpx;
	font-weight: 450;
	color: #17191c;
	padding: 8rpx 16rpx;
}

.popup-close:active {
	opacity: 0.6;
}

.popup-list {
	padding: 8rpx 0;
	max-height: 50vh;
}

.popup-item {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 28rpx 32rpx;
	border-bottom: 1px solid #f7f7f8;
}

.popup-item:active {
	background: #f7f7f8;
}

.popup-item.active {
	background: #f7f7f8;
}

.popup-item-col {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.popup-item-name {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 30rpx;
	font-weight: 450;
	color: #17191c;
}

.popup-item-type {
	font-size: 24rpx;
	color: #777b86;
}

.popup-item-addr {
	font-size: 24rpx;
	color: #777b86;
	line-height: 1.4;
}

.popup-empty {
	padding: 48rpx 32rpx;
	text-align: center;
}

.popup-empty-text {
	font-size: 26rpx;
	color: #a3a6af;
}
</style>
