-- 创建各科室体检医生角色
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted)
SELECT * FROM (VALUES
    ('EXAM_DOCTOR_LAB',        '检验科医生', 'MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_IMAGING',    '影像科医生', 'MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_INTERNAL',   '内科医生',   'MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_SURGERY',    '外科医生',   'MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_OPHTHALM',   '眼科医生',   'MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_ENT',        '耳鼻喉科医生','MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_DENTAL',     '口腔科医生', 'MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_ECG',        '心电图医生', 'MEDICAL', TRUE, FALSE),
    ('EXAM_DOCTOR_ULTRASOUND', '彩超医生',   'MEDICAL', TRUE, FALSE)
) AS t(code, name, role_type, system_built_in, deleted)
WHERE NOT EXISTS (SELECT 1 FROM sys_role r WHERE r.code = t.code);

-- 给新角色分配权限（与EXAM_DOCTOR相同）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.code LIKE 'EXAM_DOCTOR_%'
  AND p.code IN ('AUTH_LOGIN', 'RESULT_RECORD')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 为每个科室创建医生账号（密码 Passw0rd!）
-- 检验科医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001001', NULL, '检验科-李医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001001');

-- 影像科医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001002', NULL, '影像科-王医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001002');

-- 内科医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001003', NULL, '内科-张医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001003');

-- 外科医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001004', NULL, '外科-赵医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001004');

-- 眼科医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001005', NULL, '眼科-刘医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001005');

-- 耳鼻喉科医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001006', NULL, '耳鼻喉科-陈医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001006');

-- 口腔科医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001007', NULL, '口腔科-周医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001007');

-- 心电图医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001008', NULL, '心电图-吴医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001008');

-- 彩超医生
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled)
SELECT '13800001009', NULL, '彩超-郑医生', '$2a$10$8YLQTXWs.aEM71iGPu2vOOXmrjnHD8.mUW9v6qQlQjRennt0WwHp6', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE phone = '13800001009');

-- 分配角色给医生
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001001' AND r.code = 'EXAM_DOCTOR_LAB'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001002' AND r.code = 'EXAM_DOCTOR_IMAGING'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001003' AND r.code = 'EXAM_DOCTOR_INTERNAL'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001004' AND r.code = 'EXAM_DOCTOR_SURGERY'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001005' AND r.code = 'EXAM_DOCTOR_OPHTHALM'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001006' AND r.code = 'EXAM_DOCTOR_ENT'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001007' AND r.code = 'EXAM_DOCTOR_DENTAL'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001008' AND r.code = 'EXAM_DOCTOR_ECG'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.phone = '13800001009' AND r.code = 'EXAM_DOCTOR_ULTRASOUND'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
