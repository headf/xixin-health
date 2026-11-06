-- =====================================================
-- 熙心健康体检管理系统 - 测试数据补充脚本
-- =====================================================
SET NAMES utf8mb4;
USE xixin_health;

-- 关闭外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 补充患者用户 (20人)
-- =====================================================
INSERT IGNORE INTO sys_user (phone, email, display_name, password_hash, enabled, deleted)
VALUES
('13810000001', NULL, '陈伟',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000002', NULL, '周芳',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000003', NULL, '吴建国',    '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000004', NULL, '郑丽华',    '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000005', NULL, '冯明',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000006', NULL, '褚小红',    '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000007', NULL, '蒋涛',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000008', NULL, '韩雪',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000009', NULL, '杨光',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000010', NULL, '朱丽',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000011', NULL, '黄磊',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000012', NULL, '林芳',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000013', NULL, '何强',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000014', NULL, '马丽娟',    '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000015', NULL, '罗伟',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000016', NULL, '梁静',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000017', NULL, '宋涛',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000018', NULL, '许芳',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000019', NULL, '丁磊',      '$2a$10$DUMMY_HASH', TRUE, FALSE),
('13810000020', NULL, '沈丽',      '$2a$10$DUMMY_HASH', TRUE, FALSE);

-- 给新用户分配 NORMAL_USER 角色
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, (SELECT id FROM sys_role WHERE code = 'NORMAL_USER')
FROM sys_user u
WHERE u.phone LIKE '13810000%'
AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = (SELECT id FROM sys_role WHERE code = 'NORMAL_USER'));

-- =====================================================
-- 2. 补充健康档案
-- =====================================================
INSERT IGNORE INTO t_health_profile (id, owner_user_id, name, phone, id_card_no, profile_type, emergency_contact_name, emergency_contact_phone, medical_history, allergy_history, created_at, updated_at, deleted)
SELECT
    10 + n,
    u.id,
    u.display_name,
    u.phone,
    CONCAT('110101', '199', LPAD(n, 2, '0'), '0101', LPAD(n, 4, '0')),
    'SELF',
    CONCAT(u.display_name, '家属'),
    CONCAT('1390000', LPAD(n+1000, 4, '0')),
    CASE n % 5
        WHEN 0 THEN '高血压病史'
        WHEN 1 THEN '无'
        WHEN 2 THEN '糖尿病史'
        WHEN 3 THEN '冠心病史'
        WHEN 4 THEN '过敏体质'
    END,
    CASE n % 4
        WHEN 0 THEN '青霉素过敏'
        WHEN 1 THEN '无'
        WHEN 2 THEN '海鲜过敏'
        WHEN 3 THEN '花粉过敏'
    END,
    NOW(),
    NOW(),
    FALSE
FROM (SELECT ROW_NUMBER() OVER (ORDER BY id) AS n, id, display_name, phone FROM sys_user WHERE phone LIKE '13810000%') u;

