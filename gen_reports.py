import pymysql
import random
from datetime import datetime, timedelta

conn = pymysql.connect(host="localhost", user="root", password="123456", database="xixin_health", charset="utf8mb4")
cur = conn.cursor(pymysql.cursors.DictCursor)

# 获取基础数据
cur.execute("SELECT id, display_name, id_card FROM sys_user WHERE id NOT IN (SELECT DISTINCT user_id FROM t_exam_report)")
new_users = cur.fetchall()

cur.execute("""
SELECT DISTINCT u.id, u.display_name, u.id_card
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE r.code = 'NORMAL_USER'
  AND u.id IN (SELECT DISTINCT user_id FROM t_exam_report)
""")
existing_report_users = cur.fetchall()

cur.execute("SELECT id, price_cent FROM t_exam_package WHERE enabled=TRUE")
packages = cur.fetchall()

cur.execute("SELECT id, item_code, item_name, unit, reference_min, reference_max, critical_max, critical_min FROM t_exam_item WHERE enabled=TRUE")
exam_items = cur.fetchall()

cur.execute("SELECT id AS bid FROM t_branch WHERE enabled=TRUE")
branches = [b["bid"] for b in cur.fetchall()]

cur.execute("SELECT id, owner_user_id FROM t_health_profile")
all_profiles = cur.fetchall()

# 获取最大ID用于自增编号
cur.execute("SELECT MAX(id) AS mid FROM t_appointment")
base_apt_id = (cur.fetchone()["mid"] or 0) + 1

cur.execute("SELECT MAX(id) AS mid FROM t_exam_record")
base_exam_id = (cur.fetchone()["mid"] or 0) + 1

cur.execute("SELECT MAX(id) AS mid FROM t_exam_report")
base_rpt_id = (cur.fetchone()["mid"] or 0) + 1

cur.execute("SELECT MAX(id) AS mid FROM t_check_result")
base_res_id = (cur.fetchone()["mid"] or 0) + 1

import time
ts_base = int(time.time() * 1000) % 10000000

# 为每个用户找一个profile
user_profiles = {}
for p in all_profiles:
    uid = p["owner_user_id"]
    if uid not in user_profiles:
        user_profiles[uid] = p["id"]

# 决定每个用户生成多少报告 - 第二轮调高数量冲1000
all_users = []
for u in new_users:
    n = random.choices([2,3,4,5,6,7], weights=[5,15,30,30,15,5])[0]
    all_users.append((u["id"], u["display_name"], u["id_card"], n))

for u in existing_report_users:
    n = random.choices([2,3,4,5], weights=[15,35,35,15])[0]
    all_users.append((u["id"], u["display_name"], u["id_card"], n))

total_needed = sum(u[3] for u in all_users)
print(f"总用户数: {len(all_users)}, 需要创建: {total_needed}")

# 参考范围定义
ref_ranges = {
    1:  (3.9, 6.1, 11.1, None),
    2:  (0, 40, 400, None),
    3:  (0, 40, 400, None),
    4:  (44, 133, 400, None),
    5:  (2.9, 8.2, None, None),
    6:  (2.8, 5.2, None, None),
    7:  (0.56, 1.7, None, None),
    8:  (1.0, 1.55, None, None),
    9:  (0, 3.4, None, None),
    10: (150, 420, None, None),
    11: (3.5, 9.5, None, None),
    12: (120, 160, None, None),
    13: (100, 300, None, None),
}

def gen_value(item_id):
    if item_id not in ref_ranges:
        return None, "NORMAL"
    ref_min, ref_max, crit_max, crit_min = ref_ranges[item_id]
    if ref_min is None or ref_max is None:
        return None, "NORMAL"
    mid = (ref_min + ref_max) / 2
    spread = (ref_max - ref_min) / 3
    r = random.random()
    if r < 0.03:
        if crit_max:
            val = ref_max + random.uniform(0.5, 2.0) * (ref_max - ref_min)
        else:
            val = ref_max + random.uniform(0.3, 0.8) * (ref_max - ref_min)
        return round(val, 2), "CRITICAL"
    elif r < 0.12:
        val = ref_max + random.uniform(0.1, 0.5) * (ref_max - ref_min)
        return round(val, 2), "ABNORMAL"
    else:
        val = mid + random.uniform(-spread, spread)
        return round(max(ref_min * 0.7, val), 2), "NORMAL"

now = datetime.now()
created = 0
import time

# 套餐项目映射
pkg_items_map = {
    1: [1, 2, 3],
    2: [1, 2, 3, 4, 6, 7, 10, 14, 15, 16, 17, 18, 19, 21, 34, 35],
    3: [1, 2, 14],
    4: list(range(1, 37)),
    5: [1, 2, 4, 6, 7, 8, 9, 10, 11, 12],
    6: [1, 6, 7, 8, 9, 10, 14, 15, 16],
    7: [1, 2, 4, 5, 10, 14, 15, 16, 17],
    8: [6, 7, 8, 9, 10, 14, 18, 19, 20, 21],
    9: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21],
}

