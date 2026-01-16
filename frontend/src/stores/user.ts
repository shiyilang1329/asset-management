import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref<number | null>(null)
  const username = ref('')
  const realName = ref('')
  const permissions = ref<string[]>([])

  const setUserInfo = (userInfo: any) => {
    token.value = userInfo.token
    userId.value = userInfo.userId
    username.value = userInfo.username
    realName.value = userInfo.realName
    permissions.value = userInfo.permissions || []
    
    localStorage.setItem('token', userInfo.token)
    localStorage.setItem('userId', String(userInfo.userId))
    localStorage.setItem('username', userInfo.username)
    localStorage.setItem('realName', userInfo.realName || '')
    localStorage.setItem('permissions', JSON.stringify(userInfo.permissions || []))
  }

  const loadUserInfo = () => {
    const storedToken = localStorage.getItem('token')
    const storedUserId = localStorage.getItem('userId')
    const storedUsername = localStorage.getItem('username')
    const storedRealName = localStorage.getItem('realName')
    const storedPermissions = localStorage.getItem('permissions')
    
    if (storedToken) {
      token.value = storedToken
    }
    if (storedUserId) {
      userId.value = Number(storedUserId)
    }
    if (storedUsername) {
      username.value = storedUsername
    }
    if (storedRealName) {
      realName.value = storedRealName
    }
    if (storedPermissions) {
      permissions.value = JSON.parse(storedPermissions)
    }
  }

  const loadPermissions = () => {
    const stored = localStorage.getItem('permissions')
    if (stored) {
      permissions.value = JSON.parse(stored)
    }
  }

  const hasPermission = (permission: string) => {
    return permissions.value.includes(permission)
  }

  const clearUserInfo = () => {
    token.value = ''
    userId.value = null
    username.value = ''
    realName.value = ''
    permissions.value = []
    
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('permissions')
  }

  return {
    token,
    userId,
    username,
    realName,
    permissions,
    setUserInfo,
    loadUserInfo,
    loadPermissions,
    hasPermission,
    clearUserInfo
  }
})
