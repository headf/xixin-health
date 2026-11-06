<template>
  <div class="auth-page fade-in">
    <div class="auth-card">
      <h1 class="auth-title">注册账号</h1>
      <div class="field"><label>手机号</label><input v-model="phone" class="finput" placeholder="请输入手机号" @input="clearError" /></div>
      <div class="field"><label>昵称</label><input v-model="displayName" class="finput" placeholder="请输入昵称" @input="clearError" /></div>
      <div class="field"><label>邮箱 <span class="text-secondary" style="font-weight:400">（选填）</span></label><input v-model="email" class="finput" placeholder="请输入邮箱" /></div>
      <div class="field"><label>身份证号 <span class="text-secondary" style="font-weight:400">（选填）</span></label><input v-model="idCard" class="finput" placeholder="请输入身份证号" maxlength="18" @input="onIdCardInput" /></div>
<div class="field">
  <label>性别</label>
  <input v-model="genderLabel" class="finput" readonly placeholder="输入身份证号后自动识别" />
</div>
<div class="field">
  <label>出生日期</label>
  <input v-model="form.birthDate" class="finput" readonly placeholder="输入身份证号后自动识别" />
</div>
      <div class="field"><label>密码</label><input v-model="password" class="finput" type="password" placeholder="请设置密码（至少8位）" @input="clearError" /></div>
      <div class="field"><label>确认密码</label><input v-model="confirmPassword" class="finput" type="password" placeholder="请再次输入密码" @input="clearError" /></div>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <button class="btn btn-primary btn-block" style="margin-top:16px;height:44px" :disabled="loading" @click="handleRegister">{{ loading?'注册中...':'注册' }}</button>
      <div class="auth-footer">已有账号？<a @click="$router.push({name:'login'})">立即登录</a></div>
    </div>
  </div>
</template>

<script>
import { authApi } from '../../common/api.js'
export default {
  name: 'PageRegister',
  data() { return { phone:'', displayName:'', email:'', idCard:'', password:'', confirmPassword:'', error:'', loading:false, form:{gender:'',birthDate:''} } },
  computed: {
    genderLabel() {
      if (this.form.gender === 'MALE') return '男'
      if (this.form.gender === 'FEMALE') return '女'
      return this.idCard && this.idCard.length >= 18 ? '已自动识别' : ''
    }
  },
  methods: {
    onIdCardInput() {
      this.clearError()
      const card = this.idCard.trim()
      if (card.length >= 18) {
        // 出生日期：第7-14位 (YYYYMMDD)
        const year = card.substring(6, 10)
        const month = card.substring(10, 12)
        const day = card.substring(12, 14)
        if (year && month && day) {
          this.form.birthDate = year + '-' + month + '-' + day
        }
        // 性别：第17位，奇数为男，偶数为女
        const genderDigit = parseInt(card.charAt(16))
        if (!isNaN(genderDigit)) {
          this.form.gender = genderDigit % 2 === 1 ? 'MALE' : 'FEMALE'
        }
      }
    },
    clearError(){this.error=''},
    async handleRegister() {
      if(!this.phone.trim()){this.error='请输入手机号';return}
      if(!/^1\d{10}$/.test(this.phone.trim())){this.error='手机号格式不正确';return}
      if(!this.displayName.trim()){this.error='请输入昵称';return}
      if(!this.password){this.error='请设置密码';return}
      if(this.password.length<8){this.error='密码至少8位';return}
      if(this.password!==this.confirmPassword){this.error='两次密码不一致';return}
      this.loading=true
      try{await authApi.register(this.phone.trim(),this.displayName.trim(),this.password,this.email.trim()||undefined,this.idCard.trim()||undefined,this.form.gender||undefined,this.form.birthDate||undefined);this.$router.push({name:'login'});const t=document.createElement('div');t.className='toast show';t.textContent='注册成功，请登录';document.body.appendChild(t);setTimeout(()=>t.remove(),2500)}
      catch(e){this.error=e.message||'注册失败'}finally{this.loading=false}
    }
  }
}
</script>
