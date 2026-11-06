import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
import { pinia } from './pinia'
import SvgIcon from './components/SvgIcon.vue'

const app = createApp(App)
app.component('SvgIcon', SvgIcon)
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
