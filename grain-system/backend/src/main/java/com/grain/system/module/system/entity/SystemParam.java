package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_system_param")
public class SystemParam {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String paramKey;
    private String paramValue;
    private String paramDesc;
    private String paramGroup;
    private Integer isEditable;
    private Integer updateUserId;
    private LocalDateTime updateTime;
}
