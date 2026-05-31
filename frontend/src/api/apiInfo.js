import request from '@/utils/request'

/**
 * 接口信息相关API
 */
export function getAllApiInfo() {
  return request({
    url: '/info/apis',
    method: 'get'
  })
}

export function getApiStats() {
  return request({
    url: '/info/stats',
    method: 'get'
  })
}
