package com.grain.system.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.purchase.dto.WeighingRecordCreateDTO;
import com.grain.system.module.purchase.service.WeighingRecordService;
import com.grain.system.module.purchase.vo.WeighingRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "称重计量管理")
@RestController
@RequestMapping("/api/v1/purchase/weighing")
@RequiredArgsConstructor
public class WeighingRecordController {

    private final WeighingRecordService weighingService;

    @Operation(summary = "分页查询称重记录")
    @GetMapping
    @PreAuthorize("hasAuthority('purchase:weighing:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<WeighingRecordVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer reserveId) {
        return R.ok(weighingService.getWeighingPage(page, size, keyword, status, reserveId));
    }

    @Operation(summary = "查询称重记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:weighing:list') or hasAuthority('ROLE_ADMIN')")
    public R<WeighingRecordVO> getById(@PathVariable Integer id) {
        return R.ok(weighingService.getWeighingById(id));
    }

    @Operation(summary = "新增称重记录")
    @PostMapping
    @PreAuthorize("hasAuthority('purchase:weighing:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> create(@Valid @RequestBody WeighingRecordCreateDTO dto) {
        weighingService.createWeighing(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "编辑称重记录")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:weighing:update') or hasAuthority('ROLE_ADMIN')")
    public R<Void> update(@PathVariable Integer id, @RequestBody WeighingRecordCreateDTO dto) {
        weighingService.updateWeighing(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "审核称重记录")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('purchase:weighing:audit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> audit(@PathVariable Integer id) {
        weighingService.auditWeighing(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "作废称重记录")
    @PutMapping("/{id}/void")
    @PreAuthorize("hasAuthority('purchase:weighing:void') or hasAuthority('ROLE_ADMIN')")
    public R<Void> voidRecord(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        weighingService.voidWeighing(id, reason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "删除称重记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:weighing:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> delete(@PathVariable Integer id) {
        weighingService.deleteWeighing(id);
        return R.ok();
    }
}