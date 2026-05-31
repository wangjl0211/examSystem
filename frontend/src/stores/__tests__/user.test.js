import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

/**
 * 用户 Store 单元测试
 */
describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should have correct initial state', () => {
    const store = useUserStore()
    expect(store.token).toBeDefined()
    expect(store.name).toBe('')
    expect(store.avatar).toBe('')
    expect(store.roles).toEqual([])
    expect(store.userId).toBeNull()
    expect(store.isUserLoggedIn).toBe(false)
  })

  it('should set user info correctly', () => {
    const store = useUserStore()
    const userInfo = {
      id: 1,
      realName: 'Test User',
      avatar: 'https://example.com/avatar.jpg'
    }

    store.setUserInfo(userInfo)

    expect(store.userId).toBe(1)
    expect(store.name).toBe('Test User')
    expect(store.avatar).toBe('https://example.com/avatar.jpg')
  })

  it('should clear user info correctly', () => {
    const store = useUserStore()
    
    // 先设置用户信息
    store.setUserInfo({
      id: 1,
      realName: 'Test User',
      avatar: 'https://example.com/avatar.jpg'
    })
    store.setRoles(['admin'])

    // 清除用户信息
    store.clearUserInfo()

    expect(store.userId).toBeNull()
    expect(store.name).toBe('')
    expect(store.avatar).toBe('')
    expect(store.roles).toEqual([])
  })

  it('should reset state correctly', () => {
    const store = useUserStore()
    
    // 设置一些状态
    store.token = 'test-token'
    store.name = 'Test User'
    store.userId = 1

    // 重置状态
    store.resetState()

    expect(store.token).toBe('')
    expect(store.name).toBe('')
    expect(store.userId).toBeNull()
  })
})
