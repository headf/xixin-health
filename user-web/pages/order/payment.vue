<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>支付</h2>
        <p class="desc">完成体检套餐的在线支付</p>
      </div>
    </div>

    <div class="card mx-20 mb-16 text-center" v-if="order" style="padding:20px">
      <div class="text-sm text-secondary" style="margin-bottom:4px">支付金额</div>
      <div style="font-size:36px;font-weight:700;color:var(--primary);margin-bottom:8px">¥{{ $fmt(order.amountCent) }}</div>
      <div v-if="order.packageName" class="text-sm text-secondary">{{ order.packageName }}</div>
      <div class="text-sm text-secondary" style="margin-top:4px">订单号：{{ order.orderNo || '待生成' }}</div>
    </div>

    <div class="card mx-20 mb-16" v-if="order">
      <div class="card-hd"><span>选择支付方式</span></div>
      <div class="card-bd pad-0">
        <div class="list-item" @click="payMethod='wechat'">
          <div class="list-row">
            <span style="font-size:15px;font-weight:500;color:var(--title)">微信支付</span>
            <span v-if="payMethod==='wechat'" style="color:var(--primary);font-weight:600">✔</span>
          </div>
        </div>
        <div class="list-item" @click="payMethod='alipay'">
          <div class="list-row">
            <span style="font-size:15px;font-weight:500;color:var(--title)">支付宝</span>
            <span v-if="payMethod==='alipay'" style="color:var(--primary);font-weight:600">✔</span>
          </div>
        </div>
      </div>
    </div>

    <div class="card mx-20 mb-16" v-if="order">
      <div class="card-hd"><span>订单摘要</span></div>
      <div class="card-bd" style="padding:0">
        <div class="info-row"><span class="info-label">订单编号</span><span class="info-value text-sm">{{ order.orderNo||'创建中...' }}</span></div>
        <div class="info-row"><span class="info-label">商品说明</span><span class="info-value">{{ order.packageName||'体检套餐' }}</span></div>
      </div>
    </div>

    <div class="bottom-bar" v-if="order">
      <button class="btn btn-primary btn-block" :disabled="paying" @click="handlePay">{{ paying ? '支付处理中...' : '确认支付 ¥'+$fmt(order.amountCent) }}</button>
      <div class="text-center text-sm text-secondary" style="margin-top:6px">模拟支付环境，仅供测试</div>
    </div>

    <div v-if="!loading && !order" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
      <div class="empty-title">未选择订单</div>
      <div class="empty-desc">请先预约体检后生成订单</div>
      <button class="btn btn-primary" @click="$router.push({name:'packages'})">去选套餐</button>
    </div>

    <div v-if="loading" class="loading">加载订单...</div>
  </div>
</template>

<script>
import { orderApi, $fmt, $toast } from '../../common/api.js'
export default {
  name: 'PageOrderPayment',
  data() { return { order: null, payMethod: 'wechat', paying: false, loading: true } },
  methods: {
    $fmt,
    async handlePay() {
      if(!this.order||!this.order.orderNo){$toast('订单未就绪',true);return}
      this.paying=true
      try {
        const payNo='PAY'+Date.now()+Math.random().toString(36).slice(2,6).toUpperCase()
        await orderApi.simulatePayment(this.order.orderNo,payNo)
        $toast('支付成功！')
        this.order.status='PAID'; localStorage.setItem('selOrder',JSON.stringify(this.order))
        setTimeout(()=>this.$router.push({name:'orderDetail'}),800)
      } catch(e){$toast(e.message||'支付失败',true)}finally{this.paying=false}
    },
    initOrder() {
      try {
        const raw=localStorage.getItem('selOrder')
        if(!raw){const apptRaw=localStorage.getItem('selAppt');if(apptRaw){const appt=JSON.parse(apptRaw);if(appt&&appt.id){const amt=appt.amountCent||appt.priceCent||0;if(amt>0){orderApi.create(appt.id,amt).then(o=>{const od=o?.data||o;this.order=od;localStorage.setItem('selOrder',JSON.stringify(od))}).catch(()=>{this.order={...appt,amountCent:amt,orderNo:'ORD'+Date.now()}})}}}return}
        this.order=JSON.parse(raw)
      }catch{}finally{this.loading=false}
    }
  },
  mounted() { this.initOrder(); setTimeout(()=>{this.loading=false},500) }
}
</script>
