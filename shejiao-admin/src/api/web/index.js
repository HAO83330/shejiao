import request from '@/utils/request'

export function init(params) {
  return request({
    url: '/index/init',
    method: 'get',
    params
  })
}

export function getVisitByWeek(params) {
  return request({
    url: '/index/getVisitByWeek',
    method: 'get',
    params
  })
}

export function getBlogCountByTag(params) {
  return request({
    url: '/index/getBlogCountByTag',
    method: 'get',
    params
  })
}

export function getBlogCountByBlogSort(params) {
  return request({
    url: '/index/getBlogCountByBlogSort',
    method: 'get',
    params
  })
}

export function getBlogContributeCount(params) {
  return request({
    url: '/index/getBlogContributeCount',
    method: 'get',
    params
  })
}

export function getUserGrowthTrend(params) {
  return request({
    url: '/index/getUserGrowthTrend',
    method: 'get',
    params
  })
}

export function exportDashboardData(params) {
  return request({
    url: '/index/export',
    method: 'post',
    params,
    responseType: 'blob'
  })
}
