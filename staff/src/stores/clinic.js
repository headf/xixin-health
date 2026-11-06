import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import request from '@/utils/request'

const STORAGE_KEY = 'xixin_clinic'
const CACHE_PREFIX = 'xixin_cache_'
const CACHE_TTL_MS = 60 * 60 * 1000 // 1 小时

/** 从 sessionStorage 恢复持久化数据 */
function loadPersistedState() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch { /* ignore */ }
  return {}
}

/** 写入 sessionStorage */
function saveState(examRecords, checkResults, reports, patients, checkedInList) {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
      examRecords, checkResults, reports, patients, checkedInList
    }))
  } catch { /* quota exceeded etc. */ }
}

/**
 * 从 localStorage 读取缓存，若过期或不存在则返回 null
 */
function readCache(key) {
  try {
    const raw = localStorage.getItem(CACHE_PREFIX + key)
    if (!raw) return null
    const entry = JSON.parse(raw)
    if (Date.now() - entry.timestamp > CACHE_TTL_MS) {
      localStorage.removeItem(CACHE_PREFIX + key)
      return null
    }
    return entry.data
  } catch {
    return null
  }
}

/**
 * 写入 localStorage 缓存（带时间戳）
 */
function writeCache(key, data) {
  try {
    localStorage.setItem(CACHE_PREFIX + key, JSON.stringify({ data, timestamp: Date.now() }))
  } catch { /* quota exceeded etc. */ }
}

export const useClinicStore = defineStore('clinic', () => {
  const saved = loadPersistedState()

  // Exam records from check-in
  const examRecords = ref(saved.examRecords || {})
  const checkResults = ref(saved.checkResults || [])
  const reports = ref(saved.reports || {})
  const patients = ref(saved.patients || {})
  const checkedInList = ref(saved.checkedInList || [])

  /* 硬编码已移除，改用 getItemNamesFromApi() / getDepartmentNamesFromApi() 动态获取 */
  const itemNames = ref({})
  const departmentNames = ref({})

  const reviewStatusLabels = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }

  const abnormalLabels = {
    NORMAL: '正常',
    ABNORMAL: '异常',
    CRITICAL: '危急'
  }

  /**
   * 从后端 API 获取检查项目名称映射表（id -> name），结果缓存到 localStorage 1 小时
   */
  async function getItemNamesFromApi() {
    const cacheKey = 'itemNames'
    const cached = readCache(cacheKey)
    if (cached) {
      itemNames.value = cached
      return cached
    }
    try {
      // 调用 /api/v1/items 获取启用的检查项目列表
      const res = await request.get('/items')
      if (res?.data && Array.isArray(res.data)) {
        const map = {}
        res.data.forEach(item => {
          if (item.id != null && item.itemName) {
            map[item.id] = item.itemName
          }
        })
        itemNames.value = map
        writeCache(cacheKey, map)
        return map
      }
    } catch (e) {
      console.error('获取检查项目名称失败', e)
    }
    return {}
  }

  /**
   * 从后端 API 获取科室名称映射表（id -> name），结果缓存到 localStorage 1 小时
   */
  async function getDepartmentNamesFromApi() {
    const cacheKey = 'departmentNames'
    const cached = readCache(cacheKey)
    if (cached) {
      departmentNames.value = cached
      return cached
    }
    try {
      // 调用 /api/v1/departments 获取科室列表
      const res = await request.get('/departments')
      if (res?.data && Array.isArray(res.data)) {
        const map = {}
        res.data.forEach(dept => {
          // API 返回的字段可能是 name 或 departmentName
          const name = dept.name || dept.departmentName
          if (dept.id != null && name) {
            map[dept.id] = name
          }
        })
        departmentNames.value = map
        writeCache(cacheKey, map)
        return map
      }
    } catch (e) {
      console.error('获取科室名称失败', e)
    }
    return {}
  }

  function addExamRecord(data) {
    if (data?.id) {
      examRecords.value[data.id] = {
        ...examRecords.value[data.id],
        ...data
      }
    }
  }

  function setCheckedInList(list) {
    checkedInList.value = list || []
  }

  function trackResult(result) {
    if (result) {
      const idx = checkResults.value.findIndex(r => r.id === result.id)
      if (idx >= 0) {
        checkResults.value[idx] = result
      } else {
        checkResults.value.push(result)
      }
    }
  }

  function setResults(results) {
    checkResults.value = results || []
  }

  function addReport(data) {
    if (data?.id) {
      reports.value[data.id] = { ...reports.value[data.id], ...data }
    }
  }

  // 数据变化时自动持久化到 sessionStorage，刷新页面后数据不丢
  watch([examRecords, checkResults, reports, patients, checkedInList], () => {
    saveState(examRecords.value, checkResults.value, reports.value, patients.value, checkedInList.value)
  }, { deep: true })

  return {
    examRecords, checkResults, reports, patients, checkedInList,
    itemNames, departmentNames, reviewStatusLabels, abnormalLabels,
    addExamRecord, setCheckedInList, trackResult, setResults, addReport,
    getItemNamesFromApi, getDepartmentNamesFromApi,
  }
})
