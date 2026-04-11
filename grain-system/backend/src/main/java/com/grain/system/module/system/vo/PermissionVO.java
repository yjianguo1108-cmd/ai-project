package com.grain.system.module.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class PermissionVO {
    private Integer id;
    private Integer parentId;
    private String permissionName;
    private String permissionCode;
    private Integer permissionType;
    private String routePath;
    private String component;
    private String icon;
    private Integer sortOrder;
    private List<PermissionVO> children;
}
