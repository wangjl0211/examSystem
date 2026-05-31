<template>
  <div :class="classObj" class="app-wrapper">
    <div
      v-if="device === 'mobile' && sidebar.opened"
      class="drawer-bg"
      @click="handleClickOutside"
    />
    <sidebar class="sidebar-container" />
    <div class="main-container">
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar />
      </div>
      <app-main />
    </div>
  </div>
</template>

<script>
import { Navbar, Sidebar, AppMain } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import { useAppStore } from '@/stores/app'
import { useSettingsStore } from '@/stores/settings'
export default {
  name: 'Layout',
  components: {
    Navbar,
    Sidebar,
    AppMain
  },
  mixins: [ResizeMixin],
  setup() {
    const appStore = useAppStore()
    const settingsStore = useSettingsStore()
    return {
      appStore,
      settingsStore
    }
  },
  computed: {
    sidebar() {
      return this.appStore.sidebar
    },
    device() {
      return this.appStore.device
    },
    fixedHeader() {
      return this.settingsStore.fixedHeader
    },
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    }
  },
  methods: {
    handleClickOutside() {
      this.appStore.closeSideBar({ withoutAnimation: false })
    }
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/mixin.scss" as mixins;
@use "@/styles/variables.scss" as vars;

.app-wrapper {
  @include mixins.clearfix;
  position: relative;
  height: 100%;
  width: 100%;
  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}
.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - #{vars.$sideBarWidth});
  transition: width 0.28s;
}

.hideSidebar .fixed-header {
  width: 100%;
}

.mobile .fixed-header {
  width: 100%;
}
</style>
