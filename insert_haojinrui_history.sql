-- =====================================================
-- 为郝锦瑞（user_id=188）添加3次历史体检数据
-- 就诊人资料: profile_id=1313 (郝锦瑞本人)
-- =====================================================

SET @uid = 188;
SET @profileId = 1313;
SET @branchId = 1;

-- =====================================================
-- 第一次：2026-01-15 入职体检（朝阳总院）
-- 套餐：入职体检套餐 (package_id=8, 19900分)
-- =====================================================

-- 预约
INSERT INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted)
VALUES ('APT1780500000001', @uid, @profileId, 8, 1, '2026-01-15', 'MORNING', 'CHECKED_IN', '2026-01-14 10:00:00', '2026-01-15 08:00:00', FALSE);
SET @appt1 = LAST_INSERT_ID();

-- 订单
INSERT INTO t_order (order_no, user_id, appointment_id, amount_cent, status, created_at, updated_at)
VALUES ('ORD1780500000001', @uid, @appt1, 19900, 'PAID', '2026-01-14 10:00:00', '2026-01-14 10:05:00');

-- 体检记录
INSERT INTO t_exam_record (exam_no, appointment_id, user_id, profile_id, branch_id, status, checked_in_at, package_id, package_name, created_at, updated_at)
VALUES ('EX20260115001', @appt1, @uid, @profileId, 1, 'COMPLETED', '2026-01-15 08:30:00', 8, '入职体检套餐', '2026-01-15 08:30:00', '2026-01-15 10:00:00');
SET @exam1 = LAST_INSERT_ID();

