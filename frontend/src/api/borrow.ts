import request from '@/utils/request'

export interface BorrowQuery {
  pageNum: number
  pageSize: number
}

export interface BorrowForm {
  assetId: number
  borrowerId: number
  borrowDate: string
  expectReturnDate?: string
  borrowReason?: string
}

export const getBorrowList = (params: BorrowQuery) => {
  return request({
    url: '/asset/borrow/page',
    method: 'get',
    params
  })
}

export const createBorrow = (data: BorrowForm) => {
  return request({
    url: '/asset/borrow',
    method: 'post',
    data
  })
}

export const returnAsset = (id: number, returnDate: string) => {
  return request({
    url: `/asset/borrow/${id}/return`,
    method: 'post',
    params: { returnDate }
  })
}

export const deleteBorrow = (id: number) => {
  return request({
    url: `/asset/borrow/${id}`,
    method: 'delete'
  })
}

export const getAvailableAssets = () => {
  return request({
    url: '/asset/borrow/available-assets',
    method: 'get'
  })
}
