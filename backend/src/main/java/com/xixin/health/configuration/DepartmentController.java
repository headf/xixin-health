package com.xixin.health.configuration;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    public record DepartmentInfo(Long id, String name, String departmentName, Long branchId, String location, int capacity, boolean fastingRequired, String fastingNote, boolean enabled) {}

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    @Cacheable("departments")
    public ApiResponse<List<DepartmentInfo>> getAllDepartments() {
        return ApiResponse.success(departmentRepository.findAll().stream()
            .filter(Department::isEnabled)
            .map(d -> new DepartmentInfo(
                d.getId(),
                d.getDepartmentName(),
                d.getDepartmentName(),
                d.getBranchId(),
                d.getLocation(),
                d.getCapacity(),
                d.isFastingRequired(),
                d.isFastingRequired() ? "需要空腹（禁食8-12小时）" : null,
                d.isEnabled()
            ))
            .toList());
    }
}
