package com.grain.system.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.system.module.system.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 查询用户的权限码集合（通过角色）
     */
    @Select("SELECT DISTINCT p.permission_code FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN t_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.permission_code IS NOT NULL AND p.status = 1")
    List<String> selectPermCodesByUserId(@Param("userId") Integer userId);

    /**
     * 查询角色的菜单权限树
     */
    @Select("SELECT p.* FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1 " +
            "ORDER BY p.sort_order")
    List<Permission> selectPermsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 查询用户的菜单权限（用于渲染前端菜单）
     */
    @Select("SELECT DISTINCT p.* FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN t_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 " +
            "AND p.permission_type IN (0, 1) " +
            "ORDER BY p.sort_order")
    List<Permission> selectMenusByUserId(@Param("userId") Integer userId);
}
