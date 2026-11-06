-- ============================================================
-- 熙心健康体检管理系统 - 演示数据重置脚本
-- 用途：清空所有业务数据并重新插入一致的演示数据
-- 特点：使用固定日期，每次运行结果完全一致
-- 前置条件：MySQL 8.0+，数据库 xixin_health 已存在（JPA自动建表）
-- 执行：mysql -u root -p123456 --default-character-set=utf8mb4 xixin_health < reset-demo-data.sql
-- ============================================================

-- 设置客户端字符集为 utf8mb4（防止中文乱码）
SET NAMES utf8mb4;

-- ============================================================
-- 确保数据库和所有表使用 utf8mb4（解决中文乱码）
-- ============================================================
ALTER DATABASE xixin_health CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE sys_permission     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_role           CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_user           CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_branch           CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_department       CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_exam_item        CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_exam_package     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_flow_node        CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_business_rule    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_report_template  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_health_profile   CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_family_member    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_appointment      CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_order            CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_payment_record   CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_exam_record      CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_guide_sheet      CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_exam_progress    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_department_queue CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_check_result     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE t_doctor_schedule   CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 确保 t_doctor_schedule 表存在
CREATE TABLE IF NOT EXISTS t_doctor_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,
    time_slot VARCHAR(32) NOT NULL COMMENT 'MORNING/AFTERNOON/ALL_DAY',
    max_patients INT NOT NULL DEFAULT 30,
    booked_count INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_branch_date (branch_id, schedule_date),
    INDEX idx_doctor_date (doctor_id, schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 固定参考日期（所有演示数据使用此日期，确保一致性）
-- 如需调整，修改下面这个变量即可
-- ============================================================
SET @REF_DATE = '2026-07-01';
SET @REF_DATETIME = '2026-07-01 08:00:00';

-- ============================================================
-- 第一步：清空所有业务数据（关闭外键检查避免顺序问题）
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE t_check_result;
TRUNCATE TABLE t_result_review;
TRUNCATE TABLE t_exam_progress;
TRUNCATE TABLE t_department_queue;
TRUNCATE TABLE t_guide_sheet;
TRUNCATE TABLE t_exam_completion;
TRUNCATE TABLE t_specimen;
TRUNCATE TABLE t_vital_signs;
TRUNCATE TABLE t_exam_record;
TRUNCATE TABLE t_payment_record;
TRUNCATE TABLE t_refund_record;
TRUNCATE TABLE t_order;
TRUNCATE TABLE t_appointment;
TRUNCATE TABLE t_notification;
TRUNCATE TABLE t_health_management;
TRUNCATE TABLE t_health_analysis;
TRUNCATE TABLE t_exam_report;
TRUNCATE TABLE t_report_history_link;
TRUNCATE TABLE t_operation_log;
TRUNCATE TABLE t_family_member;
TRUNCATE TABLE t_health_profile;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE t_exam_package_item;
TRUNCATE TABLE t_exam_package;
TRUNCATE TABLE t_exam_item;
TRUNCATE TABLE t_department;
TRUNCATE TABLE t_branch;
TRUNCATE TABLE t_flow_node;
TRUNCATE TABLE t_business_rule;
TRUNCATE TABLE t_report_template;
TRUNCATE TABLE t_doctor_schedule;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 第二步：基础权限
-- ============================================================
INSERT INTO sys_permission (id, code, name) VALUES
(1, 'AUTH_LOGIN',           '登录认证'),
(2, 'AUTH_REGISTER',        '用户注册'),
(3, 'USER_MANAGE',          '用户管理'),
(4, 'ROLE_MANAGE',          '角色管理'),
(5, 'DEPARTMENT_MANAGE',    '科室管理'),
(6, 'EXAM_ITEM_MANAGE',     '检查项目管理'),
(7, 'PACKAGE_MANAGE',       '套餐管理'),
(8, 'APPOINTMENT_MANAGE',   '预约管理'),
(9, 'APPOINTMENT_CREATE',   '创建预约'),
(10, 'APPOINTMENT_CANCEL',  '取消预约'),
(11, 'EXAM_CHECKIN',        '签到登记'),
(12, 'EXAM_QUEUE_VIEW',     '查看排队'),
(13, 'EXAM_QUEUE_MANAGE',   '排队管理'),
(14, 'EXAM_PROGRESS_VIEW',  '查看进度'),
(15, 'EXAM_PROGRESS_UPDATE','更新进度'),
(16, 'RESULT_RECORD',       '录入结果'),
(17, 'RESULT_RECORD_TEXT',  '录入文本结果'),
(18, 'RESULT_RECORD_BATCH', '批量录入结果'),
(19, 'RESULT_QUERY',        '查询结果'),
(20, 'RESULT_QUERY_PENDING','查询待审核'),
(21, 'RESULT_REVIEW',       '审核结果'),
(22, 'REPORT_GENERATE',     '生成报告'),
(23, 'REPORT_REVIEW',       '审核报告'),
(24, 'REPORT_PUBLISH',      '发布报告'),
(25, 'REPORT_VIEW',         '查看报告'),
(26, 'ORDER_MANAGE',        '订单管理'),
(27, 'PAYMENT_PROCESS',     '支付处理'),
(28, 'REFUND_PROCESS',      '退款处理'),
(29, 'BRANCH_MANAGE',       '分院管理'),
(30, 'BUSINESS_RULE_MANAGE','业务规则管理'),
(31, 'FLOW_NODE_MANAGE',    '流程节点管理'),
(32, 'OPERATION_LOG_VIEW',  '查看操作日志'),
(33, 'SYSTEM_CONFIG',       '系统配置'),
(34, 'NOTIFICATION_SEND',   '发送通知'),
(35, 'AUDIT_LOG_VIEW',      '审计日志查看'),
(36, 'CONTENT_MANAGE',      '内容管理'),
(37, 'INTERVENTION_MANAGE', '干预管理');

-- ============================================================
-- 第三步：角色
-- ============================================================
INSERT INTO sys_role (id, code, name, role_type, system_built_in, deleted) VALUES
(1,  'NURSE',         '护士',           'STAFF',  TRUE, FALSE),
(2,  'EXAM_DOCTOR',   '体检医生',       'STAFF',  TRUE, FALSE),
(3,  'REVIEW_DOCTOR', '审核医生',       'STAFF',  TRUE, FALSE),
(4,  'NORMAL_USER',   '普通用户',       'USER',   TRUE, FALSE),
(5,  'SYSTEM_ADMIN',  '系统管理员',     'ADMIN',  TRUE, FALSE),
(6,  'OPERATIONS_ADMIN', '运营管理员',  'ADMIN',  TRUE, FALSE),
(10, 'EXAM_DOCTOR_LAB',       '检验科医生',   'STAFF', TRUE, FALSE),
(11, 'EXAM_DOCTOR_IMAGING',   '影像科医生',   'STAFF', TRUE, FALSE),
(12, 'EXAM_DOCTOR_INTERNAL',  '内科医生',     'STAFF', TRUE, FALSE),
(13, 'EXAM_DOCTOR_SURGERY',   '外科医生',     'STAFF', TRUE, FALSE),
(14, 'EXAM_DOCTOR_OPHTHALM',  '眼科医生',     'STAFF', TRUE, FALSE),
(15, 'EXAM_DOCTOR_ENT',       '耳鼻喉科医生', 'STAFF', TRUE, FALSE),
(16, 'EXAM_DOCTOR_DENTAL',    '口腔科医生',   'STAFF', TRUE, FALSE),
(17, 'EXAM_DOCTOR_ECG',       '心电图医生',   'STAFF', TRUE, FALSE),
(18, 'EXAM_DOCTOR_ULTRASOUND','超声科医生',   'STAFF', TRUE, FALSE),
(19, 'EXAM_DOCTOR_BMD',       '骨密度医生',   'STAFF', TRUE, FALSE),
(20, 'EXAM_DOCTOR_BLOOD',     '采血护士',     'STAFF', TRUE, FALSE);

-- 角色-权限分配
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
-- NURSE 权限
(1, 1), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17),
-- EXAM_DOCTOR 权限
(2, 1), (2, 12), (2, 14), (2, 16), (2, 17), (2, 18), (2, 19),
-- REVIEW_DOCTOR 权限
(3, 1), (3, 20), (3, 21), (3, 22), (3, 23), (3, 24), (3, 25),
-- NORMAL_USER 权限
(4, 1), (4, 2), (4, 9), (4, 10), (4, 25),
-- SYSTEM_ADMIN 全部权限
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10),
(5, 11), (5, 12), (5, 13), (5, 14), (5, 15), (5, 16), (5, 17), (5, 18),
(5, 19), (5, 20), (5, 21), (5, 22), (5, 23), (5, 24), (5, 25), (5, 26),
(5, 27), (5, 28), (5, 29), (5, 30), (5, 31), (5, 32), (5, 33), (5, 34),
(5, 35), (5, 36), (5, 37),
-- OPERATIONS_ADMIN 权限
(6, 1), (6, 3), (6, 5), (6, 6), (6, 7), (6, 8), (6, 11), (6, 12), (6, 13),
(6, 14), (6, 15), (6, 16), (6, 19), (6, 20), (6, 22), (6, 25), (6, 26),
(6, 27), (6, 28), (6, 29), (6, 30), (6, 31), (6, 32), (6, 34), (6, 36), (6, 37);

