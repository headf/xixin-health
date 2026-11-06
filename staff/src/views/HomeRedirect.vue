<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100%;color:var(--n-400);font-size:14px;padding:80px 0">
    <SvgIcon name="refresh" :size="20" style="margin-right:8px" /> 跳转中...
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  router.replace(auth.roleHome)
})
</script>
