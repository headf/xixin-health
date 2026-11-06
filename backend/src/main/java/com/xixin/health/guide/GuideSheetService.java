package com.xixin.health.guide;

import com.xixin.health.configuration.Department;
import com.xixin.health.configuration.DepartmentRepository;
import com.xixin.health.exam.ExamProgress;
import com.xixin.health.exam.ExamProgressRepository;
import com.xixin.health.exam.GuideSheet;
import com.xixin.health.exam.GuideSheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 导检单服务 - 管理空腹项目标记、营养餐关联等
 */
@Service
public class GuideSheetService {

    private static final Logger log = LoggerFactory.getLogger(GuideSheetService.class);

    private final GuideSheetRepository guideSheetRepository;
    private final ExamProgressRepository examProgressRepository;
    private final DepartmentRepository departmentRepository;

    public GuideSheetService(GuideSheetRepository guideSheetRepository,
                             ExamProgressRepository examProgressRepository,
                             DepartmentRepository departmentRepository) {
        this.guideSheetRepository = guideSheetRepository;
        this.examProgressRepository = examProgressRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 标记空腹项目完成
     * 当体检记录中所有需要空腹的科室项目都已完成后，将 fastingDone 设为 true
     *
     * @param examRecordId 体检记录ID
     * @return 是否所有空腹项目已完成
     */
    @Transactional
    public boolean markFastingComplete(Long examRecordId) {
        // 查询该体检记录的所有进度
        List<ExamProgress> progresses = examProgressRepository.findByExamRecordId(examRecordId);
        if (progresses.isEmpty()) {
            log.warn("No exam progress found for examRecordId={}", examRecordId);
            return false;
        }

        // 获取所有需要空腹的科室ID
        List<Department> allDepartments = departmentRepository.findAll();
        Set<Long> fastingDeptIds = allDepartments.stream()
            .filter(Department::isFastingRequired)
            .map(Department::getId)
            .collect(Collectors.toSet());

        if (fastingDeptIds.isEmpty()) {
            // 没有需要空腹的项目，自动标记完成
            updateFastingDone(examRecordId, true);
            return true;
        }

        // 检查所有空腹科室项目是否已完成
        boolean allFastingDone = progresses.stream()
            .filter(p -> p.getDepartmentId() != null && fastingDeptIds.contains(p.getDepartmentId()))
            .allMatch(p -> "COMPLETED".equals(p.getProgressStatus()) || "SKIPPED".equals(p.getProgressStatus()));

        if (allFastingDone) {
            updateFastingDone(examRecordId, true);
            log.info("All fasting items completed for examRecordId={}", examRecordId);
        }

        return allFastingDone;
    }

    /**
     * 检查空腹项目是否已完成
     *
     * @param examRecordId 体检记录ID
     * @return 是否已完成空腹项目
     */
    @Transactional(readOnly = true)
    public boolean isFastingComplete(Long examRecordId) {
        return guideSheetRepository.findByExamRecordId(examRecordId)
            .map(GuideSheet::isFastingDone)
            .orElse(false);
    }

    private void updateFastingDone(Long examRecordId, boolean done) {
        guideSheetRepository.findByExamRecordId(examRecordId).ifPresent(sheet -> {
            sheet.setFastingDone(done);
            guideSheetRepository.save(sheet);
        });
    }
}
