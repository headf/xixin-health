package com.xixin.health.questionnaire;

import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionnaireService {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireService.class);

    private final QuestionnaireRepository questionnaireRepository;

    public QuestionnaireService(QuestionnaireRepository questionnaireRepository) {
        this.questionnaireRepository = questionnaireRepository;
    }

    @Transactional
    public PreExamQuestionnaire create(Long appointmentId, Long userId) {
        // 检查是否已存在
        if (questionnaireRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "该预约已存在问卷记录");
        }

        PreExamQuestionnaire q = new PreExamQuestionnaire(appointmentId, userId);
        return questionnaireRepository.save(q);
    }

    @Transactional(readOnly = true)
    public PreExamQuestionnaire getByAppointment(Long appointmentId) {
        return questionnaireRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "问卷不存在"));
    }

    @Transactional(readOnly = true)
    public java.util.List<PreExamQuestionnaire> getByUser(Long userId) {
        return questionnaireRepository.findByUserId(userId);
    }

    @Transactional
    public PreExamQuestionnaire update(Long id, PreExamQuestionnaire updated) {
        PreExamQuestionnaire q = questionnaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "问卷不存在"));

        q.setFamilyDiseaseHistory(updated.getFamilyDiseaseHistory());
        q.setPastMedicalHistory(updated.getPastMedicalHistory());
        q.setMedicationHistory(updated.getMedicationHistory());
        q.setSmokingHistory(updated.getSmokingHistory());
        q.setDrinkingHistory(updated.getDrinkingHistory());
        q.setExerciseHabit(updated.getExerciseHabit());
        q.setDietHabit(updated.getDietHabit());
        q.setSleepQuality(updated.getSleepQuality());
        q.setMenstrualHistory(updated.getMenstrualHistory());
        q.setAllergicHistory(updated.getAllergicHistory());
        q.setSurgicalHistory(updated.getSurgicalHistory());
        q.setOtherInfo(updated.getOtherInfo());
        q.markCompleted();

        return questionnaireRepository.save(q);
    }
}
