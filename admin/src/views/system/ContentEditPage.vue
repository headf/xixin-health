<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>内容编辑</h2>
    </div>

    <div class="two-col">
      <el-card shadow="never">
        <template #header>
          <span>关于我们</span>
        </template>
        <el-form :model="aboutForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="aboutForm.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="aboutForm.desc" type="textarea" :rows="3" placeholder="请输入描述" />
          </el-form-item>
          <el-form-item label="特色">
            <el-input v-model="aboutForm.features" type="textarea" :rows="5" placeholder="请输入特色内容，每行一项" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingAbout" @click="handleSaveAbout">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <span>联系方式</span>
        </template>
        <el-form :model="contactForm" label-width="80px">
          <el-form-item label="电话">
            <el-input v-model="contactForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="contactForm.email" placeholder="请输入联系邮箱" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="contactForm.address" type="textarea" :rows="3" placeholder="请输入地址" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingContact" @click="handleSaveContact">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getContent, updateContent } from '@/api'

const savingAbout = ref(false)
const savingContact = ref(false)
const aboutForm = reactive({
  title: '',
  desc: '',
  features: ''
})
const contactForm = reactive({
  phone: '',
  email: '',
  address: ''
})

async function load() {
  try {
    const keys = ['about_title', 'about_desc', 'about_features', 'contact_phone', 'contact_email', 'contact_address']
    const res = await getContent(keys.join(','))
    const map = res.data || {}
    aboutForm.title = map.about_title || ''
    aboutForm.desc = map.about_desc || ''
    aboutForm.features = map.about_features || ''
    contactForm.phone = map.contact_phone || ''
    contactForm.email = map.contact_email || ''
    contactForm.address = map.contact_address || ''
  } catch { ElMessage.error('加载内容失败') }
}

async function handleSaveAbout() {
  savingAbout.value = true
  try {
    await Promise.all([
      updateContent('about_title', aboutForm.title),
      updateContent('about_desc', aboutForm.desc),
      updateContent('about_features', aboutForm.features)
    ])
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
  finally { savingAbout.value = false }
}

async function handleSaveContact() {
  savingContact.value = true
  try {
    await Promise.all([
      updateContent('contact_phone', contactForm.phone),
      updateContent('contact_email', contactForm.email),
      updateContent('contact_address', contactForm.address)
    ])
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
  finally { savingContact.value = false }
}

onMounted(() => load())
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; font-weight: 600; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 1000px) { .two-col { grid-template-columns: 1fr; } }
</style>
