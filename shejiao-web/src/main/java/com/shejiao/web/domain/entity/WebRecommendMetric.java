package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐算法指标实体类
 */
@Data
@TableName("web_recommend_metric")
public class WebRecommendMetric {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 推荐算法类型：user_cf, item_cf, hybrid_cf
     */
    private String algorithmType;

    /**
     * 准确率（Precision）
     */
    @TableField("`precision`")
    private Double precision;

    /**
     * 召回率（Recall）
     */
    @TableField("`recall`")
    private Double recall;

    /**
     * F1分数
     */
    private Double f1Score;

    /**
     * 平均准确率（MAP）
     */
    private Double averagePrecision;

    /**
     * 点击率（CTR）
     */
    private Double clickThroughRate;

    /**
     * 推荐数量
     */
    private Integer recommendCount;

    /**
     * 点击数量
     */
    private Integer clickCount;

    /**
     * 收藏数量
     */
    private Integer collectCount;

    /**
     * 评论数量
     */
    private Integer commentCount;

    /**
     * 计算时间
     */
    private LocalDateTime calculateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
