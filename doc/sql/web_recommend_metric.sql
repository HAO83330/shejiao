-- 创建推荐算法指标表
CREATE TABLE `web_recommend_metric` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `algorithm_type` VARCHAR(50) DEFAULT NULL COMMENT '推荐算法类型：user_cf, item_cf, hybrid_cf',
  `precision` DOUBLE DEFAULT NULL COMMENT '准确率（Precision）',
  `recall` DOUBLE DEFAULT NULL COMMENT '召回率（Recall）',
  `f1_score` DOUBLE DEFAULT NULL COMMENT 'F1分数',
  `average_precision` DOUBLE DEFAULT NULL COMMENT '平均准确率（MAP）',
  `click_through_rate` DOUBLE DEFAULT NULL COMMENT '点击率（CTR）',
  `recommend_count` INT(11) DEFAULT 0 COMMENT '推荐数量',
  `click_count` INT(11) DEFAULT 0 COMMENT '点击数量',
  `collect_count` INT(11) DEFAULT 0 COMMENT '收藏数量',
  `comment_count` INT(11) DEFAULT 0 COMMENT '评论数量',
  `calculate_time` DATETIME DEFAULT NULL COMMENT '计算时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_algorithm_type` (`algorithm_type`),
  KEY `idx_calculate_time` (`calculate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐算法指标表';

-- 插入示例数据
INSERT INTO `web_recommend_metric` (`algorithm_type`, `precision`, `recall`, `f1_score`, `average_precision`, `click_through_rate`, `recommend_count`, `click_count`, `collect_count`, `comment_count`, `calculate_time`) VALUES
('hybrid_cf', 0.85, 0.72, 0.78, 0.82, 0.15, 1000, 150, 45, 32, CURRENT_TIMESTAMP),
('user_cf', 0.78, 0.65, 0.71, 0.75, 0.12, 1000, 120, 38, 28, CURRENT_TIMESTAMP),
('item_cf', 0.82, 0.68, 0.74, 0.79, 0.13, 1000, 130, 40, 30, CURRENT_TIMESTAMP);
