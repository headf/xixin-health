-- =====================================================
-- 为郝锦瑞(user_id=188)生成一份危急(CRITICAL)体检报告
-- 并创建健康管理记录，使随访干预页面可以显示
-- =====================================================

SET @uid = 188;
SET @profileId = 1313;
SET @branchId = 1;

-- =====================================================
-- 2026-07-02 危急体检（朝阳总院）
-- 套餐：基础健康体检 (package_id=1, 29900分)
-- 异常指标：空腹血糖 18.5 mmol/L (危急) + 谷丙转氨酶 280 U/L (危急)
-- =====================================================

-- 预约
INSERT INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted)
VALUES ('APT1783000000001', @uid, @profileId, 1, 1, '2026-07-02', 'MORNING', 'CHECKED_IN', '2026-07-02 08:00:00', '2026-07-02 08:30:00', FALSE);
SET @appt = LAST_INSERT_ID();

-- 订单
INSERT INTO t_order (order_no, user_id, appointment_id, amount_cent, status, created_at, updated_at)
VALUES ('ORD1783000000001', @uid, @appt, 29900, 'PAID', '2026-07-02 08:00:00', '2026-07-02 08:05:00');

-- 体检记录
INSERT INTO t_exam_record (exam_no, appointment_id, user_id, profile_id, branch_id, status, checked_in_at, package_id, package_name, created_at, updated_at)
VALUES ('EX20260702CRITICAL', @appt, @uid, @profileId, 1, 'COMPLETED', '2026-07-02 08:30:00', 1, '基础健康体检', '2026-07-02 08:30:00', '2026-07-02 11:00:00');
SET @exam = LAST_INSERT_ID();

