<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>体检导诊</h2>
        <p class="desc">智能规划最优体检路线</p>
      </div>
    </div>

    <div v-if="loading" class="loading" style="padding:60px 0;text-align:center">加载中...</div>

    <div v-else-if="steps.length === 0" class="empty-state">
      <div class="empty-state-icon"></div>
      <div class="empty-state-title">暂无导诊信息</div>
      <div class="empty-state-desc">签到后即可查看体检导诊路线</div>
    </div>

    <div v-else class="px-20">
      <div class="guide-header">
        <span class="guide-badge">最优路线 · {{ steps.length }}站</span>
      </div>

      <div v-for="(step, i) in steps" :key="i" class="guide-card">
        <div class="step-num">{{ i + 1 }}</div>
        <div class="step-body">
          <div class="step-title">{{ step.title }}</div>
          <div class="step-loc" v-if="step.location"> {{ step.location }}</div>
          <div v-if="step.waiting > 0" class="step-queue">{{ step.waiting }}人排队</div>
          <div v-if="step.hint && step.waiting === 0" class="step-hint">{{ step.hint }}</div>
        </div>
      </div>

      <div style="text-align:center;padding:16px 0 32px;font-size:12px;color:#94a3b8">
        🧠 已用贪心算法优化路线，减少往返距离
      </div>
    </div>
  </div>
</template>

<script>
import { request } from '../../common/api.js'

// ====== 贪心最近邻算法 ======
function parseLocation(loc) {
  if (!loc) return { floor: 99, zone: 0 }
  const f = loc.match(/(\d+)楼/)
  const z = loc.match(/[A-H]/)
  return {
    floor: f ? parseInt(f[1]) : 99,
    zone: z ? z[0].charCodeAt(0) - 65 : 0
  }
}
function dist(a, b) {
  if (!a || !b) return 999
  const floorDiff = Math.abs(a.floor - b.floor) * 10
  const zoneDiff = Math.abs(a.zone - b.zone)
  let cost = floorDiff + zoneDiff
  if (b.fasting) cost -= 3       // 空腹优先
  if (b.waiting > 0) cost += b.waiting * 0.5  // 排队人数越多成本越高
  return cost
}
function greedyRoute(points) {
  if (points.length <= 1) return points
  const visited = new Set(), route = [points[0]]
  visited.add(0)
  let cur = 0
  while (visited.size < points.length) {
    let near = -1, minD = Infinity
    for (let i = 0; i < points.length; i++) {
      if (visited.has(i)) continue
      const d = dist(points[cur].coord, points[i].coord)
      if (d < minD) { minD = d; near = i }
    }
    if (near === -1) break
    route.push(points[near]); visited.add(near); cur = near
  }
  return route
}

export default {
  name: 'PageGuide',
  data() { return { loading: true, steps: [] } },
  async mounted() {
    try {
      const uid = localStorage.getItem('userId') || '0'

      // 1. 获取用户的预约信息（找到最近的有效预约）
      const apps = await request('GET', '/api/v1/appointments/user/' + uid, null, true).catch(() => [])
      const active = Array.isArray(apps) ? apps.filter(a => a.status !== 'CANCELLED' && a.status !== 'COMPLETED') : []
      const appt = active.length > 0 ? active[0] : (Array.isArray(apps) ? apps[0] : null)

      if (!appt) { this.loading = false; return }

      // 2. 获取套餐详情（含是否含总检审核）和检查项目
      let neededDeptIds = new Set()
      let hasReview = false
      if (appt.packageId) {
        // 查套餐是否含总检审核
        const pkgs = await request('GET', '/api/v1/packages', null, false).catch(() => [])
        const myPkg = Array.isArray(pkgs) ? pkgs.find(p => p.id === appt.packageId) : null
        hasReview = myPkg?.hasFinalReview === true

        // 查套餐包含的检查项目
        const items = await request('GET', '/api/v1/packages/' + appt.packageId + '/items', null, false).catch(() => null)
        if (Array.isArray(items)) {
          for (const item of items) {
            if (item.departmentId) neededDeptIds.add(item.departmentId)
          }
        }
      }

      // 3. 获取所有科室 + 排队数据
      const [allDepts, queueStats] = await Promise.all([
        request('GET', '/api/v1/departments', null, false),
        request('GET', '/api/v1/exams/queue/stats/all', null, false).catch(() => ({}))
      ])
      const deptList = Array.isArray(allDepts) ? allDepts : []

      // 4. 过滤出用户需要的科室（如果拿到套餐数据则按套餐过滤，否则显示全部）
      const filtered = neededDeptIds.size > 0
        ? deptList.filter(d => neededDeptIds.has(d.id) && d.enabled !== false)
        : deptList.filter(d => d.enabled !== false)

      // 排队人数映射
      const waitMap = {}
      if (queueStats && typeof queueStats === 'object') {
        for (const [id, stats] of Object.entries(queueStats)) {
          waitMap[id] = stats?.waiting || 0
        }
      }

      // 5. 构建节点 + 贪心排序
      const nodes = [
        { title: '前台签到', location: '一楼大厅', hint: '请先到前台办理签到', coord: { floor: 1, zone: 0 }, fasting: false, waiting: 0 }
      ]
      for (const d of filtered) {
        const waiting = waitMap[d.id] || 0
        nodes.push({
          title: d.departmentName,
          location: d.location || '院内',
          hint: d.fastingRequired ? '需空腹检查' : (waiting > 0 ? waiting + '人排队' : ''),
          coord: parseLocation(d.location),
          fasting: d.fastingRequired || false,
          waiting
        })
      }
      if (hasReview) {
        nodes.push({ title: '总检审核', location: '审核区', hint: '所有项目完成后到总检区由医生综合评审', coord: { floor: 1, zone: 99 }, fasting: false, waiting: 0 })
      }

      const sorted = greedyRoute(nodes)
      this.steps = sorted
    } catch (e) {
      console.error(e)
    } finally {
      this.loading = false
    }
  }
}
</script>

<style scoped>
.guide-header { display:flex;align-items:center;justify-content:space-between;margin:8px 0 16px }
.guide-badge { display:inline-block;padding:4px 14px;background:#E8F8F5;color:#00B894;border-radius:9999px;font-size:13px;font-weight:500 }

.guide-card {
  display:flex;gap:14px;background:#fff;border-radius:14px;
  padding:16px 18px;margin-bottom:12px;
  box-shadow:0 2px 12px rgba(0,0,0,0.04);
  border:1px solid #f1f1f1;
}
.step-num {
  width:32px;height:32px;border-radius:50%;background:#00B894;color:#fff;
  display:flex;align-items:center;justify-content:center;
  font-size:14px;font-weight:600;flex-shrink:0;margin-top:2px
}
.step-body { flex:1 }
.step-title { font-size:15px;font-weight:600;color:#1D2129;margin-bottom:4px }
.step-loc { font-size:13px;color:#4E5969;margin-bottom:2px }
.step-hint { font-size:12px;color:#F53F3F;margin-top:2px }
</style>
