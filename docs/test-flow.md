# 熙心健康体检管理系统 · 完整测试流程

> 日期：2026-06-23  
> 项目：熙心健康体检管理系统（Physical Examination）

---

## 一、账号映射

| 角色 | 手机号 | 用户ID | 密码 |
|------|--------|--------|------|
| 👤 用户张三 | `13800000001` | **4** | `Passw0rd!` |
| 👩‍⚕️ 护士小王 | `13800000100` | **1** | `Passw0rd!` |
| 🩺 体检医生老李 | `13800000200` | **2** | `Passw0rd!` |
| 🔍 审核医生张主任 | `13800000300` | **3** | `Passw0rd!` |

> 用户ID从4开始：预置了3个医护账号（ID=1,2,3），新注册用户从ID=4开始

---

## 二、业务对象ID链

```
张三(用户ID=4)
  └─ 健康档案(ID=1, ownerUserId=4)
       └─ 预约(ID=1, userId=4, profileId=1, status=PAID)
            ├─ 订单(ID=1, appointmentId=1)
            └─ 体检记录(ID=1, userId=4, appointmentId=1)
                 ├─ 生命体征(ID=1, examRecordId=1) ← 护士录入
                 ├─ 检查结果(ID=1,2,3, examRecordId=1) ← 体检医生录入
                 │    ├─ 结果ID=1 → 血糖 NORMAL
                 │    ├─ 结果ID=2 → ALT ABNORMAL
                 │    └─ 结果ID=3 → 胸部X光 NORMAL
                 └─ 报告(ID=1, examRecordId=1) ← 审核医生签发
```

---

## 三、完整测试流程

### 🔶 第一阶段：用户端（test-client.html）

http://localhost:8080/test-client.html

#### Step 1：注册用户
| 字段 | 值 |
|------|-----|
| 手机号 | `13800000001` |
| 邮箱 | `tester@example.com` |
| 显示名 | `张三` |
| 密码 | `Passw0rd!` |

👉 **「注册普通用户」**
```json
{"code":"0","message":"success","data":null}
```

#### Step 2：登录
| 字段 | 值 |
|------|-----|
| 登录账号 | `13800000001` |
| 密码 | `Passw0rd!` |

👉 **「登录并保存 Token」**
```json
{
  "code": "0",
  "message": "success",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresInSeconds": 7200
  }
}
```
✅ 右上角显示 `Token: 已保存`

#### Step 3：查看套餐 + 分院
👉 **「GET 套餐」** → 套餐ID=1 自动填入
👉 **「GET 分院」** → 分院ID=1 自动填入

#### Step 4：创建健康档案
👉 **「创建或更新本人档案」**
```json
{"data": {"id": 1, "ownerUserId": 4, "name": "张三", ...}}
```
→ 档案ID=1 自动填入

#### Step 5：创建预约
👉 **「创建预约」**
```json
{"data": {"id": 1, "status": "CREATED", ...}}
```
→ 预约ID=1 自动填入

#### Step 6：创建订单 + 支付
👉 **「创建订单」** → 订单号自动填入
👉 **「模拟支付成功」**
```json
{"code":"0","message":"success","data":null}
```
✅ 预约状态 → **PAID**

---

### 🔶 第二阶段：护士操作（staff-dashboard.html）

http://localhost:8080/staff-dashboard.html

#### 登录护士（角色=NURSE，用户ID=1）
| 字段 | 值 |
|------|-----|
| 账号 | `13800000100` |
| 密码 | `Passw0rd!` |

#### Step 7：签到登记
| 字段 | 值 |
|------|-----|
| 预约ID | `1` | 用户ID | `4` |
| 档案ID | `1` | 分院ID | `1` |

👉 **「签到登记」**
```json
{"data": {"id": 1, "status": "CHECKED_IN", ...}}
```
✅ **体检记录ID = 1**

#### Step 8：生命体征测量
| 字段 | 值 |
|------|-----|
| 体检记录ID | `1` | 记录人ID | `1` |
| 身高 | `175` | 体重 | `70` |
| 收缩压 | `120` | 舒张压 | `80` |
| 心率 | `72` | 体温 | `36.5` |
| 左眼视力 | `5.0` | 右眼视力 | `5.0` |
| 腰围 | `85` |

👉 **「录入体征」** → 录入成功

#### Step 9：排队叫号
| 字段 | 值 |
|------|-----|
| 体检记录ID | `1` | 科室ID | `1` |

👉 **「加入排队」** → WAITING
👉 **「叫下一个」** → CALLED

#### Step 10：初始化进度
| 字段 | 值 |
|------|-----|
| 体检记录ID | `1` | 科室ID | `1` |
| 项目ID列表 | `1,2,3` |

👉 **「初始化进度」**
```json
{"data": [
  {"id":1, "itemId":1, "progressStatus":"PENDING"},
  {"id":2, "itemId":2, "progressStatus":"PENDING"},
  {"id":3, "itemId":3, "progressStatus":"PENDING"}
]}
```

---

### 🔶 第三阶段：体检医生（staff-dashboard.html）

#### 登录体检医生（角色=EXAM_DOCTOR，用户ID=2）
| 字段 | 值 |
|------|-----|
| 账号 | `13800000200` |
| 密码 | `Passw0rd!` |

#### Step 11：数值结果 — 血糖（正常）
| 字段 | 值 |
|------|-----|
| 体检记录ID | `1` | 项目ID | `1` |
| 项目名称 | `血糖` | 单位 | `mmol/L` |
| 结果值 | `5.2` | 参考范围 | `3.9 ~ 6.1` |
| 危急值 | `11.1` | 录入医生ID | `2` |

