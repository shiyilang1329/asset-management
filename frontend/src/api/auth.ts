import request from '@/utils/request'

export interface LoginForm {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  realName: string
}

export interface CaptchaResponse {
  captchaId: string
  captchaImage: string
}

export const getCaptcha = () => {
  return request({
    url: '/auth/captcha',
    method: 'get'
  })
}

export const login = (data: LoginForm) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
