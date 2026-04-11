package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_farmer")
public class Farmer {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;

    /** 身份证（AES加密存储） */
    private String idCard;
    /** 身份证脱敏展示 */
    private String idCardMask;
    /** 身份证SHA-256哈希（唯一索引） */
    private String idCardHash;

    /** 银行卡（AES加密存储） */
    private String bankCard;
    private String bankCardMask;
    private String bankName;

    /** 常售粮食种类(JSON数组) */
    private String preferredGrainTypes;
    private String remark;

    /** 档案状态 0待审核/1正常/2冻结 */
    private Integer auditStatus;
    private Integer createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
