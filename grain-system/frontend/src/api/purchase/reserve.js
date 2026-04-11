import request from '@/utils/request'

export function getReserveList(params) {
  return request({
    url: '/purchase/reserve',
    method: 'get',
    params
  })
}

export function getReserve(id) {
  return request({
    url: `/purchase/reserve/${id}`,
    method: 'get'
  })
}

export function createReserve(data) {
  return request({
    url: '/purchase/reserve',
    method: 'post',
    data
  })
}

export function updateReserve(id, data) {
  return request({
    url: `/purchase/reserve/${id}`,
    method: 'put',
    data
  })
}

export function deleteReserve(id) {
  return request({
    url: `/purchase/reserve/${id}`,
    method: 'delete'
  })
}

export function auditReserve(id, auditStatus, rejectReason) {
  return request({
    url: `/purchase/reserve/${id}/audit`,
    method: 'put',
    data: { auditStatus, rejectReason }
  })
}

export function scheduleReserve(id) {
  return request({
    url: `/purchase/reserve/${id}/schedule`,
    method: 'put'
  })
}

export function cancelReserve(id, reason) {
  return request({
    url: `/purchase/reserve/${id}/cancel`,
    method: 'put',
    data: { reason }
  })
}