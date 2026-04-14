package com.grain.system.module.warehouse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OutboundCreateDTO {

    @NotNull(message = "粮食品种不能为空")
    private Integer grainId;

    @NotNull(message = "储位不能为空")
    private Integer positionId;

    @NotNull(message = "出库重量不能为空")
    @Positive(message = "出库重量必须大于0")
    private BigDecimal outWeight;

    private String outboundReason;
}
