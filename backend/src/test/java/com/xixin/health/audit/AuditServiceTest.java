package com.xixin.health.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(AuditService.class)
class AuditServiceTest {

    @Autowired
    AuditService auditService;

    @Autowired
    OperationLogRepository operationLogRepository;

    @Test
    void recordsOperationLogWithTraceId() {
        auditService.record("u-1", "LOGIN", "sys_user", "u-1", "SUCCESS", "127.0.0.1", "JUnit", "trace-1");

        assertThat(operationLogRepository.findAll()).hasSize(1);
        OperationLog log = operationLogRepository.findAll().get(0);
        assertThat(log.getOperationType()).isEqualTo("LOGIN");
        assertThat(log.getTraceId()).isEqualTo("trace-1");
    }
}
