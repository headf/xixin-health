<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>{{ isEdit?'编辑档案':'新增档案' }}</h2>
        <p class="desc">{{ isEdit?'修改就诊人信息':'添加新的就诊人' }}</p>
      </div>
    </div>

    <div class="mx-20" style="padding-bottom:100px">
      <div class="field"><label>姓名 <span class="required-star">*</span></label><input v-model="form.name" class="finput" placeholder="请输入姓名" /></div>
      <div class="field">
        <label>档案类型 <span class="required-star">*</span></label>
        <div class="gender-row">
          <div class="gender-item" :class="{active:form.profileType==='SELF'}" @click="form.profileType='SELF'">本人</div>
          <div class="gender-item" :class="{active:form.profileType==='FAMILY'}" @click="form.profileType='FAMILY'">家属</div>
        </div>
      </div>
      <div class="field"><label>手机号 <span class="required-star">*</span></label><input v-model="form.phone" class="finput" placeholder="请输入11位手机号" maxlength="11" /></div>
      <div class="field"><label>身份证号 <span class="required-star">*</span></label><input v-model="form.idCardNo" class="finput" placeholder="请输入18位身份证号" maxlength="18" /></div>
      <div class="field"><label>紧急联系人 <span class="required-star">*</span></label><input v-model="form.emergencyContactName" class="finput" placeholder="请输入紧急联系人姓名" /></div>
      <div class="field"><label>紧急联系人电话 <span class="required-star">*</span></label><input v-model="form.emergencyContactPhone" class="finput" placeholder="请输入11位手机号" maxlength="11" /></div>
      <div class="field"><label>过敏史</label><textarea v-model="form.allergyHistory" class="ftextarea" placeholder="请输入过敏史（如无请留空）" rows="2"></textarea></div>
      <div class="field"><label>既往病史</label><textarea v-model="form.medicalHistory" class="ftextarea" placeholder="请输入既往病史（如无请留空）" rows="2"></textarea></div>
    </div>

    <div v-if="errorMsg" class="alert alert-error mx-20" style="margin-top:12px">{{ errorMsg }}</div>

    <div style="height:80px"></div>
    <div class="bottom-bar">
      <button class="btn btn-primary profile-save-btn" :disabled="saving" @click="handleSave">{{ saving?'保存中...':'保存' }}</button>
    </div>
  </div>
</template>

<script>
import { profileApi, $toast } from '../../common/api.js'
export default {
  name: 'PageProfileEdit',
  data() {
    return {
      form: { id:null, name:'', profileType:'SELF', phone:'', idCardNo:'', allergyHistory:'', medicalHistory:'', emergencyContactName:'', emergencyContactPhone:'' },
      saving: false, isEdit: false, errorMsg: ''
    }
  },
  methods: {
    async handleSave() {
      this.errorMsg = ''

      // 姓名校验
      if (!this.form.name || !this.form.name.trim()) { this.errorMsg = '请输入姓名'; return }
      // 手机号校验
      const phone = (this.form.phone || '').trim()
      if (!phone) { this.errorMsg = '请输入手机号'; return }
      if (!/^1\d{10}$/.test(phone)) { this.errorMsg = '手机号格式不正确，请输入11位手机号'; return }
      // 身份证号校验
      const idCard = (this.form.idCardNo || '').trim()
      if (!idCard) { this.errorMsg = '请输入身份证号'; return }
      if (!/^\d{17}[\dXx]$/.test(idCard) && !/^\d{15}$/.test(idCard)) { this.errorMsg = '身份证号格式不正确'; return }
      // 紧急联系人校验
      if (!this.form.emergencyContactName || !this.form.emergencyContactName.trim()) { this.errorMsg = '请输入紧急联系人'; return }
      // 紧急联系人电话校验
      const ePhone = (this.form.emergencyContactPhone || '').trim()
      if (!ePhone) { this.errorMsg = '请输入紧急联系人电话'; return }
      if (!/^1\d{10}$/.test(ePhone)) { this.errorMsg = '紧急联系人电话格式不正确'; return }

      this.saving = true
      try {
        const result = await profileApi.save(localStorage.getItem('userId') || '0', {
          ...this.form,
          phone: phone,
          idCardNo: idCard,
          emergencyContactPhone: ePhone
        })
        // 保存成功后，把完整数据存到 selProfile，供详情页直接使用
        if (result && result.id) {
          localStorage.setItem('selProfile', JSON.stringify(result))
        }
        $toast(this.isEdit ? '已更新' : '已创建')
        setTimeout(() => this.$router.back(), 800)
      } catch(e) {
        this.errorMsg = e.message || '保存失败'
      } finally { this.saving = false }
    },
    loadProfile() {
      try {
        const raw = localStorage.getItem('selProfile')
        if (!raw) return
        const data = JSON.parse(raw)
        this.isEdit = true
        // 优先从API加载完整数据
        if (data.id) {
          this.loadFullProfile(data)
        } else {
          Object.assign(this.form, data)
        }
      } catch {}
    },
    async loadFullProfile(fallback) {
      try {
        const detail = await profileApi.getProfileDetail(fallback.id)
        Object.assign(this.form, detail)
        this.isEdit = true
      } catch (e) {
        console.error('加载编辑档案数据失败，使用缓存', e)
        Object.assign(this.form, fallback)
      }
    }
  },
  mounted() { this.loadProfile() }
}
</script>

<style scoped>
.required-star { color: #e53e3e; font-weight: 700; margin-left: 2px; }
.field { margin-bottom: 16px; }
.field label { font-weight: 600; font-size: 13px; color: #334155; margin-bottom: 6px; display: block; }
.gender-row { display: flex; gap: 10px; }
.gender-item {
  flex: 1; height: 40px; line-height: 40px; text-align: center;
  border-radius: 10px; border: 1px solid var(--border); cursor: pointer;
  font-size: 14px; color: var(--text); background: #fff;
}
.gender-item.active { border-color: var(--primary); color: var(--primary); background: var(--primary-light); }
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 12px 20px 20px; background: linear-gradient(to top, #fff 60%, transparent); display: flex; justify-content: center; }
.profile-save-btn { width: auto !important; padding: 0 36px; height: 40px; line-height: 40px; font-size: 14px; border-radius: 20px; }
.alert { padding: 10px 14px; border-radius: 10px; font-size: 13px; }
.alert-error { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.mx-20 { margin-left: 20px; margin-right: 20px; }
</style>
