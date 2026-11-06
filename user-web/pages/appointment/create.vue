<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>填写预约</h2>
        <p class="desc">选择套餐、分院和日期进行体检预约</p>
      </div>
    </div>

    <div class="px-20">
      <!-- Package picker -->
      <div class="field">
        <label>体检套餐</label>
        <div class="finput picker" @click="$router.push({name:'packages'})">
          <span :class="form.packageName?'val':'ph'">{{ form.packageName || '请选择套餐' }}</span>
          <span class="arw">›</span>
        </div>
        <div v-if="form.packagePrice" style="padding: 6px 4px 0; font-size: 13px; color: var(--color-teal); font-weight: 500;">
          价格：¥{{ $fmt(form.packagePrice) }}
        </div>
      </div>

      <!-- Branch picker -->
      <div class="field">
        <label>体检分院</label>
        <div class="finput picker" @click="showBranches = true; loadBranches()">
          <span :class="form.branchName?'val':'ph'">{{ form.branchName || '请选择分院' }}</span>
          <span class="arw">›</span>
        </div>
      </div>

      <!-- Date picker -->
      <div class="field">
        <label>预约日期</label>
        <div class="date-grid">
          <div v-for="d in dateOptions" :key="d.value" class="date-item" :class="{active: form.appointmentDate === d.value}" @click="selectDate(d.value)">{{ d.label }}</div>
        </div>
      </div>

      <!-- Time slot -->
      <div class="field">
        <label>时段 <span v-if="slotsLoading" style="font-size:12px;color:#999">加载中...</span></label>
        <div class="slot-row">
          <div v-for="s in slots" :key="s.slotCode"
            class="slot-item"
            :class="{
              active: form.timeSlot === s.slotCode,
              disabled: s.status === 'NO_SCHEDULE' || s.status === 'FULL'
            }"
            @click="selectSlot(s)">
            <div style="display:flex;flex-direction:column;align-items:center;gap:2px">
              <span>{{ s.slotCode === 'MORNING' ? '' : '' }} {{ s.slotLabel.split(' ')[0] }}</span>
              <span v-if="s.status === 'FULL'" style="font-size:11px;color:#f56c6c;font-weight:600">已满</span>
              <span v-else-if="s.status === 'NO_SCHEDULE'" style="font-size:11px;color:#bbb">无排班</span>
              <span v-else style="font-size:11px;color:#67c23a;font-weight:500">余{{ s.remainingSlots }}号</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile picker -->
      <div class="field">
        <label>就诊人</label>
        <div class="finput picker" @click="showProfiles = true; loadProfiles()">
          <span :class="form.profileName?'val':'ph'">{{ form.profileName || '请选择就诊人' }}</span>
          <span class="arw">›</span>
        </div>
      </div>

      <!-- Remark -->
      <div class="field">
        <label>备注</label>
        <textarea v-model="form.remark" class="ftextarea" placeholder="如有特殊需求请在此备注" rows="3"></textarea>
      </div>
    </div>

    <div style="height:80px"></div>
    <div class="bottom-bar">
      <button class="btn btn-primary btn-block btn-glow" :disabled="submitting" @click="handleSubmit">
        <span v-if="submitting" class="loading-spinner"></span>
        {{ submitting ? '提交中...' : '确认预约' }}
      </button>
      <div v-if="form.packagePrice" style="text-align:center;margin-top:8px;font-size:13px;color:var(--color-graphite)">
        预约后可直接在线支付
      </div>
    </div>

    <!-- Branch sheet -->
    <div v-if="showBranches" class="overlay" @click.self="showBranches = false">
      <div class="sheet">
        <div class="sheet-title">选择体检分院</div>
        <div v-for="b in branches" :key="b.id" class="sheet-item" @click="selectBranch(b)">
          <div class="sheet-item-name">{{ b.branchName || b.name }}</div>
          <div class="sheet-item-sub">{{ b.address || '' }}</div>
        </div>
        <div v-if="branches.length === 0" style="text-align:center;padding:20px;color:var(--color-dove)">加载中...</div>
        <button class="btn btn-outline btn-block btn-sm" style="margin-top:16px" @click="showBranches = false">取消</button>
      </div>
    </div>

    <!-- Profile sheet -->
    <div v-if="showProfiles" class="overlay" @click.self="showProfiles = false">
      <div class="sheet">
        <div class="sheet-title">选择就诊人</div>
        <div v-for="p in profiles" :key="p.id" class="sheet-item" @click="selectProfile(p)">
          <div class="sheet-item-name">{{ p.name }}</div>
          <div class="sheet-item-sub">{{ p.profileType === 'SELF' ? '本人' : '家属' }} · {{ p.phone }}</div>
        </div>
        <div v-if="profiles.length === 0" style="text-align:center;padding:20px;color:var(--color-dove)">暂无就诊人，请先添加</div>
        <button class="btn btn-outline btn-block btn-sm" style="margin-top:16px" @click="showProfiles = false">取消</button>
      </div>
    </div>

    <!-- Success modal with payment option -->
    <div v-if="showSuccess" class="modal-overlay" @click.self="showSuccess = false">
      <div class="modal-box">
        <div class="modal-icon">✅</div>
        <div class="modal-title">预约成功！</div>
        <div class="modal-desc">
          您的体检预约已提交成功<br>
          是否立即支付？<br>
          <span style="font-weight:600;font-size:18px;color:var(--color-ink);display:block;margin-top:8px">
            ¥{{ $fmt(payAmount || form.packagePrice) }}
          </span>
          <div style="margin-top:16px;padding:16px;background:#fff3cd;border:1px solid #ffc107;border-radius:12px;font-size:15px;text-align:left;line-height:1.8">
            <strong style="color:#856404">⚠️ 温馨提示</strong>
            <ul style="margin:8px 0 0;padding-left:20px;color:#856404;font-size:14px">
              <li>体检当天请空腹（禁食8-12小时）</li>
              <li>请携带本人身份证</li>
              <li>建议穿着宽松、便于穿脱的衣物</li>
            </ul>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-primary btn-block btn-glow" @click="goPay">立即支付</button>
          <button class="btn btn-outline btn-block" @click="goAppointments">稍后再说</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { packageApi, appointmentApi, profileApi, orderApi, slotApi, $toast, $fmt } from '../../common/api.js'

