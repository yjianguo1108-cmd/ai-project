package com.grain.system.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.module.system.entity.Permission;
import com.grain.system.module.system.entity.Role;
import com.grain.system.module.system.mapper.PermissionMapper;
import com.grain.system.module.system.mapper.RoleMapper;
import com.grain.system.module.system.mapper.RolePermissionMapper;
import com.grain.system.module.system.service.RoleService;
import com.grain.system.module.system.vo.PermissionVO;
import com.grain.system.module.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<RoleVO> listAll() {
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().eq(Role::getStatus, 1).orderByAsc(Role::getId));
        return roles.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public IPage<RoleVO> getRolePage(int page, int size, String keyword) {
        Page<Role> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>().orderByAsc(Role::getId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Role::getRoleName, keyword).or().like(Role::getRoleCode, keyword);
        }
        IPage<Role> result = roleMapper.selectPage(pageParam, wrapper);
        return result.convert(this::toVO);
    }

    @Override
    public void createRole(Role role) {
        long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, role.getRoleCode()));
        if (count > 0) throw new BusinessException("角色编码已存在");
        roleMapper.insert(role);
    }

    @Override
    public void updateRole(Role role) {
        Role existing = roleMapper.selectById(role.getId());
        if (existing == null) throw new BusinessException("角色不存在");
        if (existing.getIsSystem() == 1) {
            // 内置角色只允许改描述和状态，不允许改编码
            existing.setDescription(role.getDescription());
            existing.setStatus(role.getStatus());
            roleMapper.updateById(existing);
        } else {
            roleMapper.updateById(role);
        }
    }

    @Override
    public void deleteRole(Integer id) {
        Role role = roleMapper.selectById(id);
        if (role == null) throw new BusinessException("角色不存在");
        if (role.getIsSystem() == 1) throw new BusinessException("内置角色不允许删除");
        roleMapper.deleteById(id);
        rolePermissionMapper.deleteByRoleId(id);
    }

    @Override
    public List<Integer> getPermissionIds(Integer roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignPermissions(Integer roleId, List<Integer> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            rolePermissionMapper.batchInsert(roleId, permissionIds);
        }
    }

    @Override
    public List<PermissionVO> getFullPermissionTree() {
        List<Permission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().eq(Permission::getStatus, 1).orderByAsc(Permission::getSortOrder));
        return buildTree(all, 0);
    }

    private List<PermissionVO> buildTree(List<Permission> all, int parentId) {
        return all.stream()
                .filter(p -> p.getParentId() == parentId)
                .map(p -> {
                    PermissionVO vo = new PermissionVO();
                    vo.setId(p.getId());
                    vo.setParentId(p.getParentId());
                    vo.setPermissionName(p.getPermissionName());
                    vo.setPermissionCode(p.getPermissionCode());
                    vo.setPermissionType(p.getPermissionType());
                    vo.setRoutePath(p.getRoutePath());
                    vo.setComponent(p.getComponent());
                    vo.setIcon(p.getIcon());
                    vo.setSortOrder(p.getSortOrder());
                    vo.setChildren(buildTree(all, p.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private RoleVO toVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setIsSystem(role.getIsSystem());
        vo.setStatus(role.getStatus());
        return vo;
    }
}
