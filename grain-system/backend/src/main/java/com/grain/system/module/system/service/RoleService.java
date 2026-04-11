package com.grain.system.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.system.entity.Role;
import com.grain.system.module.system.vo.PermissionVO;
import com.grain.system.module.system.vo.RoleVO;

import java.util.List;

public interface RoleService {
    List<RoleVO> listAll();
    IPage<RoleVO> getRolePage(int page, int size, String keyword);
    void createRole(Role role);
    void updateRole(Role role);
    void deleteRole(Integer id);
    List<Integer> getPermissionIds(Integer roleId);
    void assignPermissions(Integer roleId, List<Integer> permissionIds);
    List<PermissionVO> getFullPermissionTree();
}
