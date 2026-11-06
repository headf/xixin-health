-- Test seed data for H2 (MySQL mode)

-- Permissions
INSERT INTO sys_permission (code, name) VALUES ('AUTH_LOGIN', 'Login');
INSERT INTO sys_permission (code, name) VALUES ('CONFIG_MANAGE', 'Manage config');
INSERT INTO sys_permission (code, name) VALUES ('FRONT_DESK_CHECKIN', 'Front desk checkin');
INSERT INTO sys_permission (code, name) VALUES ('REPORT_REVIEW', 'Report review');
INSERT INTO sys_permission (code, name) VALUES ('RESULT_RECORD', 'Result record');
INSERT INTO sys_permission (code, name) VALUES ('DEPARTMENT_MANAGE', 'Dept manage');
INSERT INTO sys_permission (code, name) VALUES ('FLOW_MANAGE', 'Flow manage');
INSERT INTO sys_permission (code, name) VALUES ('IAM_MANAGE', 'IAM manage');
INSERT INTO sys_permission (code, name) VALUES ('TEMPLATE_MANAGE', 'Template manage');
INSERT INTO sys_permission (code, name) VALUES ('AUDIT_QUERY', 'Audit query');
INSERT INTO sys_permission (code, name) VALUES ('STATISTICS_QUERY', 'Stats query');
INSERT INTO sys_permission (code, name) VALUES ('STATISTICS_EXPORT', 'Stats export');

-- Roles
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES ('SYSTEM_ADMIN', 'System Admin', 'ADMIN', TRUE, FALSE);
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES ('EXAM_DOCTOR', 'Exam Doctor', 'MEDICAL', TRUE, FALSE);
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES ('REVIEW_DOCTOR', 'Review Doctor', 'MEDICAL', TRUE, FALSE);
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES ('FRONT_DESK', 'Front Desk', 'BUSINESS', TRUE, FALSE);
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES ('NURSE', 'Nurse', 'MEDICAL', TRUE, FALSE);
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES ('OPERATIONS_ADMIN', 'Ops Admin', 'ADMIN', TRUE, FALSE);
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES ('NORMAL_USER', 'Normal User', 'USER', TRUE, FALSE);

-- Role-permission (SYSTEM_ADMIN gets all)
INSERT INTO sys_role_permission (role_id, permission_id) SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.code = 'SYSTEM_ADMIN';

-- Users
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled, deleted, created_at, updated_at) VALUES ('13800000001', 'u1@t.com', 'User1', 'hash', TRUE, FALSE, NOW(), NOW());
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled, deleted, created_at, updated_at) VALUES ('13800000200', 'doc@t.com', 'Doctor', 'hash', TRUE, FALSE, NOW(), NOW());
INSERT INTO sys_user (phone, email, display_name, password_hash, enabled, deleted, created_at, updated_at) VALUES ('13800000300', 'rev@t.com', 'Reviewer', 'hash', TRUE, FALSE, NOW(), NOW());

-- User-role
INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.phone = '13800000001' AND r.code = 'FRONT_DESK';
INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.phone = '13800000200' AND r.code = 'EXAM_DOCTOR';
INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.phone = '13800000300' AND r.code = 'REVIEW_DOCTOR';

-- Branch
INSERT INTO t_branch (branch_code, branch_name, address, service_phone, enabled, created_at, updated_at) VALUES ('BR001', 'Main Branch', 'Addr', 'Tel', TRUE, NOW(), NOW());

-- Departments
INSERT INTO t_department (branch_id, department_code, department_name, location, capacity, fasting_required, enabled, created_at, updated_at) VALUES (1, 'D001', 'Lab', '2F-A', 30, TRUE, TRUE, NOW(), NOW());
INSERT INTO t_department (branch_id, department_code, department_name, location, capacity, fasting_required, enabled, created_at, updated_at) VALUES (1, 'D002', 'Imaging', '3F-B', 20, FALSE, TRUE, NOW(), NOW());

-- Exam items
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled, created_at, updated_at) VALUES ('ITEM_GLU', '血糖', 1, 'mmol/L', 3.9000, 6.1000, 11.1000, TRUE, NOW(), NOW());
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled, created_at, updated_at) VALUES ('ITEM_ALT', '谷丙转氨酶', 1, 'U/L', 0.0000, 40.0000, 400.0000, TRUE, NOW(), NOW());
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled, created_at, updated_at) VALUES ('ITEM_CXR', '胸部X光', 2, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW());

-- Package
INSERT INTO t_exam_package (package_code, package_name, category, gender_limit, age_range, price_cent, enabled, created_at, updated_at) VALUES ('PKG_BASIC', 'Basic Package', 'BASIC', 'ALL', '18-65', 29900, TRUE, NOW(), NOW());

-- Package-item
INSERT INTO t_exam_package_item (package_id, item_id, sort_no) VALUES (1, 1, 1);
INSERT INTO t_exam_package_item (package_id, item_id, sort_no) VALUES (1, 2, 2);
INSERT INTO t_exam_package_item (package_id, item_id, sort_no) VALUES (1, 3, 3);

-- Business rule
INSERT INTO t_business_rule (rule_code, rule_name, rule_type, rule_content, version_no, enabled, review_status, created_at, updated_at) VALUES ('APPOINTMENT_CANCEL_LIMIT', 'Cancel Limit', 'APPOINTMENT', '{"minHoursBeforeExam":24}', 1, TRUE, 'APPROVED', NOW(), NOW());

-- Report template
INSERT INTO t_report_template (template_code, template_name, package_id, template_content, enabled, review_status, version_no, created_at, updated_at) VALUES ('TPL_BASIC', 'Basic Template', 1, 'summary:{summary}; advice:{advice}', TRUE, 'APPROVED', 1, NOW(), NOW());

-- Health profile
INSERT INTO t_health_profile (owner_user_id, profile_type, name, id_card_no, phone, allergy_history, medical_history, emergency_contact_name, emergency_contact_phone, deleted, created_at, updated_at) VALUES (1, 'SELF', 'User1', '110101199001011234', '13800000001', NULL, NULL, 'Emerg', '13900000001', FALSE, NOW(), NOW());

-- Appointment (PAID, today)
INSERT INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted) VALUES ('APPT001', 1, 1, 1, 1, CURRENT_DATE, 'MORNING', 'PAID', NOW(), NOW(), FALSE);
