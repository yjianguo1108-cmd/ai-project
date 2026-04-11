package com.grain.system.module.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.warehouse.service.InventoryLedgerService;
import com.grain.system.module.warehouse.vo.InventoryLedgerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "库存台账管理")
@RestController
@RequestMapping("/api/v1/warehouse/ledger")
@RequiredArgsConstructor
public class InventoryLedgerController {

    private final InventoryLedgerService ledgerService;

    @Operation(summary = "分页查询库存台账")
    @GetMapping
    @PreAuthorize("hasAuthority('warehouse:ledger:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<InventoryLedgerVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer grainId,
            @RequestParam(required = false) Integer storagePositionId) {
        return R.ok(ledgerService.getLedgerPage(page, size, grainId, storagePositionId));
    }

    @Operation(summary = "查询台账详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:ledger:list') or hasAuthority('ROLE_ADMIN')")
    public R<InventoryLedgerVO> getById(@PathVariable Integer id) {
        return R.ok(ledgerService.getLedgerById(id));
    }

    @Operation(summary = "调整库存")
    @PutMapping("/{id}/adjust")
    @PreAuthorize("hasAuthority('warehouse:ledger:adjust') or hasAuthority('ROLE_ADMIN')")
    public R<Void> adjust(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        BigDecimal adjustWeight = new BigDecimal(body.get("adjustWeight").toString());
        String reason = (String) body.get("reason");
        ledgerService.adjustWeight(id, adjustWeight, reason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }
}