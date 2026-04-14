package com.grain.system.module.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_sales_payment_record")
public class SalesPaymentRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer paymentId;

    private Integer salesOrderId;

    private Integer orgId;

    private BigDecimal receiveAmount;

    private Integer receiveMethod;

    private LocalDateTime receiveTime;

    private String flowNo;

    private String bankInfo;

    private String remark;

    private Integer operatorId;

    private LocalDateTime createTime;
}
