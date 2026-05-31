import request from '@/utils/request'

export function recordExamDetail(params) {
  return request({
    url: 'records/exam/detail',
    method: 'get',
    params
  })
}


