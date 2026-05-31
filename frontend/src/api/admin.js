import request from '@/utils/request'

/**
 * 构建API URL
 * @param {string} endpoint 接口端点
 * @returns {string} 处理后的URL
 */
function buildApiUrl(endpoint) {
  // 修剪尾随斜线
  const trimmedEndpoint = endpoint.replace(/\/$/, '')
  // 直接返回端点，使用baseURL自动处理前缀
  return trimmedEndpoint
}

/**
 * 管理员登录
 * @param {Object} data 登录数据 { userNo, password, verifyToken }
 * @returns {Promise} 请求结果
 */
export function adminLogin(data) {
  const { verifyToken, ...loginData } = data
  return request({
    url: buildApiUrl('/admin/login'),
    method: 'post',
    data: loginData,
    headers: {
      'X-Inline-Error': 'true',
      'X-Verify-Token': verifyToken || ''  // 传递验证token到请求头
    }
  })
}

/**
 * 获取IP白名单列表
 * @returns {Promise} 请求结果
 */
export function getWhitelistList() {
  return request({
    url: buildApiUrl('/admin/whitelist/list'),
    method: 'get',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 分页获取IP白名单列表
 * @param {Object} params 分页参数 { page, size }
 * @returns {Promise} 请求结果
 */
export function getWhitelistPage(params) {
  return request({
    url: buildApiUrl('/admin/whitelist/page'),
    method: 'get',
    params,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 根据ID获取IP白名单详情
 * @param {number} id 规则ID
 * @returns {Promise} 请求结果
 */
export function getWhitelistById(id) {
  return request({
    url: buildApiUrl(`/admin/whitelist/${id}`),
    method: 'get',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 添加IP白名单规则
 * @param {Object} data IP白名单数据 { ipAddress, ipType, description }
 * @returns {Promise} 请求结果
 */
export function addWhitelist(data) {
  return request({
    url: buildApiUrl('/admin/whitelist'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 更新IP白名单规则
 * @param {Object} data IP白名单数据 { id, ipAddress, ipType, description }
 * @returns {Promise} 请求结果
 */
export function updateWhitelist(data) {
  return request({
    url: buildApiUrl('/admin/whitelist'),
    method: 'put',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 删除IP白名单规则
 * @param {number} id 规则ID
 * @returns {Promise} 请求结果
 */
export function deleteWhitelist(id) {
  return request({
    url: buildApiUrl(`/admin/whitelist/${id}`),
    method: 'delete',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 启用/禁用IP白名单规则
 * @param {number} id 规则ID
 * @param {number} status 状态：1启用 0禁用
 * @returns {Promise} 请求结果
 */
export function toggleWhitelistStatus(id, status) {
  return request({
    url: buildApiUrl(`/admin/whitelist/${id}/status`),
    method: 'put',
    params: { status },
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 刷新IP白名单缓存
 * @returns {Promise} 请求结果
 */
export function refreshWhitelistCache() {
  return request({
    url: buildApiUrl('/admin/whitelist/refresh'),
    method: 'post',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 获取所有启用的IP白名单规则
 * @returns {Promise} 请求结果
 */
export function getEnabledWhitelist() {
  return request({
    url: buildApiUrl('/admin/whitelist/enabled'),
    method: 'get',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}
