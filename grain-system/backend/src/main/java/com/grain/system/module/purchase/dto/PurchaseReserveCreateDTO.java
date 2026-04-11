package com.grain.system.module.purchase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseReserveCreateDTO {

    @NotNull(message = "农户ID不能为空")
    private Integer farmerId;

    @NotNull(message = "粮食品种ID不能为空")
    private Integer grainId;

    @NotNull(message = "预估重量不能为空")
    private BigDecimal estimatedWeight;

    @NotNull(message = "预约收购时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reserveTime;
}