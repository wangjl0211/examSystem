<script setup>
/**
 * 回复列表组件
 * 展示讨论的回复列表，支持删除操作
 */
import { defineProps, defineEmits } from 'vue'
import Discussion from '@/components/discussion'

const props = defineProps({
  replies: {
    type: [Array, Object],
    default: () => ({})
  },
  currentRole: {
    type: String,
    default: ''
  },
  discussionId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['delete', 'refresh'])

/**
 * 处理删除回复
 * @param {number} replyId 回复ID
 */
const handleDelete = (replyId) => {
  emit('delete', replyId)
}

/**
 * 刷新回复列表
 */
const handleRefresh = () => {
  emit('refresh')
}
</script>

<template>
  <div class="reply-list">
    <div v-for="item in replies" :key="item.id" class="reply-item">
      <Discussion
        :discussionData="item"
        :delFun="handleDelete"
        :discussionId="discussionId"
        :onConfirm="handleRefresh"
        style="margin: 10px 0;"
      />
    </div>
    <div v-if="!replies || Object.keys(replies).length === 0" class="empty-reply">
      暂无回复
    </div>
  </div>
</template>

<style scoped>
.reply-list {
  margin-top: 16px;
}

.reply-item {
  margin-bottom: 8px;
}

.empty-reply {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
