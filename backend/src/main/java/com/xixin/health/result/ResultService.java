package com.xixin.health.result;

import com.xixin.health.audit.AuditService;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResultService {

    private final CheckResultRepository checkResultRepository;
    private final ResultReviewRepository resultReviewRepository;
    private final AuditService auditService;
    private final ExamRecordRepository examRecordRepository;
    private final UserAccountRepository userAccountRepository;
    private final CriticalAlertService criticalAlertService;

    public ResultService(CheckResultRepository checkResultRepository,
                         ResultReviewRepository resultReviewRepository,
                         AuditService auditService,
                         ExamRecordRepository examRecordRepository,
                         UserAccountRepository userAccountRepository,
                         CriticalAlertService criticalAlertService) {
        this.checkResultRepository = checkResultRepository;
        this.resultReviewRepository = resultReviewRepository;
        this.auditService = auditService;
        this.examRecordRepository = examRecordRepository;
        this.userAccountRepository = userAccountRepository;
        this.criticalAlertService = criticalAlertService;
    }

    // ========== 数值型结果录入 ==========

    @Transactional
    public CheckResult recordNumericResult(
        Long examRecordId, Long itemId, String itemName, String unit,
        double numericValue, double referenceMin, double referenceMax,
        double criticalMax, double criticalMin, Long recorderId
    ) {
        // 性别互斥校验
        validateGenderConflict(examRecordId, itemId, itemName);

        BigDecimal value = BigDecimal.valueOf(numericValue);
        BigDecimal bdCriticalMin = Double.isNaN(criticalMin) ? null : BigDecimal.valueOf(criticalMin);
        AbnormalLevel level = detectLevel(
            value, BigDecimal.valueOf(referenceMin),
            BigDecimal.valueOf(referenceMax), BigDecimal.valueOf(criticalMax), bdCriticalMin);
        CheckResult saved = checkResultRepository.save(new CheckResult(
            examRecordId, itemId, itemName, unit,
            value, BigDecimal.valueOf(referenceMin), BigDecimal.valueOf(referenceMax),
            BigDecimal.valueOf(criticalMax), bdCriticalMin, recorderId, level));

        // 危急值自动预警
        if (level == AbnormalLevel.CRITICAL) {
            Long userId = null;
            if (examRecordId != null) {
                userId = examRecordRepository.findById(examRecordId)
                        .map(ExamRecord::getUserId).orElse(null);
            }
            criticalAlertService.createAlert(saved.getId(), examRecordId, userId);
        }

        return saved;
    }

    // ========== 文本型结果录入 (Doctor) ==========

    @Transactional
    public CheckResult recordTextResult(
        Long examRecordId, Long itemId, String itemName, String textValue,
        Long recorderId
    ) {
        // 性别互斥校验
        validateGenderConflict(examRecordId, itemId, itemName);

        // 文本结果无法自动判断异常，默认为 NORMAL
        CheckResult result = new CheckResult(
            examRecordId, itemId, itemName, null,
            null, null, null, null, null, recorderId, AbnormalLevel.NORMAL);
        result.setTextValue(textValue);
        return checkResultRepository.save(result);
    }

    // ========== 数值型结果修改（留痕审计）=========

    @Transactional
    public CheckResult updateNumericResult(
            Long resultId, double numericValue, double referenceMin, double referenceMax,
            double criticalMax, double criticalMin, Long operatorId
    ) {
        CheckResult result = checkResultRepository.findById(resultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Check result not found"));

        BigDecimal bdCriticalMin = Double.isNaN(criticalMin) ? null : BigDecimal.valueOf(criticalMin);

        // 记录旧值到审计日志
        String oldValue = String.format("numericValue=%s, referenceMin=%s, referenceMax=%s, criticalMax=%s, criticalMin=%s",
                result.getNumericValue(), result.getReferenceMin(),
                result.getReferenceMax(), result.getCriticalMax(), result.getCriticalMin());

        BigDecimal newVal = BigDecimal.valueOf(numericValue);
        BigDecimal newRefMin = BigDecimal.valueOf(referenceMin);
        BigDecimal newRefMax = BigDecimal.valueOf(referenceMax);
        BigDecimal newCritMax = BigDecimal.valueOf(criticalMax);

        // 自动判断异常级别（同时检测上下限危急值）
        AbnormalLevel level = detectLevel(newVal, newRefMin, newRefMax, newCritMax, bdCriticalMin);

        result.setNumericValue(newVal);
        result.setReferenceMin(newRefMin);
        result.setReferenceMax(newRefMax);
        result.setCriticalMax(newCritMax);
        result.setCriticalMin(bdCriticalMin);
        result.setAbnormalLevel(level);
        result.setReviewStatus(ReviewStatus.PENDING);
        CheckResult saved = checkResultRepository.save(result);

        // 审计日志记录修改
        String newValue = String.format("numericValue=%s, referenceMin=%s, referenceMax=%s, criticalMax=%s, criticalMin=%s",
                saved.getNumericValue(), saved.getReferenceMin(),
                saved.getReferenceMax(), saved.getCriticalMax(), saved.getCriticalMin());
        auditService.record(
                String.valueOf(operatorId),
                "UPDATE_NUMERIC_RESULT",
                "CheckResult",
                String.valueOf(resultId),
                "OLD:[" + oldValue + "] -> NEW:[" + newValue + "]",
                null, null, null
        );

        return saved;
    }

    // ========== 文本型结果修改（留痕审计）=========

    @Transactional
    public CheckResult updateTextResult(
            Long resultId, String textValue, Long operatorId
    ) {
        CheckResult result = checkResultRepository.findById(resultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Check result not found"));

        // 记录旧值到审计日志
        String oldValue = result.getTextValue();

        result.setTextValue(textValue);
        result.setReviewStatus(ReviewStatus.PENDING);
        CheckResult saved = checkResultRepository.save(result);

        // 审计日志记录修改
        auditService.record(
                String.valueOf(operatorId),
                "UPDATE_TEXT_RESULT",
                "CheckResult",
                String.valueOf(resultId),
                "OLD:[" + oldValue + "] -> NEW:[" + saved.getTextValue() + "]",
                null, null, null
        );

        return saved;
    }

    // ========== 批量结果录入 (Doctor) ==========

    @Transactional
    public List<CheckResult> recordBatchResults(Long examRecordId, Long recorderId,
                                                 List<BatchResultItem> items) {
        List<CheckResult> results = items.stream()
            .map(item -> {
                if (item.textValue() != null && !item.textValue().isBlank()) {
                    // 文本型
                    CheckResult r = new CheckResult(
                        examRecordId, item.itemId(), item.itemName(), null,
                        null, null, null, null, null, recorderId, AbnormalLevel.NORMAL);
                    r.setTextValue(item.textValue());
                    return r;
                } else {
                    // 数值型
                    BigDecimal value = BigDecimal.valueOf(item.numericValue());
                    AbnormalLevel level = detectLevel(
                        value,
                        item.referenceMin() != null ? item.referenceMin() : BigDecimal.ZERO,
                        item.referenceMax() != null ? item.referenceMax() : BigDecimal.ZERO,
                        item.criticalMax(),
                        item.criticalMin());
                    return new CheckResult(
                        examRecordId, item.itemId(), item.itemName(), item.unit(),
                        value, item.referenceMin(), item.referenceMax(),
                        item.criticalMax(), item.criticalMin(), recorderId, level);
                }
            })
            .toList();
        return checkResultRepository.saveAll(results);
    }

    public record BatchResultItem(
        Long itemId,
        String itemName,
        String unit,
        Double numericValue,
        BigDecimal referenceMin,
        BigDecimal referenceMax,
        BigDecimal criticalMax,
        BigDecimal criticalMin,
        String textValue
    ) {}

    // ========== 结果审核 (Doctor / Review Doctor) ==========

    @Transactional
    public void reviewResult(Long resultId, Long reviewerId, boolean approved, String comment) {
        CheckResult result = checkResultRepository.findById(resultId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Check result not found"));
        if (approved) {
            result.approve();
        } else {
            result.reject();
        }
        checkResultRepository.save(result);
        resultReviewRepository.save(new ResultReview(resultId, reviewerId, approved, comment));
    }

    // ========== 待审核列表 (Review Doctor) ==========

    @Transactional(readOnly = true)
    public List<CheckResult> getPendingReviewResults() {
        return checkResultRepository.findByReviewStatus(ReviewStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<PendingReviewItem> getPendingReviewByBranch(Long branchId) {
        // 查出该分院的所有体检记录
        List<ExamRecord> branchRecords = examRecordRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
        if (branchRecords.isEmpty()) {
            return new ArrayList<>();
        }
        // 提取体检记录ID
        List<Long> recordIds = branchRecords.stream()
            .map(ExamRecord::getId)
            .toList();
        // 查出这些体检记录中待审核的结果
        List<CheckResult> pendingResults = checkResultRepository
            .findByExamRecordIdInAndReviewStatus(recordIds, ReviewStatus.PENDING);

        // 构建返回DTO，包含患者姓名
        // 先构建 examRecordId -> patientName 映射
        java.util.Map<Long, String> patientNameMap = new java.util.HashMap<>();
        java.util.Map<Long, Long> branchIdMap = new java.util.HashMap<>();
        for (ExamRecord er : branchRecords) {
            branchIdMap.put(er.getId(), er.getBranchId());
            if (er.getUserId() != null && !patientNameMap.containsKey(er.getId())) {
                userAccountRepository.findById(er.getUserId())
                    .ifPresent(u -> patientNameMap.put(er.getId(), u.getDisplayName()));
            }
        }

        List<PendingReviewItem> items = new ArrayList<>();
        for (CheckResult r : pendingResults) {
            items.add(new PendingReviewItem(
                r.getId(), r.getResultNo(), r.getExamRecordId(),
                patientNameMap.getOrDefault(r.getExamRecordId(), "#" + r.getExamRecordId()),
                branchIdMap.getOrDefault(r.getExamRecordId(), branchId),
                r.getItemId(), r.getItemName(), r.getUnit(),
                r.getNumericValue(), r.getTextValue(),
                r.getReferenceMin(), r.getReferenceMax(),
                r.getCriticalMax(),
                r.getAbnormalLevel() != null ? r.getAbnormalLevel().name() : null,
                r.getReviewStatus() != null ? r.getReviewStatus().name() : null,
                r.getRecorderId()
            ));
        }
        return items;
    }

    @Transactional(readOnly = true)
    public List<CheckResult> getPendingReviewByRecorder(Long recorderId) {
        return checkResultRepository.findByReviewStatusAndRecorderId(ReviewStatus.PENDING, recorderId);
    }

    @Transactional(readOnly = true)
    public List<CheckResult> getResultsByExamRecord(Long examRecordId) {
        return checkResultRepository.findByExamRecordId(examRecordId);
    }

    @Transactional(readOnly = true)
    public List<CheckResult> getCriticalResults() {
        return checkResultRepository.findByAbnormalLevel(AbnormalLevel.CRITICAL);
    }

    // ========== 性别互斥校验 ==========

    private void validateGenderConflict(Long examRecordId, Long itemId, String itemName) {
        if (examRecordId == null) return;
        ExamRecord examRecord = examRecordRepository.findById(examRecordId).orElse(null);
        if (examRecord == null) return;
        UserAccount user = userAccountRepository.findById(examRecord.getUserId()).orElse(null);
        if (user == null) return;
        String gender = user.getGender();
        if (gender == null) return;

        if ("MALE".equals(gender)) {
            if ((itemId != null && itemId == 25L) || (itemName != null && itemName.contains("子宫"))) {
                throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "男性不能录入子宫附件相关检查");
            }
        }
        if ("FEMALE".equals(gender)) {
            if ((itemId != null && itemId == 24L) || (itemName != null && itemName.contains("前列腺"))) {
                throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "女性不能录入前列腺相关检查");
            }
        }
    }

    // ========== 异常检测逻辑（支持上下限危急值）==========

    private AbnormalLevel detectLevel(BigDecimal value, BigDecimal referenceMin,
                                       BigDecimal referenceMax, BigDecimal criticalMax,
                                       BigDecimal criticalMin) {
        if (value == null) return AbnormalLevel.NORMAL;
        // 上限危急值检测（值过高）
        if (criticalMax != null && value.compareTo(criticalMax) > 0) {
            return AbnormalLevel.CRITICAL;
        }
        // 下限危急值检测（值过低，如血糖、血钾等）
        if (criticalMin != null && value.compareTo(criticalMin) < 0) {
            return AbnormalLevel.CRITICAL;
        }
        if (value.compareTo(referenceMin) < 0 || value.compareTo(referenceMax) > 0) {
            return AbnormalLevel.ABNORMAL;
        }
        return AbnormalLevel.NORMAL;
    }
}
