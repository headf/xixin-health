package com.xixin.health.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime operationTime;

    @Column(length = 64)
    private String operatorId;

    @Column(nullable = false, length = 64)
    private String operationType;

    @Column(nullable = false, length = 64)
    private String targetType;

    @Column(length = 64)
    private String targetId;

    @Column(nullable = false, length = 32)
    private String result;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 256)
    private String userAgent;

    @Column(length = 128)
    private String traceId;

    @Column(length = 128)
    private String previousHash;

    @Column(length = 512)
    private String detail;

    @Column(nullable = false, length = 128)
    private String entryHash = "";

    protected OperationLog() {
    }

    public OperationLog(
        String operatorId,
        String operationType,
        String targetType,
        String targetId,
        String result,
        String ipAddress,
        String userAgent,
        String traceId
    ) {
        this.operationTime = LocalDateTime.now();
        this.operatorId = operatorId;
        this.operationType = operationType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.result = result;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.traceId = traceId;
    }

    public void linkPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public void seal(String entryHash) {
        this.entryHash = entryHash;
    }

    String hashPayload() {
        return value(operatorId)
            + "|" + value(operationType)
            + "|" + value(targetType)
            + "|" + value(targetId)
            + "|" + value(result)
            + "|" + value(ipAddress)
            + "|" + value(userAgent)
            + "|" + value(traceId)
            + "|" + value(previousHash);
    }

    public void forceTamperForTest(String result) {
        this.result = result;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getResult() {
        return result;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getEntryHash() {
        return entryHash;
    }

    public String getDetail() {
        return detail;
    }
}
