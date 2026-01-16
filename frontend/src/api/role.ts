import request from '@/utils/request'

export const getRoleList = (params: any) => {
  return request.get('/api/system/role/page', { params })
}

export const getAllRoles = () => {
  return request.get('/api/system/role/all')
}

export const createRole = (data: any) => {
  return request.post('/api/system/role', data)
}

export const updateRole = (id: number, data: any) => {
  return request.put(`/api/system/role/${id}`, data)
}

export const deleteRole = (id: number) => {
  return request.delete(`/api/system/role/${id}`)
}

export const getUserRoles = (userId: number) => {
  return request.get(`/api/system/role/user/${userId}`)
}

export const assignRoles = (data: any) => {
  return request.post('/api/system/role/assign', data)
}
