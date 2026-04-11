package com.grain.system.module.warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryLedgerVO {

    private Integer id;

    private Integer grainId;

    private String grainType;

    private String grainGrade;

    private Integer storagePositionId;

    private String storagePositionName;

    private BigDecimal currentWeight;

    private BigDecimal refPrice;

    private BigDecimal totalAmount;

    private BigDecimal inTotalWeight;

    private BigDecimal outTotalWeight;

    private LocalDateTime lastInTime;

    private LocalDateTime lastOutTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}