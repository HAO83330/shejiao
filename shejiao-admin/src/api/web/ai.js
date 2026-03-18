import request from '@/utils/request';

// AI对话相关API

/**
 * 获取AI对话列表
 */
export function listAi(params) {
  return request({
    url: '/system/ai/list',
    method: 'get',
    params
  });
}

/**
 * 获取AI对话详情
 */
export function getAi(id) {
  return request({
    url: `/system/ai/${id}`,
    method: 'get'
  });
}

/**
 * 删除AI对话
 */
export function delAi(ids) {
  return request({
    url: '/system/ai/' + ids,
    method: 'delete'
  });
}

/**
 * 刷新AI对话数据
 */
export function refreshAiDate() {
  return request({
    url: '/system/ai/refresh',
    method: 'post'
  });
}
