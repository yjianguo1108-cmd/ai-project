import request from '@/utils/request'

export function getOrderList(params) {
  return request({
    url: '/purchase/order',
    method: 'get',
    params
  })
}

export function getOrder(id) {
  return request({
    url: `/purchase/order/${id}`,
    method: 'get'
  })
}

export function createOrder(data) {
  return request({
    url: '/purchase/order',
    method: 'post',
    data
  })
}

export function updateOrder(id, data) {
  return request({
    url: `/purchase/order/${id}`,
    method: 'put',
    data
  })
}

export function deleteOrder(id) {
  return request({
    url: `/purchase/order/${id}`,
    method: 'delete'
  })
}

export function auditOrder(id) {
  return request({
    url: `/purchase/order/${id}/audit`,
    method: 'put'
  })
}

export function completeOrder(id) {
  return request({
    url: `/purchase/order/${id}/complete`,
    method: 'put'
  })
}

export function voidOrder(id, reason) {
  return request({
    url: `/purchase/order/${id}/void`,
    method: 'put',
    data: { reason }
  })
}