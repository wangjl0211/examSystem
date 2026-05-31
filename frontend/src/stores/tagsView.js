import { defineStore } from 'pinia'
import router from '@/router'

const losePath = ['/404']

export const useTagsViewStore = defineStore('tagsView', {
  state: () => ({
    // 修复P2问题：添加try-catch保护，防止sessionStorage数据被篡改时崩溃
    tags: (() => {
      try {
        const stored = sessionStorage.getItem('TAGS')
        return stored ? JSON.parse(stored) : []
      } catch (e) {
        console.warn('解析TAGS数据失败，使用默认值:', e)
        sessionStorage.removeItem('TAGS')
        return []
      }
    })()
  }),
  actions: {
    addTag(tag) {
      const pathList = this.tags.map(item => item.path)
      if (!losePath.includes(tag.path)) {
        if (pathList.includes(tag.path)) {
          this.tags.forEach(item => {
            if (item.path === tag.path) {
              item.checked = true
            } else {
              item.checked = false
            }
          })
        } else {
          this.tags.forEach(item => {
            item.checked = false
          })
          const newTag = {
            ...tag,
            checked: true
          }
          this.tags.push(newTag)
        }
        sessionStorage.setItem('TAGS', JSON.stringify(this.tags))
      }
    },
    removeTag(tag) {
      if (this.tags && this.tags.length === 1) {
        return
      }
      // 修复P2问题：使用filter替代forEach+splice，避免遍历中修改数组导致索引错位
      const tagIndex = this.tags.findIndex(item => item.title === tag.title)
      if (tagIndex === -1) return
      
      // 如果删除的是当前路由，需要跳转到最后一个标签
      if (router.currentRoute.value.fullPath === tag.path) {
        const lastTag = this.tags[this.tags.length - 1]
        // 确保跳转的不是即将被删除的标签
        if (lastTag.title !== tag.title) {
          router.push(lastTag.path)
        } else if (this.tags.length > 1) {
          router.push(this.tags[this.tags.length - 2].path)
        }
      }
      
      // 使用filter移除指定标签
      this.tags = this.tags.filter(item => item.title !== tag.title)
      sessionStorage.setItem('TAGS', JSON.stringify(this.tags))
    },
    closeSidebar() {
      this.tags = []
    }
  }
})
