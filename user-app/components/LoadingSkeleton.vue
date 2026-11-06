<template>
	<view class="skeleton">
		<!-- 卡片骨架 -->
		<template v-if="type === 'card'">
			<view class="skeleton__card" v-for="i in count" :key="i">
				<view class="skeleton__line skeleton__line--h40 skeleton__line--w60"></view>
				<view class="skeleton__line skeleton__line--h28 skeleton__line--w80"></view>
				<view class="skeleton__line skeleton__line--h28 skeleton__line--w45"></view>
				<view class="skeleton__line skeleton__line--h24 skeleton__line--w30"></view>
			</view>
		</template>

		<!-- 列表骨架 -->
		<template v-if="type === 'list'">
			<view class="skeleton__list-item" v-for="i in count" :key="i">
				<view class="skeleton__circle skeleton__circle--sm"></view>
				<view class="skeleton__list-body">
					<view class="skeleton__line skeleton__line--h28 skeleton__line--w70"></view>
					<view class="skeleton__line skeleton__line--h24 skeleton__line--w40"></view>
				</view>
			</view>
		</template>

		<!-- 段落骨架 -->
		<template v-if="type === 'paragraph'">
			<view class="skeleton__paragraph">
				<view class="skeleton__line skeleton__line--h28 skeleton__line--w100"></view>
				<view class="skeleton__line skeleton__line--h28 skeleton__line--w90"></view>
				<view class="skeleton__line skeleton__line--h28 skeleton__line--w75"></view>
				<view class="skeleton__line skeleton__line--h28 skeleton__line--w60"></view>
			</view>
		</template>

		<!-- 个人资料骨架 -->
		<template v-if="type === 'profile'">
			<view class="skeleton__profile">
				<view class="skeleton__circle skeleton__circle--lg"></view>
				<view class="skeleton__profile-body">
					<view class="skeleton__line skeleton__line--h32 skeleton__line--w40"></view>
					<view class="skeleton__line skeleton__line--h24 skeleton__line--w60"></view>
				</view>
			</view>
		</template>

		<!-- 套餐卡片骨架（专用） -->
		<template v-if="type === 'package'">
			<view class="skeleton__card skeleton__card--pkg" v-for="i in count" :key="i">
				<view class="skeleton__line skeleton__line--h32 skeleton__line--w50"></view>
				<view class="skeleton__line skeleton__line--h28 skeleton__line--w25"></view>
				<view class="skeleton__tags">
					<view class="skeleton__line skeleton__line--h20 skeleton__line--w15"></view>
					<view class="skeleton__line skeleton__line--h20 skeleton__line--w12"></view>
					<view class="skeleton__line skeleton__line--h20 skeleton__line--w18"></view>
				</view>
			</view>
		</template>
	</view>
</template>

<script>
export default {
	name: 'LoadingSkeleton',
	props: {
		type: { type: String, default: 'card' },
		count: { type: Number, default: 3 }
	}
}
</script>

<style>
.skeleton {
	padding: 0 32rpx;
}

/* 通用骨架元素 */
.skeleton__line {
	background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	border-radius: 12rpx;
	animation: skeleton-shimmer 1.5s ease-in-out infinite;
	margin-bottom: 16rpx;
}
.skeleton__line--h20 { height: 20rpx; }
.skeleton__line--h24 { height: 24rpx; }
.skeleton__line--h28 { height: 28rpx; }
.skeleton__line--h32 { height: 32rpx; }
.skeleton__line--h40 { height: 40rpx; }
.skeleton__line--w12 { width: 12%; }
.skeleton__line--w15 { width: 15%; }
.skeleton__line--w18 { width: 18%; }
.skeleton__line--w25 { width: 25%; }
.skeleton__line--w30 { width: 30%; }
.skeleton__line--w40 { width: 40%; }
.skeleton__line--w45 { width: 45%; }
.skeleton__line--w50 { width: 50%; }
.skeleton__line--w60 { width: 60%; }
.skeleton__line--w70 { width: 70%; }
.skeleton__line--w75 { width: 75%; }
.skeleton__line--w80 { width: 80%; }
.skeleton__line--w90 { width: 90%; }
.skeleton__line--w100 { width: 100%; }

.skeleton__circle {
	background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	border-radius: 9999px;
	animation: skeleton-shimmer 1.5s ease-in-out infinite;
	flex-shrink: 0;
}
.skeleton__circle--sm { width: 64rpx; height: 64rpx; }
.skeleton__circle--lg { width: 96rpx; height: 96rpx; }

/* 卡片骨架 */
.skeleton__card {
	background: #ffffff;
	border-radius: 48rpx;
	padding: 32rpx 28rpx;
	margin-bottom: 20rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx;
}
.skeleton__card--pkg {
	padding: 28rpx;
}
.skeleton__tags {
	display: flex;
	flex-direction: row;
	gap: 12rpx;
	margin-top: 8rpx;
}

/* 列表骨架 */
.skeleton__list-item {
	display: flex;
	flex-direction: row;
	align-items: center;
	background: #ffffff;
	border-radius: 48rpx;
	padding: 28rpx 32rpx;
	margin-bottom: 16rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx;
}
.skeleton__list-body {
	flex: 1;
	margin-left: 24rpx;
}

/* 段落骨架 */
.skeleton__paragraph {
	background: #ffffff;
	border-radius: 48rpx;
	padding: 36rpx 32rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx;
}

/* 个人资料骨架 */
.skeleton__profile {
	display: flex;
	flex-direction: row;
	align-items: center;
	background: #ffffff;
	border-radius: 48rpx;
	padding: 32rpx;
	margin: 0 24rpx 24rpx;
	box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx;
}
.skeleton__profile-body {
	flex: 1;
	margin-left: 28rpx;
}

@keyframes skeleton-shimmer {
	0% { background-position: -200% 0; }
	100% { background-position: 200% 0; }
}
</style>
