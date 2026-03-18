-- 修复笔记收藏数和点赞数为负数的问题
-- 执行时间：2026-03-17
-- 用途：将收藏数和点赞数为负数的笔记修复为0

-- 1. 查看当前有多少笔记的收藏数或点赞数为负数
SELECT 
    COUNT(*) as negative_count,
    SUM(CASE WHEN collection_count < 0 THEN 1 ELSE 0 END) as negative_collection,
    SUM(CASE WHEN like_count < 0 THEN 1 ELSE 0 END) as negative_like
FROM web_note 
WHERE collection_count < 0 OR like_count < 0;

-- 2. 查看具体的负数记录（用于确认）
SELECT 
    id, 
    title, 
    collection_count, 
    like_count,
    create_time
FROM web_note 
WHERE collection_count < 0 OR like_count < 0
ORDER BY collection_count ASC, like_count ASC
LIMIT 50;

-- 3. 修复收藏数为负数的记录（将负数设为0）
UPDATE web_note 
SET collection_count = 0 
WHERE collection_count < 0;

-- 4. 修复点赞数为负数的记录（将负数设为0）
UPDATE web_note 
SET like_count = 0 
WHERE like_count < 0;

-- 5. 验证修复结果
SELECT 
    COUNT(*) as negative_count
FROM web_note 
WHERE collection_count < 0 OR like_count < 0;

-- 6. 查看修复后的记录
SELECT 
    id, 
    title, 
    collection_count, 
    like_count
FROM web_note 
WHERE id IN (
    SELECT id FROM web_note 
    WHERE collection_count = 0 OR like_count = 0
)
ORDER BY create_time DESC
LIMIT 20;
