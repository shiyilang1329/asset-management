import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { permission } from './directives/permission'
import { useUserStore } from './stores/user'

// 导入自定义主题样式
import './styles/theme.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
app.directive('permission', permission)

const pinia = createPinia()
app.use(pinia)

// 从 localStorage 加载用户信息和权限
const userStore = useUserStore()
userStore.loadUserInfo()

app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
