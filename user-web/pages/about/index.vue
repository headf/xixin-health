<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>关于我们</h2>
        <p class="desc">了解熙心健康体检管理系统</p>
      </div>
    </div>

    <div class="card mx-20 mb-16" v-if="content">
      <div class="card-hd"><span>{{ content.about_title || '熙心健康' }}</span></div>
      <div class="card-bd" style="gap:12px">
        <div class="avatar-sm flex items-center" style="width:48px;height:48px;border-radius:12px;font-size:20px;background:#f1f5f9;color:#64748b">熙</div>
        <div style="font-size:16px;font-weight:600">{{ content.about_title || '熙心健康体检管理系统' }}</div>
        <div class="text-sm text-secondary" style="line-height:1.7">
          {{ content.about_desc || '熙心健康是一套专业的体检管理平台...' }}
        </div>
      </div>
    </div>

    <div class="card mx-20 mb-16" v-if="featureList.length">
      <div class="card-hd"><span>核心功能</span></div>
      <div class="card-bd" style="gap:10px">
        <div v-for="(f, i) in featureList" :key="i" class="flex gap-12 items-center">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
          <span style="font-size:14px;color:var(--title)">{{ f }}</span>
        </div>
      </div>
    </div>

    <div class="card mx-20 mb-16">
      <div class="card-hd"><span>版本信息</span></div>
      <div class="card-bd" style="gap:0;padding:0">
        <div class="info-row"><span class="info-label">版本号</span><span class="info-value">v1.0.0</span></div>
        <div class="info-row"><span class="info-label">更新日期</span><span class="info-value">2026年6月</span></div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12" style="color:var(--text-secondary);font-size:14px">加载中...</div>
    <div class="text-center py-12 text-sm text-secondary">熙心健康 © 2026 All Rights Reserved.</div>
  </div>
</template>

<script>
import { request } from '../../common/api.js'
export default {
  name: 'PageAbout',
  data() {
    return { content: {}, featureList: [], loading: true }
  },
  async mounted() {
    try {
      const keys = ['about_title', 'about_desc', 'about_features'].join(',')
      const data = await request('GET', `/api/v1/content/batch?keys=${keys}`, null, false)
      if (data) {
        this.content = data
        try { const features = JSON.parse(data.about_features || '[]'); this.featureList = Array.isArray(features) ? features : [] }
        catch { this.featureList = data.about_features ? data.about_features.split('\n') : [] }
      }
    } catch (e) { console.warn('Failed to load content') }
    finally { this.loading = false }
  }
}
</script>
