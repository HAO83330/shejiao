import request from '@/utils/request';

// AI聊天管理相关API

/**
 * 获取聊天会话列表
 */
export function getList(params) {
  return request({
    url: '/system/ai/chat/list',
    method: 'get',
    params
  });
}

/**
 * 获取会话详情
 */
export function getSessionDetail(sessionId) {
  return request({
    url: `/system/ai/${sessionId}`,
    method: 'get'
  });
}

/**
 * 获取会话消息列表
 */
export function getSessionMessages(sessionId) {
  return request({
    url: `/system/ai/message/list`,
    method: 'get',
    params: { sessionId }
  });
}

/**
 * 删除聊天会话
 */
export function deleteChat(sessionId) {
  return request({
    url: `/system/ai/chat/${sessionId}`,
    method: 'delete'
  });
}

/**
 * 批量删除聊天会话
 */
export function batchDeleteChat(sessionIds) {
  return request({
    url: '/system/ai/chat/batch',
    method: 'delete',
    data: sessionIds
  });
}

/**
 * 获取消息列表
 */
export function getMessageList(params) {
  return request({
    url: '/system/ai/message/list',
    method: 'get',
    params
  });
}

/**
 * 删除消息
 */
export function deleteMessage(messageId) {
  return request({
    url: `/system/ai/message/${messageId}`,
    method: 'delete'
  });
}

/**
 * 批量删除消息
 */
export function batchDeleteMessage(messageIds) {
  return request({
    url: '/system/ai/message/batch',
    method: 'delete',
    data: messageIds
  });
}

/**
 * 导出聊天会话列表
 */
export function exportChatList(params) {
  return request({
    url: '/system/ai/chat/export',
    method: 'post',
    params,
    responseType: 'blob'
  });
}

/**
 * 导出消息列表
 */
export function exportMessageList(params) {
  return request({
    url: '/system/ai/message/export',
    method: 'post',
    params,
    responseType: 'blob'
  });
}
