package com.xixin.health.guide;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 营养餐/早餐管理控制器
 */
@RestController
@RequestMapping("/api/v1/meals")
public class MealController {

    private final MealRecordRepository mealRecordRepository;
    private final ExamRecordRepository examRecordRepository;

    public MealController(MealRecordRepository mealRecordRepository,
                          ExamRecordRepository examRecordRepository) {
        this.mealRecordRepository = mealRecordRepository;
        this.examRecordRepository = examRecordRepository;
    }

    /**
     * 查看就餐状态
     *
     * @param examRecordId 体检记录ID
     * @return 就餐状态信息
     */
    @GetMapping("/status/{examRecordId}")
    public ApiResponse<Map<String, Object>> getStatus(@PathVariable Long examRecordId) {
        Optional<MealRecord> mealOpt = mealRecordRepository.findByExamRecordId(examRecordId);

        Map<String, Object> result = new HashMap<>();
        if (mealOpt.isPresent()) {
            MealRecord meal = mealOpt.get();
            result.put("id", meal.getId());
            result.put("examRecordId", meal.getExamRecordId());
            result.put("userId", meal.getUserId());
            result.put("mealStatus", meal.getMealStatus());
            result.put("eligibleAt", meal.getEligibleAt());
            result.put("claimedAt", meal.getClaimedAt());
            result.put("createdAt", meal.getCreatedAt());
        } else {
            // 未创建记录时，返回 PENDING 状态
            result.put("examRecordId", examRecordId);
            result.put("mealStatus", "PENDING");
            result.put("eligibleAt", null);
            result.put("claimedAt", null);
        }
        return ApiResponse.success(result);
    }

    /**
     * 领取餐券
     * 只在 ELIGIBLE 状态下可以领取
     *
     * @param examRecordId 体检记录ID
     * @return 更新后的就餐状态
     */
    @PostMapping("/claim/{examRecordId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> claimMeal(@PathVariable Long examRecordId) {
        MealRecord meal = mealRecordRepository.findByExamRecordId(examRecordId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "未找到餐券记录，请先完成空腹项目"));

        if (!MealRecord.MealStatus.ELIGIBLE.name().equals(meal.getMealStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT,
                "当前状态不可领取餐券，状态: " + meal.getMealStatus());
        }

        meal.markClaimed();
        mealRecordRepository.save(meal);

        Map<String, Object> result = new HashMap<>();
        result.put("id", meal.getId());
        result.put("examRecordId", meal.getExamRecordId());
        result.put("mealStatus", meal.getMealStatus());
        result.put("claimedAt", meal.getClaimedAt());
        return ApiResponse.success(result);
    }
}
