# -*- coding: utf-8 -*-
import os
base = r"D:\Desktop\学习课程\Course Design\physical-examination"

def fix_v1():
    p = base + r"\backend\src\main\resources\db\migration\V1__init_schema.sql"
    with open(p, 'r', encoding='utf-8') as f:
        c = f.read()
    for o, n in [
        ("'AUTH_LOGIN', 'Login'", "'AUTH_LOGIN', '登录'"),
        ("'PROFILE_MANAGE_SELF', 'Manage own profile'", "'PROFILE_MANAGE_SELF', '管理个人档案'"),
        ("'APPOINTMENT_MANAGE_SELF', 'Manage own appointments'", "'APPOINTMENT_MANAGE_SELF', '管理个人预约'"),
        ("'RESULT_RECORD', 'Record check results'", "'RESULT_RECORD', '录入检查结果'"),
        ("'REPORT_REVIEW', 'Review reports'", "'REPORT_REVIEW', '审核报告'"),
        ("'FRONT_DESK_CHECKIN', 'Front desk check-in'", "'FRONT_DESK_CHECKIN', '前台签到'"),
        ("'CONFIG_MANAGE', 'Manage base configuration'", "'CONFIG_MANAGE', '管理基础配置'"),
        ("'DEPARTMENT_MANAGE', 'Manage departments'", "'DEPARTMENT_MANAGE', '管理科室'"),
        ("'TEMPLATE_MANAGE', 'Manage report templates'", "'TEMPLATE_MANAGE', '管理报告模板'"),
        ("'FLOW_MANAGE', 'Manage flow nodes'", "'FLOW_MANAGE', '管理流程节点'"),
        ("'IAM_MANAGE', 'Manage accounts and permissions'", "'IAM_MANAGE', '管理账号与权限'"),
        ("'STATISTICS_QUERY', 'Query statistics'", "'STATISTICS_QUERY', '查询统计'"),
        ("'STATISTICS_EXPORT', 'Export statistics'", "'STATISTICS_EXPORT', '导出统计'"),
        ("'AUDIT_QUERY', 'Query audit logs'", "'AUDIT_QUERY', '查询审计日志'"),
        ("'NORMAL_USER', 'Normal User'", "'NORMAL_USER', '普通用户'"),
        ("'EXAM_DOCTOR', 'Exam Doctor'", "'EXAM_DOCTOR', '体检医生'"),
        ("'REVIEW_DOCTOR', 'Review Doctor'", "'REVIEW_DOCTOR', '审核医生'"),
        ("'FRONT_DESK', 'Front Desk'", "'FRONT_DESK', '前台'"),
        ("'OPERATIONS_ADMIN', 'Operations Admin'", "'OPERATIONS_ADMIN', '运营管理员'"),
        ("'SYSTEM_ADMIN', 'System Admin'", "'SYSTEM_ADMIN', '系统管理员'"),
        ("'Xixin Main Branch'", "'熙心总院'"),
        ("'No. 1 Health Road'", "'健康路1号'"),
        ("'D001', 'Laboratory'", "'D001', '检验科'"),
        ("'D002', 'Imaging'", "'D002', '影像科'"),
        ("'Blood Glucose'", "'血糖'"),
        ("'Chest X-Ray'", "'胸部X光'"),
        ("'Basic Health Check'", "'基础健康检查'"),
        ("'ALL', '18-65'", "'全部', '18-65'"),
        ("'Appointment cancel limit'", "'预约取消限制'"),
        ("'Basic Report Template'", "'基础报告模板'"),
        ("'summary:{summary}; advice:{advice}'", "'总结:{summary}; 建议:{advice}'"),
        ("'FLOW_APPOINTMENT', 'Appointment'", "'FLOW_APPOINTMENT', '预约'"),
        ("'FLOW_CHECKIN', 'Front Desk Check-in'", "'FLOW_CHECKIN', '前台签到'"),
        ("'FLOW_REPORT_REVIEW', 'Report Review'", "'FLOW_REPORT_REVIEW', '报告审核'"),
    ]:
        c = c.replace(o, n)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(c)
    print("V1 done")

