
import { getUserId,getsubjectId } from './auth'
import { getRole } from '@/utils/jwtUtils'
import { ElNotification } from 'element-plus'
import { EventBus } from './eventBus'

// 定义 WebSocket 实例
let socket
// 修复P0问题：使用环境变量配置WebSocket地址，支持加密连接
const getWebSocketBaseUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = import.meta.env.VITE_WS_HOST || window.location.host
  return `${protocol}//${host}/websocket`
}
// eslint-disable-next-line no-unused-vars
let isConnected = false
let reconnectTimer
const reconnectInterval = 5000 // 重连间隔时间，单位：毫秒
// websocket收到消息回调
 
// let messageCallback
let reconnectCount = 0
const maxReconnectCount = 10 // 设置最大重连次数
 
let isManuallyClosed = false // 标记是否是主动断开连接
let connecting = false

// 连接 WebSocket
const connectWebSocket = () => {
  if (!getUserId()) {
    return
  }
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
    return
  }
  if (connecting) {
    return
  }
  connecting = true
  isManuallyClosed = false
  const baseSocketUrl = getWebSocketBaseUrl()
  const socketUrl = `${baseSocketUrl}?userId=${getUserId()}`
  socket = new WebSocket(socketUrl)

  socket.onopen = () => {
    console.log('WebSocket 连接成功')
    isConnected = true
    connecting = false
    reconnectCount = 0
    clearInterval(reconnectTimer)
  }

  socket.onmessage = (event) => {
    console.log('收到消息:', event.data)
    const res = JSON.parse(event.data)
    if (res.type === 'NOTICE' && getRole() === 'student' && res.data.subjectIds.includes(getsubjectId())) {
      // 弹出通知
      ElNotification({
        title: `通知`,
        // dangerouslyUseHTMLString: true,
        message: `你有一条新公告请及时查收`
      })
    } else if (res.type === 'EXAM_DELETED' && getRole() === 'student') {
      // 处理考试被删除的通知
      ElNotification({
        title: `考试通知`,
        type: 'error',
        message: res.data.message || '您正在参加的考试已被教师删除'
      })
      
      // 检查当前是否在考试页面
      const currentPath = window.location.pathname
      if (currentPath.includes('/start-exam') || currentPath.includes('/exam')) {
        // 跳转到试卷中心页面
        setTimeout(() => {
          window.location.href = '/text-center'
        }, 2000)
      }
    }
    // 使用事件总线发送时间
    EventBus.emit('websocket-message', res)
  }

  socket.onclose = () => {
    console.log('WebSocket 连接关闭')
    isConnected = false
    connecting = false
    // 非主动断开连接，或未达到最大连接次数，尝试重新连接
    if (!isManuallyClosed && reconnectCount < maxReconnectCount) {
      console.log('尝试重连...')
      // 清除之前的定时任务
      if (reconnectTimer) {
        clearInterval(reconnectTimer)
      }
      // 指数型增加重连间隔时间
      const currentInterval = reconnectInterval * Math.pow(2, reconnectCount)
      reconnectTimer = setInterval(connectWebSocket, currentInterval)
      reconnectCount++
    } else {
      // 主动断开连接或达到最大连接次数，清除定时任务
      clearInterval(reconnectTimer)
      if (isManuallyClosed) {
        console.log('主动断开连接，停止重连')
      } else {
        console.log('已达到最大重连次数，停止重连')
      }
    }
  }

  socket.onerror = () => {
    // console.error('WebSocket 发生错误:', error)
  }
}

// 封装发送消息的方法
function sendMessage(message) {
  console.log('-------------')
  console.log(socket)
  if (!socket) {
    console.error('WebSocket 未初始化，尝试重新连接')
    connectWebSocket()
    return false
  }
  const readyState = socket.readyState
  if (readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(message))
    return true
  }
  if (readyState === WebSocket.CONNECTING) {
    console.log('WebSocket 正在连接中，请稍后再试')
    return false
  }
  if (readyState === WebSocket.CLOSING) {
    console.log('WebSocket 正在关闭，无法发送消息')
    return false
  } if (readyState === WebSocket.CLOSED) {
    console.log('WebSocket 已关闭，尝试重新连接')
    connectWebSocket()
    return false
  }
}

// 断开 WebSocket 连接的方法
function disconnectWebSocket() {
  if (socket) {
    isManuallyClosed = true // 标记为主动断开
    clearInterval(reconnectTimer) // 清除重连定时器
    socket.close()
    console.log('主动断开 WebSocket 连接')
  }
}

export {
  connectWebSocket,
  disconnectWebSocket,
  sendMessage
}
