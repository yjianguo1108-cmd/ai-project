package com.grain.system.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.result.R;
import com.grain.system.module.system.entity.DownstreamOrg;
import com.grain.system.module.system.mapper.DownstreamOrgMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "下游机构管理")
@RestController
@RequestMapping("/downstream/org")
@RequiredArgsConstructor
public class DownstreamOrgController {

    private final DownstreamOrgMapper orgMapper;

    @Operation(summary = "分页查询下游机构")
    @GetMapping
    @PreAuthorize("hasAuthority('system:org:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<DownstreamOrg>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        Page<DownstreamOrg> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DownstreamOrg> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DownstreamOrg::getOrgName, keyword);
        }
        if (auditStatus != null) {
            wrapper.eq(DownstreamOrg::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(DownstreamOrg::getCreateTime);
        return R.ok(orgMapper.selectPage(pageParam, wrapper));
    }

    @Operation(summary = "获取下游机构详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:list') or hasAuthority('ROLE_ADMIN')")
    public R<DownstreamOrg> getById(@PathVariable Integer id) {
        return R.ok(orgMapper.selectById(id));
    }

    @Operation(summary = "创建下游机构")
    @PostMapping
    @PreAuthorize("hasAuthority('system:org:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> create(@RequestBody DownstreamOrg org) {
        org.setCreateTime(LocalDateTime.now());
        orgMapper.insert(org);
        return R.ok();
    }

    @Operation(summary = "更新下游机构")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:update') or hasAuthority('ROLE_ADMIN')")
    public R<Void> update(@PathVariable Integer id, @RequestBody DownstreamOrg org) {
        org.setId(id);
        orgMapper.updateById(org);
        return R.ok();
    }

    @Operation(summary = "删除下游机构")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:org:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> delete(@PathVariable Integer id) {
        orgMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "审核下游机构")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('system:org:audit') or hasAuthority('ROLE_ADMIN')")
    public R<Void> audit(@PathVariable Integer id, @RequestParam Integer auditStatus, @RequestParam(required = false) String rejectReason) {
        DownstreamOrg org = orgMapper.selectById(id);
        if (org == null) {
            return R.fail("机构不存在");
        }
        org.setAuditStatus(auditStatus);
        org.setRejectReason(rejectReason);
        orgMapper.updateById(org);
        return R.ok();
    }
}
