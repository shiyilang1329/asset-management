import request from '@/utils/request'

export interface DepartmentForm {
  deptCode: string
  deptName: string
  sortOrder?: number
  status?: number
}

export const getDepartmentList = () => {
  return request({
    url: '/department/list',
    method: 'get'
  })
}

export const getAllDepartments = () => {
  return request({
    url: '/department/all',
    method: 'get'
  })
}

export const createDepartment = (data: DepartmentForm) => {
  return request({
    url: '/department',
    method: 'post',
    data
  })
}

export const updateDepartment = (id: number, data: DepartmentForm) => {
  return request({
    url: `/department/${id}`,
    method: 'put',
    data
  })
}

export const deleteDepartment = (id: number) => {
  return request({
    url: `/department/${id}`,
    method: 'delete'
  })
}
