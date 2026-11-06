<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>就诊人档案</h2>
        <p class="desc">管理就诊人档案信息</p>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="profiles.length===0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
      <div class="empty-title">暂无就诊人</div>
      <div class="empty-desc">添加就诊人信息，方便预约体检</div>
      <button class="btn btn-primary" @click="addProfile">+ 添加就诊人</button>
    </div>

    <div v-else class="card mx-20">
      <div class="card-bd pad-0">
        <div v-for="p in profiles" :key="p.id" class="list-item" @click="goDetail(p)" style="display:flex;align-items:center;gap:12px">
          <div class="avatar-sm" style="background:#f1f5f9;color:#64748b;width:40px;height:40px;border-radius:20px;font-size:16px;display:flex;align-items:center;justify-content:center;flex-shrink:0">{{ (p.name||'?').slice(0,1) }}</div>
          <div class="flex-1">
            <div style="font-size:15px;font-weight:500;color:var(--title)">{{ p.name }}</div>
            <div class="text-sm text-secondary">{{ p.profileType==='FAMILY'?'家属':'本人' }}
              <template v-if="p.idCardNo"> · {{ getIdCardGender(p.idCardNo) }} · {{ getIdCardAge(p.idCardNo) }}岁</template>
              · {{ p.phone||'' }}</div>
          </div>
          <span style="font-size:16px;color:#cbd5e1">›</span>
        </div>
      </div>
      <div style="padding:12px 16px">
        <button class="btn btn-outline btn-block" @click="addProfile">+ 添加</button>
      </div>
    </div>
  </div>
</template>

<script>
import { profileApi, getIdCardGender, getIdCardAge } from '../../common/api.js'
export default {
  name: 'PageProfiles',
  data() { return { profiles:[], loading:true } },
  methods: {
    getIdCardGender(idCard) { return getIdCardGender(idCard) },
    getIdCardAge(idCard) { return getIdCardAge(idCard) },
    async load() { const uid=localStorage.getItem('userId')||'0'; this.loading=true; try{this.profiles=await profileApi.getByUser(uid)}catch{}finally{this.loading=false} },
    goDetail(p) {
      // 如果storage里已经有同id的完整数据（含idCardNo），不覆盖
      try {
        const raw = localStorage.getItem('selProfile')
        if (raw) {
          const existing = JSON.parse(raw)
          if (existing && existing.id === p.id && existing.idCardNo) {
            this.$router.push({name:'profileDetail'})
            return
          }
        }
      } catch {}
      localStorage.setItem('selProfile',JSON.stringify(p)); this.$router.push({name:'profileDetail'})
    },
    goEdit(p) { localStorage.setItem('selProfile',JSON.stringify(p)); this.$router.push({name:'profileEdit'}) },
    addProfile() { localStorage.removeItem('selProfile'); this.$router.push({name:'profileEdit'}) },
    handleDelete(p) { if(confirm('确定删除 '+p.name+' 的就诊人档案？')){this.profiles=this.profiles.filter(x=>x.id!==p.id)} }
  },
  mounted() { this.load() }
}
</script>
