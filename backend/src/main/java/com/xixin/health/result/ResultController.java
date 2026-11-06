package com.xixin.health.result;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/results")
public class ResultController {

    private final ResultService resultService;
    private final UserAccountRepository userAccountRepository;
    private final ExamRecordRepository examRecordRepository;

    public ResultController(ResultService resultService,
                            UserAccountRepository userAccountRepository,
                            ExamRecordRepository examRecordRepository) {
        this.resultService = resultService;
        this.userAccountRepository = userAccountRepository;
        this.examRecordRepository = examRecordRepository;
    }

    // ========== 数值型结果录入 (Doctor) ==========

    @PostMapping("/numeric")
    @PreAuthorize("hasRole('EXAM_DOCTOR')")
    public ApiResponse<CheckResult> recordNumeric(@Valid @RequestBody NumericResultRequest request) {
        return ApiResponse.success(resultService.recordNumericResult(
            request.examRecordId(), request.itemId(), request.itemName(),
            request.unit(), request.numericValue(), request.referenceMin(),
            request.referenceMax(), request.criticalMax(), request.criticalMin(), request.recorderId()));
    }

    // ========== 文本型结果录入 (Doctor) ==========

    @PostMapping("/text")
    @PreAuthorize("hasRole('EXAM_DOCTOR')")
    public ApiResponse<CheckResult> recordText(@Valid @RequestBody TextResultRequest request) {
        return ApiResponse.success(resultService.recordTextResult(
            request.examRecordId(), request.itemId(), request.itemName(),
            request.textValue(), request.recorderId()));
    }

    // ========== 数值型结果修改（留痕审计）=========

    @PutMapping("/numeric/{resultId}")
    @PreAuthorize("hasRole('EXAM_DOCTOR')")
    public ApiResponse<CheckResult> updateNumeric(@PathVariable Long resultId,
                                                   @Valid @RequestBody UpdateNumericResultRequest request) {
        return ApiResponse.success(resultService.updateNumericResult(
            resultId, request.numericValue(), request.referenceMin(),
            request.referenceMax(), request.criticalMax(), request.criticalMin(), request.operatorId()));
    }

    // ========== 文本型结果修改（留痕审计）=========

    @PutMapping("/text/{resultId}")
    @PreAuthorize("hasRole('EXAM_DOCTOR')")
    public ApiResponse<CheckResult> updateText(@PathVariable Long resultId,
                                                @Valid @RequestBody UpdateTextResultRequest request) {
        return ApiResponse.success(resultService.updateTextResult(
            resultId, request.textValue(), request.operatorId()));
    }

    // ========== 批量结果录入 (Doctor) ==========

    @PostMapping("/batch")
    @PreAuthorize("hasRole('EXAM_DOCTOR')")
    public ApiResponse<List<CheckResult>> recordBatch(@Valid @RequestBody BatchResultRequest request) {
        return ApiResponse.success(resultService.recordBatchResults(
            request.examRecordId(), request.recorderId(), request.items()));
    }

    // ========== 审核结果 (Review Doctor) ==========

