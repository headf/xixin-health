<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>消息通知</h2>
        <p class="desc">查看系统消息和通知</p>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="notifications.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:12px"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
      <div class="empty-title">暂无消息</div>
      <div class="empty-desc">有新消息时将在此显示</div>
    </div>

    <div v-else class="card mx-20">
      <div class="card-bd pad-0">
        <div v-for="n in notifications" :key="n.id" class="list-item" :style="{opacity:n.readFlag?0.6:1}" @click="openDetail(n)">
          <div class="list-row">
            <span class="flex items-center gap-8">
              <span v-if="!n.readFlag" style="width:6px;height:6px;border-radius:50%;background:var(--primary);display:inline-block;flex-shrink:0"></span>
              <span style="font-size:14px;font-weight:500;color:var(--title)">{{ n.title }}</span>
            </span>
            <span class="text-sm text-secondary">{{ fmtTime(n.createdAt) }}</span>
          </div>
          <div class="text-sm text-secondary" style="margin-top:6px;line-height:1.5;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ n.content }}</div>
        </div>
      </div>
    </div>

    <!-- 消息详情弹窗 -->
    <div v-if="detailVisible" class="modal-overlay" @click.self="detailVisible = false">
      <div class="modal-content">
        <div class="modal-header">
          <span style="font-size:16px;font-weight:600;color:var(--title)">{{ detailData?.title }}</span>
          <button class="close-btn" @click="detailVisible = false">&times;</button>
        </div>
        <div class="modal-body">
          <div style="font-size:12px;color:#94a3b8;margin-bottom:12px">{{ fmtTime(detailData?.createdAt) }}</div>
          <div style="font-size:14px;color:#334155;line-height:1.8;white-space:pre-wrap">{{ detailData?.content }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary btn-sm" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PageNotifications',
  data() { return { notifications: [], loading: true, pollTimer: null, detailVisible: false, detailData: null } },
  methods: {
    async load() {
      const uid = localStorage.getItem('userId')
      if (!uid) { this.loading = false; return }
      try {
        const res = await fetch('/api/v1/notifications?userId=' + uid)
        const json = await res.json()
        if (json.code === '0' && Array.isArray(json.data)) {
          this.notifications = json.data
        }
      } catch (e) { /* ignore */ }
      finally { this.loading = false }
    },
    async openDetail(n) {
      this.detailData = n
      this.detailVisible = true
      if (!n.readFlag) {
        try { await fetch('/api/v1/notifications/' + n.id + '/read', { method: 'PATCH' }); n.readFlag = true } catch {}
      }
    },
    fmtTime(t) { if (!t) return ''; try { const d = new Date(t); return `${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` } catch { return t } }
  },
  mounted() { this.load(); this.pollTimer = setInterval(() => { this.load() }, 5000) },
  unmounted() { if (this.pollTimer) clearInterval(this.pollTimer) }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 100;
  display: flex; align-items: center; justify-content: center; padding: 20px;
}
.modal-content {
  background: #fff; border-radius: 16px; width: 100%; max-width: 460px;
  max-height: 80vh; overflow: hidden; display: flex; flex-direction: column;
  box-shadow: 0 16px 48px rgba(0,0,0,0.15);
}
.modal-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  padding: 18px 20px 10px; border-bottom: 1px solid #e2e8f0; gap: 12px;
}
.modal-body {
  padding: 16px 20px 12px; overflow-y: auto; flex: 1;
}
.modal-footer {
  padding: 8px 20px 16px; display: flex; justify-content: flex-end;
}
.close-btn {
  background: none; border: none; font-size: 24px; color: #94a3b8;
  cursor: pointer; padding: 0 4px; line-height: 1; flex-shrink: 0;
}
.close-btn:hover { color: #475569; }
</style>
