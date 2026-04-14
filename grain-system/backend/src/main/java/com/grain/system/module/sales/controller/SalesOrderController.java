package com.grain.system.module.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.module.sales.dto.PaymentReceiveDTO;
import com.grain.system.module.sales.dto.SalesOrderCreateDTO;
import com.grain.system.module.sales.service.SalesOrderService;
import com.grain.system.module.sales.vo.SalesOrderVO;
import com.grain.system.module.sales.vo.SalesPaymentVO;
import com.grain.system.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "销售订单管理")
@RestController
@RequestMapping("/api/v1/sales/order")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService orderService;

    @Operation(summary = "分页查询销售订单")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:order:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<SalesOrderVO>> getOrderPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orgId,
            @RequestParam(required = false) Integer grainId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentStatus) {
        return R.ok(orderService.getOrderPage(page, size, keyword, orgId, grainId, status, paymentStatus));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:order:list') or hasAuthority('ROLE_ADMIN')")
    public R<SalesOrderVO> getOrderById(@PathVariable Integer id) {
        return R.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "创建销售订单")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:order:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> createOrder(@Valid @RequestBody SalesOrderCreateDTO dto) {
        orderService.createOrder(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "提交订单")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('sales:order:submit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> submitOrder(@PathVariable Integer id) {
        orderService.submitOrder(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "审核订单")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('sales:order:audit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> auditOrder(@PathVariable Integer id,
                              @RequestParam Integer status,
                              @RequestParam(required = false) String rejectReason) {
        orderService.auditOrder(id, status, rejectReason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "作废订单")
    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('sales:order:void') or hasAuthority('ROLE_ADMIN')")
    public R<Void> voidOrder(@PathVariable Integer id, @RequestParam String reason) {
        orderService.voidOrder(id, reason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "开始出库")
    @PutMapping("/outbound/{id}/start")
    @PreAuthorize("hasAuthority('sales:outbound:start') or hasAuthority('ROLE_ADMIN')")
    public R<Void> startOutbound(@PathVariable Integer id) {
        orderService.startOutbound(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "确认出库")
    @PutMapping("/outbound/{id}/confirm")
    @PreAuthorize("hasAuthority('sales:outbound:confirm') or hasAuthority('ROLE_ADMIN')")
    public R<Void> confirmOutbound(@PathVariable Integer id,
                                   @RequestParam BigDecimal actualWeight,
                                   @RequestParam(required = false) String attachmentUrl) {
        orderService.confirmOutbound(id, actualWeight, attachmentUrl, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "追加出库单")
    @PostMapping("/{id}/outbound/add")
    @PreAuthorize("hasAuthority('sales:outbound:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> addOutbound(@PathVariable Integer id, @RequestParam BigDecimal planWeight) {
        orderService.addOutboundForOrder(id, planWeight, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "获取可出库的订单")
    @GetMapping("/available-outbound")
    @PreAuthorize("hasAuthority('sales:outbound:create') or hasAuthority('ROLE_ADMIN')")
    public R<List<SalesOrderVO>> getAvailableOrdersForOutbound() {
        return R.ok(orderService.getAvailableOrdersForOutbound());
    }

    @Operation(summary = "获取可收款的订单")
    @GetMapping("/available-payment")
    @PreAuthorize("hasAuthority('sales:payment:create') or hasAuthority('ROLE_ADMIN')")
    public R<List<SalesOrderVO>> getAvailableOrdersForPayment() {
        return R.ok(orderService.getAvailableOrdersForPayment());
    }
}
