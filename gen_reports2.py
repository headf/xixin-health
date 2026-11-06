"""第二轮：补充生成更多报告，直到总量超过1000"""
import pymysql, random, time, math
from datetime import datetime, timedelta

conn = pymysql.connect(host="localhost", user="root", password="123456", database="xixin_health", charset="utf8mb4")
cur = conn.cursor(pymysql.cursors.DictCursor)

# 获取所有普通用户
cur.execute("""
SELECT DISTINCT u.id, u.display_name, u.id_card
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE r.code = 'NORMAL_USER'
ORDER BY u.id
""")
all_normal_users = cur.fetchall()

# 获取各用户已有报告数
cur.execute("SELECT user_id, COUNT(*) AS cnt FROM t_exam_report GROUP BY user_id")
report_counts = {r["user_id"]: r["cnt"] for r in cur.fetchall()}

cur.execute("SELECT id, owner_user_id FROM t_health_profile")
all_profiles = cur.fetchall()
user_profiles = {}
for p in all_profiles:
    if p["owner_user_id"] not in user_profiles:
        user_profiles[p["owner_user_id"]] = p["id"]

cur.execute("SELECT id AS bid FROM t_branch WHERE enabled=TRUE")
branches = [b["bid"] for b in cur.fetchall()]

cur.execute("SELECT id, price_cent FROM t_exam_package WHERE enabled=TRUE")
packages = cur.fetchall()

cur.execute("SELECT id, item_code, item_name, unit, reference_min, reference_max, critical_max, critical_min FROM t_exam_item WHERE enabled=TRUE")
exam_items = cur.fetchall()

ref_ranges = {
    1: (3.9, 6.1, 11.1, None), 2: (0, 40, 400, None), 3: (0, 40, 400, None),
    4: (44, 133, 400, None), 5: (2.9, 8.2, None, None), 6: (2.8, 5.2, None, None),
    7: (0.56, 1.7, None, None), 8: (1.0, 1.55, None, None), 9: (0, 3.4, None, None),
    10: (150, 420, None, None), 11: (3.5, 9.5, None, None), 12: (120, 160, None, None),
    13: (100, 300, None, None),
}

def gen_value(item_id):
    if item_id not in ref_ranges: return None, "NORMAL"
    ref_min, ref_max, crit_max, crit_min = ref_ranges[item_id]
    if ref_min is None or ref_max is None: return None, "NORMAL"
    mid = (ref_min + ref_max) / 2
    spread = (ref_max - ref_min) / 3
    r = random.random()
    if r < 0.04:
        val = ref_max + random.uniform(0.5, 1.5) * (ref_max - ref_min)
        return round(val, 2), "CRITICAL"
    elif r < 0.15:
        val = ref_max + random.uniform(0.1, 0.5) * (ref_max - ref_min)
        return round(val, 2), "ABNORMAL"
    else:
        val = mid + random.uniform(-spread, spread)
        return round(max(ref_min * 0.7, val), 2), "NORMAL"

pkg_items_map = {
    1: [1, 2, 3], 2: [1, 2, 3, 4, 6, 7, 10, 14, 15, 16, 17, 18, 19, 21, 34, 35],
    3: [1, 2, 14], 4: list(range(1, 37)), 5: [1, 2, 4, 6, 7, 8, 9, 10, 11, 12],
    6: [1, 6, 7, 8, 9, 10, 14, 15, 16], 7: [1, 2, 4, 5, 10, 14, 15, 16, 17],
    8: [6, 7, 8, 9, 10, 14, 18, 19, 20, 21], 9: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21],
}

cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_report")
current_rpt = cur.fetchone()["cnt"]
target = max(1000, current_rpt + 500)
print(f"当前报告数: {current_rpt}, 目标: {target}")
print(f"用户数: {len(all_normal_users)}")

now = datetime.now()
created = 0
needed = target - current_rpt

