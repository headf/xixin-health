package com.xixin.health.configuration;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 对外号源查询接口 — 供用户端预约时查看可用时段
 */
@RestController
@RequestMapping("/api/v1/slots")
public class SlotController {

    private final DoctorScheduleRepository scheduleRepository;

    public SlotController(DoctorScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * 查询某分院某日期的可用号源
     *
     * @param branchId 分院ID
     * @param date     日期 (YYYY-MM-DD)，默认今天
     */
    @GetMapping("/available")
    public ApiResponse<List<SlotInfo>> getAvailableSlots(
            @RequestParam Long branchId,
            @RequestParam(required = false) String date) {

        LocalDate queryDate = (date != null && !date.isEmpty())
                ? LocalDate.parse(date)
                : LocalDate.now();

        // 标准时段定义
        List<String> standardSlots = List.of("MORNING", "AFTERNOON");

        // 查该分院该日期所有排班
        List<DoctorSchedule> schedules = scheduleRepository
                .findByBranchIdAndScheduleDateOrderByTimeSlot(branchId, queryDate);

        List<SlotInfo> result = new ArrayList<>();
        for (String slot : standardSlots) {
            // 找匹配的排班（可能有多个医生）
            List<DoctorSchedule> matched = schedules.stream()
                    .filter(s -> s.getTimeSlot().equals(slot) || s.getTimeSlot().equals("ALL_DAY"))
                    .filter(DoctorSchedule::isEnabled)
                    .toList();

            int totalMax = matched.stream().mapToInt(DoctorSchedule::getMaxPatients).sum();
            int totalBooked = matched.stream().mapToInt(DoctorSchedule::getBookedCount).sum();
            int remaining = totalMax - totalBooked;

            String status;
            if (matched.isEmpty()) {
                status = "NO_SCHEDULE";
            } else if (remaining <= 0) {
                status = "FULL";
            } else if (remaining <= 3) {
                status = "TIGHT";
            } else {
                status = "AVAILABLE";
            }

            result.add(new SlotInfo(
                    slot,
                    slotLabel(slot),
                    remaining,
                    totalMax,
                    totalBooked,
                    status
            ));
        }

        return ApiResponse.success(result);
    }

    /**
     * 批量查询日期范围内的号源概览（用于预约日历标记）
     */
    @GetMapping("/calendar")
    public ApiResponse<List<CalendarDayInfo>> getCalendarSlots(
            @RequestParam Long branchId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<DoctorSchedule> schedules;
        if (branchId != null) {
            schedules = scheduleRepository
                    .findByBranchIdAndScheduleDateBetweenOrderByScheduleDate(branchId, start, end);
        } else {
            schedules = scheduleRepository
                    .findByScheduleDateBetweenOrderByScheduleDate(start, end);
        }

        // 按日期聚合
        Map<LocalDate, List<DoctorSchedule>> byDate = new LinkedHashMap<>();
        for (DoctorSchedule s : schedules) {
            if (!s.isEnabled()) continue;
            byDate.computeIfAbsent(s.getScheduleDate(), k -> new ArrayList<>()).add(s);
        }

        List<CalendarDayInfo> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            List<DoctorSchedule> daySchedules = byDate.getOrDefault(d, List.of());
            int totalMax = daySchedules.stream().mapToInt(DoctorSchedule::getMaxPatients).sum();
            int totalBooked = daySchedules.stream().mapToInt(DoctorSchedule::getBookedCount).sum();
            int remaining = totalMax - totalBooked;

            String status;
            if (daySchedules.isEmpty()) {
                status = "NO_SCHEDULE";
            } else if (remaining <= 0) {
                status = "FULL";
            } else {
                status = "AVAILABLE";
            }

            result.add(new CalendarDayInfo(
                    d.toString(),
                    daySchedules.isEmpty() ? 0 : remaining,
                    totalMax,
                    status
            ));
        }

        return ApiResponse.success(result);
    }

    // ========== Records ==========

    public record SlotInfo(
            String slotCode,
            String slotLabel,
            int remainingSlots,
            int maxCapacity,
            int bookedCount,
            String status
    ) {}

    public record CalendarDayInfo(
            String date,
            int remainingSlots,
            int maxCapacity,
            String status
    ) {}

    private static String slotLabel(String slot) {
        return switch (slot) {
            case "MORNING" -> "上午 (08:00-12:00)";
            case "AFTERNOON" -> "下午 (13:00-17:00)";
            case "ALL_DAY" -> "全天";
            default -> slot;
        };
    }
}
