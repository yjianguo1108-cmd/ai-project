package com.grain.system.module.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchasePaymentVO {

    private Integer id;

    private String paymentNo;

    private Integer orderId;

    private String orderNo;

    private BigDecimal orderTotalAmount;

    private BigDecimal orderPaidAmount;

    private BigDecimal orderUnpaidAmount;

    private Integer farmerId;

    private String farmerName;

    private BigDecimal payAmount;

    private Integer payMethod;

    private String payMethodName;

    private String flowNo;

    private LocalDateTime payTime;

    private String remark;

    private Integer operatorId;

    private String operatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}