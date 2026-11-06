package com.xixin.health.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditIntegrityService {

    private final OperationLogRepository operationLogRepository;

    public AuditIntegrityService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Transactional(readOnly = true)
    public boolean verify() {
        String previousHash = "";
        for (OperationLog log : operationLogRepository.findAllByOrderByIdAsc()) {
            if (!previousHash.equals(value(log.getPreviousHash()))) {
                return false;
            }
            String expectedHash = AuditService.sha256(log.hashPayload());
            if (!expectedHash.equals(log.getEntryHash())) {
                return false;
            }
            previousHash = log.getEntryHash();
        }
        return true;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
