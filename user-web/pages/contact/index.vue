<template>
  <div class="fade-in">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>联系客服</h2>
        <p class="desc">如有疑问，请随时联系我们</p>
      </div>
    </div>

    <div class="card mx-20 mb-16">
      <div class="card-hd"><span>客服联系方式</span></div>
      <div class="card-bd" style="gap:16px">
        <div v-for="item in contactMethods" :key="item.label" class="flex items-center gap-12" style="padding:8px 0;border-bottom:1px solid var(--border)">
          <div class="flex items-center" style="width:36px;height:36px;border-radius:8px;background:#f1f5f9;flex-shrink:0;justify-content:center">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" v-html="item.icon"></svg>
          </div>
          <div class="flex-1">
            <div style="font-size:14px;font-weight:500;color:var(--title)">{{ item.label }}</div>
            <div class="text-sm text-secondary" style="margin-top:2px">{{ item.value }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="card mx-20 mb-16">
      <div class="card-hd"><span>在线留言</span></div>
      <div class="card-bd">
        <div class="field"><label>您的称呼</label><input v-model="form.name" class="finput" placeholder="请输入姓名" /></div>
        <div class="field"><label>联系方式</label><input v-model="form.phone" class="finput" placeholder="手机号" /></div>
        <div class="field"><label>留言内容</label><textarea v-model="form.content" class="ftextarea" rows="4" placeholder="请描述您的问题或建议..."></textarea></div>
        <button class="btn btn-primary" :disabled="submitting" @click="handleSubmit">{{ submitting?'提交中...':'提交留言' }}</button>
        <div v-if="submitted" class="alert alert-success" style="margin-top:8px">留言已提交，客服回复将通过消息通知发送给您！</div>
      </div>
    </div>

    <div class="card mx-20 mb-16">
      <div class="card-hd"><span>服务时间</span></div>
      <div class="card-bd" style="gap:0;padding:0">
        <div class="info-row"><span class="info-label">工作日</span><span class="info-value">周一至周五 8:00 - 18:00</span></div>
        <div class="info-row"><span class="info-label">周末</span><span class="info-value">周六 8:00 - 12:00</span></div>
        <div class="info-row"><span class="info-label">节假日</span><span class="info-value">另行通知</span></div>
      </div>
    </div>
  </div>
</template>

<script>
import { request, $toast } from '../../common/api.js'
export default {
  name: 'PageContact',
  data() {
    return {
      submitting: false, submitted: false,
      form: { name: '', phone: '', content: '' },
      contactMethods: [
        { label:'客服热线', value:'加载中...', icon:'<path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>' },
        { label:'电子邮件', value:'加载中...', icon:'<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>' },
        { label:'办公地址', value:'加载中...', icon:'<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>' },
      ]
    }
  },
  async mounted() {
    try {
      const keys = ['contact_phone','contact_email','contact_address'].join(',')
      const data = await request('GET', `/api/v1/content/batch?keys=${keys}`, null, false)
      if(data){this.contactMethods=[{label:'客服热线',value:data.contact_phone||'400-100-0001',icon:this.contactMethods[0].icon},{label:'电子邮件',value:data.contact_email||'service@xixin-health.com',icon:this.contactMethods[1].icon},{label:'办公地址',value:data.contact_address||'北京市朝阳区建国路88号',icon:this.contactMethods[2].icon}]}
    } catch(e){this.contactMethods=[{label:'客服热线',value:'400-100-0001',icon:this.contactMethods[0].icon},{label:'电子邮件',value:'service@xixin-health.com',icon:this.contactMethods[1].icon},{label:'办公地址',value:'北京市朝阳区建国路88号',icon:this.contactMethods[2].icon}]}
  },
  methods: {
    async handleSubmit() {
      if(!this.form.name||!this.form.phone||!this.form.content){$toast('请填写完整信息',true);return}
      this.submitting=true
      try{await request('POST','/api/v1/contact',{name:this.form.name,phone:this.form.phone,content:this.form.content,userId:localStorage.getItem('userId')||''},false);this.submitted=true;this.form={name:'',phone:'',content:''};$toast('留言已提交')}
      catch(e){$toast('提交失败',true)}finally{this.submitting=false}
    }
  }
}
</script>
