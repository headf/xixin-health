package com.xixin.health.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_flow_node")
public class FlowNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String nodeCode;

    @Column(nullable = false, length = 128)
    private String nodeName;

    @Column(nullable = false, length = 64)
    private String nodeType;

    @Column(nullable = false)
    private int sortNo;

    @Column(length = 128)
    private String requiredPermission;

    @Column(length = 64)
    private String approvalRoleCode;

    @Column(nullable = false)
    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected FlowNode() {
    }

    public static FlowNode create(
        String nodeCode,
        String nodeName,
        String nodeType,
        int sortNo,
        String requiredPermission,
        String approvalRoleCode,
        boolean enabled
    ) {
        FlowNode node = new FlowNode();
        node.update(nodeCode, nodeName, nodeType, sortNo, requiredPermission, approvalRoleCode, enabled);
        return node;
    }

    public void update(
        String nodeCode,
        String nodeName,
        String nodeType,
        int sortNo,
        String requiredPermission,
        String approvalRoleCode,
        boolean enabled
    ) {
        this.nodeCode = nodeCode;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.sortNo = sortNo;
        this.requiredPermission = requiredPermission;
        this.approvalRoleCode = approvalRoleCode;
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public int getSortNo() {
        return sortNo;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getApprovalRoleCode() {
        return approvalRoleCode;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
