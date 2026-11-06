package com.xixin.health.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuditService {

    private final OperationLogRepository operationLogRepository;

    public AuditService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Transactional
    public OperationLog record(
        String operatorId,
        String operationType,
        String targetType,
        String targetId,
        String result,
        String ipAddress,
        String userAgent,
        String traceId
    ) {
        OperationLog log = new OperationLog(
            operatorId,
            operationType,
            targetType,
            targetId,
            result,
            ipAddress,
            userAgent,
            traceId
        );
        String previousHash = operationLogRepository.findTopByOrderByIdDesc()
            .map(OperationLog::getEntryHash)
            .orElse("");
        log.linkPreviousHash(previousHash);
        log.seal(sha256(log.hashPayload()));
        return operationLogRepository.save(log);
    }

    static String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash audit payload", exception);
        }
    }
}
