<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  const saved = localStorage.getItem('xixin_admin_token')
  if (saved) {
    // 动态导入 Pinia store（确保 Pinia 已安装）
    import('@/stores/auth').then(({ useAuthStore }) => {
      const auth = useAuthStore()
      auth.init()
    }).catch(() => {
      // 如果 store 不可用，由 router guard 处理
    })
  }
})
</script>
