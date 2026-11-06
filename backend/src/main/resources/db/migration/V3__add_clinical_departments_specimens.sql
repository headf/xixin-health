-- V3: Add clinical departments (内科/外科/眼科/耳鼻喉科/口腔科/心电图/彩超),
-- extend exam items, add specimen tracking, add BMI to vital signs,
-- add exam completion check, enhance report for comprehensive audit.

-- ============================================================
-- 1. Add BMI field to vital signs
-- ============================================================
ALTER TABLE t_vital_signs ADD COLUMN bmi DECIMAL(5, 2);

-- ============================================================
-- 2. Add clinical departments (内科/外科/眼科/耳鼻喉科/口腔科/心电图室/彩超室)
-- ============================================================
INSERT INTO t_department (branch_id, department_code, department_name, location, capacity, fasting_required, enabled)
VALUES
(1, 'D003', '内科', '2F-Area C', 15, FALSE, TRUE),
(1, 'D004', '外科', '2F-Area D', 15, FALSE, TRUE),
(1, 'D005', '眼科', '3F-Area A', 10, FALSE, TRUE),
(1, 'D006', '耳鼻喉科', '3F-Area B', 10, FALSE, TRUE),
(1, 'D007', '口腔科', '3F-Area C', 10, FALSE, TRUE),
(1, 'D008', '心电图室', '2F-Area E', 20, FALSE, TRUE),
(1, 'D009', '彩超室', '2F-Area F', 15, FALSE, TRUE);

-- ============================================================
-- 3. Add comprehensive exam items
-- ============================================================
-- Items for Lab (department 1 - 检验科)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_BLOOD_RT', '血常规', 1, NULL, NULL, NULL, NULL, TRUE),
('ITEM_URINE_RT', '尿常规', 1, NULL, NULL, NULL, NULL, TRUE),
('ITEM_LIPID', '血脂', 1, 'mmol/L', NULL, NULL, NULL, TRUE),
('ITEM_CR', '肌酐', 1, 'μmol/L', 44.0000, 133.0000, 400.0000, TRUE),
('ITEM_BUN', '尿素氮', 1, 'mmol/L', 2.9000, 8.2000, NULL, TRUE);

-- Items for Imaging (department 2 - 影像科)
UPDATE t_exam_item SET item_name = '胸部X光' WHERE item_code = 'ITEM_CXR';

-- Items for Internal Medicine (department 3 - 内科)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_HEART_AUSC', '心肺听诊', 3, NULL, NULL, NULL, NULL, TRUE),
('ITEM_ABD_PALP', '腹部触诊', 3, NULL, NULL, NULL, NULL, TRUE),
('ITEM_MED_HISTORY', '病史询问', 3, NULL, NULL, NULL, NULL, TRUE),
('ITEM_MED_CONCLUSION', '内科结论', 3, NULL, NULL, NULL, NULL, TRUE);

-- Items for Surgery (department 4 - 外科)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_SKIN', '皮肤检查', 4, NULL, NULL, NULL, NULL, TRUE),
('ITEM_LYMPH_NODE', '淋巴结', 4, NULL, NULL, NULL, NULL, TRUE),
('ITEM_SPINE_LIMB', '脊柱四肢', 4, NULL, NULL, NULL, NULL, TRUE),
('ITEM_THYROID', '甲状腺触诊', 4, NULL, NULL, NULL, NULL, TRUE),
('ITEM_POSTURE', '体态检查', 4, NULL, NULL, NULL, NULL, TRUE),
('ITEM_SURG_CONCLUSION', '外科结论', 4, NULL, NULL, NULL, NULL, TRUE);

-- Items for Ophthalmology (department 5 - 眼科)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_VISUAL_ACUITY', '视力检查', 5, NULL, NULL, NULL, NULL, TRUE),
('ITEM_FUNDUS', '眼底检查', 5, NULL, NULL, NULL, NULL, TRUE),
('ITEM_EYE_CONCLUSION', '眼科结论', 5, NULL, NULL, NULL, NULL, TRUE);

-- Items for ENT (department 6 - 耳鼻喉科)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_EAR_CANAL', '耳道检查', 6, NULL, NULL, NULL, NULL, TRUE),
('ITEM_NASAL', '鼻腔检查', 6, NULL, NULL, NULL, NULL, TRUE),
('ITEM_THROAT', '咽喉检查', 6, NULL, NULL, NULL, NULL, TRUE),
('ITEM_ENT_CONCLUSION', '耳鼻喉科结论', 6, NULL, NULL, NULL, NULL, TRUE);

-- Items for Dentistry (department 7 - 口腔科)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_TEETH', '牙齿检查', 7, NULL, NULL, NULL, NULL, TRUE),
('ITEM_PERIODONTAL', '牙周检查', 7, NULL, NULL, NULL, NULL, TRUE),
('ITEM_ORAL_MUCOSA', '口腔黏膜', 7, NULL, NULL, NULL, NULL, TRUE),
('ITEM_DENTAL_CONCLUSION', '口腔科结论', 7, NULL, NULL, NULL, NULL, TRUE);

