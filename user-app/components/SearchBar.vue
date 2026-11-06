<template>
	<view class="search-bar">
		<view class="search-bar__inner">
			<view class="search-bar__icon" @tap="handleSearch"></view>
			<input
				class="search-bar__input"
				:placeholder="placeholder"
				:value="modelValue"
				@input="handleInput"
				confirm-type="search"
				@confirm="handleSearch"
			/>
			<text class="search-bar__clear" v-if="modelValue" @tap="handleClear">✕</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'SearchBar',
	props: {
		placeholder: { type: String, default: '搜索体检套餐、项目...' },
		modelValue: { type: String, default: '' }
	},
	emits: ['update:modelValue', 'search', 'clear'],
	methods: {
		handleInput(e) {
			this.$emit('update:modelValue', e.detail.value)
		},
		handleSearch(e) {
			const val = (e && e.detail && e.detail.value) || this.modelValue
			this.$emit('search', val)
		},
		handleClear() {
			this.$emit('update:modelValue', '')
			this.$emit('clear')
		}
	}
}
</script>

<style>
.search-bar {
	padding: 12rpx 32rpx 16rpx;
}
.search-bar__inner {
	display: flex;
	flex-direction: row;
	align-items: center;
	background: #ffffff;
	border-radius: 48rpx;
	padding: 0 24rpx;
	height: 80rpx;
	box-shadow: 0 4rpx 12rpx rgba(123, 159, 224, 0.10);
	border: 2rpx solid transparent;
	transition: all 0.2s;
}
.search-bar__inner:focus-within {
	border-color: #7B9FE0;
	box-shadow: 0 4rpx 16rpx rgba(123, 159, 224, 0.18);
}
.search-bar__icon {
	width: 36rpx;
	height: 36rpx;
	flex-shrink: 0;
	margin-right: 16rpx;
	background-image: url('/static/search_icon.png');
	background-size: contain;
	background-repeat: no-repeat;
	background-position: center;
}
.search-bar__input {
	flex: 1;
	height: 80rpx;
	font-size: 28rpx;
	color: #17191c;
	background: transparent;
	border: none;
	outline: none;
}
.search-bar__input::placeholder {
	color: #a3a6af;
	font-size: 26rpx;
}
.search-bar__clear {
	width: 36rpx;
	height: 36rpx;
	line-height: 36rpx;
	text-align: center;
	font-size: 24rpx;
	color: #a3a6af;
	flex-shrink: 0;
	border-radius: 50%;
	background: #f0f0f0;
}
</style>
