# 管理端需求追踪矩阵

| 需求项 | 来源 | 必选性 | 后端实现 | 前端实现 | 测试 |
| --- | --- | --- | --- | --- | --- |
| 基础配置-体检项目管理 | 功能结构表 R54 / 规约 3.2.11 | 必选 | `AdminConfigurationController` | `admin/src/pages/config/ExamItemPage.tsx` | `AdminConfigurationComplianceTest.examItemRequiresConfigPermissionAndAudit()` |
| 基础配置-套餐配置 | 功能结构表 R55 / 规约 3.2.11 | 必选 | `AdminConfigurationController` | `admin/src/pages/config/PackagePage.tsx` | `AdminConfigurationComplianceTest.packageDisableDoesNotPhysicalDelete()` |
| 基础配置-规则配置审核 | 功能结构表 R56 / 规约 3.2.11 | 必选 | `ConfigReviewController` | `admin/src/pages/config/RulePage.tsx` | `AdminConfigurationComplianceTest.medicalRuleChangeRequiresReviewBeforeEffective()` |
| 基础配置-分院管理 | 功能结构表 R57 / 规约 3.2.11 | 必选 | `AdminConfigurationController` | `admin/src/pages/config/BranchPage.tsx` | `AdminConfigurationComplianceTest.branchCrudRequiresConfigPermission()` |
| 基础配置-科室管理 | 功能结构表 R58 / 规约附录 | 必选 | `AdminDepartmentController` | `admin/src/pages/config/DepartmentPage.tsx` | `AdminConfigurationComplianceTest.departmentCrudRequiresDepartmentPermission()` |
| 权限-角色管理 | 功能结构表 R60 / 规约 3.2.12 | 必选 | `RoleController` | `admin/src/pages/iam/RolePage.tsx` | `AdminIamComplianceTest.builtInRoleCannotBeModifiedOrDeleted()` |
| 权限-账号管理 | 功能结构表 R61 / 规约 3.2.12 | 必选 | `AdminAccountController` | `admin/src/pages/iam/AccountPage.tsx` | `AdminIamComplianceTest.accountResponseDoesNotExposePasswordHash()` |
| 审计-操作日志查询 | 功能结构表 R62 / 规约 3.3.4 | 必选 | `AuditLogController` | `admin/src/pages/audit/AuditLogPage.tsx` | `AuditComplianceTest.auditHashChainDetectsTampering()` |
| 医务端-前台签到登记 | 功能结构表 R37 / 规约 3.2.7 | 必选 | `ExamController` | `admin/src/pages/exam/CheckInPage.tsx` | `MedicalWorkflowComplianceTest.checkInCreatesExamRecordGuideSheetAndProgress()` |
| 医务端-指引单管理 | 功能结构表 R38 / 规约 3.2.7 | 必选 | `ExamController` | `admin/src/pages/exam/GuideSheetPage.tsx` | `MedicalWorkflowComplianceTest.guideSheetContainsOrderedDepartments()` |
| 医务端-科室队列管理 | 功能结构表 R39 / 规约 3.2.7 | 必选 | `ExamController` | `admin/src/pages/exam/DepartmentQueuePage.tsx` | `MedicalWorkflowComplianceTest.departmentQueueSupportsCallSkipAndComplete()` |
| 医务端-体检进度跟踪 | 功能结构表 R40 / 规约 3.2.7 | 必选 | `ExamController` | `admin/src/pages/exam/ProgressPage.tsx` | `MedicalWorkflowComplianceTest.progressBlocksReportUntilRequiredItemsCompleted()` |
| 结果-结果录入 | 功能结构表 R43 / 规约 3.2.8 | 必选 | `ResultController` | `admin/src/pages/result/ResultEntryPage.tsx` | `ResultReviewComplianceTest.recordNumericResultDetectsAbnormalLevel()` |
| 结果-结果审核 | 功能结构表 R44 / 规约 3.2.8 | 必选 | `ResultController` | `admin/src/pages/result/ReviewPage.tsx` | `ResultReviewComplianceTest.reviewEndpointApprovesAndRejectsResult()` |
| 结果-批量审核 | 功能结构表 R45 / 规约 3.2.8 | 可选但当前保留 | `ResultController` | `admin/src/pages/result/BatchReviewPage.tsx` | `ResultReviewComplianceTest.batchReviewOnlyApprovesNormalPendingResults()` |
| 报告-模板配置 | 功能结构表 R47 / 规约 3.2.9 | 必选 | `AdminReportTemplateController` | `admin/src/pages/report/TemplatePage.tsx` | `ReportComplianceTest.templatePreviewUsesConfiguredContent()` |
| 报告-异常报告预警 | 功能结构表 R48 / 规约 3.2.9 | 必选 | `ReportController` | `admin/src/pages/report/ReportWarningPage.tsx` | `ReportComplianceTest.criticalReportIsMarkedAndRequiresDoctorPublish()` |
| 健康干预-高危人群管理 | 功能结构表 R50 / 规约 3.2.10 | 当前保留 | `HealthManagementController` | `admin/src/pages/management/RiskUserPage.tsx` | `HealthManagementComplianceTest.criticalReportCreatesHighRiskManagementRecord()` |
| 健康干预-随访与干预方案 | 功能结构表 R51 / 规约 3.2.10 | 当前保留 | `HealthManagementController` | `admin/src/pages/management/InterventionPage.tsx` | `HealthManagementComplianceTest.followUpPlanCreatesNotification()` |
| 统计-业务数据统计 | 功能结构表 R64 / 规约 3.2.13 | 当前保留 | `StatisticsController` | `admin/src/pages/statistics/BusinessStatisticsPage.tsx` | `StatisticsComplianceTest.businessStatisticsAreAggregatedAndAudited()` |
| 统计-健康数据统计 | 功能结构表 R65 / 规约 3.2.13 | 当前保留 | `StatisticsController` | `admin/src/pages/statistics/HealthStatisticsPage.tsx` | `StatisticsComplianceTest.healthStatisticsAreDesensitized()` |
| 统计-报表导出 | 功能结构表 R66 / 规约 3.2.13 | 当前保留 | `StatisticsController` | `admin/src/pages/statistics/ExportPage.tsx` | `StatisticsComplianceTest.exportSupportsBusinessAndHealthExcelPdf()` |
