# 熙心健康体检管理系统 — API 接口文档

> **基础信息**
>
> - **Base URL**: `http://localhost:8080`（开发环境）
> - **Content-Type**: `application/json`
> - **统一响应格式**: 所有接口均返回 `ApiResponse<T>` 包装
>
> ```json
> {
>   "code": "0",        // "0" 表示成功，非零为错误码
>   "message": "success",
>   "data": { ... }     // 实际响应数据，失败时为 null
> }
> ```
>
> **认证方式**: Bearer Token（JWT），在请求头中携带 `Authorization: Bearer <accessToken>`
> **角色权限**: 通过 `@PreAuthorize` 注解控制，支持角色：`NURSE`、`FRONT_DESK`、`EXAM_DOCTOR`、`REVIEW_DOCTOR`

---

## 目录

1. [认证模块 — Auth](#1-认证模块--auth)
2. [体检套餐与分院 — Package & Branch](#2-体检套餐与分院--package--branch)
3. [预约管理 — Appointment](#3-预约管理--appointment)
4. [就诊人档案 — Profile](#4-就诊人档案--profile)
5. [订单与支付 — Order](#5-订单与支付--order)
6. [患者管理 — Patient](#6-患者管理--patient)
7. [体检登记与记录 — Exam](#7-体检登记与记录--exam)
   - 7.1 签到登记
   - 7.2 生命体征
   - 7.3 科室排队
   - 7.4 检查进度
   - 7.5 体检看板
8. [检查结果 — Result](#8-检查结果--result)
9. [体检报告 — Report](#9-体检报告--report)
10. [通知消息 — Notification](#10-通知消息--notification)
11. [后台管理 — Admin](#11-后台管理--admin)
    - 11.1 账户管理
    - 11.2 角色管理
    - 11.3 审计日志
12. [数据模型总览](#12-数据模型总览)
    - 12.1 枚举值说明
    - 12.2 实体关联图

---

## 1. 认证模块 — Auth

### 1.1 用户注册

> `POST /api/v1/auth/register`

**权限**: 无需认证

**Request Body**:
```json
{
  "phone": "13800138000",
  "email": "user@example.com",
  "displayName": "张三",
  "password": "password123"
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `phone` | String | ✅ | 正则 `^1[3-9]\\d{9}$` | 手机号（中国大陆） |
| `email` | String | ❌ | 格式须为合法的 Email | 邮箱地址，可选 |
| `displayName` | String | ✅ | 最大 64 字符 | 显示名称 |
| `password` | String | ✅ | 长度 8–64 字符 | 登录密码 |

**Response**: `ApiResponse<Void>`
- 成功时 `data` 为 `null`

---

### 1.2 用户登录

> `POST /api/v1/auth/login`

**权限**: 无需认证

**Request Body**:
```json
{
  "loginId": "13800138000",
  "password": "password123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `loginId` | String | ✅ | 登录标识（手机号或邮箱） |
| `password` | String | ✅ | 登录密码 |

**额外请求信息**（由服务端自动提取）:
- 客户端 IP 地址（从 `X-Forwarded-For` 或 `RemoteAddr` 获取）
- `User-Agent` 请求头

**Response**: `ApiResponse<AuthTokenResponse>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresInSeconds": 7200
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `tokenType` | String | Token 类型，固定为 `"Bearer"` |
| `accessToken` | String | 访问令牌（JWT），用于接口鉴权 |
| `refreshToken` | String | 刷新令牌（JWT），用于续签 |
| `expiresInSeconds` | long | 过期时间（秒） |

---

## 2. 体检套餐与分院 — Package & Branch

### 2.1 获取体检套餐列表

> `GET /api/v1/packages`

**权限**: 任意已认证用户

**Query 参数**: 无

**Response**: `ApiResponse<List<ExamPackage>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "packageCode": "PKG001",
      "packageName": "入职体检套餐",
      "category": "入职体检",
      "genderLimit": "ALL",
      "ageRange": "18-60",
      "priceCent": 19900,
      "enabled": true,
      "createdAt": "2026-01-01T08:00:00",
      "updatedAt": "2026-01-01T08:00:00"
    }
  ]
}
```

> 仅返回 `enabled = true` 的套餐。

### 2.2 获取分院列表

> `GET /api/v1/branches`

**权限**: 任意已认证用户

**Query 参数**: 无

**Response**: `ApiResponse<List<Branch>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "branchCode": "BJ001",
      "branchName": "北京朝阳分院",
      "address": "北京市朝阳区XXX路XX号",
      "servicePhone": "010-88888888",
      "enabled": true,
      "createdAt": "2026-01-01T08:00:00",
      "updatedAt": "2026-01-01T08:00:00"
    }
  ]
}
```

> 仅返回 `enabled = true` 的分院。

---

## 3. 预约管理 — Appointment

### 3.1 创建预约

> `POST /api/v1/appointments`

**权限**: 任意已认证用户

**Request Body**:
```json
{
  "userId": 1,
  "profileId": 1,
  "packageId": 1,
  "branchId": 1,
  "appointmentDate": "2026-06-30",
  "timeSlot": "上午"
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `userId` | Long | ✅ | — | 用户 ID |
| `profileId` | Long | ✅ | — | 就诊人档案 ID |
| `packageId` | Long | ✅ | — | 体检套餐 ID |
| `branchId` | Long | ✅ | — | 分院 ID |
| `appointmentDate` | LocalDate | ✅ | 当天或未来日期 | 预约日期 |
| `timeSlot` | String | ✅ | 非空 | 时段（如 "上午"、"下午"） |

**Response**: `ApiResponse<Appointment>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "appointmentNo": "APT20260624123456",
    "userId": 1,
    "profileId": 1,
    "packageId": 1,
    "branchId": 1,
    "appointmentDate": "2026-06-30",
    "timeSlot": "上午",
    "status": "CREATED",
    "createdAt": "2026-06-24T10:30:00",
    "updatedAt": "2026-06-24T10:30:00",
    "deleted": false
  }
}
```

---

## 4. 就诊人档案 — Profile

### 4.1 创建/更新个人档案

> `POST /api/v1/profiles/me?ownerUserId={userId}`

**权限**: 任意已认证用户

**Query 参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `ownerUserId` | Long | ✅ | 档案所属用户 ID |

**Request Body**:
```json
{
  "name": "张三",
  "idCardNo": "110101199001011234",
  "phone": "13800138000",
  "allergyHistory": "青霉素过敏",
  "medicalHistory": "高血压",
  "emergencyContactName": "李四",
  "emergencyContactPhone": "13900139000"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | ✅ | 姓名 |
| `idCardNo` | String | ❌ | 身份证号 |
| `phone` | String | ❌ | 联系电话 |
| `allergyHistory` | String | ❌ | 过敏史 |
| `medicalHistory` | String | ❌ | 既往病史 |
| `emergencyContactName` | String | ❌ | 紧急联系人姓名 |
| `emergencyContactPhone` | String | ❌ | 紧急联系人电话 |

> 若该用户已存在档案，则更新；不存在则创建。

**Response**: `ApiResponse<HealthProfile>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "ownerUserId": 1,
    "profileType": "SELF",
    "name": "张三",
    "idCardNo": "110101199001011234",
    "phone": "13800138000",
    "allergyHistory": "青霉素过敏",
    "medicalHistory": "高血压",
    "emergencyContactName": "李四",
    "emergencyContactPhone": "13900139000",
    "createdAt": "2026-06-24T10:30:00",
    "updatedAt": "2026-06-24T10:30:00",
    "deleted": false
  }
}
```

### 4.2 查询用户档案列表

> `GET /api/v1/profiles/by-user?userId={userId}`

**权限**: 任意已认证用户

**Query 参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `userId` | Long | ✅ | 用户 ID |

**Response**: `ApiResponse<List<ProfileBrief>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "type": "SELF"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 档案 ID |
| `name` | String | 姓名 |
| `type` | String | 档案类型：`SELF`（本人）/ `FAMILY`（家人） |

---

## 5. 订单与支付 — Order

### 5.1 创建订单

> `POST /api/v1/orders`

**权限**: 任意已认证用户

**Request Body**:
```json
{
  "appointmentId": 1,
  "amountCent": 19900
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `appointmentId` | Long | ✅ | — | 预约 ID |
| `amountCent` | int | ✅ | 正数 | 金额（单位：分） |

**Response**: `ApiResponse<Order>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "ORD2026062412345678",
    "appointmentId": 1,
    "userId": 1,
    "amountCent": 19900,
    "status": "PENDING_PAYMENT",
    "createdAt": "2026-06-24T10:30:00",
    "updatedAt": "2026-06-24T10:30:00"
  }
}
```

### 5.2 模拟支付

> `POST /api/v1/orders/simulate-payment`

**权限**: 任意已认证用户（仅开发环境使用）

**Request Body**:
```json
{
  "orderNo": "ORD2026062412345678",
  "paymentNo": "PAY2026062412345678"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderNo` | String | ✅ | 订单号 |
| `paymentNo` | String | ✅ | 支付流水号 |

**Response**: `ApiResponse<Void>`

---

## 6. 患者管理 — Patient

### 6.1 患者列表

> `GET /api/v1/patients`

**权限**: 任意已认证用户

**Query 参数**: 无

> 返回所有角色为 `NORMAL_USER` 的用户。

**Response**: `ApiResponse<List<PatientInfo>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "phone": "13800138000"
    }
  ]
}
```

---

## 7. 体检登记与记录 — Exam

> **基础路径**: `/api/v1/exams`

### 7.1 签到登记

#### 7.1.1 患者签到

> `POST /api/v1/exams/check-in`

**权限**: `NURSE`, `FRONT_DESK`

**Request Body**:
```json
{
  "appointmentId": 1,
  "userId": 1,
  "profileId": 1,
  "branchId": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `appointmentId` | Long | ✅ | 预约 ID |
| `userId` | Long | ✅ | 用户 ID |
| `profileId` | Long | ✅ | 档案 ID |
| `branchId` | Long | ✅ | 分院 ID |

**Response**: `ApiResponse<ExamRecord>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "examNo": "EX20260624123456",
    "appointmentId": 1,
    "userId": 1,
    "profileId": 1,
    "branchId": 1,
    "status": "CHECKED_IN",
    "checkedInAt": "2026-06-24T10:30:00",
    "createdAt": "2026-06-24T10:30:00",
    "updatedAt": "2026-06-24T10:30:00"
  }
}
```

#### 7.1.2 获取体检记录（按 ID）

> `GET /api/v1/exams/{id}`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 体检记录 ID |

**Response**: `ApiResponse<ExamRecord>`（结构同上）

#### 7.1.3 按分院查询体检记录

> `GET /api/v1/exams/branch/{branchId}`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `branchId` | Long | 分院 ID |

**Response**: `ApiResponse<List<ExamRecord>>`

#### 7.1.4 查询已签到记录

> `GET /api/v1/exams/checked-in/{branchId}`

**权限**: `NURSE`, `FRONT_DESK`, `EXAM_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `branchId` | Long | 分院 ID |

**Response**: `ApiResponse<List<ExamRecord>>`

#### 7.1.5 查询已签到记录（含详细信息）

> `GET /api/v1/exams/checked-in-with-details/{branchId}`

**权限**: `NURSE`, `FRONT_DESK`, `EXAM_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `branchId` | Long | 分院 ID |

**Response**: `ApiResponse<List<CheckedInDetail>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "record": {
        "id": 1,
        "examNo": "EX20260624123456",
        "appointmentId": 1,
        "userId": 1,
        "profileId": 1,
        "branchId": 1,
        "status": "CHECKED_IN",
        "checkedInAt": "2026-06-24T10:30:00",
        "createdAt": "2026-06-24T10:30:00",
        "updatedAt": "2026-06-24T10:30:00"
      },
      "hasVitalSigns": false,
      "hasOtherDeptQueue": true
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `record` | ExamRecord | 体检记录 |
| `hasVitalSigns` | boolean | 是否已采集生命体征 |
| `hasOtherDeptQueue` | boolean | 是否已在其他科室排队 |

#### 7.1.6 完成体检

> `POST /api/v1/exams/{examRecordId}/complete`

**权限**: `EXAM_DOCTOR`, `NURSE`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `examRecordId` | Long | 体检记录 ID |

**Response**: `ApiResponse<ExamRecord>`（返回更新后的记录，status 变更为 `COMPLETED`）

---

### 7.2 生命体征

#### 7.2.1 记录生命体征

> `POST /api/v1/exams/vital-signs`

**权限**: `NURSE`

**Request Body**:
```json
{
  "examRecordId": 1,
  "recordedBy": 2,
  "height": 170.5,
  "weight": 65.0,
  "bloodPressureSystolic": 120,
  "bloodPressureDiastolic": 80,
  "heartRate": 72,
  "temperature": 36.5,
  "visionLeft": 5.0,
  "visionRight": 5.0,
  "waistline": 80.0
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `examRecordId` | Long | ✅ | 体检记录 ID |
| `recordedBy` | Long | ✅ | 记录人（护士 ID） |
| `height` | BigDecimal | ❌ | 身高（cm） |
| `weight` | BigDecimal | ❌ | 体重（kg） |
| `bloodPressureSystolic` | Integer | ❌ | 收缩压（mmHg） |
| `bloodPressureDiastolic` | Integer | ❌ | 舒张压（mmHg） |
| `heartRate` | Integer | ❌ | 心率（bpm） |
| `temperature` | BigDecimal | ❌ | 体温（°C） |
| `visionLeft` | BigDecimal | ❌ | 左眼视力 |
| `visionRight` | BigDecimal | ❌ | 右眼视力 |
| `waistline` | BigDecimal | ❌ | 腰围（cm） |

**Response**: `ApiResponse<VitalSigns>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "examRecordId": 1,
    "height": 170.5,
    "weight": 65.0,
    "bloodPressureSystolic": 120,
    "bloodPressureDiastolic": 80,
    "heartRate": 72,
    "temperature": 36.5,
    "visionLeft": 5.0,
    "visionRight": 5.0,
    "waistline": 80.0,
    "recordedBy": 2,
    "recordedAt": "2026-06-24T10:35:00",
    "createdAt": "2026-06-24T10:35:00",
    "updatedAt": "2026-06-24T10:35:00"
  }
}
```

#### 7.2.2 查询生命体征

> `GET /api/v1/exams/{examRecordId}/vital-signs`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `examRecordId` | Long | 体检记录 ID |

**Response**: `ApiResponse<VitalSigns>`（结构同上）

---

### 7.3 科室排队

#### 7.3.1 加入科室排队

> `POST /api/v1/exams/queue/add`

**权限**: `NURSE`, `EXAM_DOCTOR`

**Request Body**:
```json
{
  "examRecordId": 1,
  "departmentId": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `examRecordId` | Long | ✅ | 体检记录 ID |
| `departmentId` | Long | ✅ | 科室 ID |

**Response**: `ApiResponse<DepartmentQueue>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "examRecordId": 1,
    "departmentId": 1,
    "queueNo": "3-5",
    "queueStatus": "WAITING",
    "createdAt": "2026-06-24T10:40:00",
    "updatedAt": "2026-06-24T10:40:00"
  }
}
```

#### 7.3.2 查询科室队列

> `GET /api/v1/exams/queue/{departmentId}`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `departmentId` | Long | 科室 ID |

> 返回该科室所有状态的排队记录。

**Response**: `ApiResponse<List<DepartmentQueue>>`

#### 7.3.3 查询等待中队列

> `GET /api/v1/exams/queue/{departmentId}/waiting`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `departmentId` | Long | 科室 ID |

> 仅返回 `queueStatus = WAITING` 的排队记录。

**Response**: `ApiResponse<List<DepartmentQueue>>`

#### 7.3.4 查询等待人数

> `GET /api/v1/exams/queue/{departmentId}/waiting-count`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `departmentId` | Long | 科室 ID |

**Response**: `ApiResponse<Map<String, Long>>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "count": 5
  }
}
```

#### 7.3.5 叫号（呼叫下一位）

> `POST /api/v1/exams/queue/call-next/{departmentId}`

**权限**: `NURSE`, `EXAM_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `departmentId` | Long | 科室 ID |

