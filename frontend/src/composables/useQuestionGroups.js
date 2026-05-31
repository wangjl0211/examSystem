/**
 * 按题型分组题目（Composition API）
 */
import { computed, unref } from 'vue'
import { groupQuestionsByType } from '@/utils/questionFormat'

/**
 * @param {import('vue').Ref|Array} paperListRef 试卷题目平铺列表
 */
export function useQuestionGroups(paperListRef) {
  const questionGroups = computed(() => groupQuestionsByType(unref(paperListRef)))

  return { questionGroups }
}
