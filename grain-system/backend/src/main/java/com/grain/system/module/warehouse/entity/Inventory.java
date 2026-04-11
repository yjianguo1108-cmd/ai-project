package com.grain.system.module.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_inventory")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer grainId;

    private Integer positionId;

    private BigDecimal currentStock;

    private BigDecimal bookStock;

    private BigDecimal reservedStock;

    private LocalDateTime earliestInboundTime;

    private Integer qualityStatus;

    private Integer status;

    private LocalDateTime lastCheckTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}