package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    @TableField(select = false)  // 查询时默认不返回密码
    private String password;

    private String realName;
    private String phone;
    private String email;
    /** 状态 0禁用/1正常/2锁定 */
    private Integer status;

    /** 首次登录强制改密 */
    private Integer mustChangePwd;

    private Integer loginFailCount;
    private LocalDateTime lockTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
