package com.grain.system.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;

    private Integer farmerId;

    private Integer grainId;

    private Integer weighRecordId;

    private BigDecimal actualWeight;

    private BigDecimal refPrice;

    private BigDecimal actualPrice;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    /** 付款状态: 0未付款/1部分付款/2已付款 */
    private Integer paymentStatus;

    /** 单据状态: 0草稿/1待审核/2审核通过/3已完成/4已作废 */
    private Integer status;

    private String voidReason;

    private String attachmentUrl;

    private Integer createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer auditUserId;

    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}