import request from '@/utils/request'
import axios from 'axios'

export interface AssetQuery {
  pageNum: number
  pageSize: number
  assetName?: string
  assetNo?: string
  categoryId?: number
  status?: number
}

export interface AssetForm {
  assetNo: string
  assetName: string
  categoryId: number
  brand?: string
  model?: string
  purchasePrice?: number
  purchaseDate?: string
  supplier?: string
  location?: string
  remark?: string
}

export const getAssetList = (params: AssetQuery) => {
  return request({
    url: '/asset/page',
    method: 'get',
    params
  })
}

export const getAssetDetail = (id: number) => {
  return request({
    url: `/asset/${id}`,
    method: 'get'
  })
}

export const createAsset = (data: AssetForm) => {
  return request({
    url: '/asset',
    method: 'post',
    data
  })
}

export const updateAsset = (id: number, data: AssetForm) => {
  return request({
    url: `/asset/${id}`,
    method: 'put',
    data
  })
}

export const deleteAsset = (id: number) => {
  return request({
    url: `/asset/${id}`,
    method: 'delete'
  })
}

export const getAssetStatistics = () => {
  return request({
    url: '/asset/statistics',
    method: 'get'
  })
}

export const importAssets = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/asset/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const downloadAssetTemplate = () => {
  return axios({
    url: '/api/asset/template',
    method: 'get',
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`
    }
  })
}

export const getAvailableForMaintenance = (params: AssetQuery) => {
  return request({
    url: '/asset/available-for-maintenance',
    method: 'get',
    params
  })
}
