<template>
  <div v-html="sanitizedContent"></div>
</template>

<script>
import DOMPurify from 'dompurify'

/**
 * 安全的HTML渲染组件
 * 使用DOMPurify对HTML内容进行消毒，防止XSS攻击
 */
export default {
  name: 'SafeHtml',
  props: {
    content: {
      type: String,
      default: ''
    }
  },
  computed: {
    sanitizedContent() {
      if (!this.content) return ''
      return DOMPurify.sanitize(this.content, {
        ALLOWED_TAGS: ['div', 'p', 'br', 'strong', 'em', 'u', 's', 'ol', 'ul', 'li', 
                       'a', 'img', 'table', 'thead', 'tbody', 'tr', 'td', 'th',
                       'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'pre', 'code', 'blockquote',
                       'span', 'sub', 'sup', 'hr', 'del', 'ins', 'mark'],
        ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class', 'style', 'target', 'rel']
      })
    }
  }
}
</script>
