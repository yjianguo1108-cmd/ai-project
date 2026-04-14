package com.grain.system.module.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.warehouse.dto.OutboundCreateDTO;
import com.grain.system.module.warehouse.entity.OutboundOrder;
import com.grain.system.module.warehouse.service.InventoryCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仓储出库管理")
@RestController
@RequestMapping("/api/v1/inventory/check/outbound")
@RequiredArgsConstructor
public class WarehouseOutboundController {

    private final InventoryCheckService checkService;

    @Operation(summary = "分页查询出库单")
    @GetMapping
    @PreAuthorize("hasAuthority('warehouse:outbound:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<OutboundOrder>> getOutboundPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer grainId,
            @RequestParam(required = false) Integer positionId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer status) {
        return R.ok(checkService.getOutboundPage(page, size, grainId, positionId, dateFrom, dateTo, status));
    }

    @Operation(summary = "查询出库单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:outbound:list') or hasAuthority('ROLE_ADMIN')")
    public R<OutboundOrder> getOutboundById(@PathVariable Integer id) {
        return R.ok(checkService.getOutboundById(id));
    }

    @Operation(summary = "创建出库单")
    @PostMapping
    @PreAuthorize("hasAuthority('warehouse:outbound:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> createOutbound(@Valid @RequestBody OutboundCreateDTO dto) {
        checkService.createOutbound(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "确认出库")
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('warehouse:outbound:confirm') or hasAuthority('ROLE_ADMIN')")
    public R<Void> confirmOutbound(@PathVariable Integer id) {
        checkService.confirmOutbound(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "作废出库单")
    @PutMapping("/{id}/void")
    @PreAuthorize("hasAuthority('warehouse:outbound:void') or hasAuthority('ROLE_ADMIN')")
    public R<Void> voidOutbound(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        checkService.voidOutbound(id, reason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }
}