-- 专科医生角色权限（继承 EXAM_DOCTOR 的基础权限）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.code IN ('EXAM_DOCTOR_LAB','EXAM_DOCTOR_IMAGING','EXAM_DOCTOR_INTERNAL',
                 'EXAM_DOCTOR_SURGERY','EXAM_DOCTOR_OPHTHALM','EXAM_DOCTOR_ENT',
                 'EXAM_DOCTOR_DENTAL','EXAM_DOCTOR_ECG','EXAM_DOCTOR_ULTRASOUND',
                 'EXAM_DOCTOR_BMD','EXAM_DOCTOR_BLOOD')
  AND p.code IN ('AUTH_LOGIN', 'RESULT_RECORD', 'RESULT_RECORD_TEXT', 'RESULT_RECORD_BATCH',
                 'EXAM_QUEUE_VIEW', 'EXAM_PROGRESS_UPDATE');

-- ============================================================
-- 第四步：用户账号（密码均为 Passw0rd!）
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ============================================================
INSERT INTO sys_user (id, phone, display_name, password_hash, enabled, deleted, created_at, updated_at) VALUES
-- 管理员
(1,  '13900000000', '系统管理员', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
-- 医护人员
(2,  '13800000100', '护士小王',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(3,  '13800000200', '体检医生老李', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(4,  '13800000300', '审核医生张主任','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
-- 普通用户（体检者）
(5,  '13800000001', '张三', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(6,  '13800000002', '李四', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(7,  '13800000003', '王五', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(8,  '13800000004', '赵六', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(9,  '13800000005', '孙七', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
-- 分院医护人员（各分院）
(10, '13800001911', '刘护士', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(11, '13800001912', '王医生', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(12, '13800001913', '赵主任', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
-- 专科医生（总院）
(50, '13800001101', '总院检验科医生',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(51, '13800001102', '总院影像科医生',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(52, '13800001103', '总院内科医生',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(53, '13800001104', '总院外科医生',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(54, '13800001105', '总院眼科医生',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(55, '13800001106', '总院耳鼻喉科医生', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(56, '13800001107', '总院口腔科医生',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(57, '13800001108', '总院超声科医生',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(58, '13800001109', '总院心电图医生',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(59, '13800001110', '总院骨密度医生',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME),
(60, '13800001111', '总院采血护士',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 0, @REF_DATETIME, @REF_DATETIME);

-- 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
-- 管理员
(1, 5), (1, 6),
-- 医护人员
(2, 1), (3, 2), (4, 3),
-- 普通用户
(5, 4), (6, 4), (7, 4), (8, 4), (9, 4),
-- 分院医护人员
(10, 1), (11, 2), (12, 3),
-- 专科医生（总院）
(50, 10), (51, 11), (52, 12), (53, 13), (54, 14),
(55, 15), (56, 16), (57, 18), (58, 17), (59, 19), (60, 20);

-- Auto-increment 修复
SET @max_id = (SELECT MAX(id) FROM sys_user);
SET @sql = CONCAT('ALTER TABLE sys_user AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 第五步：分院
-- ============================================================
INSERT INTO t_branch (id, branch_code, branch_name, address, service_phone, enabled, created_at, updated_at) VALUES
(1, 'HQ', '熙心健康体检中心（总院）',  '北京市海淀区中关村大街1号',  '010-88886666', 1, @REF_DATETIME, @REF_DATETIME),
(2, 'PD', '熙心健康体检中心（浦东分院）', '上海市浦东新区陆家嘴金融中心2号', '021-68886666', 1, @REF_DATETIME, @REF_DATETIME),
(3, 'NS', '熙心健康体检中心（南山分院）', '深圳市南山区科技园南路3号', '0755-86668888', 1, @REF_DATETIME, @REF_DATETIME),
(4, 'CY', '熙心健康体检中心（朝阳分院）', '北京市朝阳区建国路88号',    '010-85886666', 1, @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第六步：科室
-- ============================================================
INSERT INTO t_department (id, branch_id, department_code, department_name, location, capacity, fasting_required, enabled, created_at, updated_at) VALUES
-- 总院
(1,  1, 'D001', '检验科',   '1楼A区', 30, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(2,  1, 'D002', '影像科',   '1楼B区', 20, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(3,  1, 'D003', '内科',     '2楼A区', 25, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(4,  1, 'D004', '外科',     '2楼B区', 20, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(5,  1, 'D005', '眼科',     '3楼A区', 15, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(6,  1, 'D006', '耳鼻喉科', '3楼B区', 15, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(7,  1, 'D007', '口腔科',   '3楼C区', 15, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(8,  1, 'D008', '超声科',   '4楼A区', 25, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(9,  1, 'D009', '心电图室', '4楼B区', 20, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(10, 1, 'D010', '骨密度室', '4楼C区', 10, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(11, 1, 'D011', '采血室',   '1楼C区', 40, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
-- 浦东分院
(12, 2, 'PD_INSPECTION',   '一般检查', '1楼A区', 40, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(13, 2, 'PD_BLOOD',        '抽血检验', '1楼B区', 60, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(14, 2, 'PD_INTERNAL',     '内科',     '2楼A区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(15, 2, 'PD_SURGERY',      '外科',     '2楼B区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(16, 2, 'PD_ECG',          '心电图',   '3楼A区', 35, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(17, 2, 'PD_ULTRASOUND',   '彩超',     '3楼B区', 25, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(18, 2, 'PD_ENT',          '耳鼻喉科', '4楼A区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(19, 2, 'PD_OPHTHALMOLOGY','眼科',     '4楼B区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
-- 南山分院
(20, 3, 'NS_INSPECTION',   '一般检查', '1楼A区', 40, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(21, 3, 'NS_BLOOD',        '抽血检验', '1楼B区', 60, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(22, 3, 'NS_INTERNAL',     '内科',     '2楼A区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(23, 3, 'NS_SURGERY',      '外科',     '2楼B区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(24, 3, 'NS_ECG',          '心电图',   '3楼A区', 35, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(25, 3, 'NS_ULTRASOUND',   '彩超',     '3楼B区', 25, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(26, 3, 'NS_ENT',          '耳鼻喉科', '4楼A区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(27, 3, 'NS_OPHTHALMOLOGY','眼科',     '4楼B区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
-- 朝阳分院
(28, 4, 'CY_INSPECTION',   '一般检查', '1楼A区', 40, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(29, 4, 'CY_BLOOD',        '抽血检验', '1楼B区', 60, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(30, 4, 'CY_INTERNAL',     '内科',     '2楼A区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(31, 4, 'CY_SURGERY',      '外科',     '2楼B区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(32, 4, 'CY_ECG',          '心电图',   '3楼A区', 35, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(33, 4, 'CY_ULTRASOUND',   '彩超',     '3楼B区', 25, TRUE,  TRUE, @REF_DATETIME, @REF_DATETIME),
(34, 4, 'CY_ENT',          '耳鼻喉科', '4楼A区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME),
(35, 4, 'CY_OPHTHALMOLOGY','眼科',     '4楼B区', 30, FALSE, TRUE, @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第七步：体检项目
-- ============================================================
INSERT INTO t_exam_item (id, item_code, item_name, department_id, unit, reference_min, reference_max, enabled, created_at, updated_at) VALUES
(1,  'ITEM_GLU',       '血糖',         1,  'mmol/L',    3.9000,  6.1000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(2,  'ITEM_ALT',       '谷丙转氨酶',   1,  'U/L',       0.0000, 40.0000, TRUE, @REF_DATETIME, @REF_DATETIME),
(3,  'ITEM_CXR',       '胸部X光',      2,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(4,  'ITEM_WBC',       '白细胞计数',   1,  '×10⁹/L',  3.5000,  9.5000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(5,  'ITEM_RBC',       '红细胞计数',   1,  '×10¹²/L', 4.3000,  5.8000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(6,  'ITEM_HGB',       '血红蛋白',     1,  'g/L',      130.0000,175.0000,TRUE, @REF_DATETIME, @REF_DATETIME),
(7,  'ITEM_PLT',       '血小板计数',   1,  '×10⁹/L',  125.0000,350.0000,TRUE, @REF_DATETIME, @REF_DATETIME),
(8,  'ITEM_TC',        '总胆固醇',     1,  'mmol/L',    0.0000,  5.2000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(9,  'ITEM_TG',        '甘油三酯',     1,  'mmol/L',    0.0000,  1.7000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(10, 'ITEM_HDL',       '高密度脂蛋白', 1,  'mmol/L',    1.0000,  NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(11, 'ITEM_LDL',       '低密度脂蛋白', 1,  'mmol/L',    0.0000,  3.4000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(12, 'ITEM_CREA',      '肌酐',         1,  'μmol/L',   57.0000,111.0000,TRUE, @REF_DATETIME, @REF_DATETIME),
(13, 'ITEM_BUN',       '尿素氮',       1,  'mmol/L',    2.9000,  8.2000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(14, 'ITEM_UA',        '尿酸',         1,  'μmol/L',  200.0000,420.0000,TRUE, @REF_DATETIME, @REF_DATETIME),
(15, 'ITEM_TBIL',      '总胆红素',     1,  'μmol/L',    0.0000, 21.0000, TRUE, @REF_DATETIME, @REF_DATETIME),
(16, 'ITEM_ALB',       '白蛋白',       1,  'g/L',       35.0000, 55.0000, TRUE, @REF_DATETIME, @REF_DATETIME),
(17, 'ITEM_GLB',       '球蛋白',       1,  'g/L',       20.0000, 35.0000, TRUE, @REF_DATETIME, @REF_DATETIME),
(18, 'ITEM_AST',       '谷草转氨酶',   1,  'U/L',       0.0000, 40.0000, TRUE, @REF_DATETIME, @REF_DATETIME),
(19, 'ITEM_HBA1C',     '糖化血红蛋白', 1,  '%',         4.0000,  6.0000,  TRUE, @REF_DATETIME, @REF_DATETIME),
(20, 'ITEM_CT_CHEST',  '胸部CT',       2,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(21, 'ITEM_US_LIVER',  '肝脏超声',     8,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(22, 'ITEM_US_KIDNEY', '肾脏超声',     8,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(23, 'ITEM_US_THYROID','甲状腺超声',   8,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(24, 'ITEM_US_PROSTATE','前列腺超声',  8,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(25, 'ITEM_US_UTERUS', '子宫附件超声', 8,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(26, 'ITEM_US_CAROTID', '颈动脉超声',  8,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(27, 'ITEM_US_HEART',  '心脏彩色超声', 8,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(28, 'ITEM_BMD',       '骨密度检测',   10, NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(29, 'ITEM_VISION',    '视力检查',     5,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(30, 'ITEM_HEARING',   '听力检查',     6,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(31, 'ITEM_ECG',       '心电图',       9,  NULL,        NULL,    NULL,    TRUE, @REF_DATETIME, @REF_DATETIME),
(32, 'ITEM_BP',        '血压',         3,  'mmHg',      90.0000,140.0000,TRUE, @REF_DATETIME, @REF_DATETIME),
(33, 'ITEM_BMI',       'BMI指数',      3,  'kg/m²',    18.5000, 24.9000, TRUE, @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第八步：体检套餐
-- package_id: 1=基础, 2=全面型, 3=精简型, 4=深度型
-- 注意：这里使用 4 个套餐与 DataSeeder/init-seed-data 保持一致
-- ============================================================
INSERT INTO t_exam_package (id, package_code, package_name, category, gender_limit, age_range, price_cent, enabled, created_at, updated_at) VALUES
(1, 'PKG_BASIC',    '基础体检套餐',   'BASIC',   'ALL',    '18-60', 19900, TRUE, @REF_DATETIME, @REF_DATETIME),
(2, 'PKG_FULL',     '全面型体检套餐', 'FULL',    'ALL',    '25-60', 39900, TRUE, @REF_DATETIME, @REF_DATETIME),
(3, 'PKG_ESSENTIAL','精简型体检套餐', 'BASIC',   'ALL',    '18-45', 29900, TRUE, @REF_DATETIME, @REF_DATETIME),
(4, 'PKG_DEEP',     '深度型体检套餐', 'DEEP',    'ALL',    '35-70', 59900, TRUE, @REF_DATETIME, @REF_DATETIME);

-- 套餐-项目关联
INSERT INTO t_exam_package_item (package_id, item_id, sort_no) VALUES
-- 基础套餐（1）：身高、体重、血压、血常规基础项
(1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5), (1, 6, 6), (1, 7, 7),
-- 全面型套餐（2）：全部基础 + 血脂、肝功、肾功、心电图
(2, 1, 1), (2, 2, 2), (2, 3, 3), (2, 4, 4), (2, 5, 5), (2, 6, 6), (2, 7, 7),
(2, 8, 8), (2, 9, 9), (2, 10, 10), (2, 11, 11), (2, 12, 12), (2, 13, 13), (2, 14, 14),
-- 精简型套餐（3）：基础项 + 血脂
(3, 1, 1), (3, 2, 2), (3, 3, 3), (3, 4, 4), (3, 5, 5), (3, 6, 6), (3, 7, 7), (3, 8, 8), (3, 9, 9),
-- 深度型套餐（4）：全部项目 + 彩超
(4, 1, 1), (4, 2, 2), (4, 3, 3), (4, 4, 4), (4, 5, 5), (4, 6, 6), (4, 7, 7),
(4, 8, 8), (4, 9, 9), (4, 10, 10), (4, 11, 11), (4, 12, 12), (4, 13, 13), (4, 14, 14),
(4, 15, 15), (4, 16, 16);

-- ============================================================
-- 第九步：流程节点
-- ============================================================
INSERT INTO t_flow_node (id, node_code, node_name, node_type, sort_no, required_permission, approval_role_code, enabled, created_at, updated_at) VALUES
(1,  'CHECK_IN',    '前台签到',    'FRONT_DESK', 1,  NULL,                    NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(2,  'VITAL_SIGNS', '生命体征测量', 'NURSE',      2,  'VITAL_SIGNS_RECORD',   NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(3,  'BLOOD_DRAW',  '抽血检验',    'LAB',        3,  'RESULT_RECORD',        NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(4,  'INTERNAL_MED','内科检查',    'DOCTOR',     4,  'RESULT_RECORD',        NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(5,  'SURGERY',     '外科检查',    'DOCTOR',     5,  'RESULT_RECORD',        NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(6,  'ECG',         '心电图检查',  'DOCTOR',     6,  'RESULT_RECORD',        NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(7,  'ULTRASOUND',  '彩超检查',    'DOCTOR',     7,  'RESULT_RECORD',        NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(8,  'ENT',         '耳鼻喉检查',  'DOCTOR',     8,  'RESULT_RECORD',        NULL,          TRUE, @REF_DATETIME, @REF_DATETIME),
(9,  'REVIEW',      '总检审核',    'REVIEW',     9,  'RESULT_REVIEW',        'REVIEW_DOCTOR', TRUE, @REF_DATETIME, @REF_DATETIME),
(10, 'REPORT',      '报告出具',    'REPORT',     10, 'REPORT_GENERATE',      NULL,          TRUE, @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十步：业务规则
-- ============================================================
INSERT INTO t_business_rule (id, rule_code, rule_name, rule_type, rule_content, version_no, enabled, review_status, created_at, updated_at) VALUES
(1, 'FASTING_REQUIRED',      '空腹检查规则',       'EXAM_FLOW', '血压、抽血等检查需空腹进行', 1, TRUE, 'APPROVED', @REF_DATETIME, @REF_DATETIME),
(2, 'ABNORMAL_ALERT',        '异常值预警规则',     'RESULT',    '检查结果超出参考范围时自动标记异常', 1, TRUE, 'APPROVED', @REF_DATETIME, @REF_DATETIME),
(3, 'CRITICAL_REVIEW',       '危急值审核规则',     'RESULT',    '危急值结果需审核医生复核确认', 1, TRUE, 'APPROVED', @REF_DATETIME, @REF_DATETIME),
(4, 'AGE_GENDER_LIMIT',      '年龄性别限制规则',   'PACKAGE',   '根据套餐设定的年龄性别范围校验', 1, TRUE, 'APPROVED', @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十一步：报告模板
-- ============================================================
INSERT INTO t_report_template (template_code, template_name, package_id, template_content, enabled, version_no, review_status, created_at, updated_at) VALUES
('TPL_BASIC', '基础报告模板', 1, 'summary:{summary}; advice:{advice}', TRUE, 1, 'APPROVED', @REF_DATETIME, @REF_DATETIME),
('TPL_FULL',  '全面体检报告模板', 2, 'summary:{summary}; advice:{advice}; detailed_results:{results}', TRUE, 1, 'APPROVED', @REF_DATETIME, @REF_DATETIME),
('TPL_SENIOR','精简体检报告模板', 3, 'summary:{summary}; advice:{advice}', TRUE, 1, 'APPROVED', @REF_DATETIME, @REF_DATETIME),
('TPL_DEEP',  '深度体检报告模板', 4, 'summary:{summary}; advice:{advice}; detailed_results:{results}; recommendations:{recs}', TRUE, 1, 'APPROVED', @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十一步B：号源排班（未来30天，医生2=老李，医生3=张主任）
-- ============================================================
INSERT INTO t_doctor_schedule (doctor_id, branch_id, department_id, schedule_date, time_slot, max_patients, booked_count, enabled, created_at, updated_at)
SELECT
    2 AS doctor_id, 1 AS branch_id, 1 AS department_id,
    @REF_DATE + INTERVAL n DAY AS schedule_date,
    'MORNING', 30, FLOOR(RAND() * 15), 1, @REF_DATETIME, @REF_DATETIME
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
      UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
      UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29) numbers;

INSERT INTO t_doctor_schedule (doctor_id, branch_id, department_id, schedule_date, time_slot, max_patients, booked_count, enabled, created_at, updated_at)
SELECT
    2 AS doctor_id, 1 AS branch_id, 1 AS department_id,
    @REF_DATE + INTERVAL n DAY AS schedule_date,
    'AFTERNOON', 25, FLOOR(RAND() * 12), 1, @REF_DATETIME, @REF_DATETIME
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
      UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
      UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29) numbers;

INSERT INTO t_doctor_schedule (doctor_id, branch_id, department_id, schedule_date, time_slot, max_patients, booked_count, enabled, created_at, updated_at)
SELECT
    3 AS doctor_id, 1 AS branch_id, 3 AS department_id,
    @REF_DATE + INTERVAL n DAY AS schedule_date,
    'MORNING', 20, FLOOR(RAND() * 10), 1, @REF_DATETIME, @REF_DATETIME
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
      UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
      UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29) numbers;

INSERT INTO t_doctor_schedule (doctor_id, branch_id, department_id, schedule_date, time_slot, max_patients, booked_count, enabled, created_at, updated_at)
SELECT
    3 AS doctor_id, 1 AS branch_id, 3 AS department_id,
    @REF_DATE + INTERVAL n DAY AS schedule_date,
    'AFTERNOON', 20, FLOOR(RAND() * 10), 1, @REF_DATETIME, @REF_DATETIME
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
      UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19
      UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29) numbers;

-- ============================================================
-- 第十二步：健康档案
-- ============================================================
INSERT INTO t_health_profile (id, owner_user_id, profile_type, name, id_card_no, phone, allergy_history, medical_history, emergency_contact_name, emergency_contact_phone, deleted, created_at, updated_at) VALUES
(1, 5, 'SELF', '张三', '110101199001011234', '13800000001', NULL,            NULL,             '张母', '13900000001', 0, @REF_DATETIME, @REF_DATETIME),
(2, 6, 'SELF', '李四', '110101199002022345', '13800000002', '青霉素过敏',    '高血压病史2年',  '李父', '13900000002', 0, @REF_DATETIME, @REF_DATETIME),
(3, 7, 'SELF', '王五', '110101199003033456', '13800000003', NULL,            '糖尿病史5年',    '王妻', '13900000003', 0, @REF_DATETIME, @REF_DATETIME),
(4, 8, 'SELF', '赵六', '110101199004044567', '13800000004', '磺胺类药物过敏',NULL,              '赵兄', '13900000004', 0, @REF_DATETIME, @REF_DATETIME),
(5, 9, 'SELF', '孙七', '110101199005055678', '13800000005', NULL,            '高血脂症',        '孙母', '13900000005', 0, @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十三步：家庭成员
-- ============================================================
INSERT INTO t_family_member (id, owner_user_id, name, relationship, id_card_no, phone, deleted, created_at, updated_at) VALUES
(1, 5, '张三父亲', '父子', '110101197001011234', '13700000001', 0, @REF_DATETIME, @REF_DATETIME),
(2, 5, '张三母亲', '母子', '110101197102022345', '13700000002', 0, @REF_DATETIME, @REF_DATETIME),
(3, 6, '李四妻子', '夫妻', '110101199103033456', '13700000003', 0, @REF_DATETIME, @REF_DATETIME),
(4, 6, '李四儿子', '父子', '110101201501011234', '13700000004', 0, @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十四步：预约
-- 张三 -> 全面型套餐(2), 已签到
-- 李四 -> 精简型套餐(3), 已付费
-- 王五 -> 深度型套餐(4), 已付费
-- 赵六 -> 基础套餐(1),   待支付
-- 孙七 -> 全面型套餐(2), 待支付
-- ============================================================
INSERT INTO t_appointment (id, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, appointment_no, deleted, created_at, updated_at) VALUES
(1, 5, 1, 2, 1, @REF_DATE, '08:00-09:00', 'CHECKED_IN', 'APT-DEMO-001', 0, @REF_DATETIME, @REF_DATETIME),
(2, 6, 2, 3, 1, @REF_DATE, '09:00-10:00', 'PAID',        'APT-DEMO-002', 0, @REF_DATETIME, @REF_DATETIME),
(3, 7, 3, 4, 1, @REF_DATE, '10:00-11:00', 'PAID',        'APT-DEMO-003', 0, @REF_DATETIME, @REF_DATETIME),
(4, 8, 4, 1, 1, DATE_ADD(@REF_DATE, INTERVAL 1 DAY), '08:00-09:00', 'CREATED',    'APT-DEMO-004', 0, @REF_DATETIME, @REF_DATETIME),
(5, 9, 5, 2, 1, DATE_ADD(@REF_DATE, INTERVAL 1 DAY), '09:00-10:00', 'CREATED',    'APT-DEMO-005', 0, @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十五步：订单
-- ============================================================
INSERT INTO t_order (id, order_no, appointment_id, user_id, amount_cent, status, created_at, updated_at) VALUES
(1, 'ORD-DEMO-001', 1, 5, 39900, 'PAID',            @REF_DATETIME, @REF_DATETIME),
(2, 'ORD-DEMO-002', 2, 6, 29900, 'PAID',            @REF_DATETIME, @REF_DATETIME),
(3, 'ORD-DEMO-003', 3, 7, 59900, 'PAID',            @REF_DATETIME, @REF_DATETIME),
(4, 'ORD-DEMO-004', 4, 8, 19900, 'PENDING_PAYMENT', @REF_DATETIME, @REF_DATETIME),
(5, 'ORD-DEMO-005', 5, 9, 39900, 'PENDING_PAYMENT', @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十六步：支付记录
-- ============================================================
INSERT INTO t_payment_record (id, order_id, payment_no, channel, amount_cent, status, paid_at, created_at) VALUES
(1, 1, 'PAY-DEMO-001', 'ALIPAY', 39900, 'SUCCESS', @REF_DATETIME, @REF_DATETIME),
(2, 2, 'PAY-DEMO-002', 'WECHAT', 29900, 'SUCCESS', @REF_DATETIME, @REF_DATETIME),
(3, 3, 'PAY-DEMO-003', 'ALIPAY', 59900, 'SUCCESS', @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第十七步：体检记录
--   id=1: 张三 -> 全面型, 进行中 (已签到)
--   id=2: 李四 -> 精简型, 进行中 (已签到)
--   id=3: 王五 -> 深度型, 已签到未开始
-- ============================================================
INSERT INTO t_exam_record (id, user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT 1, 5, 1, 2, p.package_name, 1, 1, 'EXM-DEMO-001', 'IN_PROGRESS', @REF_DATETIME, @REF_DATETIME, @REF_DATETIME
FROM t_exam_package p WHERE p.id = 2;

INSERT INTO t_exam_record (id, user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT 2, 6, 2, 3, p.package_name, 1, 2, 'EXM-DEMO-002', 'IN_PROGRESS', @REF_DATETIME, @REF_DATETIME, @REF_DATETIME
FROM t_exam_package p WHERE p.id = 3;

INSERT INTO t_exam_record (id, user_id, profile_id, package_id, package_name, branch_id, appointment_id, exam_no, status, checked_in_at, created_at, updated_at)
SELECT 3, 7, 3, 4, p.package_name, 1, 3, 'EXM-DEMO-003', 'CHECKED_IN', @REF_DATETIME, @REF_DATETIME, @REF_DATETIME
FROM t_exam_package p WHERE p.id = 4;

-- ============================================================
-- 第十八步：导检单
-- ============================================================
INSERT INTO t_guide_sheet (id, exam_record_id, guide_no, route_text, created_at) VALUES
(1, 1, 'GD-DEMO-001', '1F 一般检查 -> 1F 抽血(空腹) -> 2F 内科 -> 3F 心电图 -> 4F 耳鼻喉科', @REF_DATETIME),
(2, 2, 'GD-DEMO-002', '1F 一般检查 -> 1F 抽血(空腹) -> 2F 内科 -> 2F 外科 -> 3F 心电图', @REF_DATETIME),
(3, 3, 'GD-DEMO-003', '1F 一般检查 -> 1F 抽血(空腹) -> 3F 彩超(空腹) -> 4F 耳鼻喉科', @REF_DATETIME);

-- ============================================================
-- 第十九步：检查进度
--     张三：身高、体重、血压 已完成；抽血检验进行中；其余待检
--     李四：全部待检
--     王五：全部待检
-- ============================================================
INSERT INTO t_exam_progress (id, exam_record_id, item_id, department_id, progress_status, created_at, updated_at) VALUES
-- 张三（exam_record_id=1）已完成的检查
(1,  1, 32, 3, 'COMPLETED', @REF_DATETIME, @REF_DATETIME),
(2,  1, 33, 3, 'COMPLETED', @REF_DATETIME, @REF_DATETIME),
(3,  1, 4,  1, 'COMPLETED', @REF_DATETIME, @REF_DATETIME),
(4,  1, 5,  1, 'COMPLETED', @REF_DATETIME, @REF_DATETIME),
(5,  1, 6,  1, 'COMPLETED', @REF_DATETIME, @REF_DATETIME),
-- 张三进行中的检查
(6,  1, 7,  1, 'IN_PROGRESS', @REF_DATETIME, @REF_DATETIME),
(7,  1, 8,  1, 'IN_PROGRESS', @REF_DATETIME, @REF_DATETIME),
-- 张三待检
(8,  1, 9,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(9,  1, 10, 1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(10, 1, 11, 1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(11, 1, 12, 1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(12, 1, 13, 1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(13, 1, 14, 1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
-- 李四（exam_record_id=2）全部待检
(14, 2, 1,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(15, 2, 2,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(16, 2, 3,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(17, 2, 32, 3, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(18, 2, 33, 3, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(19, 2, 4,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(20, 2, 5,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(21, 2, 6,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME),
(22, 2, 7,  1, 'PENDING', @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第二十步：科室队列
-- ============================================================
INSERT INTO t_department_queue (id, exam_record_id, department_id, queue_no, queue_status, created_at, updated_at) VALUES
(1, 1, 3, 'A001', 'COMPLETED', @REF_DATETIME, @REF_DATETIME),
(2, 1, 1, 'B001', 'WAITING',  @REF_DATETIME, @REF_DATETIME),
(3, 2, 3, 'A002', 'WAITING',  @REF_DATETIME, @REF_DATETIME),
(4, 2, 1, 'B002', 'WAITING',  @REF_DATETIME, @REF_DATETIME),
(5, 3, 3, 'A003', 'WAITING',  @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 第二十一步：检查结果（张三已完成的项目）
-- ============================================================
INSERT INTO t_check_result (id, exam_record_id, item_id, item_name, numeric_value, unit, reference_min, reference_max, abnormal_level, review_status, recorder_id, result_no, created_at, updated_at) VALUES
(1, 1, 32, '血压',   128.00, 'mmHg',  90.00, 140.00, 'NORMAL',   'APPROVED', 2, 'RES-DEMO-001', @REF_DATETIME, @REF_DATETIME),
(2, 1, 33, 'BMI指数', 22.50, 'kg/m²', 18.50, 24.90,  'NORMAL',   'APPROVED', 2, 'RES-DEMO-002', @REF_DATETIME, @REF_DATETIME),
(3, 1, 4,  '白细胞计数', 11.20, '×10⁹/L', 3.50, 9.50, 'ABNORMAL', 'PENDING',  2, 'RES-DEMO-003', @REF_DATETIME, @REF_DATETIME),
(4, 1, 5,  '红细胞计数', 5.10, '×10¹²/L', 4.30, 5.80, 'NORMAL',   'APPROVED', 2, 'RES-DEMO-004', @REF_DATETIME, @REF_DATETIME),
(5, 1, 6,  '血红蛋白', 142.00, 'g/L', 130.00, 175.00, 'NORMAL',   'APPROVED', 2, 'RES-DEMO-005', @REF_DATETIME, @REF_DATETIME);

-- ============================================================
-- 汇总输出
-- ============================================================
SELECT '=== 数据重置完成 ===' AS info;
SELECT CONCAT('用户数: ', COUNT(*)) AS info FROM sys_user;
SELECT CONCAT('角色数: ', COUNT(*)) AS info FROM sys_role;
SELECT CONCAT('分院数: ', COUNT(*)) AS info FROM t_branch;
SELECT CONCAT('科室数: ', COUNT(*)) AS info FROM t_department;
SELECT CONCAT('项目数: ', COUNT(*)) AS info FROM t_exam_item;
SELECT CONCAT('套餐数: ', COUNT(*)) AS info FROM t_exam_package;
SELECT CONCAT('预约数: ', COUNT(*)) AS info FROM t_appointment;
SELECT CONCAT('体检记录数: ', COUNT(*)) AS info FROM t_exam_record;

SELECT '演示账号密码: Passw0rd!' AS info;
SELECT '管理员: 13900000000' AS info
UNION ALL SELECT '护士: 13800000100'
UNION ALL SELECT '体检医生: 13800000200'
UNION ALL SELECT '审核医生: 13800000300'
UNION ALL SELECT '张三(用户): 13800000001'
UNION ALL SELECT '李四(用户): 13800000002'
UNION ALL SELECT '王五(用户): 13800000003'
UNION ALL SELECT '赵六(用户): 13800000004'
UNION ALL SELECT '孙七(用户): 13800000005';
