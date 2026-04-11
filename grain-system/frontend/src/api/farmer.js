import request from '@/utils/request'

export function getFarmerList(params) {
  return request({ url: '/farmer', method: 'get', params })
}

export function getFarmer(id) {
  return request({ url: `/farmer/${id}`, method: 'get' })
}

export function createFarmer(data) {
  return request({ url: '/farmer', method: 'post', data })
}

export function updateFarmer(id, data) {
  return request({ url: `/farmer/${id}`, method: 'put', data })
}

export function deleteFarmer(id) {
  return request({ url: `/farmer/${id}`, method: 'delete' })
}

export function updateFarmerAuditStatus(id, auditStatus) {
  return request({ url: `/farmer/${id}/audit-status`, method: 'put', data: { auditStatus } })
}
