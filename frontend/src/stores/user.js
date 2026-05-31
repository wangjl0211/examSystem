import { defineStore } from 'pinia'
import { login, logout, getInfo } from '@/api/user'
import { getToken, setToken, removeToken, setUserId, removeUserId, setRole, removeRole, setsubjectId, getRoleFromStorage } from '@/utils/auth'
import { resetRouter } from '@/router'
import { parseJwt } from '@/utils/jwtUtils'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'
import { trackPresence } from '@/api/user'

/**
 * 用户状态管理 Store
 * 使用 Pinia 管理用户相关的状态和操作
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    name: '',
    avatar: '',
    roles: [],
    userId: null,
    isUserLoggedIn: false,
    heartbeatIntervalId: null
  }),

  actions: {
    /**
     * 设置 Token
     * @param {string} token JWT Token
     */
    setToken(token) {
      this.token = token
      setToken(token)
    },

    /**
     * 设置角色
     * @param {Array} roles 角色列表
     */
    setRoles(roles) {
      this.roles = roles
      if (roles && roles.length > 0) {
        setRole(roles[0])
      }
    },

    /**
     * 设置用户信息
     * @param {Object} userInfo 用户信息对象
     */
    setUserInfo(userInfo) {
      if (userInfo) {
        this.userId = userInfo.id || null
        this.name = userInfo.realName || userInfo.username || ''
        this.avatar = userInfo.avatar || ''
        if (userInfo.id) {
          setUserId(userInfo.id)
        }
      }
    },

    /**
     * 清除用户信息
     */
    clearUserInfo() {
      this.userId = null
      this.name = ''
      this.avatar = ''
      this.roles = []
      removeUserId()
      removeRole()
    },

    /**
     * 用户登录
     * @param {Object} userInfo 登录表单信息
     * @returns {Promise} 登录结果
     */
    login(userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo).then(response => {
          const { data } = response
          if (response.code === 1) {
            const info = parseJwt(data)
            const user = JSON.parse(info.userInfo)
            const roleId = user.roleId

            // 设置用户ID
            this.userId = user.id
            setUserId(user.id)

            // 根据角色ID确定角色名称
            let role = ''
            if (roleId === 1) {
              role = 'teacher'
            } else if (roleId === 2) {
              role = 'student'
              setsubjectId(user.subjectId)
            } else if (roleId === 0) {
              role = 'admin'
            }

            // 设置角色
            if (role) {
              setRole(role)
              this.roles = [role]
            }

            // 设置用户信息
            this.setUserInfo(user)

            // 建立 WebSocket 连接
            connectWebSocket()
            this.token = data
            setToken(data)

            // 设置登录状态
            this.setUserLoggedIn(true)

            resolve({ role })
          } else {
            reject(response)
          }
        }).catch(error => {
          reject(error)
        })
      })
    },

    /**
     * 获取用户信息
     * @returns {Promise} 用户信息
     */
    getInfo() {
      return new Promise((resolve, reject) => {
        getInfo(this.token).then(response => {
          const { data } = response

          if (!data) {
            return reject('验证失败，请重新登录。')
          }

          const { realName, avatar } = data

          // 读取正确的 localStorage 键名（与 auth.js 中的 roleKey 一致）
          const role = getRoleFromStorage()
          if (role) {
            this.roles = [role]
          } else {
            this.roles = ['student']
          }

          this.name = realName
          this.avatar = avatar
          resolve(data)
        }).catch(error => {
          reject(error)
        })
      })
    },

    /**
     * 重置 Token
     * @returns {Promise} 重置结果
     */
    resetToken() {
      return new Promise(resolve => {
        removeToken()
        this.resetState()
        resolve()
      })
    },

    /**
     * 用户登出
     * @returns {Promise} 登出结果
     */
    logout() {
      return new Promise((resolve, reject) => {
        logout(this.token).then(() => {
          // 清理所有状态
          this.clearAllState()
          resolve()
        }).catch(error => {
          // 即使登出 API 失败，也要清理本地状态
          this.clearAllState()
          reject(error)
        })
      })
    },

    /**
     * 清理所有状态
     */
    clearAllState() {
      removeToken()
      this.clearUserInfo()
      resetRouter()
      this.resetState()
      sessionStorage.clear()
      disconnectWebSocket()
      this.setUserLoggedIn(false)
    },

    /**
     * 重置状态
     */
    resetState() {
      this.token = ''
      this.name = ''
      this.avatar = ''
      this.roles = []
      this.userId = null
    },

    /**
     * 设置用户登录状态和心跳
     * @param {boolean} value 登录状态
     */
    setUserLoggedIn(value) {
      this.isUserLoggedIn = value
      if (value && !this.heartbeatIntervalId) {
        this.sendHeartbeat()
        this.heartbeatIntervalId = setInterval(() => {
          this.sendHeartbeat()
        }, 300000) // 每5分钟发送一次心跳
      } else if (!value && this.heartbeatIntervalId) {
        clearInterval(this.heartbeatIntervalId)
        this.heartbeatIntervalId = null
      }
    },

    /**
     * 发送心跳
     */
    sendHeartbeat() {
      if (!this.userId) {
        console.warn('用户ID未设置，跳过心跳发送')
        return
      }
      trackPresence({ userId: this.userId }).catch(error => {
        console.error('心跳发送失败:', error)
      })
    }
  }
})
