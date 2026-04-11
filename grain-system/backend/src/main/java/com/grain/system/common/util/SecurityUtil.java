package com.grain.system.common.util;

import com.grain.system.security.LoginUser;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户工具类
 */
public class SecurityUtil {

    public static LoginUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    public static String getCurrentUsername() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    public static boolean hasRole(String roleCode) {
        LoginUser user = getCurrentUser();
        if (user == null) return false;
        return user.getRoleCodes().contains(roleCode);
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
