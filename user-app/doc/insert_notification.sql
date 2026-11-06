-- 插入消息通知（user_id = 9）
INSERT INTO t_notification (user_id, notification_type, title, content, read_flag, created_at)
VALUES (9, 'APPOINTMENT_REMINDER', '体检预约提醒', '您预约的2026-06-25体检即将开始，请于当日上午按时到达体检分院。', false, NOW()),

(9, 'REPORT_READY', '体检报告已发布', '您的体检报告已生成并发布，点击查看详细检查结果。', false, NOW()),

(9, 'APPOINTMENT_REMINDER', '预约成功通知', '您已成功预约基础体检套餐，体检日期：2026-06-25 上午，请按时前往。', false, DATE_SUB(NOW(), INTERVAL 2 DAY)),

(9, 'SYSTEM', '系统通知', '欢迎使用熙心健康体检管理系统，您可以在这里管理预约、查看报告。', true, DATE_SUB(NOW(), INTERVAL 7 DAY));
