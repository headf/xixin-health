-- 熙心健康 - 历史体检记录趋势演示数据
-- 为现有用户补充 2-3 次历史体检记录，用于趋势对比展示

SET NAMES utf8mb4;
USE xixin_health;
SET FOREIGN_KEY_CHECKS = 0;

-- 为 5 位主要演示用户添加历史记录
-- 张三(5), 李四(6), 王五(7), 赵六(8), 孙七(9)

-- 设置历史日期范围（每月一次，持续3个月）
SET @BASE_DATE = '2026-03-15';
SET @BASE_DATE2 = '2026-04-20';
SET @BASE_DATE3 = '2026-05-25';

-- 插入历史体检记录 (针对每个主要用户)
INSERT INTO t_exam_record (user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT u.id, hp.id, u.id % 4 + 1,
  CASE u.id % 4 WHEN 0 THEN '基础体检套餐' WHEN 1 THEN '全面型体检套餐' WHEN 2 THEN '精简型体检套餐' ELSE '深度型体检套餐' END,
  u.id % 4 + 1, 0,
  CONCAT('EXM-HIS-', u.id, '-1'), 'COMPLETED', @BASE_DATE + INTERVAL 8 HOUR, @BASE_DATE, @BASE_DATE
FROM sys_user u
JOIN t_health_profile hp ON hp.owner_user_id = u.id AND hp.profile_type = 'SELF'
WHERE u.phone IN ('13800000001','13800000002','13800000003','13800000004','13800000005')
AND NOT EXISTS (SELECT 1 FROM t_exam_record er WHERE er.exam_no = CONCAT('EXM-HIS-', u.id, '-1'));

INSERT INTO t_exam_record (user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT u.id, hp.id, u.id % 4 + 1,
  CASE u.id % 4 WHEN 0 THEN '基础体检套餐' WHEN 1 THEN '全面型体检套餐' WHEN 2 THEN '精简型体检套餐' ELSE '深度型体检套餐' END,
  u.id % 4 + 1, 0,
  CONCAT('EXM-HIS-', u.id, '-2'), 'COMPLETED', @BASE_DATE2 + INTERVAL 8 HOUR, @BASE_DATE2, @BASE_DATE2
FROM sys_user u
JOIN t_health_profile hp ON hp.owner_user_id = u.id AND hp.profile_type = 'SELF'
WHERE u.phone IN ('13800000001','13800000002','13800000003','13800000004','13800000005')
AND NOT EXISTS (SELECT 1 FROM t_exam_record er WHERE er.exam_no = CONCAT('EXM-HIS-', u.id, '-2'));

INSERT INTO t_exam_record (user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT u.id, hp.id, u.id % 4 + 1,
  CASE u.id % 4 WHEN 0 THEN '基础体检套餐' WHEN 1 THEN '全面型体检套餐' WHEN 2 THEN '精简型体检套餐' ELSE '深度型体检套餐' END,
  u.id % 4 + 1, 0,
  CONCAT('EXM-HIS-', u.id, '-3'), 'COMPLETED', @BASE_DATE3 + INTERVAL 8 HOUR, @BASE_DATE3, @BASE_DATE3
FROM sys_user u
JOIN t_health_profile hp ON hp.owner_user_id = u.id AND hp.profile_type = 'SELF'
WHERE u.phone IN ('13800000001','13800000002','13800000003','13800000004','13800000005')
AND NOT EXISTS (SELECT 1 FROM t_exam_record er WHERE er.exam_no = CONCAT('EXM-HIS-', u.id, '-3'));

-- 插入历史检查结果-血糖(GLUCOSE, item_id=1)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES-HIS-GLU-', er.id), er.id, 1, '血糖',
  CASE er.user_id
    WHEN 5 THEN 5.2 + (RAND() * 1.5)   -- 张三：5.2~6.7 (有异常趋势)
    WHEN 6 THEN 4.8 + (RAND() * 0.8)   -- 李四：4.8~5.6
    WHEN 7 THEN 5.5 + (RAND() * 2.0)   -- 王五：5.5~7.5 (血糖偏高)
    WHEN 8 THEN 4.5 + (RAND() * 1.0)   -- 赵六：4.5~5.5
    WHEN 9 THEN 5.0 + (RAND() * 1.2)   -- 孙七：5.0~6.2
  END,
  'mmol/L', 3.90, 6.10,
  CASE WHEN er.user_id = 7 AND er.id = (SELECT MAX(e2.id) FROM t_exam_record e2 WHERE e2.user_id = er.user_id) THEN 'ABNORMAL' ELSE 'NORMAL' END,
  'APPROVED', 3, er.created_at, er.created_at
FROM t_exam_record er
WHERE er.exam_no LIKE 'EXM-HIS-%'
AND NOT EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.item_id = 1);

-- 总胆固醇 (item_id=8)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES-HIS-TC-', er.id), er.id, 8, '总胆固醇',
  CASE er.user_id
    WHEN 5 THEN 4.0 + (RAND() * 1.2)
    WHEN 6 THEN 3.8 + (RAND() * 1.5)
    WHEN 7 THEN 4.5 + (RAND() * 1.8)  -- 逐渐升高
    WHEN 8 THEN 3.5 + (RAND() * 1.0)
    WHEN 9 THEN 4.2 + (RAND() * 1.3)
  END,
  'mmol/L', 0.00, 5.20,
  CASE WHEN er.user_id = 7 AND er.id = (SELECT MAX(e2.id) FROM t_exam_record e2 WHERE e2.user_id = er.user_id) THEN 'ABNORMAL' ELSE 'NORMAL' END,
  'APPROVED', 3, er.created_at, er.created_at
FROM t_exam_record er
WHERE er.exam_no LIKE 'EXM-HIS-%'
AND NOT EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.item_id = 8);

-- 甘油三酯 (item_id=9)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES-HIS-TG-', er.id), er.id, 9, '甘油三酯',
  CASE er.user_id
    WHEN 5 THEN 1.0 + (RAND() * 0.8)
    WHEN 6 THEN 0.8 + (RAND() * 1.0)
    WHEN 7 THEN 1.2 + (RAND() * 1.5)  -- 显著升高
    WHEN 8 THEN 0.9 + (RAND() * 0.6)
    WHEN 9 THEN 1.1 + (RAND() * 0.9)
  END,
  'mmol/L', 0.00, 1.70,
  CASE WHEN er.user_id = 7 AND er.created_at > @BASE_DATE2 THEN 'ABNORMAL' ELSE 'NORMAL' END,
  'APPROVED', 3, er.created_at, er.created_at
FROM t_exam_record er
WHERE er.exam_no LIKE 'EXM-HIS-%'
AND NOT EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.item_id = 9);

