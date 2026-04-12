package com.grain.system.module.purchase.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderVO {

    private Integer id;

    private String orderNo;

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

    @JsonProperty("weighRecordId")
    private Integer weighRecordId;

    @JsonProperty("weighNo")
    private String weighNo;

    @JsonProperty("actualWeight")
    private BigDecimal actualWeight;

    @JsonProperty("refPrice")
    private BigDecimal refPrice;

    @JsonProperty("actualPrice")
    private BigDecimal actualPrice;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("paidAmount")
    private BigDecimal paidAmount;

    @JsonProperty("unpaidAmount")
    private BigDecimal unpaidAmount;

    private Integer paymentStatus;

    @JsonProperty("paymentStatusName")
    private String paymentStatusName;

    private Integer status;

    @JsonProperty("statusName")
    private String statusName;

    @JsonProperty("voidReason")
    private String voidReason;

    @JsonProperty("attachmentUrl")
    private String attachmentUrl;

    @JsonProperty("createUserId")
    private Integer createUserId;

    @JsonProperty("createUserName")
    private String createUserName;

    @JsonProperty("createTime")
    private LocalDateTime createTime;

    @JsonProperty("auditUserId")
    private Integer auditUserId;

    @JsonProperty("auditUserName")
    private String auditUserName;

    @JsonProperty("auditTime")
    private LocalDateTime auditTime;

    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
}
