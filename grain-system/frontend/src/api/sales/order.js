import request from '@/utils/request'

export function getSalesOrderList(params) {
  return request({
    url: '/sales/order',
    method: 'get',
    params
  })
}

export function getSalesOrder(id) {
  return request({
    url: `/sales/order/${id}`,
    method: 'get'
  })
}

export function createSalesOrder(data) {
  return request({
    url: '/sales/order',
    method: 'post',
    data
  })
}

export function auditSalesOrder(id, status, rejectReason) {
  return request({
    url: `/sales/order/${id}/audit`,
    method: 'put',
    params: { status, rejectReason }
  })
}

export function voidSalesOrder(id, reason) {
  return request({
    url: `/sales/order/${id}/void`,
    method: 'post',
    params: { reason }
  })
}

export function submitSalesOrder(id) {
  return request({
    url: `/sales/order/${id}/submit`,
    method: 'put'
  })
}

export function getOutboundList(params) {
  return request({
    url: '/sales/outbound',
    method: 'get',
    params
  })
}

export function getOutboundById(id) {
  return request({
    url: `/sales/outbound/${id}`,
    method: 'get'
  })
}

export function startOutbound(id) {
  return request({
    url: `/sales/outbound/${id}/start`,
    method: 'put'
  })
}

export function confirmOutbound(id, actualWeight, attachmentUrl) {
  return request({
    url: `/sales/outbound/${id}/confirm`,
    method: 'put',
    params: { actualWeight, attachmentUrl }
  })
}

export function addOutbound(id, planWeight) {
  return request({
    url: `/sales/order/${id}/outbound/add`,
    method: 'post',
    params: { planWeight }
  })
}

export function voidOutbound(id, reason) {
  return request({
    url: `/sales/outbound/${id}/void`,
    method: 'post',
    params: { reason }
  })
}

export function getAvailableOrdersForOutbound() {
  return request({
    url: '/sales/order/available-outbound',
    method: 'get'
  })
}

export function getAvailableOrdersForPayment() {
  return request({
    url: '/sales/order/available-payment',
    method: 'get'
  })
}

export function getSalesPaymentList(params) {
  return request({
    url: '/sales/payment',
    method: 'get',
    params
  })
}

export function getSalesPayment(id) {
  return request({
    url: `/sales/payment/${id}`,
    method: 'get'
  })
}

export function receivePayment(id, data) {
  return request({
    url: `/sales/payment/${id}/receive`,
    method: 'post',
    data
  })
}

export function addRemind(orderId, content) {
  return request({
    url: `/sales/payment/collection/${orderId}/remind`,
    method: 'post',
    params: { content }
  })
}
