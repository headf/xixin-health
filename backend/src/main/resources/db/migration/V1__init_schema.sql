CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL
);

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    role_type VARCHAR(32) NOT NULL,
    system_built_in BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
);

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(128) UNIQUE,
    display_name VARCHAR(64) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
);

CREATE TABLE t_health_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    profile_type VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    id_card_no VARCHAR(256),
    phone VARCHAR(256),
    allergy_history VARCHAR(512),
    medical_history VARCHAR(512),
    emergency_contact_name VARCHAR(64),
    emergency_contact_phone VARCHAR(256),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE t_family_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    relationship VARCHAR(32) NOT NULL,
    id_card_no VARCHAR(256),
    phone VARCHAR(256),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE t_branch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(64) NOT NULL UNIQUE,
    branch_name VARCHAR(128) NOT NULL,
    address VARCHAR(256),
    service_phone VARCHAR(32),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    department_code VARCHAR(64) NOT NULL,
    department_name VARCHAR(128) NOT NULL,
    location VARCHAR(128),
    capacity INT NOT NULL DEFAULT 0,
    fasting_required BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_exam_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code VARCHAR(64) NOT NULL UNIQUE,
    item_name VARCHAR(128) NOT NULL,
    department_id BIGINT,
    unit VARCHAR(32),
    reference_min DECIMAL(12, 4),
    reference_max DECIMAL(12, 4),
    critical_max DECIMAL(12, 4),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_exam_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_code VARCHAR(64) NOT NULL UNIQUE,
    package_name VARCHAR(128) NOT NULL,
    category VARCHAR(64),
    gender_limit VARCHAR(16),
    age_range VARCHAR(64),
    price_cent INT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_exam_package_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_package_item_package FOREIGN KEY (package_id) REFERENCES t_exam_package (id),
    CONSTRAINT fk_package_item_item FOREIGN KEY (item_id) REFERENCES t_exam_item (id)
);

CREATE TABLE t_business_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL UNIQUE,
    rule_name VARCHAR(128) NOT NULL,
    rule_type VARCHAR(64) NOT NULL,
    rule_content TEXT NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_report_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(64) NOT NULL UNIQUE,
    template_name VARCHAR(128) NOT NULL,
    package_id BIGINT,
    template_content TEXT NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_flow_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_code VARCHAR(64) NOT NULL UNIQUE,
    node_name VARCHAR(128) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    required_permission VARCHAR(128),
    approval_role_code VARCHAR(64),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    package_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    appointment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount_cent INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_no VARCHAR(64) NOT NULL UNIQUE,
    channel VARCHAR(32) NOT NULL,
    amount_cent INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_refund_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    refund_no VARCHAR(64) NOT NULL UNIQUE,
    amount_cent INT NOT NULL,
    reason VARCHAR(256),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_exam_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_no VARCHAR(64) NOT NULL UNIQUE,
    appointment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    checked_in_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_guide_sheet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_record_id BIGINT NOT NULL,
    guide_no VARCHAR(64) NOT NULL UNIQUE,
    route_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_department_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_record_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    queue_no VARCHAR(32) NOT NULL,
    queue_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_exam_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_record_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    department_id BIGINT,
    progress_status VARCHAR(32) NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_check_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    result_no VARCHAR(64) NOT NULL UNIQUE,
    exam_record_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    unit VARCHAR(32),
    numeric_value DECIMAL(12, 4),
    text_value VARCHAR(512),
    reference_min DECIMAL(12, 4),
    reference_max DECIMAL(12, 4),
    critical_max DECIMAL(12, 4),
    abnormal_level VARCHAR(32) NOT NULL,
    review_status VARCHAR(32) NOT NULL,
    recorder_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_result_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    result_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    approved BOOLEAN NOT NULL,
    review_comment VARCHAR(512),
    reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_exam_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_no VARCHAR(64) NOT NULL UNIQUE,
    exam_record_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    template_id BIGINT,
    summary TEXT,
    doctor_advice TEXT,
    abnormal_status VARCHAR(32) NOT NULL,
    report_status VARCHAR(32) NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewer_id BIGINT,
    published_at TIMESTAMP,
    file_url VARCHAR(512)
);

CREATE TABLE t_report_history_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_report_id BIGINT NOT NULL,
    history_report_id BIGINT NOT NULL,
    compare_note VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_health_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    abnormal_count INT NOT NULL DEFAULT 0,
    critical_count INT NOT NULL DEFAULT 0,
    trend_data TEXT,
    risk_level VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_health_management (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_id BIGINT,
    risk_level VARCHAR(32) NOT NULL,
    management_status VARCHAR(32) NOT NULL,
    follow_up_plan VARCHAR(512),
    intervention_plan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(64) NOT NULL,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(512) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE TABLE t_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_time TIMESTAMP NOT NULL,
    operator_id VARCHAR(64),
    operation_type VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64),
    result VARCHAR(32) NOT NULL,
    ip_address VARCHAR(64),
    user_agent VARCHAR(256),
    trace_id VARCHAR(128)
);

