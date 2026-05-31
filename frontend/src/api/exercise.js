
import request from '@/utils/request'

export function exercisePaging(params) {
  return request({
    url: 'exercises/getRepo',
    method: 'get',
    params
  })
}

export function getQuestion(repoId, params) {
  return request({
    url: 'exercises/' + repoId,
    method: 'get',
    params
  })
}

export function getQuestionDetail(id) {
  return request({
    url: `exercises/question/${id}`,
    method: 'get'
  })
}

export function submitAnswer(data) {
  return request({
    url: `exercises/fillAnswer`,
    method: 'post',
    data
  })
}

export function getAnswerInfo(repoId, quId) {
  return request({
    url: `exercises/answerInfo/${repoId}/${quId}`,
    method: 'get'
  })
}

export function clearRecord(repoId) {
  return request({
    url: `exercises/clearRecord/${repoId}`,
    method: 'delete'
  })
}

