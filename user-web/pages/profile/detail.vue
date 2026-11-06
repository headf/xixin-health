<template>
  <div class="fade-in" v-if="!loading && profile">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>档案详情</h2>
        <p class="desc">查看就诊人详细信息</p>
      </div>
    </div>

    <div class="text-center" style="padding:24px 0 16px">
      <div class="avatar-sm" style="width:64px;height:64px;border-radius:32px;font-size:24px;margin:0 auto 12px;background:#f1f5f9;color:#64748b;display:flex;align-items:center;justify-content:center">{{ (profile.name||'?').slice(0,1) }}</div>
      <div style="font-size:18px;font-weight:600">{{ profile.name }}</div>
      <div class="text-sm text-secondary" style="margin-top:4px">{{ profile.profileType==='FAMILY'?'家属':'本人' }}</div>
    </div>

    <div class="card mx-20">
      <div class="card-hd"><span>基本信息</span></div>
      <div class="card-bd" style="padding:0">
        <div class="info-row"><span class="info-label">手机号</span><span class="info-value">{{ profile.phone||'-' }}</span></div>
        <div class="info-row"><span class="info-label">性别</span><span class="info-value">{{ profile.idCardNo ? getIdCardGender(profile.idCardNo) : '-' }}</span></div>
        <div class="info-row"><span class="info-label">年龄</span><span class="info-value">{{ profile.idCardNo ? getIdCardAge(profile.idCardNo) + '岁' : '-' }}</span></div>
        <div class="info-row">
          <span class="info-label">身份证号</span>
          <span class="info-value info-value-row">
            <span>{{ profile.idCardNo ? (showIdCard ? profile.idCardNo : maskIdCard(profile.idCardNo, showIdCard)) : '-' }}</span>
            <span class="eye-btn" @click="showIdCard=!showIdCard">
              <svg class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <template v-if="showIdCard">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </template>
                <template v-else>
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </template>
              </svg>
            </span>
          </span>
        </div>
        <div class="info-row"><span class="info-label">过敏史</span><span class="info-value">{{ profile.allergyHistory||'无' }}</span></div>
        <div class="info-row"><span class="info-label">既往病史</span><span class="info-value">{{ profile.medicalHistory||'无' }}</span></div>
        <div class="info-row">
          <span class="info-label">紧急联系人</span>
          <span class="info-value info-value-row">
            <span>{{ profile.emergencyContactName ? (showContact ? profile.emergencyContactName : maskName(profile.emergencyContactName, showContact)) : '-' }}</span>
            <span class="eye-btn" @click="showContact=!showContact">
              <svg class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <template v-if="showContact">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </template>
                <template v-else>
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </template>
              </svg>
            </span>
          </span>
        </div>
        <div class="info-row">
          <span class="info-label">紧急电话</span>
          <span class="info-value info-value-row">
            <span>{{ profile.emergencyContactPhone ? (showContactPhone ? profile.emergencyContactPhone : maskPhone(profile.emergencyContactPhone, showContactPhone)) : '-' }}</span>
            <span class="eye-btn" @click="showContactPhone=!showContactPhone">
              <svg class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <template v-if="showContactPhone">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </template>
                <template v-else>
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </template>
              </svg>
            </span>
          </span>
        </div>
      </div>
    </div>

    <div style="padding:16px 20px">
      <button class="btn btn-outline btn-block" @click="$router.push({name:'profileEdit'})">编辑档案</button>
    </div>
  </div>

  <div v-else-if="loading" class="empty-state">
    <div class="empty-title">加载中...</div>
  </div>

  <div v-else class="empty-state">
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" style="margin-bottom:12px"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
    <div class="empty-title">未选择档案</div>
  </div>
</template>

<script>
import { profileApi, getIdCardGender, getIdCardAge } from '../../common/api.js'
export default {
  name: 'PageProfileDetail',
  data() {
    return {
      loading: true,
      profile: null,
      showIdCard: false,
      showContact: false,
      showContactPhone: false
    }
  },
  mounted() {
    this.fetchProfile()
  },
  methods: {
    getIdCardGender(idCard) { return getIdCardGender(idCard) },
    getIdCardAge(idCard) { return getIdCardAge(idCard) },
    async fetchProfile() {
      this.loading = true
      try {
        const raw = localStorage.getItem('selProfile')
        if (!raw) { this.loading = false; return }
        const brief = JSON.parse(raw)
        if (!brief || !brief.id) { this.profile = brief; this.loading = false; return }

        // 直接从API获取完整数据
        const detail = await profileApi.getProfileDetail(brief.id)
        this.profile = detail || brief
      } catch (e) {
        console.error('加载失败', e)
        try {
          const raw = localStorage.getItem('selProfile')
          if (raw) this.profile = JSON.parse(raw)
        } catch {}
      } finally {
        this.loading = false
      }
    },
    maskIdCard(val, show) {
      if (!val) return '-'
      if (show) return val
      if (val.length <= 10) {
        const half = Math.floor(val.length / 2)
        return val.slice(0, Math.max(half, 2)) + '****' + val.slice(-Math.max(half, 2))
      }
      return val.slice(0, 6) + '****' + val.slice(-4)
    },
    maskName(val, show) {
      if (!val) return '-'
      if (show) return val
      if (val.length <= 1) return '*'
      return val[0] + '*'.repeat(val.length - 1)
    },
    maskPhone(val, show) {
      if (!val) return '-'
      if (show) return val
      if (val.length <= 7) return val.slice(0, 3) + '****'
      return val.slice(0, 3) + '****' + val.slice(-4)
    }
  }
}
</script>

<style scoped>
.info-value-row {
  display: flex !important;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.eye-btn {
  display: inline-flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 50%;
  background: #f1f5f9; cursor: pointer; flex-shrink: 0;
  transition: background 0.15s;
}
.eye-btn:hover { background: #e2e8f0; }
.eye-btn:active { transform: scale(0.92); }
.eye-icon {
  width: 18px; height: 18px;
  color: #94a3b8;
}
</style>
