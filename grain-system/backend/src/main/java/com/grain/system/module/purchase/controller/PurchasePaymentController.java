package com.grain.system.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.purchase.dto.PurchasePaymentCreateDTO;
import com.grain.system.module.purchase.entity.PurchaseOrder;
import com.grain.system.module.purchase.service.PurchasePaymentService;
import com.grain.system.module.purchase.vo.PurchasePaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收购付款管理")
@RestController
@RequestMapping("/api/v1/purchase/payment")
@RequiredArgsConstructor
public class PurchasePaymentController {

    private final PurchasePaymentService paymentService;

    @Operation(summary = "分页查询付款记录")
    @GetMapping
    @PreAuthorize("hasAuthority('purchase:payment:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<PurchasePaymentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderId) {
        return R.ok(paymentService.getPaymentPage(page, size, keyword, orderId));
    }

    @Operation(summary = "查询付款记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:payment:list') or hasAuthority('ROLE_ADMIN')")
    public R<PurchasePaymentVO> getById(@PathVariable Integer id) {
        return R.ok(paymentService.getPaymentById(id));
    }

    @Operation(summary = "获取可付款的收购单列表")
    @GetMapping("/available-orders")
    @PreAuthorize("hasAuthority('purchase:payment:create') or hasAuthority('ROLE_ADMIN')")
    public R<List<PurchaseOrder>> getAvailableOrders() {
        return R.ok(paymentService.getAvailableOrdersForPayment());
    }

    @Operation(summary = "新增付款记录")
    @PostMapping
    @PreAuthorize("hasAuthority('purchase:payment:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> create(@Valid @RequestBody PurchasePaymentCreateDTO dto) {
        paymentService.createPayment(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "删除付款记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:payment:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> delete(@PathVariable Integer id) {
        paymentService.deletePayment(id);
        return R.ok();
    }
}