<template>
  <span v-if="!loading" style="color: #ff0000; font-weight: 700">{{ min }}分钟{{ sec }}秒</span>
  <span v-else style="color: #999; font-weight: 400">加载中...</span>
</template>

<script>
import { getServerTime } from '@/api/exam'

export default {
  name: 'ExamTimer',
  props: {
    // 考试开始时间的时间戳（毫秒）
    startTime: {
      type: Number,
      required: true
    },
    // 考试时长（秒）
    duration: {
      type: Number,
      required: true
    },
    // 时间同步间隔（毫秒），默认30秒同步一次
    syncInterval: {
      type: Number,
      default: 30000
    }
  },
  emits: ['timeout', 'tick', 'update:leftSeconds'],
  data() {
    return {
      leftSeconds: 0,
      min: '00',
      sec: '00',
      timer: null,
      syncTimer: null,
      visibilityHandler: null,
      // 服务器时间偏移量（服务器时间 - 本地时间）
      serverTimeOffset: 0,
      // 是否已同步
      isSynced: false,
      // 是否正在加载
      loading: true,
      // 记录上次计算时间，用于精确计时
      lastCalculatedTime: 0
    }
  },
  computed: {
    // 计算考试结束时间（毫秒）
    endTime() {
      return this.startTime + this.duration * 1000
    }
  },
  watch: {
    // 监听开始时间或时长变化，重新计算剩余时间
    startTime() {
      this.recalculateLeftSeconds()
    },
    duration() {
      this.recalculateLeftSeconds()
    }
  },
  async created() {
    console.log('=== ExamTimer组件创建（服务器时间模式）===')
    console.log('考试开始时间:', this.startTime)
    console.log('考试时长:', this.duration, '秒')
    
    // 同步服务器时间
    await this.syncServerTime()
    
    // 计算初始剩余时间
    this.recalculateLeftSeconds()
    
    // 隐藏loading
    this.loading = false
    
    // 启动定时器
    this.startCountdown()
    
    // 启动定期同步
    this.startSyncTimer()
    
    // 监听页面可见性变化
    this.setupVisibilityListener()
  },
  beforeUnmount() {
    // 组件销毁时清除所有定时器和监听器
    this.cleanup()
  },
  methods: {
    /**
     * 同步服务器时间
     * 计算本地时间与服务器时间的偏移量
     */
    async syncServerTime() {
      try {
        const localTimeBefore = Date.now()
        const res = await getServerTime()
        const localTimeAfter = Date.now()
        
        if (res.code) {
          const serverTime = res.data
          // 使用请求中间时间点作为本地时间参考，减少网络延迟影响
          const localTime = (localTimeBefore + localTimeAfter) / 2
          
          // 计算偏移量：服务器时间 - 本地时间
          this.serverTimeOffset = serverTime - localTime
          this.isSynced = true
          
          console.log('[时间同步] 服务器时间:', new Date(serverTime).toLocaleTimeString())
          console.log('[时间同步] 本地时间:', new Date(localTime).toLocaleTimeString())
          console.log('[时间同步] 偏移量:', this.serverTimeOffset, 'ms')
          console.log('[时间同步] 网络延迟:', localTimeAfter - localTimeBefore, 'ms')
        } else {
          console.warn('[时间同步] 获取服务器时间失败:', res.msg)
        }
      } catch (error) {
        console.error('[时间同步] 同步服务器时间异常:', error)
      }
    },
    
    /**
     * 启动时间同步定时器
     * 定期与服务器同步时间
     */
    startSyncTimer() {
      this.syncTimer = setInterval(async () => {
        console.log('[时间同步] 执行定期同步')
        await this.syncServerTime()
        
        // 同步后重新计算剩余时间
        this.recalculateLeftSeconds()
      }, this.syncInterval)
    },
    
    /**
     * 设置页面可见性监听器
     * 页面重新可见时立即同步时间
     */
    setupVisibilityListener() {
      this.visibilityHandler = async () => {
        if (document.visibilityState === 'visible') {
          console.log('[页面可见] 立即同步服务器时间')
          await this.syncServerTime()
          this.recalculateLeftSeconds()
        }
      }
      document.addEventListener('visibilitychange', this.visibilityHandler)
    },
    
    /**
     * 获取校准后的当前时间（毫秒）
     * 使用方法而非computed，确保每次调用都获取最新时间
     */
    getCalibratedNow() {
      return Date.now() + this.serverTimeOffset
    },
    
    /**
     * 重新计算剩余时间
     * 基于服务器时间校准，以服务器时间为单一真实来源
     */
    recalculateLeftSeconds() {
      if (!this.startTime || !this.duration) {
        console.warn('[时间计算] 缺少开始时间或时长')
        return
      }
      
      // 使用校准后的当前时间
      const now = this.getCalibratedNow()
      
      // 计算剩余时间（毫秒）
      const remainingMs = this.endTime - now
      
      // 转换为秒，向上取整（确保不会少计时间）
      const newLeftSeconds = Math.max(0, Math.ceil(remainingMs / 1000))
      
      console.log('[时间计算] 当前校准时间:', new Date(now).toLocaleTimeString())
      console.log('[时间计算] 考试结束时间:', new Date(this.endTime).toLocaleTimeString())
      console.log('[时间计算] 剩余时间:', newLeftSeconds, '秒')
      
      // 更新剩余时间
      this.leftSeconds = newLeftSeconds
      this.lastCalculatedTime = now
      
      // 更新显示
      this.updateDisplay()
      
      // 通知父组件
      this.$emit('update:leftSeconds', this.leftSeconds)
    },
    
    /**
     * 更新显示值
     */
    updateDisplay() {
      const min = parseInt(this.leftSeconds / 60)
      const sec = parseInt(this.leftSeconds % 60)
      this.min = min > 9 ? String(min) : '0' + min
      this.sec = sec > 9 ? String(sec) : '0' + sec
    },
    
    /**
     * 开始倒计时
     * 本地定时器只做显示更新，每秒从校准时间重新计算
     */
    startCountdown() {
      console.log('=== 开始倒计时===')
      console.log('初始剩余时间:', this.leftSeconds, '秒')
      console.log('服务器时间偏移量:', this.serverTimeOffset, 'ms')
      
      // 使用setInterval每秒更新显示
      this.timer = setInterval(() => {
        // 重新计算剩余时间（基于校准后的服务器时间）
        // 直接使用 Date.now() + this.serverTimeOffset 确保获取最新时间
        const now = Date.now() + this.serverTimeOffset
        const remainingMs = this.endTime - now
        const newLeftSeconds = Math.max(0, Math.ceil(remainingMs / 1000))
        
        // 更新剩余时间
        this.leftSeconds = newLeftSeconds
        
        // 更新显示
        this.updateDisplay()
        
        // 发送tick事件供父组件记录
        this.$emit('tick', this.leftSeconds)
        
        // 倒计时结束
        if (this.leftSeconds <= 0) {
          console.log('倒计时结束，触发timeout事件')
          clearInterval(this.timer)
          this.timer = null
          this.$emit('timeout')
        }
      }, 1000)
    },
    
    /**
     * 清理所有定时器和监听器
     */
    cleanup() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
        console.log('组件销毁，清除倒计时定时器')
      }
      if (this.syncTimer) {
        clearInterval(this.syncTimer)
        this.syncTimer = null
        console.log('组件销毁，清除同步定时器')
      }
      if (this.visibilityHandler) {
        document.removeEventListener('visibilitychange', this.visibilityHandler)
        this.visibilityHandler = null
        console.log('组件销毁，移除可见性监听器')
      }
    }
  }
}
</script>
