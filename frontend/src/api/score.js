
import request from '@/utils/request'

// 辅助函数：构建API URL
function buildApiUrl(endpoint) {
  // 修剪尾随斜线
  const trimmedEndpoint = endpoint.replace(/\/$/, '')
  // 直接返回端点，使用baseURL自动处理前缀
  return trimmedEndpoint
}

export function scorePaging(params) {
  return request({
    url: buildApiUrl('/score/paging'),
    method: 'get',
    params
  })
}

export function getExamScore(params) {
  return request({
    url: buildApiUrl('/score/getExamScore'),
    method: 'get',
    params
  })
}

export function exportScores(examId, subjectId) {
  return request({
    url: buildApiUrl(`/score/export/${examId}/${subjectId}`),
    method: 'get',
    responseType: 'blob'
  })
}

