import request from '@/utils/request'

export function getCheckPlanPage(params) {
  return request({
    url: '/inventory/check/plan',
    method: 'get',
    params
  })
}

export function getCheckPlanById(id) {
  return request({
    url: `/inventory/check/plan/${id}`,
    method: 'get'
  })
}

export function createCheckPlan(data) {
  return request({
    url: '/inventory/check/plan',
    method: 'post',
    data
  })
}

export function publishCheckPlan(id) {
  return request({
    url: `/inventory/check/plan/${id}/publish`,
    method: 'put'
  })
}

export function getCheckDetailList(planId) {
  return request({
    url: '/inventory/check/detail',
    method: 'get',
    params: { planId }
  })
}

export function updateCheckDetail(id, data) {
  return request({
    url: `/inventory/check/detail/${id}`,
    method: 'put',
    data
  })
}

export function submitCheckPlan(id) {
  return request({
    url: `/inventory/check/plan/${id}/submit`,
    method: 'put'
  })
}

export function auditCheckPlan(id, data) {
  return request({
    url: `/inventory/check/plan/${id}/audit`,
    method: 'put',
    data
  })
}

export function handleDiff(id, params) {
  return request({
    url: `/inventory/check/diff/${id}/handle`,
    method: 'put',
    params
  })
}

export function getCheckReport(id) {
  return request({
    url: `/inventory/check/plan/${id}/report`,
    method: 'get'
  })
}

export function deleteCheckPlan(id) {
  return request({
    url: `/inventory/check/plan/${id}`,
    method: 'delete'
  })
}
