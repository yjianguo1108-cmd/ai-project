package com.grain.system.module.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.warehouse.service.InboundOrderService;
import com.grain.system.module.warehouse.vo.InboundOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "粮食入库管理")
@RestController
@RequestMapping("/api/v1/warehouse/inbound")
@RequiredArgsConstructor
public class InboundOrderController {

    private final InboundOrderService inboundService;

    @Operation(summary = "分页查询入库记录")
    @GetMapping
    @PreAuthorize("hasAuthority('warehouse:inbound:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<InboundOrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer grainId,
            @RequestParam(required = false) Integer positionId) {
        return R.ok(inboundService.getInboundPage(page, size, keyword, grainId, positionId));
    }

    @Operation(summary = "查询入库记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:inbound:list') or hasAuthority('ROLE_ADMIN')")
    public R<InboundOrderVO> getById(@PathVariable Integer id) {
        return R.ok(inboundService.getInboundById(id));
    }

    @Operation(summary = "创建入库记录")
    @PostMapping
    @PreAuthorize("hasAuthority('warehouse:inbound:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> create(@RequestBody Map<String, Object> body) {
        Integer purchaseOrderId = (Integer) body.get("purchaseOrderId");
        Integer grainId = (Integer) body.get("grainId");
        Integer positionId = (Integer) body.get("positionId");
        BigDecimal weight = new BigDecimal(body.get("weight").toString());
        inboundService.createInbound(purchaseOrderId, grainId, positionId, weight, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "确认入库")
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('warehouse:inbound:confirm') or hasAuthority('ROLE_ADMIN')")
    public R<Void> confirm(@PathVariable Integer id) {
        inboundService.confirmInbound(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "删除入库记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:inbound:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> delete(@PathVariable Integer id) {
        inboundService.deleteInbound(id);
        return R.ok();
    }
}