import { useAppStore } from '@/stores/app'

const { body } = document
const WIDTH = 992 // 参考Bootstrap的响应式设计断点

export default {
  watch: {
    $route() {
      const appStore = useAppStore()
      if (this.device === 'mobile' && this.sidebar.opened) {
        appStore.closeSideBar({ withoutAnimation: false })
      }
    }
  },
  beforeMount() {
    window.addEventListener('resize', this.$_resizeHandler)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.$_resizeHandler)
  },
  mounted() {
    const isMobile = this.$_isMobile()
    if (isMobile) {
      const appStore = useAppStore()
      appStore.toggleDevice('mobile')
      appStore.closeSideBar({ withoutAnimation: true })
    }
  },
  methods: {
    // 使用$_前缀作为mixin私有属性
    // https://vuejs.org/v2/style-guide/index.html#Private-property-names-essential
    $_isMobile() {
      const rect = body.getBoundingClientRect()
      return rect.width - 1 < WIDTH
    },
    $_resizeHandler() {
      if (!document.hidden) {
        const appStore = useAppStore()
        const isMobile = this.$_isMobile()
        appStore.toggleDevice(isMobile ? 'mobile' : 'desktop')

        if (isMobile) {
          appStore.closeSideBar({ withoutAnimation: true })
        }
      }
    }
  }
}
