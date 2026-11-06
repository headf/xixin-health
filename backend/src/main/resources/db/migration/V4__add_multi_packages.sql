-- V4: Add multiple exam packages, update seed data for package-based exams

-- ============================================================
-- 1. Add new packages with different department sets
-- ============================================================
INSERT INTO t_exam_package (package_code, package_name, category, gender_limit, age_range, price_cent, enabled)
VALUES
('PKG_BASIC_PLUS', '套餐一（基础全面型）', 'BASIC', 'ALL', '18-65', 49900, TRUE),
('PKG_LITE',       '套餐二（基础精简型）', 'LITE', 'ALL', '18-65', 19900, TRUE),
('PKG_FULL',       '套餐三（深度体检型）', 'FULL', 'ALL', '18-65', 99900, TRUE);

-- ============================================================
-- 2. Assign items to each package
-- ============================================================

-- 套餐一（基础全面型）package_id=2: 检验科(glu,alt,血常规,尿常规) + 内科 + 外科 + 心电图
INSERT INTO t_exam_package_item (package_id, item_id, sort_no)
SELECT 2, id, ROW_NUMBER() OVER (ORDER BY id)
FROM t_exam_item WHERE item_code IN (
  'ITEM_GLU', 'ITEM_ALT', 'ITEM_BLOOD_RT', 'ITEM_URINE_RT',
  'ITEM_HEART_AUSC', 'ITEM_ABD_PALP', 'ITEM_MED_HISTORY', 'ITEM_MED_CONCLUSION',
  'ITEM_SKIN', 'ITEM_LYMPH_NODE', 'ITEM_SPINE_LIMB', 'ITEM_THYROID', 'ITEM_POSTURE', 'ITEM_SURG_CONCLUSION',
  'ITEM_ECG', 'ITEM_ECG_CONCLUSION'
);

-- 套餐二（基础精简型）package_id=3: 检验科(glu,alt) + 影像科(cxr)
INSERT INTO t_exam_package_item (package_id, item_id, sort_no)
SELECT 3, id, ROW_NUMBER() OVER (ORDER BY id)
FROM t_exam_item WHERE item_code IN (
  'ITEM_GLU', 'ITEM_ALT', 'ITEM_CXR'
);

-- 套餐三（深度体检型）package_id=4: 全部科室的所有项目
INSERT INTO t_exam_package_item (package_id, item_id, sort_no)
SELECT 4, id, ROW_NUMBER() OVER (ORDER BY id)
FROM t_exam_item WHERE enabled = TRUE;

-- ============================================================
-- 3. Update existing patients to have different packages
--    张三 -> 套餐一(全面型)  id=2
--    李四 -> 套餐二(精简型)  id=3
--    王五 -> 套餐三(深度型)  id=4
--    赵六 -> 基础套餐(原)    id=1
--    孙七 -> 套餐一(全面型)  id=2
-- ============================================================
-- Note: This is for fresh installs. Existing data is handled by the seed.
-- The V1 seed already created appointments with package_id=1 for all patients.
-- For V4 we create NEW appointments with different packages via DataSeeder updates.
-- The V3 package-item mappings already added items to the basic package (id=1).

-- ============================================================
-- 4. Add package reference to exam record
-- ============================================================
ALTER TABLE t_exam_record ADD COLUMN package_id BIGINT;
ALTER TABLE t_exam_record ADD COLUMN package_name VARCHAR(128);
