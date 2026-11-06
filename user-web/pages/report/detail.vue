<template>
  <div class="fade-in" v-if="report">
    <div class="page-hd">
      <div class="page-hd-left"><h2>体检报告</h2><p class="desc">查看体检报告的详细数据与趋势分析</p></div>
      <button class="btn btn-sm btn-outline" @click="downloadPdf" style="margin-top:4px">
        📥 下载 PDF
      </button>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs">
      <div class="tab" :class="{active:activeTab==='detail'}" @click="activeTab='detail'">报告详情</div>
      <div class="tab" :class="{active:activeTab==='trend'}" @click="switchTab('trend')">趋势对比</div>
      <div class="tab" :class="{active:activeTab==='ai'}" @click="switchTab('ai')">AI分析</div>
    </div>

    <!-- ====== 报告详情 ====== -->
    <div v-show="activeTab==='detail'">
      <!-- 报告头部 -->
      <div class="card mx-20 mb-16 text-center" style="padding:24px 20px;border-top:4px solid var(--primary)">
        <div style="font-size:12px;color:var(--primary);letter-spacing:2px;margin-bottom:4px">熙心健康体检中心</div>
        <div style="font-size:20px;font-weight:700;color:var(--title);letter-spacing:4px;margin-bottom:12px">体 检 报 告</div>
        <span :class="riskTagClass(report.abnormalStatus)" style="margin-bottom:8px;display:inline-block">{{ riskLabel(report.abnormalStatus) }}</span>
        <div style="font-size:14px;font-weight:600;color:var(--title);margin-bottom:2px">{{ report.reportNo||'体检报告' }}</div>
        <div class="text-sm text-secondary" v-if="report.publishedAt">{{ report.publishedAt.substring(0,10) }}</div>
      </div>

      <!-- 统计摘要 -->
      <div class="card mx-20 mb-16" v-if="report.stats">
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <span class="stat-chip" style="background:#e8effa;color:#3b82f6">共 {{ report.stats.totalItems||0 }} 项</span>
          <span class="stat-chip" style="background:#d1fae5;color:#059669">正常 {{ report.stats.normalCount||0 }}</span>
          <span class="stat-chip" v-if="report.stats.abnormalCount>0" style="background:#fef3c7;color:#d97706">异常 {{ report.stats.abnormalCount }}</span>
          <span class="stat-chip" v-if="report.stats.criticalCount>0" style="background:#fee2e2;color:#dc2626">危急 {{ report.stats.criticalCount }}</span>
        </div>
      </div>

      <!-- ====== AI 健康风险评估 ====== -->
      <div class="card mx-20 mb-16" v-if="report.healthRiskLevel">
        <div class="card-hd" style="border-left:4px solid #8b5cf6;padding-left:12px">
          <span style="color:#7c3aed"> AI 健康风险评估</span>
        </div>
        <div class="card-bd">
          <div :class="'risk-banner risk-' + report.healthRiskLevel.toLowerCase()">
            <span style="font-size:36px">{{ riskLevelIcon() }}</span>
            <div style="flex:1">
              <div style="font-size:16px;font-weight:700;color:#1e293b;margin-bottom:4px">健康风险等级：{{ riskLevelLabel() }}</div>
              <div style="font-size:13px;color:#64748b;line-height:1.6">{{ report.healthRiskAssessment }}</div>
            </div>
          </div>
          <div v-if="report.diseaseWarnings && report.diseaseWarnings.length > 0" style="margin-top:12px">
            <div v-for="(w, wi) in report.diseaseWarnings" :key="wi" style="display:flex;gap:8px;padding:12px 16px;background:#fef2f2;border-radius:12px;margin-bottom:8px;border-left:4px solid #ef4444">
              <span style="font-size:16px;flex-shrink:0;padding-top:2px">⚠️</span>
              <div style="flex:1">
                <div style="font-size:14px;font-weight:600;color:#dc2626;margin-bottom:2px">{{ w.warning }}</div>
                <div style="font-size:12px;color:#7f1d1d;line-height:1.5">{{ w.suggestion }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 基础信息 -->
      <div class="card mx-20 mb-16" v-if="report.userInfo">
        <div class="card-hd"><span>个人信息</span></div>
        <div class="card-bd">
          <div class="info-row"><span class="info-label">姓名</span><span class="info-value">{{ report.userInfo.name||'-' }}</span><span class="info-label" style="width:60px;padding-left:20px">性别</span><span class="info-value">{{ report.userInfo.gender||'-' }}</span></div>
          <div class="info-row"><span class="info-label">年龄</span><span class="info-value">{{ report.userInfo.age||'-' }}岁</span><span class="info-label" style="width:60px;padding-left:20px">手机</span><span class="info-value">{{ report.userInfo.phone||'-' }}</span></div>
        </div>
      </div>

      <!-- 检查结果 - 按科室 -->
      <div class="card mx-20 mb-16" v-for="(group,gi) in report.departmentGroups||[]" :key="gi">
        <div class="card-hd" style="justify-content:space-between">
          <span>{{ group.departmentName }}</span>
          <span class="text-sm text-secondary">{{ group.items.length }} 项</span>
        </div>
        <div style="overflow-x:auto">
          <table style="width:100%;border-collapse:collapse;font-size:13px">
            <thead><tr style="background:#f8fafc;border-bottom:2px solid #e2e8f0">
              <th style="padding:10px 12px;text-align:left;color:#94a3b8;font-weight:500">检查项目</th>
              <th style="padding:10px 12px;text-align:center;color:#94a3b8;font-weight:500">结果</th>
              <th style="padding:10px 12px;text-align:center;color:#94a3b8;font-weight:500">提示</th>
              <th style="padding:10px 12px;text-align:left;color:#94a3b8;font-weight:500">参考范围</th>
            </tr></thead>
            <tbody>
              <template v-for="item in group.items" :key="item.id">
                <tr :style="item.abnormalLevel==='ABNORMAL'||item.abnormalLevel==='CRITICAL'?'background:#fff8f0':''">
                  <td style="padding:12px;font-weight:500;color:#1e293b;border-bottom:1px solid #f1f5f9">
                    {{ item.itemName }}
                    <span v-if="item.abnormalLevel==='CRITICAL'" style="display:inline-block;font-size:11px;font-weight:600;padding:1px 8px;border-radius:9999px;margin-left:4px;background:#fef2f2;color:#dc2626">危急</span>
                    <span v-else-if="item.abnormalLevel==='ABNORMAL'" style="display:inline-block;font-size:11px;font-weight:600;padding:1px 8px;border-radius:9999px;margin-left:4px;background:#fffbeb;color:#d97706">异常</span>
                  </td>
                  <td style="padding:12px;text-align:center;font-weight:600;border-bottom:1px solid #f1f5f9"
                    :style="{color:item.abnormalLevel==='ABNORMAL'?'#d97706':item.abnormalLevel==='CRITICAL'?'#dc2626':'#1e293b'}">
                    {{ item.numericValue!=null ? item.numericValue : (item.textValue||'-') }}
                  </td>
                  <td style="padding:12px;text-align:center;font-weight:600;border-bottom:1px solid #f1f5f9"
                    :style="{color:item.abnormalLevel==='ABNORMAL'?'#d97706':item.abnormalLevel==='CRITICAL'?'#dc2626':'#94a3b8'}">
                    {{ item.flag||'-' }}
                  </td>
                  <td style="padding:12px;font-size:12px;color:#94a3b8;border-bottom:1px solid #f1f5f9">
                    {{ item.referenceMin!=null ? item.referenceMin+' ~ '+item.referenceMax+' '+(item.unit||'') : (item.unit||'-') }}
                  </td>
                </tr>
                <!-- 异常项风险提示 -->
                <tr v-if="item.riskPrompt" style="background:#fffbeb">
                  <td colspan="4" style="padding:6px 12px 10px 24px;font-size:12px;color:#b45309;line-height:1.5;border-bottom:1px solid #f1f5f9">
                    {{ item.riskPrompt }}
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 诊断结论 -->
      <div class="card mx-20 mb-16" v-if="report.summary||report.doctorAdvice">
        <div class="card-hd"><span>诊断结论</span></div>
        <div class="card-bd" style="gap:12px">
          <div v-if="report.summary" style="border-left:4px solid var(--primary);padding:8px 16px">
            <div style="font-size:12px;color:#64748b;margin-bottom:4px">诊断总结</div>
            <div class="text-sm" style="line-height:1.6;color:var(--text)">{{ report.summary }}</div>
          </div>
          <div v-if="report.doctorAdvice" style="border-left:4px solid #e86868;padding:8px 16px">
            <div style="font-size:12px;color:#64748b;margin-bottom:4px">医生建议</div>
            <div class="text-sm" style="line-height:1.6;color:var(--text)">{{ report.doctorAdvice }}</div>
          </div>
        </div>
      </div>

      <!-- ====== AI 个性化健康建议 ====== -->
      <div class="card mx-20 mb-16" v-if="report.personalizedRecommendations">
        <div class="card-hd" style="border-left:4px solid #8b5cf6;padding-left:12px">
          <span style="color:#7c3aed"> AI 个性化健康建议</span>
        </div>
        <div class="card-bd" style="gap:12px">
          <div v-if="report.personalizedRecommendations.diet && report.personalizedRecommendations.diet.length > 0"
               style="border-left:4px solid #8b5cf6;padding:8px 16px;margin-bottom:8px">
            <div style="font-size:12px;color:#64748b;margin-bottom:4px">饮食建议</div>
            <div v-for="(d, di) in report.personalizedRecommendations.diet" :key="'d-'+di"
                 class="text-sm" style="line-height:1.6;color:var(--text);padding:2px 0">{{ d }}</div>
          </div>
          <div v-if="report.personalizedRecommendations.exercise && report.personalizedRecommendations.exercise.length > 0"
               style="border-left:4px solid #8b5cf6;padding:8px 16px;margin-bottom:8px">
            <div style="font-size:12px;color:#64748b;margin-bottom:4px">运动建议</div>
            <div v-for="(e, ei) in report.personalizedRecommendations.exercise" :key="'e-'+ei"
                 class="text-sm" style="line-height:1.6;color:var(--text);padding:2px 0">{{ e }}</div>
          </div>
          <div v-if="report.personalizedRecommendations.followUp && report.personalizedRecommendations.followUp.length > 0"
               style="border-left:4px solid #e86868;padding:8px 16px">
            <div style="font-size:12px;color:#64748b;margin-bottom:4px">复查建议</div>
            <div v-for="(f, fi) in report.personalizedRecommendations.followUp" :key="'f-'+fi"
                 class="text-sm" style="line-height:1.6;color:var(--text);padding:2px 0">{{ f }}</div>
          </div>
        </div>
      </div>

      <!-- 免责声明 -->
      <div class="card mx-20 mb-16" v-if="report.disclaimer" style="background:#fafafa;padding:16px 20px">
        <div class="text-xs" style="color:#94a3b8;line-height:1.6;text-align:center">{{ report.disclaimer }}</div>
      </div>
    </div>

    <!-- ====== 趋势对比 ====== -->
    <div v-show="activeTab==='trend'">
      <div class="card mx-20 mb-16" style="background:#f0f5ff">
        <div class="text-sm" style="color:#64748b;line-height:1.6">📈 展示历次可比指标变化趋势，帮助跟踪健康动态</div>
      </div>
      <div v-if="trendLoading" class="loading-state"><div class="loading-text">加载中...</div></div>

      <div v-for="(item,idx) in trendItems" :key="item.itemId" class="card mx-20 mb-16">
        <div class="card-hd" style="justify-content:space-between">
          <span>{{ item.itemName }}</span>
          <span class="text-sm text-secondary">{{ item.unit }}</span>
        </div>
        <div v-if="item.referenceMin!=null" class="text-sm text-secondary" style="margin:0 16px 12px">参考范围：{{ item.referenceMin }} ~ {{ item.referenceMax }}</div>
        <div style="padding:8px 16px 16px;position:relative">
          <canvas :id="'trend-chart-'+idx" style="width:100%;height:220px;display:block"></canvas>
        </div>
      </div>
      <div v-if="!trendLoading&&trendItems.length===0" class="card mx-20 mb-16 text-center" style="padding:60px 20px">
        <div style="font-size:40px;margin-bottom:12px"></div>
        <div class="text-sm text-secondary">暂无历史数据对比</div>
      </div>
    </div>

    <!-- ====== AI分析 ====== -->
    <div v-show="activeTab==='ai'">
      <div class="card mx-20 mb-16" style="background:#f0f5ff">
        <div class="text-sm" style="color:#64748b;line-height:1.6"> 基于历次体检数据的智能趋势分析，仅供参考</div>
      </div>
      <div v-if="aiLoading" class="card mx-20 mb-16 text-center" style="padding:48px 20px">
        <div style="font-size:48px;margin-bottom:12px"></div>
        <div style="font-size:16px;font-weight:700;color:var(--title);margin-bottom:20px">AI 智能分析中</div>
        <div style="height:6px;background:#e8edff;border-radius:3px;overflow:hidden;max-width:280px;margin:0 auto 16px">
          <div class="ai-bar-anim"></div>
        </div>
        <div class="text-sm text-secondary">正在分析历次体检数据变化趋势...</div>
      </div>
      <div v-if="!aiLoading&&aiData?.status==='SUCCESS'">
        <div class="card mx-20 mb-16" style="border-top:4px solid var(--primary)">
          <div class="card-hd"><span>{{ aiData.summary?.overallIcon||'' }} 总体健康趋势评估</span></div>
          <div class="card-bd" style="padding-bottom:12px">
            <div class="text-sm" style="line-height:1.7;color:var(--text)">{{ aiData.summary?.overallAssessment }}</div>
            <div style="display:flex;justify-content:space-around;margin-top:16px;padding-top:12px;border-top:1px solid #f1f5f9">
              <div style="text-align:center"><div style="font-size:24px;font-weight:700;color:#059669">{{ aiData.summary?.improvingCount||0 }}</div><div style="font-size:12px;color:#94a3b8">改善</div></div>
              <div style="text-align:center"><div style="font-size:24px;font-weight:700;color:#6b9fff">{{ aiData.summary?.stableCount||0 }}</div><div style="font-size:12px;color:#94a3b8">稳定</div></div>
              <div style="text-align:center"><div style="font-size:24px;font-weight:700;color:#dc2626">{{ aiData.summary?.worseningCount||0 }}</div><div style="font-size:12px;color:#94a3b8">恶化</div></div>
            </div>
          </div>
        </div>
        <div v-for="(item,idx) in aiData.itemAnalyses" :key="idx" class="card mx-20 mb-16">
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
            <span style="font-size:20px">{{ item.trendIcon }}</span>
            <div style="flex:1"><div style="font-size:14px;font-weight:600;color:var(--title)">{{ item.itemName }}</div><div style="font-size:12px;color:#64748b">{{ item.trend }}</div></div>
            <span :style="{background:item.isWorsening?'#fde8e8':'#d1fae5',color:item.isWorsening?'#dc2626':'#059669',padding:'2px 12px',borderRadius:'999px',fontSize:'14px',fontWeight:'700'}">
              {{ item.changePct>0?'+':'' }}{{ item.changePct }}%
            </span>
          </div>
          <div class="text-sm" style="line-height:1.6;color:var(--text);margin-bottom:8px">{{ item.analysis }}</div>
          <div v-if="item.statusChange" style="font-size:12px;font-weight:500;padding:4px 12px;borderRadius:999px;display:inline-block"
            :style="{background:item.statusChange.includes('正常')?'#d1fae5':item.statusChange.includes('新发')?'#fef3c7':'#fee2e2',color:item.statusChange.includes('正常')?'#059669':item.statusChange.includes('新发')?'#d97706':'#dc2626'}">
            {{ item.statusChange }}
          </div>
        </div>
        <!-- 健康建议 -->
        <div class="card mx-20 mb-16" style="border-top:4px solid #10b981;background:#f0fdf4" v-if="aiData?.summary?.healthAdvice">
          <div class="card-hd"><span>健康建议</span></div>
          <div class="card-bd">
            <div class="text-sm" style="line-height:1.7;color:var(--text)">{{ aiData.summary.healthAdvice }}</div>
          </div>
        </div>

        <!-- AI 风险评估 -->
        <div class="card mx-20 mb-16" v-if="aiData?.healthRiskLevel">
          <div class="card-hd" style="border-left:4px solid #8b5cf6;padding-left:12px">
            <span style="color:#7c3aed"> AI 健康风险评估</span>
          </div>
          <div class="card-bd">
            <div :class="'risk-banner risk-' + aiData.healthRiskLevel.toLowerCase()">
              <span style="font-size:36px">{{ riskLevelIcon() }}</span>
              <div style="flex:1">
                <div style="font-size:16px;font-weight:700;color:#1e293b;margin-bottom:4px">健康风险等级：{{ riskLevelLabel() }}</div>
              </div>
            </div>
            <div v-if="aiData?.disclaimer" class="text-xs" style="color:#94a3b8;line-height:1.6;text-align:center;padding:12px 0 0;border-top:1px solid #f1f5f9;margin-top:12px">
              {{ aiData.disclaimer }}
            </div>
          </div>
        </div>

        <!-- AI 个性化建议 -->
        <div class="card mx-20 mb-16" v-if="report?.personalizedRecommendations">
          <div class="card-hd" style="border-left:4px solid #8b5cf6;padding-left:12px">
            <span style="color:#7c3aed"> AI 个性化健康建议</span>
          </div>
          <div class="card-bd" style="gap:12px">
            <div v-if="report.personalizedRecommendations.diet && report.personalizedRecommendations.diet.length > 0"
                 style="border-left:4px solid #8b5cf6;padding:8px 16px;margin-bottom:8px">
              <div style="font-size:12px;color:#64748b;margin-bottom:4px">饮食建议</div>
              <div v-for="(d, di) in report.personalizedRecommendations.diet" :key="'d-'+di"
                   class="text-sm" style="line-height:1.6;color:var(--text);padding:2px 0">{{ d }}</div>
            </div>
            <div v-if="report.personalizedRecommendations.exercise && report.personalizedRecommendations.exercise.length > 0"
                 style="border-left:4px solid #8b5cf6;padding:8px 16px;margin-bottom:8px">
              <div style="font-size:12px;color:#64748b;margin-bottom:4px">运动建议</div>
              <div v-for="(e, ei) in report.personalizedRecommendations.exercise" :key="'e-'+ei"
                   class="text-sm" style="line-height:1.6;color:var(--text);padding:2px 0">{{ e }}</div>
            </div>
            <div v-if="report.personalizedRecommendations.followUp && report.personalizedRecommendations.followUp.length > 0"
                 style="border-left:4px solid #e86868;padding:8px 16px">
              <div style="font-size:12px;color:#64748b;margin-bottom:4px">复查建议</div>
              <div v-for="(f, fi) in report.personalizedRecommendations.followUp" :key="'f-'+fi"
                   class="text-sm" style="line-height:1.6;color:var(--text);padding:2px 0">{{ f }}</div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!aiLoading&&aiData?.status==='NO_DATA'" class="card mx-20 mb-16 text-center" style="padding:60px 20px">
        <div class="empty-icon">·</div><div class="text-sm text-secondary">暂无体检记录</div>
      </div>
    </div>
  </div>

  <div v-else class="empty-state" style="background:#f8fafc">
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" style="margin-bottom:12px"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M16 13H8"/></svg>
    <div class="empty-title" style="color:#94a3b8">未选择报告</div>
  </div>
</template>

<script>
import { reportApi, $toast } from '../../common/api.js'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const CHART_COLORS = {
  normal: { border: '#3b82f6', bg: 'rgba(59,130,246,0.1)', point: '#3b82f6' },
  abnormal: { border: '#f59e0b', bg: 'rgba(245,158,11,0.1)', point: '#f59e0b' },
  critical: { border: '#ef4444', bg: 'rgba(239,68,68,0.1)', point: '#ef4444' },
}

export default {
  name: 'PageReportDetail',
  data() {
    return {
      report: null, healthItems:[], abnormalItems:[],
      activeTab: 'detail', trendLoading: false, trendItems: [],
      trendCharts: [],
      aiLoading: false, aiData: null,
      downloading: false
    }
  },
  mounted() { this.loadDetail() },
  beforeUnmount() { this.destroyCharts() },
  methods: {
    riskLabel(l){const m={NORMAL:'正常',ABNORMAL:'异常',CRITICAL:'危急'};return m[l]||'正常'},
    riskTagClass(l){const m={NORMAL:'tag tag-green',ABNORMAL:'tag tag-orange',CRITICAL:'tag tag-red'};return m[l]||'tag tag-green'},
    riskLevelIcon(){return ({LOW:'🟢',MEDIUM:'🟡',HIGH:'🔴'})[this.report?.healthRiskLevel]||'⚪'},
    riskLevelLabel(){return ({LOW:'低风险',MEDIUM:'中风险',HIGH:'高风险'})[this.report?.healthRiskLevel]||'未知'},
    async loadDetail(){
      try{
        const raw=localStorage.getItem('selReport')
        if(!raw)return
        this.report=JSON.parse(raw)
        this.fetchDetail()
      }catch(e){console.error(e)}
    },
    async fetchDetail(){
      if(!this.report||!this.report.id)return
      try{
        const detail=await reportApi.getDetail(this.report.id)
        if(detail&&detail.departmentGroups){
          this.report={...this.report,...detail}
          return
        }
      }catch(e){}
      if(this.report?.examRecordId){
        try{
          const results=await reportApi.getResults(this.report.examRecordId)
          if(results?.length){
            const items=results.map(r=>({name:r.itemName,value:r.numericValue!=null?r.numericValue:(r.textValue||'-'),unit:r.unit,level:r.abnormalLevel,range:r.referenceMin!=null?r.referenceMin+' ~ '+r.referenceMax+' '+(r.unit||''):(r.unit||'')}))
            this.report.stats={totalItems:items.length,abnormalCount:items.filter(i=>i.level==='ABNORMAL').length,criticalCount:items.filter(i=>i.level==='CRITICAL').length,normalCount:items.filter(i=>i.level==='NORMAL').length}
            this.report.departmentGroups=[{departmentName:'检查项目',items:results.map(r=>({...r,flag:''}))}]
          }
        }catch(e){}
      }
    },
    destroyCharts() {
      this.trendCharts.forEach(c => c.destroy())
      this.trendCharts = []
    },
    renderTrendCharts() {
      this.destroyCharts()
      this.$nextTick(() => {
        this.trendItems.forEach((item, idx) => {
          const canvas = document.getElementById('trend-chart-' + idx)
          if (!canvas) return
          const ctx = canvas.getContext('2d')
          if (!ctx) return
          const vals = item.values || []
          // 判断是否跨多年，跨年显示完整日期 YYYY-MM-DD，否则只显示 MM/DD
          const dates = vals.map(v => v.date || '')
          const hasMultiYear = dates.some(d => d.length >= 4) && new Set(dates.map(d => d.slice(0, 4))).size > 1
          const labels = vals.map(v => v.date ? hasMultiYear ? v.date.slice(0, 10) : v.date.slice(5).replace('-', '/') : '')
          const data = vals.map(v => v.value)
          const colors = vals.map(v => {
            if (v.abnormalLevel === 'CRITICAL') return CHART_COLORS.critical
            if (v.abnormalLevel === 'ABNORMAL') return CHART_COLORS.abnormal
            return CHART_COLORS.normal
          })
          const pointColors = colors.map(c => c.point)
          const hasRef = item.referenceMin != null && item.referenceMax != null
          const gradient = ctx.createLinearGradient(0, 0, 0, 220)
          gradient.addColorStop(0, 'rgba(59,130,246,0.15)')
          gradient.addColorStop(1, 'rgba(59,130,246,0.01)')
          const chart = new Chart(canvas, {
            type: 'line',
            data: {
              labels,
              datasets: [{
                data,
                borderColor: '#3b82f6',
                backgroundColor: gradient,
                borderWidth: 2.5,
                fill: true,
                tension: 0.3,
                pointBackgroundColor: pointColors,
                pointBorderColor: pointColors,
                pointRadius: 5,
                pointHoverRadius: 7,
                pointBorderWidth: 2,
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              animation: { duration: 800, easing: 'easeOutQuart' },
              plugins: {
                legend: { display: false },
                tooltip: {
                  backgroundColor: '#1e293b',
                  titleFont: { size: 12 },
                  bodyFont: { size: 13 },
                  padding: 10,
                  cornerRadius: 8,
                  callbacks: {
                    label: ctx => ctx.parsed.y + (item.unit ? ' ' + item.unit : '')
                  }
                }
              },
              scales: {
                x: {
                  grid: { display: false },
                  ticks: { font: { size: 11 }, color: '#94a3b8' }
                },
                y: {
                  grid: { color: '#f1f5f9', drawBorder: false },
                  ticks: { font: { size: 11 }, color: '#94a3b8' },
                  beginAtZero: false
                }
              }
            },
            plugins: hasRef ? [{
              id: 'refRange',
              beforeDraw(chart) {
                const { ctx: c, chartArea: { left, right, top: ct, bottom } } = chart
                const yMin = chart.scales.y.getPixelForValue(item.referenceMin)
                const yMax = chart.scales.y.getPixelForValue(item.referenceMax)
                if (yMin == null || yMax == null) return
                const yTop = Math.min(yMin, yMax)
                const yBot = Math.max(yMin, yMax)
                const rangeTop = Math.max(ct, yTop)
                const rangeBot = Math.min(bottom, yBot)
                if (rangeTop >= rangeBot) return
                c.save()
                c.fillStyle = 'rgba(34,197,94,0.08)'
                c.fillRect(left, rangeTop, right - left, rangeBot - rangeTop)
                c.strokeStyle = 'rgba(34,197,94,0.25)'
                c.lineWidth = 1
                c.setLineDash([4, 4])
                const midRef = (yMin + yMax) / 2
                if (midRef >= ct && midRef <= bottom) {
                  c.beginPath()
                  c.moveTo(left, yMin)
                  c.lineTo(right, yMin)
                  c.stroke()
                  c.beginPath()
                  c.moveTo(left, yMax)
                  c.lineTo(right, yMax)
                  c.stroke()
                }
                c.restore()
              }
            }] : []
          })
          this.trendCharts.push(chart)
        })
      })
    },
    async switchTab(tab){
      this.activeTab=tab
      if(tab==='trend'){
        if(!this.trendItems.length) await this.loadTrend()
        else this.renderTrendCharts()
      }
      if(tab==='ai'&&!this.aiData)await this.loadAI()
    },
    downloadPdf() {
      if (this.downloading) return
      if (!this.report?.id) { $toast('报告数据未加载'); return }
      this.downloading = true
      try {
        reportApi.downloadPdf(this.report.id)
        setTimeout(() => { this.downloading = false }, 2000)
      } catch (e) {
        console.error('PDF下载失败', e)
        $toast('下载失败', true)
        this.downloading = false
      }
    },
    async loadTrend(){
      this.trendLoading=true
      try{
        const uid=this.report?.userId||localStorage.getItem('userId')
        if(!uid)return
        const data=await reportApi.getTrendByItem(uid)
        this.trendItems=(data||[]).filter(i=>(i.values||[]).length>=2)
        await this.$nextTick()
        this.renderTrendCharts()
      }catch(e){console.error(e)}
      finally{this.trendLoading=false}
    },
    async loadAI(){
      this.aiLoading=true
      try{
        const uid=this.report?.userId||localStorage.getItem('userId')
        if(uid)this.aiData=await reportApi.getTrendAnalysis(uid)||null
      }catch(e){console.error(e);this.aiData=null}
      finally{this.aiLoading=false}
    }
  }
}
</script>

<style scoped>
.risk-banner{display:flex;align-items:center;gap:12px;padding:16px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.06)}
.risk-low{background:#ecfdf5;border:2px solid #a7f3d0}
.risk-medium{background:#fffbeb;border:2px solid #fde68a}
.risk-high{background:#fef2f2;border:2px solid #fecaca}
.tabs{display:flex;background:#fff;border-bottom:1px solid var(--border);margin-bottom:16px}
.tab{flex:1;text-align:center;padding:12px 0;font-size:14px;color:#64748b;cursor:pointer;position:relative}
.tab.active{color:var(--primary);font-weight:600}
.tab.active::after{content:'';position:absolute;bottom:0;left:10%;width:80%;height:2px;background:var(--primary);border-radius:1px}
.stat-chip{padding:4px 14px;border-radius:999px;font-size:12px;font-weight:500}
.loading-state{display:flex;align-items:center;justify-content:center;padding:60px 0}
.loading-text{font-size:14px;color:#94a3b8}
.ai-bar-anim{height:100%;width:0;background:linear-gradient(90deg,#6b9fff,#93b5ff);border-radius:3px;animation:aiBar 2.5s ease-in-out infinite}
@keyframes aiBar{0%{width:0;margin-left:0}50%{width:70%;margin-left:15%}100%{width:100%;margin-left:0}}
</style>
