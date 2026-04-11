package com.grain.system.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.system.dto.FarmerCreateDTO;
import com.grain.system.module.system.service.FarmerService;
import com.grain.system.module.system.vo.FarmerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "农户档案管理")
@RestController
@RequestMapping("/api/v1/farmer")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    @Operation(summary = "分页查询农户列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:farmer:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<FarmerVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        return R.ok(farmerService.getFarmerPage(page, size, keyword, auditStatus));
    }

    @Operation(summary = "查询农户详情")
    @GetMapping("/{id}")
    public R<FarmerVO> getById(@PathVariable Integer id) {
        return R.ok(farmerService.getFarmerById(id));
    }

    @Operation(summary = "新增农户档案")
    @PostMapping
    @PreAuthorize("hasAuthority('system:farmer:create')")
    public R<Void> create(@Valid @RequestBody FarmerCreateDTO dto) {
        farmerService.createFarmer(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "编辑农户档案")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:farmer:update')")
    public R<Void> update(@PathVariable Integer id, @RequestBody FarmerCreateDTO dto) {
        farmerService.updateFarmer(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "更新农户档案状态")
    @PutMapping("/{id}/audit-status")
    @PreAuthorize("hasAuthority('system:farmer:update')")
    public R<Void> updateAuditStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        farmerService.updateAuditStatus(id, body.get("auditStatus"), SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "删除农户档案")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:farmer:delete')")
    public R<Void> delete(@PathVariable Integer id) {
        farmerService.deleteFarmer(id);
        return R.ok();
    }
}
