package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_role")
public class UserRole {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer roleId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer createUserId;
}
