import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

// 队友的全局样式（订单/总检模块）
import './styles/global.css'
// 医生端体检报告等模块的全局样式（主题变量、卡片、标签）
import './assets/styles/global.css'

const app = createApp(App)

// 全局注册 Element Plus 图标（医生端页面大量使用 <el-icon><Xxx /></el-icon>）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router).use(ElementPlus).mount('#app')
