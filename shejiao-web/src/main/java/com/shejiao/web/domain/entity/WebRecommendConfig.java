package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐算法配置实体类
 */
@Data
@TableName("web_recommend_config")
public class WebRecommendConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户CF相似用户数
     */
    private Integer userCfk;

    /**
     * 物品CF相似物品数
     */
    private Integer itemCfk;

    /**
     * 推荐数量
     */
    private Integer n;

    /**
     * 时间衰减系数
     */
    private Double lambda;

    /**
     * 用户CF权重
     */
    private Double userCfWeight;

    /**
     * 物品CF权重
     */
    private Double itemCfWeight;

    /**
     * 热门推荐权重
     */
    private Double hotWeight;

    /**
     * 最新推荐权重
     */
    private Double latestWeight;

    /**
     * 收藏权重
     */
    private Double collectWeight;

    /**
     * 评论权重
     */
    private Double commentWeight;

    /**
     * 搜索权重
     */
    private Double searchWeight;

    /**
     * 点赞权重
     */
    private Double likeWeight;

    /**
     * 浏览权重
     */
    private Double visitWeight;

    /**
     * 关注权重
     */
    private Double followWeight;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取默认配置
     */
    public static WebRecommendConfig getDefaultConfig() {
        WebRecommendConfig config = new WebRecommendConfig();
        config.setUserCfk(10);
        config.setItemCfk(15);
        config.setN(100);
        config.setLambda(0.04);
        config.setUserCfWeight(0.6);
        config.setItemCfWeight(0.4);
        config.setHotWeight(0.3);
        config.setLatestWeight(0.2);
        config.setCollectWeight(3.0);
        config.setCommentWeight(2.5);
        config.setSearchWeight(1.5);
        config.setLikeWeight(1.5);
        config.setVisitWeight(0.5);
        config.setFollowWeight(2.0);
        return config;
    }
}
