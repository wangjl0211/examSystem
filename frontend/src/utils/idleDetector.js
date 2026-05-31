import { useUserStore } from '@/stores/user'
import router from '@/router'
import { ElMessage } from 'element-plus'

class IdleDetector {
  constructor() {
    this.idleTime = 0 // 无操作时间（分钟）
    this.idleThreshold = 60 // 无操作阈值1小时
    this.lastActivityTime = Date.now()
    this.timer = null
    this.eventTypes = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click', 'dblclick', 'contextmenu', 'focus', 'blur']
    // 绑定上下文，确保 removeEventListener 能正确移除
    this.handleActivity = this.updateActivityTime.bind(this)
  }

  // 初始化无操作检测
  init() {
    // 监听用户操作事件
    this.eventTypes.forEach(eventType => {
      document.addEventListener(eventType, this.handleActivity, true)
    })

    // 启动定时器检测
    this.startTimer()
  }

  // 更新最后操作时间
  updateActivityTime() {
    this.lastActivityTime = Date.now()
    this.idleTime = 0
  }

  // 启动定时器
  startTimer() {
    this.timer = setInterval(() => {
      const currentTime = Date.now()
      const elapsedMinutes = (currentTime - this.lastActivityTime) / (1000 * 60)

      if (elapsedMinutes >= this.idleThreshold) {
        this.handleIdleTimeout()
      }
    }, 60000) // 每分钟检查一次
  }

  // 处理无操作超时
  handleIdleTimeout() {
    console.log('用户1小时无操作，自动注销')
    
    // 清除定时器
    this.clearTimer()
    
    // 清除事件监听器
    this.clearEventListeners()
    
    // 自动注销
    this.logout()
  }

  // 注销操作
  logout() {
    const userStore = useUserStore()
    // 清除本地存储的认证信息
    userStore.resetToken().then(() => {
      // 显示用户友好的提示
      ElMessage({
        message: '您的登录信息已过期，请重新登录',
        type: 'warning',
        duration: 5000
      })
      
      // 跳转到登录页面
      router.replace({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    })
  }

  // 清除定时器
  clearTimer() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  }

  // 清除事件监听器
  clearEventListeners() {
    this.eventTypes.forEach(eventType => {
      document.removeEventListener(eventType, this.handleActivity, true)
    })
  }

  // 销毁实例
  destroy() {
    this.clearTimer()
    this.clearEventListeners()
  }
}

// 导出单例实例
export default new IdleDetector()