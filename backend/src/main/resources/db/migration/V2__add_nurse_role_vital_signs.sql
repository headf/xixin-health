-- V2: Add NURSE role, extended permissions, vital signs table

-- 1. Add new permissions
INSERT INTO sys_permission (code, name) VALUES
('RESULT_RECORD_TEXT', '录入文本结果'),
('RESULT_RECORD_BATCH', '批量录入结果'),
('RESULT_QUERY_PENDING', '查询待审结果'),
('REPORT_PUBLISH', '发布最终报告'),
('REPORT_DIAGNOSIS', '撰写诊断建议'),
('EXAM_QUEUE_MANAGE', '管理科室队列'),
('EXAM_PROGRESS_UPDATE', '更新检查进度'),
('VITAL_SIGNS_RECORD', '记录生命体征'),
('EXAM_CHECKIN', '体检签到'),
('EXAM_QUEUE_VIEW', '查看科室队列');

-- 2. Add NURSE role
INSERT INTO sys_role (code, name, role_type, system_built_in, deleted)
VALUES ('NURSE', '护士', 'MEDICAL', TRUE, FALSE);

-- 3. Assign permissions to NURSE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.code = 'NURSE'
  AND p.code IN ('AUTH_LOGIN', 'EXAM_CHECKIN', 'VITAL_SIGNS_RECORD',
                 'EXAM_QUEUE_MANAGE', 'EXAM_QUEUE_VIEW', 'EXAM_PROGRESS_UPDATE');

-- 4. Assign additional permissions to EXISTING roles
-- EXAM_DOCTOR gets text/batch recording + queue view + progress update
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.code = 'EXAM_DOCTOR'
  AND p.code IN ('RESULT_RECORD_TEXT', 'RESULT_RECORD_BATCH',
                 'EXAM_QUEUE_VIEW', 'EXAM_PROGRESS_UPDATE');

-- REVIEW_DOCTOR gets pending query + report publish + diagnosis
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.code = 'REVIEW_DOCTOR'
  AND p.code IN ('RESULT_QUERY_PENDING', 'REPORT_PUBLISH', 'REPORT_DIAGNOSIS');

-- 5. Create vital signs table
CREATE TABLE t_vital_signs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_record_id BIGINT NOT NULL,
    height DECIMAL(5, 1),
    weight DECIMAL(5, 1),
    blood_pressure_systolic INT,
    blood_pressure_diastolic INT,
    heart_rate INT,
    temperature DECIMAL(4, 1),
    vision_left DECIMAL(4, 2),
    vision_right DECIMAL(4, 2),
    waistline DECIMAL(5, 1),
    recorded_by BIGINT NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vital_signs_exam FOREIGN KEY (exam_record_id) REFERENCES t_exam_record (id)
);
