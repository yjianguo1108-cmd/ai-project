package com.grain.system.module.system.service;

import com.grain.system.module.system.dto.LoginDTO;
import com.grain.system.module.system.vo.PermissionVO;

import java.util.List;
import java.util.Map;

public interface AuthService {

    /**
     * 登录，返回token及用户信息
     */
    Map<String, Object> login(LoginDTO dto, String ip);

    /**
     * 登出
     */
    void logout(Long userId);

    /**
     * 获取当前用户权限码集合
     */
    List<String> getCurrentPermissions(Long userId);

    /**
     * 获取当前用户菜单树
     */
    List<PermissionVO> getCurrentMenuTree(Long userId);

    /**
     * 获取当前登录用户信息
     */
    Map<String, Object> getCurrentUserInfo(Long userId);

    /**
     * 修改当前登录用户密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