-- 检查结果 (入职体检 - 基础项目)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, text_value, abnormal_level, reference_min, reference_max, unit, recorder_id, review_status, created_at, updated_at)
VALUES
('RSLT20260115001', @exam1, 11, '白细胞计数', 6.80, NULL, 'NORMAL', 4.00, 10.00, '×10⁹/L', 1, 'APPROVED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
('RSLT20260115002', @exam1, 12, '血红蛋白', 148.00, NULL, 'NORMAL', 110.00, 160.00, 'g/L', 1, 'APPROVED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
('RSLT20260115003', @exam1, 13, '血小板计数', 210.00, NULL, 'NORMAL', 100.00, 300.00, '×10⁹/L', 1, 'APPROVED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
('RSLT20260115004', @exam1, 1, '空腹血糖', 5.10, NULL, 'NORMAL', 3.90, 6.10, 'mmol/L', 1, 'APPROVED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
('RSLT20260115005', @exam1, 2, '谷丙转氨酶', 22.00, NULL, 'NORMAL', 0.00, 40.00, 'U/L', 1, 'APPROVED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
('RSLT20260115006', @exam1, 6, '总胆固醇', 4.20, NULL, 'NORMAL', 2.80, 5.20, 'mmol/L', 1, 'APPROVED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
('RSLT20260115007', @exam1, 7, '甘油三酯', 1.30, NULL, 'NORMAL', 0.56, 1.70, 'mmol/L', 1, 'APPROVED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
('RSLT20260115008', @exam1, 14, '胸部X光检查', NULL, '双肺纹理清晰，未见异常密度影', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-01-15 09:30:00', '2026-01-15 09:30:00'),
('RSLT20260115009', @exam1, 15, '心肺听诊', NULL, '心率齐，未闻及病理性杂音', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-01-15 09:30:00', '2026-01-15 09:30:00'),
('RSLT20260115010', @exam1, 18, '皮肤检查', NULL, '皮肤未见异常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-01-15 09:30:00', '2026-01-15 09:30:00');

-- 体检进度
INSERT INTO t_exam_progress (exam_record_id, department_id, item_id, progress_status, created_at, updated_at)
VALUES
(@exam1, 1, 11, 'COMPLETED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
(@exam1, 1, 12, 'COMPLETED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
(@exam1, 1, 13, 'COMPLETED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
(@exam1, 1,  1, 'COMPLETED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
(@exam1, 1,  2, 'COMPLETED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
(@exam1, 1,  6, 'COMPLETED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
(@exam1, 1,  7, 'COMPLETED', '2026-01-15 09:00:00', '2026-01-15 09:00:00'),
(@exam1, 2, 14, 'COMPLETED', '2026-01-15 09:30:00', '2026-01-15 09:30:00'),
(@exam1, 3, 15, 'COMPLETED', '2026-01-15 09:30:00', '2026-01-15 09:30:00'),
(@exam1, 4, 18, 'COMPLETED', '2026-01-15 09:30:00', '2026-01-15 09:30:00');

-- 报告
INSERT INTO t_exam_report (report_no, exam_record_id, user_id, reviewer_id, abnormal_status, report_status, summary, doctor_advice, generated_at, published_at, signed_at, created_at, updated_at)
VALUES ('RPT20260115001', @exam1, @uid, 2, 'NORMAL', 'PUBLISHED', '体检各项指标均在正常范围内，身体状况良好。', '建议保持规律作息，均衡饮食，适当运动，定期体检。', '2026-01-15 12:00:00', '2026-01-15 14:00:00', '2026-01-15 14:00:00', '2026-01-15 12:00:00', '2026-01-15 14:00:00');


-- =====================================================
-- 第二次：2026-04-10 基础健康体检（海淀分院）
-- 套餐：基础健康体检 (package_id=1, 29900分)
-- =====================================================

INSERT INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted)
VALUES ('APT1781000000001', @uid, @profileId, 1, 2, '2026-04-10', 'MORNING', 'CHECKED_IN', '2026-04-08 09:00:00', '2026-04-10 08:00:00', FALSE);
SET @appt2 = LAST_INSERT_ID();

INSERT INTO t_order (order_no, user_id, appointment_id, amount_cent, status, created_at, updated_at)
VALUES ('ORD1781000000001', @uid, @appt2, 29900, 'PAID', '2026-04-08 09:00:00', '2026-04-08 09:05:00');

INSERT INTO t_exam_record (exam_no, appointment_id, user_id, profile_id, branch_id, status, checked_in_at, package_id, package_name, created_at, updated_at)
VALUES ('EX20260410001', @appt2, @uid, @profileId, 2, 'COMPLETED', '2026-04-10 08:00:00', 1, '基础健康体检', '2026-04-10 08:00:00', '2026-04-10 11:00:00');
SET @exam2 = LAST_INSERT_ID();

-- 检查结果 (基础健康体检 - 较全面)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, text_value, abnormal_level, reference_min, reference_max, unit, recorder_id, review_status, created_at, updated_at)
VALUES
('RSLT20260410001', @exam2, 1, '空腹血糖', 5.30, NULL, 'NORMAL', 3.90, 6.10, 'mmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410002', @exam2, 2, '谷丙转氨酶', 28.00, NULL, 'NORMAL', 0.00, 40.00, 'U/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410003', @exam2, 3, '谷草转氨酶', 24.00, NULL, 'NORMAL', 0.00, 40.00, 'U/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410004', @exam2, 4, '肌酐', 76.00, NULL, 'NORMAL', 44.00, 133.00, 'μmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410005', @exam2, 5, '尿素氮', 4.80, NULL, 'NORMAL', 2.90, 8.20, 'mmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410006', @exam2, 6, '总胆固醇', 4.60, NULL, 'NORMAL', 2.80, 5.20, 'mmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410007', @exam2, 7, '甘油三酯', 1.60, NULL, 'NORMAL', 0.56, 1.70, 'mmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410008', @exam2, 8, '高密度脂蛋白', 1.25, NULL, 'NORMAL', 1.00, 1.55, 'mmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410009', @exam2, 9, '低密度脂蛋白', 2.80, NULL, 'NORMAL', 0.00, 3.40, 'mmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410010', @exam2, 10, '尿酸', 380.00, NULL, 'NORMAL', 150.00, 420.00, 'μmol/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410011', @exam2, 11, '白细胞计数', 5.90, NULL, 'NORMAL', 4.00, 10.00, '×10⁹/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410012', @exam2, 12, '血红蛋白', 142.00, NULL, 'NORMAL', 110.00, 160.00, 'g/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410013', @exam2, 13, '血小板计数', 195.00, NULL, 'NORMAL', 100.00, 300.00, '×10⁹/L', 1, 'APPROVED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
('RSLT20260410014', @exam2, 14, '胸部X光检查', NULL, '双肺纹理清晰，心影大小正常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-04-10 09:00:00', '2026-04-10 09:00:00'),
('RSLT20260410015', @exam2, 34, '心电图检查', NULL, '窦性心律，心电图正常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-04-10 09:00:00', '2026-04-10 09:00:00'),
('RSLT20260410016', @exam2, 15, '心肺听诊', NULL, '心率齐，未闻及杂音', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-04-10 09:30:00', '2026-04-10 09:30:00'),
('RSLT20260410017', @exam2, 18, '皮肤检查', NULL, '皮肤未见明显异常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-04-10 09:30:00', '2026-04-10 09:30:00'),
('RSLT20260410018', @exam2, 30, '牙齿检查', NULL, '牙齿排列整齐，未见龋齿', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-04-10 09:30:00', '2026-04-10 09:30:00');

-- 体检进度
INSERT INTO t_exam_progress (exam_record_id, department_id, item_id, progress_status, created_at, updated_at)
VALUES
(@exam2, 1,  1, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  2, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  3, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  4, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  5, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  6, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  7, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  8, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1,  9, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1, 10, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1, 11, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1, 12, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 1, 13, 'COMPLETED', '2026-04-10 08:30:00', '2026-04-10 08:30:00'),
(@exam2, 2, 14, 'COMPLETED', '2026-04-10 09:00:00', '2026-04-10 09:00:00'),
(@exam2, 8, 34, 'COMPLETED', '2026-04-10 09:00:00', '2026-04-10 09:00:00'),
(@exam2, 3, 15, 'COMPLETED', '2026-04-10 09:30:00', '2026-04-10 09:30:00'),
(@exam2, 4, 18, 'COMPLETED', '2026-04-10 09:30:00', '2026-04-10 09:30:00'),
(@exam2, 7, 30, 'COMPLETED', '2026-04-10 09:30:00', '2026-04-10 09:30:00');

-- 报告
INSERT INTO t_exam_report (report_no, exam_record_id, user_id, reviewer_id, abnormal_status, report_status, summary, doctor_advice, overall_health_assessment, lifestyle_recommendations, generated_at, published_at, signed_at, created_at, updated_at)
VALUES ('RPT20260410001', @exam2, @uid, 2, 'NORMAL', 'PUBLISHED',
  '各项检查结果均在正常范围内，体检结论为健康。',
  '建议继续保持良好的生活习惯，定期进行健康体检。',
  '您本次体检整体健康状况良好，各项指标均正常。建议继续保持规律作息、均衡饮食。',
  '1. 饮食方面：建议多吃蔬菜水果，减少高脂高糖食物摄入。\n2. 运动方面：建议每周进行3-5次中等强度运动，如快走、慢跑等。\n3. 作息方面：保持规律作息，避免熬夜。',
  '2026-04-10 14:00:00', '2026-04-10 16:00:00', '2026-04-10 16:00:00', '2026-04-10 14:00:00', '2026-04-10 16:00:00');


-- =====================================================
-- 第三次：2026-06-05 标准健康体检（朝阳总院）
-- 套餐：标准健康体检 (package_id=2, 59900分)
-- 有一些异常指标：甘油三酯偏高、总胆固醇偏高
-- =====================================================

INSERT INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted)
VALUES ('APT1782000000001', @uid, @profileId, 2, 1, '2026-06-05', 'MORNING', 'CHECKED_IN', '2026-06-03 10:00:00', '2026-06-05 08:00:00', FALSE);
SET @appt3 = LAST_INSERT_ID();

INSERT INTO t_order (order_no, user_id, appointment_id, amount_cent, status, created_at, updated_at)
VALUES ('ORD1782000000001', @uid, @appt3, 59900, 'PAID', '2026-06-03 10:00:00', '2026-06-03 10:05:00');

INSERT INTO t_exam_record (exam_no, appointment_id, user_id, profile_id, branch_id, status, checked_in_at, package_id, package_name, created_at, updated_at)
VALUES ('EX20260605001', @appt3, @uid, @profileId, 1, 'COMPLETED', '2026-06-05 07:45:00', 2, '标准健康体检', '2026-06-05 07:45:00', '2026-06-05 11:30:00');
SET @exam3 = LAST_INSERT_ID();

-- 检查结果 (标准健康体检 - 最全面)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, text_value, abnormal_level, reference_min, reference_max, unit, recorder_id, review_status, created_at, updated_at)
VALUES
('RSLT20260605001', @exam3, 1, '空腹血糖', 5.40, NULL, 'NORMAL', 3.90, 6.10, 'mmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605002', @exam3, 2, '谷丙转氨酶', 32.00, NULL, 'NORMAL', 0.00, 40.00, 'U/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605003', @exam3, 3, '谷草转氨酶', 26.00, NULL, 'NORMAL', 0.00, 40.00, 'U/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605004', @exam3, 4, '肌酐', 80.00, NULL, 'NORMAL', 44.00, 133.00, 'μmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605005', @exam3, 5, '尿素氮', 5.10, NULL, 'NORMAL', 2.90, 8.20, 'mmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605006', @exam3, 6, '总胆固醇', 5.80, NULL, 'ABNORMAL', 2.80, 5.20, 'mmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605007', @exam3, 7, '甘油三酯', 2.30, NULL, 'ABNORMAL', 0.56, 1.70, 'mmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605008', @exam3, 8, '高密度脂蛋白', 1.10, NULL, 'NORMAL', 1.00, 1.55, 'mmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605009', @exam3, 9, '低密度脂蛋白', 3.10, NULL, 'NORMAL', 0.00, 3.40, 'mmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605010', @exam3, 10, '尿酸', 410.00, NULL, 'NORMAL', 150.00, 420.00, 'μmol/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605011', @exam3, 11, '白细胞计数', 6.20, NULL, 'NORMAL', 4.00, 10.00, '×10⁹/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605012', @exam3, 12, '血红蛋白', 144.00, NULL, 'NORMAL', 110.00, 160.00, 'g/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605013', @exam3, 13, '血小板计数', 205.00, NULL, 'NORMAL', 100.00, 300.00, '×10⁹/L', 1, 'APPROVED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
('RSLT20260605014', @exam3, 14, '胸部X光检查', NULL, '双肺纹理增粗，建议随访', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:00:00', '2026-06-05 09:00:00'),
('RSLT20260605015', @exam3, 34, '心电图检查', NULL, '窦性心律，心电图正常范围', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:00:00', '2026-06-05 09:00:00'),
('RSLT20260605016', @exam3, 35, '腹部彩超', NULL, '肝胆胰脾未见明显异常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:15:00', '2026-06-05 09:15:00'),
('RSLT20260605017', @exam3, 15, '心肺听诊', NULL, '心率齐，呼吸音清晰', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:30:00', '2026-06-05 09:30:00'),
('RSLT20260605018', @exam3, 16, '腹部触诊', NULL, '腹软，无压痛', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:30:00', '2026-06-05 09:30:00'),
('RSLT20260605019', @exam3, 17, '内科结论', NULL, '内科检查未见明显异常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:30:00', '2026-06-05 09:30:00'),
('RSLT20260605020', @exam3, 18, '皮肤检查', NULL, '皮肤未见明显异常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:45:00', '2026-06-05 09:45:00'),
('RSLT20260605021', @exam3, 19, '淋巴结检查', NULL, '全身未及肿大淋巴结', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 09:45:00', '2026-06-05 09:45:00'),
('RSLT20260605022', @exam3, 23, '视力检查', NULL, '双眼视力5.0', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 10:00:00', '2026-06-05 10:00:00'),
('RSLT20260605023', @exam3, 25, '眼科结论', NULL, '眼科检查未见明显异常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-06-05 10:00:00', '2026-06-05 10:00:00');

-- 体检进度
INSERT INTO t_exam_progress (exam_record_id, department_id, item_id, progress_status, created_at, updated_at)
VALUES
(@exam3, 1,  1, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  2, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  3, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  4, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  5, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  6, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  7, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  8, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1,  9, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1, 10, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1, 11, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1, 12, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 1, 13, 'COMPLETED', '2026-06-05 08:15:00', '2026-06-05 08:15:00'),
(@exam3, 2, 14, 'COMPLETED', '2026-06-05 09:00:00', '2026-06-05 09:00:00'),
(@exam3, 8, 34, 'COMPLETED', '2026-06-05 09:00:00', '2026-06-05 09:00:00'),
(@exam3, 9, 35, 'COMPLETED', '2026-06-05 09:15:00', '2026-06-05 09:15:00'),
(@exam3, 3, 15, 'COMPLETED', '2026-06-05 09:30:00', '2026-06-05 09:30:00'),
(@exam3, 3, 16, 'COMPLETED', '2026-06-05 09:30:00', '2026-06-05 09:30:00'),
(@exam3, 3, 17, 'COMPLETED', '2026-06-05 09:30:00', '2026-06-05 09:30:00'),
(@exam3, 4, 18, 'COMPLETED', '2026-06-05 09:45:00', '2026-06-05 09:45:00'),
(@exam3, 4, 19, 'COMPLETED', '2026-06-05 09:45:00', '2026-06-05 09:45:00'),
(@exam3, 5, 23, 'COMPLETED', '2026-06-05 10:00:00', '2026-06-05 10:00:00'),
(@exam3, 5, 25, 'COMPLETED', '2026-06-05 10:00:00', '2026-06-05 10:00:00');

-- 报告 (有异常)
INSERT INTO t_exam_report (report_no, exam_record_id, user_id, reviewer_id, abnormal_status, report_status, summary, doctor_advice, overall_health_assessment, lifestyle_recommendations, follow_up_recommendations, classification, generated_at, published_at, signed_at, created_at, updated_at)
VALUES ('RPT20260605001', @exam3, @uid, 2, 'ABNORMAL', 'PUBLISHED',
  '本次体检发现以下异常指标：总胆固醇偏高（5.80 mmol/L）、甘油三酯偏高（2.30 mmol/L）。其余检查项目未见明显异常。',
  '建议：1. 控制饮食，减少高脂高热量食物摄入，增加膳食纤维。2. 适当增加体育锻炼，建议每周运动3-5次。3. 建议3-6个月后复查血脂四项，监测指标变化。4. 胸部X光提示肺纹理增粗，建议结合临床，必要时复查。',
  '您本次体检总体状况尚可，但已出现血脂异常信号（总胆固醇、甘油三酯偏高），属于亚健康状态，需要及时干预调整。若不控制，可能增加心血管疾病风险。',
  '1. 饮食调整：减少油炸食品、动物内脏、甜食等高脂高糖食物，增加蔬菜、水果、全谷物摄入。\n2. 运动计划：每周至少150分钟中等强度有氧运动（快走、慢跑、游泳等）。\n3. 生活习惯：戒烟限酒，保证充足睡眠。\n4. 定期监测：建议每3-6个月复查血脂。',
  '建议3个月后复查血脂四项，关注甘油三酯和总胆固醇变化趋势。如持续偏高，建议到心内科门诊进一步诊治。',
  'SUB_HEALTH',
  '2026-06-05 15:00:00', '2026-06-05 17:00:00', '2026-06-05 17:00:00', '2026-06-05 15:00:00', '2026-06-05 17:00:00');

SELECT '插入完成！' AS 状态;