> 将 queueStatus 从 `WAITING` 更新为 `CALLED`。

**Response**: `ApiResponse<DepartmentQueue>`

#### 7.3.6 到诊确认

> `PUT /api/v1/exams/queue/{queueId}/arrived`

**权限**: `NURSE`, `EXAM_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `queueId` | Long | 排队记录 ID |

> 将 queueStatus 从 `CALLED` 更新为 `ARRIVED`。

**Response**: `ApiResponse<DepartmentQueue>`

#### 7.3.7 完成检查

> `PUT /api/v1/exams/queue/{queueId}/complete`

**权限**: `NURSE`, `EXAM_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `queueId` | Long | 排队记录 ID |

> 将 queueStatus 更新为 `COMPLETED`。

**Response**: `ApiResponse<DepartmentQueue>`

---

### 7.4 检查进度

#### 7.4.1 初始化检查项目进度

> `POST /api/v1/exams/progress/init`

**权限**: `NURSE`, `EXAM_DOCTOR`

**Request Body**:
```json
{
  "examRecordId": 1,
  "itemIds": [1, 2, 3],
  "departmentId": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `examRecordId` | Long | ✅ | 体检记录 ID |
| `itemIds` | List<Long> | ✅ | 检查项目 ID 列表 |
| `departmentId` | Long | ❌ | 科室 ID |

**Response**: `ApiResponse<List<ExamProgress>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "examRecordId": 1,
      "itemId": 1,
      "departmentId": 1,
      "progressStatus": "PENDING",
      "completedAt": null,
      "createdAt": "2026-06-24T10:45:00",
      "updatedAt": "2026-06-24T10:45:00"
    }
  ]
}
```

#### 7.4.2 查询体检项目进度

> `GET /api/v1/exams/{examRecordId}/progress`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `examRecordId` | Long | 体检记录 ID |

**Response**: `ApiResponse<List<ExamProgress>>`

#### 7.4.3 查询科室待处理项目

> `GET /api/v1/exams/progress/pending/{departmentId}`

**权限**: `EXAM_DOCTOR`, `NURSE`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `departmentId` | Long | 科室 ID |

> 返回该科室下 `progressStatus = PENDING` 的检查项目。

**Response**: `ApiResponse<List<ExamProgress>>`

#### 7.4.4 更新项目进度

> `PUT /api/v1/exams/progress/{progressId}/{action}`

**权限**: `EXAM_DOCTOR`, `NURSE`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `progressId` | Long | 进度记录 ID |
| `action` | String | 操作：`start`（开始）/ `complete`（完成）/ `skip`（跳过） |

**操作对照**:

| action | 将 status 变更为 |
|--------|-----------------|
| `start` | `IN_PROGRESS` |
| `complete` | `COMPLETED` |
| `skip` | `SKIPPED` |

**Response**: `ApiResponse<ExamProgress>`

---

### 7.5 体检看板

#### 7.5.1 护士总览看板

> `GET /api/v1/dashboard/nurse/overview`

**权限**: 任意已认证用户

**Query 参数**: 无

**Response**: `ApiResponse<Map<String, Object>>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "totalAppointments": 50,
    "paidAppointments": 35,
    "checkedInAppointments": 20,
    "date": "2026-06-24"
  }
}
```

#### 7.5.2 分院体检看板

> `GET /api/v1/exams/dashboard/{branchId}`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `branchId` | Long | 分院 ID |

**Response**: `ApiResponse<Map<String, Object>>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "total": 100,
    "checkedIn": 30,
    "inProgress": 15,
    "completed": 55
  }
}
```

---

## 8. 检查结果 — Result

> **基础路径**: `/api/v1/results`

### 8.1 记录数值型结果

> `POST /api/v1/results/numeric`

**权限**: `EXAM_DOCTOR`

**Request Body**:
```json
{
  "examRecordId": 1,
  "itemId": 1,
  "itemName": "血清总胆固醇",
  "unit": "mmol/L",
  "numericValue": 5.2,
  "referenceMin": 3.1,
  "referenceMax": 5.7,
  "criticalMax": 10.0,
  "recorderId": 3
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `examRecordId` | Long | ✅ | 体检记录 ID |
| `itemId` | Long | ✅ | 检查项目 ID |
| `itemName` | String | ✅ | 检查项目名称 |
| `unit` | String | ❌ | 单位 |
| `numericValue` | double | ✅ | 数值结果 |
| `referenceMin` | double | ✅ | 参考范围下限 |
| `referenceMax` | double | ✅ | 参考范围上限 |
| `criticalMax` | double | ✅ | 危急值上限 |
| `recorderId` | Long | ✅ | 记录人（医生 ID） |

**Response**: `ApiResponse<CheckResult>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "resultNo": "RES20260624123456",
    "examRecordId": 1,
    "itemId": 1,
    "itemName": "血清总胆固醇",
    "unit": "mmol/L",
    "numericValue": 5.2,
    "textValue": null,
    "referenceMin": 3.1,
    "referenceMax": 5.7,
    "criticalMax": 10.0,
    "abnormalLevel": "NORMAL",
    "reviewStatus": "PENDING",
    "recorderId": 3,
    "createdAt": "2026-06-24T11:00:00",
    "updatedAt": "2026-06-24T11:00:00"
  }
}
```

### 8.2 记录文本型结果

> `POST /api/v1/results/text`

**权限**: `EXAM_DOCTOR`

**Request Body**:
```json
{
  "examRecordId": 1,
  "itemId": 2,
  "itemName": "胸部X光描述",
  "textValue": "双肺纹理清晰，未见明显异常",
  "recorderId": 3
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `examRecordId` | Long | ✅ | 体检记录 ID |
| `itemId` | Long | ✅ | 检查项目 ID |
| `itemName` | String | ✅ | 检查项目名称 |
| `textValue` | String | ✅ | 文本描述结果 |
| `recorderId` | Long | ✅ | 记录人（医生 ID） |

**Response**: `ApiResponse<CheckResult>`

### 8.3 批量录入结果

> `POST /api/v1/results/batch`

**权限**: `EXAM_DOCTOR`

**Request Body**:
```json
{
  "examRecordId": 1,
  "recorderId": 3,
  "items": [
    {
      "itemId": 1,
      "itemName": "血清总胆固醇",
      "unit": "mmol/L",
      "numericValue": 5.2,
      "referenceMin": 3.1,
      "referenceMax": 5.7,
      "criticalMax": 10.0
    },
    {
      "itemId": 2,
      "itemName": "胸部X光描述",
      "textValue": "双肺纹理清晰，未见明显异常"
    }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `examRecordId` | Long | ✅ | 体检记录 ID |
| `recorderId` | Long | ✅ | 记录人 ID |
| `items` | List | ✅ | 结果列表（支持数值型和文本型混合） |

**BatchResultItem 结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `itemId` | Long |—| 检查项目 ID |
| `itemName` | String |—| 检查项目名称 |
| `unit` | String |❌| 单位 |
| `numericValue` | Double |❌| 数值结果（有则录入数值型） |
| `textValue` | String |❌| 文本结果（有则录入文本型） |
| `referenceMin` | BigDecimal |❌| 参考下限 |
| `referenceMax` | BigDecimal |❌| 参考上限 |
| `criticalMax` | BigDecimal |❌| 危急值上限 |

**Response**: `ApiResponse<List<CheckResult>>`

### 8.4 审核检查结果

> `PUT /api/v1/results/{resultId}/review`

**权限**: `REVIEW_DOCTOR`, `EXAM_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `resultId` | Long | 检查结果 ID |

**Request Body**:
```json
{
  "reviewerId": 4,
  "approved": true,
  "comment": "结果正常，无异常"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reviewerId` | Long | ✅ | 审核人 ID |
| `approved` | boolean | ✅ | 是否通过 |
| `comment` | String | ❌ | 审核意见 |

**Response**: `ApiResponse<Void>`

### 8.5 查询待审核结果

> `GET /api/v1/results/pending-review`

**权限**: `REVIEW_DOCTOR`

**Query 参数**: 无

> 返回所有 `reviewStatus = PENDING` 的检查结果。

**Response**: `ApiResponse<List<CheckResult>>`

### 8.6 按记录人查询待审核结果

> `GET /api/v1/results/pending-review/by-recorder?recorderId={recorderId}`

**权限**: `REVIEW_DOCTOR`

**Query 参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `recorderId` | Long | ✅ | 记录人（医生）ID |

> 返回该记录人录入的、`reviewStatus = PENDING` 的检查结果。

**Response**: `ApiResponse<List<CheckResult>>`

---

## 9. 体检报告 — Report

> **基础路径**: `/api/v1/reports`

### 9.1 生成报告

> `POST /api/v1/reports/generate`

**权限**: `REVIEW_DOCTOR`, `EXAM_DOCTOR`

**Request Body**:
```json
{
  "examRecordId": 1,
  "reviewerId": 4
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `examRecordId` | Long | ✅ | 体检记录 ID |
| `reviewerId` | Long | ✅ | 审核人 ID |

**Response**: `ApiResponse<ExamReport>`

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "id": 1,
    "reportNo": "RPT20260624123456",
    "examRecordId": 1,
    "userId": 1,
    "templateId": null,
    "summary": null,
    "doctorAdvice": null,
    "abnormalStatus": "NORMAL",
    "reportStatus": "DRAFT",
    "generatedAt": "2026-06-24T14:00:00",
    "reviewerId": 4,
    "publishedAt": null,
    "fileUrl": null
  }
}
```

### 9.2 写入诊断结论

> `PUT /api/v1/reports/{reportId}/diagnosis`

**权限**: `REVIEW_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `reportId` | Long | 报告 ID |

**Request Body**:
```json
{
  "summary": "各检查指标均在正常范围内",
  "doctorAdvice": "建议保持良好生活习惯，定期复查"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `summary` | String | ❌ | 诊断总结 |
| `doctorAdvice` | String | ❌ | 医生建议 |

**Response**: `ApiResponse<ExamReport>`

### 9.3 发布报告

> `POST /api/v1/reports/{reportId}/publish`

**权限**: `REVIEW_DOCTOR`

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `reportId` | Long | 报告 ID |

**Optional Request Body**（可为空）:
```json
{
  "summary": "各检查指标均在正常范围内",
  "doctorAdvice": "建议保持良好生活习惯，定期复查"
}
```

> 将报告状态从 `DRAFT` 变更为 `PUBLISHED`。

**Response**: `ApiResponse<ExamReport>`

### 9.4 获取报告详情

> `GET /api/v1/reports/{reportId}`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `reportId` | Long | 报告 ID |

**Response**: `ApiResponse<ExamReport>`

### 9.5 按体检记录查询报告

> `GET /api/v1/reports/by-exam/{examRecordId}`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `examRecordId` | Long | 体检记录 ID |

**Response**: `ApiResponse<List<ExamReport>>`

### 9.6 按用户查询报告

> `GET /api/v1/reports/by-user/{userId}`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `userId` | Long | 用户 ID |

**Response**: `ApiResponse<List<ExamReport>>`

---

## 10. 通知消息 — Notification

### 10.1 查询用户通知

> `GET /api/v1/notifications?userId={userId}`

**权限**: 任意已认证用户

**Query 参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `userId` | Long | ✅ | 用户 ID |

**Response**: `ApiResponse<List<Notification>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "notificationType": "APPOINTMENT_REMINDER",
      "title": "体检预约提醒",
      "content": "您预约的2026-06-30体检即将开始，请准时到达。",
      "readFlag": false,
      "createdAt": "2026-06-24T08:00:00",
      "readAt": null
    }
  ]
}
```

### 10.2 标记通知已读

> `PATCH /api/v1/notifications/{notificationId}/read`

**权限**: 任意已认证用户

**Path 参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `notificationId` | Long | 通知 ID |

**Response**: `ApiResponse<Void>`

---

## 11. 后台管理 — Admin

### 11.1 账户管理

#### 11.1.1 查询所有用户账户

> `GET /api/v1/admin/accounts`

**权限**: 任意已认证用户

**Query 参数**: 无

**Response**: `ApiResponse<List<UserAccount>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "phone": "13800138000",
      "email": "user@example.com",
      "displayName": "张三",
      "passwordHash": "$2a$10$...",
      "enabled": true,
      "deleted": false,
      "createdAt": "2026-01-01T08:00:00",
      "updatedAt": "2026-01-01T08:00:00",
      "roles": [
        {
          "id": 1,
          "code": "NORMAL_USER",
          "name": "普通用户",
          "roleType": "FRONTEND",
          "systemBuiltIn": true,
          "deleted": false,
          "permissions": [
            {
              "id": 1,
              "code": "appointment:create",
              "name": "创建预约"
            }
          ]
        }
      ]
    }
  ]
}
```

### 11.2 角色管理

#### 11.2.1 查询所有角色

> `GET /api/v1/admin/roles`

**权限**: 任意已认证用户

**Response**: `ApiResponse<List<Role>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "NORMAL_USER",
      "name": "普通用户",
      "roleType": "FRONTEND",
      "systemBuiltIn": true,
      "deleted": false,
      "permissions": [
        {
          "id": 1,
          "code": "appointment:create",
          "name": "创建预约"
        }
      ]
    }
  ]
}
```

### 11.3 审计日志

#### 11.3.1 查询操作日志

> `GET /api/v1/admin/audit-logs`

**权限**: 任意已认证用户

**Response**: `ApiResponse<List<OperationLog>>`

```json
{
  "code": "0",
  "message": "success",
  "data": [
    {
      "id": 1,
      "operationTime": "2026-06-24T10:30:00",
      "operatorId": "1",
      "operationType": "LOGIN",
      "targetType": "USER",
      "targetId": "1",
      "result": "SUCCESS",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0 ...",
      "traceId": "trace-abc-123"
    }
  ]
}
```

---

## 12. 数据模型总览

### 12.1 通用响应包装

**ApiResponse\<T\>**

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | String | 状态码，`"0"` 表示成功 |
| `message` | String | 提示信息，成功时为 `"success"` |
| `data` | T | 实际响应数据 |

### 12.2 核心实体字段

#### Appointment（预约）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `appointmentNo` | String | 预约编号（自动生成，`APT` + 时间戳） |
| `userId` | Long | 用户 ID |
| `profileId` | Long | 就诊人档案 ID |
| `packageId` | Long | 套餐 ID |
| `branchId` | Long | 分院 ID |
| `appointmentDate` | LocalDate | 预约日期 |
| `timeSlot` | String | 时段（如 "上午"、"下午"） |
| `status` | AppointmentStatus | 状态枚举 |
| `deleted` | boolean | 逻辑删除标志 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### ExamRecord（体检记录）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `examNo` | String | 体检编号（自动生成，`EX` + 时间戳） |
| `appointmentId` | Long | 预约 ID |
| `userId` | Long | 用户 ID |
| `profileId` | Long | 档案 ID |
| `branchId` | Long | 分院 ID |
| `status` | ExamStatus | 状态枚举 |
| `checkedInAt` | LocalDateTime | 签到时间 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### Order（订单）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `orderNo` | String | 订单号（自动生成，`ORD` + 时间戳） |
| `appointmentId` | Long | 预约 ID |
| `userId` | Long | 用户 ID |
| `amountCent` | int | 金额（单位：分） |
| `status` | OrderStatus | 状态枚举 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### HealthProfile（健康档案）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `ownerUserId` | Long | 所属用户 ID |
| `profileType` | ProfileType | 档案类型（`SELF` / `FAMILY`） |
| `name` | String | 姓名 |
| `idCardNo` | String | 身份证号 |
| `phone` | String | 电话 |
| `allergyHistory` | String | 过敏史 |
| `medicalHistory` | String | 既往病史 |
| `emergencyContactName` | String | 紧急联系人 |
| `emergencyContactPhone` | String | 紧急联系人电话 |
| `deleted` | boolean | 逻辑删除 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### VitalSigns（生命体征）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `examRecordId` | Long | 体检记录 ID |
| `height` | BigDecimal | 身高（cm） |
| `weight` | BigDecimal | 体重（kg） |
| `bloodPressureSystolic` | Integer | 收缩压（mmHg） |
| `bloodPressureDiastolic` | Integer | 舒张压（mmHg） |
| `heartRate` | Integer | 心率（bpm） |
| `temperature` | BigDecimal | 体温（°C） |
| `visionLeft` | BigDecimal | 左眼视力 |
| `visionRight` | BigDecimal | 右眼视力 |
| `waistline` | BigDecimal | 腰围（cm） |
| `recordedBy` | Long | 记录人 ID |
| `recordedAt` | LocalDateTime | 记录时间 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### DepartmentQueue（科室排队）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `examRecordId` | Long | 体检记录 ID |
| `departmentId` | Long | 科室 ID |
| `queueNo` | String | 排队编号（如 `"3-5"`） |
| `queueStatus` | String | 状态：`WAITING` / `CALLED` / `ARRIVED` / `COMPLETED` / `SKIPPED` |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### ExamProgress（检查进度）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `examRecordId` | Long | 体检记录 ID |
| `itemId` | Long | 检查项目 ID |
| `departmentId` | Long | 科室 ID |
| `progressStatus` | String | 状态：`PENDING` / `IN_PROGRESS` / `COMPLETED` / `SKIPPED` |
| `completedAt` | LocalDateTime | 完成时间 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### CheckResult（检查结果）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `resultNo` | String | 结果编号（自动生成，`RES` + 时间戳） |
| `examRecordId` | Long | 体检记录 ID |
| `itemId` | Long | 检查项目 ID |
| `itemName` | String | 检查项目名称 |
| `unit` | String | 单位 |
| `numericValue` | BigDecimal | 数值结果 |
| `textValue` | String | 文本结果 |
| `referenceMin` | BigDecimal | 参考范围下限 |
| `referenceMax` | BigDecimal | 参考范围上限 |
| `criticalMax` | BigDecimal | 危急值上限 |
| `abnormalLevel` | AbnormalLevel | 异常等级 |
| `reviewStatus` | ReviewStatus | 审核状态 |
| `recorderId` | Long | 记录人 ID |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### ExamReport（体检报告）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `reportNo` | String | 报告编号（自动生成，`RPT` + 时间戳） |
| `examRecordId` | Long | 体检记录 ID |
| `userId` | Long | 用户 ID |
| `templateId` | Long | 模板 ID |
| `summary` | String | 诊断总结 |
| `doctorAdvice` | String | 医生建议 |
| `abnormalStatus` | AbnormalLevel | 异常状态 |
| `reportStatus` | ReportStatus | 报告状态 |
| `generatedAt` | LocalDateTime | 生成时间 |
| `reviewerId` | Long | 审核人 ID |
| `publishedAt` | LocalDateTime | 发布时间 |
| `fileUrl` | String | 报告文件 URL |

#### Notification（通知）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `userId` | Long | 用户 ID |
| `notificationType` | String | 通知类型 |
| `title` | String | 标题 |
| `content` | String | 内容 |
| `readFlag` | boolean | 是否已读 |
| `createdAt` | LocalDateTime | 创建时间 |
| `readAt` | LocalDateTime | 读取时间 |

#### ExamPackage（体检套餐）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `packageCode` | String | 套餐编码（唯一） |
| `packageName` | String | 套餐名称 |
| `category` | String | 套餐分类 |
| `genderLimit` | String | 性别限制 |
| `ageRange` | String | 年龄范围 |
| `priceCent` | int | 价格（单位：分） |
| `enabled` | boolean | 是否启用 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### Branch（分院）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `branchCode` | String | 分院编码（唯一） |
| `branchName` | String | 分院名称 |
| `address` | String | 地址 |
| `servicePhone` | String | 服务电话 |
| `enabled` | boolean | 是否启用 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |

#### UserAccount（用户账户）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `phone` | String | 手机号（唯一） |
| `email` | String | 邮箱（唯一） |
| `displayName` | String | 显示名称 |
| `passwordHash` | String | 密码哈希 |
| `enabled` | boolean | 是否启用 |
| `deleted` | boolean | 是否删除 |
| `createdAt` | LocalDateTime | 创建时间 |
| `updatedAt` | LocalDateTime | 更新时间 |
| `roles` | Set\<Role\> | 角色集合 |

#### Role（角色）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `code` | String | 角色编码（唯一） |
| `name` | String | 角色名称 |
| `roleType` | String | 角色类型 |
| `systemBuiltIn` | boolean | 是否系统内置 |
| `deleted` | boolean | 是否删除 |
| `permissions` | Set\<Permission\> | 权限集合 |

#### OperationLog（操作日志）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 ID |
| `operationTime` | LocalDateTime | 操作时间 |
| `operatorId` | String | 操作人 ID |
| `operationType` | String | 操作类型 |
| `targetType` | String | 目标类型 |
| `targetId` | String | 目标 ID |
| `result` | String | 操作结果 |
| `ipAddress` | String | IP 地址 |
| `userAgent` | String | 用户代理 |
| `traceId` | String | 追踪 ID |

### 12.3 枚举值说明

| 枚举 | 取值 |
|------|------|
| **AppointmentStatus** | `CREATED`（已创建）、`PAID`（已支付）、`CHECKED_IN`（已签到）、`CANCELLED`（已取消）、`COMPLETED`（已完成） |
| **ExamStatus** | `CHECKED_IN`（已签到）、`IN_PROGRESS`（进行中）、`COMPLETED`（已完成） |
| **OrderStatus** | `PENDING_PAYMENT`（待支付）、`PAID`（已支付）、`REFUND_REQUESTED`（已申请退款）、`REFUNDED`（已退款）、`CLOSED`（已关闭） |
| **ProfileType** | `SELF`（本人）、`FAMILY`（家人） |
| **QueueStatus** | `WAITING`（等待中）、`CALLED`（已叫号）、`ARRIVED`（已到诊）、`COMPLETED`（已完成）、`SKIPPED`（已跳过） |
| **ProgressStatus** | `PENDING`（待检查）、`IN_PROGRESS`（检查中）、`COMPLETED`（已完成）、`SKIPPED`（已跳过） |
| **AbnormalLevel** | `NORMAL`（正常）、`ABNORMAL`（异常）、`CRITICAL`（危急） |
| **ReviewStatus** | `PENDING`（待审核）、`APPROVED`（已通过）、`REJECTED`（已驳回） |
| **ReportStatus** | `DRAFT`（草稿）、`CONFIRMED`（已确认）、`PUBLISHED`（已发布） |

### 12.4 实体间业务关系

```
UserAccount (用户)
  ├── hasMany → Appointment (预约)
  ├── hasMany → Order (订单)
  ├── hasMany → ExamRecord (体检记录)
  ├── hasMany → HealthProfile (健康档案)
  ├── hasMany → Notification (通知)
  ├── hasMany → ExamReport (体检报告)
  └── hasMany → Role (角色) [多对多]

