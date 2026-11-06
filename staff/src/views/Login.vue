<template>
  <div class="login-page">
    <div class="login-illus">
      <div class="illus-glow-1"></div>
      <div class="illus-glow-2"></div>
      <div class="heartbeat"><svg width="200" height="40" viewBox="0 0 200 40"><path class="heart-path" d="M0 20 L30 20 L40 8 L50 32 L60 20 L90 20 L100 10 L110 30 L120 20 L200 20" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" opacity="0.3"/></svg></div>
      <div class="float-card fc-1"><div class="fc-dot-row"><span class="fc-dot"></span><span class="fc-dot active"></span><span class="fc-dot"></span></div><div class="fc-val"><span class="fc-num">1,284</span><span class="fc-unit">今日预约</span></div><div class="fc-bar-row"><div class="fc-bar" style="height:60%"></div><div class="fc-bar" style="height:85%"></div><div class="fc-bar" style="height:45%"></div><div class="fc-bar" style="height:70%"></div><div class="fc-bar" style="height:90%"></div></div></div>
      <div class="float-card fc-2"><div class="fc-2-row"><span class="fc-2-dot"></span><span>在线客服</span><span class="fc-2-badge">3</span></div><div class="fc-2-row"><span class="fc-2-dot green"></span><span>系统运行</span><span class="fc-2-status">正常</span></div><div class="fc-2-row"><span class="fc-2-dot orange"></span><span>待审核</span><span class="fc-2-badge warn">12</span></div></div>
      <div class="float-card fc-3"><svg width="44" height="44" viewBox="0 0 44 44"><circle cx="22" cy="22" r="18" fill="none" stroke="var(--border)" stroke-width="3"/><circle class="ring-progress" cx="22" cy="22" r="18" fill="none" stroke="var(--primary)" stroke-width="3" stroke-dasharray="85 30" stroke-dashoffset="10" transform="rotate(-90 22 22)"/></svg><div class="fc-pct-info"><span class="fc-pct-val">86%</span><span class="fc-pct-lbl">到检率</span></div></div>
      <div class="particles"><span></span><span></span><span></span><span></span><span></span><span></span><span></span><span></span></div>
      <div class="illus-content">
        <div class="illus-logo"><svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="1.5"><path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg><span>熙心健康</span></div>
        <div class="illus-sub">Smart Health for Everyone</div>
        <div class="typewriter-wrap"><span class="typewriter">专业医护工作平台</span><span class="typewriter-cursor">|</span></div>
      </div>
    </div>
    <div class="login-form-wrap">
      <div class="login-card">
        <div class="login-bar"></div>
        <h2 class="login-welcome">医护工作台</h2>
        <p class="login-sub-title">熙心健康体检管理系统</p>
        <transition name="slide-up">
          <div v-if="errorMsg" class="form-error" role="alert">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" style="margin-right:6px;flex-shrink:0"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
            {{ errorMsg }}
          </div>
        </transition>
        <div class="login-form">
          <div class="field"><label for="loginId">登录账号</label><div class="input-wrap"><svg class="input-icn" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--text-secondary)" stroke-width="1.8"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg><input id="loginId" v-model="form.loginId" class="finput" placeholder="请输入登录账号" @keyup.enter="handleLogin" /></div></div>
          <div class="field"><label for="password">登录密码</label><div class="input-wrap"><svg class="input-icn" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--text-secondary)" stroke-width="1.8"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg><input id="password" v-model="form.password" type="password" class="finput" placeholder="请输入密码" @keyup.enter="handleLogin" /></div></div>
          <label class="remember-row"><input type="checkbox" v-model="form.rememberPwd" class="remember-checkbox" /><span class="remember-label">记住密码</span></label>
          <button class="login-btn" :disabled="loading" @click="handleLogin">
            <span v-if="loading" class="btn-loading"></span>
            {{ loading ? '验证中...' : '登录' }}
          </button>
        </div>
        <div class="login-tips"><p>首次登录请联系管理员获取账号权限</p><p>登录即表示同意 <a>《用户协议》</a> 和 <a>《隐私政策》</a></p></div>
        <div class="login-footer-text">熙心健康 v2.0 · 医护管理系统</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const errorMsg = ref('')
const form = reactive({ loginId: '', password: '', rememberPwd: false })
// 加载记住的密码
function loadRemembered() {
  const savedId = localStorage.getItem('staff_remembered_login')
  const savedPwd = localStorage.getItem('staff_remembered_password')
  if (savedId) { form.loginId = savedId; form.rememberPwd = !!savedPwd; if (savedPwd) form.password = savedPwd }
}
loadRemembered()
async function handleLogin() {
  if (!form.loginId || !form.password) { errorMsg.value = '请输入账号和密码'; return }
  errorMsg.value = ''
  loading.value = true
  try {
    const ok = await auth.doLogin(form.loginId, form.password)
    if (ok) {
      // 记住密码处理
      if (form.rememberPwd) {
        localStorage.setItem('staff_remembered_login', form.loginId)
        localStorage.setItem('staff_remembered_password', form.password)
      } else {
        localStorage.removeItem('staff_remembered_password')
        localStorage.removeItem('staff_remembered_login')
      }
      ElMessage.success('欢迎使用'); router.push('/')
    }
    else { errorMsg.value = '账号不存在或无医护端权限' }
  } catch { errorMsg.value = '登录失败，请检查网络' }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; background: var(--bg); }
