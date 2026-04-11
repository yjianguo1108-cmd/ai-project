package com.grain.system.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrgVO {
    private Integer id;
    private Integer userId;
    private String orgName;
    private Integer orgType;
    private String orgTypeName;
    private String contactName;
    private String phone;
    private String address;
    private String credentialUrl;
    private Integer auditStatus;
    private String auditStatusName;
    private String rejectReason;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
