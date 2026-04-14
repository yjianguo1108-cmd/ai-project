import request from '@/utils/request'

export function getOutboundPage(params) {
  return request({
    url: '/inventory/check/outbound',
    method: 'get',
    params
  })
}

export function getOutboundById(id) {
  return request({
    url: `/inventory/check/outbound/${id}`,
    method: 'get'
  })
}

export function createOutbound(data) {
  return request({
    url: '/inventory/check/outbound',
    method: 'post',
    data
  })
}

export function confirmOutbound(id) {
  return request({
    url: `/inventory/check/outbound/${id}/confirm`,
    method: 'put'
  })
}

export function voidOutbound(id, reason) {
  return request({
    url: `/inventory/check/outbound/${id}/void`,
    method: 'put',
    params: { reason }
  })
}