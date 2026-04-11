package com.grain.system.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.system.module.system.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询用户的角色列表
     */
    @Select("SELECT r.* FROM t_role r " +
            "INNER JOIN t_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<Role> selectRolesByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户的角色编码集合
     */
    @Select("SELECT r.role_code FROM t_role r " +
            "INNER JOIN t_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Integer userId);
}
