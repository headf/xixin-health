package com.xixin.health.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mapping table: which items belong to which package.
 * t_exam_package_item
 */
@Entity
@Table(name = "t_exam_package_item")
public class PackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long packageId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private int sortNo;

    protected PackageItem() {}

    public Long getId() { return id; }
    public Long getPackageId() { return packageId; }
    public Long getItemId() { return itemId; }
    public int getSortNo() { return sortNo; }
}
