package com.xixin.health.result;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_result_review")
public class ResultReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long resultId;

    @Column(nullable = false)
    private Long reviewerId;

    @Column(nullable = false)
    private boolean approved;

    @Column(length = 512)
    private String reviewComment;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    protected ResultReview() {
    }

    public ResultReview(Long resultId, Long reviewerId, boolean approved, String reviewComment) {
        this.resultId = resultId;
        this.reviewerId = reviewerId;
        this.approved = approved;
        this.reviewComment = reviewComment;
        this.reviewedAt = LocalDateTime.now();
    }
}
