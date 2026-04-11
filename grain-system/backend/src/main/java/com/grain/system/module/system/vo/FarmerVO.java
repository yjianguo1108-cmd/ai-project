package com.grain.system.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FarmerVO {
    private Integer id;
    private Integer userId;
    private String realName;     // 关联用户的真实姓名
    private String phone;
    private String phoneMask;
    private String idCard;
    private String idCardMask;
    private String bankCard;
    private String bankCardMask;
    private String bankName;
    private String preferredGrainTypes;
    private Integer auditStatus;
    private LocalDateTime createTime;
}
