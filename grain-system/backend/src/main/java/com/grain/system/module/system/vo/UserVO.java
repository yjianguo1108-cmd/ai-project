package com.grain.system.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Integer id;
    private String username;
    private String realName;
    private String phone;
    private String phoneMask;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private List<RoleVO> roles;
}
