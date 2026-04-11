package com.grain.system.module.purchase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderCreateDTO {

    @NotNull(message = "农户ID不能为空")
    private Integer farmerId;

    @NotNull(message = "粮食品种ID不能为空")
    private Integer grainId;

    @NotNull(message = "称重记录ID不能为空")
    private Integer weighRecordId;

    @NotNull(message = "实际收购重量不能为空")
    private BigDecimal actualWeight;

    @NotNull(message = "实际收购单价不能为空")
    private BigDecimal actualPrice;
}