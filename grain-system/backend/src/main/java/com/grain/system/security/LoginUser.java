package com.grain.system.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户信息（存入Redis + SecurityContext）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String realName;
    private Set<String> roleCodes;
    private Set<String> permissions;  // 权限码集合
    private String token;
    private Long loginTime;
    private String loginIp;
}