-- =====================================================
-- 3. 补充预约记录 (各种状态)
-- =====================================================
INSERT IGNORE INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, status, appointment_date, time_slot, created_at, updated_at, deleted)
SELECT
    CONCAT('APT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 3, '0')),
    u.id,
    hp.id,
    CASE seq % 4
        WHEN 0 THEN 1  -- PKG_BASIC
        WHEN 1 THEN 2  -- PKG_FULL
        WHEN 2 THEN 3  -- PKG_ESSENTIAL
        WHEN 3 THEN 4  -- PKG_DEEP
    END,
    CASE seq % 4
        WHEN 0 THEN 1  -- 总院
        WHEN 1 THEN 2  -- 浦东
        WHEN 2 THEN 3  -- 南山
        WHEN 3 THEN 4  -- 朝阳
    END,
    CASE
        WHEN seq <= 4  THEN 'COMPLETED'
        WHEN seq <= 8  THEN 'CHECKED_IN'
        WHEN seq <= 12 THEN 'PAID'
        WHEN seq <= 16 THEN 'CREATED'
        ELSE 'CANCELLED'
    END,
    CASE
        WHEN seq <= 4  THEN CURDATE() - INTERVAL 7 DAY
        WHEN seq <= 8  THEN CURDATE()
        WHEN seq <= 12 THEN CURDATE() + INTERVAL 1 DAY
        WHEN seq <= 16 THEN CURDATE() + INTERVAL 3 DAY
        ELSE CURDATE() + INTERVAL 5 DAY
    END,
    CASE (seq % 3)
        WHEN 0 THEN '08:00-09:00'
        WHEN 1 THEN '09:00-10:00'
        WHEN 2 THEN '10:00-11:00'
    END,
    NOW() - INTERVAL (20 - seq) DAY,
    NOW() - INTERVAL (20 - seq) DAY,
    FALSE
FROM (
    SELECT u.id, p.id AS profile_id, ROW_NUMBER() OVER (ORDER BY u.id) AS seq
    FROM sys_user u
    JOIN t_health_profile p ON p.owner_user_id = u.id
    WHERE u.phone LIKE '13810000%'
) data
JOIN sys_user u ON u.id = data.id
JOIN t_health_profile hp ON hp.id = data.profile_id;

-- =====================================================
-- 4. 补充订单
-- =====================================================
INSERT IGNORE INTO t_order (order_no, user_id, appointment_id, amount_cent, status, created_at, updated_at)
SELECT
    CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 3, '0')),
    a.user_id,
    a.id,
    p.price_cent,
    CASE a.status
        WHEN 'CANCELLED' THEN 'CLOSED'
        WHEN 'CREATED' THEN 'PENDING_PAYMENT'
        ELSE 'PAID'
    END,
    a.created_at,
    a.updated_at
FROM (
    SELECT a.*, ROW_NUMBER() OVER (ORDER BY a.id) AS seq
    FROM t_appointment a
    WHERE a.appointment_no LIKE CONCAT('APT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
) a
JOIN t_exam_package p ON p.id = a.package_id;

-- =====================================================
-- 5. 补充支付记录
-- =====================================================
INSERT IGNORE INTO t_payment_record (payment_no, order_id, amount_cent, status, channel, paid_at, created_at)
SELECT
    CONCAT('PAY-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 3, '0')),
    o.id,
    o.amount_cent,
    'SUCCESS',
    CASE seq % 3
        WHEN 0 THEN 'ALIPAY'
        WHEN 1 THEN 'WECHAT'
        WHEN 2 THEN 'BANK_CARD'
    END,
    o.created_at,
    o.created_at
FROM (
    SELECT o.*, ROW_NUMBER() OVER (ORDER BY o.id) AS seq
    FROM t_order o
    WHERE o.order_no LIKE CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
      AND o.status = 'PAID'
) o;

-- =====================================================
-- 6. 补充体检记录
-- =====================================================
INSERT IGNORE INTO t_exam_record (exam_no, user_id, profile_id, appointment_id, package_id, package_name, branch_id, status, barcode, checked_in_at, created_at, updated_at)
SELECT
    CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 3, '0')),
    a.user_id,
    a.profile_id,
    a.id,
    a.package_id,
    p.package_name,
    a.branch_id,
    CASE a.status
        WHEN 'COMPLETED' THEN 'COMPLETED'
        WHEN 'CHECKED_IN' THEN 'IN_PROGRESS'
        ELSE 'CHECKED_IN'
    END,
    CONCAT('BC', DATE_FORMAT(CURDATE(), '%Y%m%d'), LPAD(seq, 4, '0')),
    CASE WHEN a.status IN ('CHECKED_IN', 'COMPLETED') THEN a.appointment_date + INTERVAL 8 HOUR ELSE NULL END,
    a.created_at,
    a.updated_at