INSERT INTO sys_permission (code, name) VALUES
('AUTH_LOGIN', '登录'),
('PROFILE_MANAGE_SELF', '管理个人档案'),
('APPOINTMENT_MANAGE_SELF', '管理个人预约'),
('RESULT_RECORD', '录入检查结果'),
('REPORT_REVIEW', '审核报告'),
('FRONT_DESK_CHECKIN', '前台签到'),
('CONFIG_MANAGE', '管理基础配置'),
('DEPARTMENT_MANAGE', '管理科室'),
('TEMPLATE_MANAGE', '管理报告模板'),
('FLOW_MANAGE', '管理流程节点'),
('IAM_MANAGE', '管理账号与权限'),
('STATISTICS_QUERY', '查询统计'),
('STATISTICS_EXPORT', '导出统计'),
('AUDIT_QUERY', '查询审计日志');

INSERT INTO sys_role (code, name, role_type, system_built_in, deleted) VALUES
('NORMAL_USER', '普通用户', 'USER', TRUE, FALSE),
('EXAM_DOCTOR', '体检医生', 'MEDICAL', TRUE, FALSE),
('REVIEW_DOCTOR', '审核医生', 'MEDICAL', TRUE, FALSE),
('FRONT_DESK', '前台', 'BUSINESS', TRUE, FALSE),
('OPERATIONS_ADMIN', '运营管理员', 'ADMIN', TRUE, FALSE),
('SYSTEM_ADMIN', '系统管理员', 'ADMIN', TRUE, FALSE);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE (r.code = 'NORMAL_USER' AND p.code IN ('AUTH_LOGIN', 'PROFILE_MANAGE_SELF', 'APPOINTMENT_MANAGE_SELF'))
   OR (r.code = 'EXAM_DOCTOR' AND p.code IN ('AUTH_LOGIN', 'RESULT_RECORD'))
   OR (r.code = 'REVIEW_DOCTOR' AND p.code IN ('AUTH_LOGIN', 'REPORT_REVIEW'))
   OR (r.code = 'FRONT_DESK' AND p.code IN ('AUTH_LOGIN', 'FRONT_DESK_CHECKIN'))
   OR (r.code = 'OPERATIONS_ADMIN' AND p.code IN (
        'AUTH_LOGIN',
        'CONFIG_MANAGE',
        'DEPARTMENT_MANAGE',
        'TEMPLATE_MANAGE',
        'FLOW_MANAGE',
        'STATISTICS_QUERY',
        'STATISTICS_EXPORT',
        'AUDIT_QUERY'
   ))
   OR (r.code = 'SYSTEM_ADMIN');

INSERT INTO t_branch (branch_code, branch_name, address, service_phone, enabled)
VALUES ('BR001', '熙心总院', '健康路1号', '400-100-0001', TRUE);

INSERT INTO t_department (branch_id, department_code, department_name, location, capacity, fasting_required, enabled)
VALUES
(1, 'D001', '检验科', '2F-Area A', 30, TRUE, TRUE),
(1, 'D002', '影像科', '3F-Area B', 20, FALSE, TRUE);

INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_GLU', '血糖', 1, 'mmol/L', 3.9000, 6.1000, 11.1000, TRUE),
('ITEM_ALT', '谷丙转氨酶', 1, 'U/L', 0.0000, 40.0000, 400.0000, TRUE),
('ITEM_CXR', '胸部X光', 2, NULL, NULL, NULL, NULL, TRUE);

INSERT INTO t_exam_package (package_code, package_name, category, gender_limit, age_range, price_cent, enabled)
VALUES ('PKG_BASIC', '基础健康检查', 'BASIC', '全部', '18-65', 29900, TRUE);

INSERT INTO t_exam_package_item (package_id, item_id, sort_no)
VALUES (1, 1, 1), (1, 2, 2), (1, 3, 3);

INSERT INTO t_business_rule (rule_code, rule_name, rule_type, rule_content, version_no, enabled)
VALUES ('APPOINTMENT_CANCEL_LIMIT', '预约取消限制', 'APPOINTMENT', '{"minHoursBeforeExam":24}', 1, TRUE);

INSERT INTO t_report_template (template_code, template_name, package_id, template_content, version_no, enabled)
VALUES ('TPL_BASIC', '基础报告模板', 1, '总结:{summary}; 建议:{advice}', 1, TRUE);

INSERT INTO t_flow_node (node_code, node_name, node_type, sort_no, required_permission, approval_role_code, enabled)
VALUES
('FLOW_APPOINTMENT', '预约', 'BUSINESS', 1, 'APPOINTMENT_MANAGE_SELF', NULL, TRUE),
('FLOW_CHECKIN', '前台签到', 'BUSINESS', 2, 'FRONT_DESK_CHECKIN', 'FRONT_DESK', TRUE),
('FLOW_REPORT_REVIEW', '报告审核', 'APPROVAL', 3, 'REPORT_REVIEW', 'REVIEW_DOCTOR', TRUE);
