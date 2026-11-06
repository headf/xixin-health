import pymysql

conn = pymysql.connect(host="localhost", user="root", password="123456", database="xixin_health", charset="utf8mb4")
cur = conn.cursor(pymysql.cursors.DictCursor)

# Stats
cur.execute("SELECT COUNT(*) AS cnt FROM sys_user"); total_users = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(DISTINCT u.id) AS cnt FROM sys_user u JOIN sys_user_role ur ON u.id=ur.user_id JOIN sys_role r ON ur.role_id=r.id WHERE r.code='NORMAL_USER'")
normal_users = cur.fetchone()["cnt"]
staff_count = total_users - normal_users
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_report"); reports = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_appointment WHERE NOT deleted"); appointments = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_record"); exam_records = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result"); total_results = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result WHERE abnormal_level IN ('ABNORMAL','CRITICAL')"); abnormal_results = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result WHERE abnormal_level='CRITICAL'"); critical_results = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_health_management"); mgmt_count = cur.fetchone()["cnt"]
cur.execute("SELECT risk_level, COUNT(*) AS cnt FROM t_health_management GROUP BY risk_level")
risk_dist = {r["risk_level"]: r["cnt"] for r in cur.fetchall()}
cur.execute("SELECT COUNT(*) AS cnt FROM t_critical_alert"); alert_count = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_department WHERE enabled=TRUE"); departments = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_package WHERE enabled=TRUE"); packages = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_item WHERE enabled=TRUE"); exam_items = cur.fetchone()["cnt"]

cur.execute("""
SELECT u.id, u.display_name, u.phone, u.id_card,
  (SELECT COUNT(*) FROM t_exam_report WHERE user_id=u.id) AS rpt_cnt,
  (SELECT COUNT(*) FROM t_appointment WHERE user_id=u.id AND NOT deleted) AS appt_cnt,
  (SELECT COUNT(*) FROM t_check_result cr JOIN t_exam_record er ON cr.exam_record_id=er.id WHERE er.user_id=u.id) AS res_cnt,
  (SELECT COUNT(*) FROM t_check_result cr JOIN t_exam_record er ON cr.exam_record_id=er.id WHERE er.user_id=u.id AND cr.abnormal_level IN ('ABNORMAL','CRITICAL')) AS abn_cnt,
  (SELECT GROUP_CONCAT(DISTINCT r.code) FROM sys_user_role ur JOIN sys_role r ON ur.role_id=r.id WHERE ur.user_id=u.id) AS roles
FROM sys_user u ORDER BY u.id
""")
user_rows = cur.fetchall()
conn.close()

def get_gender(id_card):
    if id_card and len(id_card) >= 18:
        try: return '男' if int(id_card[16]) % 2 == 1 else '女'
        except: pass
    return '未知'

def get_type(roles):
    if not roles: return '医护'
    codes = roles.split(',')
    if len(codes) == 1 and codes[0] == 'NORMAL_USER': return '用户'
    return '医护'

rows = ""
for r in user_rows:
    utype = get_type(r["roles"])
    tc = "tag-green" if utype == "用户" else "tag-blue"
    res_cnt = r["res_cnt"] or 0
    abn_cnt = r["abn_cnt"] or 0
    rst = f"总{res_cnt}" + (f" 异常{abn_cnt}" if abn_cnt > 0 else "")
    if res_cnt == 0 and (r["appt_cnt"] or 0) == 0:
        rst = "-"
    rows += f"<tr><td>{r['id']}</td><td>{r['display_name'] or '未知'}</td><td>{r['phone'] or '-'}</td><td>{get_gender(r['id_card'])}</td><td><span class=\"tag {tc}\">{utype}</span></td><td>{r['rpt_cnt'] or 0}</td><td>{r['appt_cnt'] or 0}</td><td>{rst}</td></tr>"

