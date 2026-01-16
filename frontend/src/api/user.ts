import request from '@/utils/request'
import axios from 'axios'

export interface UserQuery {
  pageNum: number
  pageSize: number
  username?: string
  realName?: string
  status?: number
}

export interface UserForm {
  username: string
  password?: string
  realName?: string
  email?: string
  phone?: string
  departmentId?: number
  status?: number
}

export const getUserList = () => {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

export const getUserPage = (params: UserQuery) => {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

export const getUserDetail = (id: number) => {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export const createUser = (data: UserForm) => {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export const updateUser = (id: number, data: UserForm) => {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id: number) => {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}


export const importUsers = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const downloadUserTemplate = () => {
  return axios({
    url: '/api/user/template',
    method: 'get',
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`
    }
  })
}

export const resetPassword = (userId: number, newPassword: string) => {
  return request({
    url: `/user/${userId}/reset-password`,
    method: 'put',
    data: { newPassword }
  })
}

export const changePassword = (oldPassword: string, newPassword: string) => {
  return request({
    url: '/user/change-password',
    method: 'put',
    data: { oldPassword, newPassword }
  })
}
