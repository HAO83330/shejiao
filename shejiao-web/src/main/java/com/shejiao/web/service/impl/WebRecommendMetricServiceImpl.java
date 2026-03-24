package com.shejiao.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shejiao.web.domain.entity.WebLikeOrCollect;
import com.shejiao.web.domain.entity.WebRecommendMetric;
import com.shejiao.web.domain.entity.WebVisit;
import com.shejiao.web.domain.entity.WebComment;
import com.shejiao.web.mapper.WebLikeOrCollectMapper;
import com.shejiao.web.mapper.WebRecommendMetricMapper;
import com.shejiao.web.mapper.SysVisitMapper;
import com.shejiao.web.mapper.WebCommentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐算法指标计算服务实现类
 */
@Service
@Slf4j
public class WebRecommendMetricServiceImpl {

    @Autowired
    private WebRecommendMetricMapper metricMapper;

    @Autowired
    private SysVisitMapper visitMapper;

    @Autowired
    private WebLikeOrCollectMapper likeOrCollectMapper;

    @Autowired
    private WebCommentMapper commentMapper;

    /**
     * 计算推荐算法指标
     * 基于用户的实际点击、收藏、评论等行为
     */
    public void calculateMetrics() {
        log.info("开始计算推荐算法指标");

        // 计算混合协同过滤指标
        calculateMetricForAlgorithm("hybrid_cf");

        // 计算用户协同过滤指标
        calculateMetricForAlgorithm("user_cf");

        // 计算物品协同过滤指标
        calculateMetricForAlgorithm("item_cf");

        log.info("推荐算法指标计算完成");
    }

    /**
     * 为特定算法计算指标
     * @param algorithmType 算法类型
     */
    private void calculateMetricForAlgorithm(String algorithmType) {
        try {
            // 获取推荐相关的访问记录
            QueryWrapper<WebVisit> visitQuery = new QueryWrapper<>();
            visitQuery.eq("behavior", "推荐");
            visitQuery.eq("other_data", algorithmType);
            List<WebVisit> recommendVisits = visitMapper.selectList(visitQuery);

            // 统计推荐次数
            int recommendCount = recommendVisits.size();

            // 统计点击次数
            int clickCount = 0;
            Map<Long, String> recommendedItems = new HashMap<>();

            for (WebVisit visit : recommendVisits) {
                if (visit.getModuleUid() != null && visit.getUserUid() != null) {
                    try {
                        recommendedItems.put(Long.parseLong(visit.getModuleUid()), visit.getUserUid());
                    } catch (NumberFormatException e) {
                        log.warn("解析推荐记录的moduleUid失败: {}", visit.getModuleUid());
                    }
                }
            }

            // 统计用户对推荐内容的点击
            QueryWrapper<WebVisit> clickQuery = new QueryWrapper<>();
            clickQuery.eq("behavior", "点击了文章");
            List<WebVisit> clickVisits = visitMapper.selectList(clickQuery);

            for (WebVisit click : clickVisits) {
                if (click.getUserUid() != null && click.getModuleUid() != null) {
                    try {
                        String userId = click.getUserUid();
                        Long noteId = Long.parseLong(click.getModuleUid());
                        if (recommendedItems.containsKey(noteId) && recommendedItems.get(noteId).equals(userId)) {
                            clickCount++;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析点击记录的moduleUid失败: {}", click.getModuleUid());
                    }
                }
            }

            // 统计收藏次数
            int collectCount = 0;
            QueryWrapper<WebLikeOrCollect> collectQuery = new QueryWrapper<>();
            collectQuery.eq("type", 3); // 3表示收藏
            List<WebLikeOrCollect> collects = likeOrCollectMapper.selectList(collectQuery);

            for (WebLikeOrCollect collect : collects) {
                if (collect.getUid() != null && collect.getLikeOrCollectionId() != null) {
                    try {
                        String userId = collect.getUid();
                        Long noteId = Long.parseLong(collect.getLikeOrCollectionId());
                        if (recommendedItems.containsKey(noteId) && recommendedItems.get(noteId).equals(userId)) {
                            collectCount++;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析收藏记录的likeOrCollectionId失败: {}", collect.getLikeOrCollectionId());
                    }
                }
            }

            // 统计评论次数
            int commentCount = 0;
            QueryWrapper<WebComment> commentQuery = new QueryWrapper<>();
            List<WebComment> comments = commentMapper.selectList(commentQuery);

            for (WebComment comment : comments) {
                if (comment.getUid() != null && comment.getNid() != null) {
                    try {
                        String userId = comment.getUid();
                        Long noteId = Long.parseLong(comment.getNid());
                        if (recommendedItems.containsKey(noteId) && recommendedItems.get(noteId).equals(userId)) {
                            commentCount++;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析评论记录的nid失败: {}", comment.getNid());
                    }
                }
            }

            // 计算准确率（Precision）
            double precision = recommendCount > 0 ? (double) clickCount / recommendCount : 0;

            // 计算召回率（Recall）
            // 这里简化处理，假设所有用户点击的内容都应该被推荐
            double recall = clickVisits.size() > 0 ? (double) clickCount / clickVisits.size() : 0;

            // 计算F1分数
            double f1Score = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0;

            // 计算点击率（CTR）
            double clickThroughRate = recommendCount > 0 ? (double) clickCount / recommendCount : 0;

            // 计算收藏率
            double collectRate = recommendCount > 0 ? (double) collectCount / recommendCount : 0;

            // 计算评论率
            double commentRate = recommendCount > 0 ? (double) commentCount / recommendCount : 0;

            // 创建或更新指标记录
            WebRecommendMetric metric = new WebRecommendMetric();
            metric.setAlgorithmType(algorithmType);
            metric.setPrecision(precision);
            metric.setRecall(recall);
            metric.setF1Score(f1Score);
            metric.setAveragePrecision(precision); // 简化处理，使用准确率作为平均准确率
            metric.setClickThroughRate(clickThroughRate);
            metric.setRecommendCount(recommendCount);
            metric.setClickCount(clickCount);
            metric.setCollectCount(collectCount);
            metric.setCommentCount(commentCount);
            metric.setCalculateTime(LocalDateTime.now());

            // 检查是否已存在该算法的指标记录
            QueryWrapper<WebRecommendMetric> metricQuery = new QueryWrapper<>();
            metricQuery.eq("algorithm_type", algorithmType);
            List<WebRecommendMetric> existingMetrics = metricMapper.selectList(metricQuery);

            if (existingMetrics != null && !existingMetrics.isEmpty()) {
                // 更新现有记录
                metric.setId(existingMetrics.get(0).getId());
                metric.setCreateTime(existingMetrics.get(0).getCreateTime());
                metricMapper.updateById(metric);
            } else {
                // 创建新记录
                metricMapper.insert(metric);
            }

            log.info("算法 {} 指标计算完成: precision={}, recall={}, f1Score={}, ctr={}",
                    algorithmType, precision, recall, f1Score, clickThroughRate);

        } catch (Exception e) {
            log.error("计算算法 {} 指标失败: {}", algorithmType, e.getMessage());
        }
    }
}
