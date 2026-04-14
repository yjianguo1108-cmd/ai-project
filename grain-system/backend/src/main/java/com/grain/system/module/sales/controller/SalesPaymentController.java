package com.grain.system.module.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.module.sales.dto.PaymentReceiveDTO;
import com.grain.system.module.sales.service.SalesOrderService;
import com.grain.system.module.sales.vo.SalesPaymentVO;
import com.grain.system.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "销售收款管理")
@RestController
@RequestMapping("/api/v1/sales/payment")
@RequiredArgsConstructor
public class SalesPaymentController {

    private final SalesOrderService orderService;

    @Operation(summary = "分页查询收款单")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:payment:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<SalesPaymentVO>> getPaymentPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer orgId,
            @RequestParam(required = false) Integer status) {
        return R.ok(orderService.getPaymentPage(page, size, orgId, status));
    }

    @Operation(summary = "查询收款单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:payment:list') or hasAuthority('ROLE_ADMIN')")
    public R<SalesPaymentVO> getPaymentById(@PathVariable Integer id) {
        return R.ok(orderService.getPaymentById(id));
    }

    @Operation(summary = "登记收款")
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('sales:payment:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> receivePayment(@PathVariable Integer id, @Valid @RequestBody PaymentReceiveDTO dto) {
        orderService.receivePayment(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "登记催款记录")
    @PostMapping("/collection/{orderId}/remind")
    @PreAuthorize("hasAuthority('sales:payment:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> addRemind(@PathVariable Integer orderId, @RequestParam String content) {
        orderService.addRemind(orderId, content, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }
}
