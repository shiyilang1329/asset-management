import request from '@/utils/request'

export interface CategoryForm {
  categoryCode: string
  categoryName: string
  parentId: number
  level: number
  sortOrder: number
}

export const getCategoryTree = () => {
  return request({
    url: '/asset/category/tree',
    method: 'get'
  })
}

export const createCategory = (data: CategoryForm) => {
  return request({
    url: '/asset/category',
    method: 'post',
    data
  })
}

export const updateCategory = (id: number, data: CategoryForm) => {
  return request({
    url: `/asset/category/${id}`,
    method: 'put',
    data
  })
}

export const deleteCategory = (id: number) => {
  return request({
    url: `/asset/category/${id}`,
    method: 'delete'
  })
}
