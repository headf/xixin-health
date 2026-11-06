package com.xixin.health.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByBranchIdAndDeletedFalse(Long branchId, Pageable pageable);

    long countByAppointmentDate(LocalDate date);

    long countByBranchIdAndAppointmentDate(Long branchId, LocalDate date);

    long countByBranchIdAndAppointmentDateAndStatus(Long branchId, LocalDate date, AppointmentStatus status);

    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);

    List<Appointment> findByAppointmentDateOrderByCreatedAt(LocalDate date);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(LocalDate start, LocalDate end);

    List<Appointment> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByBranchIdAndAppointmentDateAndTimeSlot(Long branchId, LocalDate date, String timeSlot);

    List<Appointment> findByUserIdAndProfileIdAndAppointmentDateAndTimeSlotAndDeletedFalse(
        Long userId, Long profileId, LocalDate appointmentDate, String timeSlot);

    // ========== 重复预约校验相关 ==========

    /** 查询同一用户+同一就诊人+同一天+同一时段中，未取消且未删除的预约 */
    List<Appointment> findByUserIdAndProfileIdAndAppointmentDateAndTimeSlotAndStatusNotAndDeletedFalse(
        Long userId, Long profileId, LocalDate appointmentDate, String timeSlot, AppointmentStatus status);

    /** 查询同一就诊人+同一天+同一时段中，未取消且未删除的预约（跨用户检测就诊人时间冲突） */
    List<Appointment> findByProfileIdAndAppointmentDateAndTimeSlotAndStatusNotAndDeletedFalse(
        Long profileId, LocalDate appointmentDate, String timeSlot, AppointmentStatus status);

    /** 查询同一用户+同一天中，未取消且未删除的预约（检测用户是否约了多个时段） */
    List<Appointment> findByUserIdAndAppointmentDateAndStatusNotAndDeletedFalse(
        Long userId, LocalDate appointmentDate, AppointmentStatus status);

    /** 查询同一就诊人+同一天中，未取消且未删除的预约（检测就诊人当天是否已有预约） */
    List<Appointment> findByProfileIdAndAppointmentDateAndStatusNotAndDeletedFalse(
        Long profileId, LocalDate appointmentDate, AppointmentStatus status);
}
