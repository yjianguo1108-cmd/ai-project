package com.grain.system.module.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryLedgerVO {

    private Integer id;

    @JsonProperty("grainId")
    private Integer grainId;

    @JsonProperty("grainType")
    private String grainType;

    @JsonProperty("grainGrade")
    private String grainGrade;

    @JsonProperty("storagePositionId")
    private Integer storagePositionId;

    @JsonProperty("storagePositionName")
    private String storagePositionName;

    @JsonProperty("currentWeight")
    private BigDecimal currentWeight;

    @JsonProperty("refPrice")
    private BigDecimal refPrice;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("inTotalWeight")
    private BigDecimal inTotalWeight;

    @JsonProperty("outTotalWeight")
    private BigDecimal outTotalWeight;

    @JsonProperty("lastInTime")
    private LocalDateTime lastInTime;

    @JsonProperty("lastOutTime")
    private LocalDateTime lastOutTime;

    @JsonProperty("createTime")
    private LocalDateTime createTime;

    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
}
