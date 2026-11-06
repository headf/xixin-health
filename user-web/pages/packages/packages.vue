<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>体检套餐</h2>
        <p class="desc">选择适合您的体检套餐</p>
      </div>
    </div>

    <div v-if="recommendedPkg" class="alert alert-info" style="margin-bottom:16px">
      根据您的年龄/性别，推荐 <strong>{{ recommendedPkg.packageName }}</strong>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="packages.length===0" class="empty-state">
      <div class="empty-title">暂无套餐</div>
      <div class="empty-desc">敬请期待更多套餐上线</div>
    </div>

    <div v-else class="pkg-grid">
      <div v-for="pkg in packages" :key="pkg.id" class="pkg-card" @click="goDetail(pkg)">
        <div class="pkg-img" :style="{ backgroundImage: 'url(' + bgImg(pkg) + ')' }">
          <span class="pkg-badge">{{ pkg.icon || '套餐' }}</span>
        </div>
        <div class="pkg-body">
          <div class="pkg-name">{{ pkg.packageName }}</div>
          <div class="pkg-summary">{{ pkg.summary||'' }}</div>
          <div class="pkg-tags">
            <span class="pkg-tag">{{ $cat(pkg.category) }}</span>
            <span class="pkg-tag">{{ $gen(pkg.genderLimit) }}</span>
          </div>
          <div class="pkg-price-row">
            <span class="pkg-price">¥{{ $fmt(pkg.priceCent) }}</span>
            <button class="pkg-btn" @click.stop="goDetail(pkg)">选套餐</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { packageApi, $fmt, $cat, $gen } from '../../common/api.js'
export default {
  name: 'PagePackages',
  data() { return { packages:[], loading:true, bgMap:{} } },
  computed: {
    recommendedPkg() {
      if (!this.packages || this.packages.length === 0) return null
      try {
        const profile = JSON.parse(localStorage.getItem('selProfile'))
        if (!profile) return null
        const birthDate = profile.birthDate || profile.birthday
        const gender = profile.gender
        let age = null
        if (birthDate) {
          const bd = new Date(birthDate)
          const today = new Date()
          age = today.getFullYear() - bd.getFullYear()
          const m = today.getMonth() - bd.getMonth()
          if (m < 0 || (m === 0 && today.getDate() < bd.getDate())) age--
        }
        if (age !== null && age >= 40) {
          return this.packages.find(p => p.packageName && p.packageName.includes('深度')) || this.packages[this.packages.length - 1]
        }
        if (gender === 'FEMALE') {
          return this.packages.find(p => p.genderLimit === 'FEMALE' || p.genderLimit === 'ALL') || this.packages[0]
        }
        return this.packages[0]
      } catch {
        return null
      }
    }
  },
  methods: {
    $fmt, $cat, $gen,
    bgImg(pkg) {
      const map = { 1:'pkg-basic.svg', 2:'pkg-full.svg', 3:'pkg-compact.svg', 4:'pkg-deep.svg' }
      return '/images/' + (map[pkg.id] || 'pkg-basic.svg')
    },
    async load() { this.loading=true; try{this.packages=await packageApi.getPackages()}catch{}finally{this.loading=false} },
    goDetail(pkg) { localStorage.setItem('selPkg',JSON.stringify(pkg)); this.$router.push({name:'packageDetail'}) }
  },
  mounted() { this.load() }
}
</script>

<style scoped>
.pkg-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin: 0 16px; }
.pkg-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); cursor: pointer; transition: all 0.15s; display: flex; flex-direction: column; }
.pkg-card:active { transform: scale(0.97); }
.pkg-img { height: 100px; display: flex; align-items: center; justify-content: center; background-size: cover; background-position: center; position: relative; }
.pkg-badge { font-size: 36px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1)); }
.pkg-body { padding: 12px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
.pkg-name { font-size: 14px; font-weight: 600; color: #1e293b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pkg-summary { font-size: 11px; color: #94a3b8; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pkg-tags { display: flex; gap: 4px; flex-wrap: wrap; }
.pkg-tag { font-size: 10px; padding: 1px 6px; border-radius: 4px; background: #f1f5f9; color: #64748b; white-space: nowrap; }
.pkg-price-row { display: flex; align-items: center; justify-content: space-between; margin-top: auto; padding-top: 6px; }
.pkg-price { font-size: 18px; font-weight: 700; color: #dc2626; }
.pkg-btn { height: 28px; padding: 0 14px; border: none; border-radius: 6px; background: #2563eb; color: #fff; font-size: 12px; font-weight: 500; cursor: pointer; }
.pkg-btn:active { opacity: 0.8; }
@media (min-width: 600px) { .pkg-grid { grid-template-columns: 1fr 1fr 1fr; } }
</style>
