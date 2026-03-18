import request from '@/utils/request'

// 查询评论列表
export function listComment(query) {
  return request({
    url: '/comment/list',
    method: 'get',
    params: query
  })
}

// 查询评论树列表（支持按笔记ID过滤）
export function treeListComment(query) {
  return request({
    url: '/comment/treeList',
    method: 'get',
    params: query
  })
}

// 查询评论详细
export function getComment(id) {
  return request({
    url: '/comment/' + id,
    method: 'get'
  })
}

// 查询会员详细
export function getCommentByNid(nid) {
  return request({
    url: '/comment/list/' + nid,
    method: 'get'
  })
}

// 删除评论
export function delComment(id) {
  return request({
    url: '/comment/' + id,
    method: 'delete'
  })
}
