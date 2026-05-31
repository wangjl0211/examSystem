
/**
 * 权限控制文件
 * 配置路由守卫，处理登录状态验证和页面访问权限控制
 */
import router from './router'
import { useUserStore } from '@/stores/user'
import { useTagsViewStore } from '@/stores/tagsView'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress' // 进度条
import 'nprogress/nprogress.css' // 进度条样式
import { getToken } from '@/utils/auth' // 从cookie中获取token
import getPageTitle from '@/utils/get-page-title'

NProgress.configure({ showSpinner: false }) // NProgress配置

const whiteList = ['/login', '/admin/login', '/register', '/forgot-password', '/admin/forgot-password'] // 免登录白名单

router.beforeEach(async(to, from, next) => {
  // 启动进度条
  NProgress.start()

  // 设置页面标题
  document.title = getPageTitle(to.meta.title)

  // 判断用户是否已登录
  const hasToken = getToken()
  
  // 初始化状态管理
  const userStore = useUserStore()
  const tagsViewStore = useTagsViewStore()

  if (hasToken) {
    if (to.path === '/login' || to.path === '/admin/login') {
      // 已登录则重定向到首页
      next({ path: '/' })
      NProgress.done()
    } else {
      const hasGetUserInfo = userStore.name
      if (hasGetUserInfo) {
        // 检查路由权限（修复P3问题：增加角色校验）
        if (to.meta.roles && to.meta.roles.length > 0) {
          const userRole = userStore.roles[0]
          if (to.meta.roles.includes(userRole)) {
            // 用户有权限访问该页面
            tagsViewStore.addTag({
              path: to.path,
              checked: false,
              title: to.meta.title
            })
            next()
          } else {
            // 用户无权限访问该页面
            ElMessage.error('您没有权限访问该页面')
            next('/index')
          }
        } else {
          // 没有角色限制的页面，直接放行
          tagsViewStore.addTag({
            path: to.path,
            checked: false,
            title: to.meta.title
          })
          next()
        }
      } else {
        try {
          // 获取用户信息
          await userStore.getInfo()
          
          // 获取用户信息后，检查路由权限
          if (to.meta.roles && to.meta.roles.length > 0) {
            const userRole = userStore.roles[0]
            if (!to.meta.roles.includes(userRole)) {
              ElMessage.error('您没有权限访问该页面')
              next('/index')
              NProgress.done()
              return
            }
          }

          tagsViewStore.addTag({
            path: to.path,
            checked: false,
            title: to.meta.title
          })

          next()
        } catch (error) {
          // 移除token并跳转到登录页重新登录
          await userStore.resetToken()
          // 安全处理错误信息
          if (error && error.msg) {
            ElMessage.error(error.msg)
          } else if (error && error.message) {
            ElMessage.error(error.message)
          } else {
            ElMessage.error('获取用户信息失败')
          }
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    /* 没有token */

    if (whiteList.indexOf(to.path) !== -1) {
      // 在免登录白名单中，直接放行
      next()
    } else {
      // 其他没有权限访问的页面，重定向到登录页
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // 关闭进度条
  NProgress.done()
})
