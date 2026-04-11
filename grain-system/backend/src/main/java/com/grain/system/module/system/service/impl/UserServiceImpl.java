package com.grain.system.module.system.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.constant.RedisKey;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.common.util.AesUtil;
import com.grain.system.module.system.dto.UserCreateDTO;
import com.grain.system.module.system.dto.UserUpdateDTO;
import com.grain.system.module.system.entity.User;
import com.grain.system.module.system.entity.UserRole;
import com.grain.system.module.system.mapper.RoleMapper;
import com.grain.system.module.system.mapper.UserMapper;
import com.grain.system.module.system.mapper.UserRoleMapper;
import com.grain.system.module.system.service.UserService;
import com.grain.system.module.system.vo.RoleVO;
import com.grain.system.module.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Override
    public IPage<UserVO> getUserPage(int page, int size, String keyword, Integer roleId, Integer status) {
        Page<UserVO> pageParam = new Page<>(page, size);
        IPage<UserVO> result = userMapper.selectUserPage(pageParam, keyword, roleId, status);
        result.getRecords().forEach(vo -> {
            if (vo.getPhone() != null) {
                vo.setPhoneMask(AesUtil.maskPhone(vo.getPhone()));
                vo.setPhone(null);
            }
        });
        return result;
    }

    @Override
    public UserVO getUserById(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone()); // 返回原手机号供编辑使用
        vo.setPhoneMask(AesUtil.maskPhone(user.getPhone()));
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        
        // 获取角色列表
        List<com.grain.system.module.system.entity.Role> userRoles = roleMapper.selectRolesByUserId(id);
        List<RoleVO> roles = userRoles.stream().map(role -> {
            RoleVO rvo = new RoleVO();
            rvo.setId(role.getId());
            rvo.setRoleName(role.getRoleName());
            rvo.setRoleCode(role.getRoleCode());
            return rvo;
        }).toList();
        vo.setRoles(roles);
        
        return vo;
    }

    @Override
    @Transactional
    public void createUser(UserCreateDTO dto, Integer operatorId) {
        checkUsernameUnique(dto.getUsername(), null);
        if (dto.getPhone() != null) checkPhoneUnique(dto.getPhone(), null);

        String initPwd = RandomUtil.randomString(
                "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz0123456789@#$", 10);
        log.info("新建用户[{}]初始密码: {}", dto.getUsername(), initPwd);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(initPwd));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setMustChangePwd(1);
        user.setIsDeleted(0);
        user.setLoginFailCount(0);
        userMapper.insert(user);

        assignRoles(user.getId(), dto.getRoleIds(), operatorId);
    }

    @Override
    @Transactional
    public void updateUser(Integer id, UserUpdateDTO dto, Integer operatorId) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        if (dto.getPhone() != null && !dto.getPhone().equals(user.getPhone())) {
            checkPhoneUnique(dto.getPhone(), id);
            user.setPhone(dto.getPhone());
        }
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        userMapper.updateById(user);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            userRoleMapper.deleteByUserId(id);
            assignRoles(id, dto.getRoleIds(), operatorId);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Integer id, Integer status, Integer operatorId) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);

        if (status == 0) {
            redisTemplate.delete(RedisKey.USER_TOKEN + id);
            redisTemplate.delete(RedisKey.USER_INFO + id);
        }
    }

    @Override
    @Transactional
    public void resetPassword(Integer id, String password, Integer operatorId) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(passwordEncoder.encode(password));
        user.setMustChangePwd(1);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id, Integer operatorId) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) return;
        user.setIsDeleted(1);
        user.setStatus(0);
        userMapper.updateById(user);
        redisTemplate.delete(RedisKey.USER_TOKEN + id);
        redisTemplate.delete(RedisKey.USER_INFO + id);
    }

    @Override
    @Transactional
    public void assignRoles(Integer userId, List<Integer> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        roleIds.forEach(roleId -> {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        });
    }

    private void checkUsernameUnique(String username, Integer excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username).eq(User::getIsDeleted, 0);
        if (excludeId != null) wrapper.ne(User::getId, excludeId);
        if (userMapper.selectCount(wrapper) > 0) throw new BusinessException("账号已存在");
    }

    private void checkPhoneUnique(String phone, Integer excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone).eq(User::getIsDeleted, 0);
        if (excludeId != null) wrapper.ne(User::getId, excludeId);
        if (userMapper.selectCount(wrapper) > 0) throw new BusinessException("手机号已被使用");
    }

    private void assignRoles(Integer userId, List<Integer> roleIds, Integer operatorId) {
        roleIds.forEach(roleId -> {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreateUserId(operatorId);
            userRoleMapper.insert(ur);
        });
    }
}
