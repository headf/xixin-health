<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">体检报告</text>
			<view class="header-spacer"></view>
		</view>

		<!-- Tab 切换 -->
		<view class="tab-bar">
			<view class="tab-item" :class="{ active: activeTab === 'detail' }" @tap="activeTab = 'detail'">报告详情</view>
			<view class="tab-item" :class="{ active: activeTab === 'trend' }" @tap="activeTab = 'trend'">趋势对比</view>
			<view class="tab-item" :class="{ active: activeTab === 'ai' }" @tap="activeTab = 'ai'">AI分析</view>
		</view>

		<!-- ====== 报告详情 Tab ====== -->
		<scroll-view class="scroll" scroll-y v-if="activeTab === 'detail' && detailData">
			<!-- 报告头部 -->
			<view class="report-header">
				<text class="hospital-name">熙心健康体检中心</text>
				<text class="report-title">体 检 报 告</text>
				<view class="report-no-row">
					<text class="report-no-label">报告编号：</text>
					<text class="report-no-value">{{ detailData.reportNo }}</text>
				</view>
				<view class="report-date-row" v-if="detailData.publishedAt">
					<text class="report-date-label">报告日期：</text>
					<text class="report-date-value">{{ detailData.publishedAt.substring(0,10) }}</text>
				</view>
			</view>

			<!-- 总体结论横幅 -->
			<view class="conclusion-banner" :class="'cb-' + (detailData.abnormalStatus || 'NORMAL')">
				<text class="cb-icon">{{ statusIcon }}</text>
				<view class="cb-text">
					<text class="cb-title">{{ abnormalLabel(detailData.abnormalStatus) }}</text>
					<text class="cb-desc">{{ statusDesc }}</text>
				</view>
			</view>

			<!-- 个人信息 -->
			<view class="section">
				<view class="sec-title">个人信息</view>
				<view class="info-table">
					<view class="info-row" v-if="detailData.userInfo">
						<text class="info-cell label">姓　　名</text>
						<text class="info-cell value">{{ detailData.userInfo.name || '-' }}</text>
						<text class="info-cell label">性　　别</text>
						<text class="info-cell value">{{ detailData.userInfo.gender || '-' }}</text>
					</view>
					<view class="info-row" v-if="detailData.userInfo">
						<text class="info-cell label">年　　龄</text>
						<text class="info-cell value">{{ detailData.userInfo.age || '-' }}岁</text>
						<text class="info-cell label">手机号码</text>
						<text class="info-cell value">{{ detailData.userInfo.phone || '-' }}</text>
					</view>
					<view class="info-row" v-if="detailData.userInfo && detailData.userInfo.idCard">
						<text class="info-cell label">证件号码</text>
						<text class="info-cell value" style="-webkit-box-flex:3;flex:3">{{ detailData.userInfo.idCard }}</text>
					</view>
					<view class="info-row" v-if="detailData.packageName">
						<text class="info-cell label">套餐名称</text>
						<text class="info-cell value" style="-webkit-box-flex:3;flex:3">{{ detailData.packageName }}</text>
					</view>
				</view>
			</view>

			<!-- 统计摘要 -->
			<view class="stats-row" v-if="detailData.stats">
				<view class="stat-chip total">共 <text class="stat-num">{{ detailData.stats.totalItems }}</text> 项</view>
				<view class="stat-chip normal" v-if="detailData.stats.normalCount">正常 <text class="stat-num">{{ detailData.stats.normalCount }}</text></view>
				<view class="stat-chip abnormal" v-if="detailData.stats.abnormalCount > 0">异常 <text class="stat-num">{{ detailData.stats.abnormalCount }}</text></view>
				<view class="stat-chip critical" v-if="detailData.stats.criticalCount > 0">危急 <text class="stat-num">{{ detailData.stats.criticalCount }}</text></view>
			</view>

			<!-- 总体健康评估 -->
			<view class="section" v-if="detailData.overallHealthAssessment">
				<view class="sec-title">总体健康评估</view>
				<view class="text-card">
					<text class="text-body">{{ detailData.overallHealthAssessment }}</text>
				</view>
			</view>

			<!-- 检查结果 - 按科室分组 -->
			<view class="section" v-for="(group, gi) in detailData.departmentGroups" :key="gi">
				<view class="sec-title dept-title">
					<text class="dept-name">{{ group.departmentName }}</text>
					<text class="dept-count">{{ group.items.length }} 项</text>
				</view>

				<!-- 表格头 -->
				<view class="result-table">
					<view class="result-thead">
						<text class="th th-item">检查项目</text>
						<text class="th th-result">结果</text>
						<text class="th th-flag">提示</text>
						<text class="th th-ref">参考范围</text>
					</view>

					<!-- 表格行 -->
					<view class="result-tbody">
						<view
							class="result-tr"
							v-for="(item, ri) in group.items"
							:key="item.id"
							:class="{'tr-abnormal': item.abnormalLevel === 'ABNORMAL' || item.abnormalLevel === 'CRITICAL'}"
						>
							<text class="td td-item">{{ item.itemName }}</text>
							<text class="td td-result" :class="'tdr-' + item.abnormalLevel">
								{{ item.numericValue != null ? item.numericValue : (item.textValue || '-') }}
							</text>
							<text class="td td-flag" :class="'tdf-' + item.abnormalLevel">
								{{ item.flag || '-' }}
							</text>
							<text class="td td-ref">
								{{ item.referenceMin != null ? item.referenceMin + ' ~ ' + item.referenceMax + ' ' + (item.unit || '') : (item.unit || '-') }}
							</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 诊断总结 -->
			<view class="section" v-if="detailData.summary">
				<view class="sec-title">诊断总结</view>
				<view class="text-card diag-summary">
					<text class="text-body">{{ detailData.summary }}</text>
				</view>
			</view>

			<!-- 医生建议 -->
			<view class="section" v-if="detailData.doctorAdvice">
				<view class="sec-title sec-title-warn">医生建议</view>
				<view class="text-card diag-advice">
					<text class="text-body">{{ detailData.doctorAdvice }}</text>
				</view>
			</view>

			<!-- 生活方式建议 -->
			<view class="section" v-if="detailData.lifestyleRecommendations">
				<view class="sec-title">生活建议</view>
				<view class="text-card">
					<text class="text-body">{{ detailData.lifestyleRecommendations }}</text>
				</view>
			</view>

			<!-- 复查建议 -->
			<view class="section" v-if="detailData.followUpRecommendations">
				<view class="sec-title sec-title-warn">复查建议</view>
				<view class="text-card diag-advice">
					<text class="text-body">{{ detailData.followUpRecommendations }}</text>
				</view>
				<button class="recheck-btn" @tap="goToPackages">一键预约复查</button>
			</view>

			<!-- 健康分类 -->
			<view class="section" v-if="detailData.classification">
				<view class="sec-title">健康分类</view>
				<view class="classification-tag" :class="'cl-' + detailData.classification">
					{{ classificationLabel(detailData.classification) }}
				</view>
			</view>

			<!-- 医生签名 -->
			<view class="signature-area">
				<view class="sign-line" v-if="detailData.signedAt">
					<text class="sign-label">审核医生：</text>
					<text class="sign-value">（电子签名）</text>
					<text class="sign-date">{{ detailData.signedAt.substring(0,10) }}</text>
				</view>
				<view class="disclaimer">
					<text class="disclaimer-text">本报告仅供健康参考，不作为临床诊断依据</text>
				</view>
			</view>

			<view class="bottom-spacer"></view>
		</scroll-view>

		<!-- ====== 趋势对比 Tab ====== -->
		<scroll-view class="scroll" scroll-y v-if="activeTab === 'trend'">
			<view class="trend-tip">
				<text class="trend-tip-icon">📈</text>
				<text class="trend-tip-text">展示历次可比指标变化趋势，帮助跟踪健康动态</text>
			</view>
			<view class="loading-state" v-if="trendLoading"><text class="loading-text">加载趋势数据...</text></view>
			<view class="empty-trend" v-if="!trendLoading && trendItems.length === 0">
				<text class="empty-icon">📊</text>
				<text class="empty-text">暂无历史数据对比</text>
				<text class="empty-desc">完成多次体检后可查看指标趋势</text>
			</view>
			<view class="trend-list" v-if="!trendLoading && trendItems.length > 0">
				<view class="trend-chart-card" v-for="(item, idx) in trendItems" :key="item.itemId">
					<view class="chart-header">
						<text class="chart-title">{{ item.itemName }}</text>
						<text class="chart-unit" v-if="item.unit">单位: {{ item.unit }}</text>
					</view>
					<view class="chart-ref" v-if="item.referenceMin != null">
						<text class="ref-label">参考范围: {{ item.referenceMin }} ~ {{ item.referenceMax }}</text>
					</view>
					<!-- 折线趋势图 -->
					<view class="line-chart">
						<view class="lc-yaxis">
							<text class="lcy-val">{{ lineYMax(item) }}</text>
							<text class="lcy-val">{{ lineYMid(item) }}</text>
							<text class="lcy-val">{{ lineYMin(item) }}</text>
						</view>
						<view class="lc-body">
							<view class="lc-ref-zone"
								:style="{ top: lineRefTopPct(item) + '%', height: lineRefHeightPct(item) + '%' }"></view>
							<view class="lc-dots-wrap">
								<!-- 连线 -->
								<view v-for="(point, pi) in item.values" :key="'seg-' + pi"
									v-if="pi > 0"
									class="lc-seg"
									:style="lineSegStyle(item, item.values[pi-1], point, pi-1, item.values.length)"></view>
								<!-- 数据点 -->
								<view v-for="(point, pi) in item.values" :key="pi"
									class="lc-dot"
									:style="lineDotPos(item, point, pi, item.values.length)">
									<text class="lc-dot-val">{{ point.value }}</text>
								</view>
							</view>
							<view class="lc-xaxis">
								<text class="lcx-label" v-for="(point, pi) in item.values" :key="'x'+pi"
									:style="{ width: lineXWidth(item.values.length) }">{{ point.date ? point.date.slice(5) : '' }}</text>
							</view>
						</view>
					</view>
					<view class="chart-data-list">
						<view class="data-row" v-for="(point, pi) in item.values" :key="'d-' + pi">
							<text class="data-date">{{ point.date }}</text>
							<text class="data-value" :class="point.abnormalLevel === 'ABNORMAL' || point.abnormalLevel === 'CRITICAL' ? 'dv-abnormal' : 'dv-normal'">
								{{ point.value }} <text class="data-unit">{{ item.unit }}</text>
							</text>
						</view>
					</view>
				</view>
			</view>
			<view class="bottom-spacer"></view>
		</scroll-view>

		<!-- ====== AI 分析 Tab ====== -->
		<scroll-view class="scroll" scroll-y v-if="activeTab === 'ai'">
			<view class="ai-tip">
				<text class="ai-tip-icon">🤖</text>
				<text class="ai-tip-text">基于历次体检数据的智能趋势分析，仅供参考，不构成医疗建议</text>
			</view>

			<!-- AI 加载进度条 -->
			<view class="ai-loading" v-show="aiLoading">
				<view class="ai-loading-card">
					<text class="ai-loading-icon">🤖</text>
					<text class="ai-loading-title">AI 智能分析中</text>
					<view class="ai-progress-track">
						<view class="ai-progress-bar"></view>
					</view>
					<text class="ai-loading-hint">正在分析历次体检数据变化趋势...</text>
				</view>
			</view>

			<!-- 无数据 -->
			<view class="empty-trend" v-show="!aiLoading && aiData && aiData.status === 'NO_DATA'">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无体检记录</text>
			</view>
			<view class="empty-trend" v-show="!aiLoading && aiData && aiData.status === 'INSUFFICIENT_DATA'">
				<text class="empty-icon">📊</text>
				<text class="empty-text">数据不足</text>
				<text class="empty-desc">需要至少 2 次体检记录才能进行对比分析</text>
			</view>

			<!-- AI分析内容 -->
			<view v-show="!aiLoading && aiData && aiData.status === 'SUCCESS'">
				<!-- 总体评估 -->
				<view class="ai-summary-card">
					<view class="ai-summary-header">
						<text class="ai-summary-icon">{{ aiData.summary && aiData.summary.overallIcon || '📊' }}</text>
						<text class="ai-summary-title">总体健康趋势评估</text>
					</view>
					<text class="ai-summary-text">{{ aiData.summary && aiData.summary.overallAssessment }}</text>
					<view class="ai-stats-row">
						<view class="ai-stat">
							<text class="ais-num ais-improve">{{ aiData.summary && aiData.summary.improvingCount || 0 }}</text>
							<text class="ais-label">改善</text>
						</view>
						<view class="ai-stat">
							<text class="ais-num ais-stable">{{ aiData.summary && aiData.summary.stableCount || 0 }}</text>
							<text class="ais-label">稳定</text>
						</view>
						<view class="ai-stat">
							<text class="ais-num ais-worsen">{{ aiData.summary && aiData.summary.worseningCount || 0 }}</text>
							<text class="ais-label">恶化</text>
						</view>
					</view>
				</view>

				<!-- 检查日期 -->
				<view class="ai-dates">
					<text class="ai-dates-label">对比周期：</text>
					<text class="ai-dates-value">{{ aiData.dates?.join('  →  ') || '' }}</text>
				</view>

				<!-- 逐项分析 -->
				<view class="ai-item-card" v-for="(item, idx) in aiData.itemAnalyses" :key="idx">
					<view class="ai-item-header">
						<text class="ai-item-icon">{{ item.trendIcon }}</text>
						<view class="ai-item-info">
							<text class="ai-item-name">{{ item.itemName }}</text>
							<text class="ai-item-trend">{{ item.trend }}</text>
						</view>
						<text class="ai-item-change" :class="item.isWorsening ? 'aic-worsen' : (item.isImproving ? 'aic-improve' : 'aic-stable')">
							{{ item.changePct > 0 ? '+' : '' }}{{ item.changePct }}%
						</text>
					</view>
					<text class="ai-item-analysis">{{ item.analysis }}</text>
					<view class="ai-item-values">
						<text class="aiv-label">上次：{{ item.previousValue }} {{ item.unit || '' }}</text>
						<text class="aiv-arrow">→</text>
						<text class="aiv-label aiv-current">本次：{{ item.currentValue }} {{ item.unit || '' }}</text>
					</view>
					<text class="ai-item-status" v-if="item.statusChange" :class="item.statusChange.includes('好转') || item.statusChange.includes('正常') ? 'ais-good' : 'ais-warn'">
						{{ item.statusChange }}
					</text>
				</view>

				<!-- 健康建议 -->
				<view class="ai-advice">
					<text class="ai-advice-icon">💡</text>
					<text class="ai-advice-text">{{ aiData.summary && aiData.summary.healthAdvice || '建议保持规律作息，均衡饮食，适量运动。对于持续异常的指标，请及时咨询医生。' }}</text>
				</view>
			</view>
			<view class="bottom-spacer"></view>
		</scroll-view>

		<!-- 加载中 -->
		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载报告数据...</text>
		</view>
	</view>
