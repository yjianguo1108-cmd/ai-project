package com.grain.system.module.purchase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WeighingRecordCreateDTO {

    private Integer reserveId;

    private Integer weighbridgeId;

    @NotNull(message = "毛重不能为空")
    private BigDecimal grossWeight;

    @NotNull(message = "皮重不能为空")
    private BigDecimal tareWeight;

    @NotNull(message = "称重时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime weighTime;

    @NotNull(message = "数据来源不能为空")
    private Integer dataSource;

    private String remark;
}