-- 创建推荐算法配置表
CREATE TABLE `web_recommend_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_cfk` INT(11) DEFAULT 10 COMMENT '用户CF相似用户数',
  `item_cfk` INT(11) DEFAULT 15 COMMENT '物品CF相似物品数',
  `n` INT(11) DEFAULT 100 COMMENT '推荐数量',
  `lambda` DOUBLE DEFAULT 0.04 COMMENT '时间衰减系数',
  `user_cf_weight` DOUBLE DEFAULT 0.6 COMMENT '用户CF权重',
  `item_cf_weight` DOUBLE DEFAULT 0.4 COMMENT '物品CF权重',
  `hot_weight` DOUBLE DEFAULT 0.3 COMMENT '热门推荐权重',
  `latest_weight` DOUBLE DEFAULT 0.2 COMMENT '最新推荐权重',
  `collect_weight` DOUBLE DEFAULT 3.0 COMMENT '收藏权重',
  `comment_weight` DOUBLE DEFAULT 2.5 COMMENT '评论权重',
  `search_weight` DOUBLE DEFAULT 1.5 COMMENT '搜索权重',
  `like_weight` DOUBLE DEFAULT 1.5 COMMENT '点赞权重',
  `visit_weight` DOUBLE DEFAULT 0.5 COMMENT '浏览权重',
  `follow_weight` DOUBLE DEFAULT 2.0 COMMENT '关注权重',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐算法配置表';

-- 插入默认配置数据
INSERT INTO `web_recommend_config` (`user_cfk`, `item_cfk`, `n`, `lambda`, `user_cf_weight`, `item_cf_weight`, `hot_weight`, `latest_weight`, `collect_weight`, `comment_weight`, `search_weight`, `like_weight`, `visit_weight`, `follow_weight`) VALUES
(10, 15, 100, 0.04, 0.6, 0.4, 0.3, 0.2, 3.0, 2.5, 1.5, 1.5, 0.5, 2.0);
