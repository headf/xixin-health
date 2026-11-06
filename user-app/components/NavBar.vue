<template>
	<view class="nav-bar" :class="{ 'nav-bar--transparent': transparent }" :style="navStyle">
		<view class="nav-bar__left">
			<view class="nav-bar__back" v-if="showBack" @tap="handleBack" hover-class="nav-bar__back--hover">
				<text class="nav-bar__back-icon">←</text>
				<text class="nav-bar__back-text" v-if="backText">{{ backText }}</text>
			</view>
		</view>
		<text class="nav-bar__title">{{ title }}</text>
		<view class="nav-bar__right">
			<slot name="right"></slot>
		</view>
	</view>
</template>

<script>
let sysInfo = null
function getStatusBarHeight() {
	if (!sysInfo) {
		try { sysInfo = uni.getSystemInfoSync() } catch (e) {}
	}
	return (sysInfo && sysInfo.statusBarHeight) || 44
}

export default {
	name: 'NavBar',
	props: {
		title: { type: String, default: '' },
		showBack: { type: Boolean, default: true },
		backText: { type: String, default: '' },
		transparent: { type: Boolean, default: false }
	},
	computed: {
		navStyle() {
			return { '--status-bar-height': getStatusBarHeight() + 'px' }
		}
	},
	methods: {
		handleBack() {
			this.$emit('back')
			uni.navigateBack()
		}
	}
}
</script>

<style>
.nav-bar {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 32rpx;
	padding-top: calc(var(--status-bar-height, 44px) + 16rpx);
	background: #ffffff;
	position: relative;
	z-index: 10;
}
.nav-bar--transparent {
	background: transparent;
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
}

.nav-bar__left {
	display: flex;
	align-items: center;
	min-width: 120rpx;
}
.nav-bar__back {
	display: flex;
	flex-direction: row;
	align-items: center;
	padding: 12rpx 8rpx;
	border-radius: 16rpx;
	transition: opacity 0.2s;
}
.nav-bar__back--hover {
	opacity: 0.5;
}
.nav-bar__back-icon {
	font-size: 32rpx;
	color: var(--color-ink, #17191c);
	margin-right: 6rpx;
	line-height: 1;
}
.nav-bar__back-text {
	font-family: var(--font-system, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif);
	font-size: 26rpx;
	font-weight: 450;
	color: var(--color-ink, #17191c);
}

.nav-bar__title {
	font-family: var(--font-sohne, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif);
	font-size: 36rpx;
	font-weight: 400;
	color: var(--color-ink, #17191c);
	letter-spacing: -0.3rpx;
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
	white-space: nowrap;
}

.nav-bar__right {
	display: flex;
	align-items: center;
	min-width: 120rpx;
	justify-content: flex-end;
}
</style>
