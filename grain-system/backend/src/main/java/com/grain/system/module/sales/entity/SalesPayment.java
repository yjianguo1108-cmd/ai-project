package com.grain.system.module.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_sales_payment")
public class SalesPayment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String paymentNo;

    private Integer salesOrderId;

    private Integer orgId;

    private BigDecimal totalReceivable;

    private BigDecimal receivedAmount;

    private Integer status;

    private Integer isLocked;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
