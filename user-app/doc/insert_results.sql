-- 先查看 user_id=9 的体检记录ID（确认一下）
-- SELECT id, exam_no, user_id, status FROM t_exam_record WHERE user_id = 9 ORDER BY created_at DESC;

-- 插入详细检查指标（假设 exam_record_id 为 1，请根据实际ID修改）
-- 把下面的 :exam_record_id 替换为实际的ID

-- 1. 血常规 - 白细胞计数
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '01'), r.id, 101, '白细胞计数', '×10⁹/L', 6.2, 3.5, 9.5, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 2. 血常规 - 红细胞计数
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '02'), r.id, 102, '红细胞计数', '×10¹²/L', 4.8, 4.0, 5.5, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 3. 血常规 - 血红蛋白
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '03'), r.id, 103, '血红蛋白', 'g/L', 145, 120, 160, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 4. 肝功能 - 谷丙转氨酶
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '04'), r.id, 201, '谷丙转氨酶(ALT)', 'U/L', 28, 0, 40, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 5. 肝功能 - 谷草转氨酶
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '05'), r.id, 202, '谷草转氨酶(AST)', 'U/L', 32, 0, 40, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 6. 肾功能 - 肌酐
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '06'), r.id, 301, '肌酐(Cr)', 'μmol/L', 72, 44, 133, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 7. 肾功能 - 尿素氮
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '07'), r.id, 302, '尿素氮(BUN)', 'mmol/L', 5.2, 2.9, 8.2, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 8. 血脂 - 总胆固醇
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '08'), r.id, 401, '总胆固醇(TC)', 'mmol/L', 4.2, 3.1, 5.7, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 9. 血脂 - 甘油三酯
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '09'), r.id, 402, '甘油三酯(TG)', 'mmol/L', 1.8, 0.56, 1.7, 'ABNORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 10. 血糖 - 空腹血糖
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '10'), r.id, 501, '空腹血糖(GLU)', 'mmol/L', 5.1, 3.9, 6.1, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 11. 尿常规 - 尿蛋白
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '11'), r.id, 601, '尿蛋白(PRO)', 'g/L', 0.1, 0, 0.15, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;

-- 12. 尿常规 - 尿糖
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT CONCAT('RES', UNIX_TIMESTAMP(NOW()), '12'), r.id, 602, '尿糖(GLU)', 'mmol/L', 0, 0, 1.0, 'NORMAL', 'APPROVED', 1, NOW(), NOW()
FROM t_exam_record r WHERE r.user_id = 9 ORDER BY r.created_at DESC LIMIT 1;
