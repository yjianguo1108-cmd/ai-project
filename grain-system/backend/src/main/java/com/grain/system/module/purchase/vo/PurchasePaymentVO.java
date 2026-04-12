package com.grain.system.module.purchase.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchasePaymentVO {

    private Integer id;

    private String paymentNo;

    private Integer orderId;

    private String orderNo;

    @JsonProperty("orderTotalAmount")
    private BigDecimal orderTotalAmount;

    @JsonProperty("orderPaidAmount")
    private BigDecimal orderPaidAmount;

    @JsonProperty("orderUnpaidAmount")
    private BigDecimal orderUnpaidAmount;

    private Integer farmerId;

    private String farmerName;

    @JsonProperty("paymentAmount")
    private BigDecimal payAmount;

    private Integer payMethod;

    @JsonProperty("paymentMethodName")
    private String payMethodName;

    @JsonProperty("paymentAccount")
    private String paymentAccount;

    private String recipientName;

    private String flowNo;

    @JsonProperty("paymentTime")
    private LocalDateTime payTime;

    private String remark;

    private Integer operatorId;

    @JsonProperty("operatorName")
    private String operatorName;

    @JsonProperty("createTime")
    private LocalDateTime createTime;

    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
}