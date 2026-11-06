<template>
  <div class="page">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>标本送检</h2>
        <p class="desc">核对标本信息，统一送往检验科化验</p>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="heart" :size="16" style="margin-right:6px;color:var(--blue)" /> 待送检标本</span>
          <span class="tag tag-orange">{{ pendingList.length }}项</span>
        </div>
        <div class="card-bd pad-0">
          <template v-if="pendingList.length">
            <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th class="col-nowrap">标本编号</th><th class="col-nowrap">类型</th><th class="col-nowrap">条码</th><th class="col-nowrap">状态</th><th class="col-nowrap">操作</th></tr></thead>
              <tbody>
                <tr v-for="s in pendingList" :key="s.id">
                  <td style="font-weight:600;font-size:13px">{{ s.specimenNo }}</td>
                  <td><span :class="['tag', s.specimenType==='BLOOD'?'tag-red':'tag-orange']">{{ s.specimenType==='BLOOD'?'血液':'尿液' }}</span></td>
                  <td>{{ s.barcode || '-' }}</td>
                  <td><span class="tag tag-orange">待送检</span></td>
                  <td>
                    <button class="btn btn-sm btn-primary" @click="handleSend(s.id)" :disabled="sendingId === s.id">
                      <SvgIcon name="send" :size="13" /> 送检
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            </div>
          </template>
          <div v-else class="empty-state">
            <SvgIcon name="check" :size="36" style="color:var(--n-300);margin-bottom:12px" />
            <span class="empty-text">所有标本已送检</span>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-hd">
          <span><SvgIcon name="list" :size="16" style="margin-right:6px;color:var(--blue)" /> 送检指引</span>
        </div>
        <div class="card-bd">
          <div class="steps" style="flex-direction:column;align-items:flex-start;gap:8px">
            <div style="display:flex;align-items:center;gap:8px;padding:8px 0">
              <span style="width:24px;height:24px;border-radius:50%;background:var(--blue-light);color:var(--blue);display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600">1</span>
              <span>核对标本条码与体检编号一致</span>
            </div>
            <div style="display:flex;align-items:center;gap:8px;padding:8px 0">
              <span style="width:24px;height:24px;border-radius:50%;background:var(--blue-light);color:var(--blue);display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600">2</span>
              <span>按血液/尿液分类整理装袋</span>
            </div>
            <div style="display:flex;align-items:center;gap:8px;padding:8px 0">
              <span style="width:24px;height:24px;border-radius:50%;background:var(--blue-light);color:var(--blue);display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600">3</span>
              <span>点击「送检」确认送往检验科</span>
            </div>
            <div style="display:flex;align-items:center;gap:8px;padding:8px 0">
              <span style="width:24px;height:24px;border-radius:50%;background:var(--blue-light);color:var(--blue);display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600">4</span>
              <span>检验科上机检测生成数据</span>
            </div>
          </div>
          <div class="divider"></div>
          <button class="btn btn-primary" :disabled="!pendingList.length || sendingAll" @click="handleSendAll" style="width:100%">
            <SvgIcon name="send" :size="16" /> 批量送检（{{ pendingList.length }}项）
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { sendSpecimen, getPendingSpecimens } from '@/api'
import { ElMessage } from 'element-plus'

const pendingList = ref([])
const sendingId = ref(null)
const sendingAll = ref(false)

async function loadPending() {
  try {
    const res = await getPendingSpecimens()
    pendingList.value = res?.data || []
  } catch {}
}

onMounted(() => { loadPending() })

async function handleSend(specimenId) {
  sendingId.value = specimenId
  try {
    await sendSpecimen(specimenId, 1)
    ElMessage.success('标本已送检')
    pendingList.value = pendingList.value.filter(s => s.id !== specimenId)
  } catch { ElMessage.error('送检失败') }
  finally { sendingId.value = null }
}

async function handleSendAll() {
  sendingAll.value = true
  let success = 0
  for (const s of [...pendingList.value]) {
    try {
      await sendSpecimen(s.id, 1)
      pendingList.value = pendingList.value.filter(item => item.id !== s.id)
      success++
    } catch {}
  }
  ElMessage.success(`批量送检完成，成功 ${success} 项`)
  sendingAll.value = false
}
</script>
