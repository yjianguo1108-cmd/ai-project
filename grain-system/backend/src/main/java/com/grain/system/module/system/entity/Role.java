package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String roleName;
    private String roleCode;
    private String description;

    /** 是否内置角色(不可删除) */
    private Integer isSystem;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