def fix_v2():
    p = base + r"\backend\src\main\resources\db\migration\V2__add_nurse_role_vital_signs.sql"
    with open(p, 'r', encoding='utf-8') as f:
        c = f.read()
    for o, n in [
        ("'RESULT_RECORD_TEXT', 'Record text-based results'", "'RESULT_RECORD_TEXT', '录入文本结果'"),
        ("'RESULT_RECORD_BATCH', 'Batch record results'", "'RESULT_RECORD_BATCH', '批量录入结果'"),
        ("'RESULT_QUERY_PENDING', 'Query pending review results'", "'RESULT_QUERY_PENDING', '查询待审结果'"),
        ("'REPORT_PUBLISH', 'Publish final report'", "'REPORT_PUBLISH', '发布最终报告'"),
        ("'REPORT_DIAGNOSIS', 'Write diagnosis and advice'", "'REPORT_DIAGNOSIS', '撰写诊断建议'"),
        ("'EXAM_QUEUE_MANAGE', 'Manage department queue'", "'EXAM_QUEUE_MANAGE', '管理科室队列'"),
        ("'EXAM_PROGRESS_UPDATE', 'Update exam progress'", "'EXAM_PROGRESS_UPDATE', '更新检查进度'"),
        ("'VITAL_SIGNS_RECORD', 'Record vital signs'", "'VITAL_SIGNS_RECORD', '记录生命体征'"),
        ("'EXAM_CHECKIN', 'Check in for exam'", "'EXAM_CHECKIN', '体检签到'"),
        ("'EXAM_QUEUE_VIEW', 'View department queue'", "'EXAM_QUEUE_VIEW', '查看科室队列'"),
        ("'NURSE', 'Nurse'", "'NURSE', '护士'"),
    ]:
        c = c.replace(o, n)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(c)
    print("V2 done")

def fix_v3():
    p = base + r"\backend\src\main\resources\db\migration\V3__add_clinical_departments_specimens.sql"
    with open(p, 'r', encoding='utf-8') as f:
        c = f.read()
    for o, n in [
        ("'Internal Medicine (内科)'", "'内科'"), ("'Surgery (外科)'", "'外科'"),
        ("'Ophthalmology (眼科)'", "'眼科'"), ("'ENT (耳鼻喉科)'", "'耳鼻喉科'"),
        ("'Dentistry (口腔科)'", "'口腔科'"), ("'ECG (心电图室)'", "'心电图室'"),
        ("'Ultrasound (彩超室)'", "'彩超室'"),
        ("'Blood Routine (血常规)'", "'血常规'"), ("'Urine Routine (尿常规)'", "'尿常规'"),
        ("'Blood Lipid (血脂)'", "'血脂'"), ("'Creatinine (肌酐)'", "'肌酐'"),
        ("'Blood Urea Nitrogen (尿素氮)'", "'尿素氮'"),
        ("'Heart Auscultation (心肺听诊)'", "'心肺听诊'"),
        ("'Abdominal Palpation (腹部触诊)'", "'腹部触诊'"),
        ("'Medical History Inquiry (病史询问)'", "'病史询问'"),
        ("'Internal Medicine Conclusion (内科结论)'", "'内科结论'"),
        ("'Skin Examination (皮肤检查)'", "'皮肤检查'"), ("'Lymph Node (淋巴结)'", "'淋巴结'"),
        ("'Spine & Limbs (脊柱四肢)'", "'脊柱四肢'"),
        ("'Thyroid Palpation (甲状腺触诊)'", "'甲状腺触诊'"),
        ("'Posture & Body Type (体态检查)'", "'体态检查'"),
        ("'Surgery Conclusion (外科结论)'", "'外科结论'"),
        ("'Visual Acuity (视力检查)'", "'视力检查'"),
        ("'Fundus Examination (眼底检查)'", "'眼底检查'"),
        ("'Ophthalmology Conclusion (眼科结论)'", "'眼科结论'"),
        ("'Ear Canal (耳道检查)'", "'耳道检查'"), ("'Nasal Cavity (鼻腔检查)'", "'鼻腔检查'"),
        ("'Throat & Larynx (咽喉检查)'", "'咽喉检查'"),
        ("'ENT Conclusion (耳鼻喉科结论)'", "'耳鼻喉科结论'"),
        ("'Teeth Examination (牙齿检查)'", "'牙齿检查'"),
        ("'Periodontal (牙周检查)'", "'牙周检查'"),
        ("'Oral Mucosa (口腔黏膜)'", "'口腔黏膜'"),
        ("'Dentistry Conclusion (口腔科结论)'", "'口腔科结论'"),
        ("'Electrocardiogram (心电图)'", "'心电图'"),
        ("'ECG Conclusion (心电图结论)'", "'心电图结论'"),
        ("'Abdominal Ultrasound (腹部彩超)'", "'腹部彩超'"),
        ("'Thyroid Ultrasound (甲状腺彩超)'", "'甲状腺彩超'"),
        ("'Ultrasound Conclusion (彩超结论)'", "'彩超结论'"),
        ("SET item_name = 'Chest X-Ray (胸部X光)'", "SET item_name = '胸部X光'"),
    ]:
        c = c.replace(o, n)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(c)
    print("V3 done")

fix_v1()
fix_v2()
fix_v3()
print("全部OK!")
