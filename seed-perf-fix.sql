-- ============================================================
-- 熙心健康 - 性能测试数据补充（修复版）
-- 使用存储过程绕过 RAND() 过滤问题
-- ============================================================
SET NAMES utf8mb4;
USE xixin_health;
SET FOREIGN_KEY_CHECKS = 0;

-- ====== 1. 批量生成预约 (5000条) ======
DROP PROCEDURE IF EXISTS seed_appts;
DELIMITER //
CREATE PROCEDURE seed_appts()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE uid INT;
  DECLARE pid INT;
  DECLARE cur CURSOR FOR SELECT u.id, hp.id FROM sys_user u JOIN t_health_profile hp ON hp.owner_user_id = u.id AND hp.profile_type = 'SELF' WHERE u.phone LIKE '139%' ORDER BY u.id;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO uid, pid;
    IF done THEN LEAVE read_loop; END IF;
    IF NOT EXISTS (SELECT 1 FROM t_appointment WHERE user_id = uid LIMIT 1) THEN
      INSERT INTO t_appointment (user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted)
      VALUES (uid, pid, 1 + uid % 4, 1 + uid % 4, DATE_ADD(CURDATE(), INTERVAL -(uid % 60) DAY), '08:00-09:00', 'COMPLETED', DATE_SUB(NOW(), INTERVAL uid % 90 DAY), NOW(), 0);
    END IF;
  END LOOP;
  CLOSE cur;
END//
DELIMITER ;
CALL seed_appts();
DROP PROCEDURE IF EXISTS seed_appts;

-- ====== 2. 为这些预约生成订单 ======
INSERT IGNORE INTO t_order (user_id, appointment_id, amount_cent, status, created_at, updated_at)
SELECT a.user_id, a.id, 19900 + (a.package_id * 10000), 'PAID', a.created_at, a.updated_at
FROM t_appointment a
WHERE a.status != 'CANCELLED'
AND NOT EXISTS (SELECT 1 FROM t_order o WHERE o.appointment_id = a.id);

-- ====== 3. 生成支付记录 ======
INSERT IGNORE INTO t_payment_record (order_id, payment_no, channel, amount_cent, status, paid_at, created_at)
SELECT o.id, CONCAT('PAY-', o.id), 'ALIPAY', o.amount_cent, 'SUCCESS',
  DATE_ADD(o.created_at, INTERVAL 30 MINUTE), o.created_at
FROM t_order o WHERE o.status = 'PAID'
AND NOT EXISTS (SELECT 1 FROM t_payment_record pr WHERE pr.order_id = o.id);

-- ====== 4. 生成体检记录 ======
INSERT IGNORE INTO t_exam_record (user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT a.user_id, a.profile_id, a.package_id,
  (SELECT p.package_name FROM t_exam_package p WHERE p.id = a.package_id),
  a.branch_id, a.id, CONCAT('EXM-PERF-', a.id),
  CASE WHEN a.status = 'CANCELLED' THEN 'CANCELLED' ELSE 'COMPLETED' END,
  DATE_ADD(a.appointment_date, INTERVAL 8 HOUR), a.created_at, a.updated_at
FROM t_appointment a
WHERE NOT EXISTS (SELECT 1 FROM t_exam_record er WHERE er.appointment_id = a.id);

-- ====== 5. 生成体检进度 ======
INSERT IGNORE INTO t_exam_progress (exam_record_id, item_id, department_id, progress_status, created_at, updated_at)
SELECT er.id, ei.id, ei.department_id, 'COMPLETED', er.created_at, er.updated_at
FROM t_exam_record er
JOIN t_exam_package_item epi ON epi.package_id = er.package_id
JOIN t_exam_item ei ON ei.id = epi.item_id
WHERE NOT EXISTS (SELECT 1 FROM t_exam_progress ep WHERE ep.exam_record_id = er.id AND ep.item_id = ei.id);

-- ====== 6. 生成检查结果 ======
INSERT IGNORE INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT
  CONCAT('RES-PERF-', er.id, '-', ei.id),
  er.id, ei.id, ei.item_name,
  3.0 + RAND() * 5.0,
  ei.unit, ei.reference_min, ei.reference_max,
  'NORMAL', 'APPROVED', 3, er.created_at, er.updated_at
FROM t_exam_record er
JOIN t_exam_package_item epi ON epi.package_id = er.package_id
JOIN t_exam_item ei ON ei.id = epi.item_id
WHERE NOT EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.item_id = ei.id)
LIMIT 50000;

-- ====== 7. 生成报告 ======
INSERT IGNORE INTO t_exam_report (report_no, exam_record_id, user_id, template_id, abnormal_status, summary, doctor_advice, report_status, reviewer_id, generated_at, published_at, created_at, updated_at)
SELECT
  CONCAT('RPT-PERF-', er.id),
  er.id, er.user_id, 1 + er.package_id % 4, 'NORMAL',
  '各项指标均在正常范围内。', '建议保持良好生活习惯。',
  'PUBLISHED', 4,
  DATE_ADD(er.checked_in_at, INTERVAL 2 DAY), DATE_ADD(er.checked_in_at, INTERVAL 3 DAY),
  er.created_at, er.updated_at
FROM t_exam_record er
WHERE er.status = 'COMPLETED'
AND NOT EXISTS (SELECT 1 FROM t_exam_report r WHERE r.exam_record_id = er.id);

-- ====== 汇总 ======
SELECT '=== 数据补充完成 ===' AS result;
SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_SCHEMA='xixin_health' ORDER BY TABLE_ROWS DESC;

SET FOREIGN_KEY_CHECKS = 1;
