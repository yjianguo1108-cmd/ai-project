package com.grain.system.module.sales.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentReceiveDTO {

    @NotNull(message = "收款金额不能为空")
    @Positive(message = "收款金额必须大于0")
    private BigDecimal receiveAmount;

    @NotNull(message = "收款方式不能为空")
    private Integer receiveMethod;

    private LocalDateTime receiveTime;

    private String flowNo;

    private String bankInfo;

    private String remark;
}