-- 检查结果 (含危急值)
INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, text_value, abnormal_level, reference_min, reference_max, unit, recorder_id, review_status, created_at, updated_at)
VALUES
('RSLT20260702C001', @exam,  1, '空腹血糖', 18.50, NULL, 'CRITICAL', 3.90, 6.10, 'mmol/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C002', @exam,  2, '谷丙转氨酶', 280.00, NULL, 'CRITICAL', 0.00, 40.00, 'U/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C003', @exam,  3, '谷草转氨酶', 195.00, NULL, 'CRITICAL', 0.00, 40.00, 'U/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C004', @exam,  4, '肌酐', 88.00, NULL, 'NORMAL', 44.00, 133.00, 'μmol/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C005', @exam,  5, '尿素氮', 5.20, NULL, 'NORMAL', 2.90, 8.20, 'mmol/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C006', @exam,  6, '总胆固醇', 5.60, NULL, 'ABNORMAL', 2.80, 5.20, 'mmol/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C007', @exam,  7, '甘油三酯', 2.10, NULL, 'ABNORMAL', 0.56, 1.70, 'mmol/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C008', @exam,  8, '高密度脂蛋白', 0.92, NULL, 'ABNORMAL', 1.00, 1.55, 'mmol/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C009', @exam, 10, '尿酸', 388.00, NULL, 'NORMAL', 150.00, 420.00, 'μmol/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C010', @exam, 11, '白细胞计数', 12.80, NULL, 'ABNORMAL', 4.00, 10.00, '×10⁹/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C011', @exam, 12, '血红蛋白', 168.00, NULL, 'ABNORMAL', 110.00, 160.00, 'g/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C012', @exam, 13, '血小板计数', 180.00, NULL, 'NORMAL', 100.00, 300.00, '×10⁹/L', 1, 'APPROVED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('RSLT20260702C013', @exam, 14, '胸部X光检查', NULL, '双肺纹理增粗、模糊，建议进一步检查', 'ABNORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-07-02 09:30:00', '2026-07-02 09:30:00'),
('RSLT20260702C014', @exam, 34, '心电图检查', NULL, '窦性心动过速，ST-T改变', 'ABNORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-07-02 09:30:00', '2026-07-02 09:30:00'),
('RSLT20260702C015', @exam, 15, '心肺听诊', NULL, '心率112次/分，心律齐，未闻及明显杂音', 'ABNORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-07-02 09:45:00', '2026-07-02 09:45:00'),
('RSLT20260702C016', @exam, 18, '皮肤检查', NULL, '皮肤未见明显异常', 'NORMAL', NULL, NULL, NULL, 1, 'APPROVED', '2026-07-02 09:45:00', '2026-07-02 09:45:00');

-- 体检进度
INSERT INTO t_exam_progress (exam_record_id, department_id, item_id, progress_status, created_at, updated_at) VALUES
(@exam, 1,  1, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1,  2, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1,  3, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1,  4, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1,  5, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1,  6, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1,  7, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1,  8, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1, 10, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1, 11, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1, 12, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 1, 13, 'COMPLETED', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
(@exam, 2, 14, 'COMPLETED', '2026-07-02 09:30:00', '2026-07-02 09:30:00'),
(@exam, 8, 34, 'COMPLETED', '2026-07-02 09:30:00', '2026-07-02 09:30:00'),
(@exam, 3, 15, 'COMPLETED', '2026-07-02 09:45:00', '2026-07-02 09:45:00'),
(@exam, 4, 18, 'COMPLETED', '2026-07-02 09:45:00', '2026-07-02 09:45:00');

-- 报告 (CRITICAL 危急)
INSERT INTO t_exam_report (report_no, exam_record_id, user_id, reviewer_id, abnormal_status, report_status, summary, doctor_advice, overall_health_assessment, lifestyle_recommendations, follow_up_recommendations, classification, generated_at, published_at, signed_at, created_at, updated_at)
VALUES ('RPT20260702CRITICAL', @exam, @uid, 2, 'CRITICAL', 'PUBLISHED',
  '⚠️ 危急值报告！本次体检发现多项严重异常指标：空腹血糖18.50 mmol/L（正常3.90-6.10），谷丙转氨酶280 U/L（正常0-40），谷草转氨酶195 U/L（正常0-40）。同时存在总胆固醇偏高、甘油三酯偏高、白细胞计数偏高等多项异常。心电图提示窦性心动过速伴ST-T改变。',
  '【紧急建议】\n1. 空腹血糖18.50 mmol/L，提示可能存在严重糖尿病或糖尿病酮症酸中毒风险，请立即到内分泌科就诊！\n2. 转氨酶显著升高（ALT 280, AST 195），提示可能存在严重肝损伤，请立即到消化内科/肝病科就诊！\n3. 心电图ST-T改变，建议心内科进一步评估。\n4. 白细胞计数升高提示可能存在感染。\n请立即就医，不要延误！',
  '本次体检结果非常令人担忧，存在多项危急值指标，提示代谢综合征（严重高血糖、高血脂）、肝功能严重异常、心血管异常等多系统问题。患者需要立即接受全面的医疗干预，不能拖延。',
  '1. 【紧急】立即到综合医院急诊科或内分泌科就诊。\n2. 暂停一切剧烈活动，保持休息。\n3. 严格控制饮食，糖尿病患者饮食管理。\n4. 待病情稳定后由专科医生制定后续健康管理计划。',
  '请立即就医！建议住院进行全面检查和治疗。待病情稳定后，1个月复查血糖、肝功能、心电图，3个月复查全套生化指标。',
  'NEED_TREATMENT',
  '2026-07-02 14:00:00', '2026-07-02 14:30:00', '2026-07-02 14:30:00', '2026-07-02 14:00:00', '2026-07-02 14:30:00');

SET @reportId = LAST_INSERT_ID();

-- 健康管理记录 (危急) — 对应随访干预页面
INSERT INTO t_health_management (user_id, report_id, risk_level, management_status, follow_up_plan, intervention_plan, created_at, updated_at)
VALUES (@uid, @reportId, 'CRITICAL', 'OPEN',
  '建议患者立即到综合医院内分泌科及消化内科就诊。1周内电话随访确认就医情况，1个月后复查空腹血糖、肝功能。',
  '1. 立即联系患者通知危急值，建议紧急就医。\n2. 若患者已就医，跟踪诊断结果。\n3. 纳入高危人群管理，每月随访一次。\n4. 控制血糖、保肝治疗，生活方式全面干预。',
  '2026-07-02 14:30:00', '2026-07-02 14:30:00');

-- 危急值预警通知
INSERT INTO t_notification (user_id, notification_type, title, content, read_flag, created_at)
VALUES (@uid, 'CRITICAL', '⚠️ 危急值预警通知',
  '您的体检报告中发现危急值（空腹血糖18.5 mmol/L，谷丙转氨酶280 U/L），请立即前往医院就诊，详情请查看体检报告。',
  FALSE, '2026-07-02 14:30:00');

SELECT '✅ 危急报告创建完成！' AS 状态;
