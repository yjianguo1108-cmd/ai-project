package com.grain.system.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.system.module.system.entity.RolePermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    @Select("SELECT permission_id FROM t_role_permission WHERE role_id = #{roleId}")
    List<Integer> selectPermissionIdsByRoleId(@Param("roleId") Integer roleId);

    @Delete("DELETE FROM t_role_permission WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Integer roleId);

    @Insert("<script>" +
            "INSERT INTO t_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='permissionIds' item='pid' separator=','>" +
            "(#{roleId}, #{pid})" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("roleId") Integer roleId, @Param("permissionIds") List<Integer> permissionIds);
}