export default {
  name: 'PageAppointmentCreate',
  data() {
    return {
      form: {
        packageId: '', packageName: '', packagePrice: 0,
        branchId: '', branchName: '',
        appointmentDate: '', timeSlot: '',
        profileId: '', profileName: '',
        remark: ''
      },
      branches: [], profiles: [],
      slots: [],
      slotsLoading: false,
      showBranches: false, showProfiles: false,
      submitting: false,
      dateOptions: [],
      showSuccess: false,
      payAmount: 0,
      createdOrder: null
    }
  },
  methods: {
    $fmt,
    buildDateOptions() {
      const opts = []
      for (let i = 0; i < 14; i++) {
        const d = new Date(); d.setDate(d.getDate() + i)
        opts.push({
          label: (d.getMonth()+1) + '/' + d.getDate(),
          value: d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')
        })
      }
      this.dateOptions = opts
    },
    async loadSlots() {
      if (!this.form.branchId || !this.form.appointmentDate) return
      this.slotsLoading = true
      try {
        this.slots = await slotApi.getAvailable(
          this.form.branchId,
          this.form.appointmentDate
        ) || []
        // 如果当前所选时段已满则清空
        if (this.form.timeSlot) {
          const current = this.slots.find(s => s.slotCode === this.form.timeSlot)
          if (!current || current.status === 'FULL' || current.status === 'NO_SCHEDULE') {
            this.form.timeSlot = ''
          }
        }
        // 默认选中第一个可用时段
        if (!this.form.timeSlot) {
          const firstAvail = this.slots.find(s => s.status === 'AVAILABLE' || s.status === 'TIGHT')
          if (firstAvail) this.form.timeSlot = firstAvail.slotCode
        }
      } catch (e) {
        console.warn('加载号源失败，使用默认时段', e)
        this.slots = [
          { slotCode: 'MORNING', slotLabel: '上午', remainingSlots: 0, maxCapacity: 0, bookedCount: 0, status: 'AVAILABLE' },
          { slotCode: 'AFTERNOON', slotLabel: '下午', remainingSlots: 0, maxCapacity: 0, bookedCount: 0, status: 'AVAILABLE' }
        ]
      } finally {
        this.slotsLoading = false
      }
    },
    selectSlot(s) {
      if (s.status === 'NO_SCHEDULE' || s.status === 'FULL') return
      this.form.timeSlot = s.slotCode
    },
    selectBranch(b) {
      this.form.branchId = b.id
      this.form.branchName = b.branchName || b.name
      this.showBranches = false
      this.loadSlots()
    },
    selectProfile(p) {
      this.form.profileId = p.id
      this.form.profileName = p.name
      this.showProfiles = false
    },
    async loadBranches() {
      try { this.branches = await packageApi.getBranches() }
      catch(e) {}
    },
    async loadProfiles() {
      try { this.profiles = await profileApi.getByUser(localStorage.getItem('userId')||'1') }
      catch(e) {}
    },
    selectDate(date) {
      this.form.appointmentDate = date
      this.loadSlots()
    },
    restoreSelections() {
      try {
        const pkg = JSON.parse(localStorage.getItem('selPkg') || 'null')
        if (pkg) {
          this.form.packageId = pkg.id
          this.form.packageName = pkg.packageName || pkg.name
          this.form.packagePrice = pkg.priceCent || 0
        }
        const profile = JSON.parse(localStorage.getItem('selProfile') || 'null')
        if (profile) {
          this.form.profileId = profile.id
          this.form.profileName = profile.name
        }
      } catch(e) {}
    },
    async handleSubmit() {
      if (!this.form.packageId) { $toast('请选择体检套餐', true); return }
      if (!this.form.branchId) { $toast('请选择体检分院', true); return }
      if (!this.form.appointmentDate) { $toast('请选择预约日期', true); return }
      if (!this.form.timeSlot) { $toast('请选择预约时段', true); return }
      if (!this.form.profileId) { $toast('请选择就诊人', true); return }

      this.submitting = true
      try {
        // Step 1: Create appointment (use slot code MORNING/AFTERNOON)
        const appt = await appointmentApi.create({
          userId: parseInt(localStorage.getItem('userId') || '0'),
          packageId: this.form.packageId,
          branchId: this.form.branchId,
          appointmentDate: this.form.appointmentDate,
          timeSlot: this.form.timeSlot,
          profileId: this.form.profileId,
          remark: this.form.remark || undefined
        })

        // Step 2: Create order for payment
        const apptId = appt?.data?.id || appt?.id || appt
        const amount = this.form.packagePrice

        if (apptId && amount) {
          try {
            const order = await orderApi.create(apptId, amount)
            this.createdOrder = order?.data || order
            this.payAmount = amount
            // Store for payment page
            localStorage.setItem('selOrder', JSON.stringify(this.createdOrder))
          } catch (orderErr) {
            console.warn('Order creation skipped:', orderErr)
          }
        }

        // Step 3: Show success with payment option
        this.showSuccess = true

      } catch (e) {
        $toast(e.message || '预约失败', true)
      } finally {
        this.submitting = false
      }
    },
    goPay() {
      this.showSuccess = false
      this.$router.push({ name: 'orderPayment' })
    },
    goAppointments() {
      this.showSuccess = false
      this.$router.push({ name: 'appointments' })
    }
  },
  mounted() {
    this.buildDateOptions()
    this.loadBranches()
    this.restoreSelections()
  }
}
</script>

<style scoped>
.picker {
  display: flex;
  align-items: center;
  cursor: pointer;
  border: 1.5px solid rgba(26,29,35,0.08);
  background: var(--color-pure-white);
}
.picker .val { color: var(--color-ink); font-weight: 450; }
.picker .ph { color: var(--color-dove); }
.picker .arw { margin-left: auto; color: var(--color-dove); font-size: 20px; }
.loading-spinner {
  display: inline-block;
  width: 18px; height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
.slot-item.disabled {
  opacity: 0.45;
  cursor: not-allowed;
  background: #f5f5f5;
  border-color: #eee;
}
.slot-item.disabled:hover {
  transform: none;
  box-shadow: none;
  border-color: #eee;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
