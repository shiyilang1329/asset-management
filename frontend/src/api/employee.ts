import request from '@/utils/request'
import axios from 'axios'

export interface EmployeeQuery {
  pageNum: number
  pageSize: number
  name?: string
  phone?: string
  department?: string
  status?: number
}

export interface EmployeeForm {
  employeeNo?: string
  name: string
  phone?: string
  email?: string
  department?: string
  position?: string
  status?: number
}

export const getEmployeeList = () => {
  return request({
    url: '/employee/list',
    method: 'get'
  })
}

export const getEmployeePage = (params: EmployeeQuery) => {
  return request({
    url: '/employee/page',
    method: 'get',
    params
  })
}

export const createEmployee = (data: EmployeeForm) => {
  return request({
    url: '/employee',
    method: 'post',
    data
  })
}

export const updateEmployee = (id: number, data: EmployeeForm) => {
  return request({
    url: `/employee/${id}`,
    method: 'put',
    data
  })
}

export const deleteEmployee = (id: number) => {
  return request({
    url: `/employee/${id}`,
    method: 'delete'
  })
}

export const importEmployees = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/employee/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const downloadEmployeeTemplate = () => {
  return axios({
    url: '/api/employee/template',
    method: 'get',
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`
    }
  })
}
