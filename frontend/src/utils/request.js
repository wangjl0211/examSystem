/**
 * HTTP请求工具模块
 * 封装axios，配置请求/响应拦截器，处理错误和Token刷新
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getToken, setToken } from '@/utils/auth'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  withCredentials: false,
  timeout: 30000,
  crossDomain: true
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + getToken()
    }
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 错误状态码映射表
const HTTP_ERROR_MESSAGES = {
  // 4xx - 客户端错误
  400: '请求参数错误，请检查后重试',
  401: '您的登录信息已过期，请重新登录',
  403: '您没有权限访问该资源',
  404: '请求的资源不存在',
  405: '请求方法不允许',
  408: '请求超时，请稍后重试',
  409: '资源冲突，请稍后重试',
  410: '请求的资源已被永久删除',
  411: '请指定Content-Length头信息',
  412: '前置条件验证失败',
  413: '上传的文件体积过大',
  414: '请求URL过长',
  415: '不支持的媒体类型，请检查Content-Type',
  416: '请求的范围不合法',
  422: '数据验证失败，请检查输入',
  423: '资源已被锁定',
  424: '前置请求失败',
  426: '请升级客户端协议版本',
  428: '请求必须包含前置条件',
  429: '请求过于频繁，请稍后再试',
  431: '请求头字段过大',
  451: '因法律原因无法提供该内容',
  
  // 5xx - 服务器错误
  500: '服务器内部错误，请稍后重试',
  501: '服务器不支持此功能',
  502: '网关错误，请稍后重试',
  503: '服务暂时不可用，请稍后重试',
  504: '网关超时，请稍后重试',
  505: '不支持的HTTP协议版本',
  506: '服务器配置错误',
  507: '服务器存储空间不足',
  508: '检测到服务器循环',
  510: '服务器需要扩展请求',
  511: '需要网络认证'
}

// 不需要弹窗提示的状态码（静默处理）
const SILENT_ERROR_STATUSES = [401, 403, 404] // 404可以静默，或根据业务调整

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 判断响应是否为Blob类型（文件下载）
    const isBlobResponse = response.config.responseType === 'blob' || 
                          response.headers['content-type']?.includes('application/octet-stream') ||
                          response.headers['content-type']?.includes('application/vnd.openxmlformats')
    
    if (isBlobResponse) {
      // 检查Blob的实际内容类型，判断是否为错误响应
      const blob = response.data
      if (blob && blob.type === 'application/json') {
        // Blob内容实际是JSON错误信息，需要解析并返回错误
        return new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => {
            try {
              const errorData = JSON.parse(reader.result)
              // 返回解析后的JSON数据，让调用方处理
              resolve(errorData)
            } catch (e) {
              reject(new Error(errorData?.msg || '文件下载失败'))
            }
          }
          reader.onerror = () => reject(new Error('文件下载失败'))
          reader.readAsText(blob)
        })
      }
      // 有效的文件Blob，直接返回
      return response.data
    }
    
    const res = response.data
    let newToken = response.headers['authorization']
    const userStore = useUserStore()
    
    // Token 刷新处理，确保正确处理刷新后的令牌
    if (newToken) {
      // 确保新令牌格式正确
      if (newToken.startsWith('Bearer ')) {
        newToken = newToken.substring(7);
      }
      setToken(newToken);
      userStore.token = newToken;
    }

    // 登录响应连接信息输出
    const requestUrl = response.config?.url || ''
    if (requestUrl.includes('/user/login')) {
      if (res.code === 1) {
        console.log('  登录成功，Token已获取')
      }
    } else if (requestUrl.includes('/admin/login')) {
      if (res.code === 1) {
        console.log('  登录成功，Token已获取')
      }
    }

    // 兼容不同的成功状态码
    if (res.code !== 1 && res.code !== 200 && res.code !== 0) {
      // 登录失败详细输出
      if (requestUrl.includes('/user/login') || requestUrl.includes('/admin/login')) {
        console.log('%c[登录失败] 错误信息:', 'color: #F56C6C; font-weight: bold;', res.msg || '操作失败')
      }
      // 检查是否需要内联提示（不弹窗）
      if (response.config.headers['X-Inline-Error'] !== 'true') {
        ElMessage({
          message: res.msg || '操作失败',
          type: 'error',
          duration: 3 * 1000
        })
      }
      return Promise.reject(new Error(res.msg || '操作失败'))
    }
    
    return res
  },
  error => {
    // 完整错误信息输出（开发环境）
    if (import.meta.env.DEV) {
      console.group('❌ HTTP 请求错误')
      console.error('请求URL:', error.config?.url)
      console.error('请求方法:', error.config?.method)
      console.error('错误详情:', error)
      if (error.response) {
        console.error('状态码:', error.response.status)
        console.error('响应数据:', error.response.data)
      }
      console.groupEnd()
    }

    // 获取配置信息
    const config = error.config || {}
    const useInlineError = config.headers?.['X-Inline-Error'] === 'true'
    
    // 处理网络错误/跨域错误/取消请求
    if (!error.response) {
      // 请求取消 - 静默处理，不弹窗
      if (axios.isCancel(error)) {
        return Promise.reject(new Error('请求已取消'))
      }
      // 网络错误
      if (!useInlineError) {
        ElMessage({
          message: '网络连接失败，请检查网络后重试',
          type: 'error',
          duration: 3 * 1000
        })
      }
      return Promise.reject(new Error('网络连接失败'))
    }
    // 处理HTTP状态码错误
    const status = error.response.status
    const responseData = error.response.data || {}
    const userStore = useUserStore()
    
    // 处理Blob类型的错误响应（responseType: 'blob' 时，错误响应也是Blob）
    const isBlobError = config.responseType === 'blob' && responseData instanceof Blob
    
    // 获取错误提示信息（优先级：后端返回msg > 状态码映射 > 默认文案）
    const getErrorMessage = () => {
      // 后端返回的错误信息（JSON对象）
      if (responseData.msg || responseData.message) {
        return responseData.msg || responseData.message
      }
      // HTTP状态码映射
      if (HTTP_ERROR_MESSAGES[status]) {
        return HTTP_ERROR_MESSAGES[status]
      }
      // 默认
      return `请求失败 (${status})`
    }
    
    // 如果是Blob类型的错误响应，需要异步读取内容
    if (isBlobError) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => {
          try {
            const errorJson = JSON.parse(reader.result)
            const msg = errorJson.msg || errorJson.message || HTTP_ERROR_MESSAGES[status] || `请求失败 (${status})`
            if (!useInlineError) {
              ElMessage({ message: msg, type: 'error', duration: 3000 })
            }
            reject(new Error(msg))
          } catch (e) {
            const msg = HTTP_ERROR_MESSAGES[status] || `请求失败 (${status})`
            if (!useInlineError) {
              ElMessage({ message: msg, type: 'error', duration: 3000 })
            }
            reject(new Error(msg))
          }
        }
        reader.onerror = () => {
          const msg = HTTP_ERROR_MESSAGES[status] || `请求失败 (${status})`
          reject(new Error(msg))
        }
        reader.readAsText(responseData)
      })
    }

    const errorMessage = getErrorMessage()

    // 特殊状态码处理策略
    switch (status) {
      case 401:
        // 401: Token失效，跳转登录页
        userStore.resetToken()
        // 避免重复跳转，同时排除注册页面
        const currentPath = router.currentRoute.value.path
        if (!currentPath.includes('/login') && !currentPath.includes('/register')) {
          router.replace({
            path: '/login',
            query: { 
              redirect: currentPath,
              timestamp: Date.now() // 防止浏览器缓存
            }
          })
        }
        break
        
      case 403:
        // 403: 无权限访问该资源（纯权限不足，不跳转登录页）
        console.warn('403错误：无权限访问该资源:', error.config?.url)
        // 403不自动跳转登录页，只记录警告（可能是某接口权限不足，非token失效）
        break
        
      case 429:
        // 429: 请求频繁，增加延迟提示
        ElMessage({
          message: errorMessage,
          type: 'warning',
          duration: 5 * 1000
        })
        // 不继续执行下面的弹窗逻辑
        return Promise.reject(new Error(errorMessage))
        
      case 502:
      case 503:
      case 504:
        // 网关类错误：可以尝试自动重试（需额外实现）
        console.warn(`服务暂时不可用 (${status})，建议稍后重试`)
        break
    }

    // 错误弹窗提示（非静默状态码 + 非内联提示）
    const isSilent = SILENT_ERROR_STATUSES.includes(status)
    if (!useInlineError && !isSilent) {
      // 根据状态码类型使用不同提示样式
      const messageType = status >= 500 ? 'error' : 'warning'
      
      ElMessage({
        message: errorMessage,
        type: messageType,
        duration: status === 429 ? 5 * 1000 : 3 * 1000
      })
    }

    // 构造错误对象
    const httpError = new Error(errorMessage)
    httpError.status = status
    httpError.response = error.response
    httpError.config = error.config
    
    return Promise.reject(httpError)
  }
)

/**
 * 扩展请求方法 - 支持取消请求
 */
export const cancelTokenSource = axios.CancelToken.source

/**
 * 扩展GET请求（带缓存控制）
 */
export const getWithCache = (url, config = {}) => {
  return service.get(url, {
    ...config,
    headers: {
      ...config.headers,
      'Cache-Control': 'max-age=300' // 5分钟缓存
    }
  })
}

/**
 * 静默请求（不显示错误弹窗）
 */
export const silentRequest = (method, url, data = {}, config = {}) => {
  return service({
    method,
    url,
    [method.toLowerCase() === 'get' ? 'params' : 'data']: data,
    ...config,
    headers: {
      ...config.headers,
      'X-Inline-Error': 'true' // 内联错误，不弹窗
    }
  })
}

export default service