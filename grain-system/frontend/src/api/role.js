import request from '@/utils/request'

export function getRoleList(params) {
  return request({ url: '/system/role', method: 'get', params })
}

export function getRoleListAll() {
  return request({ url: '/system/role/all', method: 'get' })
}

export function createRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}

export function updateRole(id, data) {
  return request({ url: `/system/role/${id}`, method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/system/role/${id}`, method: 'delete' })
}

export function getRolePermissions(roleId) {
  return request({ url: `/system/role/${roleId}/permissions`, method: 'get' })
}

export function assignPermissions(roleId, permissionIds) {
  return request({ url: `/system/role/${roleId}/permissions`, method: 'put', data: { permissionIds } })
}

export function getPermissionTree() {
  return request({ url: '/system/role/permission-tree', method: 'get' })
}
