
import request from '@/utils/request'

/**
 * 用户相关API
 * 包含登录、注册、用户信息管理、密码重置等功能
 */

// 辅助函数：构建API URL
function buildApiUrl(endpoint) {
  // 修剪尾随斜线
  const trimmedEndpoint = endpoint.replace(/\/$/, '')
  // 直接返回端点，使用baseURL自动处理前缀
  return trimmedEndpoint
}

/**
 * 添加用户（用于管理员或教师添加学生）
 * @param {Object} data 用户信息数据
 * @returns {Promise} 请求结果
 */
export function classAdd(data) {
  return request({
    url: buildApiUrl('/user'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 用户登录
 * @param {Object} data 登录数据，包含userNo、password和verifyToken
 * @returns {Promise} 请求结果，包含Token信息
 */
export function login(data) {
  // 普通用户登录接口
  const { verifyToken, ...loginData } = data
  return request({
    url: buildApiUrl('/user/login'),
    method: 'post',
    data: loginData,
    headers: {
      'X-Inline-Error': 'true',
      'X-Verify-Token': verifyToken || ''  // 传递验证token到请求头
    }
  })
}

/**
 * 创建滑块验证码
 * @returns {Promise} 验证码数据，包含背景图、滑块图和Token
 */
export function createSlideCaptcha() {
  return request({
    url: buildApiUrl('/auths/captcha/slide/create'),
    method: 'get',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 验证滑块验证码
 * @param {Object} data 验证数据，包含token和xPos
 * @returns {Promise} 验证结果
 */
export function verifySlideCaptcha(data) {
  return request({
    url: buildApiUrl('/auths/captcha/slide/verify'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 用户信息数据
 */
export function getInfo() {
  return request({
    url: buildApiUrl('/user/info'),
    method: 'get',
    headers: {
      'X-Inline-Error': 'true'
    }

  })
}

/**
 * 用户登出
 * @returns {Promise} 登出结果
 */
export function logout() {
  return request({
    url: buildApiUrl('/auths/logout'),
    method: 'delete',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 分页获取用户列表
 * @param {Object} params 查询参数，包含pageNum、pageSize等
 * @returns {Promise} 分页用户数据
 */
export function userPaging(params) {
  return request({
    url: buildApiUrl('/user/paging'),
    method: 'get',
    params,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}
/**
 * 删除用户
 * @param {string} ids 用户ID，多个ID用逗号分隔
 * @returns {Promise} 删除结果
 */
export function userDel(ids) {
  return request({
    url: buildApiUrl(`/user/${ids}`),
    method: 'delete',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}
/**
 * 批量导入用户
 * @param {FormData} data 包含用户数据的FormData对象
 * @returns {Promise} 导入结果
 */
export function userImport(data) {
  return request({
    url: buildApiUrl('/user/import'),
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data',
      'X-Inline-Error': 'true'
    },
    data
  })
}

/**
 * 修改用户密码
 * @param {Object} data 密码数据，包含旧密码和新密码
 * @returns {Promise} 修改结果
 */
export function changePassword(data) {
  return request({
    url: buildApiUrl('/user'),
    method: 'put',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 用户加入课程
 * @param {Object} params 课程参数，包含课程代码
 * @returns {Promise} 加入结果
 */
export function userAddClass(params) {
  return request({
    url: buildApiUrl('/user/subject/join'),
    method: 'put',
    params,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 用户注册
 * @param {Object} data 注册数据，包含用户信息和验证码Token
 * @returns {Promise} 注册结果
 */
export function register(data) {
  const { verifyToken, ...registerData } = data
  return request({
    url: buildApiUrl('/auths/register'),
    method: 'post',
    data: registerData,
    headers: {
      'X-Inline-Error': 'true',
      'X-Verify-Token': verifyToken || ''
    }
  })
}

/**
 * 验证教师资格证编号
 * @param {Object} params 查询参数
 * @returns {Promise} 验证结果
 */
export function checkTeacherCert(params) {
  return request({
    url: buildApiUrl('/teacher/check-cert'),
    method: 'get',
    params,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 跟踪用户在线状态（心跳）
 * @param {Object} data 心跳数据
 * @returns {Promise} 跟踪结果
 */
export function trackPresence(data) {
  return request({
    url: buildApiUrl('/auths/track-presence'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 上传用户头像
 * @param {FormData} data 包含头像文件的FormData对象
 * @returns {Promise} 上传结果，包含头像URL
 */
export function uploadAvatar(data) {
  return request({
    url: buildApiUrl('/user/uploadAvatar'),
    method: 'put',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}
/**
 * 学生退出课程
 * @returns {Promise} 退出结果
 */
export function exitUseSubject() {
  return request({
    url: buildApiUrl('/subjects/student/leave-primary-subject'),
    method: 'put',
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 发送忘记密码验证码 (旧接口，保留兼容性)
 * @param {Object} data 请求数据
 * @returns {Promise} 发送结果
 * @deprecated 请使用 sendUserVerificationCode 或 sendAdminVerificationCode
 */
export function sendVerificationCode(data) {
  return request({
    url: buildApiUrl('/auths/forgot-password/send-code'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 发送普通用户忘记密码验证码
 * @param {Object} data 请求数据
 * @returns {Promise} 发送结果
 */
export function sendUserVerificationCode(data) {
  return request({
    url: buildApiUrl('/auths/forgot-password/user/send-code'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 重置普通用户密码
 * @param {Object} data 重置数据
 * @returns {Promise} 重置结果
 */
export function resetUserPassword(data) {
  const { verifyToken, ...resetData } = data
  return request({
    url: buildApiUrl('/auths/forgot-password/user/reset'),
    method: 'post',
    data: resetData,
    headers: {
      'X-Inline-Error': 'true',
      'X-Verify-Token': verifyToken || ''
    }
  })
}

/**
 * 发送管理员忘记密码验证码
 * @param {Object} data 请求数据
 * @returns {Promise} 发送结果
 */
export function sendAdminVerificationCode(data) {
  return request({
    url: buildApiUrl('/auths/forgot-password/admin/send-code'),
    method: 'post',
    data,
    headers: {
      'X-Inline-Error': 'true'
    }
  })
}

/**
 * 重置管理员密码
 * @param {Object} data 重置数据
 * @returns {Promise} 重置结果
 */
export function resetAdminPassword(data) {
  const { verifyToken, ...resetData } = data
  return request({
    url: buildApiUrl('/auths/forgot-password/admin/reset'),
    method: 'post',
    data: resetData,
    headers: {
      'X-Inline-Error': 'true',
      'X-Verify-Token': verifyToken || ''
    }
  })
}
