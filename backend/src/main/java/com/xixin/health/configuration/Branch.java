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
@Table(name = "t_branch")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String branchCode;

    @Column(nullable = false, length = 128)
    private String branchName;

    @Column(length = 256)
    private String address;

    @Column(length = 32)
    private String servicePhone;

    @Column(nullable = false)
    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Branch() {
    }

    public static Branch create(
        String branchCode,
        String branchName,
        String address,
        String servicePhone,
        boolean enabled
    ) {
        Branch branch = new Branch();
        branch.update(branchCode, branchName, address, servicePhone, enabled);
        return branch;
    }

    public void update(
        String branchCode,
        String branchName,
        String address,
        String servicePhone,
        boolean enabled
    ) {
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.address = address;
        this.servicePhone = servicePhone;
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

    public String getBranchCode() {
        return branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAddress() {
        return address;
    }

    public String getServicePhone() {
        return servicePhone;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
