package com.xixin.health.iam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "sys_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 32)
    private String roleType;

    @Column(nullable = false)
    private boolean systemBuiltIn;

    @Column(nullable = false)
    private boolean deleted;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sys_role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new LinkedHashSet<>();

    protected Role() {
    }

    public Role(String code, String name, String roleType, boolean systemBuiltIn) {
        this.code = code;
        this.name = name;
        this.roleType = roleType;
        this.systemBuiltIn = systemBuiltIn;
    }

    public static Role system(String code, String name, String roleType) {
        return new Role(code, name, roleType, true);
    }

    public void update(String code, String name, String roleType) {
        this.code = code;
        this.name = name;
        this.roleType = roleType;
    }

    public void replacePermissions(Collection<Permission> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getRoleType() {
        return roleType;
    }

    public boolean isSystemBuiltIn() {
        return systemBuiltIn;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
