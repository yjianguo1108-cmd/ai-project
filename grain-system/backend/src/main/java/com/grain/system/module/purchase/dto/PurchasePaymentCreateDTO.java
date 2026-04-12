package com.grain.system.module.purchase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchasePaymentCreateDTO {

    @NotNull(message = "收购单ID不能为空")
    private Integer orderId;

    private Integer farmerId;

    @NotNull(message = "付款金额不能为空")
    @JsonProperty("paymentAmount")
    private BigDecimal payAmount;

    @NotNull(message = "付款方式不能为空")
    @JsonProperty("paymentMethod")
    private Integer payMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("paymentTime")
    private LocalDateTime payTime;

    @JsonProperty("paymentAccount")
    private String paymentAccount;

    @JsonProperty("recipientName")
    private String recipientName;

    private String remark;
}