import request from '@/utils/request'

export function getMenuList() {
  return request({
    url: '/system/menu/list',
    method: 'get'
  })
}

export function getMenuTree() {
  return request({
    url: '/system/menu/tree',
    method: 'get'
  })
}

export function getMenu(id) {
  return request({
    url: `/system/menu/${id}`,
    method: 'get'
  })
}

export function createMenu(data) {
  return request({
    url: '/system/menu',
    method: 'post',
    data
  })
}

export function updateMenu(id, data) {
  return request({
    url: `/system/menu/${id}`,
    method: 'put',
    data
  })
}

export function deleteMenu(id) {
  return request({
    url: `/system/menu/${id}`,
    method: 'delete'
  })
}
