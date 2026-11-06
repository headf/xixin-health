<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>订单管理</h2>
        <p class="desc">查看所有体检订单和支付状态</p>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="orders.length===0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
      <div class="empty-title">暂无订单</div>
      <div class="empty-desc">预约体检后将自动生成订单</div>
    </div>

    <div v-else class="card mx-20">
      <div class="card-bd pad-0">
        <div v-for="o in orders" :key="o.id" class="list-item" @click="goDetail(o)">
          <div class="list-row">
            <span class="text-sm text-secondary">订单号：{{ o.orderNo }}</span>
            <span :class="statusTag(o.status)">{{ orderStatusLabel(o.status) }}</span>
          </div>
          <div class="list-row" style="margin-top:8px">
            <span style="font-size:15px;font-weight:500;color:var(--title)">{{ o.packageName || '体检套餐' }}</span>
            <span style="font-size:18px;font-weight:600;color:var(--primary)">¥{{ $fmt(o.amountCent) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { orderApi, $fmt, ORDER_STATUS } from '../../common/api.js'
export default {
  name: 'PageOrders',
  data() { return { orders: [], loading: true } },
  methods: {
    $fmt,
    async load() { const uid=localStorage.getItem('userId')||'0'; this.loading=true; try{this.orders=await orderApi.getByUser(uid)}catch{}finally{this.loading=false} },
    goDetail(o) { localStorage.setItem('selOrder',JSON.stringify(o)); this.$router.push({name:'orderDetail'}) },
    orderStatusLabel(s) { return ORDER_STATUS[s]?.label||s||'未知' },
    statusTag(s) { const m={PENDING_PAYMENT:'tag tag-orange',PAID:'tag tag-green',REFUND_REQUESTED:'tag tag-orange',REFUNDED:'tag tag-gray',CLOSED:'tag tag-gray'}; return m[s]||'tag tag-gray' }
  },
  mounted() { this.load() }
}
</script>
