import request from '@/utils/request'

export const getPermissionTree = () => {
  return request.get('/api/system/permission/tree')
}

export const createPermission = (data: any) => {
  return request.post('/api/system/permission', data)
}

export const updatePermission = (id: number, data: any) => {
  return request.put(`/api/system/permission/${id}`, data)
}

export const deletePermission = (id: number) => {
  return request.delete(`/api/system/permission/${id}`)
}

export const getRolePermissions = (roleId: number) => {
  return request.get(`/api/system/permission/role/${roleId}`)
}

export const assignPermissions = (data: any) => {
  return request.post('/api/system/permission/assign', data)
}
