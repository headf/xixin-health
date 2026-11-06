package com.xixin.health.appointment;

import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.configuration.DoctorSchedule;
import com.xixin.health.configuration.DoctorScheduleRepository;
import com.xixin.health.configuration.ExamPackage;
import com.xixin.health.configuration.ExamPackageRepository;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.notification.NotificationService;
import com.xixin.health.order.Order;
import com.xixin.health.order.OrderRepository;
import com.xixin.health.order.OrderStatus;
import com.xixin.health.profile.HealthProfile;
import com.xixin.health.profile.HealthProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final ExamRecordRepository examRecordRepository;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final ExamPackageRepository examPackageRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               ExamRecordRepository examRecordRepository,
                               NotificationService notificationService,
                               OrderRepository orderRepository,
                               ExamPackageRepository examPackageRepository,
                               HealthProfileRepository healthProfileRepository,
                               DoctorScheduleRepository doctorScheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.examRecordRepository = examRecordRepository;
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
        this.examPackageRepository = examPackageRepository;
        this.healthProfileRepository = healthProfileRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    @Transactional
    public Appointment createAppointment(Long userId, Long profileId, Long packageId,
                                          Long branchId, LocalDate appointmentDate, String timeSlot) {
        // ========== 1. 日期校验 ==========
        LocalDate today = LocalDate.now();
        if (appointmentDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "预约日期不能早于今天");
        }
        // 预约日期不能超过 30 天（前端限制 14 天，后端放宽一点但也要有上限）
        if (appointmentDate.isAfter(today.plusDays(30))) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "预约日期不能超过30天");
        }

        // ========== 2. 套餐校验 ==========
        ExamPackage pkg = examPackageRepository.findById(packageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "套餐不存在"));
        if (!pkg.isEnabled()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "该套餐已下架，请选择其他套餐");
        }

        // ========== 3. 就诊人校验 ==========
        HealthProfile profile = healthProfileRepository.findById(profileId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "健康档案不存在"));
        // 3a. 就诊人必须属于当前用户
        if (!profile.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "无权使用该就诊人信息进行预约");
        }

        // ========== 4. 套餐性别限制校验 ==========
        if (pkg.getGenderLimit() != null && !pkg.getGenderLimit().isEmpty()) {
            String genderLimit = pkg.getGenderLimit().toUpperCase();
            if (profile.getIdCardNo() != null && profile.getIdCardNo().length() >= 18) {
                char genderDigit = profile.getIdCardNo().charAt(16);
                int genderNum = Character.getNumericValue(genderDigit);
                boolean isMale = genderNum % 2 == 1;
                if ("MALE".equals(genderLimit) && !isMale) {
                    throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "该套餐仅限男性预约");
                }
                if ("FEMALE".equals(genderLimit) && isMale) {
                    throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "该套餐仅限女性预约");
                }
            }
        }

        // ========== 5. 重复预约校验 ==========
        String mappedSlot = mapTimeSlot(timeSlot);

        // 5a. 同一用户 + 同一就诊人 + 同一天 + 同一时段 不能有未取消的预约
        List<Appointment> duplicateAppts = appointmentRepository
                .findByUserIdAndProfileIdAndAppointmentDateAndTimeSlotAndStatusNotAndDeletedFalse(
                        userId, profileId, appointmentDate, mappedSlot, AppointmentStatus.CANCELLED);
        if (!duplicateAppts.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                    "您已在该日期该时段为该就诊人预约过体检，请勿重复预约");
        }

        // 5b. 同一就诊人 + 同一天 + 同一时段 不能有来自其他用户的预约（就诊人时间冲突）
        List<Appointment> profileConflictAppts = appointmentRepository
                .findByProfileIdAndAppointmentDateAndTimeSlotAndStatusNotAndDeletedFalse(
                        profileId, appointmentDate, mappedSlot, AppointmentStatus.CANCELLED);
        if (!profileConflictAppts.isEmpty()) {
            // 如果冲突记录不是当前用户创建的，说明就诊人已经被别人预约了
            boolean conflictByOtherUser = profileConflictAppts.stream()
                    .anyMatch(a -> !a.getUserId().equals(userId));
            if (conflictByOtherUser) {
                throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                        "该就诊人在该日期该时段已有其他用户预约，请选择其他时段或日期");
            }
        }

        // 5c. 同一就诊人 + 同一天 不能有两个不同时段的预约（体检通常只需要一天）
        // 但如果预约的时段不同（上午/下午），可能存在当天跨时段体检的场景，这里仅做提示性限制
        List<Appointment> sameDayProfileAppts = appointmentRepository
                .findByProfileIdAndAppointmentDateAndStatusNotAndDeletedFalse(
                        profileId, appointmentDate, AppointmentStatus.CANCELLED);
        if (!sameDayProfileAppts.isEmpty()) {
            // 如果已有其他时段的预约，提示用户
            boolean differentSlot = sameDayProfileAppts.stream()
                    .anyMatch(a -> !a.getTimeSlot().equals(mappedSlot));
            if (differentSlot) {
                throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                        "该就诊人在当天已有其他时段的体检预约，无法重复预约");
            }
        }

        // ========== 6. 号源校验：检查该日期该时段是否有可用号源 ==========
        List<DoctorSchedule> schedules = doctorScheduleRepository
                .findByBranchIdAndScheduleDateAndTimeSlotAndEnabledTrueAndBookedCountLessThan(
                        branchId, appointmentDate, mappedSlot, Integer.MAX_VALUE);

        // 如果没找到排班，检查是否有对应时段的 ALL_DAY 排班
        if (schedules.isEmpty()) {
            schedules = doctorScheduleRepository
                    .findByBranchIdAndScheduleDateOrderByTimeSlot(branchId, appointmentDate)
                    .stream()
                    .filter(s -> "ALL_DAY".equals(s.getTimeSlot()) && s.hasAvailableSlots())
                    .toList();
        }

        if (schedules.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                    "该日期该时段暂无可用号源，请选择其他日期或时段");
        }

        // 取第一个可用排班，增加已预约数
        DoctorSchedule schedule = schedules.get(0);
        schedule.incrementBooked();
        doctorScheduleRepository.save(schedule);
        log.info("号源已占用: scheduleId={}, booked={}/{}", schedule.getId(),
                schedule.getBookedCount(), schedule.getMaxPatients());

        return appointmentRepository.save(new Appointment(userId, profileId, packageId,
                branchId, appointmentDate, mappedSlot));
    }

    @Transactional(readOnly = true)
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
    }

    @Transactional
    public void markPaid(Long id) {
        Appointment appointment = getById(id);
        appointment.markPaid();
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Appointment already cancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Cannot cancel completed appointment");
        }
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.markCancelled();
        appointmentRepository.save(appointment);

        // 发送通知
        Long userId = appointment.getUserId();
        String dateStr = appointment.getAppointmentDate().toString();
        if (oldStatus == AppointmentStatus.PAID) {
            notificationService.create(userId, "REFUND_NOTICE",
                "体检退款通知",
                "您预约的 " + dateStr + " 体检已取消，退款将原路返回，请留意查收。");
        } else {
            notificationService.create(userId, "APPOINTMENT_CANCELLED",
                "预约已取消",
                "您 " + dateStr + " 的体检预约已成功取消。");
        }
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = getById(appointmentId);
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "无权取消该预约");
        }
        if (appointment.getStatus() == AppointmentStatus.CHECKED_IN ||
            appointment.getStatus() == AppointmentStatus.COMPLETED ||
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "该预约无法取消");
        }
        appointment.markCancelled();
        appointmentRepository.save(appointment);

        // 释放号源（忽略旧数据不匹配的情况）
        try {
            tryReleaseSlot(appointment.getBranchId(), appointment.getAppointmentDate(), appointment.getTimeSlot());
        } catch (Exception e) {
            log.warn("释放号源失败，不影响取消操作: {}", e.getMessage());
        }

        // 取消关联的订单
        orderRepository.findByAppointmentId(appointmentId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PAID) {
                order.setStatus(OrderStatus.REFUND_REQUESTED);
                orderRepository.save(order);
            } else if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                order.setStatus(OrderStatus.CLOSED);
                orderRepository.save(order);
            }
        });
    }

    // ========== 时段号源查询 ==========

    public record TimeSlotAvailability(String timeSlot, boolean available, int bookedCount, int maxCapacity) {}

    @Transactional(readOnly = true)
    public List<TimeSlotAvailability> getTimeSlotAvailability(Long branchId, LocalDate date) {
        List<DoctorSchedule> schedules = doctorScheduleRepository
                .findByBranchIdAndScheduleDateOrderByTimeSlot(branchId, date);

        return schedules.stream()
                .filter(DoctorSchedule::isEnabled)
                .map(s -> new TimeSlotAvailability(
                        s.getTimeSlot(),
                        s.hasAvailableSlots(),
                        s.getBookedCount(),
                        s.getMaxPatients()))
                .toList();
    }

    // ========== 每周签到统计 (供签到趋势图使用) ==========

    public record DailyTrend(String dayLabel, long appointments, long checkins) {}

    @Transactional(readOnly = true)
    public List<DailyTrend> getWeeklyTrend(Long branchId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
        List<DailyTrend> trends = new ArrayList<>();
        String[] dayNames = {"周一","周二","周三","周四","周五","周六","周日"};
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            long aptCount = branchId != null
                ? appointmentRepository.countByBranchIdAndAppointmentDate(branchId, date)
                : appointmentRepository.countByAppointmentDate(date);
            long checkedIn = branchId != null
                ? appointmentRepository.countByBranchIdAndAppointmentDateAndStatus(branchId, date, AppointmentStatus.CHECKED_IN)
                : appointmentRepository.countByAppointmentDateAndStatus(date, AppointmentStatus.CHECKED_IN);
            trends.add(new DailyTrend(dayNames[i], aptCount, checkedIn));
        }
        return trends;
    }

    // ========== 内部方法 ==========

    /** 将前端传入的时段映射为排班编码 */
    private String mapTimeSlot(String slot) {
        if (slot == null) return "MORNING";
        return switch (slot.trim()) {
            case "上午", "MORNING" -> "MORNING";
            case "下午", "AFTERNOON" -> "AFTERNOON";
            case "全天", "ALL_DAY" -> "ALL_DAY";
            // 旧数据格式 "08:00-09:00" → 映射到 MORNING
            case "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00" -> "MORNING";
            case "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00" -> "AFTERNOON";
            default -> slot.trim().toUpperCase();
        };
    }

    /** 释放号源（无匹配不抛异常） */
    private void tryReleaseSlot(Long branchId, LocalDate date, String timeSlot) {
        String mappedSlot = mapTimeSlot(timeSlot);
        doctorScheduleRepository.findByBranchIdAndScheduleDateAndTimeSlot(branchId, date, mappedSlot)
                .ifPresent(schedule -> {
                    schedule.decrementBooked();
                    doctorScheduleRepository.save(schedule);
                    log.info("号源已释放: scheduleId={}, booked={}/{}", schedule.getId(),
                            schedule.getBookedCount(), schedule.getMaxPatients());
                });
    }
}
