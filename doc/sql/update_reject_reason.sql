-- 为 web_note 表添加 reject_reason 字段（驳回理由）
-- 执行时间：2026-03-06
-- 用途：支持笔记审核驳回功能，允许管理员填写和编辑驳回理由

-- 1. 添加 reject_reason 字段
ALTER TABLE `web_note` 
ADD COLUMN `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回理由' AFTER `audit_status`;

-- 说明:
-- - 字段类型：varchar(500)，最多存储 500 个字符
-- - 默认值：NULL，表示没有填写驳回理由
-- - 位置：紧跟在 audit_status 字段后面
-- - 用途：存储管理员审核驳回时填写的理由，可选填

-- 2. 查询示例（验证字段是否添加成功）
-- SELECT id, title, audit_status, reject_reason FROM web_note WHERE audit_status = '2' LIMIT 10;
