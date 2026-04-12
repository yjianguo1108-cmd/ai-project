package com.grain.system.module.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InboundOrderVO {

    private Integer id;

    private String inboundNo;

    @JsonProperty("purchaseOrderId")
    private Integer purchaseOrderId;

    @JsonProperty("purchaseOrderNo")
    private String purchaseOrderNo;

    @JsonProperty("grainId")
    private Integer grainId;

    @JsonProperty("grainType")
    private String grainType;

    @JsonProperty("grainGrade")
    private String grainGrade;

    @JsonProperty("positionId")
    private Integer positionId;

    @JsonProperty("positionName")
    private String positionName;

    @JsonProperty("netWeight")
    private BigDecimal netWeight;

    @JsonProperty("inboundType")
    private Integer inboundType;

    @JsonProperty("inboundReason")
    private String inboundReason;

    @JsonProperty("attachmentUrl")
    private String attachmentUrl;

    private Integer status;

    @JsonProperty("statusName")
    private String statusName;

    @JsonProperty("confirmUserId")
    private Integer confirmUserId;

    @JsonProperty("confirmUserName")
    private String confirmUserName;

    @JsonProperty("confirmTime")
    private LocalDateTime confirmTime;

    @JsonProperty("createUserId")
    private Integer createUserId;

    @JsonProperty("createUserName")
    private String createUserName;

    @JsonProperty("createTime")
    private LocalDateTime createTime;

    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
}