/**
 * 应用入口文件
 * 初始化 Vue 应用，注册全局组件和插件，配置路由守卫
 */
import { createApp } from 'vue'
import 'normalize.css/normalize.css' // CSS重置的现代替代方案
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import '@/styles/index.scss' // 全局样式
import * as echarts from 'echarts'
import App from './App.vue'
import { createPinia } from 'pinia'
import router from './router'
import installIcons from '@/icons' // 图标
import axios from 'axios'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import { connectWebSocket, sendMessage } from '@/utils/websocket'
import { initConfig } from './utils/configLoader'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 如果是开发环境，关闭一些提示
if (import.meta.env.MODE === 'development') {
  console.warn = function(message) {
    if (typeof message === 'string' && !message.includes('DOMNodeInserted')) {
      console.log(message)
    }
  }
}

// 定义白名单
app.config.globalProperties.$echarts = echarts

axios.defaults.withCredentials = true

app.use(ElementPlus, { locale: zhCn })
app.use(installIcons)
app.use(pinia)
app.use(router)

// 导入权限控制，确保在 Pinia 初始化后导入
import '@/permission' // 权限控制

// 导入无操作检测，确保在 Pinia 初始化后导入
import idleDetector from '@/utils/idleDetector'

// 标记是否已初始化无操作检测
let idleDetectorInitialized = false

// 路由守卫，在路由切换前判断是否连接 WebSocket 和启动无操作检测
router.beforeEach((to, from, next) => {
  const isLoginOrRegister = ['login', 'register'].includes(to.name)
  // 页面加载时 不是登录页或注册页 尝试重新连接
  if (!isLoginOrRegister) {
    // WebSocket 由 userStore 登录/getInfo 后统一连接，避免路由重复建连
    // 仅启动无操作检测一次
    if (!idleDetectorInitialized) {
      // 启动无操作检测
      idleDetector.init()
      idleDetectorInitialized = true
    }
  }
  next()
})

// 将 WebSocket 相关方法挂载到 Vue 原型上
app.config.globalProperties.$connectWebSocket = connectWebSocket
app.config.globalProperties.$sendMessage = sendMessage

// 初始化配置并启动应用
initConfig().catch(error => {
  console.warn('配置加载失败，使用默认配置:', error)
})

// 挂载Vue实例
app.mount('#app')

