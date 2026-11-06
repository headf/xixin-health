package com.xixin.health.questionnaire;

import com.xixin.health.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    /**
     * 创建问卷（用户端）
     */
    @PostMapping
    public ApiResponse<PreExamQuestionnaire> create(@Valid @RequestBody CreateRequest request) {
        return ApiResponse.success(
                questionnaireService.create(request.appointmentId(), request.userId()));
    }

    /**
     * 按预约查询问卷（用户端）
     */
    @GetMapping("/by-appointment/{appointmentId}")
    public ApiResponse<PreExamQuestionnaire> getByAppointment(@PathVariable Long appointmentId) {
        return ApiResponse.success(questionnaireService.getByAppointment(appointmentId));
    }

    /**
     * 按用户查询问卷列表（用户端）
     */
    @GetMapping("/by-user/{userId}")
    public ApiResponse<List<PreExamQuestionnaire>> getByUser(@PathVariable Long userId) {
        return ApiResponse.success(questionnaireService.getByUser(userId));
    }

    /**
     * 医护端 - 按预约查询患者检前问卷
     * 护士、体检医生、审核医生均可查看
     */
    @GetMapping("/staff/by-appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('NURSE', 'EXAM_DOCTOR', 'REVIEW_DOCTOR', 'SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
    public ApiResponse<PreExamQuestionnaire> getByAppointmentForStaff(@PathVariable Long appointmentId) {
        return ApiResponse.success(questionnaireService.getByAppointment(appointmentId));
    }

    /**
     * 更新（提交）问卷
     */
    @PutMapping("/{id}")
    public ApiResponse<PreExamQuestionnaire> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request) {

        PreExamQuestionnaire q = new PreExamQuestionnaire(null, null);
        q.setFamilyDiseaseHistory(request.familyDiseaseHistory());
        q.setPastMedicalHistory(request.pastMedicalHistory());
        q.setMedicationHistory(request.medicationHistory());
        q.setSmokingHistory(request.smokingHistory());
        q.setDrinkingHistory(request.drinkingHistory());
        q.setExerciseHabit(request.exerciseHabit());
        q.setDietHabit(request.dietHabit());
        q.setSleepQuality(request.sleepQuality());
        q.setMenstrualHistory(request.menstrualHistory());
        q.setAllergicHistory(request.allergicHistory());
        q.setSurgicalHistory(request.surgicalHistory());
        q.setOtherInfo(request.otherInfo());

        return ApiResponse.success(questionnaireService.update(id, q));
    }

    // ========== Request DTOs ==========

    public record CreateRequest(
            @NotNull Long appointmentId,
            @NotNull Long userId) {
    }

    public record UpdateRequest(
            String familyDiseaseHistory,
            String pastMedicalHistory,
            String medicationHistory,
            String smokingHistory,
            String drinkingHistory,
            String exerciseHabit,
            String dietHabit,
            String sleepQuality,
            String menstrualHistory,
            String allergicHistory,
            String surgicalHistory,
            String otherInfo) {
    }
}
