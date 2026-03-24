import request from '@/utils/request'

// 查询用户聊天列表
export function getUserChatList(params) {
  return request({
    url: '/system/chat/list',
    method: 'get',
    params
  })
}

// 删除用户聊天
export function deleteUserChat(id) {
  return request({
    url: `/system/chat/${id}`,
    method: 'delete'
  })
}

// 批量删除用户聊天
export function batchDeleteUserChat(ids) {
  return request({
    url: '/system/chat/batch',
    method: 'delete',
    data: ids
  })
}

// 导出用户聊天列表
export function exportUserChatList(params) {
  return request({
    url: '/system/chat/export',
    method: 'post',
    params,
    responseType: 'blob'
  })
}