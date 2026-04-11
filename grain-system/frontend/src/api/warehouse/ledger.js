import request from '@/utils/request'

export function getLedgerList(params) {
  return request({
    url: '/warehouse/ledger',
    method: 'get',
    params
  })
}

export function getLedger(id) {
  return request({
    url: `/warehouse/ledger/${id}`,
    method: 'get'
  })
}

export function adjustLedger(id, adjustWeight, reason) {
  return request({
    url: `/warehouse/ledger/${id}/adjust`,
    method: 'put',
    data: { adjustWeight, reason }
  })
}