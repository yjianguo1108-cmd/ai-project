package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_permission")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer parentId;
    private String permissionName;
    private String permissionCode;

    /** 类型 0目录/1菜单/2按钮 */
    private Integer permissionType;
    private String routePath;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
