<template>
  <div class="fade-in" v-if="appt">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>预约详情</h2>
        <p class="desc">查看体检预约的详细信息</p>
      </div>
    </div>

    <div class="card mx-20 mb-16 text-center" style="padding:24px;border-left:4px solid var(--primary)">
      <div style="margin-bottom:8px"><span :class="statusTag(appt.status)" style="font-size:14px;padding:6px 20px">{{ statusLabel(appt.status) }}</span></div>
      <div style="font-size:20px;font-weight:600;margin-bottom:4px">{{ appt.packageName }}</div>
      <div class="text-sm text-secondary">{{ appt.appointmentDate }} {{ appt.timeSlot }}</div>
    </div>

    <div class="card mx-20">
      <div class="card-hd"><span>预约信息</span></div>
      <div class="card-bd" style="padding:0">
        <div class="info-row"><span class="info-label">体检分院</span><span class="info-value">{{ appt.branchName }}</span></div>
        <div class="info-row"><span class="info-label">就诊人</span><span class="info-value">{{ appt.profileName || '-' }}</span></div>
        <div v-if="appt.remark" class="info-row"><span class="info-label">备注</span><span class="info-value">{{ appt.remark }}</span></div>
        <div v-if="appt.appointmentNo" class="info-row"><span class="info-label">预约编号</span><span class="info-value text-sm">{{ appt.appointmentNo }}</span></div>
      </div>
    </div>

    <div v-if="appt.status === 'CREATED' || appt.status === 'PAID'" class="card mx-20 mt-16 text-center" style="border:1px solid var(--border);padding:20px">
      <div class="text-sm text-secondary" style="margin-bottom:8px">{{ appt.status === 'PAID' ? '此预约已支付' : '此预约尚未支付' }}</div>
      <div v-if="appt.status === 'CREATED'" style="font-size:28px;font-weight:600;color:var(--primary);margin-bottom:16px">¥{{ $fmt(appt.amountCent || appt.priceCent || 0) }}</div>
      <div class="flex gap-8" style="justify-content:center">
        <button v-if="appt.status === 'CREATED'" class="btn btn-primary" @click="handlePay">立即支付</button>
        <button class="btn btn-outline btn-sm" @click="showCancelConfirm = true" :disabled="processing">{{ processing ? '处理中...' : '取消预约' }}</button>
      </div>
    </div>

    <!-- 取消预约确认弹窗 -->
    <div v-if="showCancelConfirm" class="modal-overlay" @click.self="showCancelConfirm = false">
      <div class="modal-box" style="max-width:320px">
        <div class="modal-icon">⚠️</div>
        <div class="modal-title">确定取消预约？</div>
        <div class="modal-desc">取消后预约将标记为已取消，已支付的金额将进入退款流程。</div>
        <div class="modal-actions" style="flex-direction:column;gap:8px">
          <button class="btn btn-primary btn-block" :disabled="cancelling" @click="handleCancel">
            <span v-if="cancelling" class="loading-spinner"></span>
            {{ cancelling ? '取消中...' : '确认取消' }}
          </button>
          <button class="btn btn-outline btn-block" @click="showCancelConfirm = false">暂不取消</button>
        </div>
      </div>
    </div>

    <div v-if="appt.status === 'CANCELLED'" class="text-center py-12 text-secondary" style="font-size:14px">此预约已取消</div>
    <div v-if="appt.status === 'COMPLETED'" class="flex justify-center" style="padding:16px 20px">
      <button class="btn btn-primary" @click="viewReport" v-if="appt.reportId">查看报告</button>
    </div>
  </div>

  <div v-else class="empty-state">
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/></svg>
    <div class="empty-title">未选择预约</div>
  </div>
</template>

<script>
import { $toast, APPOINTMENT_STATUS, orderApi, $fmt, request } from '../../common/api.js'
export default {
  name: 'PageAppointmentDetail',
  data() { return { appt: null, processing: false, showCancelConfirm: false, cancelling: false } },
  methods: {
    $fmt,
    statusLabel(s) { return APPOINTMENT_STATUS[s]?.label || s || '未知' },
    statusTag(s) { const m = { CREATED:'tag tag-orange', PAID:'tag tag-blue', CHECKED_IN:'tag tag-blue', COMPLETED:'tag tag-green', CANCELLED:'tag tag-gray' }; return m[s]||'tag tag-gray' },
    async handlePay() { this.processing = true; try { const amount = this.appt.amountCent||this.appt.priceCent||0; if(amount>0){try{const o=await orderApi.create(this.appt.id,amount);localStorage.setItem('selOrder',JSON.stringify(o?.data||o))}catch{} } this.$router.push({name:'orderPayment'})}catch(e){$toast('跳转支付失败',true)}finally{this.processing=false} },
    async handleCancel() {
      this.cancelling = true
      try {
        const uid = localStorage.getItem('userId')
        await request('POST', '/api/v1/appointments/' + this.appt.id + '/cancel', { userId: parseInt(uid) }, true)
        this.appt.status = 'CANCELLED'
        this.showCancelConfirm = false
        $toast('预约已取消')
      } catch (e) { $toast(e.message || '取消失败', true) }
      finally { this.cancelling = false }
    },
    viewReport() { if(this.appt.reportId){localStorage.setItem('selReport',JSON.stringify({id:this.appt.reportId}));this.$router.push({name:'reportDetail'})} }
  },
  mounted() { try{const raw=localStorage.getItem('selAppt');if(raw)this.appt=JSON.parse(raw)}catch{} }
}
</script>
