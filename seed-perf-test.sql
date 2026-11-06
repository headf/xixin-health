-- ============================================================
-- 熙心健康体检管理系统 - 性能测试数据生成脚本
-- 生成约 5000+ 条数据，用于性能测试
-- ============================================================
SET NAMES utf8mb4;
USE xixin_health;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 批量生成用户账号 (5000人)
-- ============================================================
DROP PROCEDURE IF EXISTS seed_users;
DELIMITER //
CREATE PROCEDURE seed_users()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE total INT DEFAULT 5000;
  DECLARE phone_base VARCHAR(11);
  DECLARE max_id INT;

  SELECT IFNULL(MAX(id), 100) INTO max_id FROM sys_user;

  WHILE i <= total DO
    SET phone_base = CONCAT('139', LPAD(max_id + i, 8, '0'));
    INSERT IGNORE INTO sys_user (phone, display_name, password_hash, enabled, deleted, created_at, updated_at)
    VALUES (
      phone_base,
      CONCAT('测试用户', max_id + i),
      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
      1, 0,
      DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
      NOW()
    );
    SET i = i + 1;
  END WHILE;
END//
DELIMITER ;
CALL seed_users();
DROP PROCEDURE IF EXISTS seed_users;

-- 给新用户分配 NORMAL_USER 角色
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, (SELECT id FROM sys_role WHERE code = 'NORMAL_USER')
FROM sys_user u
WHERE u.phone LIKE '139%'
AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id);

SELECT CONCAT('用户数: ', COUNT(*)) AS info FROM sys_user;

-- ============================================================
-- 2. 批量生成健康档案 (每用户1份)
-- ============================================================
INSERT IGNORE INTO t_health_profile (owner_user_id, profile_type, name, phone, id_card_no, emergency_contact_name, emergency_contact_phone, deleted, created_at, updated_at)
SELECT u.id, 'SELF', u.display_name, u.phone,
  CONCAT('11010119', LPAD(FLOOR(70 + RAND() * 30), 2, '0'), LPAD(FLOOR(1 + RAND() * 12), 2, '0'), LPAD(FLOOR(1 + RAND() * 28), 2, '0'), LPAD(u.id % 10000, 4, '0')),
  CONCAT(u.display_name, '家属'),
  CONCAT('137', LPAD(u.id % 100000, 7, '0')),
  0, u.created_at, u.updated_at
FROM sys_user u
WHERE u.phone LIKE '139%'
AND NOT EXISTS (SELECT 1 FROM t_health_profile hp WHERE hp.owner_user_id = u.id);

SELECT CONCAT('健康档案数: ', COUNT(*)) AS info FROM t_health_profile;

-- ============================================================
-- 3. 批量生成预约记录 (每用户1-3条)
-- ============================================================
INSERT IGNORE INTO t_appointment (user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted)
SELECT
  u.id,
  hp.id,
  1 + FLOOR(RAND() * 4),
  1 + FLOOR(RAND() * 4),
  DATE_ADD(CURDATE(), INTERVAL -FLOOR(RAND() * 60) DAY),
  CASE FLOOR(RAND() * 3) WHEN 0 THEN '08:00-09:00' WHEN 1 THEN '09:00-10:00' ELSE '10:00-11:00' END,
  CASE FLOOR(RAND() * 5) WHEN 0 THEN 'COMPLETED' WHEN 1 THEN 'COMPLETED' WHEN 2 THEN 'COMPLETED' WHEN 3 THEN 'CHECKED_IN' ELSE 'CANCELLED' END,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY),
  NOW(),
  0
FROM sys_user u
JOIN t_health_profile hp ON hp.owner_user_id = u.id AND hp.profile_type = 'SELF'
WHERE u.phone LIKE '139%'
AND FLOOR(RAND() * 3) > 0
LIMIT 3000;

SELECT CONCAT('预约数: ', COUNT(*)) AS info FROM t_appointment;

-- ============================================================
-- 4. 批量生成订单
-- ============================================================
INSERT IGNORE INTO t_order (user_id, appointment_id, amount_cent, status, created_at, updated_at)
SELECT
  a.user_id,
  a.id,
  (1 + FLOOR(RAND() * 4)) * 10000 + FLOOR(RAND() * 9900),
  CASE a.status WHEN 'CANCELLED' THEN 'CLOSED' WHEN 'CREATED' THEN 'PENDING_PAYMENT' ELSE 'PAID' END,
  a.created_at,
  a.updated_at
