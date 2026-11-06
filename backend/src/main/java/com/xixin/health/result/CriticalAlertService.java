package com.xixin.health.result;

import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CriticalAlertService {

    private final CriticalAlertRepository criticalAlertRepository;

    public CriticalAlertService(CriticalAlertRepository criticalAlertRepository) {
        this.criticalAlertRepository = criticalAlertRepository;
    }

    /**
     * 创建危急值预警
     */
    @Transactional
    public CriticalAlert createAlert(Long resultId, Long examRecordId, Long userId) {
        CriticalAlert alert = new CriticalAlert(resultId, examRecordId, userId);
        return criticalAlertRepository.save(alert);
    }

    /**
     * 医生复核危急值
     */
    @Transactional
    public CriticalAlert reviewAlert(Long alertId, Long reviewerId, String comment) {
        CriticalAlert alert = criticalAlertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CriticalAlert not found"));
        alert.review(reviewerId, comment);
        return criticalAlertRepository.save(alert);
    }

    /**
     * 患者确认收到危急值
     */
    @Transactional
    public CriticalAlert confirmAlert(Long alertId) {
        CriticalAlert alert = criticalAlertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CriticalAlert not found"));
        alert.confirm();
        return criticalAlertRepository.save(alert);
    }

    /**
     * 制定随访计划
     */
    @Transactional
    public CriticalAlert setFollowUp(Long alertId, String plan) {
        CriticalAlert alert = criticalAlertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "CriticalAlert not found"));
        alert.setFollowUpPlan(plan);
        return criticalAlertRepository.save(alert);
    }

    /**
     * 获取待处理预警（INITIAL 状态）
     */
    @Transactional(readOnly = true)
    public List<CriticalAlert> getPendingAlerts() {
        return criticalAlertRepository.findByAlertStatus("INITIAL");
    }

    /**
     * 获取用户的预警
     */
    @Transactional(readOnly = true)
    public List<CriticalAlert> getUserAlerts(Long userId) {
        return criticalAlertRepository.findByUserId(userId);
    }
}
