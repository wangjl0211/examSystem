/**
 * 认证工具模块
 * 管理用户Token、用户ID、角色等认证相关信息的存储和获取
 */
import Cookies from 'js-cookie'

// Cookie和localStorage的键名定义
const TokenKey = 'Authorization'
const UserIdKey = 'class_activity_system_user_id'
const roleKey = 'class_activity_system_role'
const discussionKey = 'class_activity_system_discussion_id'
const subjectKey = 'class_activity_system_subject_id'

// 获取Token
export function getToken() {
  return Cookies.get(TokenKey)
}

// 设置 token 时添加过期时间
export function setToken(token) {
  return Cookies.set(TokenKey, token, { expires: 30 / (24 * 60) }) // 30分钟过期
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}


export function getUserId() {
  return localStorage.getItem(UserIdKey)
}
export function setUserId(userId) {
  return localStorage.setItem(UserIdKey, userId)
}

export function removeUserId() {
  return localStorage.removeItem(UserIdKey)
}

export function getRoleFromStorage() {
  return localStorage.getItem(roleKey)
}

export function setRole(role) {
  return localStorage.setItem(roleKey, role)
}

export function removeRole() {
  return localStorage.removeItem(roleKey)
}

export function getDiscussionId() {
  return localStorage.getItem(discussionKey)
}

export function setDiscussionId(id) {
  return localStorage.setItem(discussionKey, id)
}

export function removeDiscussionId() {
  return localStorage.removeItem(discussionKey)
}

export function getsubjectId() {
  return localStorage.getItem(subjectKey)
}

export function setsubjectId(id) {
  return localStorage.setItem(subjectKey, id)
}

export function removesubjectId() {
  return localStorage.removeItem(subjectKey)
}