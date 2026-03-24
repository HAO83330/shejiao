import request from '@/utils/request'

// 根据用户ID获取用户信息（web_user表）
export function getUserById(id) {
  return request({
    url: `/member/${id}`,
    method: 'get'
  })
}

// 批量获取用户信息（web_user表）
export function getUsersByIds(ids) {
  return request({
    url: '/member/batch',
    method: 'post',
    data: ids
  })
}