FROM t_appointment a
WHERE a.status IN ('COMPLETED', 'PAID', 'CHECKED_IN', 'CANCELLED');

SELECT CONCAT('订单数: ', COUNT(*)) AS info FROM t_order;

-- ============================================================
-- 5. 批量生成支付记录
-- ============================================================
INSERT IGNORE INTO t_payment_record (order_id, payment_no, channel, amount_cent, status, paid_at, created_at)
SELECT
  o.id,
  CONCAT('PAY-PERF-', o.id),
  CASE FLOOR(RAND() * 3) WHEN 0 THEN 'ALIPAY' WHEN 1 THEN 'WECHAT' ELSE 'BANK_CARD' END,
  o.amount_cent,
  'SUCCESS',
  DATE_ADD(o.created_at, INTERVAL FLOOR(RAND() * 60) MINUTE),
  o.created_at
FROM t_order o
WHERE o.status = 'PAID';

SELECT CONCAT('支付记录数: ', COUNT(*)) AS info FROM t_payment_record;

-- ============================================================
-- 6. 批量生成体检记录 (对应已完成的预约)
-- ============================================================
INSERT IGNORE INTO t_exam_record (user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT
  a.user_id,
  a.profile_id,
  a.package_id,
  (SELECT p.package_name FROM t_exam_package p WHERE p.id = a.package_id),
  a.branch_id,
  a.id,
  CONCAT('EXM-PERF-', a.id),
  CASE a.status WHEN 'COMPLETED' THEN 'COMPLETED' WHEN 'CHECKED_IN' THEN 'IN_PROGRESS' ELSE 'COMPLETED' END,
  DATE_ADD(a.appointment_date, INTERVAL 8 HOUR),
  a.created_at,
  a.updated_at
FROM t_appointment a
WHERE a.status IN ('COMPLETED', 'CHECKED_IN');

SELECT CONCAT('体检记录数: ', COUNT(*)) AS info FROM t_exam_record;

-- ============================================================
-- 7. 批量生成体检进度
-- ============================================================
INSERT IGNORE INTO t_exam_progress (exam_record_id, item_id, department_id, progress_status, created_at, updated_at)
SELECT
  er.id,
  ei.id,
  ei.department_id,
  CASE WHEN er.status = 'COMPLETED' THEN 'COMPLETED' WHEN FLOOR(RAND() * 3) > 0 THEN 'COMPLETED' ELSE 'PENDING' END,
  er.created_at,
  er.updated_at
FROM t_exam_record er
JOIN t_exam_package_item epi ON epi.package_id = er.package_id
JOIN t_exam_item ei ON ei.id = epi.item_id;

SELECT CONCAT('体检进度数: ', COUNT(*)) AS info FROM t_exam_progress;

-- ============================================================
-- 8. 批量生成检查结果
-- ============================================================
INSERT IGNORE INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT
  CONCAT('RES-PERF-', er.id, '-', epi.item_id),
  er.id, epi.item_id, ei.item_name,
  3.0 + RAND() * 5.0,
  ei.unit, ei.reference_min, ei.reference_max,
  CASE WHEN RAND() < 0.1 THEN 'ABNORMAL' ELSE 'NORMAL' END,
  'APPROVED',
  3, er.created_at, er.updated_at
FROM t_exam_record er
JOIN t_exam_package_item epi ON epi.package_id = er.package_id
JOIN t_exam_item ei ON ei.id = epi.item_id;

SELECT CONCAT('检查结果数: ', COUNT(*)) AS info FROM t_check_result;

-- ============================================================
-- 9. 批量生成体检报告
-- ============================================================
INSERT IGNORE INTO t_exam_report (report_no, exam_record_id, user_id, template_id, abnormal_status, summary, doctor_advice, report_status, reviewer_id, generated_at, published_at, created_at, updated_at)
SELECT
  CONCAT('RPT-PERF-', er.id),
  er.id, er.user_id,
  1 + FLOOR(RAND() * 4),
  CASE WHEN EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.abnormal_level = 'ABNORMAL') THEN 'ABNORMAL' ELSE 'NORMAL' END,
  '本次体检整体评估结果正常，各项指标在正常范围内。',
  '建议保持良好生活习惯，定期体检。',
  'PUBLISHED', 4,
  DATE_ADD(er.checked_in_at, INTERVAL 2 DAY),
  DATE_ADD(er.checked_in_at, INTERVAL 3 DAY),
  er.created_at, er.updated_at
