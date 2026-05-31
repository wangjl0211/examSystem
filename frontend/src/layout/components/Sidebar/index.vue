<template>
  <div :class="{ 'has-logo': showLogo }">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :unique-opened="false"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="(route, index) in routes"
          :key="index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapState } from 'pinia'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useSettingsStore } from '@/stores/settings'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.module.scss'

export default {
  components: { SidebarItem, Logo },

  computed: {
    ...mapState(useAppStore, ['sidebar']),
    ...mapState(useUserStore, ['roles']),
    ...mapState(useSettingsStore, ['sidebarLogo']),
    routes() {
      const menuList = this.$router.options.routes
      const userRoles = this.roles || []

      // 权限检查辅助函数
      const hasPermission = (roles, route) => {
        // 如果没有配置 roles，则默认允许访问
        if (!route.meta || !route.meta.roles || route.meta.roles.length === 0) {
          return true
        }
        
        // 如果用户角色列表为空，不允许访问受保护的路由
        if (!roles || roles.length === 0) {
          return false
        }
        
        // 检查用户角色是否与路由角色匹配
        return roles.some(role => route.meta.roles.includes(role))
      }

      // 递归过滤路由
      const filterRoutes = (routes, roles) => {
        const res = []
        routes.forEach(route => {
          const tmp = { ...route }
          if (hasPermission(roles, tmp)) {
            if (tmp.children) {
              tmp.children = filterRoutes(tmp.children, roles)
            }
            res.push(tmp)
          }
        })
        return res
      }

      return filterRoutes(menuList, userRoles)
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    showLogo() {
      return this.sidebarLogo
    },
    variables() {
      if (variables && variables.menuBg) {
        return variables
      }
      return {
        menuBg: '#304156',
        menuText: '#bfcbd9',
        menuActiveText: '#409EFF'
      }
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  }
}
</script>
