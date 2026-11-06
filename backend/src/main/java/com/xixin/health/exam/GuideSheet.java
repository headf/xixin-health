package com.xixin.health.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_guide_sheet")
public class GuideSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examRecordId;

    @Column(nullable = false, unique = true, length = 64)
    private String guideNo;

    @Column(columnDefinition = "TEXT")
    private String routeText;

    @Column(nullable = false)
    private boolean fastingDone = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected GuideSheet() {
    }

    public static GuideSheet create(Long examRecordId, String guideNo, String routeText) {
        GuideSheet sheet = new GuideSheet();
        sheet.examRecordId = examRecordId;
        sheet.guideNo = guideNo;
        sheet.routeText = routeText;
        return sheet;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getExamRecordId() {
        return examRecordId;
    }

    public String getGuideNo() {
        return guideNo;
    }

    public String getRouteText() {
        return routeText;
    }

    public boolean isFastingDone() {
        return fastingDone;
    }

    public void setFastingDone(boolean fastingDone) {
        this.fastingDone = fastingDone;
    }
}
