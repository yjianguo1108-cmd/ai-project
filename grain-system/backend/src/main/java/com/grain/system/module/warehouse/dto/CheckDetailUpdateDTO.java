package com.grain.system.module.warehouse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckDetailUpdateDTO {

    @NotNull(message = "实盘数量不能为空")
    private BigDecimal actualQty;

    private List<String> photoUrls;
}
