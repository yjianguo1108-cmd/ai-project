package com.grain.system.module.system.controller;

import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.system.dto.LoginDTO;
import com.grain.system.module.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        log.info("收到登录请求: 用户名={}, IP={}", dto.getUsername(), getClientIp(request));
        String ip = getClientIp(request);
        return R.ok(authService.login(dto, ip));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            authService.logout(userId);
        }
        return R.ok();
    }

    @Operation(summary = "获取当前用户权限码")
    @GetMapping("/permissions")
    public R<List<String>> getPermissions() {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(authService.getCurrentPermissions(userId));
    }

    @Operation(summary = "获取当前用户菜单树")
    @GetMapping("/menus")
    public R<?> getMenus() {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(authService.getCurrentMenuTree(userId));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public R<Map<String, Object>> getInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        return R.ok(authService.getCurrentUserInfo(userId));
    }

    @Operation(summary = "修改当前登录用户密码")
    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return R.fail("密码不能为空");
        }
        Long userId = SecurityUtil.getCurrentUserId();
        authService.changePassword(userId, oldPassword, newPassword);
        return R.ok();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
}