-- Items for ECG (department 8 - 心电图室)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_ECG', '心电图', 8, NULL, NULL, NULL, NULL, TRUE),
('ITEM_ECG_CONCLUSION', '心电图结论', 8, NULL, NULL, NULL, NULL, TRUE);

-- Items for Ultrasound (department 9 - 彩超室)
INSERT INTO t_exam_item (item_code, item_name, department_id, unit, reference_min, reference_max, critical_max, enabled)
VALUES
('ITEM_ABD_US', '腹部彩超', 9, NULL, NULL, NULL, NULL, TRUE),
('ITEM_THYROID_US', '甲状腺彩超', 9, NULL, NULL, NULL, NULL, TRUE),
('ITEM_US_CONCLUSION', '彩超结论', 9, NULL, NULL, NULL, NULL, TRUE);

-- ============================================================
-- 4. Create specimen (blood/urine) tracking table
-- ============================================================
CREATE TABLE t_specimen (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    specimen_no VARCHAR(64) NOT NULL UNIQUE,
    exam_record_id BIGINT NOT NULL,
    specimen_type VARCHAR(32) NOT NULL,  -- BLOOD / URINE
    barcode VARCHAR(128),
    status VARCHAR(32) NOT NULL DEFAULT 'COLLECTED',  -- COLLECTED / LABELED / SENT / ANALYZED
    collected_by BIGINT NOT NULL,
    collected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_to_lab_by BIGINT,
    sent_to_lab_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_specimen_exam FOREIGN KEY (exam_record_id) REFERENCES t_exam_record (id)
);

-- ============================================================
-- 5. Create exam completion check table
-- ============================================================
CREATE TABLE t_exam_completion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_record_id BIGINT NOT NULL UNIQUE,
    all_items_checked BOOLEAN NOT NULL DEFAULT FALSE,
    specimen_collected BOOLEAN NOT NULL DEFAULT FALSE,
    vital_signs_taken BOOLEAN NOT NULL DEFAULT FALSE,
    nurse_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    confirmed_by BIGINT,
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_completion_exam FOREIGN KEY (exam_record_id) REFERENCES t_exam_record (id)
);

-- ============================================================
-- 6. Add department_conclusion field to exam_progress
-- ============================================================
ALTER TABLE t_exam_progress ADD COLUMN department_conclusion VARCHAR(1024);

-- ============================================================
-- 7. Enhance report with comprehensive audit fields
-- ============================================================
ALTER TABLE t_exam_report ADD COLUMN overall_health_assessment TEXT;
ALTER TABLE t_exam_report ADD COLUMN lifestyle_recommendations TEXT;
ALTER TABLE t_exam_report ADD COLUMN follow_up_recommendations TEXT;
ALTER TABLE t_exam_report ADD COLUMN classification VARCHAR(64);  -- NORMAL / PHYSIOLOGICAL / SUB_HEALTH / NEED_RECHECK / NEED_TREATMENT
ALTER TABLE t_exam_report ADD COLUMN signed_at TIMESTAMP;
ALTER TABLE t_exam_report ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_exam_report ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- ============================================================
-- 8. Assign new items to basic health package (package_id=1)
-- ============================================================
INSERT INTO t_exam_package_item (package_id, item_id, sort_no)
SELECT 1, id, (SELECT COALESCE(MAX(sort_no), 3) + 1 FROM t_exam_package_item WHERE package_id = 1)
FROM t_exam_item WHERE item_code IN (
  'ITEM_BLOOD_RT', 'ITEM_URINE_RT', 'ITEM_LIPID', 'ITEM_CR', 'ITEM_BUN',
  'ITEM_HEART_AUSC', 'ITEM_ABD_PALP', 'ITEM_MED_HISTORY', 'ITEM_MED_CONCLUSION',
  'ITEM_SKIN', 'ITEM_LYMPH_NODE', 'ITEM_SPINE_LIMB', 'ITEM_THYROID', 'ITEM_POSTURE', 'ITEM_SURG_CONCLUSION',
  'ITEM_VISUAL_ACUITY', 'ITEM_FUNDUS', 'ITEM_EYE_CONCLUSION',
  'ITEM_EAR_CANAL', 'ITEM_NASAL', 'ITEM_THROAT', 'ITEM_ENT_CONCLUSION',
  'ITEM_TEETH', 'ITEM_PERIODONTAL', 'ITEM_ORAL_MUCOSA', 'ITEM_DENTAL_CONCLUSION',
  'ITEM_ECG', 'ITEM_ECG_CONCLUSION',
  'ITEM_ABD_US', 'ITEM_THYROID_US', 'ITEM_US_CONCLUSION'
);
