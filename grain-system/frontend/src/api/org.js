import request from '@/utils/request'

export function getOrgList(params) {
  return request({ url: '/downstream/org', method: 'get', params })
}

export function getOrg(id) {
  return request({ url: `/downstream/org/${id}`, method: 'get' })
}

export function createOrg(data) {
  return request({ url: '/downstream/org', method: 'post', data })
}

export function updateOrg(id, data) {
  return request({ url: `/downstream/org/${id}`, method: 'put', data })
}

export function deleteOrg(id) {
  return request({ url: `/downstream/org/${id}`, method: 'delete' })
}

export function auditOrg(id, auditStatus, rejectReason) {
  return request({ url: `/downstream/org/${id}/audit`, method: 'put', data: { auditStatus, rejectReason } })
}