# 给每个用户分配补足数量
user_targets = []
for u in all_normal_users:
    uid = u["id"]
    existing = report_counts.get(uid, 0)
    add = max(0, random.choices([3,4,5,6,7], weights=[10,25,35,20,10])[0] - existing // 2)
    add = max(add, 1)
    if add > 0:
        user_targets.append((uid, u["display_name"], u["id_card"], add))

total_to_create = sum(u[3] for u in user_targets)
print(f"需创建: {total_to_create}")

batch = 0
for uid, name, id_card, add_count in user_targets:
    if created >= needed:
        break
    profile_id = user_profiles.get(uid)
    if not profile_id:
        continue
    for i in range(add_count):
        if created >= needed:
            break
        days_ago = random.randint(10, 1000)
        apt_date = now - timedelta(days=days_ago)
        apt_date_str = apt_date.strftime("%Y-%m-%d")
        ts = int(time.time() * 1000) % 100000000 + created
        try:
            pkg = random.choice(packages)
            pkg_id = pkg["id"]
            branch_id = random.choice(branches)
            apt_no = f"APT{ts}"
            cur.execute("INSERT INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                        (apt_no, uid, profile_id, pkg_id, branch_id, apt_date_str, random.choice(["MORNING","AFTERNOON"]), "COMPLETED", apt_date, apt_date, 0))
            apt_id = cur.lastrowid

            exam_no = f"EX{ts}"
            checked_in_at = apt_date + timedelta(hours=random.randint(7,10))
            cur.execute("INSERT INTO t_exam_record (exam_no, appointment_id, user_id, profile_id, branch_id, status, checked_in_at, package_id, package_name, created_at, updated_at) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                        (exam_no, apt_id, uid, profile_id, branch_id, "COMPLETED", checked_in_at, pkg_id, "体检套餐", apt_date, apt_date))
            exam_id = cur.lastrowid

            item_ids = pkg_items_map.get(pkg_id, [1,2,3])
            if len(item_ids) > 8:
                item_ids = random.sample(item_ids, random.randint(8, min(18, len(item_ids))))

            for item_id in item_ids:
                item = next((ei for ei in exam_items if ei["id"] == item_id), None)
                if not item: continue
                val, level = gen_value(item_id)
                if val is None: continue
                cur.execute("INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, critical_max, critical_min, abnormal_level, review_status, recorder_id, created_at, updated_at) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,'APPROVED',%s,%s,%s)",
                            (f"RES{ts}{item_id}", exam_id, item_id, item["item_name"], item["unit"] or "",
                             str(val), str(item["reference_min"]) if item["reference_min"] else None,
                             str(item["reference_max"]) if item["reference_max"] else None,
                             str(item["critical_max"]) if item["critical_max"] else None,
                             str(item["critical_min"]) if item["critical_min"] else None,
                             level, uid, apt_date, apt_date))

            cur.execute("SELECT MAX(CASE WHEN abnormal_level='CRITICAL' THEN 2 WHEN abnormal_level='ABNORMAL' THEN 1 ELSE 0 END) AS sev FROM t_check_result WHERE exam_record_id=%s", (exam_id,))
            max_sev = cur.fetchone()["sev"]
            abnormal_level = "CRITICAL" if max_sev == 2 else ("ABNORMAL" if max_sev == 1 else "NORMAL")
            generated_at = apt_date + timedelta(hours=random.randint(24, 72))
            cur.execute("INSERT INTO t_exam_report (report_no, exam_record_id, user_id, summary, doctor_advice, abnormal_status, report_status, generated_at, published_at, created_at, updated_at) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                        (f"RPT{ts}", exam_id, uid, f"{name}的体检报告", "建议保持良好生活习惯，定期体检。",
                         abnormal_level, "PUBLISHED", generated_at, generated_at, apt_date, apt_date))
            created += 1
            batch += 1
            if batch % 200 == 0:
                conn.commit()
                print(f"  已创建 {created}/{needed}...")
        except Exception as e:
            conn.rollback()
            continue

conn.commit()
print(f"\n完成！本轮创建 {created} 条")

cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_report")
final_cnt = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_record")
rec_cnt = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result")
res_cnt = cur.fetchone()["cnt"]
print(f"最终: 报告={final_cnt}, 记录={rec_cnt}, 结果={res_cnt}")
cur.execute("SELECT u.display_name, COUNT(*) AS cnt FROM t_exam_report r JOIN sys_user u ON r.user_id=u.id GROUP BY r.user_id ORDER BY cnt DESC LIMIT 10")
print("TOP10:")
for r in cur.fetchall():
    print(f"  {r['display_name']}: {r['cnt']}")
conn.close()
