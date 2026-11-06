import pymysql

conn = pymysql.connect(host="localhost", user="root", password="123456",
                       database="xixin_health", charset="utf8mb4")
cur = conn.cursor(pymysql.cursors.DictCursor)

cur.execute("SELECT COUNT(*) AS cnt FROM sys_user"); tu = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_report"); rp = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_health_management"); hm = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_critical_alert"); ca = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_appointment WHERE NOT deleted"); ap = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_record"); er = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result"); tr = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result WHERE abnormal_level='CRITICAL'"); cr = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result WHERE abnormal_level IN ('ABNORMAL','CRITICAL')"); ab = cur.fetchone()["cnt"]
cur.execute("SELECT risk_level, COUNT(*) AS cnt FROM t_health_management GROUP BY risk_level")
rd = {r["risk_level"]: r["cnt"] for r in cur.fetchall()}

cur.execute("""
SELECT u.id, u.display_name, u.phone, u.id_card,
  (SELECT COUNT(*) FROM t_exam_report WHERE user_id=u.id) rpt,
  (SELECT COUNT(*) FROM t_check_result cr2 JOIN t_exam_record er2 ON cr2.exam_record_id=er2.id WHERE er2.user_id=u.id) res,
  (SELECT COUNT(*) FROM t_check_result cr2 JOIN t_exam_record er2 ON cr2.exam_record_id=er2.id WHERE er2.user_id=u.id AND cr2.abnormal_level IN ('ABNORMAL','CRITICAL')) abn,
  (SELECT GROUP_CONCAT(DISTINCT r.code) FROM sys_user_role ur JOIN sys_role r ON ur.role_id=r.id WHERE ur.user_id=u.id) roles
FROM sys_user u ORDER BY u.id
""")
users = cur.fetchall()
cur.close()
conn.close()

def gender(c):
    if c and len(c) >= 18:
        try: return '男' if int(c[16]) % 2 == 1 else '女'
        except: pass
    return '未知'

def utype(roles):
    if not roles or roles.split(',') == ['NORMAL_USER']: return '用户'
    return '医护'

rows = ""
for r in users:
    t = utype(r["roles"])
    rc = r["res"] or 0
    ac = r["abn"] or 0
    s = f"总{rc}" + (f" 异常{ac}" if ac > 0 else "")
    if rc == 0: s = "-"
    rows += f"<tr><td>{r['id']}</td><td>{r['display_name'] or '-'}</td><td>{r['phone'] or '-'}</td><td>{gender(r['id_card'])}</td><td><span class=\"tag {'tag-green' if t == '用户' else 'tag-blue'}\">{t}</span></td><td>{r['rpt'] or 0}</td><td>-</td><td>{s}</td></tr>"

html = f"""<!DOCTYPE html><html><head><meta charset="UTF-8"><title>熙心健康 - 用户数据总览</title>
<style>
@page{{margin:2cm}} body{{font-family:'微软雅黑','SimSun',sans-serif;font-size:12pt;color:#333;line-height:1.6}}
h1{{text-align:center;font-size:28pt;color:#00B894;margin-top:80px;margin-bottom:10px}}
.subtitle{{text-align:center;font-size:14pt;color:#666;margin-bottom:10px}}
.meta{{text-align:center;font-size:11pt;color:#999;margin-bottom:40px}}
h2{{font-size:18pt;color:#00B894;border-bottom:2px solid #00B894;padding-bottom:6px;margin-top:40px}}
table{{width:100%;border-collapse:collapse;margin:12px 0;font-size:10pt}}
th{{background:#00B894;color:#fff;padding:8px 10px;text-align:left;font-weight:bold}}
td{{padding:6px 10px;border:1px solid #e0e0e0}}
tr:nth-child(even){{background:#f5faf8}}
.tag{{display:inline-block;padding:2px 10px;border-radius:4px;font-size:9pt;font-weight:bold}}
.tag-green{{background:#d4edda;color:#155724}} .tag-blue{{background:#d1ecf1;color:#0c5460}}
.stats-grid{{display:flex;flex-wrap:wrap;justify-content:center;gap:16px;margin:40px 0 60px}}
.stat-item{{text-align:center;padding:16px 24px;border-radius:12px;min-width:120px}}
.stat-num{{font-size:28pt;font-weight:bold}} .stat-label{{font-size:9pt;margin-top:4px;opacity:0.8}}
</style></head><body>
<h1>熙心健康体检中心</h1>
<div class="subtitle">用户数据总览报告</div>
<div class="meta">生成日期：2026/6/30</div>
<div class="stats-grid">
<div class="stat-item" style="background:#f0fdf4;"><div class="stat-num" style="color:#00B894;">{tu}</div><div class="stat-label">注册用户</div></div>
<div class="stat-item" style="background:#f5f3ff;"><div class="stat-num" style="color:#7c3aed;">{rp}</div><div class="stat-label">体检报告</div></div>
<div class="stat-item" style="background:#fefce8;"><div class="stat-num" style="color:#ca8a04;">{ap}</div><div class="stat-label">预约记录</div></div>
<div class="stat-item" style="background:#fff8f0;"><div class="stat-num" style="color:#d97706;">{er}</div><div class="stat-label">体检记录</div></div>
<div class="stat-item" style="background:#ecfdf5;"><div class="stat-num" style="color:#059669;">{tr}</div><div class="stat-label">检查结果</div></div>
<div class="stat-item" style="background:#fef2f2;"><div class="stat-num" style="color:#dc2626;">{ab}</div><div class="stat-label">异常结果</div></div>
<div class="stat-item" style="background:#fef2f2;"><div class="stat-num" style="color:#dc2626;">{cr}</div><div class="stat-label">危急值</div></div>
<div class="stat-item" style="background:#f0f8ff;"><div class="stat-num" style="color:#0d9488;">{hm}</div><div class="stat-label">高危管理</div></div>
</div>

<h2>健康管理数据</h2>
<table><tr><th>风险等级</th><th>人数</th></tr>
<tr><td>危急(CRITICAL)</td><td>{rd.get('CRITICAL',0)} 人</td></tr>
<tr><td>高风险(HIGH)</td><td>{rd.get('HIGH',0)} 人</td></tr>
<tr><td>中风险(MEDIUM)</td><td>{rd.get('MEDIUM',0)+rd.get('MODERATE',0)} 人</td></tr>
<tr><td>低风险(LOW)</td><td>{rd.get('LOW',0)} 人</td></tr>
<tr><td>危急值预警</td><td>{ca} 条</td></tr>
</table>

<div class="page-break"></div>
<h2>用户列表（{tu}人）</h2>
<table><tr><th>ID</th><th>姓名</th><th>手机号</th><th>性别</th><th>类型</th><th>报告数</th><th>检查结果</th></tr>
{rows}
</table>
</body></html>"""

with open("D:/Desktop/Course Design/physical-examination/熙心健康_用户数据总览_v2.doc", "w", encoding="utf-8") as f:
    f.write(html)
print("Done - wrote to v2 file")