-- BMI (item_id=33)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES-HIS-BMI-', er.id), er.id, 33, 'BMI指数',
  CASE er.user_id
    WHEN 5 THEN 22.0 + (RAND() * 2.0)
    WHEN 6 THEN 23.5 + (RAND() * 3.0)  -- 略有超重趋势
    WHEN 7 THEN 24.0 + (RAND() * 3.5)  -- 超重趋势
    WHEN 8 THEN 21.0 + (RAND() * 1.5)
    WHEN 9 THEN 22.5 + (RAND() * 2.5)
  END,
  'kg/m²', 18.50, 24.90,
  CASE WHEN er.user_id = 7 AND er.id = (SELECT MAX(e2.id) FROM t_exam_record e2 WHERE e2.user_id = er.user_id) THEN 'ABNORMAL' ELSE 'NORMAL' END,
  'APPROVED', 3, er.created_at, er.created_at
FROM t_exam_record er
WHERE er.exam_no LIKE 'EXM-HIS-%'
AND NOT EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.item_id = 33);

-- 血压 (item_id=32)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES-HIS-BP-', er.id), er.id, 32, '血压',
  CASE er.user_id
    WHEN 5 THEN 115 + (RAND() * 15)
    WHEN 6 THEN 120 + (RAND() * 20)   -- 血压偏高
    WHEN 7 THEN 125 + (RAND() * 25)   -- 血压升高趋势
    WHEN 8 THEN 110 + (RAND() * 10)
    WHEN 9 THEN 118 + (RAND() * 15)
  END,
  'mmHg', 90.00, 140.00,
  CASE WHEN er.user_id = 6 AND er.created_at > @BASE_DATE2 THEN 'ABNORMAL' ELSE 'NORMAL' END,
  'APPROVED', 3, er.created_at, er.created_at
FROM t_exam_record er
WHERE er.exam_no LIKE 'EXM-HIS-%'
AND NOT EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.item_id = 32);

-- 为历史记录添加对应报告
INSERT INTO t_exam_report (report_no, exam_record_id, user_id, template_id, abnormal_status, summary, doctor_advice, report_status, reviewer_id, generated_at, published_at, created_at, updated_at)
SELECT CONCAT('RPT-HIS-', er.id), er.id, er.user_id, (er.package_id % 4) + 1,
  CASE WHEN EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.abnormal_level = 'ABNORMAL') THEN 'ABNORMAL' ELSE 'NORMAL' END,
  CASE WHEN EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.abnormal_level = 'ABNORMAL') THEN '本次体检发现部分指标异常，建议定期复查' ELSE '整体健康状况良好' END,
  CASE WHEN EXISTS (SELECT 1 FROM t_check_result cr WHERE cr.exam_record_id = er.id AND cr.abnormal_level = 'ABNORMAL') THEN '建议定期监测，保持健康生活方式' ELSE '继续保持当前健康状态' END,
  'PUBLISHED', 4, er.created_at, er.created_at, er.created_at, er.created_at
FROM t_exam_record er
WHERE er.exam_no LIKE 'EXM-HIS-%'
AND NOT EXISTS (SELECT 1 FROM t_exam_report r WHERE r.exam_record_id = er.id);

SET FOREIGN_KEY_CHECKS = 1;

SELECT CONCAT('✓ 已为 ', (SELECT COUNT(DISTINCT user_id) FROM t_exam_record WHERE exam_no LIKE 'EXM-HIS-%'), ' 位用户添加历史体检记录') AS info;
SELECT CONCAT('  - 共添加 ', (SELECT COUNT(*) FROM t_exam_record WHERE exam_no LIKE 'EXM-HIS-%'), ' 条历史体检记录') AS info;
SELECT CONCAT('  - 共添加 ', (SELECT COUNT(*) FROM t_check_result cr JOIN t_exam_record er ON cr.exam_record_id = er.id WHERE er.exam_no LIKE 'EXM-HIS-%'), ' 条历史检查结果') AS info;
