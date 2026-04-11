package com.grain.system.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.module.system.entity.User;
import com.grain.system.module.system.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户列表（带角色信息）
     */
    IPage<UserVO> selectUserPage(Page<UserVO> page,
                                  @Param("keyword") String keyword,
                                  @Param("roleId") Integer roleId,
                                  @Param("status") Integer status);
}