html = """<!DOCTYPE html><html><head><meta charset="UTF-8"><title>熙心健康 - 用户数据总览</title>
<style>
@page{margin:2cm} body{font-family:'微软雅黑','SimSun',sans-serif;font-size:12pt;color:#333;line-height:1.6}
h1{text-align:center;font-size:28pt;color:#00B894;margin-top:80px;margin-bottom:10px}
.subtitle{text-align:center;font-size:14pt;color:#666;margin-bottom:10px}
.meta{text-align:center;font-size:11pt;color:#999;margin-bottom:40px}
.page-break{page-break-before:always}
h2{font-size:18pt;color:#00B894;border-bottom:2px solid #00B894;padding-bottom:6px;margin-top:40px}
table{width:100%;border-collapse:collapse;margin:12px 0;font-size:10pt}
th{background:#00B894;color:#fff;padding:8px 10px;text-align:left;font-weight:bold}
td{padding:6px 10px;border:1px solid #e0e0e0}
tr:nth-child(even){background:#f5faf8}
.tag{display:inline-block;padding:2px 10px;border-radius:4px;font-size:9pt;font-weight:bold}
.tag-green{background:#d4edda;color:#155724} .tag-blue{background:#d1ecf1;color:#0c5460}
.tag-red{background:#f8d7da;color:#721c24}
.stats-grid{display:flex;flex-wrap:wrap;justify-content:center;gap:16px;margin:40px 0 60px}
.stat-item{text-align:center;padding:16px 24px;border-radius:12px;min-width:120px}
.stat-num{font-size:28pt;font-weight:bold} .stat-label{font-size:9pt;margin-top:4px;opacity:0.8}
</style></head><body>
<h1>熙心健康体检中心</h1>
<div class="subtitle">用户数据总览报告</div>
<div class="meta">生成日期：2026/6/30</div>
<div class="stats-grid">
<div class="stat-item" style="background:#f0fdf4;"><div class="stat-num" style="color:#00B894;">""" + str(total_users) + """</div><div class="stat-label">注册用户</div></div>
<div class="stat-item" style="background:#f0f8ff;"><div class="stat-num" style="color:#0d9488;">""" + str(normal_users) + """</div><div class="stat-label">普通用户</div></div>
<div class="stat-item" style="background:#e0f2fe;"><div class="stat-num" style="color:#0284c7;">""" + str(staff_count) + """</div><div class="stat-label">医护员工</div></div>
<div class="stat-item" style="background:#fefce8;"><div class="stat-num" style="color:#ca8a04;">""" + str(appointments) + """</div><div class="stat-label">预约记录</div></div>
<div class="stat-item" style="background:#fff8f0;"><div class="stat-num" style="color:#d97706;">""" + str(exam_records) + """</div><div class="stat-label">体检记录</div></div>
<div class="stat-item" style="background:#f5f3ff;"><div class="stat-num" style="color:#7c3aed;">""" + str(reports) + """</div><div class="stat-label">体检报告</div></div>
<div class="stat-item" style="background:#ecfdf5;"><div class="stat-num" style="color:#059669;">""" + str(total_results) + """</div><div class="stat-label">检查结果</div></div>
<div class="stat-item" style="background:#fef2f2;"><div class="stat-num" style="color:#dc2626;">""" + str(abnormal_results) + """</div><div class="stat-label">异常结果</div></div>
<div class="stat-item" style="background:#fef2f2;"><div class="stat-num" style="color:#dc2626;">""" + str(critical_results) + """</div><div class="stat-label">危急值</div></div>
</div>

<h2>系统配置概览</h2>
<table><tr><th>配置项</th><th>数量</th></tr>
<tr><td>科室</td><td>""" + str(departments) + """ 个</td></tr>
<tr><td>套餐</td><td>""" + str(packages) + """ 种</td></tr>
<tr><td>检查项目</td><td>""" + str(exam_items) + """ 项</td></tr>
</table>

<h2>健康管理数据</h2>
<table><tr><th>指标</th><th>数据</th></tr>
<tr><td>高危人群管理记录</td><td>""" + str(mgmt_count) + """ 条</td></tr>
<tr><td>危急值预警</td><td>""" + str(alert_count) + """ 条</td></tr>
<tr><td>- 危急(CRITICAL)</td><td>""" + str(risk_dist.get('CRITICAL', 0)) + """ 人</td></tr>
<tr><td>- 高风险</td><td>""" + str(risk_dist.get('HIGH', 0)) + """ 人</td></tr>
<tr><td>- 中风险</td><td>""" + str(risk_dist.get('MEDIUM', 0) + risk_dist.get('MODERATE', 0)) + """ 人</td></tr>
<tr><td>- 低风险</td><td>""" + str(risk_dist.get('LOW', 0)) + """ 人</td></tr>
</table>

<div class="page-break"></div>
<h2>用户列表（""" + str(total_users) + """人）</h2>
<table><tr><th>ID</th><th>姓名</th><th>手机号</th><th>性别</th><th>类型</th><th>报告数</th><th>预约数</th><th>检查结果</th></tr>
""" + rows + """
</table>
</body></html>"""

import tempfile, os
tmp = os.path.join(tempfile.gettempdir(), "熙心健康_用户数据总览.doc")
with open(tmp, "w", encoding="utf-8") as f:
    f.write(html)
print(f"OK: users={total_users}, reports={reports}, mgmt={mgmt_count}, alerts={alert_count}")
print(f"Written to: {tmp}")
