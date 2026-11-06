<template>
  <div class="auth-page fade-in">
    <div class="auth-card">
      <h2 class="auth-title">忘记密码</h2>
      <p class="auth-desc">{{ step === 1 ? '输入账号后设置新密码' : '设置新密码' }}</p>

      <!-- Step 1: 输入账号 -->
      <template v-if="step === 1">
        <div class="field"><label>登录账号</label><input v-model="loginId" class="finput" placeholder="请输入手机号或邮箱" @input="error=''" /></div>
        <div v-if="error" class="alert alert-error">{{ error }}</div>
        <button class="btn btn-primary btn-block" style="margin-top:16px;height:44px" @click="goStep2">下一步</button>
      </template>

      <!-- Step 2: 重置密码 -->
      <template v-if="step === 2">
        <div class="field"><label>新密码</label>
          <div class="input-pwd-wrap">
            <input v-model="password" :type="showPwd?'text':'password'" class="finput" placeholder="至少8位" @input="error=''" />
            <span class="eye-toggle" @click="showPwd=!showPwd">{{ showPwd?'隐藏':'显示' }}</span>
          </div>
        </div>
        <div class="field"><label>确认密码</label>
          <input v-model="confirmPassword" type="password" class="finput" placeholder="再次输入新密码" @input="error=''" />
        </div>
        <div v-if="error" class="alert alert-error">{{ error }}</div>
        <button class="btn btn-primary btn-block" style="margin-top:16px;height:44px" :disabled="saving" @click="handleReset">{{ saving?'重置中...':'重置密码' }}</button>
      </template>

      <!-- 成功 -->
      <template v-if="step === 3">
        <div class="success-section">
          <div class="success-icon">✓</div>
          <h3>密码重置成功</h3>
          <p>请使用新密码重新登录</p>
          <button class="btn btn-primary btn-block" style="margin-top:24px;height:44px" @click="$router.push({name:'login'})">返回登录</button>
        </div>
      </template>

      <div class="auth-footer">
        <a @click="$router.push({name:'login'})">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script>
import { request } from '../../common/api.js'
export default {
  name: 'PageForgotPassword',
  data() {
    return {
      step: 1, loginId: '', password: '', confirmPassword: '',
      showPwd: false, saving: false, error: ''
    }
  },
  methods: {
    goStep2() {
      if (!this.loginId.trim()) { this.error = '请输入手机号或邮箱'; return }
      this.error = ''; this.step = 2
    },
    async handleReset() {
      if (!this.password || this.password.length < 8) { this.error = '密码至少8位'; return }
      if (this.password !== this.confirmPassword) { this.error = '两次密码不一致'; return }
      this.saving = true
      try {
        await request('POST', '/api/v1/auth/reset-password', {
          loginId: this.loginId.trim(), newPassword: this.password
        }, false)
        this.step = 3
      } catch (e) { this.error = e.message || '重置失败，请检查账号' }
      finally { this.saving = false }
    }
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--bg); padding: 20px; }
.auth-card { width: 420px; max-width: 100%; background: var(--card); border-radius: 16px; padding: 40px 32px; box-shadow: 0 8px 30px rgba(0,0,0,0.08); }
.auth-title { font-size: 24px; font-weight: 600; color: var(--title); text-align: center; margin-bottom: 6px; }
.auth-desc { font-size: 14px; color: var(--text-secondary); text-align: center; margin-bottom: 24px; }
.field { margin-bottom: 16px; }
.field label { font-weight: 500; font-size: 13px; color: var(--text); margin-bottom: 6px; display: block; }
.finput { width: 100%; height: 40px; padding: 0 12px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; color: var(--title); outline: none; background: #fff; box-sizing: border-box; transition: all 0.2s; }
.finput:focus { border-color: var(--primary); box-shadow: 0 0 0 2px rgba(124,159,255,0.2); }
.input-pwd-wrap { position: relative; }
.input-pwd-wrap .finput { padding-right: 56px; }
.eye-toggle { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); font-size: 12px; color: var(--primary); cursor: pointer; }
.alert { padding: 8px 12px; border-radius: 4px; font-size: 13px; }
.alert-error { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.success-section { text-align: center; padding: 24px 0; }
.success-icon { width: 64px; height: 64px; border-radius: 50%; background: #22c55e; color: #fff; font-size: 32px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; }
.success-section h3 { font-size: 18px; font-weight: 600; color: var(--title); margin-bottom: 8px; }
.success-section p { font-size: 14px; color: var(--text-secondary); }
.auth-footer { text-align: center; margin-top: 24px; }
.auth-footer a { font-size: 14px; color: var(--primary); cursor: pointer; }
</style>
