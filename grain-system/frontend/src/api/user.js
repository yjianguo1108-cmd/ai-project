import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/system/user',
    method: 'get',
    params
  })
}

export function getUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/system/user/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

export function updateStatus(id, status) {
  return request({
    url: `/system/user/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function resetPassword(id, password) {
  return request({
    url: `/system/user/${id}/reset-password`,
    method: 'put',
    data: { password }
  })
}

export function assignRoles(userId, roleIds) {
  return request({
    url: `/system/user/${userId}/roles`,
    method: 'put',
    data: { roleIds }
  })
}
