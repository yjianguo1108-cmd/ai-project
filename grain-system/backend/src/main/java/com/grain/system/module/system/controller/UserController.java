package com.grain.system.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.system.dto.UserCreateDTO;
import com.grain.system.module.system.dto.UserUpdateDTO;
import com.grain.system.module.system.service.UserService;
import com.grain.system.module.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/v1/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list') or hasAuthority('ROLE_ADMIN')")
    public R<IPage<UserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Integer status) {
        return R.ok(userService.getUserPage(page, size, keyword, roleId, status));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list') or hasAuthority('ROLE_ADMIN')")
    public R<UserVO> getById(@PathVariable Integer id) {
        return R.ok(userService.getUserById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:create')")
    public R<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.createUser(dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "编辑用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:update')")
    public R<Void> update(@PathVariable Integer id, @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:status')")
    public R<Void> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) return R.fail("status不能为空");
        userService.updateStatus(id, status, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "管理员重置密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:reset_pwd')")
    public R<Void> resetPassword(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null) return R.fail("密码不能为空");
        userService.resetPassword(id, password, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }

    @Operation(summary = "分配用户角色")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:update')")
    public R<Void> assignRoles(@PathVariable Integer id, @RequestBody Map<String, List<Integer>> body) {
        List<Integer> roleIds = body.get("roleIds");
        if (roleIds == null) return R.fail("角色ID列表不能为空");
        userService.assignRoles(id, roleIds);
        return R.ok();
    }

    @Operation(summary = "删除用户(逻辑删除)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public R<Void> delete(@PathVariable Integer id) {
        userService.deleteUser(id, SecurityUtil.getCurrentUserId().intValue());
        return R.ok();
    }
}
