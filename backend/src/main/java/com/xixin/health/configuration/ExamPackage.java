package com.xixin.health.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_exam_package")
public class ExamPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String packageCode;

    @Column(nullable = false, length = 128)
    private String packageName;

    @Column(length = 64)
    private String category;

    @Column(length = 16)
    private String genderLimit;

    @Column(length = 64)
    private String ageRange;

    @Column(nullable = false)
    private int priceCent;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int recommendWeight;

    @Column(nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private boolean hasFinalReview;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected ExamPackage() {
    }

    public static ExamPackage create(
        String packageCode, String packageName, String category,
        String genderLimit, String ageRange, int priceCent, boolean enabled
    ) {
        ExamPackage pkg = new ExamPackage();
        pkg.update(packageCode, packageName, category, genderLimit, ageRange, priceCent, enabled);
        return pkg;
    }

    public void update(
        String packageCode, String packageName, String category,
        String genderLimit, String ageRange, int priceCent, boolean enabled
    ) {
        this.packageCode = packageCode;
        this.packageName = packageName;
        this.category = category;
        this.genderLimit = genderLimit;
        this.ageRange = ageRange;
        this.priceCent = priceCent;
        this.enabled = enabled;
    }

    public void setRecommendWeight(int recommendWeight) {
        this.recommendWeight = recommendWeight;
    }

    public int getRecommendWeight() {
        return recommendWeight;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Long getId() {
        return id;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getPriceCent() {
        return priceCent;
    }

    public String getCategory() {
        return category;
    }

    public String getGenderLimit() {
        return genderLimit;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHasFinalReview() {
        return hasFinalReview;
    }

    public void setHasFinalReview(boolean hasFinalReview) {
        this.hasFinalReview = hasFinalReview;
    }
}
