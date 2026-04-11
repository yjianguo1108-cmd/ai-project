package com.grain.system.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_purchase_payment")
public class PurchasePayment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String paymentNo;

    private Integer orderId;

    private Integer farmerId;

    private BigDecimal payAmount;

    private Integer payMethod;

    private LocalDateTime payTime;

    private String flowNo;

    private Integer operatorId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}