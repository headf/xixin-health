<template>
	<scroll-view class="cat-tabs" scroll-x show-scrollbar="false">
		<view class="cat-tabs__inner">
			<view
				class="cat-tabs__item"
				:class="{ 'cat-tabs__item--active': activeIndex === idx }"
				v-for="(tab, idx) in tabs"
				:key="idx"
				@tap="selectTab(idx)"
			>
				<text class="cat-tabs__label">{{ tab.label }}</text>
			</view>
		</view>
	</scroll-view>
</template>

<script>
export default {
	name: 'CategoryTabs',
	props: {
		tabs: { type: Array, required: true },
		activeIndex: { type: Number, default: 0 }
	},
	emits: ['change'],
	methods: {
		selectTab(idx) {
			if (idx !== this.activeIndex) {
				this.$emit('change', idx)
			}
		}
	}
}
</script>

<style>
.cat-tabs {
	width: 100%;
	white-space: nowrap;
	padding: 16rpx 0 8rpx;
}
.cat-tabs__inner {
	display: inline-flex;
	flex-direction: row;
	padding: 0 24rpx;
	gap: 16rpx;
}
.cat-tabs__item {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	height: 64rpx;
	padding: 0 28rpx;
	background: #f0f4f8;
	border-radius: 9999px;
	flex-shrink: 0;
	transition: all 0.25s ease;
}
.cat-tabs__item--active {
	background: linear-gradient(135deg, #7B9FE0, #6DB8AD);
	box-shadow: 0 4rpx 12rpx rgba(123, 159, 224, 0.22);
}
.cat-tabs__label {
	font-size: 26rpx;
	font-weight: 450;
	color: #777b86;
}
.cat-tabs__item--active .cat-tabs__label {
	color: #ffffff;
}
</style>
