-- 插入一个用于测试的 admin 用户，如果手机号 '19999999999' 不存在的话
INSERT INTO t_user (phone, nickname, avatar_url, created_at, updated_at)
SELECT '19999999999', 'Admin', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE phone = '19999999999');
