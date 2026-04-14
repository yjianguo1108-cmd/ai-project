package com.grain.system.module.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.warehouse.dto.CheckAuditDTO;
import com.grain.system.module.warehouse.dto.CheckDetailUpdateDTO;
import com.grain.system.module.warehouse.dto.CheckPlanCreateDTO;
import com.grain.system.module.warehouse.entity.InventoryCheckDetail;
import com.grain.system.module.warehouse.entity.InventoryCheckPlan;
import com.grain.system.module.warehouse.service.InventoryCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "库存盘点管理")
@RestController
@RequestMapping("/api/v1/inventory/check")
@RequiredArgsConstructor
public class InventoryCheckController {

    private final InventoryCheckService checkService;

    @Operation(summary = "分页查询盘点计划")
    @GetMapping("/plan")
    @PreAuthorize("hasAuthority('warehouse:check:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<InventoryCheckPlan>> getPlanPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer checkType) {
        return R.ok(checkService.getPlanPage(page, size, status, checkType));
    }

    @Operation(summary = "查询盘点计划详情")
    @GetMapping("/plan/{id}")
    @PreAuthorize("hasAuthority('warehouse:check:list') or hasAuthority('ROLE_ADMIN')")
    public R<InventoryCheckPlan> getPlanById(@PathVariable Integer id) {
        return R.ok(checkService.getPlanById(id));
    }

    @Operation(summary = "创建盘点计划")
    @PostMapping("/plan")
    @PreAuthorize("hasAuthority('warehouse:check:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> createPlan(@Valid @RequestBody CheckPlanCreateDTO dto) {
        checkService.createPlan(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "发布盘点计划")
    @PutMapping("/plan/{id}/publish")
    @PreAuthorize("hasAuthority('warehouse:check:publish') or hasAuthority('ROLE_ADMIN')")
    public R<Void> publishPlan(@PathVariable Integer id) {
        checkService.publishPlan(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "查询盘点明细")
    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('warehouse:check:list') or hasAuthority('ROLE_ADMIN')")
    public R<List<InventoryCheckDetail>> getDetailList(@RequestParam Integer planId) {
        return R.ok(checkService.getDetailList(planId));
    }

    @Operation(summary = "录入实盘数量")
    @PutMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('warehouse:check:update') or hasAuthority('ROLE_ADMIN')")
    public R<Void> updateDetail(@PathVariable Integer id, @Valid @RequestBody CheckDetailUpdateDTO dto) {
        checkService.updateDetail(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "提交盘点结果")
    @PutMapping("/plan/{id}/submit")
    @PreAuthorize("hasAuthority('warehouse:check:submit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> submitPlan(@PathVariable Integer id) {
        checkService.submitPlan(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "审核盘点")
    @PutMapping("/plan/{id}/audit")
    @PreAuthorize("hasAuthority('warehouse:check:audit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> auditPlan(@PathVariable Integer id, @RequestBody CheckAuditDTO dto) {
        checkService.auditPlan(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "处理差异")
    @PutMapping("/diff/{id}/handle")
    @PreAuthorize("hasAuthority('warehouse:check:handle') or hasAuthority('ROLE_ADMIN')")
    public R<Void> handleDiff(@PathVariable Integer id,
                               @RequestParam String handleType,
                               @RequestParam(required = false) String adjustReason) {
        checkService.handleDiff(id, handleType, adjustReason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "获取盘点报告")
    @GetMapping("/plan/{id}/report")
    @PreAuthorize("hasAuthority('warehouse:check:report') or hasAuthority('ROLE_ADMIN')")
    public R<String> getReportUrl(@PathVariable Integer id) {
        return R.ok(checkService.getReportUrl(id));
    }
}
