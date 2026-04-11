package com.grain.system.module.system.controller;

import com.grain.system.common.result.R;
import com.grain.system.module.system.entity.Role;
import com.grain.system.module.system.service.RoleService;
import com.grain.system.module.system.vo.PermissionVO;
import com.grain.system.module.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/v1/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "查询所有角色(不分页,供下拉用)")
    @GetMapping("/all")
    public R<List<RoleVO>> listAll() {
        return R.ok(roleService.listAll());
    }

    @Operation(summary = "分页查询角色列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:role:list') or hasAuthority('ROLE_ADMIN')")
    public R<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(roleService.getRolePage(page, size, keyword));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:create')")
    public R<Void> create(@RequestBody Role role) {
        roleService.createRole(role);
        return R.ok();
    }

    @Operation(summary = "编辑角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:update')")
    public R<Void> update(@PathVariable Integer id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public R<Void> delete(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    @Operation(summary = "查询角色的权限ID列表")
    @GetMapping("/{id}/permissions")
    public R<List<Integer>> getPermissions(@PathVariable Integer id) {
        return R.ok(roleService.getPermissionIds(id));
    }

    @Operation(summary = "为角色分配权限")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:assign_perm')")
    public R<Void> assignPermissions(@PathVariable Integer id, @RequestBody Map<String, List<Integer>> body) {
        List<Integer> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return R.ok();
    }

    @Operation(summary = "查询完整权限树(用于分配权限弹窗)")
    @GetMapping("/permission-tree")
    public R<List<PermissionVO>> permissionTree() {
        return R.ok(roleService.getFullPermissionTree());
    }
}
