<template>
	<view class="status-badge" :class="'status-badge--' + mappedStatus">
		<view class="status-badge__dot"></view>
		<text class="status-badge__label">{{ label }}</text>
	</view>
</template>

<script>
export default {
	name: 'StatusBadge',
	props: {
		status: { type: String, required: true },
		label: { type: String, required: true }
	},
	computed: {
		mappedStatus() {
			const map = {
				CREATED: 'pending',
				PENDING_PAYMENT: 'pending',
				PAID: 'paid',
				CHECKED_IN: 'active',
				COMPLETED: 'done',
				DONE: 'done',
				CANCELLED: 'cancelled',
				REFUND_REQUESTED: 'warn',
				REFUNDED: 'cancelled',
				CLOSED: 'cancelled',
				DRAFT: 'pending',
				CONFIRMED: 'paid',
				PUBLISHED: 'done'
			}
			return map[this.status] || this.status
		}
	}
}
</script>

<style>
.status-badge {
	display: inline-flex;
	flex-direction: row;
	align-items: center;
	padding: 6rpx 20rpx;
	border-radius: 9999px;
	gap: 8rpx;
}
.status-badge__dot {
	width: 10rpx;
	height: 10rpx;
	border-radius: 50%;
}
.status-badge__label {
	font-size: 22rpx;
	font-weight: 450;
}

/* Pending / warning */
.status-badge--pending {
	background: #FDF3E0;
}
.status-badge--pending .status-badge__dot {
	background: #D9A84C;
}
.status-badge--pending .status-badge__label {
	color: #8B6B00;
}

/* Paid / confirmed */
.status-badge--paid {
	background: #EDF2FB;
}
.status-badge--paid .status-badge__dot {
	background: #7B9FE0;
}
.status-badge--paid .status-badge__label {
	color: #6A8DC8;
}

/* Active / in-progress */
.status-badge--active {
	background: #E5F3F0;
}
.status-badge--active .status-badge__dot {
	background: #6DB8AD;
}
.status-badge--active .status-badge__label {
	color: #009688;
}

/* Done / completed */
.status-badge--done {
	background: #E8F5EB;
}
.status-badge--done .status-badge__dot {
	background: #6DB87A;
}
.status-badge--done .status-badge__label {
	color: #007A4D;
}

/* Cancelled / closed */
.status-badge--cancelled {
	background: #f7f7f8;
}
.status-badge--cancelled .status-badge__dot {
	background: #a3a6af;
}
.status-badge--cancelled .status-badge__label {
	color: #777b86;
}

/* Warn */
.status-badge--warn {
	background: #FDE8E7;
}
.status-badge--warn .status-badge__dot {
	background: #D96B68;
}
.status-badge--warn .status-badge__label {
	color: #C62828;
}
</style>
