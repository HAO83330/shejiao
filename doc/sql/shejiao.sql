-- shejiao数据库表结构
-- 生成时间：2026-03-10

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `shejiao` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `shejiao`;

-- 基础实体类对应的字段：id, creator, createTime, updater, updateTime

-- 用户表
CREATE TABLE IF NOT EXISTS `web_user` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `hs_id` BIGINT DEFAULT NULL COMMENT '红薯ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
  `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '用户状态',
  `birthday` VARCHAR(20) DEFAULT NULL COMMENT '生日',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `user_cover` VARCHAR(255) DEFAULT NULL COMMENT '用户封面',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '用户标签',
  `trend_count` BIGINT DEFAULT 0 COMMENT '笔记数量',
  `follower_count` BIGINT DEFAULT 0 COMMENT '关注数量',
  `fan_count` BIGINT DEFAULT 0 COMMENT '粉丝数量',
  `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_date` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 笔记表
CREATE TABLE IF NOT EXISTS `web_note` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '笔记标题',
  `content` TEXT NOT NULL COMMENT '笔记内容',
  `note_cover` VARCHAR(255) DEFAULT NULL COMMENT '笔记封面',
  `author` VARCHAR(50) DEFAULT NULL COMMENT '作者',
  `cpid` VARCHAR(32) DEFAULT NULL COMMENT '笔记一级分类ID',
  `count` INT DEFAULT 0 COMMENT '图片数量',
  `pinned` VARCHAR(10) DEFAULT '0' COMMENT '是否置顶',
  `audit_status` VARCHAR(10) DEFAULT '0' COMMENT '审核状态',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回理由',
  `note_type` VARCHAR(10) DEFAULT NULL COMMENT '笔记类型',
  `like_count` BIGINT DEFAULT 0 COMMENT '点赞次数',
  `collection_count` BIGINT DEFAULT 0 COMMENT '收藏次数',
  `comment_count` BIGINT DEFAULT 0 COMMENT '评论次数',
  `view_count` BIGINT DEFAULT 0 COMMENT '浏览次数',
  `note_cover_height` INT DEFAULT NULL COMMENT '笔记高度',
  `uid` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
  `cid` VARCHAR(32) DEFAULT NULL COMMENT '笔记二级分类ID',
  `urls` TEXT DEFAULT NULL COMMENT '笔记urls',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '笔记状态',
  `time` BIGINT DEFAULT NULL COMMENT '时间戳',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- 评论表
CREATE TABLE IF NOT EXISTS `web_comment` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `nid` VARCHAR(32) NOT NULL COMMENT '笔记ID',
  `note_uid` VARCHAR(32) DEFAULT NULL COMMENT '评论关联笔记的用户ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '发布评论用户',
  `pid` VARCHAR(32) DEFAULT NULL COMMENT '根评论ID',
  `reply_id` VARCHAR(32) DEFAULT NULL COMMENT '回复的评论ID',
  `reply_uid` VARCHAR(32) DEFAULT NULL COMMENT '回复评论的用户ID',
  `level` INT DEFAULT 1 COMMENT '评论等级',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` BIGINT DEFAULT 0 COMMENT '评论点赞数量',
  `two_comment_count` BIGINT DEFAULT 0 COMMENT '二级评论数量',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 关注表
CREATE TABLE IF NOT EXISTS `web_follow` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `follower_uid` VARCHAR(32) NOT NULL COMMENT '关注者ID',
  `following_uid` VARCHAR(32) NOT NULL COMMENT '被关注者ID',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '关注状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 点赞收藏表
CREATE TABLE IF NOT EXISTS `web_like_or_collect` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `target_id` VARCHAR(32) NOT NULL COMMENT '目标ID',
  `type` VARCHAR(10) NOT NULL COMMENT '类型：like/collect',
  `target_type` VARCHAR(10) NOT NULL COMMENT '目标类型：note/comment',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞收藏表';

-- 标签表
CREATE TABLE IF NOT EXISTS `web_tag` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 标签笔记关联表
CREATE TABLE IF NOT EXISTS `web_tag_note_relation` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `tag_id` VARCHAR(32) NOT NULL COMMENT '标签ID',
  `note_id` VARCHAR(32) NOT NULL COMMENT '笔记ID',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签笔记关联表';

-- 聊天表
CREATE TABLE IF NOT EXISTS `web_chat` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `chat_name` VARCHAR(50) DEFAULT NULL COMMENT '聊天名称',
  `chat_type` VARCHAR(10) DEFAULT '0' COMMENT '聊天类型',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天表';

-- 聊天用户关联表
CREATE TABLE IF NOT EXISTS `web_chat_user_relation` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `chat_id` VARCHAR(32) NOT NULL COMMENT '聊天ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天用户关联表';

-- 访问记录表
CREATE TABLE IF NOT EXISTS `web_visit` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
  `target_id` VARCHAR(32) NOT NULL COMMENT '目标ID',
  `target_type` VARCHAR(10) NOT NULL COMMENT '目标类型',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '访问IP',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问记录表';

-- 提及表
CREATE TABLE IF NOT EXISTS `web_mention` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `target_id` VARCHAR(32) NOT NULL COMMENT '目标ID',
  `target_type` VARCHAR(10) NOT NULL COMMENT '目标类型',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提及表';

