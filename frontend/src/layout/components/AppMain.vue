<template>
  <section class="app-main">
    <router-view v-slot="{ Component }">
      <transition name="fade-transform" mode="out-in">
        <component :is="Component" :key="key" />
      </transition>
    </router-view>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  computed: {
    key() {
      return this.$route.path
    }
  }
}
</script>

<style scoped>
.app-main {
  /*50 = 导航栏高度 */
  min-height: calc(100vh - 111px);
  width: 100%;
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
}
.fixed-header+.app-main {
  padding-top: 110px;
}
@media (max-width: 991px) {
  .app-main {
    min-height: calc(100vh - 60px);
  }
  .fixed-header+.app-main {
    padding-top: 60px;
  }
}
</style>

<style lang="scss">
// 修复打开el-dialog时的CSS样式bug
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>
