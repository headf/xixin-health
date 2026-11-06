package com.xixin.health.result;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    List<CheckResult> findByExamRecordId(Long examRecordId);

    List<CheckResult> findByReviewStatus(ReviewStatus reviewStatus);

    List<CheckResult> findByReviewStatusAndRecorderId(ReviewStatus reviewStatus, Long recorderId);

    List<CheckResult> findByExamRecordIdAndItemId(Long examRecordId, Long itemId);

    long countByExamRecordIdAndReviewStatus(Long examRecordId, ReviewStatus reviewStatus);

    List<CheckResult> findByAbnormalLevel(AbnormalLevel abnormalLevel);

    List<CheckResult> findByExamRecordIdInAndReviewStatus(List<Long> examRecordIds, ReviewStatus reviewStatus);
}
