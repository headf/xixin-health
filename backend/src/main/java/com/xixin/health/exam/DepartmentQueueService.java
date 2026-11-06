package com.xixin.health.exam;

import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.configuration.Department;
import com.xixin.health.configuration.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科室排队叫号服务
 * 负责队列的分时段叫号、空腹优先等排队策略
 */
@Service
public class DepartmentQueueService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentQueueService.class);

    private final DepartmentQueueRepository departmentQueueRepository;
    private final DepartmentRepository departmentRepository;

    public DepartmentQueueService(DepartmentQueueRepository departmentQueueRepository,
                                  DepartmentRepository departmentRepository) {
        this.departmentQueueRepository = departmentQueueRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 叫号逻辑（分时段 + 空腹优先）
     * <p>
     * 策略：
     * 1. 先按预约时间段分层，同一时段的按先来先服务（createdAt 排序）
     * 2. 空时段可放号给其他时段用户
     * 3. 空腹科室（如血液检查）排队的人优先叫号
     *
     * @param departmentId 科室ID
     * @return 被叫号的排队记录
     */
    @Transactional
    public DepartmentQueue callNext(Long departmentId) {
        // 1. 获取科室信息，检查是否需要空腹
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "科室不存在 (id=" + departmentId + ")"));
        boolean isFastingDept = department.isFastingRequired();

        // 2. 获取所有 WAITING 状态排队记录（按创建时间排序）
        List<DepartmentQueue> waitingEntries = departmentQueueRepository
                .findByDepartmentIdAndQueueStatusOrderByCreatedAt(departmentId, "WAITING");

        if (waitingEntries.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "当前科室没有等待中的患者");
        }

        // 3. 按预约时段分组，保留插入顺序
        //    没有 timeSlot 的记录归入 "UNKNOWN" 组
        Map<String, List<DepartmentQueue>> groupedByTimeSlot = new LinkedHashMap<>();
        List<DepartmentQueue> unknownSlot = new ArrayList<>();

        for (DepartmentQueue entry : waitingEntries) {
            String slot = entry.getTimeSlot();
            if (slot == null || slot.isBlank()) {
                unknownSlot.add(entry);
            } else {
                groupedByTimeSlot.computeIfAbsent(slot, k -> new ArrayList<>()).add(entry);
            }
        }

        // 4. 对时段进行排序（按开始时间升序）
        List<String> sortedSlots = groupedByTimeSlot.keySet().stream()
                .sorted(this::compareTimeSlot)
                .collect(Collectors.toList());

        // 5. 选择叫号对象
        DepartmentQueue selected = null;

        if (isFastingDept) {
            // ===== 空腹科室：优先叫号 =====
            // 先在各个时段中找 F- 开头的（空腹标记）队列号
            for (String slot : sortedSlots) {
                List<DepartmentQueue> entries = groupedByTimeSlot.get(slot);
                for (DepartmentQueue entry : entries) {
                    if (entry.getQueueNo() != null && entry.getQueueNo().startsWith("F-")) {
                        selected = entry;
                        break;
                    }
                }
                if (selected != null) break;
            }

            // 如果没找到 F- 开头的，从最早时段取第一个
            if (selected == null) {
                for (String slot : sortedSlots) {
                    List<DepartmentQueue> entries = groupedByTimeSlot.get(slot);
                    if (!entries.isEmpty()) {
                        selected = entries.get(0);
                        break;
                    }
                }
            }
        } else {
            // ===== 非空腹科室：按时段 + FCFS =====
            // 先从最早时段取第一个
            for (String slot : sortedSlots) {
                List<DepartmentQueue> entries = groupedByTimeSlot.get(slot);
                if (!entries.isEmpty()) {
                    selected = entries.get(0);
                    break;
                }
            }
        }

        // 6. 如果按时段分组后没有选到，从未知时段中取第一个
        if (selected == null && !unknownSlot.isEmpty()) {
            selected = unknownSlot.get(0);
        }

        // 7. 最后兜底：取第一个等待记录
        if (selected == null) {
            selected = waitingEntries.get(0);
        }

        // 8. 标记为 CALLED
        selected.setQueueStatus(DepartmentQueue.QueueStatus.CALLED);
        DepartmentQueue saved = departmentQueueRepository.save(selected);

        log.info("叫号成功: departmentId={}, departmentName={}, queueNo={}, examRecordId={}, timeSlot={}",
                departmentId, department.getDepartmentName(), saved.getQueueNo(),
                saved.getExamRecordId(), saved.getTimeSlot());

        return saved;
    }

    /**
     * 比较两个时段的先后顺序
     * 格式示例: "08:00-09:00", "MORNING", "AFTERNOON"
     */
    private int compareTimeSlot(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;

        // 处理命名时段
        int priorityA = getNamedSlotPriority(a);
        int priorityB = getNamedSlotPriority(b);
        if (priorityA != priorityB) {
            return Integer.compare(priorityA, priorityB);
        }

        // 处理 "HH:mm-HH:mm" 格式
        try {
            String startA = a.split("-")[0].trim();
            String startB = b.split("-")[0].trim();
            LocalTime timeA = LocalTime.parse(startA, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime timeB = LocalTime.parse(startB, DateTimeFormatter.ofPattern("HH:mm"));
            return timeA.compareTo(timeB);
        } catch (Exception e) {
            // 如果解析失败，按字符串比较
            return a.compareTo(b);
        }
    }

    /**
     * 获取命名时段的优先级（越小越早）
     */
    private int getNamedSlotPriority(String slot) {
        if (slot == null) return 99;
        return switch (slot.toUpperCase()) {
            case "ALL_DAY" -> 0;
            case "MORNING" -> 1;
            case "AFTERNOON" -> 2;
            case "EVENING" -> 3;
            default -> 50;
        };
    }
}
