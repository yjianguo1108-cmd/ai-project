import request from '@/utils/request'

export function getInboundList(params) {
  return request({
    url: '/warehouse/inbound',
    method: 'get',
    params
  })
}

export function getInbound(id) {
  return request({
    url: `/warehouse/inbound/${id}`,
    method: 'get'
  })
}

export function createInbound(data) {
  return request({
    url: '/warehouse/inbound',
    method: 'post',
    data
  })
}

export function confirmInbound(id) {
  return request({
    url: `/warehouse/inbound/${id}/confirm`,
    method: 'put'
  })
}

export function deleteInbound(id) {
  return request({
    url: `/warehouse/inbound/${id}`,
    method: 'delete'
  })
}

export function getAvailableInboundOrders() {
  return request({
    url: '/warehouse/inbound/available-orders',
    method: 'get'
  })
}