import request from '@/utils/request'

// 获取推荐算法配置
export function getRecommendConfig() {
  return request({
    url: '/web/recommend/config',
    method: 'get'
  })
}

// 更新推荐算法配置
export function updateRecommendConfig(data) {
  return request({
    url: '/web/recommend/config',
    method: 'put',
    data: data
  })
}

// 重置推荐算法配置为默认值
export function resetRecommendConfig() {
  return request({
    url: '/web/recommend/config/reset',
    method: 'post'
  })
}

// 获取推荐算法指标
export function getRecommendMetrics() {
  return request({
    url: '/web/recommend/metrics',
    method: 'get'
  })
}

// 获取指定算法类型的指标
export function getRecommendMetricsByType(algorithmType) {
  return request({
    url: `/web/recommend/metrics/${algorithmType}`,
    method: 'get'
  })
}

// 测试推荐算法
export function testRecommend(userId, pageSize) {
  return request({
    url: `/web/recommend/note/${userId}`,
    method: 'get',
    params: { pageSize }
  })
}

// 获取用户信息
export function getUserInfo(userId) {
  return request({
    url: `/web/recommend/user/${userId}`,
    method: 'get'
  })
}
