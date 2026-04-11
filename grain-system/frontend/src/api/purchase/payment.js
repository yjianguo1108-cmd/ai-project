import request from '@/utils/request'

export function getPaymentList(params) {
  return request({
    url: '/purchase/payment',
    method: 'get',
    params
  })
}

export function getPayment(id) {
  return request({
    url: `/purchase/payment/${id}`,
    method: 'get'
  })
}

export function createPayment(data) {
  return request({
    url: '/purchase/payment',
    method: 'post',
    data
  })
}

export function deletePayment(id) {
  return request({
    url: `/purchase/payment/${id}`,
    method: 'delete'
  })
}