for uid, name, id_card, num_records in all_users:
    if created >= total_needed:
        break

    profile_id = user_profiles.get(uid)
    if not profile_id:
        continue

    branch_id = random.choice(branches)
    pkg = random.choice(packages)
    pkg_id = pkg["id"]

    for i in range(num_records):
        if created >= total_needed:
            break

        days_ago = random.randint(0, 900)
        apt_date = now - timedelta(days=days_ago + random.randint(0, 3))
        apt_date_str = apt_date.strftime("%Y-%m-%d")
        time_slot = random.choice(["MORNING", "AFTERNOON"])
        ts = int(time.time() * 1000) % 10000000 + created + base_apt_id

        try:
            # 1. 预约
            apt_no = f"APT{ts}"
            cur.execute("""
                INSERT INTO t_appointment (appointment_no, user_id, profile_id, package_id, branch_id, appointment_date, time_slot, status, created_at, updated_at, deleted)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (apt_no, uid, profile_id, pkg_id, branch_id, apt_date_str, time_slot, "COMPLETED", apt_date, apt_date, 0))
            apt_id = cur.lastrowid

            # 2. 体检记录
            exam_no = f"EX{ts}"
            checked_in_at = apt_date + timedelta(hours=7)
            cur.execute("""
                INSERT INTO t_exam_record (exam_no, appointment_id, user_id, profile_id, branch_id, status, checked_in_at, package_id, package_name, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (exam_no, apt_id, uid, profile_id, branch_id, "COMPLETED", checked_in_at, pkg_id, "体检套餐", apt_date, apt_date))
            exam_record_id = cur.lastrowid

            # 3. 检查结果
            item_ids = pkg_items_map.get(pkg_id, [1, 2, 3])
            if pkg_id >= 4 and len(item_ids) > 8:
                random.shuffle(item_ids)
                item_ids = item_ids[:random.randint(8, min(20, len(item_ids)))]

            for item_id in item_ids:
                item = next((ei for ei in exam_items if ei["id"] == item_id), None)
                if not item:
                    continue

                val, level = gen_value(item_id)
                if val is None:
                    continue

                cur.execute("""
                    INSERT INTO t_check_result (result_no, exam_record_id, item_id, item_name, unit, numeric_value, reference_min, reference_max, critical_max, critical_min, abnormal_level, review_status, recorder_id, created_at, updated_at)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 'APPROVED', %s, %s, %s)
                """, (f"RES{ts}{item_id}", exam_record_id, item_id, item["item_name"], item["unit"] or "",
                      str(val),
                      str(item["reference_min"]) if item["reference_min"] else None,
                      str(item["reference_max"]) if item["reference_max"] else None,
                      str(item["critical_max"]) if item["critical_max"] else None,
                      str(item["critical_min"]) if item["critical_min"] else None,
                      level, uid, apt_date, apt_date))

            # 4. 报告
            cur.execute("SELECT MAX(CASE WHEN abnormal_level='CRITICAL' THEN 2 WHEN abnormal_level='ABNORMAL' THEN 1 ELSE 0 END) AS sev FROM t_check_result WHERE exam_record_id=%s", (exam_record_id,))
            max_sev = cur.fetchone()["sev"]
            abnormal_level = "CRITICAL" if max_sev == 2 else ("ABNORMAL" if max_sev == 1 else "NORMAL")

            report_no = f"RPT{ts}"
            generated_at = apt_date + timedelta(hours=random.randint(24, 72))
            cur.execute("""
                INSERT INTO t_exam_report (report_no, exam_record_id, user_id, summary, doctor_advice, abnormal_status, report_status, generated_at, published_at, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (report_no, exam_record_id, uid,
                  f"{name}的体检报告", "建议保持良好生活习惯，定期体检。",
                  abnormal_level, "PUBLISHED", generated_at, generated_at, apt_date, apt_date))

            created += 1
            if created % 200 == 0:
                conn.commit()
                print(f"  已创建 {created}/{total_needed}...")

        except Exception as e:
            conn.rollback()
            print(f"  错误: {e}")
            continue

conn.commit()
print(f"\n完成！共创建 {created} 条")

# 验证
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_report")
rpt_cnt = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_exam_record")
rec_cnt = cur.fetchone()["cnt"]
cur.execute("SELECT COUNT(*) AS cnt FROM t_check_result")
res_cnt = cur.fetchone()["cnt"]
print(f"最终: 记录={rec_cnt}, 报告={rpt_cnt}, 结果={res_cnt}")

cur.execute("""
SELECT u.display_name, COUNT(*) AS cnt
FROM t_exam_report r
JOIN sys_user u ON r.user_id = u.id
GROUP BY r.user_id
ORDER BY cnt DESC
LIMIT 10
""")
print("\n报告TOP10:")
for r in cur.fetchall():
    print(f"  {r['display_name']}: {r['cnt']}")

conn.close()
