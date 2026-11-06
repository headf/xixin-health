<template>
	<view class="pkg-card" :class="{ 'pkg-card--compact': compact }" @tap="handleTap" hover-class="pkg-card--hover">
		<!-- Cover Image -->
		<view class="pkg-card__cover" v-if="!compact">
			<image class="pkg-card__cover-img" :src="pkg.coverImage || '/static/banner_health.png'" mode="aspectFill" />
			<text class="pkg-card__category-tag">{{ categoryLabel(pkg.category) }}</text>
		</view>

		<!-- Body -->
		<view class="pkg-card__body">
			<text class="pkg-card__name">{{ pkg.packageName }}</text>

			<!-- Core items summary (compact: hide, full: show) -->
			<view class="pkg-card__items" v-if="!compact && pkg.coreItems && pkg.coreItems.length > 0">
				<text class="pkg-card__items-label">包含项目</text>
				<text class="pkg-card__items-text">{{ coreItemsText }}</text>
			</view>

			<!-- Tags row -->
			<view class="pkg-card__tags" v-if="!compact">
				<text class="pkg-card__tag">{{ genderLabel(pkg.genderLimit) }}</text>
				<text class="pkg-card__tag">{{ ageLabel(pkg.ageRange) }}</text>
			</view>

			<!-- Footer: price + button -->
			<view class="pkg-card__footer">
				<text class="pkg-card__price">
					<text class="pkg-card__price-symbol">¥</text>
					<text class="pkg-card__price-value">{{ formatPrice(pkg.priceCent) }}</text>
				</text>
				<button class="pkg-card__btn" @tap.stop="handleBook">立即预约</button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'PackageCard',
	props: {
		pkg: { type: Object, required: true },
		compact: { type: Boolean, default: false }
	},
	computed: {
		coreItemsText() {
			if (!this.pkg.coreItems) return ''
			return this.pkg.coreItems.join('、')
		}
	},
	methods: {
		handleTap() {
			this.$emit('tap', this.pkg)
		},
		handleBook() {
			this.$emit('book', this.pkg)
		},
		formatPrice(cent) {
			return cent == null ? '0.00' : (cent / 100).toFixed(2)
		},
		categoryLabel(cat) {
			const map = { BASIC: '基础体检', FULL: '全面体检', SENIOR: '老年体检', GYN: '妇科体检', CARDIO: '心血管专项', DIABETES: '糖尿病筛查', LIVER: '肝脏健康', KIDNEY: '肾脏健康', STUDENT: '学生体检', PREMIUM: '至尊体检' }
			return map[cat] || cat || '体检'
		},
		genderLabel(g) { return ({ ALL: '不限性别', MALE: '男', FEMALE: '女' })[g] || g || '不限' },
		ageLabel(r) { return (r == null || r === '' || r === 'null') ? '不限年龄' : r + '岁' }
	}
}
</script>

<style>
.pkg-card {
	background: #ffffff;
	border-radius: 32rpx;
	overflow: hidden;
	box-shadow: var(--shadow-card-sm, 0 4rpx 12rpx rgba(44,123,229,0.08));
	transition: all 0.25s ease;
}
.pkg-card--hover {
	opacity: 0.95;
	transform: scale(0.98);
}

/* Compact variant (horizontal scroll card) */
.pkg-card--compact {
	width: 380rpx;
	flex-shrink: 0;
	border-radius: 24rpx;
}
.pkg-card--compact .pkg-card__body {
	padding: 20rpx 16rpx 16rpx;
}
.pkg-card--compact .pkg-card__name {
	font-size: 26rpx;
	margin-bottom: 12rpx;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	line-height: 1.3;
}
.pkg-card--compact .pkg-card__price-value {
	font-size: 30rpx;
}
.pkg-card--compact .pkg-card__btn {
	font-size: 22rpx;
	height: 56rpx;
	line-height: 56rpx;
	padding: 0 24rpx;
}

/* Cover image */
.pkg-card__cover {
	position: relative;
	width: 100%;
	height: 240rpx;
	overflow: hidden;
}
.pkg-card__cover-img {
	width: 100%;
	height: 100%;
}
.pkg-card__category-tag {
	position: absolute;
	top: 16rpx;
	left: 16rpx;
	font-size: 22rpx;
	font-weight: 450;
	color: #ffffff;
	background: rgba(44,123,229,0.85);
	padding: 4rpx 16rpx;
	border-radius: 9999px;
}


/* Body */
.pkg-card__body {
	padding: 28rpx 24rpx 24rpx;
}
.pkg-card__name {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 32rpx;
	font-weight: 500;
	color: #17191c;
	margin-bottom: 12rpx;
	display: block;
}
.pkg-card__items {
	margin-bottom: 12rpx;
}
.pkg-card__items-label {
	font-size: 22rpx;
	color: #777b86;
	margin-right: 8rpx;
}
.pkg-card__items-text {
	font-size: 22rpx;
	color: #777b86;
	line-height: 1.4;
}

/* Tags */
.pkg-card__tags {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	gap: 8rpx;
	margin-bottom: 12rpx;
}
.pkg-card__tag {
	font-size: 20rpx;
	font-weight: 430;
	color: #7B9FE0;
	background: #EDF2FB;
	padding: 4rpx 14rpx;
	border-radius: 9999px;
}

/* Meta info */

/* Footer */
.pkg-card__footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f4f8;
}
.pkg-card__price {
	display: flex;
	flex-direction: row;
	align-items: baseline;
}
.pkg-card__price-symbol {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 24rpx;
	font-weight: 450;
	color: #7B9FE0;
	margin-right: 2rpx;
}
.pkg-card__price-value {
	font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 36rpx;
	font-weight: 500;
	color: #7B9FE0;
	letter-spacing: -0.3rpx;
}
.pkg-card__btn {
	height: 64rpx;
	line-height: 64rpx;
	padding: 0 32rpx;
	background: linear-gradient(135deg, #7B9FE0, #6DB8AD);
	border-radius: 9999px;
	font-size: 26rpx;
	font-weight: 450;
	color: #ffffff;
	border: none;
	box-shadow: none;
	margin: 0;
}
.pkg-card__btn:active {
	opacity: 0.85;
	transform: scale(0.95);
}
</style>