👉 **「录入数值结果」**
```json
{"data": {"abnormalLevel": "NORMAL", "reviewStatus": "PENDING"}}
```

#### Step 12：数值结果 — ALT（异常）
| 字段 | 值 |
|------|-----|
| 体检记录ID | `1` | 项目ID | `2` |
| 项目名称 | `ALT` | 单位 | `U/L` |
| 结果值 | `55` | 参考范围 | `0 ~ 40` |
| 危急值 | `400` | 录入医生ID | `2` |

👉 **「录入数值结果」**
```json
{"data": {"abnormalLevel": "ABNORMAL", "reviewStatus": "PENDING"}}
```

#### Step 13：文本结果 — 胸部X光
| 字段 | 值 |
|------|-----|
| 体检记录ID | `1` | 项目ID | `3` |
| 项目名称 | `胸部X光` | 录入医生ID | `2` |
| 结果描述 | `双肺纹理清晰，未见明显异常密度影。` |

👉 **「录入文本结果」** → 正常

---

### 🔶 第四阶段：审核医生（staff-dashboard.html）

#### 登录审核医生（角色=REVIEW_DOCTOR，用户ID=3）
| 字段 | 值 |
|------|-----|
| 账号 | `13800000300` |
| 密码 | `Passw0rd!` |

#### Step 14：查看待审核
👉 **「获取所有待审核」**
```json
{"data": [
  {"id":1, "itemName":"血糖", "abnormalLevel":"NORMAL"},
  {"id":2, "itemName":"ALT", "abnormalLevel":"ABNORMAL"},
  {"id":3, "itemName":"胸部X光", "abnormalLevel":"NORMAL"}
]}
```

#### Step 15：逐条审核通过
| 结果ID | 审核医生ID | 意见 |
|--------|-----------|------|
| `1` | `3` | `血糖正常，通过` |
| `2` | `3` | `ALT轻度偏高，建议复查，通过` |
| `3` | `3` | `胸片正常，通过` |

👉 **「审核通过」** × 3次

#### Step 16：生成报告
| 字段 | 值 |
|------|-----|
| 体检记录ID | `1` | 审核医生ID | `3` |

👉 **「生成报告」** → 报告ID=1

#### Step 17：诊断总结
| 字段 | 值 |
|------|-----|
| 报告ID | `1` |
| 总结摘要 | `受检者血糖、胸片正常，ALT轻度偏高...建议复查。` |
| 医生建议 | `1. 注意饮食清淡\n2. 规律作息\n3. 3个月后复查肝功能` |

👉 **「保存诊断建议」**

#### Step 18：终审签发
👉 **「终审签发」**
```json
{"data": {"reportStatus": "PUBLISHED", "publishedAt": "..."}}
```

🎉 **全流程跑通！**

---

## 四、状态流转图

```
注册 → 登录 → 创档案 → 约体检 → 下单 → 支付
                                            │
                                            ▼
              ┌─── 护士签到 ──→ 测体征 ──→ 排队叫号 ──→ 初始化进度
              │                                            │
              ▼                                            ▼
      ┌── 体检医生录入结果 ─────────────────────→ 更新进度
      │         │
      │         ▼
      └── 审核医生审核 ──→ 生成报告 ──→ 诊断总结 ──→ 签发
```

**状态流转：**
```
预约: CREATED → PAID → CHECKED_IN
体检: CHECKED_IN → IN_PROGRESS → COMPLETED
结果: PENDING → APPROVED (或 REJECTED)
报告: CONFIRMED → PUBLISHED
```

---

## 五、API 概览

| 模块 | 方法 | 路径 | 角色 |
|------|------|------|------|
| 认证 | POST | `/api/v1/auth/register` | 公开 |
| 认证 | POST | `/api/v1/auth/login` | 公开 |
| 档案 | POST | `/api/v1/profiles/me` | 登录用户 |
| 预约 | POST | `/api/v1/appointments` | 登录用户 |
| 订单 | POST | `/api/v1/orders` | 登录用户 |
| 支付 | POST | `/api/v1/orders/simulate-payment` | 登录用户 |
| 签到 | POST | `/api/v1/exams/check-in` | 护士/前台 |
| 体征 | POST | `/api/v1/exams/vital-signs` | 护士 |
| 排队 | POST | `/api/v1/exams/queue/add` | 护士 |
| 叫号 | POST | `/api/v1/exams/queue/call-next/{deptId}` | 护士 |
| 进度 | POST | `/api/v1/exams/progress/init` | 护士 |
| 进度 | PUT | `/api/v1/exams/progress/{id}/{action}` | 护士/医生 |
| 数值结果 | POST | `/api/v1/results/numeric` | 体检医生 |
| 文本结果 | POST | `/api/v1/results/text` | 体检医生 |
| 批量结果 | POST | `/api/v1/results/batch` | 体检医生 |
| 待审核 | GET | `/api/v1/results/pending-review` | 审核医生 |
| 审核 | PUT | `/api/v1/results/{id}/review` | 审核医生 |
| 生成报告 | POST | `/api/v1/reports/generate` | 审核医生 |
| 诊断 | PUT | `/api/v1/reports/{id}/diagnosis` | 审核医生 |
| 签发 | POST | `/api/v1/reports/{id}/publish` | 审核医生 |
