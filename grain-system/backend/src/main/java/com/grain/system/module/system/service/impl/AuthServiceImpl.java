package com.grain.system.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain.system.common.constant.RedisKey;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.common.util.AesUtil;
import com.grain.system.common.util.JwtUtil;
import com.grain.system.module.system.dto.LoginDTO;
import com.grain.system.module.system.entity.Permission;
import com.grain.system.module.system.entity.User;
import com.grain.system.module.system.mapper.PermissionMapper;
import com.grain.system.module.system.mapper.RoleMapper;
import com.grain.system.module.system.mapper.UserMapper;
import com.grain.system.module.system.service.AuthService;
import com.grain.system.module.system.vo.PermissionVO;
import com.grain.system.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 30;

    @Override
    public Map<String, Object> login(LoginDTO dto, String ip) {
        log.info("用户[{}]尝试从IP[{}]登录", dto.getUsername(), ip);
        // 1. 查找用户 (显式查询密码字段)
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getPassword, User::getRealName, 
                        User::getStatus, User::getMustChangePwd, User::getLoginFailCount, 
                        User::getLockTime, User::getIsDeleted)
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        // 2. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (user.getStatus() == 2) {
            // 检查是否到解锁时间
            if (user.getLockTime() != null && LocalDateTime.now().isBefore(user.getLockTime())) {
                throw new BusinessException("账号已锁定，请" + LOCK_MINUTES + "分钟后再试或联系管理员");
            } else {
                // 自动解锁
                user.setStatus(1);
                user.setLoginFailCount(0);
                userMapper.updateById(user);
            }
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            handleLoginFail(user);
            throw new BusinessException("账号或密码错误");
        }

        log.info("用户[{}]登录成功", dto.getUsername());

        // 4. 登录成功，重置失败次数
        user.setLoginFailCount(0);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        // 5. 查角色和权限
        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(user.getId());
        List<String> permCodes = permissionMapper.selectPermCodesByUserId(user.getId());

        // 6. 生成Token
        String token = jwtUtil.generateToken(user.getId().longValue(), user.getUsername());

        // 7. 存Redis
        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId().longValue())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCodes(new HashSet<>(roleCodes))
                .permissions(new HashSet<>(permCodes))
                .token(token)
                .loginTime(System.currentTimeMillis())
                .loginIp(ip)
                .build();

        try {
            String userJson = objectMapper.writeValueAsString(loginUser);
            redisTemplate.opsForValue().set(
                    RedisKey.USER_TOKEN + user.getId(), token,
                    RedisKey.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(
                    RedisKey.USER_INFO + user.getId(), userJson,
                    RedisKey.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("存储LoginUser到Redis失败", e);
            throw new BusinessException("登录失败，请重试");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("roleCodes", roleCodes);
        result.put("mustChangePwd", user.getMustChangePwd());
        return result;
    }

    @Override
    public void logout(Long userId) {
        redisTemplate.delete(RedisKey.USER_TOKEN + userId);
        redisTemplate.delete(RedisKey.USER_INFO + userId);
        log.info("用户[{}]已登出", userId);
    }

    @Override
    public List<String> getCurrentPermissions(Long userId) {
        return permissionMapper.selectPermCodesByUserId(userId.intValue());
    }

    @Override
    public List<PermissionVO> getCurrentMenuTree(Long userId) {
        List<Permission> perms = permissionMapper.selectMenusByUserId(userId.intValue());
        return buildTree(perms, 0);
    }

    @Override
    public Map<String, Object> getCurrentUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(user.getId());
        List<String> permCodes = permissionMapper.selectPermCodesByUserId(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("phone", user.getPhone());
        result.put("roles", roleCodes);
        result.put("permissions", permCodes);
        result.put("avatar", ""); // 预留头像字段
        return result;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getPassword)
                .eq(User::getId, userId));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePwd(0); // 修改后清除强制改密标记
        userMapper.updateById(user);
    }

    private List<PermissionVO> buildTree(List<Permission> perms, int parentId) {
        return perms.stream()
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
                    vo.setChildren(buildTree(perms, p.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void handleLoginFail(User user) {
        int failCount = user.getLoginFailCount() + 1;
        user.setLoginFailCount(failCount);
        if (failCount >= MAX_FAIL_COUNT) {
            user.setStatus(2);
            user.setLockTime(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            log.warn("用户[{}]连续登录失败{}次，账号已锁定", user.getUsername(), failCount);
        }
        userMapper.updateById(user);
    }
}
