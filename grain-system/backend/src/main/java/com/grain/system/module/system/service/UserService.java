package com.grain.system.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.system.dto.UserCreateDTO;
import com.grain.system.module.system.dto.UserUpdateDTO;
import com.grain.system.module.system.vo.UserVO;

public interface UserService {

    IPage<UserVO> getUserPage(int page, int size, String keyword, Integer roleId, Integer status);

    UserVO getUserById(Integer id);

    void createUser(UserCreateDTO dto, Integer operatorId);

    void updateUser(Integer id, UserUpdateDTO dto, Integer operatorId);

    void updateStatus(Integer id, Integer status, Integer operatorId);

    void resetPassword(Integer id, String password, Integer operatorId);

    void deleteUser(Integer id, Integer operatorId);

    void assignRoles(Integer id, java.util.List<Integer> roleIds);
}