FROM (
    SELECT a.*, ROW_NUMBER() OVER (ORDER BY a.id) AS seq
    FROM t_appointment a
    WHERE a.appointment_no LIKE CONCAT('APT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
      AND a.status IN ('CHECKED_IN', 'COMPLETED')
) a
JOIN t_exam_package p ON p.id = a.package_id;

-- =====================================================
-- 7. 补充体检进度 (每个套餐的项目)
-- =====================================================
INSERT IGNORE INTO t_exam_progress (exam_record_id, item_id, department_id, progress_status, created_at, updated_at, completed_at)
SELECT
    er.id,
    pi.item_id,
    ei.department_id,
    CASE
        WHEN er.status = 'COMPLETED' THEN 'COMPLETED'
        WHEN er.status = 'IN_PROGRESS' AND (pi.item_id % 3) = 0 THEN 'COMPLETED'
        WHEN er.status = 'IN_PROGRESS' AND (pi.item_id % 3) = 1 THEN 'IN_PROGRESS'
        WHEN er.status = 'IN_PROGRESS' AND (pi.item_id % 3) = 2 THEN 'PENDING'
        ELSE 'PENDING'
    END,
    NOW(),
    NOW(),
    CASE
        WHEN er.status = 'COMPLETED' THEN NOW()
        WHEN er.status = 'IN_PROGRESS' AND (pi.item_id % 3) = 0 THEN NOW()
        ELSE NULL
    END
FROM t_exam_record er
JOIN t_exam_package_item pi ON pi.package_id = er.package_id
JOIN t_exam_item ei ON ei.id = pi.item_id
WHERE er.exam_no LIKE CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%');

-- =====================================================
-- 8. 补充生命体征
-- =====================================================
INSERT IGNORE INTO t_vital_signs (exam_record_id, height, weight, bmi, blood_pressure_systolic, blood_pressure_diastolic, heart_rate, temperature, vision_left, vision_right, waistline, recorded_at, recorded_by, created_at, updated_at)
SELECT
    er.id,
    165 + (er.id % 30),
    60 + (er.id % 40),
    ROUND((60 + (er.id % 40)) / POWER((165 + (er.id % 30)) / 100.0, 2), 2),
    110 + (er.id % 40),
    70 + (er.id % 20),
    68 + (er.id % 20),
    36.3 + (er.id % 10) * 0.1,
    4.8 + (er.id % 20) * 0.1,
    5.0 + (er.id % 20) * 0.05,
    75 + (er.id % 20),
    DATE_ADD(CURDATE(), INTERVAL -7 + (er.id % 7) DAY) + INTERVAL 8 HOUR,
    2, -- 护士小王
    NOW(),
    NOW()
FROM t_exam_record er
WHERE er.exam_no LIKE CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%');

-- =====================================================
-- 9. 补充检查结果
-- =====================================================
INSERT IGNORE INTO t_check_result (result_no, exam_record_id, item_id, item_name, numeric_value, text_value, unit, reference_min, reference_max, critical_max, abnormal_level, review_status, recorder_id, created_at, updated_at)
SELECT
    CONCAT('RES-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 4, '0')),
    ep.exam_record_id,
    ep.item_id,
    ep.item_name,
    CASE ep.item_code
        WHEN 'ITEM_GLU' THEN 4.5 + (ep.exam_record_id % 10) * 0.3
        WHEN 'ITEM_ALT' THEN 15 + (ep.exam_record_id % 30)
        WHEN 'ITEM_WBC' THEN 5.0 + (ep.exam_record_id % 7) * 0.5
        WHEN 'ITEM_RBC' THEN 4.0 + (ep.exam_record_id % 3) * 0.5
        WHEN 'ITEM_HGB' THEN 120 + (ep.exam_record_id % 40)
        WHEN 'ITEM_PLT' THEN 150 + (ep.exam_record_id % 100)
        WHEN 'ITEM_TC' THEN 3.5 + (ep.exam_record_id % 10) * 0.3
        WHEN 'ITEM_TG' THEN 1.0 + (ep.exam_record_id % 8) * 0.2
        WHEN 'ITEM_HDL' THEN 1.0 + (ep.exam_record_id % 5) * 0.1
        WHEN 'ITEM_LDL' THEN 2.0 + (ep.exam_record_id % 5) * 0.3
        WHEN 'ITEM_CREA' THEN 70 + (ep.exam_record_id % 30)
        WHEN 'ITEM_BUN' THEN 4.0 + (ep.exam_record_id % 5) * 0.5
        WHEN 'ITEM_UA' THEN 300 + (ep.exam_record_id % 200)
        WHEN 'ITEM_BP' THEN 120 + (ep.exam_record_id % 20)
        WHEN 'ITEM_BMI' THEN 21.0 + (ep.exam_record_id % 8)
        WHEN 'ITEM_AST' THEN 18 + (ep.exam_record_id % 20)
        WHEN 'ITEM_HBA1C' THEN 5.0 + (ep.exam_record_id % 4) * 0.2
        WHEN 'ITEM_ALB' THEN 40 + (ep.exam_record_id % 10)
        WHEN 'ITEM_GLB' THEN 25 + (ep.exam_record_id % 8)
        WHEN 'ITEM_TBIL' THEN 10 + (ep.exam_record_id % 15)
        ELSE 50 + (ep.exam_record_id % 50)
    END,
    CASE ep.item_code
        WHEN 'ITEM_CXR' THEN CASE ep.exam_record_id % 4 WHEN 0 THEN '未见异常' WHEN 1 THEN '左肺纹理增粗' WHEN 2 THEN '右肺小结节' ELSE '心肺未见异常' END
        WHEN 'ITEM_ECG' THEN CASE ep.exam_record_id % 4 WHEN 0 THEN '窦性心律' WHEN 1 THEN '窦性心律不齐' WHEN 2 THEN 'T波改变' ELSE '正常心电图' END
        WHEN 'ITEM_US_LIVER' THEN CASE ep.exam_record_id % 4 WHEN 0 THEN '未见异常' WHEN 1 THEN '脂肪肝(轻度)' WHEN 2 THEN '肝囊肿' ELSE '未见异常' END
        WHEN 'ITEM_US_KIDNEY' THEN CASE ep.exam_record_id % 4 WHEN 0 THEN '未见异常' WHEN 1 THEN '肾囊肿' WHEN 2 THEN '未见异常' ELSE '未见异常' END
        WHEN 'ITEM_VISION' THEN CASE ep.exam_record_id % 4 WHEN 0 THEN '左5.0 右5.1' WHEN 1 THEN '左4.8 右4.9' WHEN 2 THEN '左5.2 右5.1' ELSE '左5.1 右5.0' END
        WHEN 'ITEM_HEARING' THEN CASE ep.exam_record_id % 4 WHEN 0 THEN '正常' WHEN 1 THEN '正常' WHEN 2 THEN '左耳轻度下降' ELSE '正常' END
        ELSE NULL
    END,
    ep.unit,
    CASE WHEN ep.reference_min IS NOT NULL THEN ep.reference_min ELSE NULL END,
    CASE WHEN ep.reference_max IS NOT NULL THEN ep.reference_max ELSE NULL END,
    NULL,
    CASE
        WHEN (ep.exam_record_id % 5) = 0 THEN 'ABNORMAL'
        WHEN (ep.exam_record_id % 7) = 0 THEN 'CRITICAL'
        ELSE 'NORMAL'
    END,
    CASE
        WHEN ep.progress_status = 'COMPLETED' AND (ep.exam_record_id % 5) = 0 THEN 'PENDING'
        WHEN ep.progress_status = 'COMPLETED' THEN 'APPROVED'
        ELSE 'PENDING'
    END,
    CASE (ep.exam_record_id % 3) WHEN 0 THEN 3 WHEN 1 THEN 11 ELSE 52 END,
    NOW(),
    NOW()
FROM (
    SELECT ep.exam_record_id, ep.item_id, ep.department_id, ep.progress_status, ei.item_code, ei.item_name, ei.unit, ei.reference_min, ei.reference_max, ROW_NUMBER() OVER (ORDER BY ep.exam_record_id, ep.item_id) AS seq
    FROM t_exam_progress ep
    JOIN t_exam_item ei ON ei.id = ep.item_id
    WHERE ep.progress_status IN ('COMPLETED', 'IN_PROGRESS')
) ep;

-- =====================================================
-- 10. 补充标本记录 (检验科血检相关)
-- =====================================================
INSERT IGNORE INTO t_specimen (specimen_no, exam_record_id, barcode, specimen_type, status, collected_at, collected_by, sent_to_lab_at, sent_to_lab_by, received_at, received_by, created_at, updated_at)
SELECT
    CONCAT('SP-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 3, '0')),
    er.id,
    CONCAT('BC', DATE_FORMAT(CURDATE(), '%Y%m%d'), LPAD(seq, 4, '0')),
    'BLOOD',
    CASE er.status WHEN 'COMPLETED' THEN 'RECEIVED' WHEN 'IN_PROGRESS' THEN 'COLLECTED' ELSE 'COLLECTED' END,
    DATE_ADD(CURDATE(), INTERVAL -7 + (er.id % 7) DAY) + INTERVAL 8 HOUR,
    60, -- 采血护士
    DATE_ADD(CURDATE(), INTERVAL -7 + (er.id % 7) DAY) + INTERVAL 9 HOUR,
    2, -- 护士小王
    CASE WHEN er.status = 'COMPLETED' THEN DATE_ADD(CURDATE(), INTERVAL -7 + (er.id % 7) DAY) + INTERVAL 10 HOUR ELSE NULL END,
    50, -- 检验科医生
    NOW(),
    NOW()
FROM (
    SELECT er.*, ROW_NUMBER() OVER (ORDER BY er.id) AS seq
    FROM t_exam_record er
    WHERE er.exam_no LIKE CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
) er;

-- =====================================================
-- 11. 补充科室队列
-- =====================================================
INSERT IGNORE INTO t_department_queue (exam_record_id, department_id, queue_no, queue_status, created_at, updated_at)
SELECT
    ep.exam_record_id,
    ep.department_id,
    CASE ep.department_id % 3
        WHEN 0 THEN CONCAT('A', LPAD(ep.exam_record_id, 3, '0'))
        WHEN 1 THEN CONCAT('B', LPAD(ep.exam_record_id, 3, '0'))
        WHEN 2 THEN CONCAT('C', LPAD(ep.exam_record_id, 3, '0'))
    END,
    CASE ep.progress_status
        WHEN 'COMPLETED' THEN 'COMPLETED'
        WHEN 'IN_PROGRESS' THEN 'SERVING'
        ELSE 'WAITING'
    END,
    NOW(),
    NOW()
FROM (
    SELECT DISTINCT exam_record_id, department_id, progress_status
    FROM t_exam_progress
    WHERE exam_record_id IN (SELECT id FROM t_exam_record WHERE exam_no LIKE CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%'))
) ep;

-- =====================================================
-- 12. 补充指导单
-- =====================================================
INSERT IGNORE INTO t_guide_sheet (guide_no, exam_record_id, route_text, created_at)
SELECT
    CONCAT('GD-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 3, '0')),
    er.id,
    CONCAT('→ 一般检查(测身高体重血压) → 采血(空腹) → 内科 → 外科 → 心电图 → B超 → ', CASE er.branch_id WHEN 1 THEN '眼科' WHEN 2 THEN '耳鼻喉' WHEN 3 THEN '口腔科' ELSE '骨密度' END, ' → 总检审核 → 取报告'),
    NOW()
FROM (
    SELECT er.*, ROW_NUMBER() OVER (ORDER BY er.id) AS seq
    FROM t_exam_record er
    WHERE er.exam_no LIKE CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
) er;

-- =====================================================
-- 13. 补充完成记录
-- =====================================================
INSERT IGNORE INTO t_exam_completion (exam_record_id, all_items_checked, specimen_collected, vital_signs_taken, nurse_confirmed, needs_review, confirmed_at, confirmed_by, created_at, updated_at)
SELECT
    er.id,
    er.status = 'COMPLETED',
    TRUE,
    TRUE,
    er.status = 'COMPLETED',
    CASE WHEN er.status = 'COMPLETED' THEN 1 ELSE 0 END,
    CASE WHEN er.status = 'COMPLETED' THEN DATE_ADD(CURDATE(), INTERVAL -7 + (er.id % 7) DAY) + INTERVAL 12 HOUR ELSE NULL END,
    CASE WHEN er.status = 'COMPLETED' THEN 2 ELSE NULL END, -- 护士确认
    NOW(),
    NOW()
FROM t_exam_record er
WHERE er.exam_no LIKE CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%');

-- =====================================================
-- 14. 补充体检报告
-- =====================================================
INSERT IGNORE INTO t_exam_report (report_no, exam_record_id, user_id, template_id, abnormal_status, classification, overall_health_assessment, summary, doctor_advice, lifestyle_recommendations, follow_up_recommendations, report_status, reviewer_id, generated_at, published_at, signed_at, created_at, updated_at)
SELECT
    CONCAT('RPT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(seq, 3, '0')),
    er.id,
    er.user_id,
    (er.package_id % 4) + 1, -- template_id
    CASE (er.id % 5) WHEN 0 THEN 'ABNORMAL' WHEN 4 THEN 'CRITICAL' ELSE 'NORMAL' END,
    CASE er.package_id WHEN 1 THEN '基础' WHEN 2 THEN '全面' WHEN 3 THEN '精简' WHEN 4 THEN '深度' END,
    CONCAT(CASE (er.id % 5)
        WHEN 0 THEN '本次体检发现部分指标异常，建议定期复查并注意生活方式调整'
        WHEN 1 THEN '整体健康状况良好，各项指标均在正常范围内'
        WHEN 2 THEN '整体健康状况尚可，个别指标轻度异常，建议保持健康生活方式'
        WHEN 3 THEN '健康状况良好，建议继续保持良好的生活习惯'
        ELSE '体检发现部分指标需关注，已标注危急值项目，请尽快就医'
    END),
    CONCAT(CASE (er.id % 4)
        WHEN 0 THEN '本次体检涵盖血液生化、影像检查等项目。'
        WHEN 1 THEN '本次体检涵盖常规项目及专科检查。'
        WHEN 2 THEN '本次体检为常规年度健康体检。'
        ELSE '本次深度体检涵盖多个系统检查。'
    END, '总体评估结论详见报告。'),
    CASE (er.id % 5)
        WHEN 0 THEN '建议定期监测血糖、血脂等指标，每3个月复查一次；保持低盐低脂饮食'
        WHEN 1 THEN '继续保持当前健康生活方式，建议每周运动3-5次'
        WHEN 2 THEN '建议戒烟限酒，增加蔬菜水果摄入，保持适当运动'
        WHEN 3 THEN '建议每年进行一次全面体检，关注血压变化'
        ELSE '建议尽快到专科医院进一步检查异常指标，避免延误治疗'
    END,
    CASE (er.id % 4)
        WHEN 0 THEN '控制饮食，减少高脂高糖食物；建议每周至少150分钟中等强度运动'
        WHEN 1 THEN '保持规律作息，充足睡眠；多饮水，适量运动'
        WHEN 2 THEN '保持心情舒畅，避免熬夜；均衡饮食，增加钙质摄入'
        ELSE '减少久坐，增加户外活动；少食多餐，注意饮食清淡'
    END,
    CASE (er.id % 4)
        WHEN 0 THEN '建议3个月后复查肝功能及血糖'
        WHEN 1 THEN '建议6个月后复查异常指标'
        WHEN 2 THEN '建议每年进行定期体检'
        ELSE '建议1个月后到专科门诊复查'
    END,
    CASE WHEN er.status = 'COMPLETED' THEN 'PUBLISHED' ELSE 'DRAFT' END,
    CASE WHEN er.status = 'COMPLETED' THEN 4 ELSE NULL END, -- 审核医生
    CASE WHEN er.status = 'COMPLETED' THEN NOW() - INTERVAL 1 DAY ELSE NOW() END,
    CASE WHEN er.status = 'COMPLETED' THEN NOW() - INTERVAL 1 DAY ELSE NULL END,
    CASE WHEN er.status = 'COMPLETED' THEN NOW() - INTERVAL 1 DAY ELSE NULL END,
    NOW() - INTERVAL 1 DAY,
    NOW()
FROM (
    SELECT er.*, ROW_NUMBER() OVER (ORDER BY er.id) AS seq
    FROM t_exam_record er
    WHERE er.exam_no LIKE CONCAT('EXM-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
      AND er.status IN ('COMPLETED', 'IN_PROGRESS')
) er;

-- =====================================================
-- 15. 补充审核记录
-- =====================================================
INSERT IGNORE INTO t_result_review (result_id, reviewer_id, approved, review_comment, reviewed_at)
SELECT
    cr.id,
    4, -- 审核医生张主任
    CASE (cr.id % 4) WHEN 0 THEN FALSE ELSE TRUE END,
    CASE (cr.id % 4)
        WHEN 0 THEN '需复检确认后再次审核'
        WHEN 1 THEN '结果确认无误'
        WHEN 2 THEN '已审核，数值在正常波动范围内'
        ELSE '审核通过'
    END,
    NOW()
FROM t_check_result cr
WHERE cr.result_no LIKE CONCAT('RES-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
  AND cr.abnormal_level IN ('ABNORMAL', 'CRITICAL');

-- =====================================================
-- 16. 补充健康分析
-- =====================================================
INSERT IGNORE INTO t_health_analysis (report_id, user_id, risk_level, abnormal_count, critical_count, trend_data, created_at)
SELECT
    r.id,
    r.user_id,
    CASE r.abnormal_status
        WHEN 'NORMAL' THEN 'LOW'
        WHEN 'ABNORMAL' THEN 'MEDIUM'
        WHEN 'CRITICAL' THEN 'HIGH'
        ELSE 'LOW'
    END,
    (r.id % 8) + 1,
    CASE WHEN r.abnormal_status = 'CRITICAL' THEN 1 ELSE 0 END,
    CONCAT('{"trend": "', CASE (r.id % 3) WHEN 0 THEN 'stable' WHEN 1 THEN 'improving' ELSE 'worsening' END, '"}'),
    NOW()
FROM t_exam_report r
WHERE r.report_no LIKE CONCAT('RPT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-%')
  AND r.report_status = 'PUBLISHED';

-- =====================================================
-- 17. 补充健康管理方案
-- =====================================================
INSERT IGNORE INTO t_health_management (user_id, report_id, risk_level, management_status, intervention_plan, follow_up_plan, created_at, updated_at)
SELECT
    ha.user_id,
    ha.report_id,
    ha.risk_level,
    CASE ha.risk_level
        WHEN 'LOW' THEN 'MONITORING'
        WHEN 'MEDIUM' THEN 'ACTIVE_INTERVENTION'
        WHEN 'HIGH' THEN 'URGENT_INTERVENTION'
        ELSE 'MONITORING'
    END,
    CASE ha.risk_level
        WHEN 'LOW' THEN '建议定期年度体检，保持健康生活方式'
        WHEN 'MEDIUM' THEN '建议每季度复查异常指标，低盐低脂饮食，每周至少150分钟有氧运动'
        WHEN 'HIGH' THEN '已建议专科就诊，需密切监测指标变化，每周测量血压血糖'
        ELSE '常规健康管理'
    END,
    CASE ha.risk_level
        WHEN 'LOW' THEN '下次体检时间：1年后'
        WHEN 'MEDIUM' THEN '下次复查时间：3个月后'
        WHEN 'HIGH' THEN '建议1周内专科就诊，2周后复查'
        ELSE '定期随访'
    END,
    NOW(),
    NOW()
FROM t_health_analysis ha
WHERE ha.created_at > NOW() - INTERVAL 1 DAY;

-- =====================================================
-- 18. 补充通知
-- =====================================================
INSERT IGNORE INTO t_notification (user_id, notification_type, title, content, read_flag, read_at, created_at)
SELECT
    u.id,
    CASE (u.id % 4)
        WHEN 0 THEN 'SYSTEM'
        WHEN 1 THEN 'APPOINTMENT'
        WHEN 2 THEN 'REPORT'
        WHEN 3 THEN 'PAYMENT'
    END,
    CASE (u.id % 4)
        WHEN 0 THEN '系统通知'
        WHEN 1 THEN '预约提醒'
        WHEN 2 THEN '报告已生成'
        WHEN 3 THEN '支付成功'
    END,
    CASE (u.id % 4)
        WHEN 0 THEN '您的体检报告已生成，请登录查看'
        WHEN 1 THEN CONCAT('您有体检预约于 ', DATE_FORMAT(CURDATE() + INTERVAL 1 DAY, '%Y年%m月%d日'), '，请按时前往')
        WHEN 2 THEN '恭喜您，您的体检报告已完成审核，可在线查看'
        WHEN 3 THEN CONCAT('您的体检费用已支付成功，金额: ', (u.id % 4 + 1) * 199, '元')
    END,
    (u.id % 3) = 0, -- 1/3 已读
    CASE WHEN (u.id % 3) = 0 THEN NOW() - INTERVAL 1 HOUR ELSE NULL END,
    NOW() - INTERVAL (u.id % 7) DAY
FROM sys_user u
WHERE u.phone LIKE '13810000%';

-- =====================================================
-- 19. 补充操作日志
-- =====================================================
INSERT IGNORE INTO t_operation_log (operator_id, operation_type, target_type, target_id, result, detail, ip_address, user_agent, operation_time, entry_hash, previous_hash, trace_id)
SELECT
    CASE (seq % 5)
        WHEN 0 THEN 1  -- 系统管理员
        WHEN 1 THEN 2  -- 护士
        WHEN 2 THEN 3  -- 体检医生
        WHEN 3 THEN 4  -- 审核医生
        WHEN 4 THEN 10 -- 刘护士
    END,
    CASE (seq % 8)
        WHEN 0 THEN 'LOGIN'
        WHEN 1 THEN 'APPOINTMENT_VIEW'
        WHEN 2 THEN 'RESULT_RECORD'
        WHEN 3 THEN 'REPORT_REVIEW'
        WHEN 4 THEN 'CONFIG_UPDATE'
        WHEN 5 THEN 'USER_QUERY'
        WHEN 6 THEN 'STAT_QUERY'
        WHEN 7 THEN 'LOGOUT'
    END,
    CASE (seq % 5)
        WHEN 0 THEN 'appointment'
        WHEN 1 THEN 'exam_record'
        WHEN 2 THEN 'check_result'
        WHEN 3 THEN 'exam_report'
        WHEN 4 THEN 'sys_user'
    END,
    (seq % 20) + 1,
    CASE (seq % 10) WHEN 0 THEN 'FAILED' ELSE 'SUCCESS' END,
    CASE (seq % 8)
        WHEN 0 THEN '用户登录系统'
        WHEN 1 THEN '查看预约列表'
        WHEN 2 THEN '录入体检结果'
        WHEN 3 THEN '审核体检报告'
        WHEN 4 THEN '更新系统配置'
        WHEN 5 THEN '查询用户信息'
        WHEN 6 THEN '查看统计数据'
        WHEN 7 THEN '用户退出系统'
    END,
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    NOW() - INTERVAL (seq % 7) DAY,
    SHA2(CONCAT('op_', seq, '_', NOW()), 256),
    SHA2(CONCAT('prev_', seq - 1), 256),
    UUID()
FROM (SELECT ROW_NUMBER() OVER () AS seq FROM sys_user LIMIT 30) t;

-- =====================================================
-- 20. 补充家庭成员
-- =====================================================
INSERT IGNORE INTO t_family_member (owner_user_id, name, phone, id_card_no, relationship, created_at, updated_at, deleted)
SELECT
    u.id,
    CONCAT(u.display_name, CASE (u.id % 3) WHEN 0 THEN '伴侣' WHEN 1 THEN '子女' WHEN 2 THEN '父母' END),
    CONCAT('139', LPAD(u.id + 2000, 5, '0')),
    CONCAT('110101', '19', 80 + (u.id % 20), '0', u.id % 12 + 1, '01', LPAD(u.id, 4, '0')),
    CASE (u.id % 3) WHEN 0 THEN '夫妻' WHEN 1 THEN '子女' WHEN 2 THEN '父母' END,
    NOW(),
    NOW(),
    FALSE
FROM sys_user u
WHERE u.phone LIKE '13810000%'
  AND (u.id % 3) = 0;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '=== 测试数据补充完成 ===' AS result;