    @PutMapping("/{resultId}/review")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'EXAM_DOCTOR')")
    public ApiResponse<Void> review(@PathVariable Long resultId,
                                     @Valid @RequestBody ReviewRequest request) {
        resultService.reviewResult(resultId, request.reviewerId(),
            request.approved(), request.comment());
        return ApiResponse.success(null);
    }

    // ========== 待审核列表 (Review Doctor) ==========

    @GetMapping("/pending-review")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'SYSTEM_ADMIN')")
    public ApiResponse<List<CheckResult>> pendingReview() {
        return ApiResponse.success(resultService.getPendingReviewResults());
    }

    @GetMapping("/pending-review/by-branch/{branchId}")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'SYSTEM_ADMIN', 'NURSE')")
    public ApiResponse<List<PendingReviewItem>> pendingReviewByBranch(@PathVariable Long branchId) {
        return ApiResponse.success(resultService.getPendingReviewByBranch(branchId));
    }

    @GetMapping("/pending-review/by-recorder")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'SYSTEM_ADMIN')")
    public ApiResponse<List<CheckResult>> pendingByRecorder(@RequestParam Long recorderId) {
        return ApiResponse.success(resultService.getPendingReviewByRecorder(recorderId));
    }

    @GetMapping("/by-exam/{examRecordId}")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'EXAM_DOCTOR')")
    public ApiResponse<List<CheckResult>> getByExamRecord(@PathVariable Long examRecordId) {
        return ApiResponse.success(resultService.getResultsByExamRecord(examRecordId));
    }

    @GetMapping("/public/by-exam/{examRecordId}")
    public ApiResponse<List<CheckResult>> getByExamRecordPublic(@PathVariable Long examRecordId) {
        return ApiResponse.success(resultService.getResultsByExamRecord(examRecordId));
    }

    // ========== 危急值预警专档 (Review Doctor) ==========

    @GetMapping("/critical")
    @PreAuthorize("hasAnyRole('REVIEW_DOCTOR', 'SYSTEM_ADMIN')")
    public ApiResponse<List<CriticalResultItem>> getCriticalResults() {
        List<CheckResult> critical = resultService.getCriticalResults();
        List<CriticalResultItem> items = critical.stream().map(r -> {
            String patientName = examRecordRepository.findById(r.getExamRecordId())
                .map(ExamRecord::getUserId)
                .flatMap(uid -> userAccountRepository.findById(uid))
                .map(UserAccount::getDisplayName)
                .orElse("未知");
            String examNo = examRecordRepository.findById(r.getExamRecordId())
                .map(ExamRecord::getExamNo)
                .orElse("");
            return new CriticalResultItem(
                r.getId(), r.getExamRecordId(), patientName, examNo,
                r.getItemId(), r.getItemName(), r.getUnit(),
                r.getNumericValue() != null ? r.getNumericValue().doubleValue() : null,
                r.getReferenceMin() != null ? r.getReferenceMin().doubleValue() : null,
                r.getReferenceMax() != null ? r.getReferenceMax().doubleValue() : null,
                r.getCriticalMax() != null ? r.getCriticalMax().doubleValue() : null,
                r.getCriticalMin() != null ? r.getCriticalMin().doubleValue() : null,
                r.getAbnormalLevel().name(), r.getReviewStatus().name(),
                r.getResultNo(), r.getCreatedAt() != null ? r.getCreatedAt().toString() : null
            );
        }).toList();
        return ApiResponse.success(items);
    }

    // ==================== Request DTOs ====================

    public record NumericResultRequest(
        @NotNull Long examRecordId,
        @NotNull Long itemId,
        @NotBlank String itemName,
        String unit,
        double numericValue,
        double referenceMin,
        double referenceMax,
        double criticalMax,
        double criticalMin,
        @NotNull Long recorderId
    ) {}

    public record TextResultRequest(
        @NotNull Long examRecordId,
        @NotNull Long itemId,
        @NotBlank String itemName,
        @NotBlank String textValue,
        @NotNull Long recorderId
    ) {}

    public record UpdateNumericResultRequest(
            double numericValue,
            double referenceMin,
            double referenceMax,
            double criticalMax,
            double criticalMin,
            @NotNull Long operatorId
    ) {}

    public record UpdateTextResultRequest(
            @NotBlank String textValue,
            @NotNull Long operatorId
    ) {}

    public record BatchResultRequest(
        @NotNull Long examRecordId,
        @NotNull Long recorderId,
        @NotNull List<ResultService.BatchResultItem> items
    ) {}

    public record ReviewRequest(
        @NotNull Long reviewerId,
        boolean approved,
        String comment
    ) {}

    public record CriticalResultItem(Long id, Long examRecordId, String patientName, String examNo,
                                     Long itemId, String itemName, String unit, Double numericValue,
                                     Double referenceMin, Double referenceMax, Double criticalMax, Double criticalMin,
                                     String abnormalLevel, String reviewStatus,
                                     String resultNo, String createdAt) {}
}
