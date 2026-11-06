<template>
  <div class="fade-in" v-if="pkg">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>套餐详情</h2>
        <p class="desc">查看套餐详细内容和价格</p>
      </div>
    </div>

    <div class="dtl-card mx-20 mb-16">
      <div class="dtl-img" :style="{ backgroundImage: 'url(' + bgImg + ')' }">
        <span class="dtl-badge">{{ pkg.icon||'套餐' }}</span>
      </div>
      <div class="dtl-body">
        <div class="dtl-name">{{ pkg.packageName }}</div>
        <div class="dtl-summary">{{ pkg.summary||'' }}</div>
        <div class="dtl-tags">
          <span class="pkg-tag">{{ $cat(pkg.category) }}</span>
          <span class="pkg-tag">{{ $gen(pkg.genderLimit) }}</span>
          <span class="pkg-tag">{{ pkg.ageRange||'不限年龄' }}</span>
        </div>
        <div class="dtl-price">¥{{ $fmt(pkg.priceCent) }}</div>
      </div>
    </div>

    <div class="card mx-20 mb-16">
      <div class="card-hd"><span>包含项目</span><span class="tag tag-blue">{{ items.length }} 项</span></div>
      <div class="card-bd pad-0">
        <div v-if="items.length" class="item-list">
          <div v-for="(item, i) in items" :key="i" class="item-row">
            <span class="item-num">{{ i + 1 }}</span>
            <span class="item-name">{{ item.itemName }}</span>
          </div>
        </div>
        <div v-else class="empty-state" style="padding:20px">
          <span class="empty-text">加载项目信息中...</span>
        </div>
      </div>
    </div>

    <div class="bottom-bar"><button class="dtl-btn" @click="$router.push({name:'appointmentCreate'})">立即预约 · ¥{{ $fmt(pkg.priceCent) }}</button></div>
  </div>

  <div v-else class="empty-state" style="padding-top:60px">
    <div class="empty-title">未选择套餐</div>
    <button class="btn btn-primary" @click="$router.push({name:'packages'})">去选套餐</button>
  </div>
</template>

<script>
import { $fmt, $cat, $gen, request } from '../../common/api.js'
export default {
  name: 'PagePackageDetail',
  data() { return { pkg: null, items: [], bgMap:{} } },
  computed: {
    bgImg() {
      if (!this.pkg) return ''
      const map = { 1: '/images/pkg-basic.svg', 2: '/images/pkg-full.svg', 3: '/images/pkg-compact.svg', 4: '/images/pkg-deep.svg' }
      return map[this.pkg.id] || '/images/pkg-basic.svg'
    }
  },
  methods: {
    $fmt, $cat, $gen,
    async loadItems() {
      if (!this.pkg) return
      try { const data = await request('GET', '/api/v1/packages/' + this.pkg.id + '/items', null, false); this.items = Array.isArray(data) ? data : [] }
      catch { this.items = [] }
    }
  },
  mounted() {
    try{const raw=localStorage.getItem('selPkg');if(raw){this.pkg=JSON.parse(raw);this.loadItems()}}catch{}
  }
}
</script>

<style scoped>
.dtl-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.dtl-img { height: 140px; display: flex; align-items: center; justify-content: center; background-size: cover; background-position: center; }
.dtl-badge { font-size: 52px; filter: drop-shadow(0 2px 6px rgba(0,0,0,0.1)); }
.dtl-body { padding: 16px; display: flex; flex-direction: column; gap: 8px; }
.dtl-name { font-size: 18px; font-weight: 600; color: #1e293b; }
.dtl-summary { font-size: 13px; color: #94a3b8; line-height: 1.5; }
.dtl-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.pkg-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; background: #f1f5f9; color: #64748b; }
.dtl-price { font-size: 28px; font-weight: 700; color: #dc2626; }
.item-list { padding: 8px 0; }
.item-row { display: flex; align-items: center; gap: 12px; padding: 10px 16px; border-bottom: 1px solid #f1f5f9; }
.item-row:last-child { border-bottom: none; }
.item-num { width: 24px; height: 24px; border-radius: 6px; background: #eff6ff; color: #2563eb; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; flex-shrink: 0; }
.item-name { font-size: 14px; color: #475569; flex: 1; }
.dtl-btn { width: 100%; height: 48px; border: none; border-radius: 8px; background: linear-gradient(135deg, #2563eb, #1d4ed8); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.dtl-btn:active { opacity: 0.9; }
</style>