</template>

<script>
import { reportApi } from '../../common/api.js'

export default {
	data() {
		return {
			loading: true,
			trendLoading: false,
			detailData: null,
			trendItems: [],
			activeTab: 'detail',
			userId: null,
			reportId: null,
		}
	},
	computed: {
		abnormalBgClass() {
			return ({ NORMAL: 'bg-normal', ABNORMAL: 'bg-abnormal', CRITICAL: 'bg-critical' })[this.detailData?.abnormalStatus] || 'bg-normal'
		},
		statusIcon() { return ({ NORMAL: '✅', ABNORMAL: '⚠️', CRITICAL: '❗' })[this.detailData?.abnormalStatus] || '📄' },
		statusDesc() {
			return ({ NORMAL: '各项指标均在正常范围内', ABNORMAL: '部分指标异常，建议复查', CRITICAL: '存在危急值，请立即就医' })[this.detailData?.abnormalStatus] || ''
		}
	},
	watch: {
		activeTab(val) {
			if (val === 'trend') this.loadTrend()
			if (val === 'ai') this.loadAI()
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.userId = uni.getStorageSync('userId')
		this.loadDetail()
	},
	methods: {
		async loadDetail() {
			const stored = uni.getStorageSync('selectedReport')
			if (!stored) { this.loading = false; return }

			// Try to fetch detailed report from API first
			if (stored.id) {
				try {
					const data = await reportApi.getDetail(stored.id)
					if (data) {
						this.detailData = data
						// Calculate normalCount for stats
						if (data.stats) {
							data.stats.normalCount = data.stats.totalItems - (data.stats.abnormalCount || 0) - (data.stats.criticalCount || 0)
						}
						this.loading = false
						this.loadTrend()
						this.loadAI()
						return
					}
				} catch (e) { console.log('Detail API not available, using local data', e) }
			}

			// Fallback: build from stored + results
			const detail = { ...stored }
			detail.stats = { totalItems: 0, abnormalCount: 0, criticalCount: 0, normalCount: 0 }
			detail.departmentGroups = []
			if (stored.examRecordId) {
				try {
					const results = await reportApi.getResults(stored.examRecordId)
					if (results && results.length > 0) {
						detail.stats.totalItems = results.length
						const items = results.map(r => {
							if (r.abnormalLevel === 'ABNORMAL') detail.stats.abnormalCount++
							if (r.abnormalLevel === 'CRITICAL') detail.stats.criticalCount++
							let flag = ''
							if (r.abnormalLevel === 'CRITICAL') flag = '‼'
							else if (r.numericValue != null && r.referenceMax != null && r.numericValue > r.referenceMax) flag = '↑'
							else if (r.numericValue != null && r.referenceMin != null && r.numericValue < r.referenceMin) flag = '↓'
							return { ...r, flag }
						})
						detail.stats.normalCount = detail.stats.totalItems - detail.stats.abnormalCount - detail.stats.criticalCount
						detail.departmentGroups.push({ departmentName: '检查项目', items })
					}
				} catch (e2) { console.error('Failed to load results', e2) }
			}
			this.detailData = detail
			this.loading = false
			// 预加载趋势和AI数据
			this.loadTrend()
			this.loadAI()
		},

		loadTrend() {
			if (this.trendItems.length > 0) return
			this.trendLoading = true
			const uid = this.userId || this.detailData?.userId
			if (!uid) { this.trendLoading = false; return }
			reportApi.getTrendByItem(uid).then(data => {
				if (data && data.length > 0) {
					this.trendItems = data.filter(item => (item.values || []).length >= 2)
					this.trendItems.sort((a, b) => (b.values || []).length - (a.values || []).length)
				}
			}).catch(e => { console.error('加载趋势失败', e) })
			.finally(() => { this.trendLoading = false })
		},
		loadAI() {
			const uid = this.userId || this.detailData?.userId
			if (!uid) { this.aiLoading = false; return }
			reportApi.getTrendAnalysis(uid).then(data => {
				this.aiData = data || null
			}).catch(e => {
				console.error('AI分析失败', e)
				uni.showToast({ title: 'AI分析暂不可用', icon: 'none' })
			}).finally(() => {
				this.aiLoading = false
			})
		},

		// 图表辅助
		// 折线图计算
		lineYMax(item) {
			const vals = (item.values || []).map(v => v.value).filter(v => v != null)
			if (vals.length === 0) return 100
			const m = Math.max(...vals) * 1.2
			return m > 10 ? Math.round(m) : m.toFixed(1)
		},
		lineYMin(item) {
			const vals = (item.values || []).map(v => v.value).filter(v => v != null)
			if (vals.length === 0) return 0
			const m = Math.min(...vals) * 0.8
			return m < 0 ? 0 : (m > 10 ? Math.floor(m) : m.toFixed(1))
		},
		lineYMid(item) {
			return ((parseFloat(this.lineYMax(item)) + parseFloat(this.lineYMin(item))) / 2).toFixed(1)
		},
		lineRefTopPct(item) {
			if (item.referenceMax == null) return 0
			const range = parseFloat(this.lineYMax(item)) - parseFloat(this.lineYMin(item)) || 1
			return Math.max(0, Math.min(100, ((parseFloat(this.lineYMax(item)) - item.referenceMax) / range) * 100))
		},
		lineRefHeightPct(item) {
			if (item.referenceMin == null || item.referenceMax == null) return 0
			const range = parseFloat(this.lineYMax(item)) - parseFloat(this.lineYMin(item)) || 1
			return Math.max(5, ((item.referenceMax - item.referenceMin) / range) * 100)
		},
		lineDotPos(item, point, index, total) {
			const vals = (item.values || []).filter(v => v.value != null)
			if (vals.length === 0) return {}
			const range = parseFloat(this.lineYMax(item)) - parseFloat(this.lineYMin(item)) || 1
			const leftPct = total > 1 ? (index / (total - 1)) * 100 : 50
			const bottomPct = ((point.value - parseFloat(this.lineYMin(item))) / range) * 100
			const color = point.abnormalLevel === 'ABNORMAL' || point.abnormalLevel === 'CRITICAL' ? '#e86868' : '#6b9fff'
			return { left: leftPct + '%', bottom: bottomPct + '%', background: color, borderColor: color }
		},
		lineSegStyle(item, prev, curr, index, total) {
			const range = parseFloat(this.lineYMax(item)) - parseFloat(this.lineYMin(item)) || 1
			const x1 = total > 1 ? (index / (total - 1)) * 100 : 0
			const x2 = total > 1 ? ((index + 1) / (total - 1)) * 100 : 100
			const y1 = ((curr.value - parseFloat(this.lineYMin(item))) / range) * 100
			const y2 = ((prev.value - parseFloat(this.lineYMin(item))) / range) * 100
			const dx = x2 - x1
			const dy = y2 - y1
			const len = Math.sqrt(dx*dx + dy*dy)
			const angle = Math.atan2(dy, dx) * 180 / Math.PI
			const color = curr.abnormalLevel === 'ABNORMAL' || curr.abnormalLevel === 'CRITICAL' ? '#e86868' : '#6b9fff'
			return {
				left: 'calc(' + x1 + '% - 2rpx)',
				bottom: 'calc(' + y2 + '% - 2rpx)',
				width: 'calc(' + len + '% + 4rpx)',
				transform: 'rotate(' + angle + 'deg)',
				transformOrigin: '0 0',
				background: color
			}
		},
		lineXWidth(total) {
			return (100 / (total || 1)) + '%'
		},
		barHeight(item, point) {
			const vals = (item.values || []).map(v => v.value).filter(v => v != null)
			if (vals.length === 0) return 50
			const maxVal = Math.max(...vals)
			const minVal = Math.min(...vals)
			const range = maxVal - minVal || 1
			// Map to 15%-100% height range
			return 15 + ((point.value - minVal) / range) * 85
		},

		goBack() { uni.navigateBack() },
		goToPackages() { uni.switchTab({ url: '/pages/packages/packages' }) },
		abnormalLabel(s) { return ({ NORMAL: '正常', ABNORMAL: '异常', CRITICAL: '危急' })[s] || s },
		classificationLabel(s) {
			return ({ NORMAL: '健康', PHYSIOLOGICAL: '生理性变异', SUB_HEALTH: '亚健康', NEED_RECHECK: '建议复查', NEED_TREATMENT: '建议就诊' })[s] || s
		}
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f0f4f8; display: flex; flex-direction: column; padding-top: calc(48px); }

/* 顶部导航 */
.header { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 16rpx 32rpx; background: #ffffff; position: relative; }
.back-btn { font-size: 28rpx; font-weight: 450; color: var(--text, #1e293b); padding: 12rpx 8rpx; }
.back-btn:active { opacity: 0.6; }
.header-title { font-size: 34rpx; font-weight: 700; color: var(--text, #1e293b); position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap; }
.header-spacer { width: 80rpx; }

/* Tab */
.tab-bar {
	display: flex; flex-direction: row; background: #ffffff; border-bottom: 2rpx solid #e2e8f0; padding: 0 32rpx;
}
.tab-item {
	flex: 1; text-align: center; padding: 20rpx 0; font-size: 28rpx; color: #64748b; position: relative;
}
.tab-item.active { color: #6b9fff; font-weight: 600; }
.tab-item.active::after {
	content: ''; position: absolute; bottom: 0; left: 10%; width: 80%; height: 4rpx;
	background: #6b9fff; border-radius: 2rpx;
}

.loading-state { flex: 1; display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 26rpx; color: #64748b; }
.scroll { flex: 1; padding: 24rpx 24rpx; }

/* ====== 报告头部 ====== */
.report-header {
	background: #ffffff; border-radius: 20rpx; padding: 32rpx 24rpx 24rpx;
	text-align: center; margin-bottom: 20rpx; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
	border-top: 8rpx solid #6b9fff;
}
.hospital-name { font-size: 26rpx; color: #6b9fff; font-weight: 500; letter-spacing: 4rpx; display: block; margin-bottom: 8rpx; }
.report-title { font-size: 40rpx; font-weight: 700; color: #1e293b; letter-spacing: 8rpx; display: block; margin-bottom: 20rpx; }
.report-no-row, .report-date-row { display: flex; flex-direction: row; justify-content: center; margin-bottom: 4rpx; }
.report-no-label, .report-date-label { font-size: 24rpx; color: #94a3b8; }
.report-no-value, .report-date-value { font-size: 24rpx; color: #64748b; font-weight: 500; }

/* 结论横幅 */
.conclusion-banner {
	display: flex; flex-direction: row; align-items: center; padding: 24rpx 24rpx;
	border-radius: 20rpx; margin-bottom: 20rpx; gap: 16rpx;
}
.cb-icon { font-size: 40rpx; flex-shrink: 0; }
.cb-text { flex: 1; }
.cb-title { font-size: 30rpx; font-weight: 700; display: block; margin-bottom: 4rpx; }
.cb-desc { font-size: 24rpx; }
.cb-NORMAL { background: #e8effa; }
.cb-NORMAL .cb-title { color: #3b82f6; }
.cb-NORMAL .cb-desc { color: #64748b; }
.cb-ABNORMAL { background: #fdf3e0; }
.cb-ABNORMAL .cb-title { color: #d97706; }
.cb-ABNORMAL .cb-desc { color: #92400e; }
.cb-CRITICAL { background: #fde8e8; }
.cb-CRITICAL .cb-title { color: #dc2626; }
.cb-CRITICAL .cb-desc { color: #991b1b; }

/* ====== 区块通用 ====== */
.section { margin-bottom: 20rpx; }
.sec-title {
	font-size: 26rpx; font-weight: 600; color: #1e293b; margin-bottom: 12rpx;
	padding-left: 16rpx; border-left: 6rpx solid #6b9fff;
}
.sec-title-warn { border-left-color: #e86868; }
.dept-title { display: flex; flex-direction: row; align-items: center; justify-content: space-between; }
.dept-name { font-size: 28rpx; }
.dept-count { font-size: 22rpx; color: #94a3b8; font-weight: 400; }

/* ====== 个人信息表 ====== */
.info-table {
	background: #ffffff; border-radius: 16rpx; padding: 8rpx 0; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.info-row { display: flex; flex-direction: row; border-bottom: 1px solid #f1f5f9; }
.info-row:last-child { border-bottom: none; }
.info-cell { padding: 16rpx 20rpx; font-size: 26rpx; }
.info-cell.label { width: 140rpx; color: #94a3b8; flex-shrink: 0; }
.info-cell.value { flex: 1; color: #1e293b; font-weight: 450; }

/* 统计摘要 */
.stats-row {
	display: flex; flex-direction: row; flex-wrap: wrap; gap: 12rpx; margin-bottom: 20rpx;
}
.stat-chip {
	padding: 8rpx 20rpx; border-radius: 9999px; font-size: 22rpx;
}
.stat-chip.total { background: #e8effa; color: #3b82f6; }
.stat-chip.normal { background: #d1fae5; color: #059669; }
.stat-chip.abnormal { background: #fdf3e0; color: #d97706; }
.stat-chip.critical { background: #fde8e8; color: #dc2626; }
.stat-num { font-weight: 700; font-size: 26rpx; }

/* 文本卡片 */
.text-card {
	background: #ffffff; border-radius: 16rpx; padding: 24rpx 24rpx;
	box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.text-body { font-size: 26rpx; color: #1e293b; line-height: 1.7; }
.diag-summary { border-left: 6rpx solid #6b9fff; }
.diag-advice { border-left: 6rpx solid #e86868; }

/* ====== 检查结果表格 ====== */
.result-table {
	background: #ffffff; border-radius: 16rpx; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.result-thead {
	display: flex; flex-direction: row; background: #f8fafc; padding: 16rpx 0;
	border-bottom: 2rpx solid #e2e8f0;
}
.th { font-size: 22rpx; color: #94a3b8; font-weight: 500; text-align: center; }
.th-item { flex: 2.5; padding-left: 20rpx; text-align: left; }
.th-result { flex: 1.5; }
.th-flag { flex: 0.8; }
.th-ref { flex: 2.5; padding-right: 16rpx; text-align: left; }

.result-tr {
	display: flex; flex-direction: row; padding: 18rpx 0; border-bottom: 1px solid #f1f5f9; align-items: center;
}
.result-tr:last-child { border-bottom: none; }
.tr-abnormal { background: #fff8f0; }
.td { font-size: 26rpx; text-align: center; }
.td-item { flex: 2.5; padding-left: 20rpx; text-align: left; color: #1e293b; }
.td-result { flex: 1.5; font-weight: 600; }
.tdr-NORMAL { color: #1e293b; }
.tdr-ABNORMAL { color: #d97706; }
.tdr-CRITICAL { color: #dc2626; }
.td-flag { flex: 0.8; font-weight: 600; }
.tdf-NORMAL { color: #94a3b8; }
.tdf-ABNORMAL { color: #d97706; }
.tdf-CRITICAL { color: #dc2626; }
.td-ref { flex: 2.5; font-size: 22rpx; color: #94a3b8; padding-right: 16rpx; text-align: left; }

/* 健康分类标签 */
.classification-tag {
	display: inline-block; padding: 12rpx 36rpx; border-radius: 9999px;
	font-size: 28rpx; font-weight: 600; text-align: center;
}
.cl-NORMAL { background: #d1fae5; color: #059669; }
.cl-PHYSIOLOGICAL { background: #dbeafe; color: #2563eb; }
.cl-SUB_HEALTH { background: #fef3c7; color: #d97706; }
.cl-NEED_RECHECK { background: #fde8e8; color: #dc2626; }
.cl-NEED_TREATMENT { background: #fce7f3; color: #db2777; }

/* 复查按钮 */
.recheck-btn {
	margin-top: 16rpx; height: 72rpx; line-height: 72rpx;
	background: #6b9fff; color: #ffffff; border-radius: 9999px;
	font-size: 26rpx; font-weight: 500; text-align: center;
}
.recheck-btn:active { opacity: 0.85; }

/* 签名区 */
.signature-area { margin-top: 32rpx; padding: 24rpx; text-align: center; }
.sign-line { display: flex; flex-direction: row; justify-content: center; gap: 12rpx; margin-bottom: 20rpx; }
.sign-label { font-size: 24rpx; color: #94a3b8; }
.sign-value { font-size: 24rpx; color: #64748b; font-weight: 500; }
.sign-date { font-size: 24rpx; color: #94a3b8; }
.disclaimer { border-top: 1px solid #e2e8f0; padding-top: 16rpx; }
.disclaimer-text { font-size: 20rpx; color: #cbd5e1; }

/* 趋势对比 */
.trend-tip {
	display: flex; flex-direction: row; align-items: center; gap: 12rpx;
	padding: 20rpx 24rpx; background: #f0f5ff; border-radius: 20rpx; margin-bottom: 24rpx;
}
.trend-tip-icon { font-size: 32rpx; }
.trend-tip-text { font-size: 24rpx; color: #64748b; line-height: 1.5; }
.empty-trend { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; }
.empty-trend .empty-icon { font-size: 72rpx; margin-bottom: 20rpx; }
.empty-trend .empty-text { font-size: 28rpx; color: #1e293b; margin-bottom: 8rpx; }
.empty-trend .empty-desc { font-size: 24rpx; color: #94a3b8; }
.trend-list { display: flex; flex-direction: column; gap: 24rpx; }
.trend-chart-card { background: #ffffff; border-radius: 20rpx; padding: 28rpx; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.chart-header { display: flex; flex-direction: row; align-items: center; justify-content: space-between; margin-bottom: 8rpx; }
.chart-title { font-size: 30rpx; font-weight: 700; color: #1e293b; }
.chart-unit { font-size: 22rpx; color: #94a3b8; }
.chart-ref { margin-bottom: 20rpx; }
.chart-ref .ref-label { font-size: 22rpx; color: #64748b; }
.line-chart { display: flex; flex-direction: row; height: 280rpx; padding: 10rpx 0; margin-bottom: 16rpx; }
.lc-yaxis { width: 64rpx; display: flex; flex-direction: column; justify-content: space-between; padding: 0 8rpx 32rpx 0; flex-shrink: 0; }
.lcy-val { font-size: 20rpx; color: #94a3b8; text-align: right; line-height: 1; }
.lc-body { flex: 1; position: relative; border-left: 2rpx solid #e2e8f0; border-bottom: 2rpx solid #e2e8f0; margin-left: 4rpx; }
.lc-ref-zone { position: absolute; left: 0; right: 0; background: rgba(107,159,255,0.1); border-top: 1rpx dashed rgba(107,159,255,0.3); border-bottom: 1rpx dashed rgba(107,159,255,0.3); pointer-events: none; z-index: 0; }
.lc-dots-wrap { position: absolute; inset: 0; z-index: 1; }
.lc-seg { position: absolute; height: 4rpx; border-radius: 2rpx; z-index: 1; transform-origin: 0 0; pointer-events: none; }
.lc-dot { position: absolute; width: 20rpx; height: 20rpx; border-radius: 50%; border: 4rpx solid; margin-left: -10rpx; margin-bottom: -10rpx; z-index: 2; }
.lc-dot-val { position: absolute; top: -32rpx; left: 50%; transform: translateX(-50%); font-size: 20rpx; font-weight: 700; color: #1e293b; white-space: nowrap; }
.lc-xaxis { position: absolute; bottom: -32rpx; left: 0; right: 0; display: flex; flex-direction: row; }
.lcx-label { font-size: 18rpx; color: #94a3b8; text-align: center; }
.chart-data-list { border-top: 2rpx solid #f1f5f9; padding-top: 16rpx; }
.data-row { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 10rpx 0; }
.data-date { font-size: 24rpx; color: #64748b; }
.data-value { font-size: 28rpx; font-weight: 600; }
.dv-normal { color: #6b9fff; }
.dv-abnormal { color: #e86868; }
.data-unit { font-size: 20rpx; font-weight: 400; color: #94a3b8; margin-left: 4rpx; }

.bottom-spacer { height: 60rpx; }

/* ====== AI 分析 ====== */
.ai-loading { padding: 40rpx 0; display: flex; justify-content: center; }
.ai-loading-card { background: #ffffff; border-radius: 24rpx; padding: 48rpx 48rpx; text-align: center; width: 80%; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.ai-loading-icon { font-size: 64rpx; display: block; margin-bottom: 16rpx; }
.ai-loading-title { font-size: 30rpx; font-weight: 700; color: #1e293b; display: block; margin-bottom: 32rpx; }
.ai-progress-track { height: 12rpx; background: #e8edff; border-radius: 6rpx; overflow: hidden; margin-bottom: 24rpx; }
.ai-progress-bar { height: 100%; width: 0; background: linear-gradient(90deg, #6b9fff, #93b5ff); border-radius: 6rpx; animation: aiProgress 2.5s ease-in-out infinite; }
@keyframes aiProgress { 0% { width: 0; margin-left: 0; } 50% { width: 70%; margin-left: 15%; } 100% { width: 100%; margin-left: 0; } }
.ai-loading-hint { font-size: 24rpx; color: #94a3b8; }
.ai-tip { display: flex; flex-direction: row; align-items: center; gap: 12rpx; padding: 20rpx 24rpx; background: #f0f5ff; border-radius: 20rpx; margin-bottom: 24rpx; }
.ai-tip-icon { font-size: 32rpx; flex-shrink: 0; }
.ai-tip-text { font-size: 22rpx; color: #64748b; line-height: 1.5; }
.ai-summary-card { background: #ffffff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 1px 3px rgba(0,0,0,0.06); border-top: 6rpx solid #6b9fff; }
.ai-summary-header { display: flex; flex-direction: row; align-items: center; gap: 12rpx; margin-bottom: 16rpx; }
.ai-summary-icon { font-size: 40rpx; }
.ai-summary-title { font-size: 30rpx; font-weight: 700; color: #1e293b; }
.ai-summary-text { font-size: 28rpx; color: #475569; line-height: 1.7; }
.ai-stats-row { display: flex; flex-direction: row; justify-content: space-around; margin-top: 20rpx; padding-top: 16rpx; border-top: 2rpx solid #f1f5f9; }
.ai-stat { text-align: center; }
.ais-num { font-size: 40rpx; font-weight: 700; display: block; }
.ais-improve { color: #059669; }
.ais-stable { color: #6b9fff; }
.ais-worsen { color: #dc2626; }
.ais-label { font-size: 22rpx; color: #94a3b8; margin-top: 4rpx; }
.ai-dates { background: #f8fafc; border-radius: 16rpx; padding: 16rpx 20rpx; margin-bottom: 20rpx; display: flex; flex-direction: row; align-items: center; gap: 8rpx; }
.ai-dates-label { font-size: 24rpx; color: #94a3b8; }
.ai-dates-value { font-size: 24rpx; color: #1e293b; font-weight: 500; }
.ai-item-card { background: #ffffff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.ai-item-header { display: flex; flex-direction: row; align-items: center; gap: 12rpx; margin-bottom: 12rpx; }
.ai-item-icon { font-size: 36rpx; flex-shrink: 0; }
.ai-item-info { flex: 1; }
.ai-item-name { font-size: 28rpx; font-weight: 600; color: #1e293b; display: block; margin-bottom: 2rpx; }
.ai-item-trend { font-size: 22rpx; color: #64748b; }
.ai-item-change { font-size: 28rpx; font-weight: 700; padding: 4rpx 16rpx; border-radius: 9999px; }
.aic-worsen { background: #fde8e8; color: #dc2626; }
.aic-improve { background: #d1fae5; color: #059669; }
.aic-stable { background: #e8effa; color: #6b9fff; }
.ai-item-analysis { font-size: 26rpx; color: #475569; line-height: 1.6; display: block; margin-bottom: 12rpx; }
.ai-item-values { display: flex; flex-direction: row; align-items: center; gap: 12rpx; padding: 12rpx 16rpx; background: #f8fafc; border-radius: 12rpx; margin-bottom: 10rpx; }
.aiv-label { font-size: 22rpx; color: #64748b; flex: 1; }
.aiv-arrow { font-size: 24rpx; color: #94a3b8; }
.aiv-current { font-weight: 600; color: #1e293b; }
.ai-item-status { font-size: 22rpx; font-weight: 500; padding: 6rpx 16rpx; border-radius: 9999px; display: inline-block; }
.ais-good { background: #d1fae5; color: #059669; }
.ais-warn { background: #fef3c7; color: #d97706; }
.ai-advice { display: flex; flex-direction: row; align-items: flex-start; gap: 12rpx; padding: 24rpx; background: #f0fdf4; border-radius: 20rpx; margin-top: 8rpx; }
.ai-advice-icon { font-size: 32rpx; flex-shrink: 0; }
.ai-advice-text { font-size: 24rpx; color: #475569; line-height: 1.6; flex: 1; }
</style>
