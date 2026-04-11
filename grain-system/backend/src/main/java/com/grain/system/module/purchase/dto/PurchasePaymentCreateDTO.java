package com.grain.system.module.purchase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchasePaymentCreateDTO {

    @NotNull(message = "收购单ID不能为空")
    private Integer orderId;

    @NotNull(message = "农户ID不能为空")
    private Integer farmerId;

    @NotNull(message = "付款金额不能为空")
    private BigDecimal payAmount;

    @NotNull(message = "付款方式不能为空")
    private Integer payMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    private String remark;
}