package com.xixin.health.configuration;

import com.xixin.health.audit.AdminAuditRecorder;
import com.xixin.health.common.api.ApiResponse;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping({"/api/v1/admin/flow-nodes", "/api/v1/system/flow-nodes"})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
@Deprecated /* /api/v1/system/flow-nodes 已废弃，请统一使用 /api/v1/admin/flow-nodes */
public class AdminFlowNodeController {

    private final FlowNodeRepository flowNodeRepository;
    private final AdminAuditRecorder auditRecorder;

    public AdminFlowNodeController(FlowNodeRepository flowNodeRepository, AdminAuditRecorder auditRecorder) {
        this.flowNodeRepository = flowNodeRepository;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping
    public ApiResponse<List<FlowNode>> flowNodes(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.success(flowNodeRepository.findAll().stream()
            .filter(node -> enabled == null || enabled == node.isEnabled())
            .filter(node -> matches(keyword, node.getNodeCode(), node.getNodeName()))
            .sorted(Comparator.comparingInt(FlowNode::getSortNo).thenComparing(FlowNode::getId))
            .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<FlowNode> flowNode(@PathVariable Long id) {
        return ApiResponse.success(findNode(id));
    }

    @PostMapping
    @Transactional
    public ApiResponse<FlowNode> createFlowNode(
        @Valid @RequestBody FlowNodeRequest request,
        HttpServletRequest webRequest
    ) {
        ensureCodeAvailable(request.nodeCode(), null);
        FlowNode saved = flowNodeRepository.save(FlowNode.create(
            request.nodeCode(),
            request.nodeName(),
            request.nodeType(),
            request.sortNoOrDefault(),
            request.requiredPermission(),
            request.approvalRoleCode(),
            request.enabledOrDefault()
        ));
        auditRecorder.record("CONFIG_CREATE", "t_flow_node", saved.getId(), webRequest);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<FlowNode> updateFlowNode(
        @PathVariable Long id,
        @Valid @RequestBody FlowNodeRequest request,
        HttpServletRequest webRequest
    ) {
        ensureCodeAvailable(request.nodeCode(), id);
        FlowNode node = findNode(id);
        node.update(
            request.nodeCode(),
            request.nodeName(),
            request.nodeType(),
            request.sortNoOrDefault(),
            request.requiredPermission(),
            request.approvalRoleCode(),
            request.enabledOrDefault()
        );
        auditRecorder.record("CONFIG_UPDATE", "t_flow_node", node.getId(), webRequest);
        return ApiResponse.success(node);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ApiResponse<FlowNode> setFlowNodeStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request,
        HttpServletRequest webRequest
    ) {
        FlowNode node = findNode(id);
        node.setEnabled(request.enabled());
        auditRecorder.record("CONFIG_STATUS", "t_flow_node", node.getId(), webRequest);
        return ApiResponse.success(node);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<FlowNode> deleteFlowNode(@PathVariable Long id, HttpServletRequest webRequest) {
        FlowNode node = findNode(id);
        node.setEnabled(false);
        auditRecorder.record("CONFIG_DISABLE", "t_flow_node", node.getId(), webRequest);
        return ApiResponse.success(node);
    }

    private FlowNode findNode(Long id) {
        return flowNodeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Flow node not found"));
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        boolean duplicate = flowNodeRepository.findAll().stream()
            .anyMatch(node -> node.getNodeCode().equals(code)
                && !Objects.equals(node.getId(), currentId));
        if (duplicate) {
            throw new BusinessException(ErrorCode.BUSINESS_CONFLICT, "Flow node code already exists");
        }
    }

    private boolean matches(String keyword, String code, String name) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase();
        return code.toLowerCase().contains(normalized) || name.toLowerCase().contains(normalized);
    }

    public record FlowNodeRequest(
        @NotBlank String nodeCode,
        @NotBlank String nodeName,
        @NotBlank String nodeType,
        Integer sortNo,
        String requiredPermission,
        String approvalRoleCode,
        Boolean enabled
    ) {
        int sortNoOrDefault() {
            return Objects.requireNonNullElse(sortNo, 0);
        }

        boolean enabledOrDefault() {
            return Objects.requireNonNullElse(enabled, true);
        }
    }

    public record StatusRequest(boolean enabled) {
    }
}
