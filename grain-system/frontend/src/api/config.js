import request from '@/utils/request'

// ===== 粮食种类 =====
export function getGrainList(params) {
  return request({ url: '/config/grain-types', method: 'get', params })
}

export function getGrainPage(params) {
  return request({ url: '/config/grain-types/page', method: 'get', params })
}

export function createGrain(data) {
  return request({ url: '/config/grain-types', method: 'post', data })
}

export function updateGrain(id, data) {
  return request({ url: `/config/grain-types/${id}`, method: 'put', data })
}

export function deleteGrain(id) {
  return request({ url: `/config/grain-types/${id}`, method: 'delete' })
}

// ===== 储位管理 =====
export function getPositionList(params) {
  return request({ url: '/config/storage-positions', method: 'get', params })
}

export function getPositionPage(params) {
  return request({ url: '/config/storage-positions/page', method: 'get', params })
}

export function createPosition(data) {
  return request({ url: '/config/storage-positions', method: 'post', data })
}

export function updatePosition(id, data) {
  return request({ url: `/config/storage-positions/${id}`, method: 'put', data })
}

export function deletePosition(id) {
  return request({ url: `/config/storage-positions/${id}`, method: 'delete' })
}

// ===== 系统参数 =====
export function getSystemParams(paramGroup) {
  return request({ url: '/config/system-params', method: 'get', params: { paramGroup } })
}

export function updateSystemParam(id, paramValue) {
  return request({ url: `/config/system-params/${id}`, method: 'put', data: { paramValue } })
}
