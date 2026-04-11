package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_downstream_org")
public class DownstreamOrg {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String orgName;

    /** 机构类型 0粮库/1加工厂/2贸易商/3其他 */
    private Integer orgType;
    private String contactName;
    private String phone;
    private String address;

    /** 资质文件MinIO路径(JSON数组) */
    private String credentialUrl;

    /** 审核状态 0待审核/1审核通过/2已驳回 */
    private Integer auditStatus;
    private Integer auditUserId;
    private LocalDateTime auditTime;
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
