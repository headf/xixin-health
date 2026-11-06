package com.xixin.health.configuration;

import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.appointment.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PackageCatalogController {

    private final ExamPackageRepository examPackageRepository;
    private final ExamItemRepository examItemRepository;
    private final BranchRepository branchRepository;
    private final AppointmentService appointmentService;
    private final PackageItemRepository packageItemRepository;

    public PackageCatalogController(ExamPackageRepository examPackageRepository,
                                     ExamItemRepository examItemRepository,
                                     BranchRepository branchRepository,
                                     AppointmentService appointmentService,
                                     PackageItemRepository packageItemRepository) {
        this.examPackageRepository = examPackageRepository;
        this.examItemRepository = examItemRepository;
        this.branchRepository = branchRepository;
        this.appointmentService = appointmentService;
        this.packageItemRepository = packageItemRepository;
    }

    @GetMapping("/api/v1/packages")
    public ApiResponse<List<ExamPackage>> packages() {
        return ApiResponse.success(examPackageRepository.findByEnabledTrue(Sort.by(Sort.Direction.DESC, "recommendWeight")));
    }

    @GetMapping("/api/v1/packages/{packageId}/items")
    public ApiResponse<List<ExamItem>> packageItems(@PathVariable Long packageId) {
        List<PackageItem> mappings = packageItemRepository.findByPackageIdOrderBySortNo(packageId);
        List<Long> itemIds = mappings.stream().map(PackageItem::getItemId).collect(Collectors.toList());
        if (itemIds.isEmpty()) {
            return ApiResponse.success(List.of());
        }
        List<ExamItem> items = examItemRepository.findByIdIn(itemIds);
        return ApiResponse.success(items);
    }

    @GetMapping("/api/v1/items")
    public ApiResponse<List<ExamItem>> enabledItems() {
        return ApiResponse.success(examItemRepository.findByEnabledTrue());
    }

    @GetMapping("/api/v1/branches")
    public ApiResponse<List<Branch>> branches() {
        return ApiResponse.success(branchRepository.findByEnabledTrue());
    }

    @GetMapping("/api/v1/branches/{branchId}/time-slots")
    public ApiResponse<List<AppointmentService.TimeSlotAvailability>> branchTimeSlots(
        @PathVariable Long branchId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(appointmentService.getTimeSlotAvailability(branchId, date));
    }
}
