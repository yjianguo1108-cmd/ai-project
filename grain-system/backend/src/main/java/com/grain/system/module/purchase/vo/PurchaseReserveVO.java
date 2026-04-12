package com.grain.system.module.purchase.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseReserveVO {

    private Integer id;

    private String reserveNo;

    @JsonProperty("farmerId")
    private Integer farmerId;

    @JsonProperty("farmerName")
    private String farmerName;

    @JsonProperty("farmerPhone")
    private String farmerPhone;

    @JsonProperty("grainId")
    private Integer grainId;

    @JsonProperty("grainType")
    private String grainType;

    @JsonProperty("grainGrade")
    private String grainGrade;

    @JsonProperty("estimatedWeight")
    private BigDecimal estimatedWeight;

    @JsonProperty("reserveTime")
    private LocalDateTime reserveTime;

    private Integer status;

    @JsonProperty("statusName")
    private String statusName;

    @JsonProperty("rejectReason")
    private String rejectReason;

    @JsonProperty("auditUserId")
    private Integer auditUserId;

    @JsonProperty("auditUserName")
    private String auditUserName;

    @JsonProperty("auditTime")
    private LocalDateTime auditTime;

    @JsonProperty("createUserId")
    private Integer createUserId;

    @JsonProperty("createUserName")
    private String createUserName;

    @JsonProperty("createTime")
    private LocalDateTime createTime;

    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
}
