package com.grain.system.module.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.module.sales.service.SalesOrderService;
import com.grain.system.module.sales.vo.SalesOrderVO;
import com.grain.system.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "出库核销管理")
@RestController
@RequestMapping("/api/v1/sales/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final SalesOrderService orderService;

    @Operation(summary = "分页查询出库单")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:outbound:list') or hasAuthority('ROLE_ADMIN')")
    public R<List<SalesOrderVO>> getOutboundPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer salesOrderId,
            @RequestParam(required = false) Integer status) {
        return R.ok(orderService.getOutboundPage(page, size, salesOrderId, status));
    }

    @Operation(summary = "查询出库单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:outbound:list') or hasAuthority('ROLE_ADMIN')")
    public R<SalesOrderVO> getOutboundById(@PathVariable Integer id) {
        return R.ok(orderService.getOutboundById(id));
    }

    @Operation(summary = "开始出库")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAuthority('sales:outbound:start') or hasAuthority('ROLE_ADMIN')")
    public R<Void> startOutbound(@PathVariable Integer id) {
        orderService.startOutbound(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "确认出库")
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('sales:outbound:confirm') or hasAuthority('ROLE_ADMIN')")
    public R<Void> confirmOutbound(@PathVariable Integer id,
                                   @RequestParam BigDecimal actualWeight,
                                   @RequestParam(required = false) String attachmentUrl) {
        orderService.confirmOutbound(id, actualWeight, attachmentUrl, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "作废出库单")
    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('sales:outbound:void') or hasAuthority('ROLE_ADMIN')")
    public R<Void> voidOutbound(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        orderService.voidOutbound(id, reason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }
}
