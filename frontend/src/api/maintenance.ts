import request from '@/utils/request'

export interface MaintenanceQuery {
  pageNum: number
  pageSize: number
  assetName?: string
  status?: number
}

export interface MaintenanceForm {
  assetId: number
  reportUserId: number
  reportDate: string
  problemDesc: string
  maintenanceDate?: string
  maintenanceCost?: number
  maintenanceResult?: string
  status?: number
}

export const getMaintenanceList = (params: MaintenanceQuery) => {
  return request({
    url: '/asset/maintenance/page',
    method: 'get',
    params
  })
}

export const getMaintenanceDetail = (id: number) => {
  return request({
    url: `/asset/maintenance/${id}`,
    method: 'get'
  })
}

export const createMaintenance = (data: MaintenanceForm) => {
  return request({
    url: '/asset/maintenance',
    method: 'post',
    data
  })
}

export const updateMaintenance = (id: number, data: MaintenanceForm) => {
  return request({
    url: `/asset/maintenance/${id}`,
    method: 'put',
    data
  })
}

export const deleteMaintenance = (id: number) => {
  return request({
    url: `/asset/maintenance/${id}`,
    method: 'delete'
  })
}