HealthProfile (健康档案) ── belongsTo ── UserAccount
Appointment (预约)        ── belongsTo ── UserAccount, HealthProfile, ExamPackage, Branch
Order (订单)              ── belongsTo ── Appointment
ExamRecord (体检记录)     ── belongsTo ── Appointment, UserAccount, HealthProfile, Branch
VitalSigns (生命体征)     ── belongsTo ── ExamRecord
DepartmentQueue (排队)    ── belongsTo ── ExamRecord, Department
ExamProgress (检查进度)   ── belongsTo ── ExamRecord, ExamItem
CheckResult (检查结果)    ── belongsTo ── ExamRecord, ExamItem
ExamReport (体检报告)     ── belongsTo ── ExamRecord, UserAccount
```

### 12.5 接口汇总统计

| 模块 | 路径前缀 | 接口数 |
|------|----------|--------|
| 认证 | `/api/v1/auth` | 2 |
| 套餐与分院 | `/api/v1/packages`、`/api/v1/branches` | 2 |
| 预约 | `/api/v1/appointments` | 1 |
| 档案 | `/api/v1/profiles` | 2 |
| 订单 | `/api/v1/orders` | 2 |
| 患者 | `/api/v1/patients` | 1 |
| 体检登记与记录 | `/api/v1/exams` | 17 |
| 看板 | `/api/v1/dashboard` | 1 |
| 检查结果 | `/api/v1/results` | 6 |
| 体检报告 | `/api/v1/reports` | 6 |
| 通知 | `/api/v1/notifications` | 2 |
| 账户管理 | `/api/v1/admin/accounts` | 1 |
| 角色管理 | `/api/v1/admin/roles` | 1 |
| 审计日志 | `/api/v1/admin/audit-logs` | 1 |
| **总计** | | **45** |

### 12.6 HTTP 方法分布

| 方法 | 数量 |
|------|------|
| GET | 21 |
| POST | 16 |
| PUT | 7 |
| PATCH | 1 |
| **总计** | **45** |

---

> **文档版本**: v1.0
> **最后更新**: 2026-06-24
> **项目**: 熙心健康体检管理系统 (xixin-health-backend)
