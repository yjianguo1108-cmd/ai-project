package com.grain.system.module.sales.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesOrderCreateDTO {

    @NotNull(message = "下游机构ID不能为空")
    private Integer orgId;

    private List<OrderItemDTO> items;

    private String remark;

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "粮食品种ID不能为空")
        @Positive(message = "粮食品种ID必须为正数")
        private Integer grainId;

        @NotNull(message = "储位ID不能为空")
        @Positive(message = "储位ID必须为正数")
        private Integer positionId;

        @NotNull(message = "计划重量不能为空")
        @Positive(message = "计划重量必须大于0")
        private BigDecimal planWeight;

        @NotNull(message = "实际单价不能为空")
        private BigDecimal actualPrice;
    }
}
