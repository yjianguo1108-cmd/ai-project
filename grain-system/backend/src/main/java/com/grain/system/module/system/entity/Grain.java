package com.grain.system.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_grain")
public class Grain {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String grainType;
    private String grainGrade;
    private BigDecimal refPurchasePrice;
    private BigDecimal refSalePrice;
    private BigDecimal moistureMax;
    private BigDecimal impurityMax;
    private BigDecimal storageTempMin;
    private BigDecimal storageTempMax;
    private BigDecimal storageHumidityMax;
    private BigDecimal lowStockThreshold;
    private Integer maxStorageDays;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
