<template>
  <div v-if="!item.hidden">
    <template
      v-if="
        hasOneShowingChild(item.children, item) &&
          (!onlyOneChild.children || onlyOneChild.noShowingChildren) &&
          !item.alwaysShow
      "
    >
      <app-link
        v-if="onlyOneChild.meta && onlyOneChild.meta.visible"
        :to="resolvePath(onlyOneChild.path)"
      >
        <el-menu-item
          :index="resolvePath(onlyOneChild.path)"
          :class="{ 'submenu-title-noDropdown': !isNest }"
        >
          <item
            v-if="onlyOneChild.meta.visible"
            :icon="onlyOneChild.meta.icon || (item.meta && item.meta.icon)"
            :title="onlyOneChild.meta.title"
          />
        </el-menu-item>
      </app-link>
    </template>

    <div v-else>
      <el-sub-menu
        v-if="item.meta && item.meta.visible"
        ref="subMenu"
        :index="resolvePath(item.path)"
        popper-append-to-body
      >
        <template #title>
          <item
            v-if="item.meta && item.meta.visible"
            :icon="item.meta && item.meta.icon"
            :title="item.meta.title"
          />
        </template>
        <sidebar-item
          v-for="child in item.children"
          :key="child.path"
          :is-nest="true"
          :item="child"
          :base-path="resolvePath(child.path)"
          class="nest-menu"
        />
      </el-sub-menu>
    </div>
  </div>
</template>

<script>
import { isExternal } from '@/utils/validate'
import Item from './Item'
import AppLink from './Link'
import FixiOSBug from './FixiOSBug'
import path from 'path-browserify'

export default {
  name: 'SidebarItem',
  components: { Item, AppLink },
  mixins: [FixiOSBug],
  props: {
    // 路由对象
    item: {
      type: Object,
      required: true
    },
    isNest: {
      type: Boolean,
      default: false
    },
    basePath: {
      type: String,
      default: ''
    }
  },
  data() {
    // 修复此问题：https://github.com/PanJiaChen/vue-admin-template/issues/237
    // TODO: 使用render函数重构
    this.onlyOneChild = null
    return {}
  },
  methods: {
    hasOneShowingChild(children = [], parent) {
      const showingChildren = children.filter((item) => {
        if (item.hidden) {
          return false
        } else {
          // 临时设置（当只有一个显示子项时使用）
          this.onlyOneChild = item
          return true
        }
      })

      // 当只有一个子路由时，默认显示该子路由
      if (showingChildren.length === 1) {
        return true
      }

      // 如果没有可显示的子路由，则显示父路由
      if (showingChildren.length === 0) {
        this.onlyOneChild = { ...parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    },
    resolvePath(routePath) {
      if (isExternal(routePath)) {
        return routePath
      }
      if (isExternal(this.basePath)) {
        return this.basePath
      }
      return path.resolve(this.basePath, routePath)
    }
  }
}
</script>
