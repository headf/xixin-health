package com.xixin.health.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    /** 按分院+日期+时段查找排班 */
    Optional<DoctorSchedule> findByBranchIdAndScheduleDateAndTimeSlot(
            Long branchId, LocalDate scheduleDate, String timeSlot);

    /** 按分院+日期查询所有排班 */
    List<DoctorSchedule> findByBranchIdAndScheduleDateOrderByTimeSlot(
            Long branchId, LocalDate scheduleDate);

    /** 按医生+日期查询 */
    List<DoctorSchedule> findByDoctorIdAndScheduleDateOrderByTimeSlot(
            Long doctorId, LocalDate scheduleDate);

    /** 按日期范围查询 */
    List<DoctorSchedule> findByScheduleDateBetweenOrderByScheduleDate(
            LocalDate start, LocalDate end);

    /** 按分院+日期范围查询 */
    List<DoctorSchedule> findByBranchIdAndScheduleDateBetweenOrderByScheduleDate(
            Long branchId, LocalDate start, LocalDate end);

    /** 查询某天某时段已启用且未满的排班 */
    List<DoctorSchedule> findByBranchIdAndScheduleDateAndTimeSlotAndEnabledTrueAndBookedCountLessThan(
            Long branchId, LocalDate scheduleDate, String timeSlot, int maxPatients);

    /** 按分院筛选 */
    List<DoctorSchedule> findByBranchIdOrderByScheduleDateDesc(Long branchId);

    /** 统计某天某时段的总可预约量 */
    List<DoctorSchedule> findByScheduleDateAndTimeSlotAndEnabledTrue(
            LocalDate scheduleDate, String timeSlot);
}
