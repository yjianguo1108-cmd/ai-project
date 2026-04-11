import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const roles = ref([])
  const permissions = ref([])

  async function loginAction(username, password) {
    try {
      const res = await login({ username, password })
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      await getUserInfoAction()
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      return false
    }
  }

  async function getUserInfoAction() {
    try {
      const res = await getUserInfo()
      const data = res.data
      userInfo.value = data
      roles.value = data.roles || []
      permissions.value = data.permissions || []
      localStorage.setItem('userInfo', JSON.stringify(data))
      localStorage.setItem('roles', JSON.stringify(roles.value))
      localStorage.setItem('permissions', JSON.stringify(permissions.value))
      return data
    } catch (error) {
      logout()
      throw error
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
  }

  function hasRole(role) {
    return roles.value.includes(role)
  }

  function hasPermission(permission) {
    return permissions.value.includes(permission)
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    loginAction,
    getUserInfoAction,
    logout,
    hasRole,
    hasPermission
  }
})
