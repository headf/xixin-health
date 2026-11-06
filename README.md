# 熙心健康体检管理系统（Xixin Health）

面向体检中心的健康体检管理系统，覆盖 **预约 → 体检 → 审核 → 报告 → 健康干预** 全流程，采用前后端分离 + 多端（管理端 / 医护端 / 用户端 Web / 用户端 App / 心电图子系统）架构。

> 课程设计项目。后端数据库共 **36 张表**，由 Flyway 管理迁移脚本。

## 系统架构

```
┌────────────┐  ┌────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  admin     │  │  staff     │  │  user-web   │  │  user-app   │  │  ecg-system │
│ 管理端     │  │ 医护端     │  │ 用户端 Web  │  │ 用户端 App  │  │ 心电子系统  │
│ Vue3+EP    │  │ Vue3+EP    │  │ Vue3        │  │ uni-app     │  │ Vue3+EP     │
└─────┬──────┘  └─────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
      └───────────────┴────────────────┴─────────────────┴─────────────────┘
                                      │ REST
                            ┌─────────▼─────────┐
                            │   backend (8080)  │
                            │  Spring Boot 3.3  │
                            │  JPA · Security   │
                            └─────────┬─────────┘
                                      │
                                MySQL (xixin_health)
```

## 功能特性

- 📅 **体检预约**：用户在线预约体检套餐与时间
- 🩺 **体检过程**：护士分站录入体检数据
- ✅ **结果审核**：体检医生 / 审核医生三级审核
- 📄 **报告生成**：基于 PDFBox 生成中文体检报告（PDF）
- ❤️ **健康干预**：异常指标自动触发健康建议
- 📈 **趋势分析**：历史指标趋势 + AI 解读（DeepSeek，可降级）
- 🫀 **心电子系统**：心电图采集与展示
- 🔐 **权限与审计**：Spring Security + JWT，操作审计、脱敏
- 📊 **统计分析**：ECharts 数据大屏与统计报表

## 技术栈

| 模块 | 技术栈 |
|------|--------|
| `backend/` | Spring Boot 3.3.5 · Spring Data JPA · Spring Security · JWT · Flyway · MySQL · PDFBox · SpringDoc |
| `admin/` | Vue 3 · Vite · Element Plus · Pinia · Vue Router · ECharts |
| `staff/` | Vue 3 · Vite · Element Plus · Pinia · Vue Router · ECharts |
| `user-web/` | Vue 3 · Vite · Vue Router · Chart.js |
| `user-app/` | uni-app（Vue 3，H5 + 微信小程序） |
| `ecg-system/` | Vue 3 · Vite · Element Plus |

## 目录结构

```
physical-examination/
├─ backend/        Spring Boot 后端（36 张表，JPA 实体 + Flyway 迁移）
├─ admin/          管理端前端（管理员）
├─ staff/          医护端前端（护士 / 体检医生 / 审核医生）
├─ user-web/       用户端 Web 前端
├─ user-app/       用户端移动端（uni-app，H5 / 小程序）
├─ ecg-system/     心电网络子系统
├─ docs/           需求追踪矩阵、技术说明书、功能模块图等文档
├─ DESIGN.md       数据库 / 架构设计说明
└─ *.sql           建表与演示数据脚本
```

## 快速开始

### 后端

```bash
cd backend
mvn spring-boot:run        # 默认 8080，需本机 MySQL
```

### 前端（任一端）

```bash
cd admin     # 或 staff / user-web / ecg-system
npm install
npm run dev
```

`user-app` 使用 uni-app：

```bash
cd user-app
npm install
npm run dev:h5          # H5
npm run dev:mp-weixin   # 微信小程序
```

## 环境变量

| 变量 | 说明 | 默认 |
|------|------|------|
| `MYSQL_PASSWORD` | MySQL 密码 | `123456` |
| `JWT_SECRET` | JWT 签名密钥（≥32 字节） | 占位值 |
| `DEEPSEEK_API_KEY` | 趋势分析 AI 密钥（不配则降级） | 占位值 |
| `XIXIN_ADMIN_PASSWORD` | 初始化管理员密码 | `ChangeMe123!` |

## 文档

详细设计见 `DESIGN.md` 与 `docs/` 目录（需求追踪矩阵、技术说明书、功能模块图）。

## License

MIT
