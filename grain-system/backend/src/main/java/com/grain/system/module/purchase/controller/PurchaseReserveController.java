package com.grain.system.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.purchase.dto.PurchaseReserveCreateDTO;
import com.grain.system.module.purchase.service.PurchaseReserveService;
import com.grain.system.module.purchase.vo.PurchaseReserveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "收购预约管理")
@RestController
@RequestMapping("/api/v1/purchase/reserve")
@RequiredArgsConstructor
public class PurchaseReserveController {

    private final PurchaseReserveService reserveService;

    @Operation(summary = "分页查询收购预约列表")
    @GetMapping
    @PreAuthorize("hasAuthority('purchase:reserve:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<PurchaseReserveVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer farmerId) {
        return R.ok(reserveService.getReservePage(page, size, keyword, status, farmerId));
    }

    @Operation(summary = "查询预约详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:reserve:list') or hasAuthority('ROLE_ADMIN')")
    public R<PurchaseReserveVO> getById(@PathVariable Integer id) {
        return R.ok(reserveService.getReserveById(id));
    }

    @Operation(summary = "新增收购预约")
    @PostMapping
    @PreAuthorize("hasAuthority('purchase:reserve:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> create(@Valid @RequestBody PurchaseReserveCreateDTO dto) {
        reserveService.createReserve(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "编辑收购预约")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:reserve:update') or hasAuthority('ROLE_ADMIN')")
    public R<Void> update(@PathVariable Integer id, @RequestBody PurchaseReserveCreateDTO dto) {
        reserveService.updateReserve(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "审核预约")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('purchase:reserve:audit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> audit(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer auditStatus = (Integer) body.get("auditStatus");
        String rejectReason = (String) body.get("rejectReason");
        reserveService.auditReserve(id, auditStatus, rejectReason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "排期预约")
    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasAuthority('purchase:reserve:schedule') or hasAuthority('ROLE_ADMIN')")
    public R<Void> schedule(@PathVariable Integer id) {
        reserveService.scheduleReserve(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "取消预约")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('purchase:reserve:cancel') or hasAuthority('ROLE_ADMIN')")
    public R<Void> cancel(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        reserveService.cancelReserve(id, reason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "删除预约")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:reserve:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> delete(@PathVariable Integer id) {
        reserveService.deleteReserve(id);
        return R.ok();
    }
}