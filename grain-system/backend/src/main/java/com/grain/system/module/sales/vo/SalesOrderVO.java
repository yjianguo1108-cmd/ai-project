package com.grain.system.module.sales.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesOrderVO {

    private Integer id;

    private String orderNo;

    @JsonProperty("outboundNo")
    private String outboundNo;

    @JsonProperty("salesOrderId")
    private Integer salesOrderId;

    @JsonProperty("orgId")
    private Integer orgId;

    @JsonProperty("orgName")
    private String orgName;

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

    @JsonProperty("planWeight")
    private BigDecimal planWeight;

    @JsonProperty("actualOutWeight")
    private BigDecimal actualOutWeight;

    @JsonProperty("refPrice")
    private BigDecimal refPrice;

    @JsonProperty("actualPrice")
    private BigDecimal actualPrice;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("actualAmount")
    private BigDecimal actualAmount;

    @JsonProperty("receivedAmount")
    private BigDecimal receivedAmount;

    @JsonProperty("unreceivedAmount")
    private BigDecimal unreceivedAmount;

    @JsonProperty("reservedStock")
    private BigDecimal reservedStock;

    private Integer status;

    @JsonProperty("statusName")
    private String statusName;

    @JsonProperty("outStatus")
    private Integer outStatus;

    @JsonProperty("outStatusName")
    private String outStatusName;

    @JsonProperty("paymentStatus")
    private Integer paymentStatus;

    @JsonProperty("paymentStatusName")
    private String paymentStatusName;

    @JsonProperty("voidReason")
    private String voidReason;

    @JsonProperty("attachmentUrl")
    private String attachmentUrl;

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
