package com.xixin.health.questionnaire;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireRepository extends JpaRepository<PreExamQuestionnaire, Long> {

    Optional<PreExamQuestionnaire> findByAppointmentId(Long appointmentId);

    List<PreExamQuestionnaire> findByUserId(Long userId);
}
