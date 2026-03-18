-- ----------------------------
-- Table structure for web_mention
-- ----------------------------
DROP TABLE IF EXISTS `web_mention`;
CREATE TABLE `web_mention` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `nid` varchar(50) DEFAULT NULL COMMENT '笔记id',
  `note_uid` varchar(50) DEFAULT NULL COMMENT '发布笔记用户id',
  `uid` varchar(50) DEFAULT NULL COMMENT '被@的用户id',
  `mention_uid` varchar(50) DEFAULT NULL COMMENT '@人的用户id',
  `content` longtext COMMENT '笔记内容',
  `creator` varchar(50) DEFAULT NULL COMMENT '创建用户',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(50) DEFAULT NULL COMMENT '修改用户',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='@提及通知';
