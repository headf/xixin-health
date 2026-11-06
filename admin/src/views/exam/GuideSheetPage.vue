<template>
  <div class="guide-page">
    <!-- 查询区 -->
    <el-card shadow="never" style="margin-bottom:20px">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon style="margin-right:6px;color:var(--primary)"><Compass /></el-icon>
            导检单
          </span>
          <span class="tag tag-blue">查看体检流程指引</span>
        </div>
      </template>
      <div class="query-bar">
        <el-select v-model="selectedPatient" placeholder="选择已签到患者" filterable clearable style="width:360px" @change="onPatientSelect">
          <el-option v-for="p in checkedInPatients" :key="p.id" :label="`${p.patientName || '患者'} — ${p.examNo || ''}`" :value="p.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="handleQuery" style="margin-left:12px">
          <el-icon style="margin-right:4px"><Search /></el-icon>
          {{ loading ? '查询中...' : '查看导检单' }}
        </el-button>
      </div>
    </el-card>

    <!-- 加载状态 -->
    <el-card v-if="loading" shadow="never">
      <div style="padding:40px 0;text-align:center;color:var(--text-secondary)">
        <el-icon class="is-loading" :size="24" style="margin-bottom:12px"><Loading /></el-icon>
        <div>正在加载导检单...</div>
      </div>
    </el-card>

    <!-- 导检单详情 -->
    <el-card v-else-if="guideSheet" shadow="never">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon style="margin-right:6px;color:var(--primary)"><Document /></el-icon>
            导检单 — {{ guideSheet.patient_name || '患者' }}
          </span>
          <span class="guide-no">编号：{{ guideSheet.guide_no || '-' }}</span>
        </div>
      </template>

      <!-- 路线流程 -->
      <div class="route-section">
        <div class="section-title">
          <el-icon style="margin-right:6px;color:var(--secondary)"><LocationFilled /></el-icon>
          体检路线指引
        </div>
        <div class="route-steps">
          <div v-for="(step, i) in routeSteps" :key="i" class="route-step">
            <div class="step-marker">{{ i + 1 }}</div>
            <div class="step-body">
              <div class="step-dept">{{ step.dept }}</div>
              <div class="step-location">{{ step.location }}</div>
              <div v-if="step.fasting" class="step-tag">需空腹</div>
            </div>
            <div v-if="i < routeSteps.length - 1" class="step-connector"></div>
          </div>
        </div>
      </div>

      <!-- 套餐信息 -->
      <div v-if="guideSheet.package_name" class="pkg-section">
        <div class="section-title">
          <el-icon style="margin-right:6px;color:var(--info)"><Box /></el-icon>
          套餐信息
        </div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="套餐名称">{{ guideSheet.package_name }}</el-descriptions-item>
          <el-descriptions-item label="体检编号">{{ guideSheet.exam_no || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-empty
      v-else-if="!initializing"
      description="选择已签到患者查看导检单"
      :image-size="80"
      style="margin-top:60px"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGuideSheet, getCheckedIn, getDepartments, getAllQueueStats } from '@/api'

const selectedPatient = ref('')
const checkedInPatients = ref([])
const loading = ref(false)
const initializing = ref(true)
const guideSheet = ref(null)
const routeSteps = ref([])

// ====== 最优路线算法：2-opt 改进 + 实时排队权重 ======

/** 将位置字符串转为二维坐标 (楼层, 区域) */
function parseLocation(loc) {
  if (!loc) return { floor: 99, zone: 0 }
  const floorMatch = loc.match(/(\d+)楼/)
  const zoneMatch = loc.match(/区/)
  const zoneLetter = loc.match(/[A-H]/)
  return {
    floor: floorMatch ? parseInt(floorMatch[1]) : 99,
    zone: zoneLetter ? zoneLetter[0].charCodeAt(0) - 65 : (zoneMatch ? 0 : 99)
  }
}

/** 两点间综合成本 = 距离成本 + 排队时间成本 */
function distance(a, b, queueMap = {}) {
  if (!a || !b) return 999
  const floorDiff = Math.abs(a.floor - b.floor)
  const zoneDiff = Math.abs(a.zone - b.zone)
  let cost = floorDiff * 10 + zoneDiff  // 换楼成本 = 10个区域

  // 加入实时排队权重：排队人数多 → 成本增加，引导绕行
  const queueLen = queueMap[b.deptId] || 0
  if (queueLen > 15) cost += 8
  else if (queueLen > 8) cost += 4
  else if (queueLen > 3) cost += 1

  return cost
}

/** 计算一条完整路线的总成本 */
function totalRouteCost(route, queueMap) {
  let cost = 0
  for (let i = 0; i < route.length - 1; i++) {
    cost += distance(route[i].coord, route[i+1].coord, queueMap)
  }
  return cost
}

/** 2-opt 改进：翻转路线中的一段，消除交叉路径 */
function twoOptSwap(route, i, k) {
  const newRoute = route.slice(0, i)
  newRoute.push(...route.slice(i, k + 1).reverse())
  newRoute.push(...route.slice(k + 1))
  return newRoute
}

/**
 * 2-opt 优化求解器
 * 先按空腹优先分组，再各组合并后用 2-opt 优化路径
 * 对于 ≤ 20 个科室，能在毫秒级找到接近最优的解
 */
function optimizeRoute(points, queueMap = {}) {
  if (points.length <= 2) return points

  // 将科室按空腹优先排序：空腹科室先做，非空腹后做
  const start = points[0]
  const rest = points.slice(1)
  const fasting = rest.filter(p => p.fasting)
  const normal = rest.filter(p => !p.fasting)
  // 各小组内按贪心排序
  const sortByGreedy = (list) => {
    if (list.length <= 1) return list
    const visited = new Set()
    const result = []
    let current = start.coord
    while (result.length < list.length) {
      let nearest = -1, minDist = Infinity
      for (let i = 0; i < list.length; i++) {
        if (visited.has(i)) continue
        const d = distance(current, list[i].coord, queueMap)
        if (d < minDist) { minDist = d; nearest = i }
      }
      if (nearest === -1) break
      result.push(list[nearest])
      visited.add(nearest)
      current = list[nearest].coord
    }
    return result
  }

  let route = [start, ...sortByGreedy(fasting), ...sortByGreedy(normal)]

  // Phase 2: 2-opt 改进，但保证空腹科室始终在非空腹之前
  let improved = true
  let bestRoute = [...route]
  let bestCost = totalRouteCost(bestRoute, queueMap)

  while (improved) {
    improved = false
    for (let i = 1; i < bestRoute.length - 1; i++) {
      for (let k = i + 1; k < bestRoute.length; k++) {
        const newRoute = twoOptSwap(bestRoute, i, k)
        // 检查空腹科室是否还在非空腹前面
        let lastFastingIdx = -1, firstNormalIdx = bestRoute.length
        for (let j = 1; j < newRoute.length; j++) {
          if (newRoute[j].fasting) lastFastingIdx = j
          else if (firstNormalIdx === bestRoute.length) firstNormalIdx = j
        }
        // 如果空腹全在非空腹前，再比较成本
        if (lastFastingIdx < firstNormalIdx) {
          const newCost = totalRouteCost(newRoute, queueMap)
          if (newCost < bestCost) {
            bestRoute = newRoute
            bestCost = newCost
          }
        }
      }
    }
  }

  return bestRoute
}

function onPatientSelect(val) {
  if (val) handleQuery()
}

async function handleQuery() {
  if (!selectedPatient.value) { ElMessage.warning('请选择已签到患者'); return }
  loading.value = true
  try {
    const res = await getGuideSheet(selectedPatient.value)
    const data = res?.data
    guideSheet.value = data
    if (data) {
      const [deptResp, queueResp] = await Promise.all([
        getDepartments(),
        getAllQueueStats().catch(() => ({ data: [] }))
      ])
      const deptList = deptResp?.data || []
      const queueData = queueResp?.data || []
      // 构建 部门ID → 排队人数 映射
      const queueMap = {}
      if (Array.isArray(queueData)) {
        queueData.forEach(q => { if (q.departmentId) queueMap[q.departmentId] = q.waitingCount || q.totalWaiting || 0 })
      } else if (typeof queueData === 'object') {
        Object.entries(queueData).forEach(([deptId, info]) => {
          queueMap[deptId] = (info.waitingCount || info.totalWaiting || 0)
        })
      }

      // 1. 构建带坐标和排队数据的节点列表（只显示当前体检套餐包含的科室）
      const deptIds = new Set(data.departmentIds || [])
      const nodes = [
        { name: '前台签到', location: '一楼大厅', desc: '一楼大厅', fasting: false, deptId: null, coord: { floor: 1, zone: 0 } }
      ]
      for (const d of deptList) {
        if (d.enabled === false) continue
        if (!deptIds.has(d.id)) continue  // 只包含当前套餐的科室
        nodes.push({
          name: d.departmentName,
          location: d.location || '院内',
          desc: d.location || '院内',
          fasting: d.fastingRequired || false,
          deptId: d.id,
          coord: parseLocation(d.location)
        })
      }
      nodes.push({ name: '总检审核', location: '审核区', desc: '审核区', fasting: false, deptId: null, coord: { floor: 1, zone: 99 } })

      // 2. 2-opt 优化算法计算最优路线（考虑距离 + 实时排队人数）
      const sorted = optimizeRoute(nodes, queueMap)

      // 3. 映射回前端显示
      routeSteps.value = sorted.map(n => ({
        dept: n.name,
        location: n.desc,
        fasting: n.fasting
      }))
    }
    ElMessage.success('导检单加载成功')
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
  } finally {
    loading.value = false
  }
}

async function loadCheckedIn() {
  try {
    const res = await getCheckedIn()
    checkedInPatients.value = res?.data || []
  } catch (e) { console.error(e) }
  finally { initializing.value = false }
}

onMounted(() => { loadCheckedIn() })
</script>

<style scoped>
.card-header {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 15px; font-weight: var(--fw-semibold);
}
.guide-no { font-size: var(--fs-xs); color: var(--text-secondary); font-weight: var(--fw-regular); }
.query-bar { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; }

.section-title {
  display: flex; align-items: center;
  font-size: var(--fs-body); font-weight: var(--fw-semibold);
  color: var(--title);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border);
  margin-bottom: var(--space-4);
}
.route-section, .pkg-section { margin-bottom: var(--space-5); }

.route-steps { display: flex; flex-direction: column; gap: 0; }
.route-step { display: flex; align-items: stretch; gap: var(--space-4); position: relative; }
.step-marker {
  width: 32px; height: 32px; border-radius: var(--radius-full);
  background: var(--primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: var(--fs-body); font-weight: var(--fw-bold);
  position: relative; z-index: 1; flex-shrink: 0;
}
.step-body {
  flex: 1; padding-bottom: var(--space-5);
  display: flex; align-items: center; gap: var(--space-3); flex-wrap: wrap;
}
.step-dept { font-size: 15px; font-weight: var(--fw-medium); color: var(--title); }
.step-location { font-size: var(--fs-xs); color: var(--text-secondary); }
.step-tag {
  font-size: var(--fs-tiny); padding: 2px 8px; border-radius: var(--radius-pill);
  background: var(--warning-light); color: var(--warning-dark);
  font-weight: var(--fw-medium);
}
.step-connector {
  position: absolute; left: 15px; top: 32px; bottom: 0;
  width: 2px; background: var(--border); z-index: 0;
}
</style>
