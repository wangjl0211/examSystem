// 配置加载工具
// 用于从本地配置文件读取配置值

import axios from 'axios'

// 默认配置
const defaultConfig = {
  VITE_APP_BASE_API: '/api',
  VITE_APP_ENABLE_REGISTER: true,
  VITE_APP_ICP_NUMBER: '京ICP备XXXXXXXX号',
  VITE_APP_ICP_LINK: 'https://beian.miit.gov.cn/',
  VITE_APP_TITLE: '校园在线考试系统',
  API_BASE_URL: 'http://localhost:8080',
  DEFAULT_TIMEOUT: 30000,
  STORAGE_TYPE: 'local'
}

let configInstance = null

/**
 * 加载配置文件
 * @returns {Promise<Object>} 配置对象
 */
export const loadConfig = async () => {
  try {
    // 尝试从本地配置文件加载
    const response = await axios.get('/local-config.json', {
      baseURL: '',
      timeout: 5000
    })
    
    // 合并默认配置和本地配置
    const mergedConfig = { ...defaultConfig, ...response.data }
    configInstance = mergedConfig
    
    console.log('本地配置文件加载成功', mergedConfig)
    return mergedConfig
  } catch (error) {
    console.warn('本地配置文件加载失败，使用默认配置', error.message)
    
    // 检查是否是生产环境
    if (import.meta.env.MODE === 'production') {
      console.error('生产环境必须提供配置文件')
      // 在生产环境中，如果配置文件加载失败，可以选择抛出错误
      // throw new Error('配置文件加载失败')
    }
    
    configInstance = defaultConfig
    return defaultConfig
  }
}

/**
 * 获取配置值
 * @param {string} key 配置键名
 * @param {*} defaultValue 默认值
 * @returns {*} 配置值
 */
export const getConfig = (key, defaultValue = null) => {
  if (!configInstance) {
    console.warn('配置尚未加载，使用默认值')
    return defaultConfig[key] || defaultValue
  }
  return configInstance[key] || defaultValue
}

/**
 * 初始化配置
 * 应该在应用启动时调用
 */
export const initConfig = async () => {
  await loadConfig()
  // 可以在这里添加配置验证逻辑
  validateConfig()
}

/**
 * 验证配置
 */
const validateConfig = () => {
  // 检查必要的配置项
  const requiredConfig = ['VITE_APP_BASE_API', 'API_BASE_URL']
  
  requiredConfig.forEach(key => {
    if (!getConfig(key)) {
      console.error(`缺少必要配置项: ${key}`)
    }
  })
}

// 导出单例配置对象
export default {
  loadConfig,
  getConfig,
  initConfig
}
