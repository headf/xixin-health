# 熙心健康体检管理系统后端

## 技术栈

- Java 17 target，当前机器 Java 21 可运行。
- Spring Boot 3.3.5。
- Spring Security + BCrypt + HMAC 签名 JWT 风格 Token。
- Spring Data JPA。
- Flyway。
- H2 默认开发/测试数据库，Docker Compose 提供 MySQL 8.4 和 Redis 7.2。
- springdoc-openapi。

## 本地测试

本机可使用 IntelliJ IDEA 自带 Maven：

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.2\plugins\maven\lib\maven3\bin\mvn.cmd" test
```

## 本地运行

```powershell
docker compose up -d mysql redis
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.2\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

OpenAPI 地址：

```text
http://localhost:8080/swagger-ui.html
```

## 管理端合规开发约束

- 所有 `/api/v1/admin/**` 接口必须使用 `@PreAuthorize` 绑定具体权限码。
- 管理端账号响应禁止返回 `passwordHash`、加密密钥、身份证号、手机号明文等敏感字段。
- 医疗相关配置变更必须进入 `PENDING_REVIEW`，审核通过后才可启用。
- 报告生成必须等待必检项目完成且检查结果审核通过。
- 异常和危急值报告发布必须经过医生确认。
- 统计接口只返回脱敏聚合数据，不返回姓名、手机号、身份证号、档案 ID 或报告正文。
- 管理端新增、修改、删除、导出、审核等操作必须写入操作日志。
- 审计日志必须通过哈希链保护，新增审计字段时需同步更新完整性校验测试。

## 首版范围

已创建认证、RBAC、健康档案、套餐、分院浏览、预约、订单支付模拟、体检结果录入审核、报告生成、健康分析基础、通知、审计日志和管理端角色/账号查询接口。

第三方支付、短信、HIS/LIS/PACS、对象存储、AI 解读在首版中使用模拟实现或预留接口，不连接真实外部平台。
