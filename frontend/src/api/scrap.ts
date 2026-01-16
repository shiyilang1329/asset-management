import request from '@/utils/request'

export const getScrapList = (params: any) => {
  return request.get('/api/asset/scrap/page', { params })
}

export const createScrap = (data: any) => {
  return request.post('/api/asset/scrap', data)
}

export const deleteScrap = (id: number) => {
  return request.delete(`/api/asset/scrap/${id}`)
}
