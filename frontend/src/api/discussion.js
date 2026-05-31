
import request from '@/utils/request'

// 辅助函数：构建API URL
function buildApiUrl(endpoint) {
  // 修剪尾随斜线
  const trimmedEndpoint = endpoint.replace(/\/$/, '')
  // 直接返回端点，使用baseURL自动处理前缀
  return trimmedEndpoint
}

export function discussionAdd(data) {
  return request({
    url: buildApiUrl('/discussion/add'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

export function discussionDel(id) {
  return request({
    url: buildApiUrl(`/discussion/delete/${id}`),
    method: 'delete',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

export function discussionDetail(id) {
  return request({
    url: buildApiUrl(`/discussion/query/detail/${id}`),
    method: 'get',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

export function discussionpageOwner(params) {
  return request({
    url: buildApiUrl('/discussion/query/page/owner'),
    method: 'get',
    params,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

export function discussionpageStudent(params) {
  return request({
    url: buildApiUrl('/discussion/query/page/student'),
    method: 'get',
    params,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}
export function getDiscussionRely(id,orderBy) {
  return request({
    url: buildApiUrl(`/reply/query/${orderBy}/${id}`),
    method: 'get',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}
// /like/doLike
export function doLike(data) {
  return request({
    url: buildApiUrl('/like/doLike'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}