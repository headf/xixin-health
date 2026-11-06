<template>
  <div class="fade-in">
    <div v-if="!loggedIn">
      <div class="home-minimal">
        <div class="hm-glow"></div>
        <div class="hm-logo"><span>熙</span></div>
        <div class="hm-tagline">守护您的<br>每一次健康</div>
        <div class="hm-desc">专业体检管理平台 · 三甲医院合作</div>
        <div style="display:flex;gap:12px;justify-content:center;position:relative;z-index:1">
          <button class="hm-btn-primary" @click="$router.push('/login')">登录</button>
          <button class="hm-btn-ghost" @click="$router.push('/register')">注册</button>
        </div>
      </div>
    </div>

    <div v-else>
      <div class="flex items-center" style="margin:0 20px 16px;padding:16px 0;gap:12px">
        <div style="width:44px;height:44px;border-radius:50%;background:#f1f5f9;display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:600;color:#475569;flex-shrink:0">{{ phone?phone.slice(-2):'我' }}</div>
        <div>
          <div style="font-size:17px;font-weight:600;color:#0f172a">你好，{{ phone||'用户' }}</div>
          <div style="font-size:13px;color:#94a3b8;margin-top:1px">{{ greetingMsg }}</div>
        </div>
      </div>

      <div class="section-title" style="margin:0 20px 10px">快捷功能</div>
      <div class="home-features">
        <div class="home-feature" @click="$router.push({name:'packages'})">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          <span class="hf-title">体检套餐</span><span class="hf-sub">选择套餐</span>
        </div>
        <div class="home-feature" @click="$router.push({name:'appointmentCreate'})">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/></svg>
          <span class="hf-title">快速预约</span><span class="hf-sub">一键预约</span>
        </div>
        <div class="home-feature" @click="$router.push({name:'reports'})">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>
          <span class="hf-title">体检报告</span><span class="hf-sub">查看数据</span>
        </div>
        <div class="home-feature" @click="$router.push({name:'profiles'})">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" style="margin:0 auto 6px;display:block"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
          <span class="hf-title">就诊人</span><span class="hf-sub">管理档案</span>
        </div>
      </div>

      <!-- 实时体检进度（按就诊人分组） -->
      <div v-for="(exam, eidx) in examProgressList" :key="exam.exam_record_id" class="card mx-20 mb-16">
        <div class="card-hd">
          <span> {{ exam.profile_name }}</span>
          <div>
            <span class="tag tag-orange" style="margin-right:4px">{{ exam.package_name }}</span>
            <span class="tag tag-blue">{{ exam.exam_status === 'CHECKED_IN' ? '进行中' : '待签到' }}</span>
          </div>
        </div>
        <div class="card-bd" style="gap:6px;padding:12px 16px">
          <div style="font-size:12px;color:#94a3b8;margin-bottom:4px">{{ exam.appointment_date }} {{ exam.time_slot }}</div>
          <div v-for="d in exam.departments" :key="d.department_id" :class="['dept-row', 'dept-' + (d.dept_status||'pending').toLowerCase()]">
            <div :style="{width:'24px',height:'24px',borderRadius:'50%',flexShrink:0,display:'flex',alignItems:'center',justifyContent:'center',fontSize:'10px',fontWeight:'600',background:d.dept_status==='DONE'?'#dcfce7':(d.dept_status==='IN_PROGRESS'?'#dbeafe':'#f1f5f9'),color:d.dept_status==='DONE'?'#16a34a':(d.dept_status==='IN_PROGRESS'?'#2563eb':'#94a3b8')}">{{ d.dept_status==='DONE'?'✓':(d.dept_status==='IN_PROGRESS'?'◐':'○') }}</div>
            <div class="flex-1">
              <div style="font-size:13px;font-weight:500;color:var(--title)">{{ d.department_name }}</div>
              <div style="font-size:10px;color:#94a3b8">{{ d.location||'院内' }}<span v-if="d.fasting_required" style="color:#dc2626;background:#fef2f2;padding:0 4px;border-radius:3px;margin-left:4px;font-weight:500">需空腹</span></div>
            </div>
            <div style="text-align:right;flex-shrink:0">
              <div v-if="d.dept_status!=='DONE'&&d.waiting>0" style="font-size:11px;color:var(--primary);font-weight:600">{{ d.waiting }}人排队</div>
              <span :class="statusClass(d.dept_status)">{{ statusLabel(d.dept_status) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 预约卡片（未签到） -->
      <div class="card mx-20 mb-16" v-if="nextAppt">
        <div class="card-hd"><span>下一个预约</span><span :class="statusTag(nextAppt.status)">{{ statusText(nextAppt.status) }}</span></div>
        <div class="card-bd" style="flex-direction:row;gap:12px;align-items:center;padding:12px 16px">
          <div style="text-align:center;flex-shrink:0">
            <div style="font-size:20px;font-weight:700;color:var(--primary)">{{ formatDay(nextAppt.appointmentDate) }}</div>
            <div class="text-sm text-secondary">{{ formatMonth(nextAppt.appointmentDate) }}</div>
          </div>
          <div class="flex-1">
            <div style="font-size:15px;font-weight:600;color:var(--title)">{{ nextAppt.packageName }}</div>
            <div class="text-sm text-secondary">{{ nextAppt.branchName||'' }} · {{ nextAppt.timeSlot }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { isLoggedIn, appointmentApi, reportApi, profileApi, request } from '../../common/api.js'

export default {
  name: 'PageIndex',
  data() { return {
    stats: { appointments:0, reports:0, profiles:0 },
    examProgressList: [],
    nextAppt: null,
    pollTimer: null
  }},
  computed: {
    loggedIn() { return isLoggedIn() },
    phone() { return localStorage.getItem('userPhone')||'' },
    greetingMsg() { const h=new Date().getHours(); if(h<6)return'夜深了'; if(h<9)return'早上好'; if(h<12)return'上午好'; if(h<14)return'中午好'; if(h<18)return'下午好'; return'晚上好' }
  },
  methods: {
    statusClass(s) { const m={DONE:'tag tag-green',IN_PROGRESS:'tag tag-blue',PENDING:'tag tag-gray'}; return m[s]||'tag tag-gray' },
    statusLabel(s) { const m={DONE:'已完成',IN_PROGRESS:'检查中',PENDING:'待检查'}; return m[s]||s },
    statusText(s) { const m={PAID:'已支付',CREATED:'待支付',CHECKED_IN:'已签到',COMPLETED:'已完成'}; return m[s]||s },
    statusTag(s) { const m={PAID:'tag tag-blue',CREATED:'tag tag-orange',CHECKED_IN:'tag tag-green',COMPLETED:'tag tag-gray'}; return m[s]||'tag tag-gray' },
    formatDay(d) { if(!d)return''; return parseInt(d.split('-')[2]) },
    formatMonth(d) { if(!d)return''; return parseInt(d.split('-')[1])+'月' },
    async loadProgress() {
      const uid = localStorage.getItem('userId')
      if (!uid) return
      try {
        const data = await request('GET', '/api/v1/exams/my-progress?userId=' + uid, null, true)
        if (Array.isArray(data) && data.length > 0) {
          // Group by exam_record_id
          const map = {}
          for (const row of data) {
            const key = row.exam_record_id
            if (!map[key]) {
              map[key] = {
                exam_record_id: row.exam_record_id,
                profile_name: row.profile_name || '未知',
                package_name: row.package_name || '',
                exam_status: row.exam_status || '',
                appointment_date: row.appointment_date || '',
                time_slot: row.time_slot || '',
                departments: []
              }
            }
            map[key].departments.push({
              department_id: row.department_id,
              department_name: row.department_name,
              location: row.location,
              fasting_required: row.fasting_required,
              dept_status: row.dept_status || 'PENDING',
              waiting: row.waiting || 0
            })
          }
          this.examProgressList = Object.values(map)
        } else {
          this.examProgressList = []
        }
      } catch { this.examProgressList = [] }
    },
    async loadNextAppointment() {
      const uid = localStorage.getItem('userId')||'0'
      try {
        const apps = await appointmentApi.getByUser(uid)
        if (Array.isArray(apps) && apps.length > 0) {
          const active = apps.filter(a => a.status !== 'CANCELLED' && a.status !== 'COMPLETED')
          this.nextAppt = active.length > 0 ? active[0] : null
        }
      } catch {}
    },
    async loadStats() {
      const uid = localStorage.getItem('userId')||'0'
      try {
        const [apps,reps,profs] = await Promise.allSettled([appointmentApi.getByUser(uid),reportApi.getByUser(uid),profileApi.getByUser(uid)])
        this.stats = { appointments:apps.value?.length||0, reports:reps.value?.length||0, profiles:profs.value?.length||0 }
      } catch {}
    }
  },
  mounted() {
    if (this.loggedIn) {
      this.loadStats()
      this.loadProgress()
      this.loadNextAppointment()
      this.pollTimer = setInterval(() => {
        this.loadProgress()
        this.loadNextAppointment()
      }, 10000)
    }
  },
  unmounted() { if (this.pollTimer) clearInterval(this.pollTimer) }
}
</script>

<style>
.dept-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-bottom: 1px solid #f1f5f9;
  border-radius: 6px;
  margin: 2px 0;
}
.dept-done {
  background: #f0fdf4;
  border-left: 3px solid #10b981;
}
.dept-in_progress {
  background: #eff6ff;
  border-left: 3px solid #3b82f6;
}
.dept-pending {
  background: #fafafa;
  border-left: 3px solid #d1d5db;
}
</style>
