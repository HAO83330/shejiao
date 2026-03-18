import request from '@/utils/request'

// 查询评论数据列表
export function listData(query) {
  return request({
    url: '/web/comment/data/list',
    method: 'get',
    params: query
  })
}

// 查询评论数据详细
export function getData(commentId) {
  return request({
    url: '/web/comment/data/' + commentId,
    method: 'get'
  })
}

// 新增评论数据
export function addData(data) {
  return request({
    url: '/web/comment/data',
    method: 'post',
    data: data
  })
}

// 修改评论数据
export function updateData(data) {
  return request({
    url: '/web/comment/data',
    method: 'put',
    data: data
  })
}

// 删除评论数据
export function delData(commentId) {
  return request({
    url: '/web/comment/data/' + commentId,
    method: 'delete'
  })
}
