<template>
  <div class="fade-in" v-if="order">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>订单详情</h2>
        <p class="desc">查看订单信息和退款</p>
      </div>
    </div>

    <!-- Amount hero -->
    <div class="card card-ink" style="margin:16px 20px;text-align:center">
      <div class="card-hd"><span>支付金额</span></div>
      <div style="font-size:40px;font-weight:600">¥{{ $fmt(order.amountCent) }}</div>
      <div style="margin-top:8px">
        <span :class="orderStatusTag(order.status)" style="font-size:13px;padding:4px 16px">{{ orderStatusLabel(order.status) }}</span>
      </div>
    </div>

    <div class="card" style="margin:0 20px">
      <div class="card-hd"><span>订单信息</span></div>
      <div class="card-bd" style="padding:0">
        <div class="info-row">
          <span class="info-label">订单编号</span>
          <span class="info-value">{{ order.orderNo }}</span>
        </div>
        <div v-if="order.paymentNo" class="info-row">
          <span class="info-label">支付流水号</span>
          <span class="info-value">{{ order.paymentNo }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">体检套餐</span>
          <span class="info-value">{{ order.packageName || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">预约日期</span>
          <span class="info-value">{{ order.appointmentDate || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">创建时间</span>
          <span class="info-value">{{ order.createdAt || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- Refund Section -->
    <div v-if="order.status === 'PAID'" class="card" style="margin:16px 20px;border-left:4px solid #f97316">
      <div class="card-hd"><span>🔄 退款</span></div>
      <div class="card-bd">
        <div v-if="canRefund" class="refund-info">
          <div class="refund-deadline" style="font-size:13px;color:var(--n-500);margin-bottom:12px">
            ⏰ 可在 <strong>{{ refundDeadline }}</strong> 前免费取消并全额退款
          </div>
          <button class="btn btn-warning btn-block" @click="showRefundConfirm = true">
            申请退款
          </button>
        </div>
        <div v-else class="refund-info">
          <div class="refund-expired" style="font-size:13px;color:var(--n-400);text-align:center">
            😅 已过退款期限（{{ refundDeadline }}），无法退款
            <div style="margin-top:4px;font-size:12px">根据规定，预约日前一天20:00后将不再接受退款</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Refund status cards -->
    <div v-if="order.status === 'REFUND_REQUESTED'" class="card" style="margin:16px 20px;border-left:4px solid #f59e0b">
      <div class="card-hd"><span>🔄 退款处理中</span></div>
      <div class="card-bd" style="text-align:center">
        <div style="font-size:36px;margin-bottom:8px"></div>
        <div style="font-size:15px;font-weight:500;margin-bottom:4px">退款请求已提交</div>
        <div style="font-size:13px;color:var(--n-500)">我们正在处理您的退款，请耐心等待</div>
      </div>
    </div>

    <div v-if="order.status === 'REFUNDED'" class="card" style="margin:16px 20px;border-left:4px solid #22c55e">
      <div class="card-hd"><span>✅ 已退款</span></div>
      <div class="card-bd" style="text-align:center">
        <div style="font-size:36px;margin-bottom:8px">✅</div>
        <div style="font-size:15px;font-weight:500;margin-bottom:4px">退款已完成</div>
        <div style="font-size:13px;color:var(--n-500)">款项将原路返回</div>
      </div>
    </div>

    <div v-if="order.status === 'CLOSED'" class="card" style="margin:16px 20px;border-left:4px solid #94a3b8">
      <div class="card-hd"><span>已取消</span></div>
      <div class="card-bd" style="text-align:center">
        <div style="font-size:36px;margin-bottom:8px">📌</div>
        <div style="font-size:15px;font-weight:500;margin-bottom:4px">订单已取消</div>
        <div style="font-size:13px;color:var(--n-500)">该订单因预约取消而自动关闭</div>
      </div>
    </div>

    <!-- Payment button -->
    <div v-if="order.status === 'PENDING_PAYMENT'" style="padding:20px;margin-top:20px">
      <button class="btn btn-primary btn-block" @click="$router.push({name:'orderPayment'})">去支付</button>
    </div>

    <!-- Refund Confirm Dialog -->
    <div v-if="showRefundConfirm" class="modal-overlay" @click.self="showRefundConfirm = false">
      <div class="modal-dialog">
        <div class="modal-hd">确认退款</div>
        <div class="modal-bd">
          <div style="text-align:center;margin-bottom:16px">
            <div style="font-size:40px;margin-bottom:8px">⚠️</div>
            <div style="font-size:16px;font-weight:600">确定要取消该订单吗？</div>
            <div style="font-size:13px;color:var(--n-500);margin-top:8px">
              套餐：{{ order.packageName }}<br>
              金额：<strong>¥{{ $fmt(order.amountCent) }}</strong>（全额退款）
            </div>
          </div>
          <div style="display:flex;gap:12px">
            <button class="btn btn-gray btn-block" @click="showRefundConfirm = false">再想想</button>
            <button class="btn btn-warning btn-block" :disabled="refunding" @click="handleRefund">
              {{ refunding ? '处理中...' : '确认退款' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="empty-state">
    <div class="empty-icon"></div>
    <div class="empty-title">未选择订单</div>
    <div style="margin-top:12px">
      <button class="btn btn-primary" @click="$router.push({name:'orderList'})">查看订单列表</button>
    </div>
  </div>
</template>

<script>
import { $fmt, ORDER_STATUS, orderApi, appointmentApi, $toast } from '../../common/api.js'

export default {
  name: 'PageOrderDetail',
  data() {
    return {
      order: null,
      showRefundConfirm: false,
      refunding: false,
      refundDeadline: '',
      canRefund: false
    }
  },
  methods: {
    $fmt,
    orderStatusLabel(s) {
      return ORDER_STATUS[s]?.label || s || '未知'
    },
    orderStatusTag(s) {
      const m = { PENDING_PAYMENT: 'tag tag-orange', PAID: 'tag tag-green', REFUND_REQUESTED: 'tag tag-warning', REFUNDED: 'tag tag-gray', CLOSED: 'tag tag-gray' }
      return m[s] || 'tag tag-gray'
    },
    calcRefundDeadline() {
      const aptDate = this.order.appointmentDate
      if (!aptDate) {
        this.canRefund = false
        this.refundDeadline = '无法确定'
        return
      }
      const d = new Date(aptDate)
      const deadline = new Date(d)
      deadline.setDate(deadline.getDate() - 1)
      deadline.setHours(20, 0, 0, 0)
      const now = new Date()
      const month = String(deadline.getMonth() + 1).padStart(2, '0')
      const day = String(deadline.getDate()).padStart(2, '0')
      const hour = String(deadline.getHours()).padStart(2, '0')
      const min = String(deadline.getMinutes()).padStart(2, '0')
      this.refundDeadline = `${month}月${day}日 ${hour}:${min}`
      this.canRefund = now < deadline
    },
    async handleRefund() {
      this.refunding = true
      try {
        const result = await orderApi.requestRefund(this.order.orderNo, this.order.amountCent, '用户主动取消')
        if (result) {
          this.order.status = 'REFUND_REQUESTED'
          this.showRefundConfirm = false
          $toast('退款申请已提交')
        }
      } catch (e) {
        $toast(e.message || '退款申请失败', true)
      } finally {
        this.refunding = false
      }
    },
    async loadAppointmentDate() {
      if (this.order && this.order.appointmentDate) return
      try {
        const uid = localStorage.getItem('userId') || '0'
        const apps = await appointmentApi.getByUser(uid)
        if (Array.isArray(apps)) {
          const match = apps.find(a => a.id === this.order.appointmentId)
          if (match && match.appointmentDate) {
            this.order.appointmentDate = match.appointmentDate
            this.order.packageName = this.order.packageName || match.packageName
            this.calcRefundDeadline()
          }
        }
      } catch {}
    }
  },
  mounted() {
    try {
      const raw = localStorage.getItem('selOrder')
      if (raw) {
        this.order = JSON.parse(raw)
        this.loadAppointmentDate()
        this.calcRefundDeadline()
      }
    } catch (e) { /* ignore */ }
  }
}
</script>

<style scoped>
.refund-info {
  padding: 4px 0;
}
.btn-warning {
  background: #f97316;
  color: #fff;
}
.btn-warning:hover {
  background: #ea580c;
}
.btn-warning:disabled {
  background: #fdba74;
}
.btn-gray {
  background: #e2e8f0;
  color: #475569;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal-dialog {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 340px;
  overflow: hidden;
}
.modal-hd {
  padding: 20px 20px 0;
  font-size: 17px;
  font-weight: 600;
  text-align: center;
}
.modal-bd {
  padding: 16px 20px 20px;
}
</style>
