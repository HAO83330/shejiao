-- AI对话功能数据库表结构

-- 会话表
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(255) DEFAULT '新对话' COMMENT '会话标题',
    `status` TINYINT DEFAULT 0 COMMENT '状态（0：正常 1：禁用/删除）',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建用户',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '修改用户',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '消息角色（user/assistant/system）',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `message_order` INT DEFAULT 0 COMMENT '消息顺序',
    `status` VARCHAR(20) DEFAULT 'success' COMMENT '消息状态（success/failed）',
    `model` VARCHAR(50) DEFAULT 'glm-4-flash' COMMENT '使用的模型版本',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建用户',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '修改用户',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_message_order` (`message_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表';

-- 清理可能存在的错误数据（用户ID以guest_开头的临时数据）
-- DELETE FROM ai_chat_message WHERE user_id LIKE 'guest_%';
-- DELETE FROM ai_chat_session WHERE user_id LIKE 'guest_%';
