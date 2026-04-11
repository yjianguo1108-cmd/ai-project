package com.grain.system.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_purchase_reserve")
public class PurchaseReserve {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String reserveNo;

    private Integer farmerId;

    private Integer grainId;

    private BigDecimal estimatedWeight;

    private LocalDateTime reserveTime;

    /** 状态: 0待审核/1已审核/2已排期/3已取消 */
    private Integer status;

    private String rejectReason;

    private Integer auditUserId;

    private LocalDateTime auditTime;

    private Integer createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}