.login-illus { flex: 4; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; min-height: 100vh; background: linear-gradient(135deg, #eef2ff 0%, #f0f0ff 35%, #ecf7fe 65%, #f0fdf4 100%); }
.illus-glow-1 { position: absolute; width: 500px; height: 500px; border-radius: 50%; background: radial-gradient(circle, rgba(14,165,233,0.07) 0%, transparent 70%); top: -100px; right: -80px; animation: pulse 6s ease-in-out infinite; }
.illus-glow-2 { position: absolute; width: 400px; height: 400px; border-radius: 50%; background: radial-gradient(circle, rgba(13,148,136,0.05) 0%, transparent 60%); bottom: -120px; left: -100px; animation: pulse 8s ease-in-out infinite; animation-delay: -3s; }
@keyframes pulse { 0%,100%{transform:scale(1);opacity:1} 50%{transform:scale(1.08);opacity:0.7} }
.heartbeat { position: absolute; top: 22%; left: 50%; transform: translateX(-50%); z-index: 2; }
.heart-path { stroke-dasharray: 400; stroke-dashoffset: 400; animation: drawHeart 3s ease-in-out infinite; }
@keyframes drawHeart { 0%{stroke-dashoffset:400} 30%{stroke-dashoffset:0} 60%{stroke-dashoffset:0} 100%{stroke-dashoffset:-400} }
.float-card { position: absolute; z-index: 2; background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); border-radius: var(--radius-lg); padding: 16px; box-shadow: 0 0 0 1px rgba(23,25,28,0.04), 0 8px 24px rgba(0,0,0,0.06); transition: transform 0.3s ease; }
.float-card:hover { transform: translateY(-4px); }
.fc-1 { top: 10%; right: 8%; width: 160px; }
.fc-dot-row { display: flex; gap: 4px; margin-bottom: 10px; }
.fc-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--border-dark); }
.fc-dot.active { background: var(--primary); animation: dotPulse 2s ease-in-out infinite; }
@keyframes dotPulse { 0%,100%{opacity:1} 50%{opacity:0.4} }
.fc-num { font-size: 20px; font-weight: var(--fw-bold); color: var(--title); display: block; }
.fc-unit { font-size: var(--fs-tiny); color: var(--text-secondary); }
.fc-bar-row { display: flex; align-items: flex-end; gap: 4px; height: 40px; }
.fc-bar { flex: 1; background: linear-gradient(to top, var(--primary), #60a5fa); border-radius: 3px 3px 0 0; min-height: 8px; animation: barGrow 1.5s ease-out; }
@keyframes barGrow { from{height:0px} }
.fc-2 { bottom: 22%; left: 6%; width: 160px; }
.fc-2-row { display: flex; align-items: center; gap: 8px; font-size: var(--fs-xs); color: var(--text); margin-bottom: 8px; }
.fc-2-row:last-child { margin-bottom: 0; }
.fc-2-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--primary-light); flex-shrink: 0; }
.fc-2-dot.green { background: #22c55e; animation: dotPulse 2.5s ease-in-out infinite; }
.fc-2-dot.orange { background: var(--warning); }
.fc-2-badge { margin-left: auto; background: var(--primary); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 9999px; font-weight: var(--fw-semibold); }
.fc-2-badge.warn { background: var(--warning); }
.fc-2-status { margin-left: auto; font-size: var(--fs-tiny); color: #22c55e; font-weight: var(--fw-medium); }
.fc-3 { bottom: 42%; right: 6%; width: 130px; display: flex; align-items: center; gap: 12px; }
.ring-progress { animation: ringFill 2s ease-out; }
@keyframes ringFill { from{stroke-dasharray:0 113} }
.fc-pct-val { font-size: 26px; font-weight: var(--fw-bold); color: var(--primary); display: block; }
.fc-pct-lbl { font-size: var(--fs-tiny); color: var(--text-secondary); }
.particles { position: absolute; inset: 0; pointer-events: none; z-index: 1; }
.particles span { position: absolute; border-radius: 50%; background: rgba(14,165,233,0.12); animation: float 12s ease-in-out infinite; }
.particles span:nth-child(1) { width: 8px; height: 8px; top: 15%; left: 20%; animation-delay: 0s; }
.particles span:nth-child(2) { width: 5px; height: 5px; top: 35%; right: 25%; animation-delay: -2s; }
.particles span:nth-child(3) { width: 10px; height: 10px; bottom: 35%; left: 15%; animation-delay: -4s; }
.particles span:nth-child(4) { width: 4px; height: 4px; top: 55%; left: 35%; animation-delay: -6s; }
.particles span:nth-child(5) { width: 6px; height: 6px; bottom: 25%; right: 30%; animation-delay: -3s; }
.particles span:nth-child(6) { width: 7px; height: 7px; top: 20%; right: 15%; animation-delay: -5s; }
.particles span:nth-child(7) { width: 3px; height: 3px; bottom: 45%; left: 30%; animation-delay: -7s; }
.particles span:nth-child(8) { width: 5px; height: 5px; top: 45%; right: 18%; animation-delay: -1s; }
@keyframes float { 0%,100%{transform:translateY(0) scale(1)} 50%{transform:translateY(-20px) scale(1.2)} }
.typewriter-wrap { display: flex; align-items: center; justify-content: center; gap: 2px; margin-top: 24px; }
.typewriter { font-size: 26px; color: var(--text); letter-spacing: 4px; overflow: hidden; white-space: nowrap; animation: typing 4s steps(30) infinite; display: inline-block; width: 0; }
.typewriter-cursor { font-size: 26px; color: var(--primary); animation: blink 0.8s step-end infinite; }
@keyframes typing { 0%,100%{width:0} 30%,70%{width:240px} }
@keyframes blink { 50%{opacity:0} }
.login-form-wrap { flex: 3; min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--card); padding: 48px; }
.login-card { width: 100%; max-width: 400px; }
.login-bar { width: 36px; height: 4px; background: var(--primary); border-radius: 2px; margin-bottom: var(--space-6); }
.login-welcome { font-size: var(--fs-h1); font-weight: var(--fw-light); color: var(--title); letter-spacing: -0.5px; margin-bottom: 6px; }
.login-sub-title { font-size: var(--fs-h4); color: var(--text-secondary); margin-bottom: 36px; }
.form-error { display: flex; align-items: center; padding: 10px 14px; border-radius: var(--radius-base); background: var(--danger-light); color: var(--danger); font-size: var(--fs-sm); margin-bottom: var(--space-4); }
.login-form { display: flex; flex-direction: column; gap: var(--space-5); }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 15px; font-weight: var(--fw-medium); color: var(--text); }
.remember-row { display: flex; align-items: center; gap: 8px; margin: -4px 0 4px 0; cursor: pointer; }
.remember-checkbox { width: 16px; height: 16px; accent-color: var(--primary); cursor: pointer; margin: 0; }
.remember-label { font-size: 13px; color: var(--text-secondary); cursor: pointer; user-select: none; }
.input-wrap { position: relative; }
.input-icn { position: absolute; left: 16px; top: 50%; transform: translateY(-50%); pointer-events: none; }
.input-wrap .finput { width: 100%; height: 48px; padding: 0 16px 0 48px; border: 1.5px solid var(--border); border-radius: var(--radius-base); font-size: var(--fs-h4); color: var(--title); outline: none; background: var(--card); transition: all var(--transition-base); box-sizing: border-box; font-family: var(--font); }
.input-wrap .finput:focus { border-color: var(--primary); box-shadow: 0 0 0 4px rgba(14,165,233,0.1); }
.input-wrap .finput::placeholder { color: var(--text-placeholder); }
.login-btn { width: 100%; height: 50px; border-radius: var(--radius-base); font-size: var(--fs-h3); font-weight: var(--fw-semibold); border: none; cursor: pointer; background: var(--primary); color: #fff; transition: all var(--transition-base); margin-top: 4px; display: flex; align-items: center; justify-content: center; gap: var(--space-2); font-family: var(--font); }
.login-btn:hover { background: var(--primary-dark); box-shadow: 0 4px 16px rgba(14,165,233,0.25); }
.login-btn:active { transform: scale(0.98); }
.login-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-loading { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.slide-up-enter-active { transition: all 0.3s ease-out; }
.slide-up-leave-active { transition: all 0.2s ease-in; }
.slide-up-enter-from { opacity: 0; transform: translateY(12px); }
.slide-up-leave-to { opacity: 0; transform: translateY(-8px); }
.login-tips { margin-top: 28px; }
.login-tips p { font-size: var(--fs-sm); color: var(--text-placeholder); line-height: 2; text-align: center; }
.login-tips a { color: var(--primary); cursor: pointer; }
.login-tips a:hover { text-decoration: underline; }
.login-footer-text { text-align: center; margin-top: 36px; font-size: var(--fs-xs); color: var(--text-placeholder); }
@media (max-width: 900px) { .login-illus { display: none; } .login-form-wrap { flex: 1; padding: 32px 24px; } }
</style>