-- 图片表
CREATE TABLE IF NOT EXISTS `web_picture` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `picture_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `sort_id` VARCHAR(32) DEFAULT NULL COMMENT '分类ID',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片表';

-- 图片分类表
CREATE TABLE IF NOT EXISTS `web_picture_sort` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `sort_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片分类表';

-- 文件表
CREATE TABLE IF NOT EXISTS `web_file` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `file_name` VARCHAR(100) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(255) NOT NULL COMMENT '文件路径',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
  `sort_id` VARCHAR(32) DEFAULT NULL COMMENT '分类ID',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 文件分类表
CREATE TABLE IF NOT EXISTS `web_file_sort` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `sort_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件分类表';

-- 导航栏表
CREATE TABLE IF NOT EXISTS `web_navbar` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `navbar_name` VARCHAR(50) NOT NULL COMMENT '导航栏名称',
  `navbar_url` VARCHAR(255) DEFAULT NULL COMMENT '导航栏URL',
  `parent_id` VARCHAR(32) DEFAULT '0' COMMENT '父级ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导航栏表';

-- 用户笔记关联表
CREATE TABLE IF NOT EXISTS `web_user_note_relation` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `note_id` VARCHAR(32) NOT NULL COMMENT '笔记ID',
  `type` VARCHAR(10) NOT NULL COMMENT '关联类型',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户笔记关联表';

-- 评论同步表
CREATE TABLE IF NOT EXISTS `web_comment_sync` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `comment_id` VARCHAR(32) NOT NULL COMMENT '评论ID',
  `sync_status` VARCHAR(10) DEFAULT '0' COMMENT '同步状态',
  `sync_time` DATETIME DEFAULT NULL COMMENT '同步时间',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论同步表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS `web_login_log` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '登录状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- AI聊天会话表
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `uid` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `session_name` VARCHAR(100) DEFAULT NULL COMMENT '会话名称',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话表';

-- AI聊天消息表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `session_id` VARCHAR(32) NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `config_desc` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态',
  `creator` VARCHAR(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(32) DEFAULT NULL COMMENT '修改用户',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 系统默认数据
-- 插入默认系统配置
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_desc`, `status`) VALUES
('1', 'site_name', '社交平台', '网站名称', '0'),
('2', 'site_desc', '一个功能强大的社交平台', '网站描述', '0'),
('3', 'site_keywords', '社交,聊天,分享', '网站关键词', '0'),
('4', 'site_logo', '', '网站logo', '0'),
('5', 'site_favicon', '', '网站图标', '0'),
('6', 'site_copyright', '© 2026 社交平台', '网站版权', '0'),
('7', 'site_icp', '', '网站备案号', '0'),
('8', 'admin_email', 'admin@shejiao.com', '管理员邮箱', '0'),
('9', 'notice', '', '网站公告', '0'),
('10', 'close_site', '0', '网站是否关闭', '0'),
('11', 'close_reason', '', '网站关闭原因', '0'),
('12', 'user_reg', '1', '是否允许用户注册', '0'),
('13', 'user_login', '1', '是否允许用户登录', '0'),
('14', 'post_audit', '0', '发布内容是否需要审核', '0'),
('15', 'comment_audit', '0', '评论是否需要审核', '0'),
('16', 'max_post_size', '10485760', '最大上传文件大小(字节)', '0'),
('17', 'allowed_file_types', 'jpg,jpeg,png,gif,mp4,mp3', '允许上传的文件类型', '0'),
('18', 'page_size', '20', '分页大小', '0'),
('19', 'hot_post_days', '7', '热门帖子天数', '0'),
('20', 'new_post_days', '1', '最新帖子天数', '0');

-- 插入默认导航栏
INSERT INTO `web_navbar` (`id`, `navbar_name`, `navbar_url`, `parent_id`, `sort`, `status`) VALUES
('1', '首页', '/', '0', 1, '0'),
('2', '发现', '/discover', '0', 2, '0'),
('3', '消息', '/message', '0', 3, '0'),
('4', '我的', '/user', '0', 4, '0'),
('5', '关于我们', '/about', '0', 5, '0'),
('6', '帮助中心', '/help', '0', 6, '0'),
('7', '隐私政策', '/privacy', '0', 7, '0'),
('8', '用户协议', '/agreement', '0', 8, '0');

-- 插入默认标签
INSERT INTO `web_tag` (`id`, `tag_name`, `sort`, `status`) VALUES
('1', '生活', 1, '0'),
('2', '美食', 2, '0'),
('3', '旅行', 3, '0'),
('4', '运动', 4, '0'),
('5', '科技', 5, '0'),
('6', '娱乐', 6, '0'),
('7', '学习', 7, '0'),
('8', '工作', 8, '0'),
('9', '健康', 9, '0'),
('10', '时尚', 10, '0');

-- 插入默认管理员用户
INSERT INTO `web_user` (`id`, `username`, `password`, `avatar`, `gender`, `phone`, `email`, `description`, `status`, `trend_count`, `follower_count`, `fan_count`) VALUES
('1', 'admin', '123456', '', '男', '13800138000', 'admin@shejiao.com', '系统管理员', '0', 0, 0, 0);
