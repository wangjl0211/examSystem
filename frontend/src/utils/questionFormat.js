/**
 * 题目展示相关格式化工具
 */

const NUMBER_TO_CHAR_MAP = {
  0: 'A',
  1: 'B',
  2: 'C',
  3: 'D',
  4: 'E',
  5: 'F'
}

/**
 * 将选项序号转为字母 A/B/C...（支持单个数字或逗号分隔）
 * @param {number|string} input 序号或 "0,1,2" 形式
 * @returns {string}
 */
export function numberToLetter(input) {
  if (input === null || input === undefined) return ''

  if (typeof input === 'number' || /^\d+$/.test(String(input))) {
    return NUMBER_TO_CHAR_MAP[parseInt(input, 10)] || ''
  }

  if (/^\d+(,\d+)*$/.test(String(input))) {
    return String(input)
      .split(',')
      .map((num) => NUMBER_TO_CHAR_MAP[parseInt(num.trim(), 10)] || '')
      .join(',')
  }

  return String.fromCharCode(65 + (parseInt(input, 10) || 0))
}

/**
 * 判断题目列表是否有数据
 * @param {Array} list 题目列表
 * @returns {boolean}
 */
export function hasQuestions(list) {
  return Array.isArray(list) && list.length > 0
}

/**
 * 按题型分组题目（Options API 与 composable 共用）
 * @param {Array} questions 题目平铺列表
 * @returns {{ radioList: Array, multiList: Array, judgeList: Array, saqList: Array }}
 */
export function groupQuestionsByType(questions) {
  const list = questions || []
  return {
    radioList: list.filter((q) => q.quType === 1 || q.type === 1),
    multiList: list.filter((q) => q.quType === 2 || q.type === 2),
    judgeList: list.filter((q) => q.quType === 3 || q.type === 3),
    saqList: list.filter((q) => q.quType === 4 || q.type === 4)
  }
}
