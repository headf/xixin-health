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
@Table(name = "t_department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long branchId;

    @Column(nullable = false, length = 64)
    private String departmentCode;

    @Column(nullable = false, length = 128)
    private String departmentName;

    @Column(length = 128)
    private String location;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean fastingRequired;

    @Column(nullable = false)
    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Department() {
    }

    public static Department create(
        Long branchId,
        String departmentCode,
        String departmentName,
        String location,
        int capacity,
        boolean fastingRequired,
        boolean enabled
    ) {
        Department department = new Department();
        department.update(branchId, departmentCode, departmentName, location, capacity, fastingRequired, enabled);
        return department;
    }

    public void update(
        Long branchId,
        String departmentCode,
        String departmentName,
        String location,
        int capacity,
        boolean fastingRequired,
        boolean enabled
    ) {
        this.branchId = branchId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.location = location;
        this.capacity = capacity;
        this.fastingRequired = fastingRequired;
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

    public Long getBranchId() {
        return branchId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isFastingRequired() {
        return fastingRequired;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
