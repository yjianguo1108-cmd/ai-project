import request from '@/utils/request'

export function getWeighingList(params) {
  return request({
    url: '/purchase/weighing',
    method: 'get',
    params
  })
}

export function getWeighing(id) {
  return request({
    url: `/purchase/weighing/${id}`,
    method: 'get'
  })
}

export function createWeighing(data) {
  return request({
    url: '/purchase/weighing',
    method: 'post',
    data
  })
}

export function updateWeighing(id, data) {
  return request({
    url: `/purchase/weighing/${id}`,
    method: 'put',
    data
  })
}

export function deleteWeighing(id) {
  return request({
    url: `/purchase/weighing/${id}`,
    method: 'delete'
  })
}

export function auditWeighing(id) {
  return request({
    url: `/purchase/weighing/${id}/audit`,
    method: 'put'
  })
}

export function voidWeighing(id, reason) {
  return request({
    url: `/purchase/weighing/${id}/void`,
    method: 'put',
    data: { reason }
  })
}