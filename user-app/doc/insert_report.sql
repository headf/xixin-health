-- 先检查有没有体检记录，如果没有则用预约创建一条
INSERT INTO t_exam_record (exam_no, appointment_id, user_id, profile_id, branch_id, status, created_at, updated_at)
SELECT
    CONCAT('EX', UNIX_TIMESTAMP(NOW())),  -- exam_no
    a.id,                                 -- appointment_id
    a.user_id,                            -- user_id
    a.profile_id,                         -- profile_id
    a.branch_id,                          -- branch_id
    'COMPLETED',                          -- status: 已完成
    NOW(),                                -- created_at
    NOW()                                 -- updated_at
FROM t_appointment a
WHERE a.user_id = 9
  AND NOT EXISTS (SELECT 1 FROM t_exam_record r WHERE r.appointment_id = a.id)
LIMIT 1;

-- 插入体检报告
INSERT INTO t_exam_report (
    report_no,
    exam_record_id,
    user_id,
    abnormal_status,
    report_status,
    summary,
    doctor_advice,
    generated_at,
    published_at,
    overall_health_assessment,
    lifestyle_recommendations,
    follow_up_recommendations,
    classification,
    created_at,
    updated_at
)
SELECT
    CONCAT('RPT', UNIX_TIMESTAMP(NOW())),                    -- report_no
    r.id,                                                    -- exam_record_id
    r.user_id,                                               -- user_id
    'NORMAL',                                                -- abnormal_status (正常)
    'PUBLISHED',                                             -- report_status (已发布)
    '各检查指标均在正常范围内，身体状态良好。',               -- summary
    '建议保持良好作息习惯，均衡饮食，适量运动，定期体检。',    -- doctor_advice
    NOW(),                                                   -- generated_at
    NOW(),                                                   -- published_at
    '总体健康状况良好，未见明显异常指标。',                   -- overall_health_assessment
    '1. 建议保持规律作息，每天睡眠7-8小时；\n2. 均衡饮食，多吃蔬菜水果；\n3. 每周进行3-5次适量有氧运动。',  -- lifestyle_recommendations
    '建议一年后复查。',                                       -- follow_up_recommendations
    'NORMAL',                                                -- classification (正常)
    NOW(),                                                   -- created_at
    NOW()                                                    -- updated_at
FROM t_exam_record r
WHERE r.user_id = 9
ORDER BY r.created_at DESC
LIMIT 1;
