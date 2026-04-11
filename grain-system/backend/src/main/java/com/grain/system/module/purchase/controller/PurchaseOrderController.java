package com.grain.system.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.purchase.dto.PurchaseOrderCreateDTO;
import com.grain.system.module.purchase.service.PurchaseOrderService;
import com.grain.system.module.purchase.vo.PurchaseOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "收购单据管理")
@RestController
@RequestMapping("/api/v1/purchase/order")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService orderService;

    @Operation(summary = "分页查询收购单据列表")
    @GetMapping
    @PreAuthorize("hasAuthority('purchase:order:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<PurchaseOrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer farmerId,
            @RequestParam(required = false) Integer paymentStatus) {
        return R.ok(orderService.getOrderPage(page, size, keyword, status, farmerId, paymentStatus));
    }

    @Operation(summary = "查询收购单据详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:order:list') or hasAuthority('ROLE_ADMIN')")
    public R<PurchaseOrderVO> getById(@PathVariable Integer id) {
        return R.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "新增收购单据")
    @PostMapping
    @PreAuthorize("hasAuthority('purchase:order:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> create(@Valid @RequestBody PurchaseOrderCreateDTO dto) {
        orderService.createOrder(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "编辑收购单据")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:order:update') or hasAuthority('ROLE_ADMIN')")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody PurchaseOrderCreateDTO dto) {
        orderService.updateOrder(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "审核收购单据")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('purchase:order:audit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> audit(@PathVariable Integer id) {
        orderService.auditOrder(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "完成收购单据")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('purchase:order:complete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> complete(@PathVariable Integer id) {
        orderService.completeOrder(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "作废收购单据")
    @PutMapping("/{id}/void")
    @PreAuthorize("hasAuthority('purchase:order:void') or hasAuthority('ROLE_ADMIN')")
    public R<Void> voidRecord(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        orderService.voidOrder(id, reason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "删除收购单据")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:order:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> delete(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return R.ok();
    }
}