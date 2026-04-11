package com.grain.system.module.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderVO {

    private Integer id;

    private String orderNo;

    private Integer farmerId;

    private String farmerName;

    private String farmerPhone;

    private Integer grainId;

    private String grainType;

    private String grainGrade;

    private Integer weighRecordId;

    private String weighNo;

    private BigDecimal actualWeight;

    private BigDecimal refPrice;

    private BigDecimal actualPrice;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private BigDecimal unpaidAmount;

    private Integer paymentStatus;

    private String paymentStatusName;

    private Integer status;

    private String statusName;

    private String voidReason;

    private String attachmentUrl;

    private Integer createUserId;

    private String createUserName;

    private LocalDateTime createTime;

    private Integer auditUserId;

    private String auditUserName;

    private LocalDateTime auditTime;

    private LocalDateTime updateTime;
}