FROM t_exam_record er
WHERE er.status = 'COMPLETED';

SELECT CONCAT('体检报告数: ', COUNT(*)) AS info FROM t_exam_report;

-- ============================================================
-- 10. 批量生成科室队列
-- ============================================================
INSERT IGNORE INTO t_department_queue (exam_record_id, department_id, queue_no, queue_status, created_at, updated_at)
SELECT
  er.id,
  ei.department_id,
  CONCAT('Q', LPAD(FLOOR(RAND() * 100), 3, '0')),
  CASE WHEN FLOOR(RAND() * 3) = 0 THEN 'WAITING' WHEN FLOOR(RAND() * 2) = 0 THEN 'SERVING' ELSE 'COMPLETED' END,
  er.created_at, er.updated_at
FROM t_exam_record er
JOIN t_exam_progress ep ON ep.exam_record_id = er.id
JOIN t_exam_item ei ON ei.id = ep.item_id
GROUP BY er.id, ei.department_id;

SELECT CONCAT('科室队列数: ', COUNT(*)) AS info FROM t_department_queue;

-- ============================================================
-- 11. 批量生成操作日志
-- ============================================================
INSERT IGNORE INTO t_operation_log (operator_id, operation_type, target_type, target_id, result, detail, ip_address, operation_time, entry_hash)
SELECT
  CONCAT(1 + FLOOR(RAND() * 10)),
  CASE FLOOR(RAND() * 6) WHEN 0 THEN 'LOGIN' WHEN 1 THEN 'REPORT_VIEW' WHEN 2 THEN 'APPOINTMENT_CREATE' WHEN 3 THEN 'RESULT_RECORD' WHEN 4 THEN 'ORDER_PAY' ELSE 'PROFILE_UPDATE' END,
  CASE FLOOR(RAND() * 4) WHEN 0 THEN 'appointment' WHEN 1 THEN 'exam_report' WHEN 2 THEN 'check_result' ELSE 'sys_user' END,
  CONCAT(1 + FLOOR(RAND() * 1000)),
  'SUCCESS',
  CASE FLOOR(RAND() * 4) WHEN 0 THEN '用户登录系统' WHEN 1 THEN '查看体检报告' WHEN 2 THEN '创建预约' ELSE '更新个人信息' END,
  CONCAT('192.168.', FLOOR(RAND() * 255), '.', FLOOR(RAND() * 255)),
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY),
  SHA2(CONCAT('perf_', RAND()), 256)
FROM sys_user u
LIMIT 3000;

SELECT CONCAT('操作日志数: ', COUNT(*)) AS info FROM t_operation_log;

-- ============================================================
-- 12. 批量生成通知
-- ============================================================
INSERT IGNORE INTO t_notification (user_id, notification_type, title, content, read_flag, created_at)
SELECT
  u.id,
  CASE FLOOR(RAND() * 4) WHEN 0 THEN 'APPOINTMENT' WHEN 1 THEN 'REPORT' WHEN 2 THEN 'SYSTEM' ELSE 'PAYMENT' END,
  CASE FLOOR(RAND() * 3) WHEN 0 THEN '预约提醒' WHEN 1 THEN '报告已生成' ELSE '系统通知' END,
  CASE FLOOR(RAND() * 3) WHEN 0 THEN '您有新的体检预约' WHEN 1 THEN '您的体检报告已生成，可在线查看' ELSE '欢迎使用熙心健康' END,
  FLOOR(RAND() * 2),
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY)
FROM sys_user u
WHERE u.phone LIKE '139%'
LIMIT 3000;

SELECT CONCAT('通知数: ', COUNT(*)) AS info FROM t_notification;

-- ============================================================
-- 汇总统计
-- ============================================================
SELECT '=== 性能测试数据生成完成 ===' AS result;
SELECT TABLE_NAME, ROUND(TABLE_ROWS) AS ROWS_COUNT FROM information_schema.TABLES WHERE TABLE_SCHEMA='xixin_health' ORDER BY TABLE_ROWS DESC;

SET FOREIGN_KEY_CHECKS = 1;
