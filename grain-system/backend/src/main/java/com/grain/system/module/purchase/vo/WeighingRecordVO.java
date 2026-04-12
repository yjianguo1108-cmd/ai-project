package com.grain.system.module.purchase.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WeighingRecordVO {

    private Integer id;

    private String weighNo;

    @JsonProperty("reserveId")
    private Integer reserveId;

    @JsonProperty("reserveNo")
    private String reserveNo;

    @JsonProperty("weighbridgeId")
    private Integer weighbridgeId;

    @JsonProperty("weighbridgeName")
    private String weighbridgeName;

    @JsonProperty("grossWeight")
    private BigDecimal grossWeight;

    @JsonProperty("tareWeight")
    private BigDecimal tareWeight;

    @JsonProperty("netWeight")
    private BigDecimal netWeight;

    @JsonProperty("weighTime")
    private LocalDateTime weighTime;

    private Integer dataSource;

    @JsonProperty("dataSourceName")
    private String dataSourceName;

    private Integer status;

    @JsonProperty("statusName")
    private String statusName;

    private String remark;

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
