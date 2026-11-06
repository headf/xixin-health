package com.xixin.health.exam;

import com.xixin.health.appointment.Appointment;
import com.xixin.health.appointment.AppointmentService;
import com.xixin.health.appointment.AppointmentStatus;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.configuration.Department;
import com.xixin.health.configuration.DepartmentRepository;
import com.xixin.health.configuration.ExamPackage;
import com.xixin.health.configuration.ExamPackageItem;
import com.xixin.health.configuration.ExamItem;
import com.xixin.health.result.CheckResult;
import com.xixin.health.result.CheckResultRepository;
import com.xixin.health.result.AbnormalLevel;
import com.xixin.health.result.ReviewStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@Service
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);

    private final AppointmentService appointmentService;
    private final ExamRecordRepository examRecordRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final DepartmentQueueRepository departmentQueueRepository;
    private final ExamProgressRepository examProgressRepository;
    private final SpecimenRepository specimenRepository;
    private final ExamCompletionRepository examCompletionRepository;
    private final com.xixin.health.configuration.PackageItemRepository packageItemRepository;
    private final com.xixin.health.configuration.ExamItemRepository examItemRepository;
    private final com.xixin.health.configuration.ExamPackageRepository examPackageRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentQueueService departmentQueueService;
    private final JdbcTemplate jdbc;
    private final CheckResultRepository checkResultRepository;
    private final com.xixin.health.configuration.ExamPackageItemRepository examPackageItemRepository;

    public ExamService(AppointmentService appointmentService,
                       ExamRecordRepository examRecordRepository,
                       VitalSignsRepository vitalSignsRepository,
                       DepartmentQueueRepository departmentQueueRepository,
                       ExamProgressRepository examProgressRepository,
                       SpecimenRepository specimenRepository,
                       ExamCompletionRepository examCompletionRepository,
                       com.xixin.health.configuration.PackageItemRepository packageItemRepository,
                       com.xixin.health.configuration.ExamItemRepository examItemRepository,
                       com.xixin.health.configuration.ExamPackageRepository examPackageRepository,
                       DepartmentRepository departmentRepository,
                       DepartmentQueueService departmentQueueService,
                       JdbcTemplate jdbc,
                       CheckResultRepository checkResultRepository,
                       com.xixin.health.configuration.ExamPackageItemRepository examPackageItemRepository) {
        this.appointmentService = appointmentService;
        this.examRecordRepository = examRecordRepository;
        this.vitalSignsRepository = vitalSignsRepository;
        this.departmentQueueRepository = departmentQueueRepository;
        this.examProgressRepository = examProgressRepository;
        this.specimenRepository = specimenRepository;
        this.examCompletionRepository = examCompletionRepository;
        this.packageItemRepository = packageItemRepository;
        this.examItemRepository = examItemRepository;
        this.examPackageRepository = examPackageRepository;
        this.departmentRepository = departmentRepository;
        this.departmentQueueService = departmentQueueService;
        this.jdbc = jdbc;
        this.checkResultRepository = checkResultRepository;
        this.examPackageItemRepository = examPackageItemRepository;
    }

    // ========== 签到登记 (Nurse) ==========

    @Transactional
    public ExamRecord checkIn(Long appointmentId, Long userId, Long profileId, Long branchId) {
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.PAID) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "预约必须先支付才能签到，当前状态：" + appointment.getStatus());
        }
        // 只能签到当天的预约
        if (!appointment.getAppointmentDate().equals(java.time.LocalDate.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "只能签到当天的预约（预约日期：" + appointment.getAppointmentDate() + "，今天：" + java.time.LocalDate.now() + "）");
        }
        // 确保使用预约中关联的就诊人 profile，而非前端传入的可能不一致的 profileId
        Long effectiveProfileId = appointment.getProfileId();
        if (effectiveProfileId == null) {
            effectiveProfileId = profileId; // 降级：使用请求中的 profileId
        }
        if (!effectiveProfileId.equals(profileId)) {
            log.warn("签到 profileId 不匹配：预约 profileId={}，请求 profileId={}，已自动修正为预约的 profileId",
                effectiveProfileId, profileId);
        }
        String examNo = "EX" + System.currentTimeMillis();
        ExamRecord record = new ExamRecord(examNo, appointmentId, userId, effectiveProfileId, branchId);

        // Set package info from appointment
        Long packageId = appointment.getPackageId();
        record.setPackageId(packageId);
        final ExamRecord finalRecord = record;
        examPackageRepository.findById(packageId).ifPresent(pkg -> finalRecord.setPackageName(pkg.getPackageName()));

        record.checkIn();
        appointment.markCheckedIn();
        ExamRecord saved = examRecordRepository.save(record);

        // 生成条码：BR + 日期(8位) + 流水号(4位)
        String barcode = "BR" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
            + String.format("%04d", saved.getId());
        saved.setBarcode(barcode);
        examRecordRepository.save(saved);

        // Auto-init progress from package items
        autoInitProgress(saved, packageId);

        return saved;
    }

    /** Look up all items in the package and create PENDING progress for each. */
    private void autoInitProgress(ExamRecord record, Long packageId) {
        List<com.xixin.health.configuration.PackageItem> pkgItems = packageItemRepository.findByPackageIdOrderBySortNo(packageId);
        if (pkgItems.isEmpty()) return;

        List<Long> itemIds = pkgItems.stream().map(com.xixin.health.configuration.PackageItem::getItemId).toList();
        List<com.xixin.health.configuration.ExamItem> examItems = examItemRepository.findByIdIn(itemIds);
        java.util.Map<Long, Long> itemDeptMap = examItems.stream()
            .collect(java.util.stream.Collectors.toMap(
                com.xixin.health.configuration.ExamItem::getId,
                ei -> ei.getDepartmentId() != null ? ei.getDepartmentId() : 0L
            ));

        List<ExamProgress> progresses = pkgItems.stream()
            .map(pi -> new ExamProgress(record.getId(), pi.getItemId(), itemDeptMap.getOrDefault(pi.getItemId(), 0L)))
            .toList();
        examProgressRepository.saveAll(progresses);
    }

    @Transactional(readOnly = true)
    public ExamRecord getExamRecord(Long id) {
        return examRecordRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "体检记录不存在"));
    }

    @Transactional(readOnly = true)
    public List<ExamRecord> getExamRecordsByBranch(Long branchId) {
        return examRecordRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
    }

    @Transactional(readOnly = true)
    public List<ExamRecord> getCheckedInRecords(Long branchId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime start = today.atStartOfDay();
        java.time.LocalDateTime end = today.plusDays(1).atStartOfDay();
        return examRecordRepository.findByBranchIdAndStatusInAndCheckedInAtBetweenOrderByCheckedInAtAsc(
            branchId, List.of(ExamStatus.CHECKED_IN, ExamStatus.COMPLETED),
            start, end);
    }

    @Transactional(readOnly = true)
    public List<ExamRecord> getCompletedRecords(Long branchId) {
        return examRecordRepository.findByBranchIdAndStatusOrderByCheckedInAtAsc(branchId, ExamStatus.COMPLETED);
    }

    // ========== 生命体征测量 (Nurse) ==========

    @Transactional
    public VitalSigns recordVitalSigns(Long examRecordId, Long recordedBy,
                                       BigDecimal height, BigDecimal weight,
                                       Integer systolic, Integer diastolic,
                                       Integer heartRate, BigDecimal temperature,
                                       BigDecimal visionLeft, BigDecimal visionRight,
                                       BigDecimal waistline) {
        ExamRecord record = getExamRecord(examRecordId);
        if (record.getStatus() == ExamStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "体检已完成，无法重复操作");
        }

        VitalSigns vs = vitalSignsRepository.findByExamRecordId(examRecordId)
            .orElse(new VitalSigns(examRecordId, recordedBy));

        vs.setHeight(height);
        vs.setWeight(weight);
        vs.setBloodPressureSystolic(systolic);
        vs.setBloodPressureDiastolic(diastolic);
        vs.setHeartRate(heartRate);
        vs.setTemperature(temperature);
        vs.setVisionLeft(visionLeft);
        vs.setVisionRight(visionRight);
        vs.setWaistline(waistline);

        // Auto-calculate BMI
        vs.calculateAndSetBmi();

        // 保存体征后自动完成一般检查项目进度（身高/体重/BMI/收缩压/舒张压）
        List<Long> vitalItemIds = List.of(1L, 2L, 3L, 4L, 5L);
        List<ExamProgress> vitalProgress = examProgressRepository.findByExamRecordId(examRecordId)
            .stream().filter(p -> vitalItemIds.contains(p.getItemId()) && !"COMPLETED".equals(p.getProgressStatus()))
            .toList();
        for (ExamProgress p : vitalProgress) {
            p.markCompleted();
            examProgressRepository.save(p);
        }

        return vitalSignsRepository.save(vs);
    }

    @Transactional(readOnly = true)
    public VitalSigns getVitalSigns(Long examRecordId) {
        return vitalSignsRepository.findByExamRecordId(examRecordId)
            .orElse(null);
    }

    // ========== 科室排队叫号 (Nurse / Doctor) ==========

    @Transactional
    public DepartmentQueue addToQueue(Long examRecordId, Long departmentId) {
        // 1. Check vital signs exist
        if (!vitalSignsRepository.existsByExamRecordId(examRecordId)) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "患者必须先测量生命体征后才能排队");
        }
        // 2. Check if already queued in this specific department (prevent duplicate)
        if (departmentQueueRepository.findByExamRecordIdAndDepartmentId(examRecordId, departmentId).isPresent()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "已在当前科室排队中，请勿重复排队");
        }
        long count = departmentQueueRepository.countByDepartmentIdAndQueueStatus(departmentId, "WAITING");

        // 3. 空腹优先排队：若科室要求空腹，排队号前缀改为 "F-" 以优先排检
        Department department = departmentRepository.findById(departmentId).orElse(null);
        boolean fasting = department != null && department.isFastingRequired();
        String queueNo = fasting ? "F-" + (count + 1) : departmentId + "-" + (count + 1);

        // 4. 获取预约时段（通过体检记录关联预约）
        String timeSlot = null;
        ExamRecord record = examRecordRepository.findById(examRecordId).orElse(null);
        if (record != null) {
            try {
                var appointment = appointmentService.getById(record.getAppointmentId());
                if (appointment != null) {
                    timeSlot = appointment.getTimeSlot();
                }
            } catch (Exception e) {
                log.warn("获取预约时段失败: examRecordId={}", examRecordId, e);
            }
        }

        return departmentQueueRepository.save(new DepartmentQueue(examRecordId, departmentId, queueNo, timeSlot));
    }

    @Transactional(readOnly = true)
    public List<DepartmentQueue> getQueueByDepartment(Long departmentId) {
        return departmentQueueRepository.findByDepartmentIdOrderByCreatedAt(departmentId);
    }

    @Transactional(readOnly = true)
    public List<DepartmentQueue> getWaitingQueue(Long departmentId) {
        return departmentQueueRepository.findByDepartmentIdAndQueueStatusOrderByCreatedAt(departmentId, "WAITING");
    }

    @Transactional(readOnly = true)
    public long getWaitingCount(Long departmentId) {
        return departmentQueueRepository.countByDepartmentIdAndQueueStatus(departmentId, "WAITING");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDepartmentStats(Long departmentId) {
        long waiting = departmentQueueRepository.countByDepartmentIdAndQueueStatus(departmentId, "WAITING");
        long completed = departmentQueueRepository.countByDepartmentIdAndQueueStatus(departmentId, "COMPLETED");
        return Map.of("waiting", waiting, "completed", completed);
    }

    @Transactional(readOnly = true)
    public Map<Long, Map<String, Object>> getAllDepartmentStats() {
        Map<Long, Map<String, Object>> stats = new java.util.LinkedHashMap<>();
        // 动态获取所有科室ID，避免硬编码
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            stats.put(dept.getId(), getDepartmentStats(dept.getId()));
        }
        return stats;
    }

    @Transactional(readOnly = true)
    public List<DepartmentQueue> getTodayQueueRegistrations() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return departmentQueueRepository.findByCreatedAtAfterOrderByCreatedAtDesc(todayStart);
    }

    @Transactional
    public DepartmentQueue callNext(Long departmentId) {
        return departmentQueueService.callNext(departmentId);
    }

    @Transactional
    public DepartmentQueue markArrived(Long queueId) {
        DepartmentQueue entry = departmentQueueRepository.findById(queueId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "排队记录不存在"));
        entry.setQueueStatus(DepartmentQueue.QueueStatus.ARRIVED);
        return departmentQueueRepository.save(entry);
    }

    @Transactional
    public DepartmentQueue completeQueue(Long queueId) {
        DepartmentQueue entry = departmentQueueRepository.findById(queueId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "排队记录不存在"));
        entry.setQueueStatus(DepartmentQueue.QueueStatus.COMPLETED);
        return departmentQueueRepository.save(entry);
    }

    @Transactional
    public DepartmentQueue completeQueueByExamAndDept(Long examRecordId, Long departmentId) {
        DepartmentQueue entry = departmentQueueRepository
            .findByExamRecordIdAndDepartmentId(examRecordId, departmentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "未找到体检记录(" + examRecordId + ")在科室(" + departmentId + ")的排队记录"));
        entry.setQueueStatus(DepartmentQueue.QueueStatus.COMPLETED);
        return departmentQueueRepository.save(entry);
    }

    @Transactional
    public DepartmentQueue reQueue(Long queueId) {
        DepartmentQueue queue = departmentQueueRepository.findById(queueId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "排队记录不存在"));
        String status = queue.getQueueStatus();
        if (!DepartmentQueue.QueueStatus.SKIPPED.name().equals(status)
            && !DepartmentQueue.QueueStatus.COMPLETED.name().equals(status)) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "只有已跳过或已完成的排队才能重新排队");
        }
        queue.setQueueStatus(DepartmentQueue.QueueStatus.WAITING);
        return departmentQueueRepository.save(queue);
    }

    // ========== 检查进度管理 (Doctor / Nurse) ==========

    @Transactional
    public List<ExamProgress> initProgress(Long examRecordId, List<Long> itemIds, Long departmentId) {
        List<ExamProgress> existing = examProgressRepository.findByExamRecordId(examRecordId);
        if (!existing.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "该体检的进度已初始化，请勿重复操作");
        }
        List<ExamProgress> progresses = itemIds.stream()
            .map(itemId -> new ExamProgress(examRecordId, itemId, departmentId))
            .toList();
        return examProgressRepository.saveAll(progresses);
    }

    @Transactional(readOnly = true)
    public List<ExamProgressVO> getExamProgress(Long examRecordId) {
        List<ExamProgress> list = examProgressRepository.findByExamRecordId(examRecordId);
        Map<Long, String> itemNameMap = examItemRepository.findByIdIn(
            list.stream().map(ExamProgress::getItemId).toList()
        ).stream().collect(java.util.stream.Collectors.toMap(
            com.xixin.health.configuration.ExamItem::getId,
            com.xixin.health.configuration.ExamItem::getItemName,
            (a, b) -> a
        ));
        return list.stream().map(p -> new ExamProgressVO(
            p.getId(), p.getExamRecordId(), p.getItemId(),
            itemNameMap.getOrDefault(p.getItemId(), "项目#" + p.getItemId()),
            p.getDepartmentId(), p.getProgressStatus(), p.getCompletedAt()
        )).toList();
    }

    public record ExamProgressVO(
        Long id, Long examRecordId, Long itemId, String itemName,
        Long departmentId, String progressStatus, LocalDateTime completedAt
    ) {}

    @Transactional(readOnly = true)
    public List<ExamProgress> getDepartmentPendingProgress(Long departmentId) {
        return examProgressRepository.findByDepartmentIdAndProgressStatus(departmentId, "PENDING");
    }

    @Transactional
    public ExamProgress updateProgressStatus(Long progressId, String action) {
        ExamProgress progress = examProgressRepository.findById(progressId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "检查进度记录不存在"));
        switch (action.toUpperCase()) {
            case "START" -> progress.markInProgress();
            case "COMPLETE" -> {
                progress.markCompleted();
                // 检查该科室的所有项目是否都已完成，若是则同步更新队列状态
                autoCompleteQueue(progress.getExamRecordId(), progress.getDepartmentId());
            }
            case "SKIP" -> progress.markSkipped();
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "无效的操作：" + action);
        }
        return examProgressRepository.save(progress);
    }

    private void autoCompleteQueue(Long examRecordId, Long departmentId) {
        if (departmentId == null || departmentId <= 0) return;
        List<ExamProgress> deptProgress = examProgressRepository
            .findByExamRecordIdAndDepartmentId(examRecordId, departmentId);
        boolean allDone = deptProgress.stream().allMatch(p -> "COMPLETED".equals(p.getProgressStatus()));
        if (allDone) {
            departmentQueueRepository.findByExamRecordIdAndDepartmentId(examRecordId, departmentId)
                .ifPresent(q -> {
                    q.setQueueStatus(DepartmentQueue.QueueStatus.COMPLETED);
                    departmentQueueRepository.save(q);
                });
        }
    }

    @Transactional
    public ExamRecord completeExam(Long examRecordId) {
        ExamRecord record = getExamRecord(examRecordId);
        long pendingCount = examProgressRepository
            .countByExamRecordIdAndProgressStatus(examRecordId, "PENDING");
        if (pendingCount > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "无法完成体检，还有 " + pendingCount + " 个项目未完成");
        }
        record.markCompleted();
        return examRecordRepository.save(record);
    }

    // ========== 查询辅助 ==========

    @Transactional(readOnly = true)
    public boolean hasVitalSigns(Long examRecordId) {
        return vitalSignsRepository.existsByExamRecordId(examRecordId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveQueueInOtherDept(Long examRecordId, Long excludeDeptId) {
        List<DepartmentQueue> queues = departmentQueueRepository
            .findByExamRecordIdAndQueueStatusNot(examRecordId, "COMPLETED");
        return queues.stream().anyMatch(q -> !q.getDepartmentId().equals(excludeDeptId));
    }

    // ========== 标本管理 (Nurse: 采血/留尿/送检) ==========

    @Transactional
    public Specimen collectSpecimen(Long examRecordId, String specimenType, Long collectedBy) {
        Specimen specimen = new Specimen(examRecordId, specimenType, collectedBy);
        return specimenRepository.save(specimen);
    }

    @Transactional
    public Specimen labelSpecimen(Long specimenId, String barcode) {
        Specimen s = specimenRepository.findById(specimenId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "标本记录不存在"));
        s.markLabeled(barcode);
        return specimenRepository.save(s);
    }

    @Transactional
    public Specimen sendSpecimenToLab(Long specimenId, Long sentBy) {
        Specimen s = specimenRepository.findById(specimenId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "标本记录不存在"));
        s.markSent(sentBy);
        return specimenRepository.save(s);
    }

    @Transactional
    public Specimen receiveSpecimen(Long specimenId, Long receivedBy) {
        Specimen specimen = specimenRepository.findById(specimenId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "标本记录不存在"));
        if (!"SENT".equals(specimen.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "只能接收已送检的标本");
        }
        specimen.markReceived(receivedBy);
        return specimenRepository.save(specimen);
    }

    @Transactional
    public Specimen rejectSpecimen(Long specimenId, Long rejectedBy, String reason) {
        Specimen specimen = specimenRepository.findById(specimenId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "标本记录不存在"));
        specimen.markRejected(rejectedBy, reason);
        return specimenRepository.save(specimen);
    }

    @Transactional(readOnly = true)
    public List<Specimen> getSpecimensByExam(Long examRecordId) {
        return specimenRepository.findByExamRecordId(examRecordId);
    }

    @Transactional(readOnly = true)
    public List<Specimen> getPendingSpecimens() {
        return specimenRepository.findByStatusIn(List.of("COLLECTED", "LABELED"));
    }

    // ========== 收尾确认 (Nurse: 核对无漏检) ==========

    @Transactional
    public ExamCompletion confirmCompletion(Long examRecordId, Long nurseId) {
        ExamCompletion ec = examCompletionRepository.findByExamRecordId(examRecordId)
            .orElse(new ExamCompletion(examRecordId));
        ec.setVitalSignsTaken(vitalSignsRepository.existsByExamRecordId(examRecordId));
        ec.setSpecimenCollected(!specimenRepository.findByExamRecordId(examRecordId).isEmpty());
        long pendingProgress = examProgressRepository.countByExamRecordIdAndProgressStatus(examRecordId, "PENDING");
        ec.setAllItemsChecked(pendingProgress == 0);
        if (!ec.isAllItemsChecked()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "Cannot confirm: " + pendingProgress + " items still pending");
        }
        if (!ec.isVitalSignsTaken()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "生命体征尚未测量");
        }
        ec.confirm(nurseId);

        // Check if this package requires final review
        ExamRecord record = getExamRecord(examRecordId);
        boolean needsReview = false;
        if (record.getPackageId() != null) {
            needsReview = examPackageRepository.findById(record.getPackageId())
                .map(ExamPackage::isHasFinalReview)
                .orElse(false);
        }

        if (needsReview) {
            // Route to review doctor — mark as IN_PROGRESS instead of COMPLETED
            record.markInProgress();
            examRecordRepository.save(record);
            ec.setNeedsReview(true);
            log.info("体检收尾完成，需送总检审核: examRecordId={}", examRecordId);
        } else {
            record.markCompleted();
            examRecordRepository.save(record);
        }

        return examCompletionRepository.save(ec);
    }

    /** 强制完成（跳过所有校验，仅供测试） */
    @Transactional
    public ExamCompletion forceComplete(Long examRecordId, Long nurseId) {
        ExamCompletion ec = examCompletionRepository.findByExamRecordId(examRecordId)
            .orElse(new ExamCompletion(examRecordId));
        ec.setVitalSignsTaken(true);
        ec.setSpecimenCollected(true);
        ec.setAllItemsChecked(true);
        ec.setNurseConfirmed(true);
        ec.setConfirmedBy(nurseId);
        ec.setConfirmedAt(LocalDateTime.now());

        // 检查该套餐是否需总检审核
        ExamRecord record = getExamRecord(examRecordId);
        boolean needsReview = false;
        if (record.getPackageId() != null) {
            needsReview = examPackageRepository.findById(record.getPackageId())
                .map(ExamPackage::isHasFinalReview)
                .orElse(false);
        }
        if (needsReview) {
            record.markInProgress();
            ec.setNeedsReview(true);
            log.info("强制完成，送总检审核: examRecordId={}", examRecordId);
        } else {
            record.markCompleted();
        }
        // 强制完成时自动审核通过所有待审核的检查结果（测试模式）
        int approved = jdbc.update(
            "UPDATE t_check_result SET review_status = 'APPROVED' " +
            "WHERE exam_record_id = ? AND review_status = 'PENDING'", examRecordId);
        if (approved > 0) {
            log.info("强制完成自动审核通过 {} 条检查结果: examRecordId={}", approved, examRecordId);
        }
        // 如果没有检查结果，为该套餐所有项目生成默认正常结果
        List<CheckResult> existingResults = checkResultRepository.findByExamRecordId(examRecordId);
        if (existingResults.isEmpty() && record.getPackageId() != null) {
            List<ExamPackageItem> pkgItems = examPackageItemRepository.findByPackageIdOrderBySortNoAsc(record.getPackageId());
            for (ExamPackageItem pi : pkgItems) {
                String itemName = examItemRepository.findById(pi.getItemId()).map(ExamItem::getItemName).orElse("项目#" + pi.getItemId());
                CheckResult cr = new CheckResult(examRecordId, pi.getItemId(), itemName, null, null, null, null, null, null, 1L, AbnormalLevel.NORMAL);
                try { Thread.sleep(1); } catch (Exception ignored) {}  // 避免resultNo毫秒级重复
                cr.setReviewStatus(ReviewStatus.APPROVED);
                cr.setTextValue("正常");
                checkResultRepository.save(cr);
            }
            log.info("强制完成为套餐 {} 生成 {} 条默认正常结果: examRecordId={}",
                record.getPackageId(), pkgItems.size(), examRecordId);
        }
        // 将所有待检查进度标记为完成（避免报告生成时验证失败）
        int progressUpdated = jdbc.update(
            "UPDATE t_exam_progress SET progress_status = 'COMPLETED' " +
            "WHERE exam_record_id = ? AND progress_status = 'PENDING'", examRecordId);
        if (progressUpdated > 0) {
            log.info("强制完成自动完成 {} 条检查进度: examRecordId={}", progressUpdated, examRecordId);
        }
        examRecordRepository.save(record);
        return examCompletionRepository.save(ec);
    }

    @Transactional(readOnly = true)
    public ExamCompletion getCompletionStatus(Long examRecordId) {
        return examCompletionRepository.findByExamRecordId(examRecordId).orElse(null);
    }

    // ========== 科室结论 (Doctor: 每个科室写单项结论) ==========

    @Transactional
    public ExamProgress writeDepartmentConclusion(Long progressId, String conclusion) {
        ExamProgress progress = examProgressRepository.findById(progressId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "检查进度记录不存在"));
        progress.setDepartmentConclusion(conclusion);
        progress.markCompleted();
        return examProgressRepository.save(progress);
    }

    // ========== 统计概览 ==========

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(Long branchId) {
        List<ExamRecord> all = examRecordRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
        long checkedIn = all.stream().filter(r -> r.getStatus() == ExamStatus.CHECKED_IN).count();
        long inProgress = all.stream().filter(r -> r.getStatus() == ExamStatus.IN_PROGRESS).count();
        long completed = all.stream().filter(r -> r.getStatus() == ExamStatus.COMPLETED).count();
        return Map.of(
            "total", all.size(),
            "checkedIn", checkedIn,
            "inProgress", inProgress,
            "completed", completed
        );
    }
}
