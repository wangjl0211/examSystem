import request from '@/utils/request'

export function repoAdd(data) {
  return request({
    url: 'repo',
    method: 'post',
    data
  })
}

export function repoPaging(params) {
  return request({
    url: 'repo/paging',
    method: 'get',
    params
  })
}

export function repoDel(id) {
  return request({
    url: 'repo/' + id,
    method: 'delete'
  })
}

export function repoUpdate(id, data) {
  return request({
    url: 'repo/' + id,
    method: 'put',
    data
  })
}

/**
 * 保存题库
 * @param data
 */
export function fetchPaging(params) {
  return request({
    url: 'repo/list',
    method: 'get',
    params
  })
}

/**
 * 获取教师课程列表
 */
export function getTeacherCourses() {
  return request({
    url: 'repo/teacher-courses',
    method: 'get'
  })
}

/**
 * 更新题库课程关联
 * @param repoId 题库ID
 * @param courseIds 课程ID列表
 */
export function updateRepoCourses(repoId, courseIds) {
  return request({
    url: `repo/${repoId}/courses`,
    method: 'put',
    data: courseIds
  })
}

/**
 * 获取题库已关联的课程
 * @param repoId 题库ID
 */
export function getRepoCourses(repoId) {
  return request({
    url: `repo/${repoId}/courses`,
    method: 'get'
  })
}
