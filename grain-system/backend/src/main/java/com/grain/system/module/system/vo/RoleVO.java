package com.grain.system.module.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO {
    private Integer id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer isSystem;
    private Integer status;
    private List<PermissionVO> permissions;
}
