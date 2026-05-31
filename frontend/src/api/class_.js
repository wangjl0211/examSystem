
import request from '@/utils/request'

// 辅助函数：构建API URL
function buildApiUrl(endpoint) {
  // 修剪尾随斜线
  const trimmedEndpoint = endpoint.replace(/\/$/, '')
  // 直接返回端点，使用baseURL自动处理前缀
  return trimmedEndpoint
}

export function classAdd(data) {
  return request({
    url: buildApiUrl('/subjects/add'),
    method: 'post',
    data
  })
}

export function classPaging(params) {
  return request({
    url: buildApiUrl('/subjects/paging'),
    method: 'get',
    params
  })
}

export function classDel(id) {
  return request({
    url: buildApiUrl(`/subjects/delete/${id}`),
    method: 'delete'
  })
}

export function classUpdate(id, data) {
  return request({
    url: buildApiUrl(`/subjects/update/${id}`),
    method: 'put',
    data
  })
}

export function fetchClasses() {
  return request({
    url: buildApiUrl('/subjects/list'),
    method: 'get'
  })
}

export function userClassRemove(ids) {
  return request({
    url: buildApiUrl(`/subjects/remove/${ids}`),
    method: 'patch'
  })
}
export function joinSubject(params) {
  return request({
    url: buildApiUrl('/subjects/student/join'),
    method: 'get',
    params
  })
}

export function exitSubject(subjectId) {
  return request({
    url: buildApiUrl(`/subjects/student/exit/${subjectId}`),
    method: 'delete'
  })
}

export function getSubjectDetail(subjectId) {
  return request({
    url: buildApiUrl(`/subjects/detail/${subjectId}`),
    method: 'get'
  })
}

export function updateSubjectName(subjectId, data) {
  return request({
    url: buildApiUrl(`/subjects/update/${subjectId}`),
    method: 'put',
    data
  })
}

export function removeUserFromSubject(subjectId, userId) {
  return request({
    url: buildApiUrl(`/subjects/remove-user/${subjectId}/${userId}`),
    method: 'delete'
  })
}
