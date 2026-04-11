package com.grain.system.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.system.module.system.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Delete("DELETE FROM t_user_role WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
