<template>
	<view class="empty-state">
		<text class="empty-state__icon">{{ icon }}</text>
		<text class="empty-state__title">{{ title }}</text>
		<text class="empty-state__desc" v-if="description">{{ description }}</text>
		<button class="empty-state__action" v-if="actionText" @tap="handleAction" hover-class="empty-state__action--hover">
			{{ actionText }}
		</button>
	</view>
</template>

<script>
export default {
	name: 'EmptyState',
	props: {
		icon: { type: String, default: '📋' },
		title: { type: String, default: '暂无数据' },
		description: { type: String, default: '' },
		actionText: { type: String, default: '' }
	},
	methods: {
		handleAction() {
			this.$emit('action')
		}
	}
}
</script>

<style>
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 48rpx;
}
.empty-state__icon {
	font-size: 72rpx;
	margin-bottom: 24rpx;
	opacity: 0.7;
}
.empty-state__title {
	font-family: var(--font-system, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif);
	font-size: 30rpx;
	font-weight: 500;
	color: var(--color-ink, #17191c);
	margin-bottom: 8rpx;
	text-align: center;
}
.empty-state__desc {
	font-size: 26rpx;
	color: var(--color-dove, #a3a6af);
	text-align: center;
	line-height: 1.5;
	margin-bottom: 32rpx;
}
.empty-state__action {
	height: 80rpx;
	line-height: 80rpx;
	padding: 0 48rpx;
	background: var(--color-ink, #17191c);
	border-radius: 9999px;
	font-family: var(--font-system, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif);
	font-size: 26rpx;
	font-weight: 450;
	color: #ffffff;
	border: none;
	box-shadow: none;
	margin: 0;
}
.empty-state__action--hover {
	opacity: 0.85;
}
</style>
