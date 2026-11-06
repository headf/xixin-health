package com.xixin.health.admin;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AdminComplianceSmokeTest {

    @Test
    void traceabilityMatrixListsRequiredManagementModules() throws Exception {
        String matrix = Files.readString(Path.of("../docs/compliance/admin-requirements-traceability.md"));

        assertThat(matrix)
            .contains("基础配置-体检项目管理")
            .contains("权限-角色管理")
            .contains("医务端-前台签到登记")
            .contains("结果-批量审核")
            .contains("报告-异常报告预警")
            .contains("健康干预-高危人群管理")
            .contains("统计-报表导出");
    }
}
