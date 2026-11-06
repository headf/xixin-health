-- ============================================================
-- 熙心健康体检管理系统 - 数据库编码修复脚本
-- 修复所有 GBK 双编码导致的乱码数据
-- 适用：MySQL 8.0+
-- ============================================================

-- 1. 修复分院地址
UPDATE t_branch SET address = '北京市海淀区中关村大街1号' WHERE id = 1;

-- 2. 修复总院科室位置
UPDATE t_department SET location = '1楼A区' WHERE id = 1;
UPDATE t_department SET location = '1楼B区' WHERE id = 2;
UPDATE t_department SET location = '2楼A区' WHERE id = 3;
UPDATE t_department SET location = '2楼B区' WHERE id = 4;
UPDATE t_department SET location = '3楼A区' WHERE id = 5;
UPDATE t_department SET location = '3楼B区' WHERE id = 6;
UPDATE t_department SET location = '4楼A区' WHERE id = 7;
UPDATE t_department SET location = '4楼B区' WHERE id = 8;

-- 3. 修复套餐分类
UPDATE t_exam_package SET category = '基础' WHERE id IN (1, 3);
UPDATE t_exam_package SET category = '全面' WHERE id = 2;
UPDATE t_exam_package SET category = '深度' WHERE id = 4;

-- 4. 修复业务规则
UPDATE t_business_rule SET rule_name = '空腹检查规则', rule_content = '血压、抽血等检查需空腹进行' WHERE id = 1;
UPDATE t_business_rule SET rule_name = '异常值预警规则', rule_content = '检查结果超出参考范围时自动标记异常' WHERE id = 2;
UPDATE t_business_rule SET rule_name = '危急值审核规则', rule_content = '危急值结果需审核医生复核确认' WHERE id = 3;
UPDATE t_business_rule SET rule_name = '年龄性别限制规则', rule_content = '根据套餐设定的年龄性别范围校验' WHERE id = 4;
