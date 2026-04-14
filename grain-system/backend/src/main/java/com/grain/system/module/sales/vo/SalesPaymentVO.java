package com.grain.system.module.sales.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesPaymentVO {

    private Integer id;

    private String paymentNo;

    @JsonProperty("salesOrderId")
    private Integer salesOrderId;

    @JsonProperty("orderNo")
    private String orderNo;

    @JsonProperty("orgId")
    private Integer orgId;

    @JsonProperty("orgName")
    private String orgName;

    @JsonProperty("totalReceivable")
    private BigDecimal totalReceivable;

    @JsonProperty("receivedAmount")
    private BigDecimal receivedAmount;

    @JsonProperty("unreceivedAmount")
    private BigDecimal unreceivedAmount;

    private Integer status;

    @JsonProperty("statusName")
    private String statusName;

    @JsonProperty("isLocked")
    private Integer isLocked;

    @JsonProperty("createTime")
    private LocalDateTime createTime;

    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
}
