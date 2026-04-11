package com.grain.system.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.system.entity.DownstreamOrg;
import com.grain.system.module.system.service.OrgService;
import com.grain.system.module.system.vo.OrgVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "下游机构管理")
@RestController
@RequestMapping("/api/v1/downstream/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @Operation(summary = "分页查询机构列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:org:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<OrgVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer orgType) {
        return R.ok(orgService.getOrgPage(page, size, keyword, auditStatus, orgType));
    }

    @Operation(summary = "查询机构详情")
    @GetMapping("/{id}")
    public R<OrgVO> getById(@PathVariable Integer id) {
        return R.ok(orgService.getOrgById(id));
    }

    @Operation(summary = "新增机构")
    @PostMapping
    @PreAuthorize("hasAuthority('system:org:create')")
    public R<Void> create(@RequestBody DownstreamOrg org) {
        orgService.createOrg(org, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "编辑机构")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:update')")
    public R<Void> update(@PathVariable Integer id, @RequestBody DownstreamOrg org) {
        orgService.updateOrg(id, org, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "审核机构")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('system:org:audit')")
    public R<Void> audit(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer auditStatus = (Integer) body.get("auditStatus");
        String rejectReason = (String) body.get("rejectReason");
        orgService.audit(id, auditStatus, rejectReason, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "删除机构")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:delete')")
    public R<Void> delete(@PathVariable Integer id) {
        orgService.deleteOrg(id);
        return R.ok();
    }
}
