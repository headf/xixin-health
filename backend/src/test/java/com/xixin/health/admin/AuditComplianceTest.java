package com.xixin.health.admin;

import com.xixin.health.audit.AuditIntegrityService;
import com.xixin.health.audit.AuditService;
import com.xixin.health.audit.OperationLog;
import com.xixin.health.audit.OperationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({AuditService.class, AuditIntegrityService.class})
class AuditComplianceTest {

    @Autowired
    AuditService auditService;

    @Autowired
    AuditIntegrityService auditIntegrityService;

    @Autowired
    OperationLogRepository operationLogRepository;

    @Test
    void auditHashChainDetectsTampering() {
        OperationLog first = auditService.record("1", "LOGIN", "sys_user", "1", "SUCCESS", "127.0.0.1", "JUnit", "trace-1");
        OperationLog second = auditService.record("1", "CONFIG_UPDATE", "t_business_rule", "2", "SUCCESS", "127.0.0.1", "JUnit", "trace-2");

        assertThat(second.getPreviousHash()).isEqualTo(first.getEntryHash());
        assertThat(auditIntegrityService.verify()).isTrue();

        operationLogRepository.findById(second.getId()).orElseThrow().forceTamperForTest("FAILED");
        operationLogRepository.flush();

        assertThat(auditIntegrityService.verify()).isFalse();
    }
}
